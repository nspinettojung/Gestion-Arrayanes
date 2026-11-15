# Gestión Arrayanes — Frontend

Aplicación Angular (standalone + signals) que sirve como interfaz del backend Spring Boot
`ApiGestionArrayanes`. El backend empaqueta este build en `src/main/resources/static` y abre el
navegador en `http://localhost:<puerto>`.

No usa Electron: el ejecutable abre el navegador en modo app (Brave → Chrome → Edge → Opera) contra la API en el mismo origen, y al cerrar esa ventana se apaga la API.

## Requisitos

- Node.js 20+
- El backend `ApiGestionArrayanes` corriendo en `http://localhost:8080` para desarrollo.

## Desarrollo

```bash
npm start
```

Abre `http://localhost:4200`. La API se consume desde `http://localhost:8080/api`
(`src/environments/environment.development.ts`), y los controllers del backend permiten ese origen.

## Build de producción

```bash
npm run build
```

1. `scripts/build-info.mjs` escribe `public/build-info.json` con versión, commit, rama y fecha.
2. `ng build` genera el build en `dist/GestionArrayanes/browser` con `<base href="/">`.

La versión y el commit se muestran al pie del sidebar, para saber siempre qué build está corriendo.

## Publicar en el backend

```bash
# Copia local para revisar el resultado (dist/static)
npm run build:static

# Copiar al static del backend (reemplaza el contenido anterior)
$env:STATIC_TARGET = "D:\VsCode\ApiGestionArrayanes\Arrayanes\src\main\resources\static"
npm run build:static
```

`prepare-static.mjs` **borra el destino** antes de copiar, así no quedan `main-*.js` viejos
acumulados. Sin `STATIC_TARGET` escribe solamente en `dist/static`; nunca toca el backend por su cuenta.

Después de copiar, recompilá/empaquetá la API para que el nuevo `static/` quede dentro del jar o del
ejecutable nativo.

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

## Tests

```bash
npm test
```
