import { spawnSync } from 'node:child_process';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

export const root = join(dirname(fileURLToPath(import.meta.url)), '..');
export const isWindows = process.platform === 'win32';
export const mvnw = join(root, 'backend', isWindows ? 'mvnw.cmd' : 'mvnw');

/** En Windows los .cmd necesitan pasar por el shell; las rutas pueden tener espacios. */
function quote(part) {
  return isWindows && /\s/.test(part) && !part.startsWith('"') ? `"${part}"` : part;
}

export function quoteArg(arg) {
  return quote(arg);
}

export function runSync(command, args = [], options = {}) {
  const base = { cwd: root, stdio: 'inherit', ...options };

  if (!isWindows) {
    return spawnSync(command, args, base).status ?? 1;
  }

  // En Windows hay que pasar por cmd.exe para ejecutar .cmd. Armamos la línea
  // completa en vez de usar `args` con shell para no disparar DEP0190.
  const line = [quote(command), ...args.map(quoteArg)].join(' ');
  return spawnSync(line, { ...base, shell: true }).status ?? 1;
}

export function announce(label, message) {
  console.log(`\n=== ${label} :: ${message} ===\n`);
}
