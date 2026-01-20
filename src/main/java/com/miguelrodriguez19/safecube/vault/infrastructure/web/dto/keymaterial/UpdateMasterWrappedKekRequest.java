package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial;

import jakarta.validation.constraints.NotNull;

public record UpdateMasterWrappedKekRequest(@NotNull byte[] newKekEncMaster) {}
