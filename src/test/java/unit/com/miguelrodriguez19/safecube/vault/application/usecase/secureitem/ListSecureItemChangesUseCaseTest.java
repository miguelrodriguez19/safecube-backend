package unit.com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.ListSecureItemChangesUseCase;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class ListSecureItemChangesUseCaseTest {

  @Mock SecureItemRepository secureItemRepository;
  @Mock ItemTypeMapper itemTypeMapper;

  @InjectMocks private ListSecureItemChangesUseCase target;

  @Test
  void shouldReturnTheRequestedPageAndSignalMoreChanges() {
    final var accountId = UUID.randomUUID();
    final var first = item(accountId, 11L);
    final var second = item(accountId, 12L);
    final var third = item(accountId, 13L);
    when(secureItemRepository.findChanges(accountId, 10L, 3))
        .thenReturn(List.of(first, second, third));
    when(itemTypeMapper.toDto(ItemType.NOTE)).thenReturn(ItemTypeDto.NOTE);

    final var result = target.execute(accountId, 10L, 2).success().orElseThrow();

    assertThat(result.hasMore()).isTrue();
    assertThat(result.nextCursor()).isEqualTo(12L);
    assertThat(result.items()).extracting(item -> item.changeSequence()).containsExactly(11L, 12L);
    verify(secureItemRepository).findChanges(accountId, 10L, 3);
  }

  @Test
  void shouldKeepTheRequestedCursorWhenNoChangesExist() {
    final var accountId = UUID.randomUUID();
    when(secureItemRepository.findChanges(accountId, 17L, 6)).thenReturn(List.of());

    final var result = target.execute(accountId, 17L, 5).success().orElseThrow();

    assertThat(result.items()).isEmpty();
    assertThat(result.nextCursor()).isEqualTo(17L);
    assertThat(result.hasMore()).isFalse();
  }

  private SecureItem item(final UUID accountId, final long changeSequence) {
    final var timestamp = Instant.parse("2026-01-18T10:00:00Z").plusSeconds(changeSequence);
    return SecureItem.restore(
        UUID.randomUUID(),
        accountId,
        ItemType.NOTE,
        1,
        "Note",
        "encrypted".getBytes(),
        1L,
        1L,
        changeSequence,
        timestamp,
        timestamp,
        null);
  }
}
