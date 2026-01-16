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

Propiedades mínimas (v1):

- `userId` (Identifier)
- `accountId` (Identifier)
- `displayName` (String)
- `createdAt` (Instant)
- `updatedAt` (Instant)

Notas de diseño:

- `userId` es el identificador interno del perfil.
- `accountId` referencia la identidad autenticable creada en el slice `auth`.
- **No existe eliminación lógica ni física en este slice.**
- Si un perfil existe, se considera activo.
- La ausencia de perfil implica `UserProfileNotFound`.

---

## 3. Casos de Uso

### 3.1 CreateUserProfileUseCase

#### 3.1.1 Descripción

Crea un perfil de usuario asociado a una cuenta autenticable existente.

Este caso de uso puede ejecutarse:
- Tras un registro exitoso.
- Bajo demanda, cuando se necesita información de perfil.

---

#### 3.1.2 Actor

- Usuario autenticado.

---

#### 3.1.3 Input

- `CreateUserProfileCommand`
  - `accountId` (Identifier)
  - `displayName` (String)

---

#### 3.1.4 Output

- `UserProfileResponse`
  - `userId`
  - `accountId`
  - `displayName`
  - `createdAt`
  - `updatedAt`

---

#### 3.1.5 Errores Esperados

- `UserProfileAlreadyExists`  
  Ya existe un perfil asociado al `accountId`.

- `AccountNotFound`  
  La cuenta autenticable no existe.

---

#### 3.1.6 Reglas de Negocio

- Un `accountId` solo puede tener un perfil asociado.
- La existencia de la cuenta se valida mediante un **puerto explícito (`AccountExistencePort`)**.
- El `displayName` debe cumplir las reglas del dominio.

---

### 3.2 GetUserProfileUseCase

#### 3.2.1 Descripción

Recupera el perfil de usuario asociado a una cuenta autenticada.

---

#### 3.2.2 Actor

- Usuario autenticado.

---

#### 3.2.3 Input

- `accountId` (Identifier)

---

#### 3.2.4 Output

- `UserProfileResponse`
  - `userId`
  - `accountId`
  - `displayName`
  - `createdAt`
  - `updatedAt`

---

#### 3.2.5 Errores Esperados

- `UserProfileNotFound`  
  No existe perfil para el `accountId`.

---

### 3.3 UpdateUserProfileUseCase

#### 3.3.1 Descripción

Actualiza la información editable del perfil de usuario.

---

#### 3.3.2 Actor

- Usuario autenticado.

---

#### 3.3.3 Input

- `UpdateUserProfileCommand`
  - `accountId` (Identifier)
  - `displayName` (String)

---

#### 3.3.4 Output

- `UserProfileResponse`
  - `userId`
  - `accountId`
  - `displayName`
  - `createdAt`
  - `updatedAt`

---

#### 3.3.5 Errores Esperados

- `UserProfileNotFound`  
  No existe perfil para el `accountId`.

- `InvalidProfileData`  
  El `displayName` no cumple las reglas del dominio.

---

## 4. Reglas de Dominio

### 4.1 Reglas de `displayName`

- No puede ser `null`.
- No puede ser vacío ni contener solo espacios.
- Longitud máxima: **100 caracteres**.
- La validación se realiza en el **dominio**, no en infraestructura.

---

### 4.2 Relación con `auth`

- Puede existir una `AuthAccount` sin `UserProfile`.
- **No puede existir un `UserProfile` sin `AuthAccount`.**
- El slice `user` no gestiona estados de cuenta (enabled/disabled).

La comunicación con `auth` se realiza mediante un **Anti-Corruption Layer**:
- `AccountExistencePort`

---

## 5. Exclusión de Eliminación de Perfil

El slice `user` **no soporta eliminación de perfiles**.

Motivación:

- La eliminación de datos personales es una operación **transversal**, no local.
- El ciclo de vida de la cuenta pertenece al slice `auth`.

Decisiones explícitas:
- No existe `DeleteUserProfileUseCase`.
- No existe `deletedAt`.
- No existe soft-delete en `user`.

La eliminación de datos:
- Se inicia desde `auth`.
- Puede ejecutarse de forma diferida (batch / cleanup).
- Puede afectar a múltiples slices.

---

## 6. Notas de Diseño

- El slice `user` no emite ni consume tokens.
- El `accountId` se obtiene del contexto de autenticación (infraestructura).
- Los slices se comunican **exclusivamente mediante identificadores**.
- La consistencia entre `auth` y `user` se mantiene por diseño, no por acoplamiento.
- Este diseño favorece:
  - Evolución independiente.
  - Cumplimiento de RGPD.
  - Borrado centralizado y auditable.

---

## 7. Tests del Slice `user`

La estrategia de testing incluye:

- **Unit tests**
  - Casos de uso.
  - Dominio.
  - Adaptadores.

- **Integration tests**
  - Persistencia JPA.
  - Integración con `auth` vía ACL.

- **Acceptance tests**
  - Escenarios E2E vía HTTP (Karate).

La cobertura incluye:
- Casos felices.
- Casos de error.
- Reglas de dominio.
- Contratos entre slices.
