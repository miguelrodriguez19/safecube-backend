package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * VaultKeyMaterialResponse
 *
 * <p>HTTP response DTO for vault key material.
 */
public record VaultKeyMaterialResponse(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID accountId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, format = "byte") byte[] kekEncMaster,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, format = "byte") byte[] kekEncRecovery,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String kdfAlgorithm,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, format = "byte") byte[] kdfSalt,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int kdfMemoryKib,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int kdfIterations,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int kdfParallelism,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int kdfOutputLen,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String cryptoVersion,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt) {}
