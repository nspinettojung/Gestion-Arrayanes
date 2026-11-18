# PRD — Gestión Arrayanes

> **Memoria del proyecto 1/3 — QUÉ se pidió.**
> Los tres archivos de memoria son `docs/PRD.md`, `docs/DECISIONS.md` y `docs/PROGRESS.md`. Léelos en este orden antes de tocar el proyecto: PROGRESS → PRD → DECISIONS.

| Campo | Valor |
|---|---|
| Versión del PRD | 1.0 |
| Fecha | 2026-11-18 |
| Estado | Borrador — reconstrucción `(inferido)` pendiente de confirmación del usuario |

## 1. Solicitud original (literal, no editar)

> No disponible: el mensaje original no quedó registrado literalmente en el repo ni en el historial conservado de la conversación. Este PRD se reconstruyó `(inferido)` del código, los README, `backend/PROGRESS.md` y el resumen de la sesión. Si aparece el texto original, pegarlo aquí sin editarlo.

## 2. Resumen `(inferido)`

Gestión Arrayanes es una aplicación de escritorio para gestionar reservas de departamentos, clientes y estadísticas de un alojamiento. Consiste en una SPA Angular que se empaqueta junto con una API Spring Boot en un jar que abre el navegador en modo app, con soporte adicional de ejecutable nativo GraalVM.

## 3. Problema y objetivos

- **Problema:** reemplazar el manejo disperso de reservas y clientes por una app local única, con historial, dashboard y copias de seguridad.
- **Objetivos:**
  1. Cubrir el ciclo de una reserva (alta, edición, cancelación e historial) con validaciones y códigos de transacción persistentes.
  2. Ofrecer un resumen operativo mensual/diario y un calendario de ocupación.
  3. Proteger la base local con copias automáticas y restauración segura con la app detenida.
- **Fuera de objetivos:** multiusuario con autenticación, despliegue en servidor, pagos, facturación e integraciones externas.

## 4. Usuarios y roles

| Rol | Descripción | Qué necesita hacer |
|---|---|---|
| Recepcionista / administrador `(inferido)` | Usuario de la app en el equipo local | Cargar clientes y departamentos, gestionar reservas, consultar el dashboard y restaurar copias |

## 5. Alcance

**Dentro (esta versión):** reservas, clientes, departamentos, dashboard, historial, copias de seguridad con retención automática, modo escritorio del jar, contrato OpenAPI, servicios Angular migrados al contrato generado y ejecutable nativo.

**Fuera (para después o nunca):** autenticación, upgrade de Angular/Spring Boot, corrección de warnings de accesibilidad preexistentes.

## 6. Requisitos funcionales

| ID | Requisito | Prioridad | Criterios de aceptación |
|---|---|---|---|
| RF-01 | Dar de alta una reserva con cliente existente o nuevo | Must | Dado un formulario válido, cuando se guarda, entonces se persiste con `transactionCode` y `ACTIVE` y aparece en el dashboard |
| RF-02 | Editar y cancelar reservas | Must | Dado un `transactionCode` con versión vigente, cuando se modifica o cancela, entonces se aplica el cambio; si la versión es vieja, responde 409; cancelar deja `CANCELLED` |
| RF-03 | Listar y filtrar reservas con paginación en servidor | Must | Dados filtros y página, cuando se consulta `/api/v1/dashboard/filter`, entonces vuelve una página válida (page ≥ 0, tamaño 1–1000, campos permitidos) |
| RF-04 | Gestionar clientes con DNI único normalizado | Must | Dado un DNI duplicado, cuando se intenta guardar, entonces responde error de validación; los listados no hacen N+1 |
| RF-05 | Gestionar departamentos con nombre único | Should | Dado un departamento con reservas activas, cuando se intenta borrar, entonces responde 409 |
| RF-06 | Consultar el resumen operativo mensual/diario | Must | Dados año/mes/día, cuando se pide `/dashboard/summary`, entonces vuelven contadores, listas, calendario y resumen del día en una sola respuesta |
| RF-07 | Consultar el historial de reservas | Should | Dado un código de transacción, cuando se busca, entonces filtra los resultados |
| RF-08 | Crear copias y restaurar una copia elegida | Must | Dada una copia válida, cuando se elige restaurar, entonces se marca, se cierra la app y al reiniciar se extrae el `.mv.db` con la base detenida, conservando un `.bak-*` |
| RF-09 | Proteger el apagado y las copias con token local | Must | Dada una petición sin `X-Shutdown-Token` válido, entonces responde 403 RFC 9457; el token solo se entrega al mismo origen |
| RF-10 | Ejecutar la app como escritorio desde el jar | Must | Dado `pnpm start`, cuando arranca sin perfiles, entonces usa `prod`, escucha en 8080 y abre el navegador en modo app sin consola H2 |
| RF-11 | Generar contrato OpenAPI y tipos TS | Should | Dado `pnpm api`, cuando cambia un DTO, entonces se regeneran spec y `api-types.ts` y la CI detecta artefactos desactualizados |
| RF-12 | Generar el ejecutable nativo GraalVM | Should | Dado el entorno con `vcvars64`, cuando se compila con el perfil `native`, entonces se produce `backend/target/Arrayanes.exe` |
| RF-13 | Quitar la sección Calendario duplicada | Should | Dado que `/booking` ya incrusta `gda-calendar`, cuando se navega el menú, entonces no aparece un enlace Calendario ni existe la ruta `/calendar` |
| RF-14 | Migrar el resto de servicios Angular a los tipos generados | Could | Dados bookings/clientes/departamentos/dashboard, cuando se consumen DTOs, entonces usan `components['schemas']` y no interfaces duplicadas |
| RF-15 | Definir retención y limpieza de copias | Could | Dadas más de 10 copias, cuando se crea una nueva, entonces se borran las más antiguas (zips y `.bak-*`), sin tocar la copia pendiente de restauración; el límite es configurable con `arrayanes.backup.max-copies` |
| RF-16 | Resolver los 17 warnings de lint preexistentes | Could | Dado `pnpm lint`, entonces hay 0 errores y 0 warnings |

## 7. Requisitos no funcionales

| ID | Categoría | Requisito |
|---|---|---|
| RNF-01 | Versiones | Java 25 LTS, Spring Boot 4.0.6, Angular 21, Node ≥ 22, pnpm 11; versiones fijadas en los manifests |
| RNF-02 | Persistencia | H2 en archivo con Flyway (`ddl-auto=validate`); en tests, H2 en memoria |
| RNF-03 | Errores | Formato RFC 9457 (`ProblemDetail` con `detail`, `errors[]`, `requestId`) |
| RNF-04 | Puertos | 8080 reservado al jar; `pnpm dev` usa el primer puerto libre entre 8081 y 8089 |
| RNF-05 | Seguridad | Sin consola H2 ni `/v3/api-docs` en el jar de producción |
| RNF-06 | Calidad | Tests backend y frontend en verde; `format:check` y lint sin errores |
| RNF-07 | Accesibilidad | Etiquetas de formulario, foco y contraste en las plantillas (deuda parcial) |

## 8. Modelo de datos preliminar `(inferido)`

- `Client`: DNI único normalizado, datos de contacto.
- `Apartment`: nombre único, capacidad, precio.
- `Booking`: fechas, noches, precio calculado, estado (`ACTIVE`/`CANCELLED`/`ARCHIVED`), `@Version`, `transactionCode` persistido; referencia a `Client` y `Apartment`.
- `booking_code_sequence`: tabla singleton para correlativos anuales.
- Migraciones V1–V5 en `backend/src/main/resources/db/migration`.

## 9. Integraciones externas

Ninguna. El navegador se abre localmente en modo app y la base es H2 en archivo.

## 10. Flujos clave

1. **Alta de reserva:** elegir cliente o cargar uno nuevo → elegir departamento y fechas → validar → guardar con código de transacción.
2. **Cancelación:** seleccionar reserva → confirmar → pasar a `CANCELLED` (soft delete) → desaparece de consultas operativas.
3. **Restauración:** abrir `/backups` → elegir copia → confirmar → se cierra la app → al reiniciar se reemplaza la base y se conserva la anterior como `.bak-*`.
4. **Desarrollo:** `pnpm dev` elige puerto libre, publica `api-port.txt`, genera el proxy y levanta Angular.

## 11. Notas de UI/UX `(inferido)`

Angular standalone + signals, Tailwind v4 con design tokens, Material Symbols e Inter locales, modo oscuro, tablas y modales compartidos en `src/app/view/assets`.

## 12. Supuestos

- S-01 `(inferido)`: el usuario buscaba cerrar una refactorización de una app ya existente, no empezar una nueva.
- S-02 `(inferido)`: no se requiere multiusuario ni acceso remoto.
- S-03 `(inferido)`: la base H2 local es suficiente; no se pidió PostgreSQL.
- S-04 `(inferido)`: el ejecutable nativo es complementario del jar, no su reemplazo.

## 13. Riesgos

| Riesgo | Impacto | Mitigación |
|---|---|---|
| El smoke del escritorio no se ha repetido tras puntos 25/28/29 y OpenAPI | Medio | Ejecutar `pnpm start` y probar crear/restaurar copia antes de cerrar la fase |
| El build nativo necesita entorno MSVC y puede requerir metadatos de reflexión | Medio | Ya verificado el 2026-11-17; documentado el comando exacto en DECISIONS |
| Los artefactos OpenAPI cambian de orden entre ejecuciones | Bajo | El test canonicaliza claves; CI compara spec y tipos |

## 14. Preguntas abiertas

- [ ] Armar el PRD definitivo con la solicitud original del usuario (§1 sigue como `(inferido)`).

**Resuelta el 2026-11-18:** retención de copias (RF-15) → máximo 10 zips y 10 `.bak-*` por configuración, sin borrar la copia pendiente.

## 15. Definición de terminado

Requisitos Must y Should cumplidos, `pnpm test` en verde (backend + frontend), `format:check` y lint sin errores, `pnpm build` generando jar, `.exe` revalidado, smoke manual del escritorio documentado y memoria (`docs/`) al día.

## 16. Historial de cambios del PRD

| Versión | Fecha | Cambio | Motivo | Confirmado por |
|---|---|---|---|---|
| 1.0 | 2026-11-18 | Versión inicial reconstruida `(inferido)` | El proyecto ya existía y no tenía PRD; se adoptó la memoria de la skill | Pendiente |
