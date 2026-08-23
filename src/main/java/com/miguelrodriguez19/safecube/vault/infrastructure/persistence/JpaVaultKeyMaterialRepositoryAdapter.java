package com.miguelrodriguez19.safecube.vault.infrastructure.persistence;

import com.miguelrodriguez19.safecube.vault.application.port.out.VaultKeyMaterialRepository;
import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.VaultKeyMaterialJpaRepository;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.mapper.VaultKeyMaterialMapper;
import java.time.Instant;
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
  public boolean existsByAccountId(final UUID accountId) {
    return jpaRepository.existsById(accountId);
  }

  @Override
  public void save(final VaultKeyMaterial keyMaterial) {
    jpaRepository.save(mapper.toEntity(keyMaterial));
  }

  @Override
  public void update(final VaultKeyMaterial keyMaterial) {
    jpaRepository.save(mapper.toEntity(keyMaterial));
  }

  @Override
  public int updateMasterWrappedKekIfRevisionMatches(
      final UUID accountId,
      final byte[] newKekEncMaster,
      final long expectedMasterKeyRevision,
      final Instant updatedAt) {
    return jpaRepository.updateMasterWrappedKekIfRevisionMatches(
        accountId, newKekEncMaster, expectedMasterKeyRevision, updatedAt);
  }
}
