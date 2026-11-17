# Progreso

**Estado:** en curso (Fase 2 de refactors de backend, monorepo)
**Actualizado:** 2026-11-17
**Rama:** master (monorepo raíz con frontend/ y backend/)

## Objetivo
Ejecutar la Fase 2 de refactors de backend (punto por punto, con aprobación del usuario), sobre el monorepo ya armado, sin romper el modo escritorio del jar (perfil `webdev`, puerto 8080).

## Dónde me quedé
Fase 2 — Contrato y API cerrada (orden acordado 3 → 5 → 7 → 6 → 4 → 9 → 10 → 11 → 12):
- Punto 3 `9727662`: `BookingResponseDTO` incluye `clientName`/`apartmentName`.
- Punto 5 `a93a717`: `DashboardService.validatePagination` (page>=0, 1<=size<=1000, whitelist de campos, asc/desc) + 6 tests.
- Punto 7 `4a87c7a`: `SpaConfigurationTest` verifica `/api/v1/loquesea`→404 JSON, `/reservas/123`→200 HTML, `/build-info.json`→200.
- Punto 6 `12bf6d5`: errores RFC 9457 (`ProblemDetail`: type/title/status/detail + requestId, timestamp, errors[]); se eliminó `ErrorResponseDTO`; frontend lee `detail` y `errors[]`.
- Punto 4 `8b205be`: rutas REST `/api/v1/...` en bookings/clients/apartments/dashboard/system, sin aliases viejos (el jar empaqueta frontend+backend juntos, no hay build anterior servido).
- Punto 9 `67fb8a4`: asociaciones `Booking.client`/`Booking.apartment`; JPQL, Specifications, mapper y servicios migrados. Los DTO siguen exponiendo `clientId`/`apartmentId`.
- Punto 10 `96884a0`: único `createBooking` privado para mapear, validar, calcular precio, asignar código y persistir. `saveBooking` y `createBookingWithClient` solo resuelven el cliente. `saveBookingEntity` ya estaba eliminado (`c86639a`).
- Punto 11 `cb8865b`: `Stay` inmutable para resolver/validar fechas y noches; `BookingValidation` ya no muta `Booking` y el servicio aplica el `Stay` validado.
- Punto 12 `73e895e`: tabla singleton `booking_code_sequence` (V4) + `PESSIMISTIC_WRITE`; reinicio anual desde códigos persistidos y `TransactionCodeAllocator` separado del formateo.

Verificación base verde: backend 205 tests, frontend 54 tests, lint 0 errors (17 warnings), format y build OK.

## Siguiente paso
Punto 14: soft delete / estados (CANCELLED, ARCHIVED) y 409 específico al borrar un departamento con reservas.

## Pendiente (Fase 2, del documento original)
- 14 Soft delete / estados (CANCELLED, ARCHIVED) y 409 específico al borrar departamento con reservas.
- 19 N+1: `DashboardService.getDashboard` y `ClientMapper.toResponseDTO` (proyecciones/@EntityGraph/JOIN FETCH).
- 20 `GET /api/v1/dashboard/summary` (unificar endpoints chicos).
- 24 Puerto dev fijo 8080 (`server.port=0` hoy) o proxy de Angular.
- 25 Perfil default prod/desktop para el .exe (hoy arranca con `dev`).
- 26 Consola H2 apagada explícitamente en prod.
- 27 Token local en `/api/v1/system/shutdown`.
- 28 Backups H2 (`BACKUP TO` diario/al cerrar + restauración desde UI).
- 29 Java 25 LTS.
- OpenAPI + cliente TypeScript generado: pendiente; sí estaba en la lista original del usuario.

## Hecho
- [x] Bugs críticos + suite de tests base (sesión previa).
- [x] `transactionCode` persistido + `@Version` con mapeo 409.
- [x] Perfiles `dev`/`prod`/`test` + Flyway (V1 baseline, V2 columnas, V3 índices/únicos/price) + `validate`.
- [x] CORS centralizado (`CorsWebConfiguration`) + bind loopback.
- [x] Listas vacías 200 `[]`; paquete `Price` eliminado.
- [x] DTOs partidos (crear con cliente nuevo / con cliente existente / actualizar); `totalPrice` solo sale del servidor.
- [x] DNI único + normalizado; unique de departamento por nombre; índices de dashboard; tabla `price` huérfana dropeada (V3).
- [x] `/dashboard/filter` usado por el frontend con paginación server-side; `getStats` con Clock inyectado.
- [x] `generateTransactionCode` eliminado; `findLastArrivalDateByClientId` era código muerto (ausente).
- [x] Monorepo + tooling raíz (pnpm, lint, format, test, build, CI) — commits `8c75ee0`/`649ccd9`.
- [x] Fase 2 frontend: stats `440ee4e`, paginación server-side `238d1f5`, vista `/calendar` `9ad6977`, httpResource `93d84ec`.
- [x] Fase 2 backend (Contrato y API): puntos 3, 5, 7, 6, 4, 9, 10, 11, 12 (ver arriba).

## Contexto histórico
- Ejecutable nativo GraalVM `target\Arrayanes.exe` verificado con base real antes del monorepo; build nativo necesita vcvars64 y `-H:DeadlockWatchdogInterval=60`.
- El `.exe` usa `./datos/mibasedatos` relativo al directorio de trabajo.
- `DesktopLauncher` evalúa `arrayanes.desktop.enabled` en runtime.
- Perfil `dev` expone consola H2 `/testdb` y `show-sql`; `prod` los apaga (consola H2 quedó incluida por AOT, solo escucha en loopback).
- Flyway: `baseline-on-migrate=true`, `baseline-version=1`; V2 usa `IF NOT EXISTS`. H2 2.4.240 avira pero funciona.
