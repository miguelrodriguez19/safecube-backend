package unit.com.miguelrodriguez19.safecube.vault.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.miguelrodriguez19.safecube.vault.application.dto.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.dto.command.CreateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.result.CreateSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.CreateSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.domain.exception.InvalidPayloadException;
import com.miguelrodriguez19.safecube.vault.domain.model.SecureItem;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import unit.annotation.UnitTest;

@UnitTest
class CreateSecureItemUseCaseTest {

  @Mock SecureItemRepository secureItemRepository;
  @Mock ItemTypeMapper itemTypeMapper;

  @InjectMocks private CreateSecureItemUseCase target;

  @Test
  void shouldCreateSecureItem_givenValidPayload() {
    final var now = Instant.now();
    final var accountId = UUID.randomUUID();
    final var payload = "encrypted-payload".getBytes();

    final var command = getCreateSecureItemCommand(accountId, payload, now);

    final var result = target.execute(command);

    final var secureItemCaptor = ArgumentCaptor.forClass(SecureItem.class);
    verify(secureItemRepository).save(secureItemCaptor.capture());

    final var savedSecureItem = secureItemCaptor.getValue();

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.success().get())
        .extracting(CreateSecureItemResult::itemId, CreateSecureItemResult::createdAt)
        .containsExactly(savedSecureItem.getItemId(), savedSecureItem.getCreatedAt());
  }

  @Test
  void shouldReturnInvalidPayloadError_givenNullPayload() {
    try (MockedStatic<SecureItem> secureItemMock = mockStatic(SecureItem.class)) {
      final var now = Instant.now();
      final var accountId = UUID.randomUUID();

      final var command =
          new CreateSecureItemCommand(accountId, ItemTypeDto.NOTE, 1, "Note", null, now);

      secureItemMock
          .when(() -> SecureItem.of(any(), any(), any(), anyInt(), any(), any(), any()))
          .thenThrow(new InvalidPayloadException("test"));

      final var result = target.execute(command);

      assertThat(result.isFailure()).isTrue();
      assertThat(result.error()).containsInstanceOf(VaultError.InvalidPayload.class);
    }
  }

  private CreateSecureItemCommand getCreateSecureItemCommand(
      final UUID accountId, final byte[] payload, final Instant now) {
    return new CreateSecureItemCommand(accountId, ItemTypeDto.PASSWORD, 1, "GitHub", payload, now);
  }
}
