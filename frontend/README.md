# Gestión Arrayanes - Frontend

Aplicación Angular (standalone + signals) que consume la API Spring Boot del monorepo.
El build se copia a `backend/src/main/resources/static` y la API abre el navegador en modo app
contra el mismo origen.

## Requisitos

- Node.js 22+
- pnpm 11
- API corriendo en el primer puerto libre entre `8081` y `8089` para desarrollo (`pnpm dev` la inicia)

## Desarrollo

Desde la raíz del repo:

```bash
pnpm dev
```

Abre `http://localhost:4200`. `pnpm dev` espera a que el backend publique el puerto elegido en
`backend/api-port.txt`, genera `proxy.dev.conf.json` y levanta Angular con `--proxy-config`.
En desarrollo la URL de la API es relativa (`/api/v1`), definida en
`src/environments/environment.development.ts`; el proxy reenvía `/api` al puerto elegido.

También se puede levantar solo el frontend:

```bash
pnpm --dir frontend start
```

En ese caso se usa `proxy.conf.json`, que apunta a `http://127.0.0.1:8081` por defecto. Si el backend
eligió otro puerto, pasá `--proxy-config proxy.dev.conf.json` (generado por `pnpm dev`).

## Build

Desde la raíz:

```bash
pnpm build
```

Ese comando compila Angular, copia `dist/GestionArrayanes/browser` al `static/` del backend y
empaqueta el jar.

Pasos sueltos:

```bash
pnpm --dir frontend build          # build-info + ng build
pnpm --dir frontend build:static   # además copia a dist/static
```

Con `STATIC_TARGET` se puede copiar a otra ruta:

```powershell
$env:STATIC_TARGET = "D:\ruta\a\static"
pnpm --dir frontend build:static
```

`prepare-static.mjs` **borra el destino** antes de copiar, así no quedan `main-*.js` viejos.

## Tests y calidad

```bash
pnpm --dir frontend test
pnpm --dir frontend test:ci
pnpm --dir frontend lint
pnpm --dir frontend format
pnpm --dir frontend format:check
```

## Estilos y fuentes

- Tailwind v4 con los tokens del design system en `src/tailwind.css` (`@theme`). No se usa el CDN.
- Inter y Material Symbols están en `public/fonts` y se cargan con `@font-face` (`src/styles.scss`),
  así la interfaz funciona sin internet y sin flash de estilos.
- El modo oscuro se activa con la clase `.dark` en `<html>`.

## Copias de seguridad y token local

La ruta `/backups` permite crear copias y elegir una para restaurar. `LocalTokenService` pide
una vez el token local (`GET /api/v1/system/shutdown-token`) y lo reutilizan `SystemService`
(apagado) y `BackupService` (listado, creación y restauración) en la cabecera
`X-Shutdown-Token`. Al restaurar se marca la copia y se cierra la app; el cambio se aplica al
volver a abrir el jar, con la base detenida.

## Estructura

```
src/app/core/services/    servicios de API + build-info + token local + copias
src/app/view/             páginas (dashboard, reservas, clientes, departamentos, historial, copias)
src/app/view/assets/      componentes compartidos (tabla, modal, calendario, pipes)
src/environments/         URL de la API por entorno
scripts/                  build-info y copia del build al static
```
