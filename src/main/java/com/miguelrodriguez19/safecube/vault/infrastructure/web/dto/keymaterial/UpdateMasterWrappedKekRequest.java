package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record UpdateMasterWrappedKekRequest(
    @NotNull UUID accountId, @NotNull byte[] newKekEncMaster, @NotNull Instant updatedAt) {}
