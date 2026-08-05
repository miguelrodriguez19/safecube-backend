-- SafeCube least-privilege database access.
-- Roles supplied by Supabase are optional in local PostgreSQL, so their
-- revocations are applied dynamically when they exist.

REVOKE ALL PRIVILEGES ON SCHEMA public FROM PUBLIC;
REVOKE ALL PRIVILEGES ON SCHEMA public FROM safecube_app;
GRANT USAGE ON SCHEMA public TO safecube_app;
GRANT USAGE, CREATE ON SCHEMA public TO safecube_migrator;

DO $$
DECLARE
    role_name TEXT;
    table_name TEXT;
    table_names CONSTANT TEXT[] := ARRAY[
        'auth_accounts',
        'auth_refresh_tokens',
        'user_profiles',
        'vault_item_change_cursors',
        'vault_items',
        'vault_item_mutations',
        'vault_key_material'
    ];
    api_roles CONSTANT TEXT[] := ARRAY['anon', 'authenticated', 'service_role'];
BEGIN
    FOREACH role_name IN ARRAY api_roles LOOP
        IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = role_name) THEN
            EXECUTE format('REVOKE ALL PRIVILEGES ON SCHEMA public FROM %I', role_name);
        END IF;
    END LOOP;

    FOREACH table_name IN ARRAY table_names LOOP
        EXECUTE format('REVOKE ALL PRIVILEGES ON TABLE public.%I FROM PUBLIC', table_name);

        FOREACH role_name IN ARRAY api_roles LOOP
            IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = role_name) THEN
                EXECUTE format(
                    'REVOKE ALL PRIVILEGES ON TABLE public.%I FROM %I',
                    table_name,
                    role_name
                );
            END IF;
        END LOOP;
    END LOOP;

    EXECUTE format(
        'ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA public REVOKE ALL ON TABLES FROM PUBLIC',
        current_user
    );

    FOREACH role_name IN ARRAY api_roles LOOP
        IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = role_name) THEN
            EXECUTE format(
                'ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA public REVOKE ALL ON TABLES FROM %I',
                current_user,
                role_name
            );
        END IF;
    END LOOP;
END
$$;

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
