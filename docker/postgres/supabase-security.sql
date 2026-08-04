-- SafeCube Supabase hardening.
-- Execute after init-schema.sql with an administrative role.
-- The application password is provisioned separately and is never stored here.

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

ALTER ROLE safecube_app
    LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOREPLICATION
    BYPASSRLS;

ALTER ROLE safecube_app SET search_path = public;

REVOKE ALL PRIVILEGES ON TABLE public.auth_accounts
    FROM anon, authenticated, service_role;
REVOKE ALL PRIVILEGES ON TABLE public.auth_accounts FROM PUBLIC;

REVOKE ALL PRIVILEGES ON TABLE public.auth_refresh_tokens
    FROM anon, authenticated, service_role;
REVOKE ALL PRIVILEGES ON TABLE public.auth_refresh_tokens FROM PUBLIC;

REVOKE ALL PRIVILEGES ON TABLE public.user_profiles
    FROM anon, authenticated, service_role;
REVOKE ALL PRIVILEGES ON TABLE public.user_profiles FROM PUBLIC;

REVOKE ALL PRIVILEGES ON TABLE public.vault_item_change_cursors
    FROM anon, authenticated, service_role;
REVOKE ALL PRIVILEGES ON TABLE public.vault_item_change_cursors FROM PUBLIC;

REVOKE ALL PRIVILEGES ON TABLE public.vault_items
    FROM anon, authenticated, service_role;
REVOKE ALL PRIVILEGES ON TABLE public.vault_items FROM PUBLIC;

REVOKE ALL PRIVILEGES ON TABLE public.vault_item_mutations
    FROM anon, authenticated, service_role;
REVOKE ALL PRIVILEGES ON TABLE public.vault_item_mutations FROM PUBLIC;

REVOKE ALL PRIVILEGES ON TABLE public.vault_key_material
    FROM anon, authenticated, service_role;
REVOKE ALL PRIVILEGES ON TABLE public.vault_key_material FROM PUBLIC;

-- The script must run as the role that creates the public tables. This is
-- postgres in Supabase and the Testcontainers database user in integration tests.
DO $$
BEGIN
    EXECUTE format(
        'ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA public REVOKE ALL ON TABLES FROM anon, authenticated, service_role',
        current_user
    );
END
$$;

GRANT USAGE ON SCHEMA public TO safecube_app;

-- Permissions are derived from the repository operations. Physical DELETE,
-- TRUNCATE, REFERENCES and TRIGGER are intentionally not granted.
GRANT SELECT, INSERT, UPDATE
    ON TABLE public.auth_accounts
    TO safecube_app;

GRANT SELECT, INSERT, UPDATE
    ON TABLE public.auth_refresh_tokens
    TO safecube_app;

GRANT SELECT, INSERT, UPDATE
    ON TABLE public.user_profiles
    TO safecube_app;

GRANT SELECT, INSERT, UPDATE
    ON TABLE public.vault_item_change_cursors
    TO safecube_app;

GRANT SELECT, INSERT, UPDATE
    ON TABLE public.vault_items
    TO safecube_app;

GRANT SELECT, INSERT
    ON TABLE public.vault_item_mutations
    TO safecube_app;

GRANT SELECT, INSERT, UPDATE
    ON TABLE public.vault_key_material
    TO safecube_app;
