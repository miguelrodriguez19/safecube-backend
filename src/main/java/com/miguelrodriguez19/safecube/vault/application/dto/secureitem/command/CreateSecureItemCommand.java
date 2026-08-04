package com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import java.time.Instant;
import java.util.UUID;

/** Command to create a new SecureItem. */
public record CreateSecureItemCommand(
    UUID accountId,
    ItemTypeDto itemTypeDto,
    int schemaVersion,
    String displayHint,
    byte[] payload,
    long payloadVersion,
    UUID mutationId,
    Instant createdAt) {}
