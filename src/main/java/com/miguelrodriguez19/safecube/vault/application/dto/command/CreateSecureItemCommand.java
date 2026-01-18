package com.miguelrodriguez19.safecube.vault.application.dto.command;

import com.miguelrodriguez19.safecube.vault.domain.model.ItemType;
import java.time.Instant;
import java.util.UUID;

/**
 * Command to create a new SecureItem.
 */
public record CreateSecureItemCommand(
        UUID accountId,
        ItemType itemType,
        int schemaVersion,
        String displayHint,
        byte[] payload,
        Instant createdAt
) {
}
