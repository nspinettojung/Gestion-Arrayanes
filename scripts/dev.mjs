import { spawn, spawnSync } from 'node:child_process';
import { createInterface } from 'node:readline';
import { join } from 'node:path';
import { isWindows, mvnw, quoteArg, root } from './util.mjs';

const children = new Set();
let shuttingDown = false;

function prefix(name, stream, output) {
  const lines = createInterface({ input: stream });
  lines.on('line', (line) => output.write(`[${name}] ${line}\n`));
}

function stop(child) {
  if (child.exitCode !== null || child.signalCode !== null) return;

  if (isWindows) {
    spawnSync('taskkill', ['/pid', String(child.pid), '/T', '/F'], { stdio: 'ignore' });
  } else {
    child.kill('SIGTERM');
  }
}

function shutdown(code) {
  if (shuttingDown) return;
  shuttingDown = true;

  for (const child of children) stop(child);
  process.exit(code);
}

function start(name, command, args, cwd) {
  const child = isWindows
    ? spawn([`"${command}"`, ...args.map(quoteArg)].join(' '), {
        cwd,
        shell: true,
        stdio: ['ignore', 'pipe', 'pipe'],
        env: { ...process.env, FORCE_COLOR: '1' },
      })
    : spawn(command, args, {
        cwd,
        stdio: ['ignore', 'pipe', 'pipe'],
        env: { ...process.env, FORCE_COLOR: '1' },
      });

  children.add(child);
  prefix(name, child.stdout, process.stdout);
  prefix(name, child.stderr, process.stderr);

  child.on('error', (error) => {
    console.error(`[${name}] no se pudo iniciar: ${error.message}`);
    shutdown(1);
  });

  child.on('exit', (code) => {
    children.delete(child);
    if (!shuttingDown) {
      console.error(`[${name}] terminó con código ${code}. Cerrando el resto...`);
      shutdown(code ?? 1);
    }
  });
}

process.on('SIGINT', () => shutdown(0));
process.on('SIGTERM', () => shutdown(0));
process.on('SIGHUP', () => shutdown(0));

console.log('API:     http://127.0.0.1:8080/api/v1');
console.log('Frontend: http://localhost:4200');
console.log('Ctrl+C corta ambos procesos.\n');

start(
  'api',
  mvnw,
  ['spring-boot:run', '-Dspring-boot.run.profiles=dev,webdev'],
  join(root, 'backend'),
);

start('web', process.execPath, ['--run', 'start'], join(root, 'frontend'));
