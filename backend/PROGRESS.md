# Progreso

**Estado:** en curso (Fase 2 de refactors de backend, monorepo)
**Actualizado:** 2026-11-17
**Rama:** master (monorepo raíz con frontend/ y backend/)

## Objetivo
Ejecutar la Fase 2 de refactors de backend (punto por punto, con aprobación del usuario), sobre el monorepo ya armado, sin romper el modo escritorio del jar (perfil `prod`, puerto dinámico y navegador en modo app).

## Dónde me quedé
Fase 2 — Contrato y API cerrada (orden acordado 3 → 5 → 7 → 6 → 4 → 9 → 10 → 11 → 12 → 14 → 19 → 20 → 24 → 25 → 26 → 27 → 28 → 29):
- Punto 3 `9727662`: `BookingResponseDTO` incluye `clientName`/`apartmentName`.
- Punto 5 `a93a717`: `DashboardService.validatePagination` (page>=0, 1<=size<=1000, whitelist de campos, asc/desc) + 6 tests.
- Punto 7 `4a87c7a`: `SpaConfigurationTest` verifica `/api/v1/loquesea`→404 JSON, `/reservas/123`→200 HTML, `/build-info.json`→200.
- Punto 6 `12bf6d5`: errores RFC 9457 (`ProblemDetail`: type/title/status/detail + requestId, timestamp, errors[]); se eliminó `ErrorResponseDTO`; frontend lee `detail` y `errors[]`.
- Punto 4 `8b205be`: rutas REST `/api/v1/...` en bookings/clients/apartments/dashboard/system, sin aliases viejos (el jar empaqueta frontend+backend juntos, no hay build anterior servido).
- Punto 9 `67fb8a4`: asociaciones `Booking.client`/`Booking.apartment`; JPQL, Specifications, mapper y servicios migrados. Los DTO siguen exponiendo `clientId`/`apartmentId`.
- Punto 10 `96884a0`: único `createBooking` privado para mapear, validar, calcular precio, asignar código y persistir. `saveBooking` y `createBookingWithClient` solo resuelven el cliente. `saveBookingEntity` ya estaba eliminado (`c86639a`).
- Punto 11 `cb8865b`: `Stay` inmutable para resolver/validar fechas y noches; `BookingValidation` ya no muta `Booking` y el servicio aplica el `Stay` validado.
- Punto 12 `73e895e`: tabla singleton `booking_code_sequence` (V4) + `PESSIMISTIC_WRITE`; reinicio anual desde códigos persistidos y `TransactionCodeAllocator` separado del formateo.
- Punto 14: estados `ACTIVE`/`CANCELLED`/`ARCHIVED` (V5). Borrar reserva = soft delete a `CANCELLED`; `BookingStatusArchiver` pasa a `ARCHIVED` las estadías terminadas con job diario a la 1 AM; consultas operativas y `onlyActive` excluyen canceladas y archivadas; borrar departamento con reservas no canceladas devuelve 409. El `status` viaja en `BookingResponseDTO`/`DashboardDTO` y el frontend muestra "Cancelada"/"Archivada".
- Punto 19 `5ea74b8`: `@EntityGraph({"client", "apartment"})` en las consultas del dashboard (incluida `findAll(spec, pageable)`) y proyección agrupada `ClientLastCheckIn` para el listado de clientes. El mapper recibe un `Map<UUID, LocalDate>`; las operaciones de un solo cliente conservan su query individual. Tests con `StatementInspector` comprueban 1 SELECT para listas y 2 (count + datos) para la página filtrada.
- Punto 20: `GET /api/v1/dashboard/summary?year=&month=&day?` con `DashboardSummaryDTO` (resumen mensual: contadores, listas y calendario; y resumen del día opcional). Se retiraron los 9 endpoints de un solo número/lista (`monthlyArrivalsCount`, `monthlyDeparturesCount`, `monthlyArrivalsList`, `monthlyDeparturesList`, `arrivalsByDate`, `departuresByDate`, `calendar`, `arrivalsCountByDate`, `departuresCountByDate`). El frontend consume el resumen desde un único `httpResource` (`loadSummary`) y el calendario deriva las reservas de las listas mensuales. Se conservan `/dashboard`, `/filter` y `/stats`. Fechas imposibles siguen dando 400 RFC 9457.

- Punto 24: `DevPortCustomizer` (@Profile `webdev`) elige el primer puerto libre de `arrayanes.dev.ports` (8081–8089 por defecto) y falla con 500 si no hay lugar. `ApiPortPublisher` escribe `backend/api-port.txt`; `pnpm dev` borra el archivo viejo, espera el puerto, genera `frontend/proxy.dev.conf.json` y levanta Angular con `--proxy-config`. `ng serve` standalone usa `frontend/proxy.conf.json` → 8081 y el entorno de desarrollo usa `apiUrl: '/api/v1'`. El 8080 queda reservado para el jar. Smoke: con 8081 ocupado, la API arrancó en 8082 y el proxy apuntó a 8082.

- Punto 25: `spring.profiles.default=prod` en `application.properties`, con `arrayanes.desktop.enabled=true` explícito en `application-prod.properties`. El jar sin perfiles arranca en modo escritorio (navegador app, sin consola H2 ni SQL) y `pnpm dev` sigue forzando `dev,webdev`. `DefaultProfileTest` fija ambos valores.
- Punto 26: `spring.h2.console.enabled=false` y `spring.jpa.show-sql=false` ya estaban en `application-prod.properties`; se amplía `DefaultProfileTest` para fijar ese apagado en prod y, de contraste, la consola H2 activa en `/testdb` bajo el perfil `dev`.
- Punto 27: `ShutdownToken` genera un UUID por arranque (fijable con `arrayanes.shutdown.token`). `POST /api/v1/system/shutdown` exige `X-Shutdown-Token` y responde 403 RFC 9457 si falta o es inválido; `GET /api/v1/system/shutdown-token` lo entrega solo a peticiones `same-origin`/`none` (rechaza `cross-site`). El `System.exit` pasó a `ShutdownInvoker` para testear sin matar la JVM. `SystemService` pide el token y lo reenvía; 5 tests backend y 1 frontend nuevos.
- Punto 28: copias H2 con `BACKUP TO` (zip con `.mv.db`, no `RUNSCRIPT`). `BackupService` crea copias diarias/al cerrar/manuales en `arrayanes.backup.directory` (`./datos/backups`), valida nombres y marca la copia elegida en `.restore-pending`. `BackupScheduler` (3 AM, `arrayanes.backup.daily-cron`) se desactiva en tests. `ShutdownInvoker.shutdown()` guarda la copia al cerrar y lo usa también `DesktopLauncher` cuando se cierra la ventana. `RestoreOnStartup` corre antes de Spring, extrae el `.mv.db`, guarda `mibasedatos.mv.db.bak-{fecha}` y reemplaza la base; si falla, conserva el marcador. Endpoints `/api/v1/system/backups` (GET/POST/POST restore) con `X-Shutdown-Token`. Frontend: `LocalTokenService` reutilizable, `BackupService`, vista `/backups` con confirmación y cierre de la app. 17 tests backend y 8 frontend nuevos.
- Punto 29: `java.version=25` en `pom.xml` (JDK 25 LTS instalado; la CI ya usaba Temurin 25, no hubo que tocarla). `JavaVersionTest` fija tanto la propiedad declarada como el major 69 del bytecode compilado: sin `clean` detectó clases viejas de Java 24, por lo que `pnpm build` ahora ejecuta `mvnw clean package` para que el jar nunca arrastre bytecode de otra versión.

Verificación base verde: backend 248 tests (tras punto 29), frontend 63 tests, lint 0 errors (17 warnings), format y build OK (jar armado con Java 25).

## Siguiente paso
OpenAPI + cliente TypeScript generado (último punto pendiente de la lista original).

## Pendiente (Fase 2, del documento original)
- OpenAPI + cliente TypeScript generado: pendiente; sí estaba en la lista original del usuario.

## Hecho
- [x] Bugs críticos + suite de tests base (sesión previa).
- [x] `transactionCode` persistido + `@Version` con mapeo 409.
- [x] Perfiles `dev`/`prod`/`test` + Flyway (V1 baseline, V2 columnas, V3 índices/únicos/price, V4 secuencia, V5 estados) + `validate`.
- [x] CORS centralizado (`CorsWebConfiguration`) + bind loopback.
- [x] Listas vacías 200 `[]`; paquete `Price` eliminado.
- [x] DTOs partidos (crear con cliente nuevo / con cliente existente / actualizar); `totalPrice` solo sale del servidor.
- [x] DNI único + normalizado; unique de departamento por nombre; índices de dashboard; tabla `price` huérfana dropeada (V3).
- [x] `/dashboard/filter` usado por el frontend con paginación server-side; `getStats` con Clock inyectado.
- [x] `generateTransactionCode` eliminado; `findLastArrivalDateByClientId` era código muerto (ausente).
- [x] Monorepo + tooling raíz (pnpm, lint, format, test, build, CI) — commits `8c75ee0`/`649ccd9`.
- [x] Fase 2 frontend: stats `440ee4e`, paginación server-side `238d1f5`, vista `/calendar` `9ad6977`, httpResource `93d84ec`.
- [x] Fase 2 backend (Contrato y API): puntos 3, 5, 7, 6, 4, 9, 10, 11, 12, 14, 19, 20, 24, 25, 26, 27, 28, 29 (ver arriba).

## Contexto histórico
- Ejecutable nativo GraalVM `target\Arrayanes.exe` verificado con base real antes del monorepo; build nativo necesita vcvars64 y `-H:DeadlockWatchdogInterval=60`.
- El `.exe` usa `./datos/mibasedatos` relativo al directorio de trabajo.
- `DesktopLauncher` evalúa `arrayanes.desktop.enabled` en runtime.
- Perfil `dev` expone consola H2 `/testdb` y `show-sql`; `prod` los apaga (consola H2 quedó incluida por AOT, solo escucha en loopback).
- Flyway: `baseline-on-migrate=true`, `baseline-version=1`; V2 usa `IF NOT EXISTS`. H2 2.4.240 avira pero funciona.
