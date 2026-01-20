package com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** VaultKeyMaterialJpaRepository */
public interface VaultKeyMaterialJpaRepository
    extends JpaRepository<VaultKeyMaterialJpaEntity, UUID> {}
