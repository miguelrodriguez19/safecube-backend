package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.request;

import com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.annotation.ValidItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * UpdateSecureItemRequest
 *
 * <p>HTTP request payload for updating an existing SecureItem.
 */
@Schema(
    description =
        "Request to update a vault item. "
            + "The payload is opaque encrypted data produced by the client; the backend does not inspect it. "
            + "The updatedAt field is used for optimistic concurrency.")
public record UpdateSecureItemRequest(
    @Schema(description = "Item type identifier.", example = "NOTE") @NotNull @ValidItemType
        String itemType,
    @Schema(
            description =
                "Schema version for the client-side item format. "
                    + "Used by clients to handle migrations.")
        @NotNull Integer schemaVersion,
    @Schema(
            description = "Non-sensitive display hint (e.g., title) used for listing.",
            maxLength = 255)
        @NotBlank @Size(max = 255) String displayHint,
    @Schema(description = "Opaque encrypted payload bytes.", format = "binary") @NotNull byte[] payload,
    @Schema(
            description =
                "Client update timestamp used for sync/conflict handling. "
                    + "Must represent the timestamp of the payload being stored.",
            format = "date-time")
        @NotNull Instant updatedAt) {}
