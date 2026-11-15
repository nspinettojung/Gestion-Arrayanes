import { execSync } from 'node:child_process';
import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const pkg = JSON.parse(readFileSync(join(root, 'package.json'), 'utf8'));

function git(args, fallback) {
  try {
    const out = execSync(`git ${args}`, {
      cwd: root,
      stdio: ['ignore', 'pipe', 'ignore'],
    })
      .toString()
      .trim();
    return out || fallback;
  } catch {
    return fallback;
  }
}

const info = {
  version: pkg.version,
  commit: git('rev-parse --short HEAD', 'sin-git'),
  branch: git('rev-parse --abbrev-ref HEAD', 'sin-git'),
  dirty: git('status --porcelain', '') !== '',
  builtAt: new Date().toISOString(),
};

mkdirSync(join(root, 'public'), { recursive: true });
writeFileSync(join(root, 'public', 'build-info.json'), JSON.stringify(info, null, 2) + '\n');

console.log(
  `[build-info] v${info.version} ${info.commit}${info.dirty ? ' (con cambios sin commitear)' : ''} · ${info.builtAt}`,
);
