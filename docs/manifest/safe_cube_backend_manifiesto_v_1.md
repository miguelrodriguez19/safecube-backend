# SafeCube Backend – Manifiesto (v1)

## 1. Propósito

SafeCube Backend es el **servicio central** encargado de gestionar la **identidad del usuario**, la **autenticación** y la **persistencia de información cifrada**, actuando como intermediario confiable pero **ciego al contenido** de los datos que almacena.

Su objetivo principal es permitir que uno o varios clientes (mobile, web, CLI, etc.) puedan **almacenar, sincronizar y recuperar información sensible** de forma segura, sin que el backend pueda interpretar ni descifrar dicha información.

---

## 2. Principios Fundamentales

1. **Zero-Knowledge by Design**\
   El backend nunca tiene acceso al contenido en claro de la información segura. Todo dato sensible llega cifrado desde el cliente y se almacena como un blob opaco.

2. **Client-Centric Security**\
   Las decisiones criptográficas (cifrado, descifrado, rotación de claves) pertenecen al cliente. El backend solo garantiza transporte, persistencia y control de acceso.

3. **Portabilidad y Autohospedaje**\
   El backend debe poder ejecutarse tanto en infraestructuras cloud como en entornos self-hosted (NAS / homelab) sin cambios de diseño.

4. **Simplicidad antes que Perfección**\
   El sistema prioriza un MVP funcional y comprensible frente a arquitecturas complejas o soluciones criptográficas avanzadas prematuras.

---

## 3. Responsabilidades del Backend

El backend **sí es responsable de**:

- Gestión de usuarios (identidad básica).
- Autenticación y autorización.
- Gestión de sesiones / tokens.
- Persistencia de información cifrada enviada por el cliente.
- Exposición de una API HTTP estable y versionada.
- Aislamiento de datos entre usuarios.

---

## 4. No-Responsabilidades (Límites Explícitos)

El backend **NO es responsable de**:

- Cifrar o descifrar información sensible.
- Validar el significado del contenido almacenado.
- Garantizar recuperación de datos ante pérdida de claves del cliente.
- Aplicar reglas de negocio sobre los secretos.
- Gestionar contraseñas de terceros (webs, apps, etc.) como dominio propio.

---

## 5. Modelo de Datos – Nivel Conceptual

El backend maneja entidades **agnósticas al dominio funcional**, siendo la principal:

- **SecureItem** (nombre provisional):
  - Identificador único
  - Propietario (usuario)
  - Payload cifrado (blob)
  - Metadatos mínimos indexables (no sensibles)
  - Timestamps

El backend no asume qué representa un SecureItem (contraseña, nota, archivo, etc.).

---

## 6. Arquitectura (decisión y criterios)

> Nota: **la arquitectura no está “decidida” todavía**. En Fase 1 elegimos el enfoque que maximice **entrega de MVP + claridad + testabilidad**, minimizando sobreingeniería.

### 6.1 Criterios de decisión

Elegiremos el enfoque arquitectónico según estos criterios, por orden de prioridad:

1. **Velocidad de entrega del MVP** (time-to-first-release).
2. **Claridad para un proyecto personal** (que no requiera ceremonias excesivas).
3. **Separación de responsabilidades** (auth, users, vault) sin acoplarlo todo a Spring.
4. **Testabilidad** (casos de uso verificables sin levantar el contexto completo).
5. **Portabilidad** (cloud/self-hosted) mediante configuración y adaptadores.

### 6.2 Opciones evaluadas

#### Opción A — 3-Layer (Controller → Service → Repository)

- **Pros:** simple, conocida, rápida.
- **Contras:** fácil caer en “service blob”, dominio anémico, lógica acoplada a frameworks si no se disciplina.
- **Encaja si:** el MVP es muy pequeño y priorizamos velocidad pura.

#### Opción B — Hexagonal “light” (Use Cases + Ports + Adapters)

- **Idea:** mantener el **núcleo de aplicación** (casos de uso) independiente; Spring vive en infraestructura/adaptadores.
- **Pros:** testabilidad alta, menos acoplamiento, evoluciona bien hacia self-hosting/infra alterna.
- **Contras:** requiere disciplina; si se exagera aparecen capas y boilerplate.
- **Encaja si:** queremos un backend que crezca sin reescritura y con tests útiles desde el día 1.

#### Opción C — Vertical Slice Architecture (VSA)

- **Idea:** organizar por “features” (auth, vault, users) con su propia lógica y persistencia.
- **Pros:** excelente para entrega incremental, reduce dependencia de capas globales.
- **Contras:** puede duplicar infraestructura si no se controla; hay que definir bien los límites de slice.
- **Encaja si:** queremos iterar muy rápido por funcionalidades sin un “service layer” central.

### 6.3 Decisión para Fase 1 (propuesta)

**Propuesta para el MVP:** combinar lo mejor de B y C, evitando ceremonias:

- **Monolito modular por “vertical slices”** (auth, users, vault).
- Dentro de cada slice, un estilo **hexagonal light**:
  - `application` (use cases)
  - `domain` (model + invariantes mínimos)
  - `infrastructure` (Spring adapters, JPA, controllers)

Esto mantiene el foco en entregar, pero evita que el dominio quede pegado a Spring o que la lógica se disperse.

### 6.4 Regla anti-sobreingeniería

- No se crean “ports” si solo existe un adaptador y no hay pruebas que lo justifiquen.
- No se crean “DTO mappers” genéricos o librerías internas prematuras.
- Si una abstracción no reduce complejidad hoy, no entra en Fase 1.

---

## 7. Alcance de la Fase 1 (MVP)

El MVP del backend incluye exclusivamente:

- Registro y autenticación de usuarios.
- Acceso autenticado a la API.
- CRUD básico de SecureItems.
- Listado de elementos mediante metadatos no sensibles.

Quedan fuera explícitamente:

- Compartición entre usuarios.
- Recuperación de cuentas avanzada.
- Búsqueda compleja.
- Versionado de secretos.

---

## 8. Visión a Medio y Largo Plazo

SafeCube Backend está diseñado para evolucionar hacia:

- Autohospedaje completo en infraestructuras personales.
- Soporte multi-cliente (mobile, web, otros).
- Mejora progresiva de mecanismos de sincronización.
- Endurecimiento incremental de seguridad.

Sin comprometer nunca el principio de **control total del usuario sobre sus datos**.

---

## 9. Arquitectura y Mapa de Paquetes (Propuesta ARC – Hexagonal Light)

> ⚠️ **Advertencia:** esta estructura es exploratoria. Su objetivo es visualizar orden y responsabilidades, **no** fijar un contrato definitivo.

La arquitectura del backend se organiza como un **monolito modular** con *vertical slices* por dominio funcional, aplicando una **arquitectura hexagonal realista** (hexagonal light), orientada a MVP y evolución progresiva.

Cada módulo sigue la separación:

- **Domain** → modelo y reglas puras (sin dependencias técnicas).
- **Application** → casos de uso y contratos (ports).
- **Infrastructure** → adaptadores técnicos (web, persistencia, seguridad).

### 9.1 Estructura de Paquetes Propuesta

```text
com.miguelrodriguez19.safecube
│
├── auth
│   ├── domain
│   │   └── model
│   │       └── AuthAccount.java               (class · Entity / Aggregate Root)
│   │
│   ├── application
│   │   ├── usecase
│   │   │   ├── RegisterAccountUseCase.java    (class · Application Service)
│   │   │   └── AuthenticateAccountUseCase.java(class · Application Service)
│   │   │
│   │   └── port
│   │       └── out
│   │           ├── AuthAccountRepository.java (interface · Port Out)
│   │           └── PasswordHasher.java        (interface · Port Out)
│   │
│   └── infrastructure
│       ├── web
│       │   └── AuthController.java            (class · REST Controller)
│       │
│       ├── persistence
│       │   └── JpaAuthAccountRepository.java  (class · Adapter JPA)
│       │
│       └── security
│           └── BcryptPasswordHasher.java     (class · Adapter Crypto)
│
├── vault
│   ├── domain
│   │   └── model
│   │       └── SecureItem.java                (class · Entity)
│   │
│   ├── application
│   │   ├── usecase
│   │   │   ├── CreateSecureItemUseCase.java   (class · Application Service)
│   │   │   ├── ListSecureItemsUseCase.java    (class · Application Service)
│   │   │   └── GetSecureItemUseCase.java      (class · Application Service)
│   │   │
│   │   └── port
│   │       └── out
│   │           └── SecureItemRepository.java  (interface · Port Out)
│   │
│   └── infrastructure
│       ├── web
│       │   └── VaultController.java           (class · REST Controller)
│       │
│       └── persistence
│           └── JpaSecureItemRepository.java   (class · Adapter JPA)
│
└── shared
│   ├── domain
│   │   └── Identifier.java                    (value object · record)
│   │
│   └── infrastructure
│       └── configuration
│           └── PersistenceConfig.java         (class · Spring @Configuration)
```

### 9.2 Decisiones de Tipado Relevantes

- **Entities (**``**, **``**)**

  - Clases normales (no records) para permitir invariantes, mutabilidad controlada y evolución.

- **UseCases**

  - Clases concretas, *stateless*, con dependencias explícitas vía constructor.
  - No interfaces en Fase 1 (evitamos abstracción innecesaria).

- **Ports (**``**, **``**)**

  - Interfaces puras definidas por la capa de aplicación.

- **Adapters**

  - Clases concretas dependientes de framework (Spring, JPA, Security).

- **DTOs**

  - Solo en capa `web`, preferiblemente como `record`, cuando exista intercambio HTTP.

### 9.3 Reglas Arquitectónicas de Cumplimiento

- El dominio y la aplicación **no dependen de Spring**.
- Los controllers **no contienen lógica de negocio**.
- Todo acceso a infraestructura se realiza a través de ports.
- Si una abstracción no aporta testabilidad o desacoplo real, se elimina.

---

*Este documento es un marco de decisión. Cualquier cambio arquitectónico o técnico debe poder justificarse en relación con este manifiesto.*

