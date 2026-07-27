package com.miguelrodriguez19.safecube.vault.application.port.out;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsFilter;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
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

  List<SecureItem> findFilteredByAccount(final UUID accountId, final ListSecureItemsFilter filter);

  List<SecureItem> findChanges(final UUID accountId, final long after, final int limit);

  long nextChangeSequence(final UUID accountId);

  boolean updateIfRevisionMatches(final SecureItem secureItem, final long expectedItemRevision);

  boolean softDeleteIfRevisionMatches(
      final UUID itemId,
      final UUID accountId,
      final long expectedItemRevision,
      final long nextItemRevision,
      final long changeSequence,
      final java.time.Instant deletedAt);
}
