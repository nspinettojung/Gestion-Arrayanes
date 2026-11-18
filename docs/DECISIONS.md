# Decisiones — Gestión Arrayanes

> **Memoria del proyecto 2/3 — POR QUÉ se hizo así.**
> Registro de decisiones de arquitectura (ADR). Solo se agrega: una decisión vieja no se borra, se marca como reemplazada.

## Índice

| ID | Título | Estado | Fecha |
|---|---|---|---|
| ADR-0001 | Stack y versiones | Aceptada | 2026-11-18 |
| ADR-0002 | Monorepo con jar de escritorio y nativo complementario | Aceptada | 2026-11-18 |
| ADR-0003 | API REST versionada con errores RFC 9457 y token local | Aceptada | 2026-11-18 |
| ADR-0004 | Copias H2 y restauración al reiniciar | Aceptada | 2026-11-18 |
| ADR-0005 | Contrato OpenAPI mínimo y tipos generados | Aceptada | 2026-11-18 |
| ADR-0006 | No actualizar Angular ni Spring Boot en esta ronda | Aceptada | 2026-11-18 |
| ADR-0007 | Adoptar la memoria de la skill en `docs/` | Aceptada | 2026-11-18 |
| ADR-0008 | Tipos de dominio derivados del contrato en cada servicio | Aceptada | 2026-11-18 |
| ADR-0009 | Retención de copias por cantidad | Aceptada | 2026-11-18 |

Estados: Propuesta · Aceptada · Reemplazada por ADR-xxxx · Descartada

---

## ADR-0001 — Stack y versiones

- **Estado:** Aceptada
- **Fecha:** 2026-11-18
- **Motiva:** RNF-01, RF-10, RF-12

**Contexto.** El proyecto ya estaba implementado y funcionando; la skill pide fijar versiones verificadas en lugar de `latest`.

**Decisión.** `(inferido del repo; verificado localmente el 2026-11-18)`

| Componente | Versión | Verificado el | Fuente |
|---|---|---|---|
| Java | 25.0.4 LTS (Temurin/HotSpot) | 2026-11-18 | `java -version`; `pom.xml` `java.version=25` |
| Spring Boot | 4.0.6 | 2026-11-18 | `spring-boot-starter-parent` en `backend/pom.xml` |
| Springdoc | 3.0.3 (`webmvc-api`) | 2026-11-17 | `backend/pom.xml`; línea para Boot 4 |
| Angular | 21.2.x | 2026-11-18 | `frontend/package.json` (`^21.2.0`, lockfile 21.2.14) |
| Node.js | 26.8.1 (el repo declara `>=22`) | 2026-11-18 | `node -v` |
| pnpm | 11.3.0 | 2026-11-18 | `pnpm -v`; `packageManager` |
| Base de datos | H2 2.4.240 | 2026-11-18 | `backend/pom.xml` / build nativo |
| Build backend | Maven wrapper (mvnw) | 2026-11-18 | `backend/mvnw.cmd` |
| Build nativo | GraalVM 25.0.4 + `native-maven-plugin` | 2026-11-17 | `native-image --version` |

**Alternativas consideradas.** Angular 22.x y Spring Boot 4.1.x están disponibles, pero se descartaron por ahora (ADR-0006). PostgreSQL se descartó porque el modo escritorio usa una base local de archivo.

**Consecuencias.** Versiones reproducibles en CI y en el equipo; la actualización futura debe hacerse como fase propia con verificación completa.

---

## ADR-0002 — Monorepo con jar de escritorio y nativo complementario

- **Estado:** Aceptada
- **Fecha:** 2026-11-18
- **Motiva:** RF-10, RF-12, RNF-04

**Contexto.** La SPA debe entregarse junta con la API y abrirse como aplicación local sin levantar dos servidores.

**Decisión.** `frontend/` + `backend/` + scripts raíz sin dependencias (`Node .mjs`). `pnpm build` compila Angular, copia a `backend/src/main/resources/static` y empaqueta el jar con `mvnw -B clean package`. `pnpm start` ejecuta ese jar con `prod` por defecto. El 8080 queda reservado al jar; `pnpm dev` usa 8081–8089 y proxy. El `.exe` nativo se genera aparte y no reemplaza al jar.

**Alternativas consideradas.** Servir el frontend por separado (descartado: rompe la entrega de escritorio); usar solo el `.exe` (descartado: el jar sigue siendo el modo soportado y testeado).

**Consecuencias.** Dos artefactos conviven en `backend/target/`. El perfil `native` marca el jar con `Spring-Boot-Native-Processed: true`, por lo que hay que regenerar el jar con `pnpm build` y restaurar `.exe`/DLL de vuelta tras compilar nativo.

---

## ADR-0003 — API REST versionada con errores RFC 9457 y token local

- **Estado:** Aceptada
- **Fecha:** 2026-11-18
- **Motiva:** RF-01, RF-02, RF-03, RF-09, RNF-03

**Decisión.** Rutas bajo `/api/v1/...`, sin aliases viejos; errores con `ProblemDetail` (type/title/status/detail + `requestId`, `timestamp`, `errors[]`). `ShutdownToken` genera un UUID por arranque, `GET /shutdown-token` solo responde a `same-origin`/`none` y `POST /shutdown` exige `X-Shutdown-Token`.

**Alternativas consideradas.** Errores con `ErrorResponseDTO` propio (reemplazado por RFC 9457); token fijo en código (descartado por seguridad).

**Consecuencias.** Los servicios Angular leen `detail`/`errors[]`; el token se pide una vez y se reutiliza.

---

## ADR-0004 — Copias H2 y restauración al reiniciar

- **Estado:** Aceptada
- **Fecha:** 2026-11-18
- **Motiva:** RF-08, RF-09

**Decisión.** `BACKUP TO` genera zips con `.mv.db` (diaria a las 3 AM, al cerrar y manual). Elegir restaurar desde la UI marca `.restore-pending` y cierra la app; `RestoreOnStartup` corre antes de Spring, guarda `mibasedatos.mv.db.bak-{fecha}` y extrae el archivo del zip con la base detenida. La opción elegida por el usuario fue "Aplicar al reiniciar (Recommended)".

**Alternativas consideradas.** `RUNSCRIPT FROM '<zip>'` (descartado: falla con `JdbcSQLSyntaxErrorException`); restaurar en caliente (descartado: riesgo de corromper H2).

**Consecuencias.** `.bak-*` y zips se acumulan: queda pendiente la política de retención (RF-15).

---

## ADR-0005 — Contrato OpenAPI mínimo y tipos generados

- **Estado:** Aceptada
- **Fecha:** 2026-11-18
- **Motiva:** RF-11, RNF-05

**Decisión.** Springdoc `3.0.3` solo variante `-api`. `OpenApiSpecTest` genera `/v3/api-docs` y canonicaliza claves; `pnpm api` copia el spec a `frontend/src/app/core/api/openapi.json` y genera `api-types.ts`. `/v3/api-docs` desactivado en `prod`. Sin Swagger UI. Los servicios no se sustituyen: solo consumen `components['schemas']` los que ya migraron.

**Alternativas consideradas.** Swagger UI en producción (descartado: no se quiere exponer); interfaces manuales duplicadas (descartado: se derivan del contrato); regenerar todos los servicios de una vez (diferido, RF-14).

**Consecuencias.** Artefactos generados excluidos de Prettier/ESLint; CI falla si spec o tipos están desactualizados.

---

## ADR-0006 — No actualizar Angular ni Spring Boot en esta ronda

- **Estado:** Aceptada
- **Fecha:** 2026-11-18
- **Motiva:** RNF-01, RNF-06

**Contexto.** La skill sugiere versiones más nuevas (Angular 22.x, Spring Boot 4.1.x), pero el proyecto está fijado y verde en Angular 21 / Spring Boot 4.0.6.

**Decisión.** Mantener las versiones actuales; tratar un eventual upgrade como fase independiente.

**Alternativas consideradas.** Actualizar ahora (descartado: mezcla migración con deuda pendiente y multiplica la verificación).

**Consecuencias.** Los RF de esta ronda no asumen APIs de versiones posteriores.

---

## ADR-0007 — Adoptar la memoria de la skill en `docs/`

- **Estado:** Aceptada
- **Fecha:** 2026-11-18
- **Motiva:** memoria persistente del proyecto

**Decisión.** Crear `docs/PRD.md`, `docs/DECISIONS.md` y `docs/PROGRESS.md` marcando como `(inferido)` lo que se reconstruyó del repo. `docs/PROGRESS.md` es el tablero vivo; `PROGRESS.md` raíz queda como puntero para el plugin; `backend/PROGRESS.md` queda como detalle histórico. `.opencode/` se ignora porque es tooling local.

**Alternativas consideradas.** Mantener dos notas de progreso duplicadas (descartado: se desincronizan); versionar la skill (descartado: no es parte del producto).

**Consecuencias.** Cualquier sesión nueva puede arrancar leyendo los tres archivos de `docs/`.

---

## ADR-0008 — Tipos de dominio derivados del contrato en cada servicio

- **Estado:** Aceptada
- **Fecha:** 2026-11-18
- **Motiva:** RF-11, RF-14, RNF-05

**Contexto.** Los DTO generados por OpenAPI marcan casi todos los campos como opcionales, pero la UI los consume como obligatorios (`booking.id`, `client.clientName`, etc.). Algunos campos además son `null` en la práctica (`transactionCode`, `lastCheckIn`) y el spec los modela como opcionales.

**Decisión.** Cada servicio Angular exporta alias derivados de `components['schemas']`, siguiendo el patrón ya usado por `BackupService`. Se aplica `Required` a los esquemas de respuesta y se reescriben explícitamente los campos nullable. Los DTO de request se usan directamente. Se borraron las interfaces manuales de `core/interface/` y vistas/specs importan los tipos desde el servicio dueño del endpoint.

**Alternativas consideradas.** Usar los DTO opcionales tal cual (descartado: obligaría a llenar la UI de aserciones y ocultaría errores reales del contrato); mantener interfaces manuales paralelas (descartado: se desincronizan del contrato); generar clientes HTTP (descartado en ADR-0005).

**Consecuencias.** Si el backend cambia un campo, `pnpm api` regenera los tipos y la compilación falla en el alias correspondiente. Los tipos siguen siendo propiedad del servicio, no un archivo global.

---

## ADR-0009 — Retención de copias por cantidad

- **Estado:** Aceptada
- **Fecha:** 2026-11-18
- **Motiva:** RF-08, RF-15

**Contexto.** Las copias diarias, de cierre y manuales, más los `.bak-*` previos a cada restauración, se acumulan sin límite en una instalación de escritorio.

**Decisión.** Al crear una copia se conservan como máximo 10 zips y 10 `.bak-*`, ordenados por fecha de modificación y nombre. El límite se configura con `arrayanes.backup.max-copies` (mínimo 1). La copia marcada en `.restore-pending` nunca se elimina. La limpieza es best-effort: un error al borrar no falla la copia recién creada. El usuario eligió esta opción ("máximo 10 copias") frente a la retención por días o la combinación de ambas.

**Alternativas consideradas.** Conservar 30 días por `createdAt` (descartado: depende del reloj y deja picos variables); combinar cantidad y antigüedad (descartado: más reglas sin beneficio para el tamaño real de las copias); no borrar nada (descartado: deja RF-15 sin resolver).

**Consecuencias.** El historial visible en la UI se acorta a 10 copias; los `.bak-*` más antiguos dejan de estar disponibles. Los tests cubren el límite, la protección de la copia pendiente y el límite configurable.
