package unit.com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.InitVaultKeyMaterialCommand;
import com.miguelrodriguez19.safecube.vault.application.error.VaultKeyMaterialError;
import com.miguelrodriguez19.safecube.vault.application.port.out.VaultKeyMaterialRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.InitVaultKeyMaterialUseCase;
import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class InitVaultKeyMaterialUseCaseTest {

  @Mock private VaultKeyMaterialRepository repository;

  @InjectMocks private InitVaultKeyMaterialUseCase target;

  @Test
  void shouldInitializeVaultKeyMaterialSuccessfully() {
    final var command = validInitCommand();

    when(repository.findByAccountId(command.accountId())).thenReturn(Optional.empty());

    final var result = target.execute(command);

    assertThat(result).isInstanceOf(Result.Success.class);
    verify(repository).save(any(VaultKeyMaterial.class));
  }

  @Test
  void shouldFail_whenVaultIsAlreadyInitialized() {
    final var command = validInitCommand();

    when(repository.findByAccountId(command.accountId()))
        .thenReturn(Optional.of(mock(VaultKeyMaterial.class)));

    final var result = target.execute(command);

    assertThat(result).isInstanceOf(Result.Failure.class);
    assertThat(result.error())
        .containsInstanceOf(VaultKeyMaterialError.VaultAlreadyInitialized.class);

    verify(repository, never()).save(any());
  }

  private InitVaultKeyMaterialCommand validInitCommand() {
    return new InitVaultKeyMaterialCommand(
        UUID.randomUUID(),
        new byte[] {1},
        new byte[] {2},
        "ARGON2ID",
        new byte[] {3},
        65536,
        3,
        1,
        32,
        "v1",
        Instant.now());
  }
}
