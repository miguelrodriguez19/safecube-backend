# SafeCube – Ubiquitous Language Glossary

Este documento define el **lenguaje ubicuo** de SafeCube Backend.
Los términos aquí recogidos deben usarse de forma **consistente** en documentación, código, casos de uso y
conversaciones
técnicas.

El glosario se organiza en **grupos pequeños y localizados** por área funcional.

---

## Core / Principios

**Zero-Knowledge**
Propiedad del sistema por la cual el backend nunca puede acceder ni interpretar datos sensibles en claro; solo almacena
blobs cifrados opacos. El backend no posee las llaves necesarias para descifrar el vault.

**Client-Centric Security**
Enfoque en el que todas las operaciones criptográficas críticas (derivación de claves, cifrado, descifrado, rotación de
claves) se ejecutan exclusivamente en el cliente.

**Opaque Payload**
Contenido cifrado extremo a extremo cuyo significado y estructura son desconocidos para el backend.

---

## Identidad y Autenticación (auth)

**Account**
Identidad autenticable dentro del sistema. Representa credenciales y estado de acceso, pero no información de perfil ni
datos del vault.

**AuthAccount**
Entidad de dominio que modela una cuenta autenticable gestionada por el slice `auth`, incluyendo credenciales y estado
de
cuenta.

**Account State**
Estado lógico de una cuenta autenticable que condiciona el acceso al sistema y al vault.

Estados definidos en SafeCube:

* `ACTIVE`
* `DISABLED`

*(Otros estados potenciales quedan fuera de alcance en v1.)*

**Authentication**
Proceso de verificación de credenciales que confirma la identidad de una cuenta.

**Session**
Estado lógico que representa una autenticación válida en el tiempo, implementada mediante tokens.

**Access Token**
Token de vida corta utilizado para autorizar requests autenticadas.

**Refresh Token**
Token opaco de vida larga utilizado para renovar access tokens sin reautenticación.

---

## Perfil de Usuario (user)

**User Profile**
Representación del perfil de usuario orientado a experiencia de uso, separada explícitamente de autenticación y
credenciales.

**UserProfile**
Entidad que contiene información no sensible asociada a una cuenta autenticable.

**Logical Deletion (User)**
Modelo de baja en el que el perfil se marca como eliminado sin borrado físico inmediato.

---

## Vault y Persistencia Segura (vault)

**Vault**
Conjunto lógico de información cifrada perteneciente a una cuenta `ACTIVE`.
El vault solo puede inicializarse y utilizarse cuando la cuenta está activa.

**Vault Initialization**
Proceso mediante el cual el cliente genera y envía al backend el material criptográfico inicial del vault
(`VaultKeyMaterial`).

**VaultKeyMaterial**
Entidad de dominio que representa el **material criptográfico envuelto** necesario para desbloquear el vault en cliente.
El backend nunca puede derivar ni descifrar este material.

**SecureItem**
Unidad mínima de almacenamiento, cifrado, sincronización y versionado dentro del vault.

**Item Payload**
Blob cifrado que contiene la semántica completa del secreto. Es completamente opaco para el backend.

**Metadata (Vault)**
Información mínima, no sensible y en claro asociada a un `SecureItem` para soporte de UX, filtrado y sincronización.

**Item Type**
Clasificación funcional cerrada usada únicamente para organización visual y renderizado en cliente.

**Schema Version**
Versión del esquema lógico del contenido cifrado, controlada exclusivamente por el cliente.

**Payload Version**
Contador técnico incremental gestionado por el backend para control de concurrencia y sincronización.

**Soft Delete (Item)**
Marcado lógico de un `SecureItem` como eliminado, preservando su trazabilidad para sincronización entre dispositivos.

---

## Organización (Vault – UX)

**Group**
Entidad de organización visual que agrupa `SecureItems` sin afectar al cifrado ni a la semántica del payload.

**Item–Group Relation**
Relación muchos-a-muchos entre `SecureItems` y `Groups`, independiente del contenido cifrado.

---

## Criptografía (Vault Crypto)

> ⚠️ Todos los conceptos criptográficos se aplican **exclusivamente en cliente**.
> El backend solo almacena material **envuelto**.

**Passphrase / Master Password**
Secreto conocido solo por el usuario. Nunca se envía al backend. Se utiliza para derivar claves criptográficas en
cliente.

**MASTER_KEY**
Clave derivada localmente en el cliente a partir de la passphrase mediante un KDF fuerte (ej. Argon2id).
Nunca se persiste ni se transmite.

**KEK (Key Encryption Key)**
Clave simétrica generada en cliente que protege el vault.
Se envuelve usando:

* `MASTER_KEY` → `kekEncMaster`
* `RECOVERY_KEY` → `kekEncRecovery`

La KEK **nunca se almacena en claro**.

**RECOVERY_KEY**
Clave secreta generada en cliente que permite recuperar la KEK en caso de pérdida de la passphrase.
Nunca se envía ni se almacena en el backend.

**Vault Key (VK)**
Término **conceptual** que representa la clave raíz efectiva del vault.
En SafeCube v1, este rol lo cumple la **KEK**.
El término VK se mantiene únicamente a nivel conceptual y **no se usa en código**.

**DEK (Data Encryption Key)**
Clave simétrica única por `SecureItem`, generada en cliente y utilizada para cifrar su payload.

**Envelope Encryption**
Patrón criptográfico basado en envoltura jerárquica de claves:

```
MASTER_KEY / RECOVERY_KEY
        ↓
       KEK
        ↓
       DEK
        ↓
     Payload
```

**AEAD (Authenticated Encryption with Associated Data)**
Esquema de cifrado autenticado que garantiza confidencialidad e integridad del payload y su contexto.

---

## Arquitectura y Aplicación

**Slice**
Unidad vertical de funcionalidad (`auth`, `user`, `vault`) con responsabilidades claramente delimitadas.

**Use Case**
Representación explícita de una intención de aplicación, independiente de HTTP y frameworks.

**Port**
Contrato definido por la capa de aplicación para acceder a infraestructura externa.

**Adapter**
Implementación técnica concreta de un port (persistencia, web, seguridad).

**Monolito Modular**
Arquitectura donde el sistema se despliega como una unidad pero se organiza internamente por módulos bien aislados.

---

Este glosario es **vivo** y debe evolucionar junto al modelo y los casos de uso.
Cualquier término nuevo relevante debe añadirse aquí **antes** de introducirse en código o documentación.
