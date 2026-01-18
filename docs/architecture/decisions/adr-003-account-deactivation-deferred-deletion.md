# ADR-003: Account Deactivation & Deferred Deletion

* **Estado**: Accepted (design-only, not implemented in v1)
* **Fecha**: 2026-01-16
* **Decisores**: SafeCube Backend
* **Contexto**: Sistema · Slices `auth`, `user`, `vault`

---

## Contexto

SafeCube debe ofrecer, en algún momento, un mecanismo para que un usuario pueda
**abandonar el sistema** y ejercer su derecho a la eliminación de datos.

Durante la fase de definición del MVP se evaluó la posibilidad de implementar
un **borrado completo de cuentas**, incluyendo perfiles y datos cifrados.

Sin embargo, dicha funcionalidad introduce complejidad significativa:

* Coordinación entre múltiples slices (`auth`, `user`, `vault`)
* Riesgo de pérdida irreversible de datos
* Necesidad de procesos batch / administrativos
* Implicaciones de RGPD y auditoría

Todo ello **no aporta valor directo al MVP**, cuyo objetivo principal es validar
la autenticación y la gestión segura de secretos.

---

## Decisión

### ❌ No existe borrado definitivo de cuentas en v1

En la versión v1 de SafeCube:

* **No existe** un `DeleteAccountUseCase`
* **No existe** borrado físico de cuentas
* **No existe** cleanup automático de datos
* **No existe** un job programado de eliminación

La acción de “borrar cuenta” **no está disponible** para el usuario final en v1.

---

### ✔️ Modelo conceptual adoptado (no implementado)

El modelo aceptado para versiones futuras es:

1. **Desactivación de cuenta**
   * La cuenta se marca como deshabilitada (`enabled = false`)
   * Se registra un timestamp (`disabled_at`)
   * Se revocan las sesiones activas

2. **Ventana de gracia**
   * Periodo conceptual (ej. 90 días)
   * Si el usuario vuelve a autenticarse, la cuenta se reactiva
   * No se elimina ningún dato durante este periodo

3. **Eliminación diferida y orquestada**
   * Proceso técnico / administrativo
   * Limpieza explícita y ordenada de datos:
     1. Datos del vault
     2. Perfil de usuario
     3. Cuenta autenticable

Este flujo **no se implementa en v1**, pero queda aceptado como dirección futura.

---

## Justificación

### 1. El MVP no requiere borrado de cuentas

La capacidad de eliminar una cuenta **no es crítica** para validar:

* Registro
* Autenticación
* Sesiones
* CRUD de SecureItems

Retrasar esta funcionalidad permite acelerar la entrega del MVP.

---

### 2. Evitar sobreingeniería prematura

Implementar borrado definitivo implica:

* Casos de uso adicionales
* Jobs programados
* Manejo de errores parciales
* Decisiones de auditoría
* Tests complejos

Todo ello sin una necesidad inmediata.

---

### 3. Separación clara de responsabilidades

* `auth` es el dueño del ciclo de vida de la cuenta
* `user` no gestiona eliminación de perfiles (ver ADR-002)
* `vault` no inicia ni decide borrados

El borrado definitivo es una **preocupación sistémica**, no de un slice aislado.

---

## Consecuencias

### Positivas

* MVP más simple y enfocado
* Menor riesgo de pérdida de datos
* Arquitectura preparada para evolución
* Decisiones claras y documentadas

---

### Negativas / Trade-offs

* No existe derecho al olvido inmediato en v1
* Los datos permanecen almacenados tras la desactivación
* Requiere implementación futura coordinada

Estas consecuencias se aceptan explícitamente.

---

## Estado Actual (v1)

* No existe endpoint de borrado de cuenta
* No existe `DisableAccountUseCase`
* No existe batch de cleanup
* Las cuentas solo pueden existir en estado habilitado o deshabilitado técnico
* Los datos de `user` y `vault` no se eliminan

---

## Evolución Futura (fuera de v1)

Posibles extensiones futuras:

* `RequestAccountDeletionUseCase`
* Ventana de gracia configurable (ej. 90 días)
* Job programado de cleanup
* Orquestación auth → vault → user
* Nuevos ADRs si la complejidad aumenta

---

## Relación con otros ADRs

* ADR-001: Auth Authentication & Session Strategy
* ADR-002: User Profile Lifecycle & Non-Deletable Profiles

---

## Regla de Oro

> **En v1 no se borran cuentas.**
> **La eliminación definitiva es diferida, orquestada y futura.**

