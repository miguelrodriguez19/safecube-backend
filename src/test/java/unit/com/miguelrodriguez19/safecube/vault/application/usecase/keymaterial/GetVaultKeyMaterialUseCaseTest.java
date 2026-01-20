package unit.com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.GetVaultKeyMaterialQuery;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.GetVaultKeyMaterialResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultKeyMaterialError;
import com.miguelrodriguez19.safecube.vault.application.port.out.VaultKeyMaterialRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.GetVaultKeyMaterialUseCase;
import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class GetVaultKeyMaterialUseCaseTest {

  @Mock private VaultKeyMaterialRepository repository;

  @InjectMocks private GetVaultKeyMaterialUseCase target;

  @Test
  void shouldGetVaultKeyMaterial_whenVaultWasInitialized() {
    final var query = new GetVaultKeyMaterialQuery(UUID.randomUUID());

    final var vaultKeyMaterial = getVaultKeyMaterial();
    when(repository.findByAccountId(query.accountId())).thenReturn(Optional.of(vaultKeyMaterial));

    final var result = target.execute(query);

    assertThat(result).isInstanceOf(Result.Success.class);
    assertThat(result.success().get())
        .extracting(
            GetVaultKeyMaterialResult::accountId,
            GetVaultKeyMaterialResult::kekEncMaster,
            GetVaultKeyMaterialResult::kekEncRecovery,
            GetVaultKeyMaterialResult::kdfAlgorithm,
            GetVaultKeyMaterialResult::kdfSalt,
            GetVaultKeyMaterialResult::kdfMemoryKib,
            GetVaultKeyMaterialResult::kdfIterations,
            GetVaultKeyMaterialResult::kdfParallelism,
            GetVaultKeyMaterialResult::kdfOutputLen,
            GetVaultKeyMaterialResult::cryptoVersion,
            GetVaultKeyMaterialResult::createdAt,
            GetVaultKeyMaterialResult::updatedAt)
        .containsExactly(
            vaultKeyMaterial.getAccountId(),
            vaultKeyMaterial.getKekEncMaster(),
            vaultKeyMaterial.getKekEncRecovery(),
            vaultKeyMaterial.getKdfAlgorithm(),
            vaultKeyMaterial.getKdfSalt(),
            vaultKeyMaterial.getKdfMemoryKib(),
            vaultKeyMaterial.getKdfIterations(),
            vaultKeyMaterial.getKdfParallelism(),
            vaultKeyMaterial.getKdfOutputLen(),
            vaultKeyMaterial.getCryptoVersion(),
            vaultKeyMaterial.getCreatedAt(),
            vaultKeyMaterial.getUpdatedAt());
  }

  @Test
  void shouldFail_whenVaultIsNotInitialized() {
    final var query = new GetVaultKeyMaterialQuery(UUID.randomUUID());

    when(repository.findByAccountId(query.accountId())).thenReturn(Optional.empty());

    final var result = target.execute(query);

    assertThat(result).isInstanceOf(Result.Failure.class);
    assertThat(result.error()).containsInstanceOf(VaultKeyMaterialError.VaultNotInitialized.class);
  }

  private VaultKeyMaterial getVaultKeyMaterial() {
    return VaultKeyMaterial.create(
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
        Instant.now());
  }
}
