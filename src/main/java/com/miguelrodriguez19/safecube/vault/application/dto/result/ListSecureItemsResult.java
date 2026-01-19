package com.miguelrodriguez19.safecube.vault.application.dto.result;

import com.miguelrodriguez19.safecube.vault.application.dto.ItemTypeDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Result of listing SecureItems (without payload) for an account. */
public record ListSecureItemsResult(List<Item> items) {

  public record Item(
      UUID itemId,
      ItemTypeDto itemType,
      int schemaVersion,
      String displayHint,
      long payloadVersion,
      Instant updatedAt,
      Instant deletedAt) {}
}
