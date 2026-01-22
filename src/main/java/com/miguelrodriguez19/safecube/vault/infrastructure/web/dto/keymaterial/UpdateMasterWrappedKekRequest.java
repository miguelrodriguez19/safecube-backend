package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
    description =
        "Request to update the master-wrapped KEK after a passphrase change. "
            + "Opaque client-produced bytes; the backend does not decrypt or derive keys.")
public record UpdateMasterWrappedKekRequest(
    @Schema(description = "New master-wrapped KEK (opaque bytes).", format = "binary") @NotNull byte[] newKekEncMaster) {}
