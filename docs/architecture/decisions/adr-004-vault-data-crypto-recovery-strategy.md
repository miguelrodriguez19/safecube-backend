# ADR-004: Vault Data, Crypto & Recovery Strategy

- **Estado**: Accepted
- **Fecha**: 2026-01-17
- **Decisores**: SafeCube Backend
- **Slices afectados**: `vault`, `auth`

---

## Contexto

El slice `vault` es el núcleo funcional de SafeCube. Su objetivo es permitir el almacenamiento y sincronización de secretos de forma segura, manteniendo un modelo **Zero-Knowledge real**, sin sacrificar usabilidad básica ni capacidad de recuperación razonable.

Durante el diseño se identificaron varias áreas críticas que debían cerrarse **antes de implementar código**:

- Modelo criptográfico cliente/backend
- Uso de la passphrase del usuario
- Recuperación ante pérdida de contraseña
- Concurrencia y sincronización
- Borrado de datos
- Filtros y ordenación para UX

Este ADR consolida todas las decisiones acordadas.

---

## Decisión 1 — Separación Login vs Vault Unlock y estado de cuenta

La **passphrase del usuario** cumple dos roles distintos, y su uso está condicionado al **estado de la cuenta**.

- **Login (auth)**

  - La passphrase se envía al backend por HTTPS
  - Se usa exclusivamente para autenticación
  - Se almacena como `password_hash`
  - El login puede quedar **bloqueado o limitado** si la cuenta no está validada

- **Vault Unlock (crypto)**

  - La misma passphrase se usa **solo en cliente**
  - Se deriva una `MASTER_KEY` mediante KDF
  - La `MASTER_KEY` **nunca sale del cliente**
  - El vault \*\*solo\*\* puede inicializarse o abrirse cuando la cuenta está en estado \`ACTIVE\`

Se introduce el concepto de **estado de cuenta** para permitir flujos futuros como:

- Verificación de email
- 2FA / MFA
- Account recovery asistido
- Otros challenges de seguridad

Estados conceptuales iniciales:

- `PENDING_VERIFICATION`
- `ACTIVE`
- `SUSPENDED`
- `DISABLED`

> Login ≠ Vault unlock, y ambos dependen del estado de la cuenta.

Nota: queda fuera de MVP el manejo de los challenge, por defecto al hacer el registro se dejará automaticamente el estado del AuthAccount como `ACTIVE`. Posteriormente cuando dichos challenge se desarrollen, ya se verá.

### Esquema de Register (cuenta pendiente + activación + inicialización de vault)

```mermaid
sequenceDiagram
    participant U as Usuario
    participant C as Cliente
    participant B as Backend

    U->>C: Introduce email + passphrase
    C->>B: POST /auth/register (email, password)

    alt 🟢 SIN CHALLENGES (MVP)
        B->>B: Crea cuenta
        B->>B: Estado = ACTIVE
        B->>C: 201 Created (ACTIVE)

    else 🟡 CON CHALLENGES (futuro)
        B->>B: Crea cuenta
        B->>B: Estado = PENDING_VERIFICATION
        B->>C: 202 Accepted (PENDING_VERIFICATION)

        Note over B,C: Challenge(s) futuros<br>(email verification, 2FA, etc.)

        U->>C: Completa challenge
        C->>B: POST /auth/verify-email (token)
        B->>B: Estado = ACTIVE
        B->>C: 200 OK (ACTIVE)
    end

```

### Esquema de Login / Uso diario

```mermaid
sequenceDiagram
    participant U as Usuario
    participant C as Cliente
    participant B as Backend

    U->>C: Introduce passphrase
    C->>B: POST /auth/login (password)
    B->>B: Verifica password_hash

    alt 🔴 Cuenta NO ACTIVE
        B->>C: 403 ChallengeRequired / AccountNotActive
        Note over C: ❌ No hay JWT usable
        Note over C: ❌ Vault no accesible
    else 🟡 Cuenta ACTIVE + FIRST LOGIN (vault no inicializado)
        B->>C: 200 JWT / sesión

        Note over C: Setup del vault (solo en primer login)
        C->>C: Deriva MASTER_KEY (local)
        C->>C: Genera Vault Key (KEK) + RECOVERY_KEY
        C->>C: KEK_ENC_MASTER + KEK_ENC_RECOVERY
        C->>B: POST /vault/init (KEK_ENC_MASTER, KEK_ENC_RECOVERY, crypto_params)
        B->>B: Persiste material cifrado del vault
        B->>C: 201 Created (vault initialized)

        B->>C: GET /vault/keys -> KEK_ENC_MASTER
        C->>C: Descifra Vault Key (KEK)
        Note over C: ✅ Vault abierto solo en cliente
    else 🟢 Cuenta ACTIVE + vault inicializado
        B->>C: 200 JWT / sesión

        C->>C: Deriva MASTER_KEY (local)
        B->>C: GET /vault/keys -> KEK_ENC_MASTER
        C->>C: Descifra Vault Key (KEK)

        Note over C: ✅ Vault abierto solo en cliente
        Note over B: ❌ Backend no conoce llaves
    end
```

---

## Decisión 2 — Modelo Criptográfico (Zero-Knowledge)

### Responsabilidades

**Cliente**:

- Deriva `MASTER_KEY` desde la passphrase
- Genera llaves criptográficas
- Cifra y descifra todos los secretos
- Nunca envía llaves en claro

**Backend**:

- Autentica y autoriza
- Persiste material cifrado
- Define versiones y parámetros criptográficos
- Nunca puede descifrar el vault

---

## Decisión 3 — Llaves y jerarquía

### Tipos de llaves

- **MASTER\_KEY**

  - Derivada en cliente desde passphrase
  - Nunca persistida ni enviada

- **KEK (Key Encryption Key / Vault Key)**

  - Generada en cliente
  - 1 por cuenta
  - Cifrada con `MASTER_KEY`
  - Backend solo almacena `KEK_ENC_MASTER`

- **DEK (Data Encryption Key)**

  - Generada en cliente
  - 1 por `SecureItem`
  - Cifra el payload
  - Se cifra con la KEK

### Esquema de jerarquía de llaves

```mermaid
flowchart TD
    Passphrase["Passphrase del usuario"]
    MK["MASTER_KEY (KDF local)"]
    KEK["Vault Key (KEK)"]
    DEK["DEK por SecureItem"]
    Payload["Payload cifrado"]

    Passphrase --> MK
    MK -->|Cifra| KEK
    KEK -->|Cifra| DEK
    DEK -->|Cifra| Payload
```

El backend **no genera ni conoce ninguna llave en claro**.

---

## Decisión 4 — Recovery Key (recuperación de cuenta)

Se introduce una **Recovery Key** para permitir recuperación del vault manteniendo Zero-Knowledge.

### En registro

El cliente:

- Genera una `RECOVERY_KEY` (random)
- Cifra la KEK dos veces:
  - `KEK_ENC_MASTER` (con MASTER\_KEY)
  - `KEK_ENC_RECOVERY` (con RECOVERY\_KEY)

El backend persiste ambas versiones cifradas.

La `RECOVERY_KEY`:

- Se muestra **una sola vez** al usuario
- Debe guardarse offline
- Nunca se envía al backend

### En recuperación

Si el usuario pierde la passphrase:

1. Introduce la `RECOVERY_KEY`
2. El cliente descifra la KEK
3. El usuario define nueva passphrase
4. Se deriva nueva `MASTER_KEY`
5. El cliente re-cifra la KEK
6. El backend reemplaza `KEK_ENC_MASTER`

### Esquema de recuperación

```mermaid
sequenceDiagram
    participant U as Usuario
    participant C as Cliente
    participant B as Backend

    U->>C: Introduce RECOVERY_KEY
    C->>C: Descifra KEK con RECOVERY_KEY
    U->>C: Introduce nueva passphrase
    C->>C: Deriva MASTER_KEY_NEW
    C->>C: KEK_ENC_MASTER_NEW
    C->>B: Actualiza KEK_ENC_MASTER
    B->>B: Reemplaza versión cifrada
```

Si el usuario pierde **passphrase y recovery key**, el vault es irrecuperable.

---

## Decisión 5 — Cambio de contraseña

Cambiar la passphrase **no implica** recifrar los secretos.

- Solo se re-cifra la KEK
- Los `SecureItem` permanecen intactos
- Operación rápida y segura

---

## Decisión 6 — Concurrencia

La actualización de `KEK_ENC_MASTER` se controla mediante una revisión entera
server-owned (`masterKeyRevision`) expuesta como un ETag fuerte (`"master-{revision}"`).

- `GET /vault/keys` devuelve el ETag actual y `Cache-Control: no-store`; la revisión no forma parte del JSON.
- `PUT /vault/keys/master` exige exactamente un `If-Match` fuerte generado por ese GET.
- El backend ejecuta un `UPDATE` atómico condicionado por `account_id` y `master_key_revision`; solo se modifican `kek_enc_master`, `master_key_revision` y `updated_at`.
- Una revisión obsoleta devuelve `412 Precondition Failed`; un header ausente devuelve `428 Precondition Required` y un header inválido devuelve `400 Bad Request`.
- El nuevo ETag se construye únicamente después de que la transacción confirme el CAS.
- No se procesan passphrases, claves en claro ni material descifrado.

La concurrencia de `SecureItem` sigue usando su revisión de item y ETags propios.

---

## Decisión 7 — Payload

- El payload es **opaco** para el backend
- El backend no valida contenido
- Límite máximo:
  - **1 MB por SecureItem**

Si se supera el límite:

- Error `InvalidPayload`

Un `SecureItem` representa un **secreto atómico**.

---

## Decisión 8 — Delete de SecureItem

El borrado es **soft delete interno**:

- Se marca `deleted_at`
- No se devuelve en listados
- No se puede actualizar
- No se puede recuperar

Permite:

- Sync consistente
- Propagación de borrados

---

## Decisión 9 — Listado, filtros y orden

`ListSecureItems` soporta:

- `since` (sync incremental)
- `type` (exact match)
- `labels` (exact match)
- `limit`
- `order`

Orden permitido:

- `display_name ASC` (por defecto)
- `updated_at ASC|DESC` (sync)

`display_name` y metadata están en claro.

---

## Consecuencias

- Backend Zero-Knowledge real
- Recuperación posible sin romper seguridad
- UX razonable
- Modelo claro y consistente
- Base sólida para futuras iteraciones

---

## Estado final

Todas las decisiones necesarias para implementar el slice `vault` quedan cerradas. A partir de este ADR se puede:

- Definir esquema SQL
- Implementar use cases
- Implementar crypto en cliente
- Implementar infraestructura sin ambigüedad
