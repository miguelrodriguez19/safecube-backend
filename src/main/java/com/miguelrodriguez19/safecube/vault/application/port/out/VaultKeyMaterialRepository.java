package com.miguelrodriguez19.safecube.vault.application.port.out;

import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * VaultKeyMaterialRepository
 *
 * <p>Outbound port responsible for persisting and retrieving vault key material.
 */
public interface VaultKeyMaterialRepository {

  Optional<VaultKeyMaterial> findByAccountId(final UUID accountId);

  boolean existsByAccountId(final UUID accountId);

  void save(final VaultKeyMaterial keyMaterial);

  void update(final VaultKeyMaterial keyMaterial);

  int updateMasterWrappedKekIfRevisionMatches(
      final UUID accountId,
      final byte[] newKekEncMaster,
      final long expectedMasterKeyRevision,
      final Instant updatedAt);
}
