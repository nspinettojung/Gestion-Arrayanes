import { join } from 'node:path';
import { root, runSync } from './util.mjs';

const script = process.argv[2];

if (!script) {
  console.error('Uso: node scripts/frontend.mjs <script>');
  process.exit(1);
}

process.exit(runSync(process.execPath, ['--run', script], { cwd: join(root, 'frontend') }));
