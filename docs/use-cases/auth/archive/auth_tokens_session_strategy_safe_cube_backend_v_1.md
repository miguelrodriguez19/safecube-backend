# Auth Tokens & Session Strategy – SafeCube Backend (v1)

> Documento de **diseño de infraestructura de autenticación** para el slice **auth**.
>
> Este documento **complementa** a `Auth Use Cases – SafeCube Backend (v1)` y define **cómo** se mantiene una sesión autenticada una vez un usuario ha sido correctamente autenticado.
>
> Aquí **sí se toman decisiones técnicas** (tokens, expiración, transporte), pero siempre alineadas con los casos de uso ya definidos.

---

## 1. Objetivo del Documento

Definir una estrategia clara, segura y evolutiva para:
- Representar una sesión autenticada.
- Identificar de forma fiable a una cuenta (`AuthAccount`).
- Permitir renovación de sesión sin reautenticación constante.
- Mantener compatibilidad con despliegues cloud y self-hosted.

Este documento **no redefine casos de uso**, solo establece el mecanismo de sesión.

---

## 2. Alcance (Fase 1)

Incluido:
- Autenticación basada en tokens.
- Identificación de cuenta en cada request.
- Renovación de sesión.
- Revocación básica.

Explícitamente fuera:
- OAuth 2.x / OpenID Connect.
- Login social (Google, Apple, etc.).
- Single Sign-On (SSO).
- Autorización avanzada por scopes.

---

## 3. Principios de Diseño

1. **Separación Auth vs Session**  
   Autenticar credenciales y gestionar sesiones son responsabilidades distintas.

2. **Stateless First**  
   El backend no mantiene estado de sesión en memoria.

3. **Rotación y Expiración**  
   Toda sesión debe tener una vida limitada y renovable.

4. **Simplicidad y Auditabilidad**  
   El sistema debe ser fácil de razonar y auditar.

---

## 4. Modelo de Tokens (Fase 1)

Se adopta un modelo de **Access Token + Refresh Token**.

### 4.1 Access Token

- Tipo: **JWT (JSON Web Token)**.
- Uso: Autorización de requests.
- Vida corta (ej. 10–15 minutos).
- Enviado en cada request autenticada.

Contenido mínimo (claims):
- `sub` → `accountId`
- `iat` → issued at
- `exp` → expiration

Notas:
- No contiene información sensible.
- No se usa para refresh.

---

### 4.2 Refresh Token

- Tipo: **Token opaco** (UUID o similar).
- Uso: Renovar access tokens.
- Vida larga (ej. días o semanas).
- Persistido en base de datos.

Asociado a:
- `accountId`
- fecha de creación
- fecha de expiración
- estado (activo / revocado)

Notas:
- Permite revocación selectiva.
- No es un JWT.

---

## 5. Flujo de Autenticación

### 5.1 Login

1. Cliente llama a `POST /auth/login`.
2. Se ejecuta `AuthenticateAccountUseCase`.
3. Si es exitoso:
   - Se emite un **access token**.
   - Se emite un **refresh token**.
4. Tokens devueltos al cliente.

---

### 5.2 Uso Normal

- El cliente envía el access token en cada request autenticada.
- El backend valida firma y expiración.

---

### 5.3 Refresh

1. El access token expira.
2. Cliente llama a `POST /auth/refresh` con refresh token.
3. Si es válido:
   - Se emite nuevo access token.
   - Opcionalmente se rota el refresh token.

---

### 5.4 Logout / Revocación

- El cliente puede invalidar su sesión.
- El refresh token se marca como revocado.
- El access token expira de forma natural.

---

## 6. Transporte y Almacenamiento

- Transporte siempre sobre **TLS 1.3**.
- Access token:
  - Header `Authorization: Bearer <token>`.
- Refresh token:
  - Preferentemente en **cookie HttpOnly**.

---

## 7. Modelo de Persistencia de Refresh Tokens

Esta sección define el **modelo de persistencia** para los refresh tokens en Fase 1.

### 7.1 Entidad Persistida

Se persiste una entidad `RefreshToken`, asociada a una cuenta autenticable.

Campos mínimos:
- `id` (Identifier)
- `accountId` (Identifier)
- `token` (String, valor opaco, único)
- `issuedAt` (Instant)
- `expiresAt` (Instant)
- `revokedAt` (Instant, nullable)

Notas:
- El valor del token se genera de forma criptográficamente segura.
- El token **nunca** se deriva de credenciales del usuario.

---

### 7.2 Reglas de Persistencia

- Un refresh token pertenece a **una única cuenta**.
- Un refresh token puede revocarse explícitamente.
- Un refresh token expirado no es reutilizable.
- Opcionalmente se puede permitir **un único refresh token activo por cuenta** en Fase 1.

---

### 7.3 Operaciones Necesarias

El sistema debe soportar, como mínimo:
- Crear refresh token.
- Buscar refresh token por valor.
- Revocar refresh token.
- Revocar todos los refresh tokens de una cuenta.

Estas operaciones se implementan en la capa de infraestructura y **no forman parte de los casos de uso de dominio**.

---

## 7. Relación con los Use Cases

- `AuthenticateAccountUseCase` **no emite tokens**.
- La emisión de tokens ocurre en la **capa web / security adapter**.
- Los tokens contienen únicamente el `accountId`.

---

## 8. Evolución Futura

Este diseño permite evolucionar hacia:
- OAuth 2.1 / OpenID Connect.
- Múltiples sesiones por cuenta.
- Gestión de dispositivos.
- Autenticación passwordless.

Sin romper los casos de uso existentes.

---

## 9. Decisiones Cerradas (Fase 1)

- ✔️ Autenticación propia.
- ✔️ JWT solo para access token.
- ✔️ Refresh tokens opacos y persistidos.
- ✔️ Stateless backend.
- ❌ OAuth en Fase 1.

---

*Este documento define decisiones de infraestructura y puede evolucionar sin afectar al dominio ni a los casos de uso.*

