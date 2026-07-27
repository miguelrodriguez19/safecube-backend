package com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
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
    long payloadVersion,
    long expectedItemRevision,
    UUID mutationId,
    Instant updatedAt) {}
