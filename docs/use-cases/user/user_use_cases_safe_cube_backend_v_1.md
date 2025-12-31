# User Use Cases – SafeCube Backend (v1)

> Documento de **diseño de casos de uso** para el slice **user**.
>
> Este documento define el comportamiento del sistema respecto a la **gestión del perfil de usuario**, separado explícitamente de autenticación, credenciales y sesiones.

---

## 1. Propósito del Slice `user`

El slice `user` es responsable de representar y gestionar el **perfil de usuario** dentro de SafeCube.

Un perfil de usuario:
- Está asociado a una identidad autenticable (`accountId`).
- Contiene información **no sensible** y orientada a experiencia de usuario.
- Puede evolucionar independientemente del mecanismo de autenticación.

Este slice **no gestiona**:
- Credenciales.
- Autenticación.
- Tokens o sesiones.
- Información cifrada (vault).

---

## 2. Modelo Conceptual

### 2.1 UserProfile

Representa el perfil de un usuario dentro del sistema.

Propiedades mínimas (Fase 1):
- `userId` (Identifier)
- `accountId` (Identifier)
- `displayName` (String)
- `createdAt` (Instant)
- `updatedAt` (Instant)
- `deletedAt` (Instant, nullable)

Notas:
- `userId` es el identificador interno del perfil.
- `accountId` referencia la identidad autenticable creada en el slice `auth`.
- No se almacenan datos sensibles en este modelo.

---

## 3. Casos de Uso

### 3.1 CreateUserProfileUseCase

#### 3.1.1 Descripción

Crea un perfil de usuario asociado a una cuenta autenticable existente.

Este caso de uso se ejecuta normalmente tras un registro exitoso o bajo demanda cuando se requiere información de perfil.

---

#### 3.1.2 Actor

- Usuario autenticado.

---

#### 3.1.3 Input

DTO de entrada del caso de uso:

- `CreateUserProfileCommand`
  - `accountId` (Identifier)
  - `displayName` (String)

---

#### 3.1.4 Output

DTO de salida del caso de uso:

- `CreateUserProfileResult`
  - `userId` (Identifier)
  - `createdAt` (Instant)

---

#### 3.1.5 Errores Esperados

- `UserProfileAlreadyExists`
  - Ya existe un perfil asociado al `accountId`.

- `AccountNotFound`
  - La cuenta autenticable no existe.

---

#### 3.1.6 Reglas de Negocio

- Un `accountId` solo puede tener un perfil asociado.
- El perfil se crea en estado consistente.
- El `displayName` debe cumplir la política mínima definida.

---

### 3.2 GetUserProfileUseCase

#### 3.2.1 Descripción

Recupera el perfil de usuario asociado a una cuenta autenticada.

---

#### 3.2.2 Actor

- Usuario autenticado.

---

#### 3.2.3 Input

DTO de entrada del caso de uso:

- `GetUserProfileQuery`
  - `accountId` (Identifier)

---

#### 3.2.4 Output

DTO de salida del caso de uso:

- `GetUserProfileResult`
  - `userId` (Identifier)
  - `displayName` (String)
  - `createdAt` (Instant)
  - `updatedAt` (Instant)

---

#### 3.2.5 Errores Esperados

- `UserProfileNotFound`
  - No existe perfil para el `accountId`.

---

### 3.3 UpdateUserProfileUseCase

#### 3.3.1 Descripción

Actualiza la información editable del perfil de usuario.

---

#### 3.3.2 Actor

- Usuario autenticado.

---

#### 3.3.3 Input

DTO de entrada del caso de uso:

- `UpdateUserProfileCommand`
  - `accountId` (Identifier)
  - `displayName` (String)

---

#### 3.3.4 Output

DTO de salida del caso de uso:

- `UpdateUserProfileResult`
  - `updatedAt` (Instant)

---

#### 3.3.5 Errores Esperados

- `UserProfileNotFound`
  - No existe perfil para el `accountId`.

- `InvalidProfileData`
  - Los datos proporcionados no cumplen las reglas del dominio.

---

### 3.4 DeleteUserProfileUseCase

#### 3.4.1 Descripción

Solicita la **eliminación lógica** del perfil de usuario, marcándolo como eliminado y desactivando su uso en el sistema.

Este caso de uso representa una **solicitud de baja de cuenta** desde el punto de vista del usuario.

---

#### 3.4.2 Actor

- Usuario autenticado.

---

#### 3.4.3 Input

DTO de entrada del caso de uso:

- `DeleteUserProfileCommand`
  - `accountId` (Identifier)

---

#### 3.4.4 Output

DTO de salida del caso de uso:

- `DeleteUserProfileResult`
  - `deletedAt` (Instant)

---

#### 3.4.5 Errores Esperados

- `UserProfileNotFound`
  - No existe perfil para el `accountId`.

---

#### 3.4.6 Reglas de Negocio

- La eliminación es **lógica**, no física.
- El perfil se marca con `deletedAt`.
- Un perfil eliminado no puede ser recuperado ni modificado.
- El sistema deja de tratar el perfil como activo de forma inmediata.

---

---

## 4. Notas de Diseño

- Los casos de uso del slice `user` **no emiten ni consumen tokens**.
- El `accountId` se obtiene del contexto de autenticación (infraestructura).
- La comunicación con otros slices se realiza **exclusivamente mediante identificadores**.
- La eliminación de perfiles sigue un modelo de **baja lógica con borrado físico diferido**, alineado con RGPD.
- La desactivación de credenciales y sesiones asociadas se coordina con el slice `auth` a nivel de infraestructura.

---

---

## 5. Tests del Slice `user`

Esta sección define la estrategia de **tests** para el slice `user`, alineada con la **pirámide del testing** y coherente con los casos de uso definidos.

Se distinguen dos niveles principales:
- **Application Tests** (tests de casos de uso).
- **Acceptance Tests** (tests E2E vía HTTP con Karate).

Los tests unitarios por clase (Mockito) se asumen como práctica estándar y no se detallan aquí.

---

## 5.1 Application Tests (Casos de Uso)

Estos tests validan el **comportamiento del sistema** sin framework ni infraestructura real.

Características:
- Sin Spring, sin HTTP, sin base de datos real.
- Uso de dobles de test para `UserProfileRepository`.
- Validan reglas de negocio, contratos y errores.

### 5.1.1 Tests – CreateUserProfileUseCase

**Caso exitoso**
- Given una cuenta existente sin perfil.
- When se ejecuta `CreateUserProfileUseCase`.
- Then:
  - Se persiste un nuevo `UserProfile`.
  - Se devuelve `userId` y `createdAt`.

**Perfil ya existente**
- Given un perfil ya asociado al `accountId`.
- When se ejecuta el caso de uso.
- Then se devuelve `UserProfileAlreadyExists`.

**Datos inválidos**
- Given un `displayName` inválido.
- When se ejecuta el caso de uso.
- Then se devuelve `InvalidProfileData`.

---

### 5.1.2 Tests – GetUserProfileUseCase

**Caso exitoso**
- Given un perfil existente y no eliminado.
- When se ejecuta `GetUserProfileUseCase`.
- Then se devuelve el perfil con todos los campos esperados.

**Perfil no encontrado**
- Given un `accountId` sin perfil asociado.
- When se ejecuta el caso de uso.
- Then se devuelve `UserProfileNotFound`.

---

### 5.1.3 Tests – UpdateUserProfileUseCase

**Actualización exitosa**
- Given un perfil existente.
- When se actualiza el `displayName`.
- Then se actualiza `updatedAt` y se persiste el cambio.

**Perfil no encontrado**
- Given un `accountId` inexistente.
- When se ejecuta el caso de uso.
- Then se devuelve `UserProfileNotFound`.

**Perfil eliminado**
- Given un perfil con `deletedAt` no nulo.
- When se intenta actualizar.
- Then se devuelve `UserProfileNotFound`.

---

### 5.1.4 Tests – DeleteUserProfileUseCase

**Borrado lógico exitoso**
- Given un perfil existente y activo.
- When se ejecuta `DeleteUserProfileUseCase`.
- Then:
  - Se marca `deletedAt`.
  - El perfil deja de ser tratable como activo.

**Perfil no encontrado**
- Given un `accountId` sin perfil.
- When se ejecuta el caso de uso.
- Then se devuelve `UserProfileNotFound`.

---

## 5.2 Acceptance Tests (Karate)

Estos tests validan el comportamiento del slice `user` desde el punto de vista del **consumidor de la API**.

Características:
- HTTP real.
- Backend en ejecución.
- Sin mocks de negocio.

### 5.2.1 Escenarios – Perfil de Usuario

**Crear perfil**
- POST `/user/profile`
- Then `201 Created`

**Obtener perfil**
- GET `/user/profile`
- Then `200 OK`

**Actualizar perfil**
- PUT `/user/profile`
- Then `200 OK`

**Eliminar perfil (baja lógica)**
- DELETE `/user/profile`
- Then `204 No Content`

**Acceso tras eliminación**
- GET `/user/profile`
- Then `404 Not Found`

---

## 5.3 Principios de Testeo

- Los application tests validan reglas de negocio.
- Los acceptance tests validan contratos HTTP.
- Ningún test de aceptación debe depender de estado previo implícito.
- Si un test falla, existe una ruptura de contrato o de reglas del dominio.

---

*Este documento evoluciona junto al código, pero siempre precede a la implementación.*

