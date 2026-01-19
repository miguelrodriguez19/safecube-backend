package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * UpdateSecureItemRequest
 *
 * <p>HTTP request payload for updating an existing SecureItem.
 */
public record UpdateSecureItemRequest(
    @NotNull String itemType,
    @NotNull Integer schemaVersion,
    @NotBlank @Size(max = 255) String displayHint,
    @NotNull byte[] payload,
    @NotNull Instant updatedAt) {}
