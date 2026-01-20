package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.request;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * DeleteSecureItemRequest
 *
 * <p>HTTP request payload for soft-deleting a SecureItem.
 */
public record DeleteSecureItemRequest(@NotNull Instant deletedAt) {}
