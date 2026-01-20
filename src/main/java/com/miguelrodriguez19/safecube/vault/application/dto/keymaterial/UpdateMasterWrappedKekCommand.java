package com.miguelrodriguez19.safecube.vault.application.dto.keymaterial;

import java.time.Instant;
import java.util.UUID;

public record UpdateMasterWrappedKekCommand(
    UUID accountId, byte[] newKekEncMaster, Instant updatedAt) {}
