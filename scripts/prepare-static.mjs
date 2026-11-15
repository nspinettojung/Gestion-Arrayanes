import { cpSync, existsSync, mkdirSync, readdirSync, rmSync, statSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const source = join(root, 'dist', 'GestionArrayanes', 'browser');
const target = process.env.STATIC_TARGET
  ? resolve(process.env.STATIC_TARGET)
  : join(root, 'dist', 'static');

if (!existsSync(join(source, 'index.html'))) {
  console.error(`[static] No se encontró el build en ${source}. Ejecutá "npm run build" primero.`);
  process.exit(1);
}

if (target === root || target === join(root, 'dist') || target === dirname(root)) {
  console.error(`[static] Destino inseguro: ${target}`);
  process.exit(1);
}

rmSync(target, { recursive: true, force: true });
mkdirSync(target, { recursive: true });
cpSync(source, target, { recursive: true });

const files = readdirSync(target).filter((name) => statSync(join(target, name)).isFile());
const totalBytes = files.reduce((acc, name) => acc + statSync(join(target, name)).size, 0);

console.log(`[static] ${files.length} archivos copiados a ${target}`);
console.log(`[static] ${(totalBytes / 1024 / 1024).toFixed(2)} MB`);

if (!process.env.STATIC_TARGET) {
  console.log(
    '[static] Copia local (dist/static). Para publicar en la API, definí STATIC_TARGET con la ruta de static/ del backend.',
  );
}
