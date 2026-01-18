package com.miguelrodriguez19.safecube.vault.application.dto.command;

import java.time.Instant;
import java.util.UUID;

/** Command to soft-delete a SecureItem. */
public record DeleteSecureItemCommand(UUID accountId, UUID itemId, Instant deletedAt) {}
