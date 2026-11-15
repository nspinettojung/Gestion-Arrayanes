# Gestión Arrayanes - Frontend

Aplicación Angular (standalone + signals) que consume la API Spring Boot del monorepo.
El build se copia a `backend/src/main/resources/static` y la API abre el navegador en modo app
contra el mismo origen.

## Requisitos

- Node.js 22+
- pnpm 11
- API corriendo en `http://127.0.0.1:8080` para desarrollo

## Desarrollo

Desde la raíz del repo:

```bash
pnpm dev
```

Abre `http://localhost:4200`. La URL de la API está en
`src/environments/environment.development.ts` y el backend permite ese origen por CORS.

También se puede levantar solo el frontend:

```bash
pnpm --dir frontend start
```

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

## Estructura

```
src/app/core/services/    servicios de API + build-info
src/app/view/             páginas (dashboard, reservas, clientes, departamentos, historial)
src/app/view/assets/      componentes compartidos (tabla, modal, calendario, pipes)
src/environments/         URL de la API por entorno
scripts/                  build-info y copia del build al static
```
