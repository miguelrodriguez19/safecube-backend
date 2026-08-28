package unit.com.miguelrodriguez19.safecube.vault.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.GetVaultKeyMaterialResult;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.UpdateMasterWrappedKekCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.UpdateMasterWrappedKekResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultKeyMaterialError;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.GetVaultKeyMaterialUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.InitVaultKeyMaterialUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.UpdateMasterWrappedKekUseCase;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.VaultKeyMaterialController;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial.UpdateMasterWrappedKekRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import unit.annotation.UnitTest;

@UnitTest
class VaultKeyMaterialControllerTest {

  @Mock private InitVaultKeyMaterialUseCase initUseCase;
  @Mock private GetVaultKeyMaterialUseCase getUseCase;
  @Mock private UpdateMasterWrappedKekUseCase updateMasterUseCase;

  @Spy
  private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

  @InjectMocks private VaultKeyMaterialController target;

  @Test
  void shouldReturnStrongEtagAndPreventIntermediaryTransformations() {
    final var accountId = UUID.randomUUID();
    when(getUseCase.execute(any()))
        .thenReturn(
            Result.<GetVaultKeyMaterialResult, VaultKeyMaterialError>success(
                new GetVaultKeyMaterialResult(
                    accountId,
                    new byte[] {1},
                    new byte[] {2},
                    "Argon2id",
                    new byte[] {3},
                    65536,
                    3,
                    1,
                    32,
                    "v1",
                    Instant.parse("2026-01-01T00:00:00Z"),
                    Instant.parse("2026-01-01T00:00:00Z"),
                    1L)));

    final var response = target.get(accountId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getETag()).isEqualTo("\"master-1\"");
    assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store, no-transform");
  }

  @Test
  void shouldReturn428_withoutIfMatch_andAvoidUseCase() {
    final var response =
        target.updateMaster(UUID.randomUUID(), null, new MockHttpServletRequest(), validRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PRECONDITION_REQUIRED);
    verifyNoInteractions(updateMasterUseCase);
  }

  @Test
  void shouldReturn400_forWeakWildcardAndMultipleEtags() {
    for (final var value : new String[] {"W/\"master-1\"", "*", "\"master-1\", \"master-2\""}) {
      final var request = new MockHttpServletRequest();
      request.addHeader("If-Match", value);

      final var response = target.updateMaster(UUID.randomUUID(), value, request, validRequest());

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    verifyNoInteractions(updateMasterUseCase);
  }

  @Test
  void shouldReturn400_forMultipleIfMatchHeaderLines() {
    final var request = new MockHttpServletRequest();
    request.addHeader("If-Match", "\"master-1\"");
    request.addHeader("If-Match", "\"master-2\"");

    final var response =
        target.updateMaster(UUID.randomUUID(), "\"master-1\"", request, validRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    verifyNoInteractions(updateMasterUseCase);
  }

  @Test
  void shouldReturnIncrementedEtag_afterSuccessfulUseCase() {
    final var accountId = UUID.randomUUID();
    final var updateResult =
        Result.<UpdateMasterWrappedKekResult, VaultKeyMaterialError>success(
            new UpdateMasterWrappedKekResult(2L));
    when(updateMasterUseCase.execute(any(UpdateMasterWrappedKekCommand.class)))
        .thenReturn(updateResult);
    final var request = new MockHttpServletRequest();
    request.addHeader("If-Match", "\"master-1\"");

    final ResponseEntity<Void> response =
        target.updateMaster(accountId, "\"master-1\"", request, validRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getETag()).isEqualTo("\"master-2\"");
    assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store, no-transform");
    verify(updateMasterUseCase)
        .execute(
            argThat(
                command ->
                    command.accountId().equals(accountId)
                        && command.expectedMasterKeyRevision() == 1L));
  }

  private UpdateMasterWrappedKekRequest validRequest() {
    return new UpdateMasterWrappedKekRequest(new byte[] {1});
  }
}
