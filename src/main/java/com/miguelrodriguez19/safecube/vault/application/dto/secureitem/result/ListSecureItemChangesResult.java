package com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Ordered page of complete item snapshots for synchronization. */
public record ListSecureItemChangesResult(List<Item> items, long nextCursor, boolean hasMore) {

  public record Item(
      UUID itemId,
      ItemTypeDto itemType,
      int schemaVersion,
      String displayHint,
      byte[] payload,
      long payloadVersion,
      long itemRevision,
      long changeSequence,
      Instant updatedAt,
      Instant deletedAt) {}
}
