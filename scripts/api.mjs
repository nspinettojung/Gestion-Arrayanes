import { copyFileSync, mkdirSync } from 'node:fs';
import { join } from 'node:path';
import { announce, mvnw, root, runSync } from './util.mjs';

function fail(label, code) {
  console.error(`\n[api] ${label} falló con código ${code}.`);
  process.exit(code);
}

announce('backend', 'generando el contrato desde los controladores');
const backend = runSync(mvnw, ['-B', 'test', '-Dtest=OpenApiSpecTest'], {
  cwd: join(root, 'backend'),
});
if (backend !== 0) fail('backend', backend);

const apiDir = join(root, 'frontend', 'src', 'app', 'core', 'api');
mkdirSync(apiDir, { recursive: true });
copyFileSync(
  join(root, 'backend', 'target', 'openapi.json'),
  join(apiDir, 'openapi.json'),
);

announce('frontend', 'generando tipos TypeScript');
const frontend = runSync(process.execPath, ['--run', 'generate:api'], {
  cwd: join(root, 'frontend'),
});
if (frontend !== 0) fail('frontend', frontend);

console.log('\n[api] Listo. Contrato y tipos en frontend/src/app/core/api');
