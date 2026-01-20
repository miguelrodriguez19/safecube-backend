package com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result;

import java.time.Instant;
import java.util.UUID;

/** Result of a successful SecureItem update. */
public record UpdateSecureItemResult(UUID itemId, long payloadVersion, Instant updatedAt) {}
