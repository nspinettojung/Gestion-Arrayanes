import { spawn } from 'node:child_process';
import { existsSync } from 'node:fs';
import { join } from 'node:path';
import { root } from './util.mjs';

const backend = join(root, 'backend');
const jar = join(backend, 'target', 'Arrayanes-0.0.1-SNAPSHOT.jar');

if (!existsSync(jar)) {
  console.error('[start] No se encontró el jar. Ejecutá "pnpm build" primero.');
  process.exit(1);
}

console.log(`[start] Abriendo ${jar}`);

const child = spawn('java', ['-jar', jar], { cwd: backend, stdio: 'inherit' });
child.on('error', (error) => {
  console.error(`[start] No se pudo iniciar Java: ${error.message}`);
  process.exit(1);
});
child.on('exit', (code) => process.exit(code ?? 0));
