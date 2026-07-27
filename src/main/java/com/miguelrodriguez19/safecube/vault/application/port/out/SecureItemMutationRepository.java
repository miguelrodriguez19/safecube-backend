package com.miguelrodriguez19.safecube.vault.application.port.out;

import java.time.Instant;
import java.util.UUID;

/** Persistence port for accepted idempotent vault mutations. */
public interface SecureItemMutationRepository {

  void lock(UUID accountId, UUID mutationId);

  StoredMutation findByAccountAndMutationId(UUID accountId, UUID mutationId);

  void save(StoredMutation mutation);

  record StoredMutation(
      UUID accountId,
      UUID mutationId,
      UUID itemId,
      String operation,
      String requestHash,
      long payloadVersion,
      long itemRevision,
      long changeSequence,
      Instant occurredAt,
      Instant deletedAt) {}
}
