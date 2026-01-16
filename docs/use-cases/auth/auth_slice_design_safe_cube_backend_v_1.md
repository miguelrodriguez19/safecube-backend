# Auth Slice – Design & Token Strategy

**SafeCube Backend (v1 · Definitivo)**

> Documento **canónico** de diseño del slice **`auth`**.
>
> Define:
>
> * Casos de uso de autenticación
> * Modelo de sesión basado en tokens
> * Flujos soportados
> * Decisiones cerradas de seguridad
>
> Este documento **describe el comportamiento observable del sistema**
> y debe mantenerse sincronizado con el código.

---

## 1. Propósito del Slice `auth`

El slice `auth` es responsable de **identidad autenticable y gestión de sesiones**.

Gestiona:

* Registro de cuentas autenticables
* Verificación de credenciales
* Emisión y rotación de tokens
* Revocación de sesiones

No gestiona:

* Perfil de usuario (slice `user`)
* Datos cifrados (slice `vault`)
* Datos de experiencia de usuario
* Autorización de negocio

---

## 2. Principios de Diseño

1. **Autenticación ≠ Sesión**
   Verificar credenciales y gestionar sesiones son responsabilidades separadas.

2. **Stateless first**
   El backend no mantiene sesiones en memoria.

3. **Tokens como contratos**
   El cliente nunca recibe identidad ni entidades, solo tokens.

4. **Errores explícitos**
   Los errores de negocio se modelan como resultados, no excepciones.

5. **Infraestructura aislada**
   Criptografía, JWT y persistencia viven fuera del dominio.

6. **Evolución controlada**
   El diseño permite migrar a OAuth / OIDC sin romper el dominio.

---

## 3. Modelo de Dominio

### 3.1 AuthAccount

Representa una **identidad autenticable**.

Propiedades relevantes:

* `accountId`
* `email`
* `passwordHash`
* `enabled`
* `createdAt`
* `disabledAt` (nullable)

Notas:

* El dominio **no conoce tokens**
* El estado `enabled` controla la capacidad de autenticarse
* La desactivación no elimina datos inmediatamente

---

## 4. Casos de Uso

### 4.1 RegisterAccountUseCase

#### Descripción

Registra una nueva cuenta autenticable.

#### Input

* `RegisterAccountCommand`

    * `email`
    * `rawPassword`

#### Output

* `RegisterAccountResult`

    * `accountId`
    * `createdAt`

#### Errores Esperados

* `AccountAlreadyExists`
* `InvalidCredentials`

#### Reglas

* Email único
* Password nunca persistido en claro
* La cuenta se crea habilitada

---

### 4.2 AuthenticateAccountUseCase

#### Descripción

Verifica credenciales de una cuenta existente.

#### Input

* `AuthenticateAccountCommand`

    * `email`
    * `rawPassword`

#### Output (interno)

* `AuthenticateAccountResult`

    * `accountId`
    * `authenticatedAt`

> ⚠️ **Este resultado no se expone directamente al cliente.**
> Se utiliza como paso interno del flujo de login.

#### Errores Esperados

* `AccountNotFound`
* `InvalidCredentials`
* `AccountDisabled`

---

### 4.3 IssueTokensUseCase

#### Descripción

Emite una nueva sesión autenticada (access + refresh token).

#### Input

* `IssueTokensCommand`

    * `accountId`
    * `issuedAt`

#### Output

* `IssuedTokensResult`

    * `accessToken`
    * `refreshToken`
    * `issuedAt`

---

### 4.4 RefreshTokensUseCase

#### Descripción

Rota un refresh token válido y emite una nueva sesión.

#### Input

* `refreshTokenHash`
* `newRawRefreshToken`
* `newRefreshTokenHash`
* `issuedAt`
* `newRefreshTokenExpiresAt`

#### Output

* `IssuedTokensResult`

#### Reglas

* El refresh token debe existir
* No debe estar revocado
* No debe estar expirado
* El token anterior se revoca
* El refresh token **siempre rota**

#### Errores Esperados

* `InvalidCredentials`

---

### 4.5 LogoutUseCase

#### Descripción

Invalida todas las sesiones activas de una cuenta.

#### Input

* `accountId`
* `revokedAt`

#### Output

* `void`

#### Reglas

* Se revocan todos los refresh tokens
* Los access tokens expiran de forma natural

---

## 5. Modelo de Tokens

### 5.1 Access Token

* Tipo: JWT
* Vida corta (~15 min)
* Uso: autorización
* Transporte:

  ```
  Authorization: Bearer <token>
  ```

Claims mínimos:

* `sub` → `accountId`
* `iat`
* `exp`

Notas:

* No se persiste
* No es revocable individualmente

---

### 5.2 Refresh Token

* Tipo: token opaco (UUID)
* Persistido en base de datos
* Hashado con HMAC-SHA256
* Vida larga

Propiedades:

* `tokenId`
* `accountId`
* `tokenHash`
* `expiresAt`
* `createdAt`
* `revokedAt` (nullable)

---

## 6. Flujos HTTP

### 6.1 Register

`POST /auth/register`

---

### 6.2 Login

`POST /auth/login`

Flujo:

1. AuthenticateAccountUseCase
2. IssueTokensUseCase

---

### 6.3 Refresh

`POST /auth/refresh`

* Valida refresh token
* Rota refresh token
* Emite nueva sesión

---

### 6.4 Logout

`POST /auth/logout`

* Revoca todos los refresh tokens de la cuenta

---

## 7. Seguridad

* Passwords: BCrypt
* Refresh tokens: HMAC-SHA256
* Secrets inyectados por configuración
* Fallos criptográficos → `InfrastructureException`
* Sin Open Session In View

---

## 8. Tests

### 8.1 Unit Tests

* Casos de uso con mocks
* Cobertura de mutación (PiTest)

### 8.2 Integration Tests

* Repositorios JPA
* Testcontainers + PostgreSQL
* Esquema único compartido

### 8.3 Acceptance Tests

* Karate
* Contratos HTTP
* Flujos reales completos

---

## 9. Decisiones Cerradas (v1)

✔ Autenticación propia
✔ Backend stateless
✔ JWT solo para access token
✔ Refresh tokens opacos y persistidos
✔ Rotación obligatoria
✔ Revocación explícita
✔ Hashing HMAC con secret server-side

❌ OAuth / OIDC (fase futura)
❌ Soft-delete de auth accounts inmediato

---

**Este documento define el contrato del slice `auth`.
Cualquier cambio funcional debe reflejarse aquí.**