# ADR-002: User Profile Lifecycle & Non-Deletable Profiles

* **Estado**: Accepted
* **Fecha**: 2026-01-16
* **Decisores**: SafeCube Backend
* **Contexto**: Slice `user`

---

## Contexto

SafeCube separa explícitamente:

* **Identidad autenticable** → slice `auth`
* **Perfil de usuario** → slice `user`

Durante el diseño e implementación del slice `user`, se evaluó cómo debía gestionarse el **ciclo de vida del perfil de usuario**, especialmente en relación con:

* Eliminación de cuentas
* RGPD / derecho al olvido
* Coordinación con el slice `auth`
* Complejidad operativa innecesaria en fases tempranas

El diseño inicial incluía un **DeleteUserProfileUseCase**, pero su implementación real introducía más problemas que beneficios en la fase actual del producto.

---

## Decisión

### ❌ No existe eliminación de perfil de usuario en v1

El slice `user` **NO soporta**:

* Eliminación física del perfil
* Eliminación lógica (`deletedAt`)
* Recuperación de perfiles eliminados
* Estados “soft-deleted”

Un **UserProfile**:

* Se crea una sola vez
* Se puede **leer**
* Se puede **actualizar**
* **Nunca se elimina** en v1

---

## Justificación

### 1. El perfil no es la identidad

El perfil de usuario:

* No representa la identidad legal ni autenticable
* No controla acceso ni sesiones
* No es el punto de verdad de la cuenta

Eliminar un perfil **no equivale** a eliminar una cuenta.

---

### 2. La baja de cuenta es responsabilidad del slice `auth`

La desactivación real de un usuario implica:

* Revocar sesiones
* Deshabilitar credenciales
* Invalidar refresh tokens
* Posible borrado de datos sensibles

Estas responsabilidades pertenecen al slice `auth`, no al `user`.

---

### 3. Evitar falsa complejidad (soft delete prematuro)

Introducir:

* `deletedAt`
* Estados “inactive”
* Filtros implícitos en queries
* Lógica condicional transversal

…sin un caso de negocio claro es **sobreingeniería**.

---

### 4. RGPD se gestiona a nivel de sistema, no de perfil

El cumplimiento de RGPD:

* Requiere procesos orquestados
* Implica múltiples slices
* Se ejecuta de forma asíncrona / administrativa

No se resuelve correctamente con un `DeleteUserProfileUseCase`.

---

## Consecuencias

### Positivas

✅ Modelo de dominio más simple
✅ Reglas claras y fáciles de testear
✅ Sin estados ambiguos
✅ Queries sin filtros implícitos
✅ Menos deuda técnica temprana

---

### Negativas / Trade-offs

⚠️ El perfil no desaparece automáticamente
⚠️ La “baja de usuario” no es instantánea en v1
⚠️ Requiere coordinación futura con `auth` y `vault`

Estas consecuencias son **aceptadas explícitamente**.

---

## Estado Actual (v1)

* No existe `DeleteUserProfileUseCase`
* No existe `deletedAt` en `UserProfile`
* El repositorio no filtra por estado
* Los controladores no exponen endpoints de borrado
* Los tests asumen perfiles siempre activos

---

## Evolución Futura (No implementado)

Posibles evoluciones **explícitamente fuera de v1**:

* `DisableAccountUseCase` (slice `auth`)
* Borrado orquestado de datos (GDPR job)
* Pseudonimización de perfiles
* Hard delete diferido mediante batch

Estas decisiones requerirán **nuevos ADRs**.

---

## Relación con otros ADRs

* ADR-001: Auth Authentication & Session Strategy
* ADR-XXX (futuro): Account Deactivation & GDPR Cleanup

---

## Regla de Oro

> **Un perfil de usuario no se elimina.
> Una cuenta se desactiva.
> Los datos se limpian de forma orquestada.**
