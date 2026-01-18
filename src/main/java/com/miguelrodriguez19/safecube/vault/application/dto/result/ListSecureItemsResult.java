package com.miguelrodriguez19.safecube.vault.application.dto.result;

import com.miguelrodriguez19.safecube.vault.domain.model.ItemType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Result of listing SecureItems for an account.
 */
public record ListSecureItemsResult(
        List<Item> items
) {

    public record Item(
            UUID itemId,
            ItemType itemType,
            int schemaVersion,
            String displayHint,
            Instant updatedAt,
            Instant deletedAt
    ) {
    }
}
