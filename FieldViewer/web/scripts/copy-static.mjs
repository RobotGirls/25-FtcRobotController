// Copies the HTML / CSS in static/ next to the compiled TypeScript in dist/.
import { cpSync, mkdirSync, rmSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
mkdirSync(join(root, 'dist'), { recursive: true });
cpSync(join(root, 'static'), join(root, 'dist'), { recursive: true });

// The beginner Academy intentionally lives at the repository root so it is easy to find.
// Remove files from the original in-web location, then stage the separate app for Electron.
for (const oldFile of ['academy.html', 'academy.css', 'app/academy.js', 'app/academy.js.map']) {
  rmSync(join(root, 'dist', oldFile), { force: true });
}
const academyRoot = join(root, '..', '..', 'PathPlannerAcademy');
mkdirSync(join(root, 'dist', 'academy'), { recursive: true });
cpSync(join(academyRoot, 'static'), join(root, 'dist', 'academy'), { recursive: true });
