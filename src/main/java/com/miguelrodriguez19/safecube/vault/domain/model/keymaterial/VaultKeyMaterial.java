package com.miguelrodriguez19.safecube.vault.domain.model.keymaterial;

import com.miguelrodriguez19.safecube.vault.domain.exception.InvalidVaultKeyMaterialException;
import com.miguelrodriguez19.safecube.vault.domain.exception.InvalidWrappedKekException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * VaultKeyMaterial
 *
 * <p>Domain entity representing opaque cryptographic material required to unlock a vault. The
 * backend never interprets or derives cryptographic keys.
 */
@Getter
@EqualsAndHashCode
@ToString
public class VaultKeyMaterial {

  private final UUID accountId;
  private byte[] kekEncMaster;
  private final byte[] kekEncRecovery;
  private final String kdfAlgorithm;
  private final byte[] kdfSalt;
  private final int kdfMemoryKib;
  private final int kdfIterations;
  private final int kdfParallelism;
  private final int kdfOutputLen;
  private final String cryptoVersion;
  private final Instant createdAt;
  private Instant updatedAt;
  private long masterKeyRevision;

  private VaultKeyMaterial(
      final UUID accountId,
      final byte[] kekEncMaster,
      final byte[] kekEncRecovery,
      final String kdfAlgorithm,
      final byte[] kdfSalt,
      final int kdfMemoryKib,
      final int kdfIterations,
      final int kdfParallelism,
      final int kdfOutputLen,
      final String cryptoVersion,
      final Instant createdAt,
      final Instant updatedAt,
      final long masterKeyRevision) {

    this.accountId = accountId;
    this.kekEncMaster = kekEncMaster;
    this.kekEncRecovery = kekEncRecovery;
    this.kdfAlgorithm = kdfAlgorithm;
    this.kdfSalt = kdfSalt;
    this.kdfMemoryKib = kdfMemoryKib;
    this.kdfIterations = kdfIterations;
    this.kdfParallelism = kdfParallelism;
    this.kdfOutputLen = kdfOutputLen;
    this.cryptoVersion = cryptoVersion;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.masterKeyRevision = masterKeyRevision;
  }

  public static VaultKeyMaterial create(
      @NotNull final UUID accountId,
      @NotNull final byte[] kekEncMaster,
      @NotNull final byte[] kekEncRecovery,
      @NotBlank final String kdfAlgorithm,
      @NotNull final byte[] kdfSalt,
      @Positive final int kdfMemoryKib,
      @Positive final int kdfIterations,
      @Positive final int kdfParallelism,
      @Positive final int kdfOutputLen,
      @NotBlank final String cryptoVersion,
      @NotNull final Instant createdAt) {

    validateKekEncMaster(kekEncMaster);

    validateKdf(kdfMemoryKib, kdfIterations, kdfParallelism, kdfOutputLen);

    return new VaultKeyMaterial(
        accountId,
        kekEncMaster,
        kekEncRecovery,
        kdfAlgorithm,
        kdfSalt,
        kdfMemoryKib,
        kdfIterations,
        kdfParallelism,
        kdfOutputLen,
        cryptoVersion,
        createdAt,
        createdAt,
        1L);
  }

  public static VaultKeyMaterial restore(
      @NotNull final UUID accountId,
      @NotNull final byte[] kekEncMaster,
      @NotNull final byte[] kekEncRecovery,
      @NotBlank final String kdfAlgorithm,
      @NotNull final byte[] kdfSalt,
      @Positive final int kdfMemoryKib,
      @Positive final int kdfIterations,
      @Positive final int kdfParallelism,
      @Positive final int kdfOutputLen,
      @NotBlank final String cryptoVersion,
      @NotNull final Instant createdAt,
      @NotNull final Instant updatedAt,
      @Positive final long masterKeyRevision) {

    if (masterKeyRevision <= 0) {
      throw new InvalidVaultKeyMaterialException("masterKeyRevision must be > 0");
    }

    return new VaultKeyMaterial(
        accountId,
        kekEncMaster,
        kekEncRecovery,
        kdfAlgorithm,
        kdfSalt,
        kdfMemoryKib,
        kdfIterations,
        kdfParallelism,
        kdfOutputLen,
        cryptoVersion,
        createdAt,
        updatedAt,
        masterKeyRevision);
  }

  public void rotateMasterWrappedKek(
      @NotNull final byte[] newKekEncMaster, @NotNull final Instant updatedAt) {

    validateKekEncMaster(newKekEncMaster);

    this.kekEncMaster = newKekEncMaster;
    this.updatedAt = updatedAt;
    this.masterKeyRevision = Math.incrementExact(this.masterKeyRevision);
  }

  public static void validateMasterWrappedKek(@NotNull final byte[] newKekEncMaster) {
    validateKekEncMaster(newKekEncMaster);
  }

  private static void validateKekEncMaster(byte[] newKekEncMaster) {
    if (newKekEncMaster.length == 0) {
      throw new InvalidWrappedKekException("kekEncMaster must not be empty");
    }
  }

  private static void validateKdf(
      int kdfMemoryKib, int kdfIterations, int kdfParallelism, int kdfOutputLen) {
    if (kdfMemoryKib <= 0 || kdfIterations <= 0 || kdfParallelism <= 0 || kdfOutputLen <= 0) {
      throw new InvalidVaultKeyMaterialException("All KDF numeric parameters must be > 0");
    }
  }
}
