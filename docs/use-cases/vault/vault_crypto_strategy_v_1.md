# Vault Crypto Strategy – v1

> Documento de **estrategia criptográfica** del slice **vault**.
>
> Define cómo se cifran, versionan y sincronizan los secretos en SafeCube, garantizando **Zero‑Knowledge**, soporte **multi‑dispositivo** y funcionamiento **offline‑first**.
>
> Este documento **complementa** a `Vault Use Cases – SafeCube Backend (v1)` y no define casos de uso ni endpoints HTTP.

---

## 1. Objetivos de Diseño

La estrategia criptográfica del vault persigue los siguientes objetivos:

- **Zero‑Knowledge real**: el backend nunca puede interpretar secretos.
- **Cifrado extremo a extremo (E2EE)**.
- **Offline‑first**: el cliente puede operar sin red.
- **Multi‑dispositivo**: los secretos creados en un dispositivo pueden leerse en otro.
- **Evolución segura**: permitir rotación de claves y cambios de esquema sin re‑cifrar todo.

---

## 2. Principios Criptográficos

- El backend **almacena blobs opacos**.
- Toda la semántica del secreto vive **dentro del payload cifrado**.
- El cifrado usa **AEAD** (confidencialidad + integridad).
- Las claves siguen una **jerarquía explícita** para desacoplar:
  - credenciales del usuario
  - dispositivos
  - datos

---

## 3. Jerarquía de Claves

### 3.1 Tipos de Clave

- **Master Password**
  - Conocida solo por el usuario.
  - Nunca se usa directamente para cifrar datos.

- **KEK (Key Encryption Key)**
  - Derivada de la Master Password mediante KDF fuerte (ej. Argon2id).
  - Sirve para envolver la Vault Key.

- **VK (Vault Key)**
  - Clave simétrica raíz del vault.
  - Se usa para envolver las claves de los items.

- **DEK (Data Encryption Key)**
  - Clave simétrica **única por item**.
  - Se usa para cifrar el payload del item.

---

### 3.2 Relación entre Claves

```
Master Password
      ↓ KDF
     KEK
      ↓ encrypt
     VK
      ↓ encrypt
     DEK (por item)
      ↓ encrypt
   Payload cifrado
```

Notas:
- Cambiar la Master Password **no** implica re‑cifrar items.
- Cada item es criptográficamente independiente.

---

## 4. Vault Key Material (Backend)

El backend almacena **únicamente** material criptográfico envuelto y parámetros necesarios para reproducir derivaciones de claves.

### 4.1 Esquema de almacenamiento (orientativo · v1)

#### Tabla `vault_key_material`

Una fila por cuenta (`accountId`). Representa el estado **actual** del material criptográfico del vault.

Campos:
- `account_id` (UUID, PK)
- `wrapped_vk` (BLOB / BYTEA, NOT NULL)
- `kdf` (ENUM, NOT NULL) // `ARGON2ID`
- `kdf_salt` (BLOB / BYTEA, NOT NULL)
- `kdf_memory_kib` (INT, NOT NULL)
- `kdf_iterations` (INT, NOT NULL)
- `kdf_parallelism` (INT, NOT NULL)
- `kdf_output_len` (INT, NOT NULL)
- `crypto_version` (VARCHAR, NOT NULL) // ej. `v1`
- `created_at` (TIMESTAMP, NOT NULL)
- `updated_at` (TIMESTAMP, NOT NULL)
- `rotated_at` (TIMESTAMP, NULL) // rotación de VK
- `deleted_at` (TIMESTAMP, NULL)

Restricciones:
- `PRIMARY KEY (account_id)`
- Una cuenta tiene **exactamente un vault activo**.

---

#### Tabla `vault_devices` (opcional pero recomendada)

Inventario de dispositivos asociados a una cuenta.

Campos:
- `device_id` (UUID, PK)
- `account_id` (UUID, INDEX)
- `device_name` (VARCHAR, NULL)
- `created_at` (TIMESTAMP, NOT NULL)
- `last_seen_at` (TIMESTAMP, NULL)
- `revoked_at` (TIMESTAMP, NULL)

Notas:
- No participa en cifrado.
- Permite UX de gestión de dispositivos y futuras políticas de seguridad.

---

#### Tabla `vault_key_history` (opcional · futuro)

Histórico de materiales criptográficos para auditoría o migraciones controladas.

Campos:
- `id` (UUID, PK)
- `account_id` (UUID, INDEX)
- `wrapped_vk` (BLOB / BYTEA)
- mismos campos `kdf_*`
- `crypto_version` (VARCHAR)
- `valid_from` (TIMESTAMP)
- `valid_to` (TIMESTAMP, NULL)
- `reason` (ENUM) // PASSWORD_CHANGE | ROTATION | MIGRATION

---

### 4.2 Parámetros KDF recomendados (baseline v1)

- `kdf = ARGON2ID`
- `saltLen = 16 bytes`
- `outputLen = 32 bytes` (256‑bit)
- `memoryKiB >= 65536` (64 MiB)
- `iterations >= 3`
- `parallelism = min(4, available_cores)`

Notas:
- El backend **nunca** almacena VK ni DEK en claro.
- `wrappedVK` se genera **exclusivamente en cliente** durante el bootstrap del vault.

---

## 5. Bootstrap del Vault

### 5.1 Primer Dispositivo

1. El cliente genera una **VK** aleatoria.
2. El usuario define su **Master Password**.
3. El cliente deriva **KEK** mediante KDF.
4. Se calcula:
   - `wrappedVK = Encrypt(KEK, VK)`
5. El cliente envía al backend:
   - `wrappedVK`
   - `kdfParams`
   - `cryptoVersion`

---

### 5.2 Añadir un Nuevo Dispositivo

1. El dispositivo inicia sesión.
2. Descarga `wrappedVK` y `kdfParams`.
3. El usuario introduce la Master Password.
4. El cliente deriva KEK y obtiene VK.
5. El vault queda desbloqueado localmente.

---

## 6. Creación y Lectura de Items

### 6.1 Crear Item

1. El cliente genera una **DEK** aleatoria.
2. Construye el plaintext lógico (JSON):
   - `type`
   - `schemaVersion`
   - `data`
3. Cifra el plaintext con la DEK (AEAD).
4. Envuelve la DEK con la VK:
   - `wrappedDEK = Encrypt(VK, DEK)`
5. Envía al backend:
   - `ciphertext`
   - `wrappedDEK`
   - `nonce`
   - metadata técnica

---

### 6.2 Leer Item

1. El cliente descarga el payload.
2. Desenvuelve la DEK usando la VK.
3. Descifra el ciphertext.
4. Interpreta el plaintext según `type` y `schemaVersion`.

---

## 7. Envelope Criptográfico del Payload

El payload cifrado se modela como un **sobre versionado** y autocontenible.

### 7.1 Algoritmos y tamaños (v1)

Se adopta un único baseline para maximizar interoperabilidad:
- AEAD: `XCHACHA20_POLY1305` (recomendado) **o** `AES_256_GCM` (alternativa)
- `VK` size: 32 bytes (256-bit)
- `DEK` size: 32 bytes (256-bit)
- `nonce`:
  - XChaCha20-Poly1305: 24 bytes
  - AES-GCM: 12 bytes

Reglas:
- `nonce` **único por (DEK, operación)**.
- RNG criptográfico para VK/DEK/nonce.

---

### 7.2 Estructura del Envelope (v1)

El envelope es un objeto con los siguientes campos mínimos:

- `envelopeVersion` (int) // `1`
- `cipherSuite` (string)  // `XCHACHA20_POLY1305` | `AES_256_GCM`
- `nonce` (bytes)
- `wrappedDEK` (bytes)    // DEK envuelta con VK mediante AEAD
- `ciphertext` (bytes)    // payload cifrado con DEK mediante AEAD

Opcionales (si aplica):
- `aad` (bytes | omitted) // ver 7.3

Notas:
- `authTag` se considera parte de `wrappedDEK`/`ciphertext` según la librería/encoding.

---

### 7.3 AAD (Associated Data) recomendado

Para evitar ataques de "swap" (mover payloads entre items), se recomienda usar AAD en el cifrado del payload con DEK.

AAD (v1) recomendado:
- `accountId`
- `itemId`
- `payloadVersion`

Regla:
- Si se usa AAD, debe ser **idéntico** en cifrado y descifrado.

---

### 7.4 Plaintext lógico (antes de cifrar)

El plaintext es un JSON (o CBOR) con estructura estable:

- `type` (enum) // PASSWORD | NOTE | CARD | IDENTITY | GENERIC
- `schemaVersion` (int)
- `data` (object)

Reglas:
- `type` y `schemaVersion` del plaintext deben ser coherentes con la metadata en claro del item.
- El backend no valida este contenido.

---

### 7.5 Encoding y transporte

Para compatibilidad multi-plataforma:
- El envelope se serializa como **CBOR** (preferido) o JSON.
- En transporte HTTP:
  - si JSON: bytes en Base64URL (sin padding)
  - si binario: `application/cbor`

El backend persiste el envelope como `BLOB`.

---

## 8. Concurrencia y Sync

- El backend no resuelve conflictos criptográficos.
- La concurrencia se gestiona mediante:
  - `updatedAt`
  - `payloadVersion`
- El payload cifrado se trata como **unidad atómica**.

---

## 9. Rotación de Claves

### 9.1 Cambio de Master Password

- Se deriva una nueva KEK.
- Se re‑envuelve la VK.
- **No se tocan los items ni los wrappedDEK**.

---

### 9.2 Rotación de Vault Key (opcional)

- Requiere re‑envolver todos los wrappedDEK.
- No requiere re‑cifrar payloads.
- Operación controlada y poco frecuente.

---

## 10. Glosario Rápido

- **AEAD**: Authenticated Encryption with Associated Data.
- **E2EE**: End‑to‑End Encryption.
- **Envelope Encryption**: patrón de cifrado jerárquico.
- **Zero‑Knowledge**: el servidor no puede acceder al contenido.
- **VK (Vault Key)**: clave simétrica raíz del vault.
- **DEK (Data Encryption Key)**: clave simétrica por item.
- **KEK (Key Encryption Key)**: clave derivada de password para envolver VK.

---

*Este documento define la base criptográfica del vault y evoluciona con extrema cautela.*

