# 🧱 SafeCube – Architecture Tests Specification

## 1. Propósito

Este documento define las **reglas arquitectónicas obligatorias** del backend de SafeCube y las **pruebas de
arquitectura** que las hacen cumplir mediante ArchUnit.

Las reglas aquí descritas:

- Reflejan decisiones explícitas de diseño
- Protegen la arquitectura frente a degradación progresiva
- Forman parte del contrato del proyecto

Si una regla está aquí, **romperla rompe el build**.

---

## 2. Alcance

Estas reglas aplican al proyecto `safecube-backend`, diseñado como:

- Monolito modular
- Organización por *vertical slices* (`auth`, `user`, `vault`)
- Separación explícita entre `domain`, `application` e `infrastructure`

Spring está **permitido** en `application` e `infrastructure`, pero **prohibido** en `domain`.

---

## 3. Principio Base

> Las dependencias siempre apuntan hacia dentro

```
DOMAIN ← APPLICATION ← INFRASTRUCTURE
```

---

## 4. Reglas Arquitectónicas (ARC)

### ARC-D01 — El dominio no depende de Spring

**Descripción**  
La capa `domain` debe ser completamente independiente de Spring.

**Regla**

- Paquetes `..domain..`:
    - ❌ No pueden usar anotaciones Spring
    - ❌ No pueden depender de `org.springframework..`

**Justificación**

- Dominio puro y testeable
- Evita acoplamiento irreversible

---

### ARC-D02 — El dominio no depende de infraestructura

**Regla**

- `..domain..` ❌ no puede depender de `..infrastructure..`

**Justificación**

- Infraestructura es un detalle técnico

---

### ARC-A01 — Application no accede a infraestructura técnica

**Regla**

- `..application..` ❌ no puede depender de:
    - `..infrastructure.persistence..`
    - `..infrastructure.web..`
    - `..infrastructure.client..`

**Permitido**

- Spring (`@Service`, `@Transactional`)
- Dependencias a `domain`
- Dependencias a `application.port`

---

### ARC-A02 — Errores de negocio explícitos

**Descripción**  
Los errores de negocio esperables deben ser explícitos en el contrato del UseCase.

**Regla**

- Un UseCase que puede fallar por razones de negocio:
    - ✅ debe modelar esos fallos explícitamente (por ejemplo `Result`)
    - ❌ no debe lanzar excepciones de negocio

**Notas**

- UseCases sin errores de negocio pueden devolver directamente `T` o `void`
- Excepciones técnicas quedan fuera del alcance de esta regla

---

### ARC-A03 — UseCases no dependen de persistencia concreta

**Regla**

- `..application..` ❌ no puede usar:
    - `JpaRepository`
    - `EntityManager`
    - clases JPA concretas

**Permitido**

- Interfaces definidas en `application.port`

---

### ARC-I01 — Infraestructura implementa ports

**Regla**

- Clases en `..infrastructure.persistence..` acabadas en `Adapter`:
    - ✅ deben implementar al menos una interfaz de `application.port`

---

### ARC-W01 — Controllers sin lógica de negocio

**Regla**

- `..infrastructure.web..`:
    - ❌ no accede a repositories
    - ❌ no crea entidades de dominio
    - ✅ solo invoca UseCases y mapea DTOs

---

### ARC-S01 — Aislamiento entre slices

**Regla**

- `auth.domain` ❌ no depende de `user.domain` ni `vault.domain`
- `vault.domain` ❌ no depende de `auth.domain`

**Comunicación permitida**

- Identificadores
- Tipos primitivos

---

### ARC-N01 — Spring prohibido en domain

**Regla**

- `..domain..` ❌ no puede usar anotaciones Spring

---

## 5. Severidad

| Regla  | Severidad |
|--------|-----------|
| ARC-D* | CRÍTICA   |
| ARC-A* | ALTA      |
| ARC-W* | ALTA      |
| ARC-S* | CRÍTICA   |
| ARC-N* | MEDIA     |

---

## 6. Evolución

- Este documento precede al código
- Cualquier nueva regla:
    1. Se documenta aquí
    2. Se discute
    3. Se implementa en ArchUnit

---

## 7. Resultado esperado

- La arquitectura no puede degradarse silenciosamente
- Las decisiones quedan codificadas
- ArchUnit actúa como sistema inmunológico del proyecto

