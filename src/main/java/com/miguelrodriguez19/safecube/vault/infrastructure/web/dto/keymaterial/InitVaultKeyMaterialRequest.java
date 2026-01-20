package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InitVaultKeyMaterialRequest(
    @NotNull byte[] kekEncMaster,
    @NotNull byte[] kekEncRecovery,
    @NotBlank String kdfAlgorithm,
    @NotNull byte[] kdfSalt,
    @Positive int kdfMemoryKib,
    @Positive int kdfIterations,
    @Positive int kdfParallelism,
    @Positive int kdfOutputLen,
    @NotBlank String cryptoVersion) {}
