# Runbook: hardening RLS de Supabase

Este runbook configura el acceso privado de SafeCube a PostgreSQL de Supabase.
La contraseña real nunca debe guardarse en Git, en estos scripts, en tickets ni
en logs.

## Precondiciones

* Tener una copia o respaldo verificable de la base de datos.
* Disponer de una conexión administrativa a Supabase.
* Confirmar el rol que crea las tablas. El script de producción debe ejecutarse
  con ese rol; normalmente es `postgres`.
* Detener temporalmente el despliegue de Koyeb durante una reconstrucción.

## Orden de ejecución

1. Detener temporalmente Koyeb si se va a reconstruir la base.
2. Ejecutar `docker/postgres/init-schema.sql` para crear el esquema y habilitar
   RLS en las siete tablas de SafeCube.
3. Ejecutar `docker/postgres/supabase-security.sql` con el rol administrativo.
4. Establecer la contraseña de `safecube_app` usando el gestor de secretos:

   ```sql
   ALTER ROLE safecube_app
   WITH PASSWORD '<REPLACE_WITH_SECRET>';
   ```

5. Configurar los secretos de Koyeb.
6. Desplegar Spring Boot.
7. Ejecutar smoke tests de registro, login, refresh, perfil y vault.
8. Ejecutar los acceptance tests.
9. Revisar Supabase Security Advisor.

El script de hardening no cambia propietarios ni almacena contraseñas. Si el
propietario real que crea las tablas no es el rol que ejecuta el script, detener
el procedimiento y ajustar la sentencia `ALTER DEFAULT PRIVILEGES` al propietario
correcto antes de continuar.

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

No configurar `DB_USER`, `DB_PWD` o `DB_URL` para este despliegue, no usar el
usuario `postgres`, y no usar claves `SUPABASE_ANON_KEY` o
`SUPABASE_SERVICE_ROLE_KEY`.

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
