# Database Strategy – SafeCube Backend

Este documento define la **estrategia de base de datos** del backend de SafeCube, incluyendo principios de diseño,
responsabilidades por slice y el esquema actual soportado.

---

## 1. Principios de Diseño

La base de datos de SafeCube sigue los siguientes principios:

* **Single Source of Truth**: el esquema SQL es la referencia definitiva del modelo persistente.
* **Explicit lifecycle**: los estados relevantes (enabled, revoked, expired, disabled) se representan explícitamente
  mediante columnas.
* **No implicit deletes**: no se utilizan borrados lógicos implícitos salvo decisión explícita documentada.
* **Infrastructure-agnostic domain**: el dominio no depende de JPA ni de detalles de persistencia.

---

## 2. Estrategia de Entornos

### 2.1 Local Development

* Base de datos PostgreSQL levantada vía **docker-compose**.
* Inicialización mediante `docker/postgres/init-schema.sql`.

### 2.2 Tests (Integration / Acceptance)

* Uso de **Testcontainers**.
* El esquema se copia automáticamente durante el build y se monta como script de inicialización.
* No existen migraciones dinámicas en tests.

### 2.3 Producción

* La base de datos se gestiona de forma **manual/administrada**.
* Alojada en un servidor web dedicado.
* No se ejecutan scripts automáticos desde la aplicación.

---

## 3. Esquema Actual (v1)

### 3.1 auth_accounts

```sql
CREATE TABLE IF NOT EXISTS auth_accounts (
    account_id UUID PRIMARY KEY,
    email VARCHAR (255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    disabled_at TIMESTAMP WITH TIME ZONE
);
```

Responsabilidad:

* Representa la **identidad autenticable**.
* Controla si una cuenta puede autenticarse (`enabled`).

---

### 3.2 auth_refresh_tokens

```sql
CREATE TABLE IF NOT EXISTS auth_refresh_tokens (
    token_id UUID PRIMARY KEY, 
    account_id UUID NOT NULL, 
    token_hash VARCHAR( 255 ) NOT NULL UNIQUE, 
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL, 
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, 
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_account_id
    ON auth_refresh_tokens (account_id);

CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_expires_at
    ON auth_refresh_tokens (expires_at);
```

Responsabilidad:

* Persistencia de refresh tokens.
* Soporta rotación, revocación y expiración explícita.

---

### 3.3 user_profiles

```sql
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id UUID PRIMARY KEY, a
    ccount_id UUID NOT NULL UNIQUE, 
    display_name VARCHAR ( 100 ) NOT NULL, 
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, 
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

Responsabilidad:

* Información de perfil **no sensible**.
* Relación 1:1 con `auth_accounts`.

---

### 3.4 vault_secure_items

```sql
CREATE TABLE IF NOT EXISTS vault_secure_items (
    item_id UUID PRIMARY KEY, 
    account_id UUID NOT NULL, 
    item_type VARCHAR (50) NOT NULL, 
    schema_version INTEGER NOT NULL, 
    display_hint VARCHAR(255) NOT NULL, 
    payload BYTEA NOT NULL, 
    payload_version BIGINT NOT NULL, 
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, 
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL, 
    deleted_at TIMESTAMP WITH TIME ZONE
);

   CREATE INDEX IF NOT EXISTS idx_vault_items_account_id ON vault_secure_items (account_id);
   CREATE INDEX IF NOT EXISTS idx_vault_items_updated_at ON vault_secure_items (updated_at);
   CREATE INDEX IF NOT EXISTS idx_vault_items_deleted_at ON vault_secure_items (deleted_at);
```

Responsabilidad:

- Persistencia de secretos cifrados (opaque payloads).
- Aislamiento estricto por account_id.
- Soporte de sincronización mediante timestamps.
- Implementa borrado lógico explícito (deleted_at).

💡 Nota: aunque internamente uses otro nombre de tabla, el **concepto debe existir** en el doc.

---

### 3.5 vault_key_material

```sql
CREATE TABLE IF NOT EXISTS vault_key_material(
    account_id UUID PRIMARY KEY,
    kek_enc_master BYTEA NOT NULL,
    kek_enc_recovery BYTEA NOT NULL,
    kdf_algorithm VARCHAR (50) NOT NULL,
    kdf_salt BYTEA NOT NULL,
    kdf_memory_kib INTEGER NOT NULL,
    kdf_iterations INTEGER NOT NULL,
    kdf_parallelism INTEGER NOT NULL,
    kdf_output_len INTEGER NOT NULL,
    crypto_version VARCHAR (50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_vault_key_material_account
    FOREIGN KEY (account_id)
    REFERENCES auth_accounts(account_id)
    ON DELETE CASCADE
);
```

Responsabilidad:

- Almacena el material criptográfico necesario para desbloquear el vault de un account
- Backend no puede derivar claves ni descifrar contenido. Todo el material se almacena como blobs opacos.

---

## 4. Relaciones Entre Tablas

* auth_accounts (1) —— (0..*) auth_refresh_tokens
* auth_accounts (1) —— (0..1) user_profiles
* auth_accounts (1) —— (0..*) vault_secure_items
* auth_accounts (1) ── (1) vault_key_material

> La relación entre `auth_accounts` y `vault_secure_items`
> es **lógica**, no implementada mediante foreign keys físicas en v1.

---

## 5. ERD (Entity Relationship Diagram)

```mermaid
erDiagram
    AUTH_ACCOUNTS {
        UUID account_id PK
        VARCHAR email
        VARCHAR password_hash
        BOOLEAN enabled
        TIMESTAMP created_at
        TIMESTAMP disabled_at
    }

    AUTH_REFRESH_TOKENS {
        UUID token_id PK
        UUID account_id
        VARCHAR token_hash
        TIMESTAMP expires_at
        TIMESTAMP created_at
        TIMESTAMP revoked_at
    }

    USER_PROFILES {
        UUID user_id PK
        UUID account_id
        VARCHAR display_name
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    
    VAULT_SECURE_ITEMS {
        UUID item_id PK
        UUID account_id
        VARCHAR item_type
        INTEGER schema_version
        VARCHAR display_hint
        BYTEA payload
        BIGINT payload_version
        TIMESTAMP created_at
        TIMESTAMP updated_at
        TIMESTAMP deleted_at
    }
    
    vault_key_material {
        UUID account_id
        BYTEA kek_enc_master
        BYTEA kek_enc_recovery
        VARCHAR kdf_algorithm
        BYTEA kdf_salt
        INTEGER kdf_memory_kib
        INTEGER kdf_iterations
        INTEGER kdf_parallelism
        INTEGER kdf_output_len
        VARCHAR crypto_version
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }


    AUTH_ACCOUNTS ||--o{ AUTH_REFRESH_TOKENS : has
    AUTH_ACCOUNTS ||--o| USER_PROFILES : owns
    AUTH_ACCOUNTS ||--o{ VAULT_SECURE_ITEMS : owns
    AUTH_ACCOUNTS ||--o| VAULT_SECURE_ITEMS : owns

```

---

## 6. Decisiones Explícitas

* No existen `FOREIGN KEY` físicas en v1.
* No existe `deleted_at` en tablas de identidad (`auth`, `user`).
* El slice `vault` implementa borrado lógico explícito (`deleted_at`)
  para soportar sincronización entre clientes.
* No hay migraciones automáticas (Flyway/Liquibase) en v1.
* El esquema evoluciona mediante decisiones arquitectónicas documentadas (ADR).
* El slice `vault` utiliza timestamps (`created_at`, `updated_at`, `deleted_at`)
  como mecanismo de sincronización y concurrencia.
* No se utilizan locks ni versionado optimista JPA.

---

## 7. Evolución Futura

Posibles extensiones futuras:

* FK opcionales cuando el modelo se estabilice.
* Tablas de auditoría.
* Estrategia de borrado orquestado (GDPR).

---

> **Regla de oro**: la base de datos refleja estados reales del sistema, no suposiciones implícitas.
