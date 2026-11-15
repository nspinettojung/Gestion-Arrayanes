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

- API: `http://127.0.0.1:8080/api` (perfiles `dev,webdev`, sin abrir el navegador)
- Frontend: `http://localhost:4200` (Angular dev server)

El backend usa `backend/datos/mibasedatos` como base H2. `Ctrl+C` corta ambos procesos.

## Build

```bash
pnpm build
```

1. Compila Angular en `frontend/dist/GestionArrayanes/browser`.
2. Copia el resultado a `backend/src/main/resources/static` (borra el contenido anterior).
3. Empaqueta el jar con Maven en `backend/target/Arrayanes-0.0.1-SNAPSHOT.jar`.

Para ejecutar el jar (abre el navegador en modo app, comportamiento de escritorio):

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

- `application.properties`: configuración común (H2 en archivo, Flyway, CORS).
- `application-dev.properties`: consola H2 y SQL visible (perfil por defecto).
- `application-webdev.properties`: puerto fijo 8080 y sin navegador para `pnpm dev`.
- `application-prod.properties`: sin consola H2 ni SQL.
- `SpaConfiguration` / `SpaResourceResolver`: sirven la SPA y devuelven 404 para rutas
  inexistentes bajo `/api/**`.

## CI

`.github/workflows/ci.yml` ejecuta en cada push/PR:

- Backend: `./mvnw -B verify` con Java 25.
- Frontend: `pnpm lint`, `pnpm test:ci` y `pnpm build`.
