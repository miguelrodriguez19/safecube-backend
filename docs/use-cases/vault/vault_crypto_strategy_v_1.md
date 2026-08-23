# Vault Crypto Strategy – v1

> Documento de **estrategia criptográfica** del slice **vault**.
>
> Define cómo se cifran, versionan y sincronizan los secretos en SafeCube, garantizando **Zero-Knowledge**, soporte
> **multi-dispositivo** y funcionamiento **offline-first**.
>
> Este documento **complementa** a `Vault Use Cases – SafeCube Backend (v1)` y no define casos de uso ni endpoints HTTP.

---

## 1. Objetivos de Diseño

La estrategia criptográfica del vault persigue los siguientes objetivos:

* **Zero-Knowledge real**: el backend nunca puede interpretar secretos.
* **Cifrado extremo a extremo (E2EE)**.
* **Offline-first**: el cliente puede operar sin red.
* **Multi-dispositivo**: los secretos creados en un dispositivo pueden leerse en otro.
* **Evolución segura**: permitir rotación de claves y cambios de esquema sin re-cifrar todo.

---

## 2. Principios Criptográficos

* El backend **almacena blobs opacos**.
* Toda la semántica del secreto vive **dentro del payload cifrado**.
* El cifrado usa **AEAD** (confidencialidad + integridad).
* El backend **no deriva claves** ni valida material criptográfico.
* La criptografía es **client-driven**.

---

## 3. Jerarquía de Claves (Conceptual · Cliente)

> ⚠️ Esta sección describe el **modelo conceptual del cliente**.
> El backend **no conoce ni gestiona** estas claves en claro.

### 3.1 Tipos de Clave

* **Master Password**

    * Conocida solo por el usuario.
    * Nunca se usa directamente para cifrar datos.

* **KEK (Key Encryption Key)**

    * Derivada de la Master Password mediante KDF fuerte (ej. Argon2id).
    * Se usa para envolver claves del vault.

* **Vault Key (VK)**

    * Clave simétrica raíz del vault.
    * Se usa para envolver las claves de los items.
    * **Nunca se persiste en claro**.

* **DEK (Data Encryption Key)**

    * Clave simétrica **única por item**.
    * Se usa para cifrar el payload del item.

---

### 3.2 Relación entre Claves

```
Master Password
      ↓ KDF
     KEK
      ↓ encrypt
   Vault Key (VK)
      ↓ encrypt
   DEK (por item)
      ↓ encrypt
   Payload cifrado
```

Notas:

* Cambiar la Master Password **no implica re-cifrar items**.
* Cada item es criptográficamente independiente.
* El backend **no participa** en esta jerarquía.

---

## 4. VaultKeyMaterial (Backend)

El backend almacena **únicamente material criptográfico envuelto** y parámetros necesarios para que el cliente pueda
reconstruir la jerarquía de claves localmente.

### 4.1 Modelo Persistido (v1 · efectivo)

El modelo persistido refleja **exactamente** lo que existe en producción en v1.

Una fila por cuenta (`accountId`).

Propiedades:

* `accountId` (UUID, PK)
* `kekEncMaster` (byte[])

    * KEK envuelta con la master key del usuario.
* `kekEncRecovery` (byte[])

    * KEK envuelta con la recovery key.
* `kdfAlgorithm` (String)        // ej. `ARGON2ID`
* `kdfSalt` (byte[])
* `kdfMemoryKib` (int)
* `kdfIterations` (int)
* `kdfParallelism` (int)
* `kdfOutputLen` (int)
* `cryptoVersion` (String)       // ej. `v1`
* `createdAt` (Instant)
* `updatedAt` (Instant)
* `masterKeyRevision` (long)    // revisión server-owned, no se expone en JSON

Restricciones:

* `PRIMARY KEY (accountId)`
* Una cuenta tiene **como máximo un vault inicializado**.
* El material se considera **estado actual**, no histórico.

---

### 4.2 Consideraciones Importantes

* El backend **no conoce** la Vault Key (VK).
* El backend **no valida** la coherencia criptográfica del material.
* El backend **no necesita distinguir** entre cambio de password o recovery.
* `cryptoVersion` permite evolucionar el esquema sin romper clientes antiguos.

---

## 5. Bootstrap del Vault (Cliente)

### 5.1 Primer Dispositivo

1. El cliente genera una **Vault Key (VK)** aleatoria.
2. El usuario define su **Master Password**.
3. El cliente deriva una **KEK** mediante KDF.
4. Se calcula:

    * `kekEncMaster = Encrypt(MasterKey, KEK)`
    * `kekEncRecovery = Encrypt(RecoveryKey, KEK)`
5. El cliente envía al backend:

    * `kekEncMaster`
    * `kekEncRecovery`
    * `kdfParams`
    * `cryptoVersion`

---

### 5.2 Añadir un Nuevo Dispositivo

1. El dispositivo inicia sesión.
2. Descarga `VaultKeyMaterial`.
3. El usuario introduce la Master Password **o** Recovery Key.
4. El cliente obtiene la KEK.
5. El vault queda desbloqueado localmente.

---

## 6. Creación y Lectura de Items (Cliente)

### 6.1 Crear Item

1. El cliente genera una **DEK** aleatoria.
2. Construye el plaintext lógico:

    * `type`
    * `schemaVersion`
    * `data`
3. Cifra el plaintext con la DEK (AEAD).
4. Envuelve la DEK con la Vault Key:

    * `wrappedDEK = Encrypt(VK, DEK)`
5. Envía al backend:

    * payload cifrado
    * metadata mínima

---

### 6.2 Leer Item

1. El cliente descarga el payload cifrado.
2. Desenvuelve la DEK usando la VK.
3. Descifra el payload.
4. Interpreta el contenido localmente.

---

## 7. Envelope Criptográfico del Payload

El payload cifrado se trata como un **blob opaco versionado**.

### 7.1 Algoritmos recomendados (v1)

* AEAD:

    * `XCHACHA20_POLY1305` (preferido)
    * `AES_256_GCM` (alternativa)
* Tamaños:

    * KEK / VK / DEK: 32 bytes (256-bit)

Reglas:

* `nonce` único por operación.
* RNG criptográfico obligatorio.

---

### 7.2 AAD (Associated Data)

Recomendado para prevenir ataques de “swap”.

AAD sugerido:

* `accountId`
* `itemId`
* `payloadVersion`

El backend **no valida** ni interpreta el AAD.

---

## 8. Concurrencia y Sync

* El backend no resuelve conflictos criptográficos.
* El payload cifrado se trata como **unidad atómica**.
* La concurrencia de SecureItems se gestiona mediante la revisión del item y su ETag.
* La rotación de `kekEncMaster` se gestiona mediante `masterKeyRevision` y un
  `If-Match` fuerte (`"master-{revision}"`). El CAS solo sustituye el blob
  `kekEncMaster`, incrementa la revisión y actualiza `updatedAt`; el backend no
  descifra ni interpreta ningún material.

---

## 9. Rotación de Claves

### 9.1 Cambio de Master Password

* Se deriva una nueva KEK.
* Se re-envuelve la KEK.
* **No se tocan los items**.

---

### 9.2 Rotación de Vault Key (fuera de alcance v1)

* Requiere re-envolver todas las DEK.
* No requiere re-cifrar payloads.
* Se considera una operación futura y controlada.

---

## 10. Principios de Compatibilidad

* `VaultKeyMaterial` es **opcional en v1**.
* `SecureItem` funciona con o sin vault inicializado.
* La rotación de KEK **no afecta** a items existentes.
* No existen dependencias cruzadas entre ambos modelos.

---

## 11. Glosario Rápido

* **AEAD**: Authenticated Encryption with Associated Data.
* **E2EE**: End-to-End Encryption.
* **Zero-Knowledge**: el servidor no puede acceder al contenido.
* **KEK**: Key Encryption Key.
* **VK**: Vault Key (cliente).
* **DEK**: Data Encryption Key (por item).

---

*Este documento define la base criptográfica del vault y evoluciona con extrema cautela.*
