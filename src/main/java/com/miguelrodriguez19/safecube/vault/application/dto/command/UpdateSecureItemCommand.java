package com.miguelrodriguez19.safecube.vault.application.dto.command;

import com.miguelrodriguez19.safecube.vault.application.dto.ItemTypeDto;
import java.time.Instant;
import java.util.UUID;

/** Command to update an existing SecureItem. */
public record UpdateSecureItemCommand(
    UUID accountId,
    UUID itemId,
    ItemTypeDto itemTypeDto,
    int schemaVersion,
    String displayHint,
    byte[] payload,
    Instant updatedAt) {}
