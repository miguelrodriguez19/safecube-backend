package com.miguelrodriguez19.safecube.vault.application.port.out;

import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import java.util.Optional;
import java.util.UUID;

/**
 * VaultKeyMaterialRepository
 *
 * <p>Outbound port responsible for persisting and retrieving vault key material.
 */
public interface VaultKeyMaterialRepository {

  Optional<VaultKeyMaterial> findByAccountId(final UUID accountId);

  void save(final VaultKeyMaterial keyMaterial);

  void update(final VaultKeyMaterial keyMaterial);
}
