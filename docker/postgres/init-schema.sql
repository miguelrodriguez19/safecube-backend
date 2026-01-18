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
    account_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    disabled_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS auth_refresh_tokens (
    token_id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_account_id
    ON auth_refresh_tokens (account_id);

CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_expires_at
    ON auth_refresh_tokens (expires_at);

create table if not exists user_profiles (
    user_id     uuid primary key,
    account_id  uuid not null unique,
    display_name varchar(100) not null,
    created_at  timestamp with time zone not null,
    updated_at  timestamp with time zone not null
);

CREATE TABLE IF NOT EXISTS vault_items (
    item_id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    schema_version INTEGER NOT NULL,
    display_hint VARCHAR(255) NOT NULL,
    payload BYTEA NOT NULL,
    payload_version BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_vault_items_account_id
    ON vault_items (account_id);

CREATE INDEX IF NOT EXISTS idx_vault_items_account_updated_at
    ON vault_items (account_id, updated_at);

CREATE INDEX IF NOT EXISTS idx_vault_items_account_deleted_at
    ON vault_items (account_id, deleted_at);
