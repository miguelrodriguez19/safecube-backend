# SafeCube Backend

SafeCube Backend is the core backend service of the SafeCube ecosystem.  
It is responsible for user identity, authentication, and the persistence of end-to-end encrypted data.

The backend is designed as a **zero-knowledge**, **client-centric** system: it never accesses, interprets, or decrypts sensitive user data. All cryptographic decisions and secret semantics are handled exclusively on the client side.

---

## What is SafeCube Backend?

SafeCube Backend acts as a **trusted but blind intermediary** between clients and persistent storage.

It provides:
- User identity and authentication.
- Secure session management.
- Persistence and synchronization of **opaque encrypted payloads**.
- Strict data isolation between accounts.
- A stable, versioned HTTP API.

It does **not**:
- Encrypt or decrypt user secrets.
- Interpret the meaning of stored data.
- Apply business rules over encrypted content.
- Manage third-party credentials as a domain concept.

---

## Core Principles

### Zero-Knowledge by Design
The backend never has access to sensitive data in clear form.  
All secrets are stored as opaque encrypted blobs.

### Client-Centric Security
All cryptographic operations (encryption, decryption, key derivation, rotation) are performed on the client.  
The backend only handles transport, persistence, and access control.

### Simplicity Before Perfection
The project prioritizes:
- clarity over cleverness,
- explicit design over hidden magic,
- a functional MVP over premature overengineering.

---

## Architecture Overview

SafeCube Backend is implemented as a **modular monolith**, organized by vertical slices:

- `auth` — authentication and identity.
- `user` — user profile and non-sensitive user data.
- `vault` — persistence and synchronization of encrypted items.

Internally, each slice follows a **hexagonal-light** architecture:
- Domain and application logic are framework-agnostic.
- Spring is confined to infrastructure and adapters.
- Business rules are expressed through explicit use cases.

---

## Project Status

🚧 **Work in progress (MVP phase)**

- API contracts may change.
- Backward compatibility is not guaranteed yet.
- The focus is on correctness, clarity, and testability.

Versioning follows Semantic Versioning.  
The first stable API will be released as **1.0.0**.

---

## Documentation

Design documents and specifications live in the `/docs` directory:

- Architecture and guiding principles.
- Ubiquitous language glossary.
- Use case definitions per slice.

These documents are considered **authoritative** and precede implementation.

---

## Non-Goals (Phase 1)

The following are explicitly out of scope for the initial versions:

- OAuth / OpenID Connect.
- Social login providers.
- Advanced authorization (scopes, roles).
- Secret sharing between users.
- Complex search over encrypted data.

These may be evaluated in future iterations if justified.

---

## License

This project is licensed under the Apache License, Version 2.0.
