# Vault Use Cases – SafeCube Backend (v1)

> Documento de **diseño de casos de uso** para el slice **vault**.
>
> Este slice gestiona la **persistencia y sincronización de información cifrada** (opaque payloads) enviada por clientes. El backend **no interpreta** el contenido cifrado.

---

## 1. Propósito del Slice `vault`

El slice `vault` es responsable de:

- Persistir, recuperar y listar elementos de información **cifrada en cliente**.
- Asegurar aislamiento por cuenta (`accountId`).
- Exponer operaciones mínimas necesarias para sincronización.

El slice `vault` **no** es responsable de:

- Cifrar o descifrar el contenido.
- Validar el significado del payload.
- Aplicar lógica de negocio sobre secretos.

---

## 2. Modelo Conceptual

### 2.1 SecureItem

Representa un elemento del vault.

Propiedades mínimas:

- `itemId` (Identifier)
- `accountId` (Identifier)
- `itemType` (enum)              // PASSWORD | NOTE | CARD | IDENTITY | GENERIC
- `schemaVersion` (int)          // versión del esquema lógico del payload
- `displayHint` (String, nullable, short) // etiqueta corta no sensible para UX
- `payload` (byte[] | String base64)  // contenido cifrado (opaco)
- `payloadVersion` (long)             // control básico de concurrencia
- `createdAt` (Instant)
- `updatedAt` (Instant)
- `deletedAt` (Instant, nullable)     // baja lógica

Notas:

- `payload` es un blob opaco (E2E). El backend no lo parsea.
- `itemType` es un ENUM cerrado usado solo para renderizado/UX.
- `schemaVersion` permite evolucionar el formato del payload cifrado.
- `displayHint` es opcional, corto y no debe contener datos sensibles.
- `payloadVersion` permite detectar escrituras concurrentes.

---

### 2.2 Metadata y Organización

El backend almacena **metadata funcional mínima y no sensible**, separada estrictamente del payload cifrado.

Decisiones adoptadas:

- ✔ Metadata funcional **en claro**, cuidadosamente restringida.
- ✔ Separación estricta entre **secreto (item)** y **organización (groups)**.
- ❌ El payload cifrado **no contiene información de organización**.

Metadata permitida:

- `itemType` (enum cerrado)
- `schemaVersion` (int)
- `displayHint` (String corto, opcional)

Metadata prohibida:

- Cualquier dato sensible o de alta entropía (usuarios, emails, URLs, notas, etiquetas libres).

Toda semántica rica vive **dentro del payload cifrado**.

---

### 2.3 Groups (organización lógica)

Los grupos permiten organizar visualmente los items sin afectar al cifrado ni a la concurrencia.

Un grupo:

- No es un secreto.
- No contiene payload cifrado.
- No participa en control de versiones del item.

Propiedades conceptuales:

- `groupId` (Identifier)
- `accountId` (Identifier)
- `name` (String)
- `description` (String, nullable)
- `createdAt` (Instant)
- `updatedAt` (Instant)

### 2.4 Relación Item ↔ Group

La relación entre items y grupos es **muchos-a-muchos**.

Características:

- Un item puede pertenecer a múltiples grupos.
- Cambios de grupo **no modifican el payload cifrado**.
- La organización es metadata relacional independiente.

Esta relación se gestiona mediante un modelo relacional dedicado (ej. `group_items`).

La estrategia de metadata queda **definida** y no se considera abierta en esta versión.

Decisión adoptada:

- ✔ Uso de **metadata funcional mínima en claro**, cuidadosamente restringida.
- ✔ Separación estricta entre **secreto (payload cifrado)** y **organización/UX**.

Metadata permitida:

- `itemType` (enum cerrado)
- `schemaVersion` (int)
- `displayHint` (String corto, opcional)

Otras estrategias (ej. tokens de búsqueda determinísticos) se consideran **fuera de alcance** y se evaluarán en versiones futuras si existe una necesidad clara.

---

## 3. Casos de Uso

> Los casos de uso de organización (groups) se definirán en una iteración posterior. En esta versión, el slice `vault` se centra en la gestión de secretos (`SecureItem`).

> Los siguientes casos de uso se consideran el mínimo viable para sincronización.

### 3.1 CreateSecureItemUseCase

#### 3.1.1 Descripción

Crea un nuevo `SecureItem` para una cuenta, almacenando un payload cifrado junto con **metadata funcional mínima** necesaria para UX y organización.

---

#### 3.1.2 Actor

- Usuario autenticado.

---

#### 3.1.3 Input

- `CreateSecureItemCommand`
  - `accountId` (Identifier)
  - `itemType` (enum)
  - `schemaVersion` (int)
  - `displayHint` (String, nullable)
  - `payload` (byte[] | String)

---

#### 3.1.4 Output

- `CreateSecureItemResult`
  - `itemId` (Identifier)
  - `createdAt` (Instant)

---

#### 3.1.2 Actor

- Usuario autenticado.

---

#### 3.1.3 Input

- `CreateSecureItemCommand`
  - `accountId` (Identifier)
  - `payload` (byte[] | String)

---

#### 3.1.4 Output

- `CreateSecureItemResult`
  - `itemId` (Identifier)
  - `createdAt` (Instant)

---

#### 3.1.5 Errores Esperados

- `InvalidPayload`
  - El payload no cumple requisitos mínimos (tamaño, formato).

---

### 3.2 ListSecureItemsUseCase

#### 3.2.1 Descripción

Lista los `SecureItems` de una cuenta para permitir sincronización y navegación eficiente.

---

#### 3.2.2 Input

- `ListSecureItemsQuery`
  - `accountId` (Identifier)
  - `since` (Instant, nullable)
  - `filter` (ListFilter, nullable)

`ListFilter` (objeto **opcional y extensible**, no cerrado):

- `sort`
  - `field` (enum) // ej: UPDATED\_AT | CREATED\_AT
  - `direction` (enum) // ASC | DESC
- `itemType` (enum, nullable) // ejemplo de filtro
- `includeDeleted` (boolean, default = false)

> El objeto `filter` es **orientativo** y puede evolucionar sin romper el contrato del caso de uso.

---

#### 3.2.3 Output

- `ListSecureItemsResult`
  - `items` (list)
    - `itemId` (Identifier)
    - `itemType` (enum)
    - `schemaVersion` (int)
    - `displayHint` (String, nullable)
    - `updatedAt` (Instant)
    - `deletedAt` (Instant, nullable)

---

### 3.3 GetSecureItemUseCase

#### 3.3.1 Descripción

Recupera un `SecureItem` específico (payload cifrado y metadata asociada) para la cuenta.

---

#### 3.3.2 Input

- `GetSecureItemQuery`
  - `accountId` (Identifier)
  - `itemId` (Identifier)

---

#### 3.3.3 Output

- `GetSecureItemResult`
  - `itemId` (Identifier)
  - `itemType` (enum)
  - `schemaVersion` (int)
  - `displayHint` (String, nullable)
  - `payload` (byte[] | String)
  - `payloadVersion` (long)
  - `updatedAt` (Instant)
  - `deletedAt` (Instant, nullable)

---

#### 3.3.4 Errores Esperados

- `SecureItemNotFound`
  - No existe el item para esa cuenta.

---

### 3.4 UpdateSecureItemUseCase

#### 3.4.1 Descripción

Actualiza el contenido de un `SecureItem` existente, permitiendo modificar el payload cifrado y la metadata funcional asociada.

La actualización se rige por una estrategia de **concurrencia optimista basada en timestamp**: el item solo se actualiza si la fecha de modificación proporcionada es posterior a la almacenada en el sistema y si el campo `schemaVersion` también es mayor al almacenado en el sistema.

---

#### 3.4.2 Actor

- Usuario autenticado.

---

#### 3.4.3 Input

- `UpdateSecureItemCommand`
  - `accountId` (Identifier)
  - `itemId` (Identifier)
  - `itemType` (enum)
  - `schemaVersion` (int)
  - `displayHint` (String, nullable)
  - `payload` (byte[] | String)
  - `updatedAt` (Instant)  // timestamp del cliente

---

#### 3.4.4 Output

- `UpdateSecureItemResult`
  - `itemId` (Identifier)
  - `updatedAt` (Instant)
  - `payloadVersion` (long)

---

#### 3.4.5 Errores Esperados

- `SecureItemNotFound`

  - No existe el item para esa cuenta.

- `StaleUpdateRejected`

  - El `updatedAt` proporcionado es anterior o igual al almacenado.

- `InvalidPayload`

  - El payload no cumple requisitos mínimos.

---

### 3.5 DeleteSecureItemUseCase

#### 3.5.1 Descripción

Marca un `SecureItem` como eliminado mediante **baja lógica**, permitiendo que la eliminación sea sincronizada correctamente entre clientes.

---

#### 3.5.2 Actor

- Usuario autenticado.

---

#### 3.5.3 Input

- `DeleteSecureItemCommand`
  - `accountId` (Identifier)
  - `itemId` (Identifier)
  - `updatedAt` (Instant)  // timestamp del cliente

---

#### 3.5.4 Output

- `DeleteSecureItemResult`
  - `itemId` (Identifier)
  - `deletedAt` (Instant)

---

#### 3.5.5 Errores Esperados

- `SecureItemNotFound`

  - No existe el item para esa cuenta.

- `StaleDeleteRejected`

  - El `updatedAt` proporcionado es anterior o igual al almacenado.

---

## 4. Notas de Diseño

- El `accountId` se obtiene del contexto de autenticación (infraestructura).
- La comunicación con otros slices se realiza **exclusivamente mediante identificadores**.
- La eliminación de items sigue un modelo de **baja lógica con borrado físico diferido**.
- La concurrencia se gestiona mediante **timestamps de modificación**, solo se aceptan operaciones más recientes que el estado persistido y el \*\*campo\*\* `schemaVersion` : es mayor al almacenado en el sistema.
- El contenido cifrado, el modelo criptográfico y la gestión de claves se especifican en un documento separado: `Vault Crypto Strategy – v1`.

---

---

## 5. Tests del Slice `vault`

Esta sección define la estrategia de **tests** para el slice `vault`, alineada con la **pirámide del testing**.

Se distinguen dos niveles principales:

- **Application Tests** (tests de casos de uso).
- **Acceptance Tests** (tests E2E vía HTTP con Karate).

Los tests unitarios por clase (Mockito) se asumen como práctica estándar y no se detallan aquí.

---

## 5.1 Application Tests (Casos de Uso)

Estos tests validan el comportamiento del slice sin framework ni infraestructura real.

Características:

- Sin Spring, sin HTTP, sin base de datos real.
- Uso de dobles de test para el port de persistencia (`SecureItemRepository`).
- Validan reglas de negocio, contratos y errores.

### 5.1.1 Tests – CreateSecureItemUseCase

**Caso exitoso**

- Given una cuenta válida.
- And un payload válido.
- When se ejecuta `CreateSecureItemUseCase`.
- Then:
  - Se persiste un nuevo `SecureItem` con `createdAt`.
  - Se devuelve `itemId` y `createdAt`.

**Payload inválido**

- Given un payload vacío o que excede el límite permitido.
- When se ejecuta el caso de uso.
- Then se devuelve `InvalidPayload`.

---

### 5.1.2 Tests – ListSecureItemsUseCase

**Listado exitoso**

- Given una cuenta con items existentes.
- When se ejecuta `ListSecureItemsUseCase`.
- Then:
  - Se devuelven items de esa cuenta.
  - Cada item incluye `itemId`, `updatedAt` y `deletedAt` (si aplica).

\*\*Filtro \*\***`since`**

- Given items con distintos `updatedAt`.
- When se lista con `since`.
- Then solo se devuelven items con `updatedAt` posterior a `since`.

---

### 5.1.3 Tests – GetSecureItemUseCase

**Caso exitoso**

- Given un item existente para la cuenta.
- When se ejecuta `GetSecureItemUseCase`.
- Then se devuelve el payload, `payloadVersion`, `updatedAt` y `deletedAt`.

**Item no encontrado**

- Given un `itemId` inexistente (o de otra cuenta).
- When se ejecuta el caso de uso.
- Then se devuelve `SecureItemNotFound`.

---

### 5.1.4 Tests – UpdateSecureItemUseCase

**Actualización exitosa (timestamp más reciente)**

- Given un item existente con `updatedAt = T1`.
- When se actualiza con `updatedAt = T2` y `T2 > T1`.
- Then:
  - Se persiste el nuevo payload.
  - Se incrementa `payloadVersion`.
  - Se devuelve `updatedAt = T2`.

**Actualización rechazada (stale)**

- Given un item existente con `updatedAt = T1`.
- When se actualiza con `updatedAt = T0` y `T0 <= T1`.
- Then se devuelve `StaleUpdateRejected`.

**Item no encontrado**

- Given un `itemId` inexistente.
- When se ejecuta la actualización.
- Then se devuelve `SecureItemNotFound`.

**Payload inválido**

- Given un payload inválido.
- When se ejecuta la actualización.
- Then se devuelve `InvalidPayload`.

---

### 5.1.5 Tests – DeleteSecureItemUseCase

**Borrado lógico exitoso (timestamp más reciente)**

- Given un item existente con `updatedAt = T1`.
- When se elimina con `updatedAt = T2` y `T2 > T1`.
- Then:
  - Se marca `deletedAt`.
  - El item aparece como eliminado en listados.

**Borrado rechazado (stale)**

- Given un item existente con `updatedAt = T1`.
- When se elimina con `updatedAt = T0` y `T0 <= T1`.
- Then se devuelve `StaleDeleteRejected`.

**Item no encontrado**

- Given un `itemId` inexistente.
- When se ejecuta el borrado.
- Then se devuelve `SecureItemNotFound`.

---

## 5.2 Acceptance Tests (Karate)

Estos tests validan el comportamiento del slice `vault` desde el punto de vista del consumidor de la API.

Características:

- HTTP real.
- Backend en ejecución.
- Sin mocks de negocio.

### 5.2.1 Escenarios – Vault

**Crear item**

- POST `/vault/items`
- Then `201 Created`
- And body contiene `itemId`.

**Listar items**

- GET `/vault/items`
- Then `200 OK`

**Obtener item**

- GET `/vault/items/{itemId}`
- Then `200 OK`
- And body contiene `payload`.

**Actualizar item**

- PUT `/vault/items/{itemId}`
- Then `200 OK`

**Actualizar item (stale)**

- PUT `/vault/items/{itemId}` con `updatedAt` antiguo
- Then `409 Conflict`

**Eliminar item (soft delete)**

- DELETE `/vault/items/{itemId}`
- Then `204 No Content`

**Eliminar item (stale)**

- DELETE `/vault/items/{itemId}` con `updatedAt` antiguo
- Then `409 Conflict`

**Sync por cambios**

- GET `/vault/items?since=<timestamp>`
- Then `200 OK`

---

## 5.3 Principios de Testeo

- Los application tests validan reglas de negocio y concurrencia.
- Los acceptance tests validan contratos HTTP, códigos de estado y payloads.
- Ningún test de aceptación debe depender de estado previo implícito; los datos se preparan explícitamente.
- Si un test falla, existe una ruptura de contrato o de reglas.

---

*Este documento evoluciona junto al código, pero siempre precede a la implementación.*

