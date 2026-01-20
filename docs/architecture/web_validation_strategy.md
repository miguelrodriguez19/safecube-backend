# Web Validation Strategy – SafeCube Backend

Este documento define la **estrategia de validación en la capa web (HTTP)** del backend de SafeCube.

Describe **qué se valida**, **dónde se valida**, **cómo se reportan los errores** y **qué decisiones están
explícitamente descartadas**.

Este documento es **canónico** para todos los slices expuestos vía HTTP (`auth`, `user`, `vault`).

---

## 1. Objetivos

La validación web en SafeCube persigue:

- Rechazar **requests inválidas lo antes posible**
- Proteger el dominio de datos malformados
- Garantizar **contratos HTTP claros y consistentes**
- Evitar lógica de negocio en la capa web
- Mantener el dominio **framework-agnóstico**

---

## 2. Principios Fundamentales

### 2.1 Validación ≠ Negocio

- La validación web **no aplica reglas de negocio**
- Solo valida **forma, tipo y coherencia básica**
- Las reglas de negocio viven en:
    - Casos de uso
    - Dominio

---

### 2.2 Fail Fast

- Requests inválidas se rechazan **antes de entrar al caso de uso**
- No se ejecuta lógica innecesaria
- Se devuelve un error HTTP determinista

---

### 2.3 Contratos Explícitos

- Cada endpoint define explícitamente:
    - Qué campos son obligatorios
    - Qué formato se espera
    - Qué valores son aceptables
- No hay validación implícita ni “best effort”

---

### 2.4 Seguridad por Defecto

- Entradas inválidas **no se normalizan**
- Se rechazan inputs sospechosos (UUIDs inválidos, enums incorrectos, etc.)
- No se filtran ni transforman valores peligrosos

---

## 3. Capas de Validación

### 3.1 Nivel HTTP / Controller

Se valida:

- Tipos básicos (`UUID`, `Instant`, `enum`)
- Presencia de campos requeridos
- Restricciones simples (longitud, nullabilidad)

Herramientas:

- Jakarta Bean Validation (`jakarta.validation`)
- Anotaciones estándar (`@NotNull`, `@Positive`, etc.)
- Anotaciones custom (`@ValidItemType`, `@ValidOrder`)

Ejemplo:

```java

@GetMapping
public ResponseEntity<ListSecureItemsResponse> list(
        @AuthenticationPrincipal final UUID accountId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final Instant since,
        @RequestParam(required = false) @ValidItemType final String type,
        @RequestParam(required = false) final Set<String> labels,
        @RequestParam(required = false, defaultValue = "false") final boolean includeDeleted,
        @RequestParam(required = false) @Positive final Integer limit,
        @RequestParam(required = false, defaultValue = "DISPLAY_NAME_ASC") @ValidOrder final String order) { ...}

````

---

### 3.2 DTO Validation

Los DTOs de request:

* Se validan con `@Valid`
* No contienen lógica
* Solo expresan restricciones estructurales

Ejemplo:

```java
public record UpdateSecureItemRequest(
        @NotNull String itemType,
        @Positive int schemaVersion,
        String displayHint,
        @NotNull String payload,
        @NotNull Instant updatedAt
) {
}
```

---

### 3.3 Validaciones Custom

Cuando una restricción **no puede expresarse** con anotaciones estándar:

* Se define un `@Constraint`
* Se implementa un `ConstraintValidator`

Ejemplos de uso legítimo:

* Validar enums enviados como `String`
* Validar valores cerrados de ordenación
* Validar formatos no estándar

Ejemplo:

```java

@Constraint(validatedBy = ItemTypeValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface ValidItemType {
    String message() default "Invalid itemType";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

> Los tres métodos declarados son **impuestos** por el contrato del
> framework para que se reconozca el validator

---

## 4. Qué **NO** se Valida en la Capa Web

Explícitamente **NO** se valida:

* Concurrencia (`updatedAt`, versiones)
* Existencia de entidades
* Autorización de negocio
* Relaciones entre agregados
* Contenido semántico del payload cifrado

Estas validaciones pertenecen al **dominio**.

---

## 5. Manejo de Errores

### 5.1 Estrategia Global

Todos los errores de validación HTTP se capturan en un
`@RestControllerAdvice` único.

Responsabilidades:

* Mapear excepciones a códigos HTTP
* Construir respuestas homogéneas
* Evitar fugas de detalles internos

---

### 5.2 Formato de Error

Se utiliza el objeto `ErrorResponse`:

```json
{
  "error": "VALIDATION_FAILED",
  "fields": {
    "itemType": "Invalid itemType",
    "updatedAt": "must not be null"
  }
}
```

Características:

* `error`: código semántico estable
* `fields`: mapa campo → mensaje (opcional)

---

### 5.3 Excepciones Gestionadas

| Excepción                          | HTTP |
|------------------------------------|------|
| `MethodArgumentNotValidException`  | 400  |
| `ConstraintViolationException`     | 400  |
| `HandlerMethodValidationException` | 400  |
| `DomainException`                  | 400  |
| `InfrastructureException`          | 500  |

Errores no controlados → `500 Internal Server Error`

---

## 6. Logging de Errores

* La capa web **no decide comportamiento por entorno**
* El logging sigue una estrategia uniforme
* No se exponen stack traces al cliente
* Los detalles se registran solo en logs

La diferenciación por entorno se considera **fuera de alcance** de la validación.

---

## 7. Testing de la Validación Web

### 7.1 Unit Tests

* Tests de `ConstraintValidator`
* Tests de anotaciones custom
* Sin Spring

---

### 7.2 Integration / Acceptance Tests

* Karate valida:

    * HTTP 400 ante inputs inválidos
    * Mensajes de error coherentes
    * Rechazo de payloads malformados

Ejemplos:

* UUID inválido en path
* Enum incorrecto
* Campos requeridos ausentes

---

## 8. Decisiones Explícitas

✔ Bean Validation como estándar
✔ Validación temprana (fail fast)
✔ Errores HTTP homogéneos
✔ Anotaciones custom para enums y órdenes
✔ Dominio libre de validación HTTP

❌ Validación de negocio en controllers
❌ Normalización automática de inputs
❌ Mensajes de error dependientes del entorno
❌ Exposición de excepciones internas

---

## 9. Evolución Futura

Posibles extensiones:

* Grupos de validación (`groups`) si surge un caso real
* Internacionalización de mensajes
* Validación de headers (versionado API)

---

> **Regla de oro**:
> La capa web valida **la forma del mensaje**,
> el dominio valida **el significado del sistema**.

