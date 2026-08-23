# Vault Use Cases – SafeCube Backend (v1)

> Documento de **diseño de casos de uso** para el slice **vault**.
>
> Este slice gestiona la **persistencia y sincronización de información cifrada** (opaque payloads) enviada por
> clientes. El backend **no interpreta** el contenido cifrado.

---

## 1. Propósito del Slice `vault`

El slice `vault` es responsable de:

* Persistir, recuperar y listar elementos de información **cifrada en cliente**.
* Persistir material criptográfico **opaco** necesario para desbloquear el vault en cliente.
* Asegurar aislamiento estricto por cuenta (`accountId`).
* Exponer operaciones mínimas necesarias para sincronización.

El slice `vault` **no** es responsable de:

* Cifrar o descifrar el contenido.
* Derivar claves, KEKs o DEKs.
* Validar el significado del payload.
* Aplicar lógica de negocio sobre secretos.

---

## 2. Modelo Conceptual

### 2.1 SecureItem

*(sin cambios — sección existente)*

---

### 2.2 Metadata y Organización

*(sin cambios — sección existente)*

---

### 2.3 Groups (organización lógica)

*(sin cambios — sección existente)*

---

### 2.4 Relación Item ↔ Group

*(sin cambios — sección existente)*

---

### 2.5 VaultKeyMaterial (material criptográfico)

`VaultKeyMaterial` representa el **material criptográfico opaco** necesario para que un cliente pueda
desbloquear su vault local.

Características clave:

* El backend **no interpreta** ni deriva claves.
* Todo el material se almacena como **blobs opacos**.
* El modelo criptográfico es **client-driven**.

Propiedades mínimas:

* `accountId` (Identifier)
* `kekEncMaster` (byte[])        // KEK cifrada con la master key del usuario
* `kekEncRecovery` (byte[])      // KEK cifrada con recovery key
* `kdfAlgorithm` (String)
* `kdfSalt` (byte[])
* `kdfMemoryKib` (int)
* `kdfIterations` (int)
* `kdfParallelism` (int)
* `kdfOutputLen` (int)
* `cryptoVersion` (String)
* `createdAt` (Instant)
* `updatedAt` (Instant)
* `masterKeyRevision` (long, server-owned; solo se expone como ETag)

Notas importantes:

* El backend **no conoce** la master key, recovery key ni la KEK en claro.
* `cryptoVersion` permite evolucionar el esquema criptográfico sin romper compatibilidad.
* La semántica de las claves está definida exclusivamente en el documento
  **Vault Crypto Strategy – v1**.

---

## 3. Casos de Uso – SecureItem

*(Sección 3.1 → 3.5 sin cambios)*

---

## 4. Casos de Uso – VaultKeyMaterial

> ⚠️ Importante
> La gestión de `VaultKeyMaterial` es **opcional en v1** y **no es un prerequisito**
> para el uso de `SecureItem`.

### 4.1 InitVaultKeyMaterialUseCase

#### 4.1.1 Descripción

Inicializa el material criptográfico del vault para una cuenta.

Este caso de uso se ejecuta normalmente:

* Durante el onboarding del usuario.
* Tras una rotación de credenciales.
* Tras una recuperación del vault.

---

#### 4.1.2 Actor

* Usuario autenticado.

---

#### 4.1.3 Input

* `InitVaultKeyMaterialCommand`

    * `accountId` (Identifier)
    * `kekEncMaster` (byte[])
    * `kekEncRecovery` (byte[])
    * `kdfAlgorithm` (String)
    * `kdfSalt` (byte[])
    * `kdfMemoryKib` (int)
    * `kdfIterations` (int)
    * `kdfParallelism` (int)
    * `kdfOutputLen` (int)
    * `cryptoVersion` (String)
    * `createdAt` (Instant)

---

#### 4.1.4 Output

* Sin payload (HTTP `201 Created`).

---

#### 4.1.5 Errores Esperados

* `VaultAlreadyInitialized`

    * El vault ya ha sido inicializado previamente para la cuenta.

---

### 4.2 GetVaultKeyMaterialUseCase

#### 4.2.1 Descripción

Recupera el material criptográfico del vault para la cuenta autenticada.

---

#### 4.2.2 Input

* `GetVaultKeyMaterialQuery`

    * `accountId` (Identifier)

---

#### 4.2.3 Output

* `GetVaultKeyMaterialResult`

    * Todas las propiedades de `VaultKeyMaterial`.

---

#### 4.2.4 Errores Esperados

* `VaultNotInitialized`

    * El vault no ha sido inicializado para la cuenta.

---

### 4.3 UpdateMasterWrappedKekUseCase

#### 4.3.1 Descripción

Actualiza la KEK cifrada con la master key del usuario.

Este caso de uso se ejecuta típicamente cuando:

* El usuario cambia su passphrase.
* Se rota la master key local.

---

#### 4.3.2 Input

* `UpdateMasterWrappedKekCommand`

    * `accountId` (Identifier)
    * `newKekEncMaster` (byte[])
    * `expectedMasterKeyRevision` (long, obtenido del ETag `If-Match`)
    * `updatedAt` (Instant)

---

#### 4.3.3 Output

* Sin payload (HTTP `200 OK`), con el nuevo ETag `"master-{revision}"`.

---

#### 4.3.4 Errores Esperados

* `VaultNotInitialized`

* `StaleMasterWrappedKekUpdate`

    * El ETag es válido, pero la revisión ya no es la actual (`412 Precondition Failed`).

---

## 5. Compatibilidad con SecureItems Existentes

### 5.1 Principio clave

La gestión de `VaultKeyMaterial` **no afecta** al funcionamiento de `SecureItem`.

Garantías explícitas en v1:

* ✔ Es posible crear, listar, obtener y eliminar `SecureItem` **sin inicializar el vault**.
* ✔ Ningún caso de uso de `SecureItem` depende de `VaultKeyMaterial`.
* ✔ La rotación de claves **no invalida** items existentes.
* ✔ No existen joins, dependencias ni validaciones cruzadas entre ambos modelos.

Esta decisión garantiza:

* Compatibilidad con usuarios legacy.
* Onboarding progresivo.
* Evolución segura del modelo criptográfico.

---

## 6. Tests del Slice `vault`

*(Sección existente, con una pequeña ampliación)*

---

### 6.1 Application Tests

Además de los casos de uso de `SecureItem`, se incluyen:

#### VaultKeyMaterial

* InitVaultKeyMaterialUseCase

    * Inicialización exitosa.
    * Inicialización duplicada.
* GetVaultKeyMaterialUseCase

    * Vault existente.
    * Vault no inicializado.
* UpdateMasterWrappedKekUseCase

    * Rotación exitosa.
    * Vault no inicializado.

---

### 6.2 Acceptance Tests (Karate)

Se incluyen tests E2E para:

#### SecureItem

*(sin cambios)*

#### VaultKeyMaterial

* POST `/vault/keys`

    * `201 Created`
    * `409 Conflict` si ya existe
* GET `/vault/keys`

    * `200 OK`
    * Devuelve el ETag fuerte `"master-{masterKeyRevision}"` y `Cache-Control: no-store`.
    * `masterKeyRevision` no se incluye en el JSON.
    * `404 Not Found`
* PUT `/vault/keys/master`

    * `200 OK`
    * Requiere exactamente un `If-Match` fuerte.
    * Devuelve el nuevo ETag y `Cache-Control: no-store`.
    * `404 Not Found`
    * `400 Bad Request` para un `If-Match` inválido.
    * `412 Precondition Failed` para un ETag obsoleto.
    * `428 Precondition Required` si falta `If-Match`.

#### Compatibilidad

* SecureItem funciona correctamente:

    * con vault inicializado
    * sin vault inicializado
    * tras rotación de KEK

---

## 7. Principios de Diseño

* El backend permanece **zero-knowledge**.
* Todo el material criptográfico es **opaco**.
* `VaultKeyMaterial` es **opt-in** en v1.
* SecureItems y VaultKeyMaterial evolucionan de forma **independiente**.
* El modelo criptográfico se documenta y versiona explícitamente.

---

*Este documento evoluciona junto al código, pero siempre precede a la implementación.*
