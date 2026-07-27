package com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.ListSecureItemChangesResult;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.ListSecureItemChangesResult.Item;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListSecureItemChangesUseCase {

  private final SecureItemRepository secureItemRepository;
  private final ItemTypeMapper itemTypeMapper;

  public Result<ListSecureItemChangesResult, VaultError> execute(
      final UUID accountId, final long after, final int limit) {
    final var fetched = secureItemRepository.findChanges(accountId, after, limit + 1);
    final var hasMore = fetched.size() > limit;
    final var page = hasMore ? fetched.subList(0, limit) : fetched;
    final var items =
        page.stream()
            .map(
                item ->
                    new Item(
                        item.getItemId(),
                        itemTypeMapper.toDto(item.getItemType()),
                        item.getSchemaVersion(),
                        item.getDisplayHint(),
                        item.getPayload(),
                        item.getPayloadVersion(),
                        item.getItemRevision(),
                        item.getChangeSequence(),
                        item.getUpdatedAt(),
                        item.getDeletedAt()))
            .toList();
    final var nextCursor = page.isEmpty() ? after : page.get(page.size() - 1).getChangeSequence();
    return Result.success(new ListSecureItemChangesResult(items, nextCursor, hasMore));
  }
}
