import { join } from 'node:path';
import { announce, mvnw, root, runSync } from './util.mjs';

const results = [];

announce('backend', 'mvnw verify');
results.push(['backend', runSync(mvnw, ['-B', 'verify'], { cwd: join(root, 'backend') })]);

announce('frontend', 'ng test --watch=false');
results.push([
  'frontend',
  runSync(process.execPath, ['--run', 'test:ci'], { cwd: join(root, 'frontend') }),
]);

console.log('\n=== Resumen ===');
for (const [name, code] of results) {
  console.log(`${code === 0 ? 'OK  ' : 'FAIL'} ${name}`);
}

if (results.some(([, code]) => code !== 0)) {
  process.exit(1);
}
