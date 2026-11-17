# Gestión Arrayanes

Monorepo con la API Spring Boot y la SPA Angular de Gestión Arrayanes.

```
frontend/   Angular 21 (standalone + signals)
backend/    Spring Boot 4 + H2 + Flyway
scripts/    Orquestación de dev, build y test (Node, sin dependencias)
```

## Requisitos

- Node.js 22+
- pnpm 11 (`corepack enable` o `npm i -g pnpm`)
- JDK 24+ (el `pom.xml` apunta a Java 24; el wrapper de Maven se encarga del resto)

## Instalación

```bash
pnpm --dir frontend install
```

## Desarrollo

```bash
pnpm dev
```

- API: primer puerto libre entre `8081` y `8089` (perfiles `dev,webdev`, sin abrir el navegador).
- Frontend: `http://localhost:4200` (Angular dev server).

El backend publica el puerto elegido en `backend/api-port.txt`. `pnpm dev` espera a que exista ese archivo, genera `frontend/proxy.dev.conf.json` y recién entonces levanta Angular con el proxy hacia ese puerto. El 8080 queda reservado para el jar. Para usar `ng serve` por separado, el proxy por defecto apunta a `8081` (`frontend/proxy.conf.json`).

El backend usa `backend/datos/mibasedatos` como base H2. `Ctrl+C` corta ambos procesos.

## Build

```bash
pnpm build
```

1. Compila Angular en `frontend/dist/GestionArrayanes/browser`.
2. Copia el resultado a `backend/src/main/resources/static` (borra el contenido anterior).
3. Empaqueta el jar con Maven en `backend/target/Arrayanes-0.0.1-SNAPSHOT.jar`.

Para ejecutar el jar (perfil `prod` por defecto: abre el navegador en modo app, comportamiento de escritorio):

```bash
pnpm start
```

## Tests y calidad

```bash
pnpm test          # mvnw verify + ng test --watch=false
pnpm lint          # ESLint (Angular + TypeScript)
pnpm format        # Prettier
pnpm format:check  # Verifica formato sin escribir
```

## Estructura de la API

- `application.properties`: configuración común (H2 en archivo, Flyway, CORS, `prod` como perfil por defecto).
- `application-dev.properties`: consola H2 y SQL visible (se activa con `pnpm dev`).
- `application-webdev.properties`: lista `arrayanes.dev.ports` (8081–8089) y sin navegador para `pnpm dev`.
- `application-prod.properties`: modo escritorio activo, sin consola H2 ni SQL.
- `SpaConfiguration` / `SpaResourceResolver`: sirven la SPA y devuelven 404 para rutas
  inexistentes bajo `/api/**`.
- `GET /api/v1/system/shutdown-token` entrega el token de apagado solo al mismo origen;
  `POST /api/v1/system/shutdown` exige la cabecera `X-Shutdown-Token`. El token se genera
  en cada arranque y se puede fijar con `arrayanes.shutdown.token`.

## CI

`.github/workflows/ci.yml` ejecuta en cada push/PR:

- Backend: `./mvnw -B verify` con Java 25.
- Frontend: `pnpm lint`, `pnpm test:ci` y `pnpm build`.
