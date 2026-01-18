package unit.com.miguelrodriguez19.safecube.vault.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.vault.application.dto.query.GetSecureItemQuery;
import com.miguelrodriguez19.safecube.vault.application.dto.result.GetSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.GetSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.domain.model.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.SecureItem;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class GetSecureItemUseCaseTest {

  @Mock SecureItemRepository secureItemRepository;

  @InjectMocks private GetSecureItemUseCase target;

  @Test
  void shouldReturnSecureItem_givenExistingItemForAccount() {
    final var accountId = UUID.randomUUID();
    final var itemId = UUID.randomUUID();
    final var payload = "opaque-encrypted-payload".getBytes();
    final var createdAt = Instant.parse("2026-01-18T10:00:00Z");
    final var updatedAt = Instant.parse("2026-01-18T10:30:00Z");
    final var payloadVersion = 3L;

    final var persistedItem =
        SecureItem.restore(
            itemId,
            accountId,
            ItemType.PASSWORD,
            1,
            "GitHub",
            payload,
            payloadVersion,
            createdAt,
            updatedAt,
            null);

    when(secureItemRepository.findByIdAndAccount(itemId, accountId)).thenReturn(persistedItem);

    final var result = target.execute(getGetSecureItemQuery(accountId, itemId));

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.success().get())
        .extracting(
            GetSecureItemResult::itemId,
            GetSecureItemResult::itemType,
            GetSecureItemResult::schemaVersion,
            GetSecureItemResult::displayHint,
            GetSecureItemResult::payloadVersion,
            GetSecureItemResult::updatedAt,
            GetSecureItemResult::deletedAt)
        .containsExactly(itemId, ItemType.PASSWORD, 1, "GitHub", payloadVersion, updatedAt, null);

    assertThat(result.success().get().payload()).isEqualTo(payload);
  }

  @Test
  void shouldReturnSecureItemNotFound_givenMissingItem() {
    final var accountId = UUID.randomUUID();
    final var itemId = UUID.randomUUID();

    when(secureItemRepository.findByIdAndAccount(itemId, accountId)).thenReturn(null);

    final var result = target.execute(getGetSecureItemQuery(accountId, itemId));

    assertThat(result.isFailure()).isTrue();
    assertThat(result.error()).containsInstanceOf(VaultError.SecureItemNotFound.class);
  }

  private GetSecureItemQuery getGetSecureItemQuery(final UUID accountId, final UUID itemId) {
    return new GetSecureItemQuery(accountId, itemId);
  }
}
