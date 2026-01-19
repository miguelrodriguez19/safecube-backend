# ADR-005: Dynamic Filtering with JPA Specifications

* **Estado**: Accepted
* **Fecha**: 2026-01-19
* **Decisores**: SafeCube Backend
* **Slice**: vault
* **Relacionado con**:
    * `vault_use_cases_safe_cube_backend_v_1.md`
    * `database_strategy.md`

---

## Contexto

El slice `vault` debe soportar **listados dinámicos de SecureItems** para permitir:

- Sincronización incremental (`since`)
- Filtrado por tipo (`itemType`)
- Inclusión/exclusión de elementos eliminados (`includeDeleted`)
- Extensibilidad futura (labels, ordering, límites, etc.)

Estas capacidades deben cumplir los principios del sistema:

- **Zero-Knowledge**: el backend no interpreta payloads
- **Aislamiento por cuenta**
- **Infraestructura agnóstica del dominio**
- **Contratos estables y extensibles**

Durante la implementación de `ListSecureItemsUseCase` surgió la necesidad de
componer filtros dinámicos de forma **segura, legible y extensible**.

---

## Opciones Consideradas

### Opción 1 — Queries estáticas por combinación

Crear múltiples métodos en el repositorio, por ejemplo:

- `findByAccountId`
- `findByAccountIdAndUpdatedAtAfter`
- `findByAccountIdAndType`
- etc.

**Descartado** porque:

- Explosión combinatoria de métodos
- Poco mantenible
- Difícil de extender sin romper contratos
- Lógica de filtrado dispersa

---

### Opción 2 — Query dinámica manual (Criteria API / JPQL)

Construir queries dinámicas directamente con Criteria API o JPQL.

**Descartado** porque:

- Verboso y difícil de leer
- Alto riesgo de errores
- Mezcla lógica de filtrado con infraestructura
- Poco expresivo para tests

---

### Opción 3 — JPA Specifications (elegida)

Usar `org.springframework.data.jpa.domain.Specification`
para construir filtros **componibles y reutilizables**.

---

## Decisión

Se adopta el uso de **JPA Specifications** para implementar
el filtrado dinámico de `SecureItem` en el slice `vault`.

Cada criterio de filtrado se representa como una `Specification`
aislada, reutilizable y testeable.

Ejemplos:

- `accountIs(accountId)`
- `createdAt(since)`
- `hasType(type)`
- `notDeleted()`

Las specifications se **componen dinámicamente** en el adaptador
de persistencia en función del filtro recibido.

---

## Implementación

### Ubicación

Las specifications viven en el paquete:
`vault.infrastructure.persistence.specification`

Ejemplo:

```txt
  Specification<SecureItemJpaEntity> spec =
      Specification.where(accountIs(accountId));
  
  if (filter.since() != null) {
    spec = spec.and(createdAt(filter.since()));
  }
  
  if (filter.type() != null) {
    spec = spec.and(hasType(filter.type().name()));
  }
  
  if (!filter.includeDeleted()) {
    spec = spec.and(notDeleted());
  }
```

---

## Consecuencias

### Positivas

* ✔ Filtros **componibles y legibles**
* ✔ Alta extensibilidad (añadir filtros no rompe contratos)
* ✔ Fácil cobertura por tests unitarios
* ✔ Alineado con Spring Data
* ✔ Lógica de filtrado centralizada
* ✔ No expone detalles de persistencia al dominio

### Negativas / Trade-offs

* ❌ Dependencia explícita de Spring Data JPA en infraestructura
* ❌ Requiere tests específicos para specifications
* ❌ No portable tal cual a otros motores sin adaptación

---

## Relación con el Dominio

* El **dominio no conoce Specifications**
* El dominio expresa intención mediante `ListSecureItemsFilter`
* La traducción a Specifications ocurre **exclusivamente en infraestructura**

Esto mantiene el dominio:

* Limpio
* Testeable
* Independiente de JPA

---

## Impacto en Testing

### Unit Tests

* Cada `Specification` se prueba de forma aislada
* Se valida la construcción correcta de `Predicate`

### Application Tests

* Se valida la composición correcta de filtros
* Sin necesidad de base de datos real

### Integration / Acceptance Tests

* Se valida el comportamiento observable del filtrado vía HTTP
* Casos como:

    * `since`
    * `includeDeleted=false`
    * `type`

---

## Decisiones Rechazadas Explícitamente

* ❌ Filtros embebidos en payload cifrado
* ❌ Queries dinámicas manuales en JPQL
* ❌ Lógica de filtrado en el dominio
* ❌ Exposición de criterios de filtrado internos vía API

---

## Estado Final

Esta decisión se considera **cerrada para v1**.

Cualquier nuevo filtro deberá:

1. Expresarse en `ListSecureItemsFilter`
2. Implementarse como una nueva `Specification`
3. Añadir tests unitarios e integración
4. No romper el contrato HTTP existente

---

**Este ADR formaliza una decisión ya validada por implementación y tests,
y sirve como referencia canónica para futuras extensiones del slice `vault`.**
