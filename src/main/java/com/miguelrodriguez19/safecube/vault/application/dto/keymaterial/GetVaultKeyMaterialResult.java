package com.miguelrodriguez19.safecube.vault.application.dto.keymaterial;

import java.time.Instant;
import java.util.UUID;

public record GetVaultKeyMaterialResult(
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
