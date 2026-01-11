# Auth Slice – Design & Token Strategy
SafeCube Backend (v1)

> Documento definitivo de **diseño funcional y de sesión** del slice **auth**.
>
> Define:
> - los **casos de uso de autenticación**
> - los **flujos de sesión basados en tokens**
> - los **contratos observables del sistema**
>
> Este documento precede al código y debe mantenerse alineado con él.

---

## 1. Objetivo

Definir de forma clara y coherente cómo el backend de SafeCube:

- registra identidades autenticables,
- autentica credenciales,
- emite y renueva sesiones,
- y permite su revocación,

sin acoplar el dominio a mecanismos concretos de transporte o framework.

---

## 2. Principios de Diseño

1. **Separación Autenticación vs Sesión**  
   Verificar credenciales y gestionar sesiones son responsabilidades distintas.

2. **Stateless First**  
   El backend no mantiene estado de sesión en memoria.

3. **Contratos explícitos**  
   Los casos de uso exponen DTOs de aplicación, no entidades ni DTOs web.

4. **Errores esperados, no excepciones**  
   Los fallos de negocio se modelan explícitamente.

5. **Evolución sin ruptura**  
   El diseño permite evolucionar hacia OAuth/OIDC sin reescribir el dominio.

---

## 3. Casos de Uso de Autenticación

### 3.1 RegisterAccountUseCase

#### Descripción
Registra una nueva cuenta autenticable a partir de credenciales proporcionadas.

#### Input
- `RegisterAccountCommand`
    - `email`
    - `rawPassword`

#### Output
- `RegisterAccountResult`
    - `accountId`
    - `createdAt`

#### Errores Esperados
- `AccountAlreadyExists`
- `InvalidCredentials`

#### Reglas
- Email único.
- Password nunca persistido en claro.
- La cuenta se crea habilitada.

---

### 3.2 AuthenticateAccountUseCase

#### Descripción
Verifica las credenciales de una cuenta existente.

#### Input
- `AuthenticateAccountCommand`
    - `email`
    - `rawPassword`

#### Output (interno)
- `AuthenticateAccountResult`
    - `accountId`
    - `authenticatedAt`

> **Nota importante**  
> Este resultado **no se expone directamente a clientes externos**.
> Se utiliza como paso interno en flujos de autenticación completos.

#### Errores Esperados
- `AccountNotFound`
- `InvalidCredentials`
- `AccountDisabled`

---

## 4. Flujo de Autenticación (Login)

El login expuesto por el backend **no corresponde a un único caso de uso**, sino a un **flujo compuesto**:

1. Autenticación de credenciales  
   → `AuthenticateAccountUseCase`

2. Emisión de sesión  
   → `IssueTokensUseCase`

El cliente **no recibe la identidad**, sino credenciales derivadas (tokens).

---

## 5. Modelo de Tokens (Fase 1)

### 5.1 Access Token

- Tipo: JWT
- Uso: autorización de requests
- Vida corta (10–15 min)
- Transporte:  
  `Authorization: Bearer <token>`

Claims mínimos:
- `sub` → `accountId`
- `iat`
- `exp`

---

### 5.2 Refresh Token

- Tipo: token opaco (UUID)
- Vida larga (días / semanas)
- Persistido en base de datos
- Permite revocación

Asociado a:
- `accountId`
- `issuedAt`
- `expiresAt`
- `revokedAt` (nullable)

---

## 6. Flujos de Sesión

### 6.1 Login
- POST `/auth/login`
- Devuelve:
    - `accessToken`
    - `refreshToken`
    - `issuedAt`

### 6.2 Uso Normal
- El cliente envía el access token en cada request.
- El backend valida firma y expiración.

### 6.3 Refresh
- POST `/auth/refresh`
- Se valida el refresh token.
- Se emite nuevo access token.
- El refresh token se rota.

### 6.4 Logout
- POST `/auth/logout`
- Se revocan los refresh tokens de la cuenta.
- El access token expira de forma natural.

---

## 7. Persistencia de Refresh Tokens

### Reglas
- Un refresh token pertenece a una sola cuenta.
- Puede revocarse explícitamente.
- Un token expirado no es reutilizable.
- En Fase 1 puede limitarse a un token activo por cuenta.

---

## 8. Tests

### 8.1 Application Tests
- Validan casos de uso con dobles de test.
- No usan HTTP ni infraestructura real.

### 8.2 Acceptance Tests
- Ejecutados con Karate.
- Validan flujos completos:
    - register
    - login
    - refresh
    - logout
- Validan contratos HTTP y códigos de estado.

---

## 9. Decisiones Cerradas (Fase 1)

- ✔️ Autenticación propia.
- ✔️ Backend stateless.
- ✔️ JWT solo para access token.
- ✔️ Refresh tokens opacos y persistidos.
- ✔️ Rotación y revocación.
- ❌ OAuth / OIDC en Fase 1.

---

*Este documento define el comportamiento observable y las decisiones clave del slice auth.
Cualquier cambio relevante debe reflejarse aquí antes o junto con el código.*
