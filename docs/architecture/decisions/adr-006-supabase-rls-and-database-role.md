# ADR-006: RLS de Supabase y rol privado de base de datos

* **Estado**: Accepted
* **Fecha**: 2026-08-04
* **Decisores**: SafeCube Backend
* **Relacionado con**:
  * `db/migrations/V1__initial_schema.sql`
  * `db/migrations/V2__configure_database_access.sql`
  * `docker/postgres/supabase-security.sql`
  * `docs/architecture/database_strategy.md`

## 1. Contexto

SafeCube utiliza Spring Boot como único backend. Los clientes se autentican
contra el slice `auth`, que emite los JWT propios y obtiene el `accountId` del
contexto de seguridad. Ningún cliente debe consultar las tablas directamente.

Las tablas de SafeCube viven en el esquema `public` de Supabase. El Security
Advisor detectó la incidencia crítica `rls_disabled_in_public` porque las tablas
no tenían Row Level Security habilitada.

## 2. Incidencia

La ausencia de RLS deja las tablas del esquema expuesto sin una defensa de base
de datos frente a accesos accidentales o configuraciones futuras de la Data API.
Los permisos SQL y RLS son controles distintos, por lo que ambos deben quedar
restringidos explícitamente.

## 3. Arquitectura de acceso

El flujo aprobado es:

```text
Cliente -> Spring Boot -> PostgreSQL/Supabase
             |                 |
             |                 +-- safecube_app (JDBC privado)
             +-- JWT propio + accountId
```

Supabase Auth, `auth.uid()`, `SUPABASE_ANON_KEY` y
`SUPABASE_SERVICE_ROLE_KEY` no forman parte de la arquitectura.

## 4. Decisión

Se habilita RLS en las siete tablas de SafeCube y no se crean políticas. La
denegación por defecto protege a los roles sujetos a RLS. `V1` crea el esquema
determinísticamente y `V2` revoca los privilegios de `anon`, `authenticated`,
`service_role` y `PUBLIC`, y concede permisos mínimos a un rol privado para
Spring Boot. Un bootstrap administrativo crea los roles y el esquema privado
de historial antes de la primera migración.

## 5. Rol `safecube_app`

`safecube_app` tiene `LOGIN`, `NOSUPERUSER`, `NOCREATEDB`, `NOCREATEROLE`,
`NOREPLICATION` y `BYPASSRLS`. No es propietario de las tablas. Su contraseña se
configura con el gestor de secretos del entorno y no aparece en SQL, Git ni
logs.

El `BYPASSRLS` está limitado a este rol confiable porque la autorización de
negocio no se delega a PostgreSQL. El rol no es una credencial para clientes.

## 6. Revocación de roles de la Data API

Cada tabla recibe `REVOKE ALL PRIVILEGES` para `anon`, `authenticated`,
`service_role` y `PUBLIC`. La lista es explícita para no modificar objetos de
extensiones ni componentes externos de Supabase.

## 7. RLS sin políticas

No se añaden `POLICY`, `FORCE ROW LEVEL SECURITY` ni expresiones `auth.uid()`.
Las tablas tienen RLS habilitada mediante `V1`, que se utiliza tanto en Docker
Compose como en Testcontainers y producción.

## 8. Motivo para no utilizar `auth.uid()`

SafeCube no utiliza Supabase Auth. El JWT de la aplicación no es un JWT de
Supabase y la identidad se obtiene del filtro de seguridad propio. Una política
basada en `auth.uid()` no representaría la identidad real del sistema.

## 9. Motivo para utilizar `BYPASSRLS`

El backend es un servicio confiable que necesita realizar consultas multi-tabla
con la identidad ya validada en Spring Boot. RLS sin políticas bloquearía ese
tráfico aunque los permisos de objeto fueran correctos. `BYPASSRLS` mantiene la
RLS activa para el resto de roles y evita inventar políticas que no pueden
interpretar el JWT propio.

## 10. Responsabilidad de autorización de Spring Boot

La autorización permanece en los casos de uso y adaptadores existentes:

* vault obtiene y filtra siempre por el `accountId` autenticado;
* material de claves y perfiles se buscan por `accountId`;
* refresh tokens se revocan según la cuenta autenticada;
* los controllers construyen commands/queries a partir de
  `@AuthenticationPrincipal` y no aceptan un propietario desde body, path o
  query.

La auditoría de esta tarea no encontró una vulnerabilidad de aislamiento, por lo
que no se modifica el dominio ni se introduce una abstracción de autorización.

## 11. Consecuencias

* La Data API no puede leer ni escribir las tablas de SafeCube.
* Una reconstrucción del esquema conserva RLS porque forma parte de `V1`.
* La contraseña y los secretos de Koyeb requieren configuración operativa.
* Un error de autorización en Spring Boot seguiría siendo relevante porque
  `safecube_app` omite RLS.
* Las tablas nuevas requieren revisión explícita de RLS, privilegios y propiedad.

## 12. Alternativas rechazadas

* Políticas RLS basadas en `auth.uid()`.
* Uso de Supabase Auth.
* Uso de la service-role key.
* Conexión de Spring Boot como `postgres` o como propietario de las tablas.
* Transaction pooler como opción por defecto para un backend persistente.
* Migraciones ejecutadas durante el arranque de Spring Boot.

## 13. Estrategia de tests

Testcontainers inicia PostgreSQL vacío, ejecuta el bootstrap de roles y aplica
`V1` y `V2` con Flyway. Los tests de integración comprueban que una segunda
ejecución no aplica cambios, que `safecube_app` no puede ejecutar DDL, que
`safecube_migrator` sí puede hacerlo, además de RLS, denegación de `anon`,
`authenticated` y `service_role`, permisos mínimos y atributos de los roles.
Los acceptance tests cubren el aislamiento HTTP entre dos cuentas.

## 14. Procedimiento de despliegue

El procedimiento operativo está en
`docs/operations/supabase-rls-hardening.md`. En resumen: hacer backup, verificar
que el esquema de SafeCube está vacío, ejecutar una vez el bootstrap, configurar
los tres secretos de Flyway en el entorno protegido de GitHub, aprobar la
migración `V1`/`V2`, desplegar Koyeb con `safecube_app`, ejecutar smoke/acceptance
tests y revisar Security Advisor.
