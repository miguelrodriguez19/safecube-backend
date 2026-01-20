package com.miguelrodriguez19.safecube.vault.infrastructure.persistence;

import com.miguelrodriguez19.safecube.vault.application.port.out.VaultKeyMaterialRepository;
import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.VaultKeyMaterialJpaRepository;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.mapper.VaultKeyMaterialMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** JpaVaultKeyMaterialRepositoryAdapter */
@Repository
@RequiredArgsConstructor
public class JpaVaultKeyMaterialRepositoryAdapter implements VaultKeyMaterialRepository {

  private final VaultKeyMaterialJpaRepository jpaRepository;
  private final VaultKeyMaterialMapper mapper;

  @Override
  public Optional<VaultKeyMaterial> findByAccountId(final UUID accountId) {
    return jpaRepository.findById(accountId).map(mapper::toDomain);
  }

  @Override
  public void save(final VaultKeyMaterial keyMaterial) {
    jpaRepository.save(mapper.toEntity(keyMaterial));
  }

  @Override
  public void update(final VaultKeyMaterial keyMaterial) {
    jpaRepository.save(mapper.toEntity(keyMaterial));
  }
}
