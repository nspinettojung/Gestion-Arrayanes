# Progreso

**Estado:** terminado
**Actualizado:** 2026-11-15 10:20
**Rama:** (sin repositorio git)
**Sin commitear:** no aplica (sin git); cambios locales en `src/` y `pom.xml`

## Objetivo
Refactorizar por fases la API Spring Boot `Arrayanes` (tests, bugs y mejoras) sin romper el contrato HTTP que consume el frontend Angular compilado en `static/`.

## Dónde me quedé
Terminé la refactorización completa y además compilé la API como ejecutable nativo de GraalVM: `target\Arrayanes.exe` (178 MB, autocontenido).

`.\mvnw.cmd clean verify`: 179 tests, 0 fallos, BUILD SUCCESS. El build nativo ejecutó la misma suite y terminó con `BUILD SUCCESS`.

Verificado con el `.exe`: arranca en `127.0.0.1`, Flyway valida/aplica migraciones, sirve el frontend, responde `listapartment`/`listclient` con datos reales de una copia de `datos/mibasedatos` y no abre navegador con `--arrayanes.desktop.enabled=false`. `datos/` original intacto.

## Siguiente paso
Ninguno: trabajo cerrado. No se hace backfill de `transactionCode`; las reservas legacy reciben código al editarse.

## Pendiente
- (sin pendientes)

## Hecho
- [x] Bugs críticos + suite de tests base.
- [x] `transactionCode` persistido + `@Version` con mapeo 409.
- [x] Perfiles `dev`/`prod`/`test` + Flyway (V1 baseline, V2 columnas) + `validate`.
- [x] CORS centralizado (`CorsWebConfiguration`) + bind loopback.
- [x] Listas vacías: `getApartmentList`/`getClientList` devuelven 200 `[]`.
- [x] Limpieza: paquete `Price` y archivos sueltos eliminados.
- [x] Ejecutable nativo GraalVM `target\Arrayanes.exe`, verificado con base real.

## Notas
- No es repo git: no hay commit/push.
- El contrato HTTP mantiene rutas y campos JSON; cambios acordados: `transactionCode` aditivo y listas vacías pasan de 409 a 200 `[]`.
- Flyway: `baseline-on-migrate=true`, `baseline-version=1`; V2 usa `IF NOT EXISTS`.
- Perfil por defecto `dev` (consola H2 `/testdb`, `show-sql`); `prod` apaga ambos.
- Bind loopback decidido por el usuario: la app se usa solo en esta PC.
- Flyway advierte que H2 2.4.240 no es la versión verificada (2.3.232); funciona igual.
- Se quitó `sqlite-jdbc` del `pom.xml`: no se usaba y complicaba la imagen nativa.
- Build nativo: cargar MSVC y ejecutar
  `cmd /c ""D:\ProgramData\Microsoft\VisualStudio\VC\Auxiliary\Build\vcvars64.bat" && .\mvnw.cmd -Pnative native:compile"`.
  El `pom.xml` agrega `-H:DeadlockWatchdogInterval=60` porque en este equipo la codificación puede tardar >10 min.
- El `.exe` es autocontenido (se puede copiar solo). Usa `./datos/mibasedatos` relativo al directorio de trabajo:
  para conservar la base existente, ejecutarlo desde la raíz del proyecto (`.\target\Arrayanes.exe`), no desde `target`.
- `DesktopLauncher` ahora evalúa `arrayanes.desktop.enabled` en runtime (antes `@ConditionalOnProperty` quedaba congelado por AOT).
- Limitación nativa: la consola H2 (`/h2-console`) quedó incluida por AOT y `prod` no la apaga; solo escucha en loopback.
