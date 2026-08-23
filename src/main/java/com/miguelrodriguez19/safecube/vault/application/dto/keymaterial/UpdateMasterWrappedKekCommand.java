package com.miguelrodriguez19.safecube.vault.application.dto.keymaterial;

import java.time.Instant;
import java.util.UUID;

public record UpdateMasterWrappedKekCommand(
    UUID accountId, byte[] newKekEncMaster, long expectedMasterKeyRevision, Instant updatedAt) {

  public UpdateMasterWrappedKekCommand(
      final UUID accountId, final byte[] newKekEncMaster, final Instant updatedAt) {
    this(accountId, newKekEncMaster, 1L, updatedAt);
  }
}
