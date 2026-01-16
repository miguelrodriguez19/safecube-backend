# ADR-001: Diseño de Autenticación y Sesión (Slice Auth)

## Estado

Aceptado

## Contexto

SafeCube requiere un mecanismo de autenticación que:

- funcione con clientes mobile, web y API,
- evite estado de sesión en servidor,
- sea fácil de razonar, testear y auditar,
- permita evolucionar en el futuro sin romper el dominio.

Durante la fase inicial del proyecto se exploraron distintas aproximaciones
mediante varios documentos de diseño. Una vez el slice `auth` alcanzó un
estado estable y completamente testeado, las decisiones técnicas quedaron
claramente definidas.

Este ADR documenta **las decisiones técnicas realmente implementadas** en el
backend de SafeCube para el slice `auth`.

---

## Decisiones

### 1. Separación entre autenticación y sesión

- Autenticar credenciales y gestionar sesiones son responsabilidades distintas.
- La verificación de credenciales se realiza en casos de uso de aplicación.
- La representación de la sesión (tokens) se gestiona en infraestructura.

**Justificación**  
Evita acoplar el dominio y la capa de aplicación a detalles técnicos y permite
cambiar la estrategia de sesión sin reescribir los casos de uso.

---

### 2. Backend stateless basado en tokens

- El backend no mantiene estado de sesión en memoria ni en servidor.
- Cada request autenticada debe portar su propia prueba de identidad.

**Justificación**  
Mejora la escalabilidad, simplifica despliegues cloud y evita problemas de
afinidad de sesión.

---

### 3. Modelo Access Token + Refresh Token

- Los access tokens se usan para autorizar requests.
- Los refresh tokens permiten renovar sesiones sin reautenticación constante.

**Justificación**  
Limita el impacto de un token comprometido y permite revocación explícita.

---

### 4. Access tokens como JWT

- Los access tokens se implementan como JWT.
- Contienen únicamente claims mínimos:
    - `sub` (identificador de cuenta)
    - `iat`
    - `exp`

**Justificación**  
Permite validación stateless sin consultas a base de datos y evita incluir
información sensible o mutable.

---

### 5. Refresh tokens opacos y persistidos

- Los refresh tokens son valores opacos y aleatorios.
- Se almacenan en base de datos.
- Pueden revocarse de forma individual o por cuenta.

**Justificación**  
Facilita logout, revocación y futuras extensiones (dispositivos, sesiones).

---

### 6. Rotación de refresh tokens

- Al refrescar la sesión, el refresh token anterior se invalida.
- Se emite un nuevo par de tokens.

**Justificación**  
Reduce el riesgo de replay attacks y sigue buenas prácticas de seguridad.

---

### 7. Logout basado en revocación de refresh tokens

- Logout revoca los refresh tokens.
- Los access tokens no se invalidan activamente y expiran de forma natural.

**Justificación**  
Mantiene el backend stateless y evita listas negras de tokens.

---

### 8. CSRF deshabilitado

- La protección CSRF se deshabilita explícitamente.

**Justificación**  
El sistema no usa cookies ni sesiones implícitas; la autenticación se basa
exclusivamente en Bearer tokens enviados explícitamente en headers.

---

### 9. Dominio libre de frameworks

- El dominio no depende de Spring, JPA, Lombok ni mecanismos de seguridad.
- Los detalles técnicos se encapsulan en infraestructura.

**Justificación**  
Preserva la pureza del dominio y mejora testabilidad y mantenibilidad.

---

### 10. Modelado explícito de errores con Result

- Los casos de uso devuelven `Result<Success, AuthError>`.
- Los fallos esperados se modelan explícitamente.
- No se usan excepciones de negocio.

**Justificación**  
Hace el flujo de errores predecible y evita control de flujo por excepciones.

---

### 11. Estrategia de testing en capas

- Tests unitarios para lógica de dominio y aplicación.
- Tests de integración para adaptadores de persistencia.
- Tests de aceptación (Karate) para flujos HTTP reales.
- Tests de arquitectura para prevenir erosión estructural.

**Justificación**  
Combina feedback rápido con alta confianza en el comportamiento real del sistema.

---

## Consecuencias

### Positivas

- Separación clara de responsabilidades.
- Documentación alineada con código y tests.
- Diseño escalable y evolutivo.

### Trade-offs

- Los access tokens no pueden revocarse inmediatamente.
- Mayor complejidad que sesiones tradicionales.
- Gestión explícita de refresh tokens.

---

## Alternativas consideradas

- Sesiones HTTP en servidor (rechazadas).
- JWT sin refresh tokens (rechazado).
- OAuth/OIDC desde el inicio (rechazado por complejidad prematura).

---

## Notas

Este ADR refleja el estado del sistema al final de la Fase 1.
Futuras decisiones se documentarán en ADRs adicionales.
