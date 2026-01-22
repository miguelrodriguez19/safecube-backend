package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * DeleteSecureItemRequest
 *
 * <p>HTTP request payload for soft-deleting a SecureItem.
 */
@Schema(description = "Request to soft-delete a vault item.")
public record DeleteSecureItemRequest(
    @Schema(
            description =
                "Deletion timestamp set by the client. " + "Used for sync and conflict handling.",
            format = "date-time")
        @NotNull Instant deletedAt) {}
