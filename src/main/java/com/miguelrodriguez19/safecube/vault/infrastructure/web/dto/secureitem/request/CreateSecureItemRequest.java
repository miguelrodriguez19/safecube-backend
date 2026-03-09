package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.request;

import com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.annotation.ValidItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * CreateSecureItemRequest
 *
 * <p>HTTP request payload for creating a new SecureItem.
 */
@Schema(
    description =
        "Request to create a vault item. "
            + "The payload is opaque encrypted data produced by the client; the backend does not inspect it.")
public record CreateSecureItemRequest(
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
    @Schema(description = "Opaque encrypted payload bytes.", format = "byte") @NotNull byte[] payload) {}
