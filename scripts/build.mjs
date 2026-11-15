import { join } from 'node:path';
import { announce, mvnw, root, runSync } from './util.mjs';

function fail(label, code) {
  console.error(`\n[build] ${label} falló con código ${code}.`);
  process.exit(code);
}

announce('frontend', 'compilando Angular');
const frontend = runSync(process.execPath, ['--run', 'build'], {
  cwd: join(root, 'frontend'),
});
if (frontend !== 0) fail('frontend', frontend);

announce('static', 'copiando el build a backend/src/main/resources/static');
const staticCopy = runSync(process.execPath, [join(root, 'frontend', 'scripts', 'prepare-static.mjs')], {
  env: {
    ...process.env,
    STATIC_TARGET: join(root, 'backend', 'src', 'main', 'resources', 'static'),
  },
});
if (staticCopy !== 0) fail('static', staticCopy);

announce('backend', 'empaquetando con Maven');
const backend = runSync(mvnw, ['-B', 'package'], { cwd: join(root, 'backend') });
if (backend !== 0) fail('backend', backend);

console.log('\n[build] Listo. Jar en backend/target/Arrayanes-0.0.1-SNAPSHOT.jar');
