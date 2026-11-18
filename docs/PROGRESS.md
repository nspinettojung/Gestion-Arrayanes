# Progreso — Gestión Arrayanes

> **Memoria del proyecto 3/3 — POR DÓNDE va el trabajo.**
> Este archivo refleja el presente. Se reescribe al cerrar cada sesión; el detalle histórico se condensa en "Registro de sesiones".
> Para retomar en un chat nuevo: adjuntar el proyecto (o al menos `docs/PRD.md`, `docs/DECISIONS.md` y este archivo) y decir "retoma el proyecto".

## Estado actual

- **Fase:** Estabilización / nueva ronda de rebanadas
- **Última actualización:** 2026-11-18
- **Resumen:** Fase 2 de refactors (puntos 3 → 29 + OpenAPI) cerrada y commiteada en `master @ 3aeeba3`, con backend 250 tests y frontend 63 en verde. Jar de escritorio y `Arrayanes.exe` conviven en `backend/target/`. Se adoptó la memoria en `docs/`; alcance confirmado, PRD definitivo pendiente. Smoke del jar y de restauración verdes.

## Siguiente paso

1. RF-16: revisar los 17 warnings de lint preexistentes (accesibilidad en plantillas, selectores y `prefer-inject`).
2. Al terminar la ronda, armar el PRD definitivo (la "Solicitud original" sigue sin texto literal) y corregir la doc del puerto efímero.

## Tablero de requisitos

| Requisito | Estado | Dónde está el código | Tests | Notas |
|---|---|---|---|---|
| RF-01 alta de reserva | Hecho | `backend/.../Booking`, `frontend/.../mainreservation` | backend + frontend | `transactionCode` persistido |
| RF-02 editar/cancelar | Hecho | `maindashboard`, `mainhistory` | backend + frontend | Soft delete `CANCELLED`, `@Version` → 409 |
| RF-03 filtro paginado | Hecho | `/dashboard/filter`, `DashboardService` | backend | Whitelist de campos y tamaño |
| RF-04 clientes | Hecho | `Clients` | backend + frontend | DNI único normalizado |
| RF-05 departamentos | Hecho | `Apartment` | backend + frontend | 409 con reservas activas |
| RF-06 resumen | Hecho | `/dashboard/summary` | backend + frontend | Un solo `httpResource` |
| RF-07 historial | Hecho | `mainhistory` | frontend | Búsqueda por código |
| RF-08 copias/restauración | Hecho | `backend/.../Backup` | 17 backend + 8 frontend + smoke 2026-11-18 | Restauración verificada en sandbox (marcada → apagado → aplicada al reinicio) |
| RF-09 token local | Hecho | `Launcher/SystemController`, `LocalTokenService` | 5 backend + 1 frontend | 403 RFC 9457 |
| RF-10 jar escritorio | Hecho | `prod`, `DesktopLauncher`, `scripts/start.mjs` | tests de perfil + smoke 2026-11-18 | SPA 200, navegador modo app, puerto efímero publicado en `api-port.txt` |
| RF-11 OpenAPI | Hecho | `scripts/api.mjs`, `OpenApiSpecTest` | CI | Sin Swagger UI en prod |
| RF-12 nativo | Hecho | `backend/target/Arrayanes.exe` | build verificado | GraalVM 25.0.4, 17:38 min |
| RF-13 quitar Calendario | Hecho | ruta y enlace eliminados, `maincalendar/` borrado | frontend 56 tests | `/booking` ya incrusta `gda-calendar` |
| RF-14 migrar servicios a tipos | Hecho | `core/services/` (booking, clients, apartment, dashboard) | build + 56 tests | Interfaces manuales borradas; alias derivados de `components['schemas']` (ADR-0008) |
| RF-15 retención de copias | Hecho | `BackupService.java`, `application.properties` | 4 tests nuevos (254 backend) | Máximo 10 zips y 10 `.bak-*`, configurable; protege la copia pendiente (ADR-0009) |
| RF-16 warnings de lint | Pendiente | plantillas y `navbar.ts` | lint | 17 warnings preexistentes |

## Mapa del repositorio

```
backend/src/main/java/com/Gestion/Arrayanes/  API Spring Boot, dominio y launcher de escritorio
backend/src/main/resources/db/migration/      Flyway V1–V5
backend/src/main/resources/static/            SPA copiada por prepare-static (ignorada por git)
frontend/src/app/core/                        servicios, tipos derivados del contrato y contrato generado
frontend/src/app/view/                        páginas y componentes compartidos
scripts/                                      orquestación raíz (dev, build, test, start, api)
docs/                                         PRD, DECISIONS y este PROGRESS
```

## Cómo ejecutar y probar

```bash
pnpm --dir frontend install
pnpm dev            # API 8081–8089 + Angular 4200
pnpm test           # mvnw verify + ng test --watch=false
pnpm lint
pnpm format:check
pnpm build          # jar en backend/target
pnpm start          # jar en modo escritorio
pnpm api            # regenera spec y tipos TypeScript
```

Build nativo (solo cuando se necesite el `.exe`):

```powershell
cmd /c '"D:\ProgramData\Microsoft\VisualStudio\VC\Auxiliary\Build\vcvars64.bat" && cd /d "D:\VsCode\Gestion Arrayanes\backend" && mvnw.cmd -B -Pnative -DskipTests clean package native:compile'
# Después: pnpm build para restaurar el jar normal y copiar de vuelta .exe + DLL
```

## Entorno verificado

| Qué | Resultado | Fecha |
|---|---|---|
| Compila backend | Sí (`mvnw -B clean package`) | 2026-11-17 |
| Tests backend | Sí, 250 tests → 254 con RF-15 | 2026-11-17 → 2026-11-18 |
| Build frontend | Sí | 2026-11-17 |
| Tests frontend | Sí, 63 tests (56 tras quitar Calendario) | 2026-11-17 → 2026-11-18 |
| Lint / formato | 0 errores, 17 warnings / OK | 2026-11-17 → 2026-11-18 |
| Migración de tipos (RF-14) | Sí: `ng build` + 56 tests + formato + lint sin errores | 2026-11-18 |
| Smoke del jar (`pnpm start`) | Sí: SPA 200, token, copia manual creada, apagado limpio; Brave abierto en modo app | 2026-11-18 |
| Smoke de restauración | Sí: copia marcada, `.restore-pending` visible y aplicada al tercer arranque en sandbox | 2026-11-18 |
| Flujo principal de punta a punta | Parcial: smoke de arranque/copias/restauración; recorrido visual de reservas no automatizado | 2026-11-18 |
| Build nativo GraalVM | Sí, `Arrayanes.exe` + 10 DLL | 2026-11-17 |
| Versiones | Java 25.0.4, Node 26.8.1 (repo `>=22`), pnpm 11.3.0 | 2026-11-18 |

## Problemas conocidos y deuda técnica

- El jar usa `server.port=0` (puerto efímero publicado en `backend/api-port.txt`), no el 8080 fijo que dicen algunos comentarios/README; el navegador se abre al puerto real. No rompe nada, pero conviene corregir la documentación.
- 17 warnings de lint preexistentes (accesibilidad en plantillas, selectores, `prefer-inject`).
- El build nativo depende de `vcvars64` y no existe un script raíz para él.

## Preguntas abiertas para el usuario

- [ ] Confirmar el PRD reconstruido `(inferido)`, en especial la "Solicitud original" literal.

## Registro de sesiones (más reciente primero)

### 2026-11-18 — RF-15: retención de copias
- Hecho: `BackupService` conserva las 10 copias y los 10 `.bak-*` más recientes al crear una copia, con límite configurable (`arrayanes.backup.max-copies`). Nunca borra la copia marcada para restaurar. 4 tests nuevos; `pnpm test` en verde con 254 backend + 56 frontend.
- Decisiones tomadas: ADR-0009 (el usuario eligió "máximo 10 copias" entre las opciones ofrecidas).
- Quedó pendiente: RF-16, PRD definitivo, corregir la doc del puerto efímero.

### 2026-11-18 — RF-14: servicios migrados a los tipos del contrato
- Hecho: booking, clients, apartment y dashboard exportan alias derivados de `components['schemas']`; se borraron las 8 interfaces manuales de `core/interface/` y se ajustaron vistas y specs. `createClient`, `addApartment` y `editApartment` ahora usan los DTO de request generados. `ng build`, 56 tests, formato y lint (0 errores, 17 warnings) en verde.
- Decisiones tomadas: ADR-0008 (alias con `Required` y overrides de `null` dentro de cada servicio).
- Quedó pendiente: RF-15, RF-16, PRD definitivo.

### 2026-11-18 — Borrado del Calendario duplicado
- Hecho: eliminados ruta `/calendar`, enlace del nav, directorio `maincalendar/` y los miembros sin uso de `DashboardService` (`calendarDays`, `loadingCalendar`, `getCalendar`). Frontend 56 tests, format y lint sin errores (17 warnings preexistentes).
- Decisiones tomadas: ninguna nueva.
- Quedó pendiente: RF-14, RF-15, RF-16, PRD definitivo.

### 2026-11-18 — Smoke del escritorio y las copias
- Hecho: `pnpm start` levantó la SPA en puerto efímero y abrió Brave; token, listado y creación de copia OK; apagado limpio. Restauración probada en sandbox con tres arranques: marcada → pendiente → aplicada por `RestoreOnStartup`.
- Decisiones tomadas: ninguna nueva.
- Quedó pendiente: borrar Calendario, RF-14, RF-15, RF-16, PRD definitivo.

### 2026-11-18 — Adopción de la memoria de la skill
- Hecho: `docs/PRD.md`, `docs/DECISIONS.md` y `docs/PROGRESS.md` reconstruidos del repo; puntero en `PROGRESS.md` raíz; `.opencode/` va a `.gitignore`.
- Decisiones tomadas: ADR-0001 a ADR-0007.
- Quedó pendiente: confirmación del contenido inferido y ejecución del smoke.

### 2026-11-17 — Cierre de Fase 2 y build nativo
- Hecho: puntos 3 → 29 + OpenAPI; 250/63 tests; `Arrayanes.exe` verificado con GraalVM 25.0.4.
- Decisiones tomadas: perfil `prod` por defecto, consola H2 apagada, token de apagado, copias H2, Java 25, OpenAPI mínimo.
- Quedó pendiente: smoke manual, retención de copias, migración de servicios, warnings de lint, borrar Calendario duplicado.
