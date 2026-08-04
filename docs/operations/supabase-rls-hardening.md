# Runbook: hardening RLS de Supabase

Este runbook configura el acceso privado de SafeCube a PostgreSQL de Supabase.
La contraseña real nunca debe guardarse en Git, en estos scripts, en tickets ni
en logs.

## Precondiciones

* Tener una copia o respaldo verificable de la base de datos.
* Disponer de una conexión administrativa a Supabase.
* Confirmar que la base de producción está vacía respecto a las tablas de
  SafeCube. No eliminar esquemas gestionados por Supabase.
* Disponer de acceso administrativo para ejecutar el bootstrap una sola vez.
* Disponer de un entorno GitHub `production` con aprobación requerida.

## Orden de ejecución

1. Hacer un backup verificable y comprobar que no existen tablas de SafeCube en
   `public`.
2. Ejecutar `docker/postgres/supabase-security.sql` una única vez con el rol
   administrativo de Supabase. El script crea `safecube_app`,
   `safecube_migrator` y `safecube_meta`, pero no contiene contraseñas.
3. Establecer las contraseñas de ambos roles en el gestor de secretos:

   ```sql
   ALTER ROLE safecube_app
   WITH PASSWORD '<REPLACE_WITH_SECRET>';
   ALTER ROLE safecube_migrator
   WITH PASSWORD '<REPLACE_WITH_SECRET>';
   ```

4. Configurar en el entorno GitHub `production` los secretos:

   ```text
   SUPABASE_MIGRATOR_JDBC_URL=<SUPABASE_JDBC_URL_WITH_SSL>
   SUPABASE_MIGRATOR_USERNAME=safecube_migrator
   SUPABASE_MIGRATOR_PASSWORD=<MIGRATOR_SECRET>
   ```

5. Fusionar el cambio en `main` y aprobar el job `migrate-production`.
6. Flyway ejecutará `info`, `validate` y `migrate`; en la primera ejecución
   aplicará `V1` y `V2`. No se ejecuta `flyway baseline`.
7. Verificar `safecube_meta.flyway_schema_history`, RLS, propietarios y grants.
8. Permitir el despliegue de Koyeb con `safecube_app`.
9. Ejecutar smoke tests de registro, login, refresh, perfil y vault, además de
   los acceptance tests.
10. Revisar Supabase Security Advisor.

El bootstrap no almacena contraseñas. Flyway crea las tablas como
`safecube_migrator`, que es su propietario y tiene capacidad DDL. Si la
verificación inicial encuentra tablas de SafeCube, detener el procedimiento e
inspeccionar la base: `V1` debe fallar ante objetos inesperados.

## Configuración de Koyeb

Definir estas variables como secretos/configuración del servicio:

```text
DATABASE_USERNAME=safecube_app
DATABASE_PASSWORD=<KOYEB_SECRET>
DATABASE_URL=<SUPABASE_JDBC_URL_WITH_SSL>
```

`DATABASE_URL` debe ser la cadena JDBC proporcionada por Supabase y debe incluir
`sslmode=require`. Usar conexión directa si Koyeb puede alcanzar IPv6; usar
Supavisor en session mode si se necesita compatibilidad IPv4. No usar transaction
mode salvo una necesidad demostrada.

No usar el usuario `postgres` para Spring Boot, `safecube_migrator` para Koyeb,
ni claves `SUPABASE_ANON_KEY` o
`SUPABASE_SERVICE_ROLE_KEY`.

## Configuración de Flyway

El job protegido de GitHub utiliza `flyway/flyway:13.1.0` y monta únicamente
`db/migrations` en modo lectura. La configuración efectiva es:

```text
locations=filesystem:/flyway/sql
schemas=safecube_meta,public
defaultSchema=safecube_meta
table=flyway_schema_history
cleanDisabled=true
baselineOnMigrate=false
outOfOrder=false
ignoreMigrationPatterns=*:pending
```

`*:pending` se ignora únicamente para permitir que `validate` se ejecute antes
de `migrate` sobre una base vacía; las migraciones aplicadas y sus checksums
siguen validándose.

Las migraciones aplicadas son inmutables. Una corrección futura se implementa
con una nueva migración `V`; nunca se modifica una versión ya registrada.

## Queries de verificación

### RLS

```sql
SELECT schemaname, tablename, rowsecurity
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename;
```

Las siete tablas de SafeCube deben devolver `rowsecurity = true`.

### Privilegios de la Data API

```sql
SELECT table_name, grantee, privilege_type
FROM information_schema.role_table_grants
WHERE table_schema = 'public'
  AND grantee IN ('anon', 'authenticated', 'service_role')
  AND table_name IN (
      'auth_accounts', 'auth_refresh_tokens', 'user_profiles',
      'vault_item_change_cursors', 'vault_items',
      'vault_item_mutations', 'vault_key_material'
  )
ORDER BY table_name, grantee, privilege_type;
```

El resultado esperado es cero filas.

### Rol de aplicación

```sql
SELECT rolname, rolcanlogin, rolsuper, rolcreatedb, rolcreaterole,
       rolreplication, rolbypassrls
FROM pg_roles
WHERE rolname = 'safecube_app';
```

El rol debe poder iniciar sesión, no ser superusuario, no crear bases ni roles,
no replicar y tener `rolbypassrls = true`.

### Propiedad de tablas

```sql
SELECT tablename, tableowner
FROM pg_tables
WHERE schemaname = 'public'
  AND tablename IN (
      'auth_accounts', 'auth_refresh_tokens', 'user_profiles',
      'vault_item_change_cursors', 'vault_items',
      'vault_item_mutations', 'vault_key_material'
  )
ORDER BY tablename;
```

Ninguna tabla de SafeCube debe tener como propietario a `safecube_app`.

### Historial de Flyway

```sql
SELECT installed_rank, version, description, success
FROM safecube_meta.flyway_schema_history
ORDER BY installed_rank;
```

La primera puesta en producción debe mostrar `V1` y `V2` con `success = true`.

### Roles y DDL

```sql
SELECT rolname, rolcanlogin, rolsuper, rolcreatedb, rolcreaterole,
       rolreplication, rolbypassrls
FROM pg_roles
WHERE rolname IN ('safecube_app', 'safecube_migrator')
ORDER BY rolname;
```

`safecube_app` debe tener únicamente los grants de aplicación. El migrador es
el propietario de las tablas y el único de los dos roles con `CREATE` sobre
`public`.

## Rollback

1. Detener Koyeb.
2. Restaurar el respaldo si la reconstrucción afectó datos.
3. Revocar o rotar la contraseña de `safecube_app` en el gestor de secretos.
4. Restaurar la configuración anterior solo tras revisar los privilegios y el
   riesgo; no volver a publicar credenciales en el repositorio.
5. Repetir las verificaciones y desplegar únicamente después de validar la
   conexión privada.

No eliminar el rol ni deshabilitar RLS como primer mecanismo de rollback: la
restauración de datos y la rotación de credenciales son reversibles y auditables.

## Validación final

Confirmar que:

* no existen policies basadas en `auth.uid()`;
* la Data API no tiene privilegios sobre tablas de SafeCube;
* Spring Boot conecta como `safecube_app` con SSL;
* los tests de seguridad de base de datos y aislamiento HTTP pasan;
* Security Advisor ya no muestra `rls_disabled_in_public`.
