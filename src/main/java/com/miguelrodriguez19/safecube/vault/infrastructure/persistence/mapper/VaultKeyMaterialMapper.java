package com.miguelrodriguez19.safecube.vault.infrastructure.persistence.mapper;

import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.VaultKeyMaterialJpaEntity;
import org.springframework.stereotype.Component;

/** VaultKeyMaterialMapper */
@Component
public class VaultKeyMaterialMapper {

  public VaultKeyMaterialJpaEntity toEntity(final VaultKeyMaterial domain) {
    return new VaultKeyMaterialJpaEntity(
        domain.getAccountId(),
        domain.getKekEncMaster(),
        domain.getKekEncRecovery(),
        domain.getKdfAlgorithm(),
        domain.getKdfSalt(),
        domain.getKdfMemoryKib(),
        domain.getKdfIterations(),
        domain.getKdfParallelism(),
        domain.getKdfOutputLen(),
        domain.getCryptoVersion(),
        domain.getCreatedAt(),
        domain.getUpdatedAt(),
        domain.getMasterKeyRevision());
  }

  public VaultKeyMaterial toDomain(final VaultKeyMaterialJpaEntity entity) {
    return VaultKeyMaterial.restore(
        entity.getAccountId(),
        entity.getKekEncMaster(),
        entity.getKekEncRecovery(),
        entity.getKdfAlgorithm(),
        entity.getKdfSalt(),
        entity.getKdfMemoryKib(),
        entity.getKdfIterations(),
        entity.getKdfParallelism(),
        entity.getKdfOutputLen(),
        entity.getCryptoVersion(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getMasterKeyRevision());
  }
}
