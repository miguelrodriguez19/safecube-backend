-- SafeCube database roles bootstrap.
--
-- Execute once with a Supabase administrative role before running Flyway.
-- Passwords are intentionally not stored in this file. Provision them through
-- the secret manager or the local orchestration environment.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_roles
        WHERE rolname = 'safecube_app'
    ) THEN
        CREATE ROLE safecube_app
            LOGIN
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            NOREPLICATION
            BYPASSRLS;
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_roles
        WHERE rolname = 'safecube_migrator'
    ) THEN
        CREATE ROLE safecube_migrator
            LOGIN
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            NOREPLICATION
            NOBYPASSRLS;
    END IF;
END
$$;

ALTER ROLE safecube_app
    LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOREPLICATION
    BYPASSRLS;

ALTER ROLE safecube_app SET search_path = public;
ALTER ROLE safecube_migrator
    LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOREPLICATION
    NOBYPASSRLS;
ALTER ROLE safecube_migrator SET search_path = safecube_meta, public;

CREATE SCHEMA IF NOT EXISTS safecube_meta AUTHORIZATION safecube_migrator;
ALTER SCHEMA safecube_meta OWNER TO safecube_migrator;

REVOKE ALL PRIVILEGES ON SCHEMA safecube_meta FROM PUBLIC;
GRANT USAGE, CREATE ON SCHEMA safecube_meta TO safecube_migrator;

-- Flyway creates SafeCube tables as safecube_migrator. The application role
-- receives object privileges only from versioned migrations and never gets
-- CREATE on the public schema.
REVOKE ALL PRIVILEGES ON SCHEMA public FROM PUBLIC;
REVOKE ALL PRIVILEGES ON SCHEMA public FROM safecube_app;
GRANT USAGE ON SCHEMA public TO safecube_app;
GRANT USAGE, CREATE ON SCHEMA public TO safecube_migrator;

ALTER DEFAULT PRIVILEGES FOR ROLE safecube_migrator IN SCHEMA public
    REVOKE ALL ON TABLES FROM PUBLIC;
ALTER DEFAULT PRIVILEGES FOR ROLE safecube_migrator IN SCHEMA safecube_meta
    REVOKE ALL ON TABLES FROM PUBLIC;

-- No passwords belong in source control. Set them outside this script, e.g.:
-- ALTER ROLE safecube_app WITH PASSWORD '<secret-from-secret-manager>';
-- ALTER ROLE safecube_migrator WITH PASSWORD '<secret-from-secret-manager>';
