-- =========================================================
-- SAFE CUBE DATABASE SCHEMA
--
-- SOURCE OF TRUTH
-- DO NOT DUPLICATE
-- DO NOT MODIFY FROM TESTS OR MIGRATIONS
--
-- This file is used by:
--  - Docker Compose (local dev & acceptance tests)
--  - Testcontainers (via Maven copy)
--
-- This schema is used ONLY for:
--  - local development via docker-compose
--  - integration / acceptance tests via Testcontainers
-- =========================================================


CREATE TABLE IF NOT EXISTS auth_accounts (
    account_id      UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    enabled         BOOLEAN NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    disabled_at     TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_auth_accounts_enabled_state
        CHECK (
            (enabled = TRUE  AND disabled_at IS NULL) OR
            (enabled = FALSE AND disabled_at IS NOT NULL)
            )
);


CREATE TABLE IF NOT EXISTS auth_refresh_tokens (
    token_id    UUID PRIMARY KEY,
    account_id  UUID NOT NULL,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at  TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_auth_refresh_tokens_account
        FOREIGN KEY (account_id)
            REFERENCES auth_accounts (account_id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_auth_refresh_tokens_expiry
        CHECK (expires_at > created_at)
);

CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_account_id
    ON auth_refresh_tokens (account_id);

CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_expires_at
    ON auth_refresh_tokens (expires_at);


CREATE TABLE IF NOT EXISTS user_profiles (
    user_id         UUID PRIMARY KEY,
    account_id      UUID NOT NULL UNIQUE,
    display_name    VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_user_profiles_account
        FOREIGN KEY (account_id)
            REFERENCES auth_accounts (account_id)
            ON DELETE RESTRICT
);


CREATE TABLE IF NOT EXISTS vault_items (
    item_id             UUID PRIMARY KEY,
    account_id          UUID NOT NULL,
    item_type           VARCHAR(50) NOT NULL,
    schema_version      INTEGER NOT NULL,
    display_hint        VARCHAR(255) NOT NULL,
    payload             BYTEA NOT NULL,
    payload_version     BIGINT NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at          TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_vault_items_account
        FOREIGN KEY (account_id)
            REFERENCES auth_accounts (account_id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_vault_items_versions
        CHECK (
            schema_version >= 0 AND
            payload_version > 0
            ),

    CONSTRAINT chk_vault_items_payload_size
        CHECK (octet_length(payload) <= 1048576)  /* 1MB */
);

CREATE INDEX IF NOT EXISTS idx_vault_items_account_id
    ON vault_items (account_id);

CREATE INDEX IF NOT EXISTS idx_vault_items_account_updated_at
    ON vault_items (account_id, updated_at);

CREATE INDEX IF NOT EXISTS idx_vault_items_account_deleted_at
    ON vault_items (account_id, deleted_at);

CREATE INDEX IF NOT EXISTS idx_vault_items_active_account_updated
    ON vault_items (account_id, updated_at)
    WHERE deleted_at IS NULL;


CREATE TABLE IF NOT EXISTS vault_key_material (
    account_id          UUID PRIMARY KEY,
    kek_enc_master      BYTEA NOT NULL,
    kek_enc_recovery    BYTEA NOT NULL,
    kdf_algorithm       VARCHAR(50) NOT NULL,
    kdf_salt            BYTEA NOT NULL,
    kdf_memory_kib      INTEGER NOT NULL,
    kdf_iterations      INTEGER NOT NULL,
    kdf_parallelism     INTEGER NOT NULL,
    kdf_output_len      INTEGER NOT NULL,
    crypto_version      VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_vault_key_material_account
        FOREIGN KEY (account_id)
            REFERENCES auth_accounts (account_id)
            ON DELETE CASCADE
);
