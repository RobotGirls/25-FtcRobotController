import { connectLive, disconnectLive, fetchDefaults, fetchLimelightPipeline, fetchLimelightPipelineConfig, fetchRecordings, fetchRoutes, saveLimelightPipelineConfig, saveRecording, setLimelightPipeline, simulateRoute, simulateSketch, subscribeLive, } from './api.js';
import { emptySeries, TelemetryChart } from './chart.js';
import { FieldView } from './field.js';
import { download, parseRecording, Recorder, recordingToSim, slug } from './recording.js';
import { freePoint, snapPoint } from './snap.js';
import { TuningPanel } from './tuning.js';
import { F, } from './types.js';
const SKETCH = '__sketch__';
const REC = 'rec:';
const state = {
    mode: 'sim',
    liveLayout: 'field',
    holonomic: false,
    routes: [],
    selected: '',
    sim: null,
    simTrail: [],
    series: emptySeries(),
    t: 0,
    playing: false,
    speed: 1,
    request: 0,
    sketch: {
        name: '',
        points: [],
        reversed: false,
        speed: 0.6,
        lookahead: 16,
        /** degrees, or null when not set */
        startHeading: null,
        endHeading: null,
        fixedHeading: null,
    },
    selectedPoint: -1,
    history: { past: [], future: [] },
    drag: -1,
    hover: -1,
    mouse: null,
    measuring: false,
    ruler: null,
    snap: loadPref('snap', true),
    snapGuides: [],
    snapPreview: null,
    follow: false,
    ghost: null,
    graphs: loadPref('graphs', true),
    layers: { lookahead: true, trail: true, route: true, keepOut: true, grid: true },
    recordings: new Map(),
    live: {
        status: null,
        frame: null,
        prev: null,
        latest: null,
        at: 0,
        interval: 50,
        trail: [],
        route: '',
        series: emptySeries(),
        start: 0,
        speed: 0,
    },
};
let field;
let chart;
let tuning;
const recorder = new Recorder();
let dirty = true;
let backendDown = false;
let lastTick = performance.now();
let sketchTimer = 0;
let tuningTimer = 0;
let limelightRetry = 0;
let limelightPipelineAttempted = false;
/** What a mouse drag on the field is doing right now. */
let gesture = null;
const $ = (id) => document.getElementById(id);
const canvas = $('field');
const wrap = $('canvas-wrap');
// ---- startup ----------------------------------------------------------------------------------
async function init() {
    // the page itself never scrolls (only its panels do); undo it if focus ever tries
    window.addEventListener('scroll', () => window.scrollTo(0, 0));
    if (navigator.userAgent.includes('Electron') && navigator.userAgent.includes('Macintosh')) {
        document.body.classList.add('electron-mac');
    }
    for (;;) {
        try {
            const [data, defaults, saved] = await Promise.all([fetchRoutes(), fetchDefaults(), fetchRecordings()]);
            state.routes = data.routes;
            $('recording-location').title = saved.directory;
            for (const item of saved.recordings) {
                addRecording(item.recording, item.name, false);
            }
            field = new FieldView(canvas, data.field);
            field.onChange = invalidate;
            tuning = new TuningPanel($('tuning'), defaults, onTuningChanged);
            break;
        }
        catch {
            $('backend-banner').hidden = false;
            await sleep(1000);
        }
    }
    $('backend-banner').hidden = true;
    chart = new TelemetryChart($('chart'));
    chart.onSeek = (t) => {
        setPlaying(false);
        seek(t);
    };
    setGraphs(state.graphs);
    updateTuningBadge();
    buildRouteList();
    wireControls();
    wireToolbar();
    wireCanvas();
    wireKeys();
    subscribeLive(onLiveFrame, onLiveStatus);
    void selectRoute(state.routes[0]?.name ?? SKETCH);
    requestAnimationFrame(tick);
}
// ---- route list -------------------------------------------------------------------------------
function buildRouteList() {
    const list = $('route-list');
    const query = $('search').value.trim().toLowerCase();
    list.replaceChildren();
    let group = '';
    const addGroup = (name) => {
        if (name !== group) {
            group = name;
            list.append(el('div', 'route-group', name));
        }
    };
    for (const route of state.routes) {
        if (query && !`${route.name} ${route.description}`.toLowerCase().includes(query))
            continue;
        addGroup(route.group);
        const item = routeItem(route.name, route.alliance, route.name.replace(/^TEST /, ''), describeSteps(route.steps));
        item.append(route.requiresHolonomic ? el('span', 'tag', 'mecanum') : el('span'));
        list.append(item);
    }
    for (const [id, recording] of state.recordings) {
        const name = recording.route || 'Unknown route';
        if (query && !name.toLowerCase().includes(query))
            continue;
        addGroup('Recordings');
        const seconds = recording.frames[recording.frames.length - 1].t - recording.frames[0].t;
        const planned = state.routes.find((r) => r.name === recording.route);
        const item = routeItem(id, planned?.alliance ?? 'NONE', name, `${new Date(recording.recordedAt).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })} · ${seconds.toFixed(1)} s`);
        item.append(el('span', 'rec', 'REC'));
        list.append(item);
    }
    if (!list.children.length) {
        list.append(el('div', 'route-group', 'No matching routes'));
    }
}
function routeItem(id, alliance, name, meta) {
    const item = el('button', 'route-item');
    item.dataset.route = id;
    item.classList.toggle('active', id === state.selected);
    item.append(el('span', `dot ${alliance}`));
    const text = el('span');
    text.append(el('span', 'name', name), el('span', 'meta', meta));
    item.append(text);
    item.addEventListener('click', () => void selectRoute(id));
    return item;
}
function describeSteps(steps) {
    const paths = steps.filter((s) => s.type === 'follow').length;
    const markers = steps.filter((s) => s.type === 'marker').length;
    const turns = steps.filter((s) => s.type === 'turn').length;
    const parts = [];
    if (paths)
        parts.push(`${paths} path${paths > 1 ? 's' : ''}`);
    if (turns)
        parts.push(`${turns} turn${turns > 1 ? 's' : ''}`);
    if (markers)
        parts.push(`${markers} marker${markers > 1 ? 's' : ''}`);
    return parts.join(' · ');
}
async function selectRoute(name) {
    if (state.mode === 'live') {
        setMode('sim');
    }
    state.selected = name;
    const sketching = name === SKETCH;
    document.querySelectorAll('.route-item').forEach((item) => item.classList.toggle('active', item.dataset.route === name));
    $('new-sketch').classList.toggle('active', sketching);
    $('sketch-card').hidden = !sketching;
    wrap.classList.toggle('sketching', sketching);
    updateSketchHint();
    if (name.startsWith(REC)) {
        const recording = state.recordings.get(name);
        if (recording) {
            state.request++;
            loadSim(recordingToSim(recording, state.routes));
        }
    }
    else if (sketching) {
        state.request++;
        loadSim(null);
        showRouteCard(null);
        await runSketch();
    }
    else {
        const route = state.routes.find((r) => r.name === name);
        showRouteCard(route ?? null);
        await runSim(() => simulateRoute(name, state.holonomic, tuning.tuning()));
    }
}
// ---- simulation -------------------------------------------------------------------------------
async function runSim(request) {
    const id = ++state.request;
    try {
        const result = await request();
        if (id !== state.request) {
            return; // a newer request already went out
        }
        loadSim(result);
    }
    catch (e) {
        if (id !== state.request) {
            return;
        }
        if (e instanceof TypeError) {
            void onBackendLost(); // fetch itself failed: the backend isn't running
        }
        else {
            toast(e.message, true);
        }
    }
}
/** Reruns whatever is selected (after a tuning or drivetrain change), keeping the playhead. */
async function rerun() {
    if (state.selected.startsWith(REC))
        return;
    const t = state.t;
    const wasPlaying = state.playing;
    if (state.selected === SKETCH) {
        await runSketch();
    }
    else {
        await runSim(() => simulateRoute(state.selected, state.holonomic, tuning.tuning()));
    }
    if (state.sim) {
        seek(Math.min(t, duration()));
        setPlaying(wasPlaying || state.t < duration());
    }
}
/**
 * The Java backend stopped answering (the Electron app restarts it). Stop showing the old
 * run, wait for it to come back, then run whatever is selected again.
 */
async function onBackendLost() {
    if (backendDown) {
        return;
    }
    backendDown = true;
    $('backend-banner').hidden = false;
    loadSim(null);
    while (!(await healthy())) {
        await sleep(1000);
    }
    backendDown = false;
    $('backend-banner').hidden = true;
    toast('Simulator reconnected');
    void selectRoute(state.selected);
}
async function healthy() {
    try {
        return (await fetch('/api/health', { cache: 'no-store' })).ok;
    }
    catch {
        return false;
    }
}
function loadSim(result) {
    state.sim = result;
    state.simTrail = result ? result.frames.map((f) => [f[F.X], f[F.Y]]) : [];
    state.series = result ? seriesFromFrames(result.frames) : emptySeries();
    state.t = 0;
    buildSegments();
    showResults();
    if (result || state.selected === SKETCH) {
        showRouteCard(state.selected === SKETCH ? null : result?.route ?? null);
    }
    $('sk-java').textContent = result?.java ?? '';
    setPlaying(Boolean(result));
    invalidate();
}
function seriesFromFrames(frames) {
    const series = emptySeries();
    for (const f of frames) {
        series.t.push(f[F.T]);
        series.fwd.push(f[F.FWD]);
        series.left.push(f[F.LEFT]);
        series.turn.push(f[F.TURN]);
        series.speed.push(f[F.SPEED] ?? 0);
    }
    return series;
}
function duration() {
    const frames = state.sim?.frames;
    return frames && frames.length ? frames[frames.length - 1][F.T] : 0;
}
function frameIndex() {
    const sim = state.sim;
    if (!sim)
        return 0;
    return Math.max(0, Math.min(sim.frames.length - 1, Math.floor(state.t / sim.dt + 1e-6)));
}
function setPlaying(playing) {
    if (playing && state.sim && state.t >= duration() - 1e-6) {
        state.t = 0; // play again from the start
    }
    state.playing = playing && Boolean(state.sim);
    $('play').classList.toggle('playing', state.playing);
    lastTick = performance.now();
}
function seek(t) {
    state.t = Math.max(0, Math.min(duration(), t));
    invalidate();
}
function buildSegments() {
    const container = $('segments');
    container.replaceChildren();
    const sim = state.sim;
    if (!sim)
        return;
    const steps = sim.route.steps;
    let start = 0;
    for (let i = 1; i <= sim.frames.length; i++) {
        const step = sim.frames[start][F.STEP];
        if (i < sim.frames.length && sim.frames[i][F.STEP] === step)
            continue;
        const segment = el('div', step >= steps.length ? 'coast' : steps[step].type);
        segment.style.flexGrow = String(i - start);
        segment.dataset.step = String(step);
        segment.title = step >= steps.length ? 'coasting to a stop' : stepLabel(steps[step]);
        const startTime = sim.frames[start][F.T];
        segment.addEventListener('click', () => {
            setPlaying(false);
            seek(startTime);
        });
        container.append(segment);
        start = i;
    }
}
// ---- tuning -----------------------------------------------------------------------------------
function onTuningChanged() {
    updateTuningBadge();
    invalidate();
    window.clearTimeout(tuningTimer);
    tuningTimer = window.setTimeout(() => void rerun(), 200);
}
function updateTuningBadge() {
    const count = tuning.changedCount();
    const badge = $('tuning-badge');
    badge.hidden = count === 0;
    badge.textContent = `${count} changed`;
}
// ---- sketch -----------------------------------------------------------------------------------
async function runSketch() {
    updateSketchHint();
    updateUndoButtons();
    renderEditor();
    if (state.sketch.points.length < 2) {
        state.request++;
        loadSim(null);
        showResults();
        return;
    }
    const sk = state.sketch;
    const radians = (deg) => (deg === null ? null : (deg * Math.PI) / 180);
    await runSim(() => simulateSketch({
        name: sk.name,
        points: sk.points.map((p) => [p.x, p.y, p.speed, p.lookahead]),
        reversed: sk.reversed,
        moveSpeed: sk.speed,
        followDistance: sk.lookahead,
        startHeading: radians(sk.startHeading),
        endHeading: radians(sk.endHeading),
        fixedHeading: radians(sk.fixedHeading),
    }, state.holonomic, tuning.tuning()));
}
function scheduleSketch(delay = 120) {
    window.clearTimeout(sketchTimer);
    sketchTimer = window.setTimeout(() => void runSketch(), delay);
}
/** Call before changing the sketch so it can be undone. */
function remember() {
    state.history.past.push(clonePoints(state.sketch.points));
    if (state.history.past.length > 100)
        state.history.past.shift();
    state.history.future = [];
    updateUndoButtons();
}
function undo() {
    const previous = state.history.past.pop();
    if (!previous)
        return;
    state.history.future.push(state.sketch.points);
    state.sketch.points = previous;
    state.selectedPoint = Math.min(state.selectedPoint, previous.length - 1);
    void runSketch();
}
function redo() {
    const next = state.history.future.pop();
    if (!next)
        return;
    state.history.past.push(state.sketch.points);
    state.sketch.points = next;
    state.selectedPoint = Math.min(state.selectedPoint, next.length - 1);
    void runSketch();
}
function updateUndoButtons() {
    $('sk-undo').disabled = !state.history.past.length;
    $('sk-redo').disabled = !state.history.future.length;
}
/** Starts a sketch from an existing route's points so it can be tweaked. */
function editAsSketch(route) {
    const follows = route.steps.filter((s) => s.type === 'follow');
    remember();
    const sk = state.sketch;
    sk.speed = follows[0]?.speed ?? 0.6;
    sk.lookahead = follows[0]?.lookahead ?? 16;
    // each leg's speed / lookahead becomes a per-point setting where it differs from the first leg
    sk.points = [{ x: route.start[0], y: route.start[1], speed: null, lookahead: null },
        ...follows.flatMap((leg) => leg.points.map((p) => ({
            x: p[0], y: p[1],
            speed: leg.speed === sk.speed ? null : leg.speed,
            lookahead: leg.lookahead === sk.lookahead ? null : leg.lookahead,
        })))];
    sk.name = `${route.name} copy`;
    sk.reversed = follows[0]?.reversed ?? false;
    sk.startHeading = Math.round((route.start[2] * 180) / Math.PI);
    const last = follows[follows.length - 1];
    sk.endHeading = last?.endHeading == null ? null : Math.round((last.endHeading * 180) / Math.PI);
    sk.fixedHeading = follows[0]?.fixedHeading == null ? null : Math.round((follows[0].fixedHeading * 180) / Math.PI);
    state.selectedPoint = -1;
    syncPathSettings();
    if (follows.length > 1 || route.steps.some((s) => s.type !== 'follow')) {
        toast('The editor makes one path: legs were joined and markers / turns dropped');
    }
    void selectRoute(SKETCH);
}
function updateSketchHint() {
    const n = state.sketch.points.length;
    if (state.measuring) {
        return;
    }
    if (state.selected !== SKETCH || state.mode !== 'sim' || n >= 2) {
        $('hint').hidden = true;
        return;
    }
    showHint(n === 0 ? 'Click the field to place the start' : 'Click to add a waypoint');
}
// ---- live -------------------------------------------------------------------------------------
function onLiveStatus(status) {
    state.live.status = status;
    const pill = $('live-pill');
    const receiving = status.running && status.text.startsWith('Receiving');
    pill.classList.toggle('live', receiving);
    pill.classList.toggle('connecting', status.running && !receiving);
    pill.querySelector('span').textContent = receiving
        ? `Robot live · ${status.text.replace('Receiving ', '').replace(' frames/s', ' fps')}`
        : status.running ? 'Waiting for robot' : 'Robot offline';
    $('connect').textContent = status.running ? 'Disconnect' : 'Connect';
    $('live-status').textContent = status.text;
    $('live-badge').hidden = !(state.mode === 'live' && receiving);
    syncLimelightStream(state.mode === 'live' && status.running);
    if (status.running && !limelightPipelineAttempted) {
        limelightPipelineAttempted = true;
        void refreshLimelightPipeline();
    }
    else if (!status.running) {
        limelightPipelineAttempted = false;
        $('limelight-pipeline-status').textContent = 'Initialize a Limelight OpMode to edit';
    }
}
async function refreshLimelightPipeline() {
    const status = $('limelight-pipeline-status');
    status.textContent = 'Reading pipeline from robot…';
    try {
        const current = await fetchLimelightPipeline();
        $('limelight-pipeline').value = String(current.pipeline);
        status.textContent = `Robot is using pipeline ${current.pipeline}`;
    }
    catch (error) {
        status.textContent = error.message;
    }
}
async function applyLimelightPipeline() {
    const status = $('limelight-pipeline-status');
    const button = $('limelight-pipeline-apply');
    const pipeline = Number($('limelight-pipeline').value);
    button.disabled = true;
    status.textContent = `Switching robot to pipeline ${pipeline}…`;
    try {
        const current = await setLimelightPipeline(pipeline);
        $('limelight-pipeline').value = String(current.pipeline);
        status.textContent = `Robot is using pipeline ${current.pipeline}`;
        toast(`Limelight switched to pipeline ${current.pipeline}`);
    }
    catch (error) {
        status.textContent = error.message;
        toast(error.message, true);
    }
    finally {
        button.disabled = false;
    }
}
let pipelineDraft = null;
function openLimelightPipelineEditor() {
    const index = $('limelight-pipeline').value;
    $('pipeline-editor-index').value = index;
    $('pipeline-editor').hidden = false;
    void loadPipelineConfig();
}
function closeLimelightPipelineEditor() {
    $('pipeline-editor').hidden = true;
}
function pipelineEditorIndex() {
    return Number($('pipeline-editor-index').value);
}
async function loadPipelineConfig() {
    const index = pipelineEditorIndex();
    const status = $('pipeline-editor-status');
    const fields = $('pipeline-editor-fields');
    status.textContent = `Loading pipeline ${index} from Limelight…`;
    fields.replaceChildren();
    pipelineDraft = null;
    setPipelineEditorBusy(true);
    try {
        pipelineDraft = await fetchLimelightPipelineConfig(index);
        renderPipelineConfig();
        status.textContent = `Pipeline ${index} loaded — edit values, then save`;
    }
    catch (error) {
        status.textContent = error.message;
        toast(error.message, true);
    }
    finally {
        setPipelineEditorBusy(false);
    }
}
async function savePipelineConfig() {
    if (!pipelineDraft)
        return;
    const index = pipelineEditorIndex();
    const status = $('pipeline-editor-status');
    status.textContent = `Saving pipeline ${index} to Limelight…`;
    setPipelineEditorBusy(true);
    try {
        await saveLimelightPipelineConfig(index, pipelineDraft);
        status.textContent = `Pipeline ${index} saved to the camera`;
        toast(`Saved Limelight pipeline ${index}`);
    }
    catch (error) {
        status.textContent = error.message;
        toast(error.message, true);
    }
    finally {
        setPipelineEditorBusy(false);
    }
}
function setPipelineEditorBusy(busy) {
    $('pipeline-editor-load').disabled = busy;
    $('pipeline-editor-save').disabled = busy || !pipelineDraft;
}
function renderPipelineConfig() {
    const fields = $('pipeline-editor-fields');
    fields.replaceChildren();
    if (!pipelineDraft)
        return;
    for (const [key, value] of Object.entries(pipelineDraft)) {
        fields.append(renderPipelineValue(key, value, [key], true));
    }
    syncPipelineRawJson();
}
function renderPipelineValue(key, value, path, top = false) {
    if (isJsonObject(value)) {
        const section = document.createElement('details');
        section.className = 'pipeline-section';
        section.open = top;
        const summary = document.createElement('summary');
        summary.textContent = readablePipelineKey(key);
        summary.title = key;
        section.append(summary);
        const group = document.createElement('div');
        group.className = 'pipeline-field-group';
        for (const [childKey, childValue] of Object.entries(value)) {
            group.append(renderPipelineValue(childKey, childValue, [...path, childKey]));
        }
        section.append(group);
        return section;
    }
    const row = document.createElement('label');
    row.className = 'pipeline-field';
    const name = document.createElement('span');
    name.className = 'pipeline-field-name';
    name.textContent = readablePipelineKey(key);
    name.title = path.join('.');
    row.append(name);
    if (typeof value === 'boolean') {
        const control = document.createElement('input');
        control.type = 'checkbox';
        control.checked = value;
        control.addEventListener('change', () => setPipelineValue(path, control.checked));
        row.append(control);
    }
    else if (typeof value === 'number') {
        const control = document.createElement('input');
        control.className = 'input mono';
        control.type = 'number';
        control.step = 'any';
        control.value = String(value);
        control.addEventListener('change', () => {
            const next = Number(control.value);
            if (Number.isFinite(next))
                setPipelineValue(path, next);
        });
        row.append(control);
    }
    else if (typeof value === 'string') {
        const control = document.createElement('input');
        control.className = 'input mono';
        control.type = 'text';
        control.value = value;
        control.addEventListener('input', () => setPipelineValue(path, control.value));
        row.append(control);
    }
    else {
        const control = document.createElement('textarea');
        control.className = 'input mono';
        control.value = JSON.stringify(value);
        control.addEventListener('change', () => {
            try {
                setPipelineValue(path, JSON.parse(control.value));
                control.classList.remove('pipeline-field-error');
            }
            catch {
                control.classList.add('pipeline-field-error');
                $('pipeline-editor-status').textContent = `${path.join('.')} is not valid JSON`;
            }
        });
        row.append(control);
    }
    return row;
}
function isJsonObject(value) {
    return value !== null && typeof value === 'object' && !Array.isArray(value);
}
function setPipelineValue(path, value) {
    if (!pipelineDraft)
        return;
    let target = pipelineDraft;
    for (const key of path.slice(0, -1)) {
        const next = target[key];
        if (!isJsonObject(next))
            return;
        target = next;
    }
    target[path[path.length - 1]] = value;
    syncPipelineRawJson();
}
function readablePipelineKey(key) {
    return key.replace(/[_-]+/g, ' ').replace(/([a-z])([A-Z])/g, '$1 $2')
        .replace(/^./, (letter) => letter.toUpperCase());
}
function syncPipelineRawJson() {
    $('pipeline-editor-json').value = pipelineDraft
        ? JSON.stringify(pipelineDraft, null, 2) : '';
}
function applyRawPipelineJson() {
    try {
        const value = JSON.parse($('pipeline-editor-json').value);
        if (!isJsonObject(value))
            throw new Error('Pipeline JSON must be an object');
        pipelineDraft = value;
        renderPipelineConfig();
        $('pipeline-editor-status').textContent = 'JSON applied to the form — not saved yet';
    }
    catch (error) {
        $('pipeline-editor-status').textContent = error.message;
        toast(error.message, true);
    }
}
function downloadPipelineBackup() {
    if (!pipelineDraft)
        return;
    download(`limelight-pipeline-${pipelineEditorIndex()}.json`, new Blob([JSON.stringify(pipelineDraft, null, 2)], { type: 'application/json' }));
}
async function openPipelineBackup(file) {
    try {
        const value = JSON.parse(await file.text());
        if (!isJsonObject(value))
            throw new Error('Pipeline backup must contain a JSON object');
        pipelineDraft = value;
        renderPipelineConfig();
        $('pipeline-editor-status').textContent = `${file.name} opened — not saved yet`;
    }
    catch (error) {
        toast(error.message, true);
    }
}
function syncLimelightStream(shouldRun) {
    const feed = $('limelight-feed');
    const placeholder = activeLimelightPlaceholder();
    window.clearTimeout(limelightRetry);
    if (!shouldRun) {
        feed.dataset.active = 'false';
        feed.removeAttribute('src');
        feed.hidden = true;
        setLimelightPlaceholders(true);
        setLimelightBadges(false);
        $('limelight-status').textContent = 'Connect to the robot to view video';
        return;
    }
    if (feed.dataset.active === 'true')
        return;
    feed.dataset.active = 'true';
    feed.hidden = true;
    setLimelightPlaceholders(false);
    placeholder.hidden = false;
    placeholder.textContent = 'Connecting to Limelight…';
    $('limelight-status').textContent = 'Opening robot camera relay…';
    feed.src = `/api/live/limelight?time=${Date.now()}`;
}
function activeLimelightPlaceholder() {
    return state.liveLayout === 'field' ? $('limelight-placeholder') : $('limelight-stage-placeholder');
}
function setLimelightPlaceholders(showActive) {
    $('limelight-placeholder').hidden = !(showActive && state.liveLayout === 'field');
    $('limelight-stage-placeholder').hidden = !(showActive && state.liveLayout !== 'field');
}
function setLimelightBadges(show) {
    $('limelight-live').hidden = !(show && state.liveLayout === 'field');
    $('limelight-stage-live').hidden = !(show && state.liveLayout !== 'field');
}
function setLiveLayout(layout) {
    state.liveLayout = layout;
    document.body.classList.remove('live-layout-field', 'live-layout-split', 'live-layout-camera');
    document.body.classList.add(`live-layout-${layout}`);
    document.querySelectorAll('#video-layout button').forEach((button) => button.classList.toggle('active', button.dataset.videoLayout === layout));
    const feed = $('limelight-feed');
    const target = layout === 'field' ? $('limelight-preview') : $('limelight-stage-target');
    target.prepend(feed);
    const loaded = feed.dataset.active === 'true' && !feed.hidden;
    setLimelightPlaceholders(!loaded);
    setLimelightBadges(loaded);
    invalidate();
}
function onLiveFrame(frame) {
    const live = state.live;
    recorder.add(frame);
    if (frame.route !== live.route) {
        live.route = frame.route;
        live.trail = [];
        if (state.mode === 'live') {
            showRouteCard(state.routes.find((r) => r.name === frame.route) ?? null, frame.route);
        }
    }
    live.frame = frame;
    if (frame.pose) {
        const now = performance.now();
        // a jump of more than two feet means the OpMode restarted, so start a fresh trail
        if (live.latest && Math.hypot(live.latest[0] - frame.pose[0], live.latest[1] - frame.pose[1]) > 24) {
            live.trail = [];
            live.latest = null;
        }
        if (live.latest && live.at > 0) {
            const dt = (now - live.at) / 1000;
            const moved = Math.hypot(frame.pose[0] - live.latest[0], frame.pose[1] - live.latest[1]);
            live.speed = dt > 0 ? live.speed * 0.6 + (moved / dt) * 0.4 : live.speed;
        }
        live.prev = live.latest ?? frame.pose;
        live.latest = frame.pose;
        if (live.at > 0) {
            live.interval = Math.max(10, Math.min(250, now - live.at));
        }
        live.at = now;
        const last = live.trail[live.trail.length - 1];
        if (!last || Math.hypot(last[0] - frame.pose[0], last[1] - frame.pose[1]) >= 0.75) {
            live.trail.push([frame.pose[0], frame.pose[1]]);
            if (live.trail.length > 4000)
                live.trail.shift();
        }
        // rolling graphs of the last 15 seconds
        if (!live.start)
            live.start = now;
        const s = live.series;
        s.t.push((now - live.start) / 1000);
        s.fwd.push(frame.powers?.[0] ?? 0);
        s.left.push(frame.powers?.[1] ?? 0);
        s.turn.push(frame.powers?.[2] ?? 0);
        s.speed.push(live.speed);
        while (s.t.length && s.t[0] < s.t[s.t.length - 1] - 20) {
            s.t.shift();
            s.fwd.shift();
            s.left.shift();
            s.turn.shift();
            s.speed.shift();
        }
    }
    invalidate();
}
async function toggleRecording() {
    const button = $('record');
    if (!recorder.active) {
        recorder.begin();
        button.classList.add('recording');
        return;
    }
    button.classList.remove('recording');
    button.querySelector('span').textContent = 'Record run';
    const recording = recorder.finish();
    if (!recording) {
        toast('Nothing recorded: no frames came in from the robot', true);
        return;
    }
    try {
        const saved = await saveRecording(recording);
        addRecording(recording, saved.name);
        toast(`Auto-saved ${saved.name}`);
    }
    catch (error) {
        addRecording(recording);
        toast(`Recording kept in this session; auto-save failed: ${error.message}`, true);
    }
}
function addRecording(recording, storageName, rebuild = true) {
    const id = `${REC}${storageName ?? `session-${state.recordings.size + 1}`}`;
    state.recordings.set(id, recording);
    if (rebuild)
        buildRouteList();
    return id;
}
function setMode(mode) {
    state.mode = mode;
    document.body.classList.toggle('mode-live', mode === 'live');
    if (mode === 'live')
        setLiveLayout(state.liveLayout);
    document.querySelectorAll('#mode button').forEach((b) => b.classList.toggle('active', b.dataset.mode === mode));
    const live = mode === 'live';
    $('timeline').hidden = live;
    $('live-card').hidden = !live;
    $('result-card').hidden = live;
    $('sketch-card').hidden = live || state.selected !== SKETCH;
    wrap.classList.toggle('sketching', !live && state.selected === SKETCH);
    if (live) {
        setPlaying(false);
        showRouteCard(state.routes.find((r) => r.name === state.live.route) ?? null, state.live.route || 'Waiting for robot');
    }
    else if (state.sim) {
        showRouteCard(state.selected === SKETCH ? null : state.sim.route);
    }
    if (state.live.status)
        onLiveStatus(state.live.status);
    else
        syncLimelightStream(false);
    updateSketchHint();
    invalidate();
}
// ---- drawing loop -----------------------------------------------------------------------------
function invalidate() {
    dirty = true;
}
function tick(now) {
    const dt = Math.min(0.1, (now - lastTick) / 1000);
    lastTick = now;
    if (state.mode === 'sim' && state.playing) {
        state.t += dt * state.speed;
        if (state.t >= duration()) {
            state.t = duration();
            setPlaying(false);
        }
        dirty = true;
    }
    if (recorder.active) {
        $('record').querySelector('span').textContent = `Stop · ${recorder.seconds.toFixed(1)} s · ${recorder.count} frames`;
    }
    const liveMoving = state.mode === 'live' && state.live.latest !== null && now - state.live.at < 500;
    if (dirty || liveMoving) {
        dirty = false;
        const scene = state.mode === 'sim' ? simScene() : liveScene(now);
        if (state.follow && scene.pose) {
            field.lookAt([scene.pose[0], scene.pose[1]]);
        }
        field.draw(scene);
        setText('tb-zoom', `${Math.round(field.zoom * 100)}%`);
        drawChart(now);
    }
    requestAnimationFrame(tick);
}
function drawChart(now) {
    if (!state.graphs)
        return;
    if (state.mode === 'sim') {
        chart.draw(state.series, state.sim ? state.t : null, [0, Math.max(1, duration())], Boolean(state.sim));
    }
    else {
        const end = Math.max(15, state.live.start ? (now - state.live.start) / 1000 : 0);
        chart.draw(state.live.series, null, [end - 15, end], false);
    }
}
function baseScene() {
    return {
        route: null, path: [], trail: [], pose: null, follow: null, pointTo: null, clipped: null,
        colliding: false, sketch: null, sketchActive: -1, sketchSelected: -1, mouse: state.snapPreview ?? state.mouse, ruler: state.ruler,
        snapGuides: state.snapGuides, snapPreview: state.snapPreview, ghost: state.ghost, robotSize: tuning.robotSize(), layers: state.layers,
    };
}
function simScene() {
    const scene = baseScene();
    const sketching = state.selected === SKETCH;
    if (sketching) {
        scene.sketch = sketchVecs();
        scene.sketchActive = state.drag >= 0 ? state.drag : state.hover;
        scene.sketchSelected = state.selectedPoint;
    }
    const sim = state.sim;
    if (!sim) {
        scene.route = sketching ? null : state.routes.find((r) => r.name === state.selected) ?? null;
        updateInspector(null, null, backendDown ? 'Waiting for the simulator…' : '—', -1);
        setText('time', '—');
        return scene;
    }
    const i = frameIndex();
    const frame = sim.frames[i];
    const next = sim.frames[Math.min(i + 1, sim.frames.length - 1)];
    const frac = Math.max(0, Math.min(1, (state.t - frame[F.T]) / sim.dt));
    scene.route = sketching ? null : sim.planned ?? sim.route;
    scene.pose = lerpPose(framePose(frame), framePose(next), frac);
    scene.follow = frame[F.FX] === null ? null : [frame[F.FX], frame[F.FY], frame[F.FR]];
    scene.pointTo = frame[F.PX] === null ? null : [frame[F.PX], frame[F.PY], frame[F.PR]];
    scene.clipped = frame[F.CX] === null ? null : [frame[F.CX], frame[F.CY]];
    scene.path = frame[F.PATH] >= 0 ? sim.paths[frame[F.PATH]] : [];
    scene.trail = state.simTrail.slice(0, i + 1);
    scene.trail.push([scene.pose[0], scene.pose[1]]);
    scene.colliding = frame[F.COLLIDE] === 1;
    updateInspector(scene.pose, [frame[F.FWD], frame[F.LEFT], frame[F.TURN]], sim.statuses[frame[F.STATUS]] ?? '', frame[F.STEP]);
    setText('time', `${state.t.toFixed(2)} / ${duration().toFixed(2)} s`);
    const scrub = $('scrub');
    if (document.activeElement !== scrub) {
        scrub.value = String(duration() > 0 ? Math.round((state.t / duration()) * 1000) : 0);
    }
    document.querySelectorAll('#segments div').forEach((s) => s.classList.toggle('active', Number(s.dataset.step) === frame[F.STEP]));
    return scene;
}
function liveScene(now) {
    const scene = baseScene();
    const live = state.live;
    const frame = live.frame;
    scene.route = state.routes.find((r) => r.name === live.route) ?? null;
    scene.trail = live.trail;
    if (frame) {
        if (live.prev && live.latest) {
            scene.pose = lerpPose(live.prev, live.latest, Math.min(1, (now - live.at) / live.interval));
            scene.trail = [...live.trail, [scene.pose[0], scene.pose[1]]];
        }
        scene.follow = frame.follow;
        scene.pointTo = frame.pointTo;
        scene.clipped = frame.clipped;
        scene.path = frame.path;
        scene.colliding = frame.colliding;
    }
    updateInspector(scene.pose, frame?.powers ?? null, frame?.status ?? 'No data yet', -1);
    return scene;
}
// ---- inspector --------------------------------------------------------------------------------
function showRouteCard(route, fallbackName) {
    const sketching = state.mode === 'sim' && state.selected === SKETCH;
    const recording = state.mode === 'sim' && state.selected.startsWith(REC);
    const alliance = $('route-alliance');
    alliance.className = `chip ${sketching ? 'SKETCH' : recording ? 'RED' : route?.alliance ?? ''}`;
    alliance.textContent = sketching ? 'SKETCH' : recording ? 'REC' : route ? (route.alliance === 'NONE' ? 'TEST' : route.alliance) : 'LIVE';
    setText('route-name', sketching ? state.sketch.name.trim() || 'Sketch' : route?.name ?? fallbackName ?? '—');
    setText('route-desc', sketching ? 'Your own path. Copy it as Java when it drives the way you want.'
        : route?.description ?? '');
    $('edit-sketch').hidden = !route || sketching || recording || state.mode === 'live';
    $('save-recording').hidden = !recording;
    $('route-actions').hidden = $('edit-sketch').hidden && $('save-recording').hidden;
    const steps = $('steps');
    steps.replaceChildren();
    const list = sketching ? state.sim?.route.steps ?? [] : route?.steps ?? [];
    list.forEach((step, index) => {
        const li = el('li', step.type);
        li.dataset.index = String(index);
        li.append(el('span', 'num', String(index + 1)), el('span', 'label', stepLabel(step)), el('span', 'detail', stepDetail(step)));
        li.addEventListener('click', () => jumpToStep(index));
        steps.append(li);
    });
}
function stepLabel(step) {
    switch (step.type) {
        case 'follow': return step.name;
        case 'marker': return step.name;
        case 'turn': return `turn to ${Math.round((step.heading * 180) / Math.PI)}°`;
    }
}
function stepDetail(step) {
    switch (step.type) {
        case 'follow': {
            if (!step.points.length)
                return '';
            const parts = [`${step.points.length} pt`];
            if (step.reversed)
                parts.push('reverse');
            if (step.fixedHeading !== null)
                parts.push('strafe');
            if (step.endHeading !== null)
                parts.push(`→ ${Math.round((step.endHeading * 180) / Math.PI)}°`);
            return parts.join(' · ');
        }
        case 'marker': return `${step.seconds.toFixed(1)} s`;
        case 'turn': return 'in place';
    }
}
function jumpToStep(index) {
    const sim = state.sim;
    if (state.mode !== 'sim' || !sim)
        return;
    const frame = sim.frames.find((f) => f[F.STEP] === index);
    if (frame) {
        seek(frame[F.T]);
    }
}
function updateInspector(pose, powers, status, step) {
    setText('stat-x', pose ? pose[0].toFixed(1) : '—');
    setText('stat-y', pose ? pose[1].toFixed(1) : '—');
    setText('stat-h', pose ? `${((pose[2] * 180) / Math.PI).toFixed(1)}°` : '—');
    const ids = ['fwd', 'left', 'turn'];
    ids.forEach((id, i) => {
        const v = powers ? powers[i] : 0;
        const bar = $(`bar-${id}`);
        const width = Math.min(50, Math.abs(v) * 50);
        bar.style.left = v >= 0 ? '50%' : `${50 - width}%`;
        bar.style.width = `${width}%`;
        setText(`val-${id}`, v.toFixed(2));
    });
    setText('status', status);
    document.querySelectorAll('#steps li').forEach((li) => {
        const index = Number(li.dataset.index);
        li.classList.toggle('active', step >= 0 && index === step);
        li.classList.toggle('done', step >= 0 && index < step);
    });
}
function showResults() {
    const summary = state.sim?.summary;
    const set = (id, text, level) => {
        const node = $(id);
        node.textContent = text;
        node.className = `mono ${level}`;
    };
    $('pin').disabled = !state.sim;
    if (!summary) {
        ['res-time', 'res-off', 'res-contact', 'res-timeouts'].forEach((id) => set(id, '—', ''));
        return;
    }
    set('res-time', `${summary.duration.toFixed(2)} s`, '');
    set('res-off', `${summary.maxOffPath.toFixed(1)} in`, summary.maxOffPath < 3 ? 'good' : summary.maxOffPath < 8 ? 'warn' : 'bad');
    set('res-contact', summary.centerContact > 0 ? `${summary.centerContact.toFixed(2)} s` : 'none', summary.centerContact > 0 ? 'bad' : 'good');
    set('res-timeouts', summary.timedOut.length ? summary.timedOut.join(', ') : 'none', summary.timedOut.length ? 'bad' : 'good');
}
function pinRun() {
    const sim = state.sim;
    if (!sim)
        return;
    const changed = tuning.changedCount();
    const name = state.selected === SKETCH ? 'sketch' : sim.route.name.replace(/^TEST /, '');
    state.ghost = {
        trail: state.simTrail,
        label: `${name} · ${sim.holonomic ? 'mecanum' : 'tank'}${changed ? ` · ${changed} tuned` : ''} · ${sim.summary.duration.toFixed(2)} s`,
    };
    $('unpin').hidden = false;
    toast('Pinned. Change the tuning or drivetrain to compare');
    invalidate();
}
// ---- controls ---------------------------------------------------------------------------------
function wireControls() {
    $('search').addEventListener('input', buildRouteList);
    document.querySelectorAll('#mode button').forEach((b) => b.addEventListener('click', () => setMode(b.dataset.mode)));
    document.querySelectorAll('#video-layout button').forEach((button) => button.addEventListener('click', () => setLiveLayout(button.dataset.videoLayout)));
    document.querySelectorAll('#drive button').forEach((b) => b.addEventListener('click', () => {
        state.holonomic = b.dataset.drive === 'mecanum';
        document.querySelectorAll('#drive button').forEach((x) => x.classList.toggle('active', x === b));
        void rerun();
    }));
    document.querySelectorAll('#speed button').forEach((b) => b.addEventListener('click', () => {
        state.speed = Number(b.dataset.speed);
        document.querySelectorAll('#speed button').forEach((x) => x.classList.toggle('active', x === b));
    }));
    $('play').addEventListener('click', () => setPlaying(!state.playing));
    $('restart').addEventListener('click', () => {
        seek(0);
        setPlaying(true);
    });
    const scrub = $('scrub');
    scrub.addEventListener('input', () => {
        setPlaying(false);
        seek((Number(scrub.value) / 1000) * duration());
    });
    $('graphs-toggle').addEventListener('click', () => setGraphs(!state.graphs));
    $('pin').addEventListener('click', pinRun);
    $('unpin').addEventListener('click', () => {
        state.ghost = null;
        $('unpin').hidden = true;
        invalidate();
    });
    $('edit-sketch').addEventListener('click', () => {
        const route = state.routes.find((r) => r.name === state.selected);
        if (route)
            editAsSketch(route);
    });
    $('save-recording').addEventListener('click', () => {
        const recording = state.recordings.get(state.selected);
        if (recording) {
            const stamp = recording.recordedAt.slice(0, 16).replace(/[:T]/g, '-');
            download(`${slug(recording.route || 'recording')}-${stamp}.json`, new Blob([JSON.stringify(recording)], { type: 'application/json' }));
        }
    });
    $('open-recording').addEventListener('click', () => $('recording-file').click());
    $('recording-file').addEventListener('change', async (e) => {
        const input = e.target;
        const file = input.files?.[0];
        input.value = '';
        if (!file)
            return;
        try {
            const id = addRecording(parseRecording(await file.text()));
            void selectRoute(id);
        }
        catch (err) {
            toast(err.message, true);
        }
    });
    $('new-sketch').addEventListener('click', () => void selectRoute(SKETCH));
    wireEditor();
    $('sk-reversed').addEventListener('change', (e) => {
        state.sketch.reversed = e.target.checked;
        scheduleSketch(0);
    });
    $('sk-speed').addEventListener('input', (e) => {
        state.sketch.speed = Number(e.target.value);
        setText('sk-speed-val', state.sketch.speed.toFixed(2));
        scheduleSketch();
    });
    $('sk-look').addEventListener('input', (e) => {
        state.sketch.lookahead = Number(e.target.value);
        setText('sk-look-val', `${state.sketch.lookahead} in`);
        scheduleSketch();
    });
    $('sk-undo').addEventListener('click', undo);
    $('sk-redo').addEventListener('click', redo);
    $('sk-clear').addEventListener('click', () => {
        if (!state.sketch.points.length)
            return;
        remember();
        state.sketch.points = [];
        state.selectedPoint = -1;
        void runSketch();
    });
    $('sk-copy').addEventListener('click', () => {
        const java = state.sim?.java;
        if (!java) {
            toast('Add at least two points first', true);
            return;
        }
        void copy(java, 'Copied the route as Java');
    });
    $('tune-copy').addEventListener('click', () => void copy(tuning.java(), 'Copied the tuning as Java'));
    $('tune-reset').addEventListener('click', () => tuning.reset());
    $('connect').addEventListener('click', async () => {
        try {
            if (state.live.status?.running) {
                await disconnectLive();
            }
            else {
                state.live.trail = [];
                await connectLive($('host').value.trim() || '192.168.43.1');
            }
        }
        catch (e) {
            toast(e.message, true);
        }
    });
    $('clear-trail').addEventListener('click', () => {
        state.live.trail = [];
        invalidate();
    });
    $('record').addEventListener('click', () => void toggleRecording());
    $('limelight-pipeline-apply').addEventListener('click', () => void applyLimelightPipeline());
    $('limelight-pipeline-read').addEventListener('click', () => void refreshLimelightPipeline());
    $('limelight-pipeline-editor').addEventListener('click', openLimelightPipelineEditor);
    $('pipeline-editor-close').addEventListener('click', closeLimelightPipelineEditor);
    $('pipeline-editor-load').addEventListener('click', () => void loadPipelineConfig());
    $('pipeline-editor-save').addEventListener('click', () => void savePipelineConfig());
    $('pipeline-editor-download').addEventListener('click', downloadPipelineBackup);
    $('pipeline-editor-upload').addEventListener('click', () => $('pipeline-editor-file').click());
    $('pipeline-editor-apply-json').addEventListener('click', applyRawPipelineJson);
    $('pipeline-editor-index').addEventListener('change', () => void loadPipelineConfig());
    $('pipeline-editor-file').addEventListener('change', () => {
        const fileInput = $('pipeline-editor-file');
        const file = fileInput.files?.[0];
        if (file)
            void openPipelineBackup(file);
        fileInput.value = '';
    });
    $('pipeline-editor').addEventListener('click', (event) => {
        if (event.target === $('pipeline-editor'))
            closeLimelightPipelineEditor();
    });
    const limelight = $('limelight-feed');
    limelight.addEventListener('load', () => {
        limelight.hidden = false;
        setLimelightPlaceholders(false);
        setLimelightBadges(true);
        $('limelight-status').textContent = 'Live video';
        void refreshLimelightPipeline();
    });
    limelight.addEventListener('error', () => {
        limelight.dataset.active = 'false';
        limelight.hidden = true;
        setLimelightPlaceholders(true);
        setLimelightBadges(false);
        activeLimelightPlaceholder().textContent = 'Waiting for Limelight video';
        $('limelight-status').textContent = 'Camera relay unavailable — retrying…';
        if (state.mode === 'live' && state.live.status?.running) {
            limelightRetry = window.setTimeout(() => syncLimelightStream(true), 2500);
        }
    });
    document.querySelectorAll('[data-layer]').forEach((input) => input.addEventListener('change', () => {
        state.layers[input.dataset.layer] = input.checked;
        invalidate();
    }));
    $('help-btn').addEventListener('click', () => toggleHelp(true));
    $('help-close').addEventListener('click', () => toggleHelp(false));
    $('help').addEventListener('click', (e) => {
        if (e.target === $('help'))
            toggleHelp(false);
    });
}
function wireToolbar() {
    $('tb-zoom-in').addEventListener('click', () => zoomBy(1.4));
    $('tb-zoom-out').addEventListener('click', () => zoomBy(1 / 1.4));
    $('tb-fit').addEventListener('click', fitView);
    $('tb-rotate').addEventListener('click', () => {
        field.rotateView();
        invalidate();
    });
    $('tb-follow').addEventListener('click', () => setFollow(!state.follow));
    $('tb-measure').addEventListener('click', () => setMeasuring(!state.measuring));
    $('tb-snap').addEventListener('click', () => setSnap(!state.snap));
    $('tb-snap').classList.toggle('active', state.snap);
    $('tb-shot').addEventListener('click', screenshot);
}
function zoomBy(factor) {
    field.zoomBy(factor);
    invalidate();
}
function fitView() {
    setFollow(false);
    field.fitField();
    invalidate();
}
function setFollow(on) {
    state.follow = on;
    $('tb-follow').classList.toggle('active', on);
    if (on && field.zoom < 2) {
        field.zoom = 2.5; // following only makes sense zoomed in
    }
    invalidate();
}
function setMeasuring(on) {
    state.measuring = on;
    if (!on)
        state.ruler = null;
    $('tb-measure').classList.toggle('active', on);
    wrap.classList.toggle('measuring', on);
    if (on) {
        showHint('Drag on the field to measure. Esc to stop.');
    }
    else {
        $('hint').hidden = true;
        updateSketchHint();
    }
    invalidate();
}
function setSnap(on) {
    state.snap = on;
    savePref('snap', on);
    $('tb-snap').classList.toggle('active', on);
    if (isSketching() || state.measuring)
        toast(on ? 'Snapping on (hold Option/Alt to place freely)' : 'Snapping off');
    invalidate();
}
function setGraphs(on) {
    state.graphs = on;
    savePref('graphs', on);
    $('graphs').hidden = !on;
    $('graphs-toggle').classList.toggle('active', on);
    invalidate();
}
function toggleHelp(show) {
    $('help').hidden = !show;
}
function screenshot() {
    const name = state.mode === 'live' ? state.live.route || 'live'
        : state.selected === SKETCH ? 'sketch' : state.sim?.route.name ?? 'field';
    canvas.toBlob((blob) => {
        if (blob) {
            download(`${slug(name)}.png`, blob);
            toast('Saved a picture of the field');
        }
    }, 'image/png');
}
function wireCanvas() {
    const coord = $('coord');
    canvas.addEventListener('wheel', (e) => {
        e.preventDefault();
        field.zoomAt(e.clientX, e.clientY, Math.exp(-e.deltaY * 0.0015));
        invalidate();
    }, { passive: false });
    canvas.addEventListener('pointerdown', (e) => {
        if (e.button === 2) {
            // right-click deletes a sketch point
            if (isSketching()) {
                const hit = field.pick(sketchVecs(), e.clientX, e.clientY);
                if (hit >= 0) {
                    deletePoint(hit);
                }
            }
            return;
        }
        canvas.setPointerCapture(e.pointerId);
        const point = field.toField(e.clientX, e.clientY);
        if (state.measuring && e.button === 0) {
            if (point) {
                const start = snapAt(point, null, e.altKey);
                state.ruler = [start, start];
                gesture = { kind: 'ruler', x: e.clientX, y: e.clientY, moved: false };
            }
            return;
        }
        if (isSketching() && e.button === 0 && !e.shiftKey) {
            const hit = field.pick(sketchVecs(), e.clientX, e.clientY);
            const mid = hit < 0 ? field.pickMidpoint(sketchVecs(), e.clientX, e.clientY) : -1;
            if (hit >= 0) {
                remember();
                selectPoint(hit);
                state.drag = hit;
                gesture = { kind: 'point', x: e.clientX, y: e.clientY, moved: false };
            }
            else if (mid >= 0) {
                // insert halfway along the segment and keep dragging the new point
                remember();
                const a = state.sketch.points[mid];
                const b = state.sketch.points[mid + 1];
                state.sketch.points.splice(mid + 1, 0, { x: round((a.x + b.x) / 2), y: round((a.y + b.y) / 2), speed: null, lookahead: null });
                selectPoint(mid + 1);
                state.drag = mid + 1;
                gesture = { kind: 'point', x: e.clientX, y: e.clientY, moved: true };
                renderEditor();
            }
            else if (point) {
                remember();
                const [x, y] = snapAt(point, 'new', e.altKey);
                state.snapPreview = null;
                state.sketch.points.push({ x, y, speed: null, lookahead: null });
                selectPoint(state.sketch.points.length - 1);
                void runSketch();
            }
            return;
        }
        gesture = { kind: 'pan', x: e.clientX, y: e.clientY, moved: false };
    });
    canvas.addEventListener('pointermove', (e) => {
        state.mouse = field.toField(e.clientX, e.clientY);
        state.snapGuides = [];
        state.snapPreview = null;
        let readout = state.mouse;
        if (gesture) {
            const dx = e.clientX - gesture.x;
            const dy = e.clientY - gesture.y;
            if (Math.hypot(dx, dy) > 3)
                gesture.moved = true;
            if (gesture.kind === 'pan' && gesture.moved) {
                if (state.follow)
                    setFollow(false);
                field.panBy(dx, dy);
                gesture.x = e.clientX;
                gesture.y = e.clientY;
                wrap.classList.add('panning');
            }
            else if (gesture.kind === 'ruler' && state.ruler && state.mouse) {
                readout = snapAt(state.mouse, null, e.altKey);
                state.ruler = [state.ruler[0], readout];
            }
            else if (gesture.kind === 'point' && state.drag >= 0 && state.mouse) {
                const [x, y] = readout = snapAt(state.mouse, state.drag, e.altKey);
                state.sketch.points[state.drag] = { ...state.sketch.points[state.drag], x, y };
                syncEditorValues();
                scheduleSketch();
            }
        }
        else if (isSketching() && !state.measuring) {
            state.hover = field.pick(sketchVecs(), e.clientX, e.clientY);
            const overHandle = state.hover < 0 && field.pickMidpoint(sketchVecs(), e.clientX, e.clientY) >= 0;
            wrap.classList.toggle('grab', state.hover >= 0 || overHandle);
            if (state.mouse && state.hover < 0 && !overHandle && !e.shiftKey) {
                // show where a click would put the next point
                state.snapPreview = readout = snapAt(state.mouse, 'new', e.altKey);
            }
        }
        else if (state.measuring && state.mouse) {
            readout = snapAt(state.mouse, null, e.altKey);
            state.snapPreview = null;
        }
        if (readout) {
            const rect = wrap.getBoundingClientRect();
            coord.hidden = false;
            coord.style.left = `${e.clientX - rect.left}px`;
            coord.style.top = `${e.clientY - rect.top}px`;
            coord.textContent = `x ${trimInches(readout[0])}   y ${trimInches(readout[1])}`;
        }
        else {
            coord.hidden = true;
        }
        invalidate();
    });
    const endGesture = () => {
        if (!gesture)
            return;
        if (gesture.kind === 'ruler' && !gesture.moved) {
            state.ruler = null; // a click without dragging clears the ruler
        }
        state.snapGuides = [];
        if (gesture.kind === 'point') {
            if (!gesture.moved)
                state.history.past.pop(); // just a click on a point, nothing changed
            state.drag = -1;
            scheduleSketch(0);
        }
        gesture = null;
        wrap.classList.remove('panning', 'grabbing');
        updateUndoButtons();
        invalidate();
    };
    canvas.addEventListener('pointerup', endGesture);
    canvas.addEventListener('pointercancel', endGesture);
    canvas.addEventListener('pointerleave', () => {
        state.mouse = null;
        state.hover = -1;
        state.snapPreview = null;
        if (!gesture)
            state.snapGuides = [];
        coord.hidden = true;
        invalidate();
    });
    canvas.addEventListener('dblclick', () => {
        if (!isSketching() && !state.measuring)
            fitView();
    });
    canvas.addEventListener('contextmenu', (e) => e.preventDefault());
}
function wireKeys() {
    window.addEventListener('keydown', (e) => {
        const target = e.target;
        const typing = target.tagName === 'INPUT' && !['range', 'checkbox'].includes(target.type);
        if (typing)
            return;
        const mod = e.metaKey || e.ctrlKey;
        if (mod && (e.key === 'z' || e.key === 'Z')) {
            if (isSketching()) {
                e.preventDefault();
                if (e.shiftKey)
                    redo();
                else
                    undo();
            }
            return;
        }
        if (mod && e.key === 'y') {
            if (isSketching()) {
                e.preventDefault();
                redo();
            }
            return;
        }
        if (mod || e.altKey)
            return;
        switch (e.key) {
            case ' ':
                e.preventDefault();
                setPlaying(!state.playing);
                break;
            case 'r':
            case 'R':
                seek(0);
                setPlaying(true);
                break;
            case 'ArrowRight':
            case 'ArrowLeft':
            case 'ArrowUp':
            case 'ArrowDown': {
                if (isSketching() && state.selectedPoint >= 0) {
                    e.preventDefault();
                    const d = e.shiftKey ? 6 : 1;
                    const [dx, dy] = { ArrowRight: [d, 0], ArrowLeft: [-d, 0], ArrowUp: [0, d], ArrowDown: [0, -d] }[e.key];
                    nudgePoint(state.selectedPoint, dx, dy, !e.repeat);
                    break;
                }
                if (e.key === 'ArrowUp' || e.key === 'ArrowDown')
                    break;
                e.preventDefault();
                setPlaying(false);
                const step = (state.sim?.dt ?? 0.02) * (e.shiftKey ? 10 : 1);
                seek(state.t + (e.key === 'ArrowRight' ? step : -step));
                break;
            }
            case '+':
            case '=':
                zoomBy(1.4);
                break;
            case '-':
            case '_':
                zoomBy(1 / 1.4);
                break;
            case '0':
                fitView();
                break;
            case 'o':
            case 'O':
                field.rotateView();
                invalidate();
                break;
            case 'f':
            case 'F':
                setFollow(!state.follow);
                break;
            case 'm':
            case 'M':
                setMeasuring(!state.measuring);
                break;
            case 's':
            case 'S':
                setSnap(!state.snap);
                break;
            case 'g':
            case 'G':
                setGraphs(!state.graphs);
                break;
            case 'p':
            case 'P':
                if (state.mode === 'sim')
                    pinRun();
                break;
            case '?':
                toggleHelp($('help').hidden);
                break;
            case 'Escape':
                if (!$('pipeline-editor').hidden)
                    closeLimelightPipelineEditor();
                else if (!$('help').hidden)
                    toggleHelp(false);
                else if (state.measuring)
                    setMeasuring(false);
                else if (state.selectedPoint >= 0)
                    selectPoint(-1);
                break;
            case 'Backspace':
            case 'Delete':
                if (isSketching() && state.sketch.points.length) {
                    e.preventDefault();
                    deletePoint(state.selectedPoint >= 0 ? state.selectedPoint : state.sketch.points.length - 1);
                }
                break;
            default:
                break;
        }
    });
}
// ---- path editor ------------------------------------------------------------------------------
function sketchVecs() {
    return state.sketch.points.map((p) => [p.x, p.y]);
}
function clonePoints(points) {
    return points.map((p) => ({ ...p }));
}
function selectPoint(index) {
    const changed = state.selectedPoint !== index;
    state.selectedPoint = index;
    if (changed)
        renderEditor();
    if (index >= 0) {
        $('sk-list').querySelector(`[data-index="${index}"]`)?.scrollIntoView({ block: 'nearest' });
    }
    invalidate();
}
function deletePoint(index) {
    if (index < 0 || index >= state.sketch.points.length)
        return;
    remember();
    state.sketch.points.splice(index, 1);
    state.hover = -1;
    state.selectedPoint = state.sketch.points.length ? Math.min(index, state.sketch.points.length - 1) : -1;
    void runSketch();
}
/** Moves a point by an offset; rememberIt is false for held-down keys so one undo reverts the whole nudge. */
function nudgePoint(index, dx, dy, rememberIt) {
    if (rememberIt)
        remember();
    const p = state.sketch.points[index];
    state.sketch.points[index] = { ...p, x: clampField(round(p.x + dx)), y: clampField(round(p.y + dy)) };
    syncEditorValues();
    scheduleSketch();
    invalidate();
}
function movePoint(index, to) {
    const points = state.sketch.points;
    if (index < 0 || to < 0 || to >= points.length)
        return;
    remember();
    const [point] = points.splice(index, 1);
    points.splice(to, 0, point);
    state.selectedPoint = to;
    void runSketch();
}
function insertAfter(index) {
    const points = state.sketch.points;
    const a = points[index];
    if (!a)
        return;
    let x;
    let y;
    if (index + 1 < points.length) {
        x = (a.x + points[index + 1].x) / 2;
        y = (a.y + points[index + 1].y) / 2;
    }
    else {
        // past the end: continue a foot in the same direction (or towards the center)
        const prev = points[index - 1];
        const dx = prev ? a.x - prev.x : -a.x;
        const dy = prev ? a.y - prev.y : -a.y;
        const length = Math.hypot(dx, dy) || 1;
        x = a.x + (dx / length) * 12;
        y = a.y + (dy / length) * 12;
    }
    remember();
    points.splice(index + 1, 0, { x: clampField(round(x)), y: clampField(round(y)), speed: null, lookahead: null });
    state.selectedPoint = index + 1;
    void runSketch();
}
/** Rebuilds the waypoint list and the selected point's settings. */
function renderEditor() {
    const list = $('sk-list');
    const points = state.sketch.points;
    setText('sk-count', points.length ? `${points.length} pt` : '');
    list.replaceChildren();
    if (!points.length) {
        list.append(el('div', 'wp-empty', 'No points yet. Click the field to place the start.'));
    }
    points.forEach((p, i) => {
        const row = el('div', `wp-row${i === state.selectedPoint ? ' selected' : ''}`);
        row.dataset.index = String(i);
        row.append(el('span', 'wp-badge', i === 0 ? 'S' : String(i)));
        for (const axis of ['x', 'y']) {
            const label = el('label', 'wp-field', axis.toUpperCase());
            const input = document.createElement('input');
            input.type = 'number';
            input.step = '0.5';
            input.min = '-72';
            input.max = '72';
            input.value = String(p[axis]);
            input.dataset.axis = axis;
            input.addEventListener('focus', () => selectPoint(i));
            input.addEventListener('change', () => {
                const value = Number(input.value);
                if (!Number.isFinite(value))
                    return;
                remember();
                state.sketch.points[i] = { ...state.sketch.points[i], [axis]: clampField(value) };
                void runSketch();
            });
            label.append(input);
            row.append(label);
        }
        row.append(p.speed !== null || p.lookahead !== null ? el('span', 'wp-flag') : el('span'));
        const del = el('button', 'wp-del', '×');
        del.title = 'Delete this point';
        del.addEventListener('click', (e) => {
            e.stopPropagation();
            deletePoint(i);
        });
        row.append(del);
        row.addEventListener('click', (e) => {
            if (e.target.tagName !== 'INPUT')
                selectPoint(i);
        });
        list.append(row);
    });
    renderPointDetail();
}
/** Updates the numbers in the list without rebuilding it (so a focused field keeps focus). */
function syncEditorValues() {
    $('sk-list').querySelectorAll('.wp-row').forEach((row) => {
        const p = state.sketch.points[Number(row.dataset.index)];
        if (!p)
            return;
        row.querySelectorAll('input').forEach((input) => {
            if (document.activeElement !== input)
                input.value = String(p[input.dataset.axis]);
        });
    });
    renderPointDetail();
}
function renderPointDetail() {
    // when the start heading isn't set, show what it will be
    input('sk-start').placeholder = `auto ${autoStartHeading()}`;
    const i = state.selectedPoint;
    const p = state.sketch.points[i];
    $('sk-point').hidden = !p;
    if (!p)
        return;
    setText('sk-point-title', i === 0 ? 'Start' : `Point ${i}`);
    setText('sk-point-sub', `(${p.x}, ${p.y})`);
    const isStart = i === 0;
    const speedOn = $('sk-pt-speed-on');
    const lookOn = $('sk-pt-look-on');
    const speed = $('sk-pt-speed');
    const look = $('sk-pt-look');
    // the start isn't the end of any segment, so it has no speed / lookahead of its own
    for (const input of [speedOn, lookOn])
        input.disabled = isStart;
    speedOn.checked = p.speed !== null;
    lookOn.checked = p.lookahead !== null;
    speed.disabled = p.speed === null;
    look.disabled = p.lookahead === null;
    speed.value = String(p.speed ?? state.sketch.speed);
    look.value = String(p.lookahead ?? state.sketch.lookahead);
    setText('sk-pt-speed-val', (p.speed ?? state.sketch.speed).toFixed(2));
    setText('sk-pt-look-val', `${p.lookahead ?? state.sketch.lookahead} in`);
    $('sk-up').disabled = i <= 0;
    $('sk-down').disabled = i >= state.sketch.points.length - 1;
}
/** Puts the path settings controls in line with state.sketch (after "Edit as sketch"). */
function syncPathSettings() {
    const sk = state.sketch;
    $('sk-name').value = sk.name;
    $('sk-reversed').checked = sk.reversed;
    $('sk-speed').value = String(sk.speed);
    $('sk-look').value = String(sk.lookahead);
    setText('sk-speed-val', sk.speed.toFixed(2));
    setText('sk-look-val', `${sk.lookahead} in`);
    for (const key of ['start', 'end', 'fixed']) {
        const value = sk[`${key}Heading`];
        $(`sk-${key}-on`).checked = value !== null;
        const input = $(`sk-${key}`);
        input.disabled = value === null;
        if (value !== null)
            input.value = String(value);
    }
}
function wireEditor() {
    $('sk-name').addEventListener('input', (e) => {
        state.sketch.name = e.target.value;
        scheduleSketch(400);
    });
    const wireOverride = (key, toggleId, sliderId) => {
        $(toggleId).addEventListener('change', (e) => {
            const i = state.selectedPoint;
            if (i < 0)
                return;
            remember();
            const on = e.target.checked;
            state.sketch.points[i] = { ...state.sketch.points[i], [key]: on ? Number($(sliderId).value) : null };
            renderEditor();
            scheduleSketch(0);
        });
        $(sliderId).addEventListener('input', (e) => {
            const i = state.selectedPoint;
            if (i < 0)
                return;
            state.sketch.points[i] = { ...state.sketch.points[i], [key]: Number(e.target.value) };
            renderPointDetail();
            scheduleSketch();
        });
        $(sliderId).addEventListener('pointerdown', () => remember());
    };
    wireOverride('speed', 'sk-pt-speed-on', 'sk-pt-speed');
    wireOverride('lookahead', 'sk-pt-look-on', 'sk-pt-look');
    $('sk-up').addEventListener('click', () => movePoint(state.selectedPoint, state.selectedPoint - 1));
    $('sk-down').addEventListener('click', () => movePoint(state.selectedPoint, state.selectedPoint + 1));
    $('sk-insert').addEventListener('click', () => insertAfter(state.selectedPoint));
    $('sk-delete').addEventListener('click', () => deletePoint(state.selectedPoint));
    for (const key of ['start', 'end', 'fixed']) {
        const field = `${key}Heading`;
        const toggle = $(`sk-${key}-on`);
        const input = $(`sk-${key}`);
        toggle.addEventListener('change', () => {
            input.disabled = !toggle.checked;
            if (toggle.checked && input.value === '') {
                input.value = String(key === 'start' ? autoStartHeading() : 0);
            }
            state.sketch[field] = toggle.checked ? Number(input.value) || 0 : null;
            if (key === 'fixed' && toggle.checked && !state.holonomic) {
                toast('Strafing needs a mecanum drive, so this path will simulate as mecanum');
            }
            scheduleSketch(0);
        });
        input.addEventListener('change', () => {
            if (!toggle.checked)
                return;
            state.sketch[field] = Number(input.value) || 0;
            scheduleSketch(0);
        });
    }
    input('sk-start').disabled = true;
    input('sk-end').disabled = true;
    input('sk-fixed').disabled = true;
}
/** What the start heading is when it isn't set: facing the first waypoint. */
function autoStartHeading() {
    const [a, b] = state.sketch.points;
    if (!a || !b)
        return 0;
    let heading = (Math.atan2(b.y - a.y, b.x - a.x) * 180) / Math.PI + (state.sketch.reversed ? 180 : 0);
    heading = ((heading + 540) % 360) - 180;
    return Math.round(heading);
}
function input(id) {
    return $(id);
}
function round(v) {
    return Math.round(v * 2) / 2;
}
function clampField(v) {
    return Math.max(-72, Math.min(72, v));
}
// ---- helpers ----------------------------------------------------------------------------------
function isSketching() {
    return state.mode === 'sim' && state.selected === SKETCH;
}
/**
 * Where a point placed at the mouse goes, and shows what it snapped to. index is the sketch point
 * being dragged, 'new' for the next point to add, or null for the ruler. Snap off or Option/Alt
 * held places it freely.
 */
function snapAt(raw, index, free) {
    if (free || !state.snap) {
        state.snapGuides = [];
        return freePoint(raw);
    }
    const points = isSketching() ? sketchVecs() : [];
    let neighbors = [];
    let others = points;
    if (index === 'new') {
        neighbors = points.slice(-1);
        others = points.slice(0, -1);
    }
    else if (index !== null) {
        neighbors = [points[index - 1], points[index + 1]].filter((p) => p !== undefined);
        others = points.filter((_, i) => Math.abs(i - index) > 1);
    }
    const result = snapPoint(raw, {
        reach: Math.min(3, 9 / field.ppi),
        neighbors,
        others,
        robotSize: tuning.robotSize(),
    });
    state.snapGuides = result.guides;
    return result.point;
}
function trimInches(v) {
    return Number.isInteger(v) ? String(v) : v.toFixed(1);
}
function framePose(frame) {
    return [frame[F.X], frame[F.Y], frame[F.H]];
}
function lerpPose(a, b, t) {
    let dh = b[2] - a[2];
    dh = Math.atan2(Math.sin(dh), Math.cos(dh));
    return [a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + dh * t];
}
async function copy(text, message) {
    try {
        await navigator.clipboard.writeText(text);
        toast(message);
    }
    catch {
        toast("Couldn't reach the clipboard", true);
    }
}
function el(tag, className = '', text = '') {
    const node = document.createElement(tag);
    if (className)
        node.className = className;
    if (text)
        node.textContent = text;
    return node;
}
function setText(id, text) {
    const node = $(id);
    if (node.textContent !== text)
        node.textContent = text;
}
function sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}
function loadPref(key, fallback) {
    try {
        const value = localStorage.getItem(`fieldviewer.${key}`);
        return value === null ? fallback : value === 'true';
    }
    catch {
        return fallback;
    }
}
function savePref(key, value) {
    try {
        localStorage.setItem(`fieldviewer.${key}`, String(value));
    }
    catch {
        // preferences just won't stick
    }
}
function showHint(text) {
    const hint = $('hint');
    hint.textContent = text;
    hint.hidden = false;
}
let toastTimer = 0;
function toast(text, error = false) {
    const node = $('toast');
    node.textContent = text;
    node.classList.toggle('error', error);
    node.classList.add('show');
    window.clearTimeout(toastTimer);
    toastTimer = window.setTimeout(() => node.classList.remove('show'), 2600);
}
void init();
//# sourceMappingURL=main.js.map