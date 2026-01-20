package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial;

import java.time.Instant;
import java.util.UUID;

/**
 * VaultKeyMaterialResponse
 *
 * <p>HTTP response DTO for vault key material.
 */
public record VaultKeyMaterialResponse(
    UUID accountId,
    byte[] kekEncMaster,
    byte[] kekEncRecovery,
    String kdfAlgorithm,
    byte[] kdfSalt,
    int kdfMemoryKib,
    int kdfIterations,
    int kdfParallelism,
    int kdfOutputLen,
    String cryptoVersion,
    Instant createdAt,
    Instant updatedAt) {}
