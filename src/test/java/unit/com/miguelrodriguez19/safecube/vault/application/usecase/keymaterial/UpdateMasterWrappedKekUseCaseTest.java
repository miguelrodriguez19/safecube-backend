package unit.com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.UpdateMasterWrappedKekCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.UpdateMasterWrappedKekResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultKeyMaterialError;
import com.miguelrodriguez19.safecube.vault.application.port.out.VaultKeyMaterialRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.UpdateMasterWrappedKekUseCase;
import com.miguelrodriguez19.safecube.vault.domain.exception.InvalidWrappedKekException;
import java.time.Instant;
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

    final var command = new UpdateMasterWrappedKekCommand(accountId, new byte[] {9, 9, 9}, 1, now);

    when(repository.existsByAccountId(accountId)).thenReturn(true);
    when(repository.updateMasterWrappedKekIfRevisionMatches(
            accountId, command.newKekEncMaster(), command.expectedMasterKeyRevision(), now))
        .thenReturn(1);

    final var result = target.execute(command);

    verify(repository)
        .updateMasterWrappedKekIfRevisionMatches(
            accountId, command.newKekEncMaster(), command.expectedMasterKeyRevision(), now);

    assertThat(result).isInstanceOf(Result.Success.class);
    assertThat(result.success().get())
        .isEqualTo(new UpdateMasterWrappedKekResult(command.expectedMasterKeyRevision() + 1));
  }

  @Test
  void shouldFail_whenVaultIsNotInitialized() {
    final var command =
        new UpdateMasterWrappedKekCommand(
            UUID.randomUUID(), new byte[] {9, 9, 9}, 1, Instant.now());

    when(repository.existsByAccountId(command.accountId())).thenReturn(false);

    final var result = target.execute(command);

    verify(repository, never())
        .updateMasterWrappedKekIfRevisionMatches(any(), any(), anyLong(), any());

    assertThat(result).isInstanceOf(Result.Failure.class);
    assertThat(result.error()).containsInstanceOf(VaultKeyMaterialError.VaultNotInitialized.class);
  }

  @Test
  void shouldFail_whenRevisionIsStale() {
    final var now = Instant.now();
    final var accountId = UUID.randomUUID();
    final var command = new UpdateMasterWrappedKekCommand(accountId, new byte[] {9, 9, 9}, 1, now);

    when(repository.existsByAccountId(accountId)).thenReturn(true);
    when(repository.updateMasterWrappedKekIfRevisionMatches(
            accountId, command.newKekEncMaster(), command.expectedMasterKeyRevision(), now))
        .thenReturn(0);

    final var result = target.execute(command);

    assertThat(result.error())
        .containsInstanceOf(VaultKeyMaterialError.StaleMasterWrappedKekUpdate.class);
  }

  @Test
  void shouldRejectEmptyMasterWrappedKek_beforeCas() {
    final var command =
        new UpdateMasterWrappedKekCommand(UUID.randomUUID(), new byte[0], 1, Instant.now());

    when(repository.existsByAccountId(command.accountId())).thenReturn(true);

    assertThatThrownBy(() -> target.execute(command))
        .isInstanceOf(InvalidWrappedKekException.class)
        .hasMessageContaining("kekEncMaster");

    verify(repository, never())
        .updateMasterWrappedKekIfRevisionMatches(any(), any(), anyLong(), any());
  }
}
