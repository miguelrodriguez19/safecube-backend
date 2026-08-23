package unit.com.miguelrodriguez19.safecube.vault.domain.model.keymaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.miguelrodriguez19.safecube.vault.domain.exception.InvalidVaultKeyMaterialException;
import com.miguelrodriguez19.safecube.vault.domain.exception.InvalidWrappedKekException;
import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import unit.annotation.UnitTest;

@UnitTest
class VaultKeyMaterialTest {

  @Test
  void shouldCreateVaultKeyMaterial() {
    final var now = Instant.now();

    final var accountId = UUID.randomUUID();
    final var kekEncMaster = new byte[] {1};
    final var kekEncRecovery = new byte[] {1};
    final var kdfAlgorithm = "ARGON2ID";
    final var kdfSalt = new byte[] {1};
    final var kdfMemoryKib = 65536;
    final var kdfIterations = 3;
    final var kdfParallelism = 1;
    final var kdfOutputLen = 32;
    final var cryptoVersion = "v1";

    final var result =
        VaultKeyMaterial.create(
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
            now);

    assertThat(result)
        .isNotNull()
        .extracting(
            VaultKeyMaterial::getAccountId,
            VaultKeyMaterial::getKekEncMaster,
            VaultKeyMaterial::getKekEncRecovery,
            VaultKeyMaterial::getKdfAlgorithm,
            VaultKeyMaterial::getKdfSalt,
            VaultKeyMaterial::getKdfMemoryKib,
            VaultKeyMaterial::getKdfIterations,
            VaultKeyMaterial::getKdfParallelism,
            VaultKeyMaterial::getKdfOutputLen,
            VaultKeyMaterial::getCryptoVersion,
            VaultKeyMaterial::getCreatedAt,
            VaultKeyMaterial::getUpdatedAt,
            VaultKeyMaterial::getMasterKeyRevision)
        .containsExactly(
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
            now,
            now,
            1L);
  }

  @Test
  void shouldRejectCreation_whenKekEncMasterIsEmpty() {
    final var now = Instant.now();

    assertThatThrownBy(
            () ->
                VaultKeyMaterial.create(
                    UUID.randomUUID(),
                    new byte[0],
                    new byte[] {1},
                    "ARGON2ID",
                    new byte[] {1},
                    65536,
                    3,
                    1,
                    32,
                    "v1",
                    now))
        .isInstanceOf(InvalidWrappedKekException.class)
        .hasMessageContaining("kekEncMaster");
  }

  @Test
  void shouldRejectRestore_whenMasterKeyRevisionIsZero() {
    final var now = Instant.now();

    assertThatThrownBy(
            () ->
                VaultKeyMaterial.restore(
                    UUID.randomUUID(),
                    new byte[] {1},
                    new byte[] {1},
                    "ARGON2ID",
                    new byte[] {1},
                    65536,
                    3,
                    1,
                    32,
                    "v1",
                    now,
                    now,
                    0L))
        .isInstanceOf(InvalidVaultKeyMaterialException.class)
        .hasMessageContaining("masterKeyRevision");
  }

  private static Stream<Arguments> invalidKdfParametersCombinations() {
    return Stream.of(
        Arguments.of(0, 3, 1, 32),
        Arguments.of(3, 0, 1, 32),
        Arguments.of(3, 1, 0, 32),
        Arguments.of(32, 3, 1, 0));
  }

  @ParameterizedTest
  @MethodSource("invalidKdfParametersCombinations")
  void shouldRejectCreation_whenAnyKdfParameterIsInvalid(
      final int kdfMemoryKib,
      final int kdfIterations,
      final int kdfParallelism,
      final int kdfOutputLen) {
    final var now = Instant.now();

    assertThatThrownBy(
            () ->
                VaultKeyMaterial.create(
                    UUID.randomUUID(),
                    new byte[] {1},
                    new byte[] {1},
                    "ARGON2ID",
                    new byte[] {1},
                    kdfMemoryKib,
                    kdfIterations,
                    kdfParallelism,
                    kdfOutputLen,
                    "v1",
                    now))
        .isInstanceOf(InvalidVaultKeyMaterialException.class)
        .hasMessageContaining("KDF");
  }

  @Test
  void shouldRejectMasterRotation_whenNewWrappedKekIsEmpty() {
    final var now = Instant.now();

    final var material =
        VaultKeyMaterial.create(
            UUID.randomUUID(),
            new byte[] {1},
            new byte[] {1},
            "ARGON2ID",
            new byte[] {1},
            65536,
            3,
            1,
            32,
            "v1",
            now);

    assertThatThrownBy(() -> material.rotateMasterWrappedKek(new byte[0], now.plusSeconds(10)))
        .isInstanceOf(InvalidWrappedKekException.class)
        .hasMessageContaining("kekEncMaster");
  }
}
