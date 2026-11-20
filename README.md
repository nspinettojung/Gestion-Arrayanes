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
- JDK 25 LTS (el `pom.xml` apunta a Java 25; el wrapper de Maven se encarga del resto)

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

El backend publica el puerto elegido en `backend/api-port.txt`. `pnpm dev` espera a que exista ese archivo, genera `frontend/proxy.dev.conf.json` y recién entonces levanta Angular con el proxy hacia ese puerto. El jar de escritorio (`pnpm start`) usa `server.port=0`, es decir un puerto efímero: el puerto real se publica en `backend/api-port.txt` y el navegador se abre ahí, no en un 8080 fijo. Para usar `ng serve` por separado, el proxy por defecto apunta a `8081` (`frontend/proxy.conf.json`).

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
pnpm api           # Regenera el contrato OpenAPI y los tipos TypeScript
```

## Contrato OpenAPI

`pnpm api` ejecuta `OpenApiSpecTest`, que pide `/v3/api-docs` a los controladores reales y
guarda el JSON canonicalizado (claves ordenadas) en `frontend/src/app/core/api/openapi.json`.
Luego `openapi-typescript` genera `frontend/src/app/core/api/api-types.ts`. Ambos archivos son
artefactos: no se editan a mano y están excluidos de Prettier y ESLint.

Los servicios Angular usan los esquemas generados en vez de interfaces duplicadas; por ejemplo
`BackupService` y `LocalTokenService` importan `components['schemas']`. Los DTOs consumidos por
la UI declaran `@Schema(requiredMode = REQUIRED)` para que los campos obligatorios no salgan
opcionales en TypeScript.

El endpoint `/v3/api-docs` está activo en desarrollo y tests, pero desactivado en el jar
(`springdoc.api-docs.enabled=false` en el perfil `prod`). No se incluye Swagger UI. La CI falla
si el contrato o los tipos generados están desactualizados.

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
- `GET /api/v1/system/backups` lista las copias y la pendiente; `POST /api/v1/system/backups`
  crea una copia manual; `POST /api/v1/system/backups/{nombre}/restore` la marca para
  restaurar. Los tres exigen `X-Shutdown-Token`.

## Copias de seguridad

El jar hace una copia diaria (3 AM, configurable con `arrayanes.backup.daily-cron`) y otra
al cerrar, además de las manuales. Se guardan como zip en `arrayanes.backup.directory`
(`backend/datos/backups` por defecto) con nombres `backup-{diario|cierre|manual}-{fecha}.zip`.

Desde **Copias de seguridad** en la UI se crea una copia o se elige una para restaurar. La
restauración no toca la base en caliente: se marca la copia, se cierra la app y, al volver a
abrir el jar, `RestoreOnStartup` extrae el `.mv.db` del zip y reemplaza la base antes de que
arranque Spring. Primero guarda la base actual como `mibasedatos.mv.db.bak-{fecha}`; si la
copia es inválida, conserva el marcador y deja la base intacta.

En los tests la base es en memoria, así que no se crean copias. El job diario se puede
desactivar con `arrayanes.backup.scheduler.enabled=false`.

## CI

`.github/workflows/ci.yml` ejecuta en cada push/PR:

- Backend: `./mvnw -B verify` con Java 25.
- Frontend: `pnpm lint`, `pnpm test:ci` y `pnpm build`.
