package com.miguelrodriguez19.safecube.vault.application.dto.command;

import com.miguelrodriguez19.safecube.vault.domain.model.ItemType;
import java.time.Instant;
import java.util.UUID;

/** Command to update an existing SecureItem. */
public record UpdateSecureItemCommand(
    UUID accountId,
    UUID itemId,
    ItemType itemType,
    int schemaVersion,
    String displayHint,
    byte[] payload,
    Instant updatedAt) {}
