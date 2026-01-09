CREATE TABLE IF NOT EXISTS auth_accounts (
    account_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    disabled_at TIMESTAMP WITH TIME ZONE
);

