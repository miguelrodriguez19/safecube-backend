# Auth Use Cases – SafeCube Backend (v1)

> Documento de **diseño de casos de uso** para el slice **auth**.
>
> Este documento **no es código**, ni describe detalles de framework (Spring, HTTP, JPA).
> Su objetivo es definir el **comportamiento del sistema** desde el punto de vista de la aplicación.

---

## Convenciones

- Los casos de uso se describen desde la perspectiva del **backend**.
- Los casos de uso exponen **DTOs de aplicación explícitos** (Input/Output), independientes de HTTP y de persistencia.
- Los DTOs de aplicación **no son entidades de dominio** ni DTOs web.
- Los errores representan **fallos esperados del dominio o la aplicación**, no excepciones técnicas.
- Las dependencias listadas son **ports** (interfaces), nunca implementaciones.

---

## 1. RegisterAccountUseCase

### 1.1 Descripción

Permite registrar una nueva cuenta de autenticación en el sistema a partir de credenciales proporcionadas por un cliente.

El backend crea una nueva identidad autenticable, asegurando:
- unicidad del identificador (email / username),
- persistencia segura de credenciales (hash),
- y consistencia básica del estado inicial de la cuenta.

---

### 1.2 Actor

- Usuario final (a través de un cliente: mobile, web, etc.).

---

### 1.3 Input

DTO de entrada del caso de uso:

- `RegisterAccountCommand`
  - `email` (String)
  - `rawPassword` (String)

Notas:
- El DTO representa una **intención de aplicación**, no una request HTTP.
- El password llega **en claro** al backend (vía TLS 1.3).
- La validación de formato básico ocurre en este nivel.

---

### 1.4 Output

DTO de salida del caso de uso:

- `RegisterAccountResult`
  - `accountId` (Identifier)
  - `createdAt` (Instant)

Notas:
- El DTO de salida define **el contrato del caso de uso**.
- No se exponen entidades de dominio fuera de la capa de aplicación.

---

### 1.5 Errores Esperados

El caso de uso puede fallar de forma controlada en los siguientes escenarios:

- `AccountAlreadyExists`
  - Ya existe una cuenta registrada con el mismo email.

- `InvalidCredentials`
  - El email o la contraseña no cumplen los requisitos mínimos definidos.

---

### 1.6 Reglas de Negocio

- El email debe ser único en el sistema.
- La contraseña debe cumplir la política mínima definida para Fase 1.
- El password **nunca** se persiste en claro.
- El estado inicial de la cuenta es consistente y habilitado.

---

### 1.7 Dependencias (Ports)

El caso de uso depende exclusivamente de los siguientes contratos:

- `AuthAccountRepository`
  - Verificar existencia por email.
  - Persistir una nueva cuenta.

- `PasswordHasher`
  - Transformar la contraseña en un hash seguro.

---

### 1.8 Notas de Implementación

- El caso de uso debe ser **determinista** y **side-effect free** salvo por la persistencia explícita.
- No debe lanzar excepciones técnicas.
- No depende de HTTP, Spring Security, JWT u otros mecanismos.

---

## 2. AuthenticateAccountUseCase

### 2.1 Descripción

Permite autenticar a un usuario previamente registrado en el sistema mediante la verificación de sus credenciales.

El backend valida la identidad autenticable asociada al identificador proporcionado y, si las credenciales son correctas, confirma la autenticación y devuelve la información mínima necesaria para continuar el flujo de sesión.

---

### 2.2 Actor

- Usuario final (a través de un cliente: mobile, web, etc.).

---

### 2.3 Input

DTO de entrada del caso de uso:

- `AuthenticateAccountCommand`
  - `email` (String)
  - `rawPassword` (String)

Notas:
- El DTO representa una **intención de autenticación**, no una request HTTP.
- El password llega **en claro** al backend (vía TLS 1.3).
- No se asume ningún mecanismo de sesión o token en este nivel.

---

### 2.4 Output

DTO de salida del caso de uso:

- `AuthenticateAccountResult`
  - `accountId` (Identifier)
  - `authenticatedAt` (Instant)

Notas:
- El resultado indica que la identidad ha sido autenticada correctamente.
- No se emiten tokens, cookies ni credenciales derivadas en este nivel.

---

### 2.5 Errores Esperados

El caso de uso puede fallar de forma controlada en los siguientes escenarios:

- `AccountNotFound`
  - No existe ninguna cuenta registrada con el email proporcionado.

- `InvalidCredentials`
  - Las credenciales proporcionadas no son válidas.

- `AccountDisabled`
  - La cuenta existe pero se encuentra deshabilitada.

---

### 2.6 Reglas de Negocio

- La autenticación solo es posible para cuentas existentes.
- La cuenta debe encontrarse en estado habilitado.
- La verificación de credenciales se realiza comparando el hash almacenado con el secreto proporcionado.
- El backend **no revela** si el fallo se debe a email o contraseña incorrectos, salvo a nivel interno.

---

### 2.7 Dependencias (Ports)

El caso de uso depende exclusivamente de los siguientes contratos:

- `AuthAccountRepository`
  - Recuperar la cuenta por email.

- `PasswordHasher`
  - Verificar la contraseña proporcionada frente al hash almacenado.

---

### 2.8 Notas de Implementación

- El caso de uso no gestiona sesiones ni tokens.
- Cualquier mecanismo de emisión de tokens se realiza en capas superiores.
- El caso de uso debe ser **idempotente** desde el punto de vista del estado persistido.

---

---

## 3. Tests de Aplicación (Application Tests)

Esta sección define los **tests de aplicación** que deben existir para validar el comportamiento de los casos de uso del slice `auth`.

Los tests:
- Se ejecutan **sin framework** (sin Spring, sin HTTP, sin base de datos real).
- Usan **dobles de test** (stubs/fakes/mocks) para los ports.
- Validan **contratos, reglas de negocio y errores**, no implementación.

Estos tests se sitúan en la **capa intermedia de la pirámide del testing** (tests de servicio / aplicación), y se complementan con tests unitarios y tests de aceptación.

---

### 3.1 Tests – RegisterAccountUseCase

#### Caso exitoso
- **Given** un email no registrado y una contraseña válida.
- **When** se ejecuta `RegisterAccountUseCase`.
- **Then**:
  - Se persiste una nueva cuenta.
  - La contraseña se almacena como hash (nunca en claro).
  - Se devuelve un `RegisterAccountResult` con `accountId` y `createdAt`.

#### Email ya existente
- **Given** un email ya registrado.
- **When** se ejecuta `RegisterAccountUseCase`.
- **Then** se devuelve el error `AccountAlreadyExists`.

#### Credenciales inválidas
- **Given** un email con formato inválido o una contraseña que no cumple la política mínima.
- **When** se ejecuta `RegisterAccountUseCase`.
- **Then** se devuelve el error `InvalidCredentials`.

#### Contrato de dependencias
- El `PasswordHasher` es invocado exactamente una vez.
- El repositorio **no persiste** ninguna cuenta si ocurre un error.

---

### 3.2 Tests – AuthenticateAccountUseCase

#### Caso exitoso
- **Given** una cuenta existente y habilitada.
- **And** una contraseña válida.
- **When** se ejecuta `AuthenticateAccountUseCase`.
- **Then**:
  - Se valida correctamente la contraseña.
  - Se devuelve un `AuthenticateAccountResult` con `accountId` y `authenticatedAt`.

#### Cuenta inexistente
- **Given** un email no registrado.
- **When** se ejecuta `AuthenticateAccountUseCase`.
- **Then** se devuelve el error `AccountNotFound`.

#### Credenciales incorrectas
- **Given** una cuenta existente.
- **And** una contraseña incorrecta.
- **When** se ejecuta `AuthenticateAccountUseCase`.
- **Then** se devuelve el error `InvalidCredentials`.

#### Cuenta deshabilitada
- **Given** una cuenta existente pero deshabilitada.
- **When** se ejecuta `AuthenticateAccountUseCase`.
- **Then** se devuelve el error `AccountDisabled`.

#### Contrato de dependencias
- El repositorio se consulta una sola vez.
- El `PasswordHasher` se usa únicamente para verificación.
- No se modifica el estado persistido de la cuenta.

---

## 4. Tests de Aceptación (Acceptance Tests)

Esta sección define los **tests de aceptación** del slice `auth`, implementados con **Karate Framework**.

Estos tests se sitúan en la **parte superior de la pirámide del testing** y validan el sistema desde el punto de vista del consumidor de la API, ejecutándose contra una instancia real del backend (con infraestructura real o embebida).

Características:
- Se ejecutan vía **HTTP real**.
- No mockean lógica de negocio.
- Validan contratos HTTP, códigos de estado y payloads.
- Cubren flujos completos extremo a extremo.

---

### 4.1 Acceptance Tests – Registro de Cuenta

#### Escenario: Registro exitoso
- **Given** un backend en ejecución.
- **And** un email no registrado.
- **When** se realiza una petición `POST /auth/register` con credenciales válidas.
- **Then**:
  - La respuesta es `201 Created`.
  - El body contiene un `accountId` válido.
  - No se expone información sensible.

#### Escenario: Email duplicado
- **Given** una cuenta existente.
- **When** se realiza una petición `POST /auth/register` con el mismo email.
- **Then**:
  - La respuesta es `409 Conflict`.
  - El error es semánticamente consistente (no técnico).

#### Escenario: Credenciales inválidas
- **Given** un email o contraseña inválidos.
- **When** se realiza una petición `POST /auth/register`.
- **Then**:
  - La respuesta es `400 Bad Request`.

---

### 4.2 Acceptance Tests – Autenticación

#### Escenario: Autenticación exitosa
- **Given** una cuenta existente y habilitada.
- **When** se realiza una petición `POST /auth/login` con credenciales válidas.
- **Then**:
  - La respuesta es `200 OK`.
  - El body contiene un identificador de cuenta.

#### Escenario: Credenciales incorrectas
- **Given** una cuenta existente.
- **When** se realiza una petición `POST /auth/login` con contraseña incorrecta.
- **Then**:
  - La respuesta es `401 Unauthorized`.
  - El error no revela información sensible.

#### Escenario: Cuenta deshabilitada
- **Given** una cuenta deshabilitada.
- **When** se realiza una petición `POST /auth/login`.
- **Then**:
  - La respuesta es `403 Forbidden`.

---

### 4.3 Principios de Tests de Aceptación

- Un test de aceptación valida **un flujo de negocio completo**.
- No se testean detalles internos ni implementación.
- Los tests deben ser **deterministas y repetibles**.
- Los datos de prueba se preparan explícitamente.
- Si un test de aceptación falla, el sistema **no cumple el contrato externo**.

---

*Este documento evoluciona junto al código, pero siempre precede a la implementación.*

