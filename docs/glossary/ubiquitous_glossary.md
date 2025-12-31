# SafeCube – Ubiquitous Language Glossary

Este documento define el **lenguaje ubicuo** de SafeCube Backend. Los términos aquí recogidos deben usarse de forma **consistente** en documentación, código, casos de uso y conversaciones técnicas.

El glosario se organiza en **grupos pequeños y localizados** por área funcional.

---

## Core / Principios

**Zero‑Knowledge**  
Propiedad del sistema por la cual el backend nunca puede acceder ni interpretar datos sensibles en claro; solo almacena blobs cifrados opacos.

**Client‑Centric Security**  
Enfoque en el que todas las decisiones criptográficas (cifrado, descifrado, rotación de claves) pertenecen exclusivamente al cliente.

**Opaque Payload**  
Contenido cifrado extremo a extremo cuyo significado es desconocido para el backend.

---

## Identidad y Autenticación (auth)

**Account**  
Identidad autenticable dentro del sistema. Representa credenciales y estado de acceso, no información de perfil.

**AuthAccount**  
Entidad de dominio que modela una cuenta autenticable gestionada por el slice `auth`.

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
Representación del perfil de usuario orientado a experiencia de uso, separado de autenticación y credenciales.

**UserProfile**  
Entidad que contiene información no sensible asociada a una cuenta autenticable.

**Logical Deletion (User)**  
Modelo de baja en el que el perfil se marca como eliminado sin borrado físico inmediato.

---

## Vault y Persistencia Segura (vault)

**Vault**  
Conjunto lógico de información cifrada perteneciente a una cuenta.

**SecureItem**  
Unidad mínima de almacenamiento, cifrado, sincronización y versionado dentro del vault.

**Item Payload**  
Blob cifrado que contiene la semántica completa del secreto.

**Metadata (Vault)**  
Información mínima, no sensible y en claro asociada a un SecureItem para soporte de UX y sincronización.

**Item Type**  
Clasificación funcional cerrada usada solo para renderizado y organización visual.

**Schema Version**  
Versión del esquema lógico del contenido cifrado, controlada por el cliente.

**Payload Version**  
Contador técnico utilizado para control de concurrencia y sincronización.

**Soft Delete (Item)**  
Marcado lógico de un SecureItem como eliminado, preservando su trazabilidad para sincronización.

---

## Organización (Vault – UX)

**Group**  
Entidad de organización visual que agrupa SecureItems sin afectar al cifrado.

**Item–Group Relation**  
Relación muchos‑a‑muchos entre SecureItems y Groups, independiente del payload cifrado.

---

## Criptografía (Vault Crypto)

**Master Password**  
Secreto conocido solo por el usuario, usado exclusivamente para derivar claves.

**KEK (Key Encryption Key)**  
Clave derivada de la Master Password utilizada para envolver la Vault Key.

**Vault Key (VK)**  
Clave simétrica raíz del vault usada para envolver claves de items.

**DEK (Data Encryption Key)**  
Clave simétrica única por SecureItem utilizada para cifrar su payload.

**Envelope Encryption**  
Patrón criptográfico basado en envoltura jerárquica de claves.

**AEAD (Authenticated Encryption with Associated Data)**  
Esquema de cifrado autenticado que garantiza confidencialidad e integridad.

---

## Arquitectura y Aplicación

**Slice**  
Unidad vertical de funcionalidad (auth, user, vault) con responsabilidades claramente delimitadas.

**Use Case**  
Representación explícita de una intención de aplicación, independiente de HTTP y frameworks.

**Port**  
Contrato definido por la capa de aplicación para acceder a infraestructura externa.

**Adapter**  
Implementación técnica concreta de un port (persistencia, web, seguridad).

**Monolito Modular**  
Arquitectura donde el sistema se despliega como una unidad pero se organiza internamente por módulos bien aislados.

---

Este glosario es **vivo** y debe evolucionar junto al modelo y los casos de uso. Cualquier término nuevo relevante debe añadirse aquí antes de introducirse en código o documentación.

