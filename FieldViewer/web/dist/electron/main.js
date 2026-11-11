"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
const electron_1 = require("electron");
const node_child_process_1 = require("node:child_process");
const node_fs_1 = require("node:fs");
const http = __importStar(require("node:http"));
const path = __importStar(require("node:path"));
// The window is the TypeScript UI; the simulation and robot link run in the Java backend
// (FieldViewer/src/main/java) so they use the exact same pure pursuit code as the robot.
// A private per-launch port prevents a newly-built UI from accidentally reusing a stale Java
// backend left behind by an older Electron process. Browser-only launches still use port 2525.
const requestedPort = Number(process.env.FIELDVIEWER_PORT);
const PORT = Number.isInteger(requestedPort) && requestedPort > 1024 && requestedPort < 65536
    ? requestedPort : 25000 + (process.pid % 10000);
const BACKEND_URL = `http://localhost:${PORT}`;
// dist/electron -> dist -> web -> FieldViewer -> repo root
const WEB_DIST = path.resolve(__dirname, '..');
const FIELD_VIEWER_DIR = path.resolve(__dirname, '..', '..', '..');
const REPO_ROOT = path.resolve(FIELD_VIEWER_DIR, '..');
const WINDOWS = process.platform === 'win32';
let backend = null;
let window = null;
const backendLog = [];
let quitting = false;
let starting = null;
let recentRestarts = [];
// everything the backend prints (and why it stopped) also goes here, for debugging
const LOG_FILE = path.join(FIELD_VIEWER_DIR, 'build', 'fieldviewer-backend.log');
function log(line) {
    backendLog.push(line);
    if (backendLog.length > 40)
        backendLog.shift();
    console.log(`[backend] ${line}`);
    try {
        (0, node_fs_1.mkdirSync)(path.dirname(LOG_FILE), { recursive: true });
        (0, node_fs_1.appendFileSync)(LOG_FILE, `${new Date().toISOString()} ${line}\n`);
    }
    catch {
        // logging must never take the app down
    }
}
function ping() {
    return new Promise((resolve) => {
        const request = http.get(`${BACKEND_URL}/api/health`, (response) => {
            response.resume();
            resolve(response.statusCode === 200);
        });
        request.on('error', () => resolve(false));
        request.setTimeout(1000, () => {
            request.destroy();
            resolve(false);
        });
    });
}
function run(command, args, cwd) {
    const child = (0, node_child_process_1.spawn)(command, args, { cwd, shell: WINDOWS, env: process.env });
    const remember = (data) => {
        for (const line of data.toString().split(/\r?\n/)) {
            if (line.trim()) {
                log(line);
            }
        }
    };
    child.stdout?.on('data', remember);
    child.stderr?.on('data', remember);
    return child;
}
function waitForExit(child) {
    return new Promise((resolve) => child.on('exit', (code) => resolve(code ?? 1)));
}
async function waitForBackend(timeoutMs) {
    const start = Date.now();
    while (Date.now() - start < timeoutMs) {
        if (await ping())
            return true;
        if (backend && backend.exitCode !== null)
            return false;
        await new Promise((r) => setTimeout(r, 300));
    }
    return false;
}
const SCRIPT = path.join(FIELD_VIEWER_DIR, 'build', 'install', 'FieldViewer', 'bin', WINDOWS ? 'FieldViewer.bat' : 'FieldViewer');
/**
 * Uses a backend that's already running, or builds the Java side with Gradle and starts it.
 * Safe to call again at any time; only one start runs at once.
 */
function ensureBackend(build, showProgress) {
    if (!starting) {
        starting = startBackend(build, showProgress).finally(() => {
            starting = null;
        });
    }
    return starting;
}
async function startBackend(build, showProgress) {
    if (await ping()) {
        return; // e.g. started with ./gradlew :FieldViewer:run
    }
    if (build || !(0, node_fs_1.existsSync)(SCRIPT)) {
        if (showProgress)
            setStatus('Building the simulator…');
        const gradlew = WINDOWS ? 'gradlew.bat' : './gradlew';
        const gradle = run(gradlew, [':FieldViewer:installDist', '-q', '--console=plain'], REPO_ROOT);
        if ((await waitForExit(gradle)) !== 0) {
            throw new Error('Gradle build failed');
        }
    }
    if (showProgress)
        setStatus('Starting the simulator…');
    if (!(0, node_fs_1.existsSync)(SCRIPT)) {
        throw new Error(`missing ${SCRIPT}`);
    }
    const child = run(SCRIPT, ['--no-browser', '--port', String(PORT), '--web-root', WEB_DIST], FIELD_VIEWER_DIR);
    backend = child;
    child.on('exit', (code, signal) => {
        if (!quitting) {
            log(`backend stopped unexpectedly (exit code ${code}, signal ${signal}); restarting`);
        }
    });
    if (!(await waitForBackend(60_000))) {
        throw new Error('the Java backend did not start');
    }
}
/** Keeps the backend alive: if it stops answering, start a new one (the page reconnects on its own). */
function superviseBackend() {
    setInterval(async () => {
        if (quitting || starting || await ping()) {
            return;
        }
        if (backend && backend.exitCode === null && backend.signalCode === null) {
            return; // ours is still alive, maybe just busy
        }
        const now = Date.now();
        recentRestarts = recentRestarts.filter((t) => now - t < 60_000);
        if (recentRestarts.length >= 5) {
            return; // something is badly wrong; the page shows the error, don't loop forever
        }
        recentRestarts.push(now);
        ensureBackend(false, false).catch((e) => log(`restart failed: ${e.message}`));
    }, 3000);
}
function setStatus(text) {
    void window?.webContents.executeJavaScript(`document.getElementById('status') && (document.getElementById('status').textContent = ${JSON.stringify(text)})`);
}
async function createWindow() {
    window = new electron_1.BrowserWindow({
        width: 1480,
        height: 920,
        minWidth: 1000,
        minHeight: 640,
        title: 'FieldViewer',
        backgroundColor: '#0e0919',
        titleBarStyle: process.platform === 'darwin' ? 'hiddenInset' : 'default',
        autoHideMenuBar: true,
        webPreferences: {
            contextIsolation: true,
            nodeIntegration: false,
            sandbox: true,
        },
    });
    // links open in the real browser; the window itself only ever shows the local UI
    window.webContents.setWindowOpenHandler(({ url }) => {
        void electron_1.shell.openExternal(url);
        return { action: 'deny' };
    });
    window.webContents.on('will-navigate', (event, url) => {
        if (!url.startsWith(BACKEND_URL) && !url.startsWith('file:')) {
            event.preventDefault();
        }
    });
    // if the page ever crashes, note why and bring it back instead of leaving a blank window
    window.webContents.on('render-process-gone', (_event, details) => {
        log(`window crashed (${details.reason}); reloading`);
        setTimeout(() => void window?.loadURL(BACKEND_URL), 500);
    });
    await window.loadFile(path.join(WEB_DIST, 'loading.html'));
    try {
        await ensureBackend(true, true);
        await window.loadURL(BACKEND_URL);
        superviseBackend();
    }
    catch (e) {
        const message = `${e.message}\n\n${backendLog.slice(-15).join('\n')}`;
        await window.loadFile(path.join(WEB_DIST, 'loading.html'), { query: { error: message } });
    }
}
function stopBackend() {
    quitting = true;
    if (backend && backend.exitCode === null) {
        if (WINDOWS && backend.pid) {
            (0, node_child_process_1.spawn)('taskkill', ['/pid', String(backend.pid), '/T', '/F']);
        }
        else {
            backend.kill();
        }
    }
}
electron_1.app.whenReady().then(createWindow).catch((e) => console.error(e));
electron_1.app.on('window-all-closed', () => electron_1.app.quit());
electron_1.app.on('before-quit', stopBackend);
process.on('exit', stopBackend);
