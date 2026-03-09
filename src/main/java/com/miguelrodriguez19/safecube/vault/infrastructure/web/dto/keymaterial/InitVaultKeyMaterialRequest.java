package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(
    description =
        "Request to initialize vault key material. "
            + "All byte arrays are opaque encrypted/wrapped values produced by the client. "
            + "The backend never derives or decrypts keys (zero-knowledge).")
public record InitVaultKeyMaterialRequest(
    @Schema(description = "Master-wrapped KEK (base64 encoded bytes).", format = "byte") @NotNull byte[] kekEncMaster,
    @Schema(description = "Recovery-wrapped KEK (opaque bytes).", format = "byte") @NotNull byte[] kekEncRecovery,
    @Schema(description = "KDF algorithm identifier used by the client (e.g., argon2id).") @NotBlank String kdfAlgorithm,
    @Schema(description = "KDF salt (opaque bytes).", format = "byte") @NotNull byte[] kdfSalt,
    @Schema(description = "KDF memory cost in KiB.") @Positive int kdfMemoryKib,
    @Schema(description = "KDF iterations / time cost.") @Positive int kdfIterations,
    @Schema(description = "KDF parallelism.") @Positive int kdfParallelism,
    @Schema(description = "KDF output length in bytes.") @Positive int kdfOutputLen,
    @Schema(description = "Client crypto implementation/version identifier.") @NotBlank String cryptoVersion) {}
