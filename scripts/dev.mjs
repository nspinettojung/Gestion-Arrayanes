import { spawn, spawnSync } from 'node:child_process';
import { existsSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { createInterface } from 'node:readline';
import { join } from 'node:path';
import { isWindows, mvnw, quoteArg, root } from './util.mjs';

const children = new Set();
const apiPortFile = join(root, 'backend', 'api-port.txt');
const proxyFile = join(root, 'frontend', 'proxy.dev.conf.json');
let shuttingDown = false;
let pollTimer;

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
  clearTimeout(pollTimer);

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

function publishedPort() {
  if (!existsSync(apiPortFile)) return null;
  const text = readFileSync(apiPortFile, 'utf8').trim();
  return /^\d+$/.test(text) ? Number(text) : null;
}

/** El backend publica el puerto elegido en api-port.txt al iniciar el servidor. */
function waitForApiPort(deadline = Date.now() + 60000) {
  if (shuttingDown) return Promise.reject(new Error('shutdown'));

  const port = publishedPort();
  if (port !== null) return Promise.resolve(port);
  if (Date.now() > deadline) return Promise.reject(new Error('timeout'));

  return new Promise((resolve, reject) => {
    pollTimer = setTimeout(() => {
      waitForApiPort(deadline).then(resolve, reject);
    }, 250);
  });
}

function writeProxy(port) {
  const config = {
    '/api': {
      target: `http://127.0.0.1:${port}`,
      changeOrigin: true,
    },
  };
  writeFileSync(proxyFile, `${JSON.stringify(config, null, 2)}\n`, 'utf8');
}

process.on('SIGINT', () => shutdown(0));
process.on('SIGTERM', () => shutdown(0));
process.on('SIGHUP', () => shutdown(0));

// Evitamos leer el puerto de una ejecución anterior.
rmSync(apiPortFile, { force: true });

console.log('API: eligiendo el primer puerto libre de 8081 a 8089...');
console.log('Frontend: http://localhost:4200');
console.log('Ctrl+C corta ambos procesos.\n');

start(
  'api',
  mvnw,
  ['spring-boot:run', '-Dspring-boot.run.profiles=dev,webdev'],
  join(root, 'backend'),
);

waitForApiPort()
  .then((port) => {
    if (shuttingDown) return;
    writeProxy(port);
    console.log(`API: http://127.0.0.1:${port}/api/v1`);
    console.log(`Proxy Angular: /api -> http://127.0.0.1:${port}`);
    start(
      'web',
      process.execPath,
      ['--run', 'start', '--', '--proxy-config', 'proxy.dev.conf.json'],
      join(root, 'frontend'),
    );
  })
  .catch((error) => {
    if (shuttingDown || error.message === 'shutdown') return;
    console.error('[dev] La API no publicó su puerto en 60 segundos. Revisá el log de [api].');
    shutdown(1);
  });
