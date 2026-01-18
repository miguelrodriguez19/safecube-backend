package com.miguelrodriguez19.safecube.vault.application.port.out;

import com.miguelrodriguez19.safecube.vault.domain.model.SecureItem;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Persistence port for SecureItem entities.
 *
 * <p>This interface defines the required persistence operations needed by vault application use
 * cases.
 */
public interface SecureItemRepository {

  void save(final SecureItem secureItem);

  SecureItem findByIdAndAccount(final UUID itemId, final UUID accountId);

  List<SecureItem> listByAccount(
      final UUID accountId, final Instant since, final boolean includeDeleted);

  void update(final SecureItem secureItem);

  void softDelete(final UUID itemId, final UUID accountId, final Instant deletedAt);
}
