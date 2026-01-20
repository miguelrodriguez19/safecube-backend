package unit.com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.shared.result.Void;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.UpdateMasterWrappedKekCommand;
import com.miguelrodriguez19.safecube.vault.application.error.VaultKeyMaterialError;
import com.miguelrodriguez19.safecube.vault.application.port.out.VaultKeyMaterialRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.UpdateMasterWrappedKekUseCase;
import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class UpdateMasterWrappedKekUseCaseTest {

  @Mock private VaultKeyMaterialRepository repository;

  @InjectMocks private UpdateMasterWrappedKekUseCase target;

  @Test
  void shouldUpdateMasterWrappedKekSuccessfully() {
    final var now = Instant.now();
    final var accountId = UUID.randomUUID();

    final var keyMaterial =
        VaultKeyMaterial.create(
            accountId,
            new byte[] {1},
            new byte[] {2},
            "ARGON2ID",
            new byte[] {3},
            65536,
            3,
            1,
            32,
            "v1",
            now.minusSeconds(60));

    final var command = new UpdateMasterWrappedKekCommand(accountId, new byte[] {9, 9, 9}, now);

    when(repository.findByAccountId(accountId)).thenReturn(Optional.of(keyMaterial));

    final var result = target.execute(command);

    verify(repository).update(keyMaterial);

    assertThat(result).isInstanceOf(Result.Success.class);
    assertThat(result.success().get()).isEqualTo(Void.INSTANCE);
    assertThat(keyMaterial.getKekEncMaster()).isEqualTo(command.newKekEncMaster());
    assertThat(keyMaterial.getUpdatedAt()).isEqualTo(command.updatedAt());
  }

  @Test
  void shouldFail_whenVaultIsNotInitialized() {
    final var command =
        new UpdateMasterWrappedKekCommand(UUID.randomUUID(), new byte[] {9, 9, 9}, Instant.now());

    when(repository.findByAccountId(command.accountId())).thenReturn(Optional.empty());

    final var result = target.execute(command);

    verify(repository, never()).update(any());

    assertThat(result).isInstanceOf(Result.Failure.class);
    assertThat(result.error()).containsInstanceOf(VaultKeyMaterialError.VaultNotInitialized.class);
  }
}
