package com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsFilter;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsQuery;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.ListSecureItemsResult;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.ListSecureItemsResult.Item;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Use case responsible for listing SecureItems for a given account.
 *
 * <p>Applies business-level filtering, ordering and limiting based on the provided {@link
 * ListSecureItemsFilter}. No infrastructure or HTTP concerns are handled here.
 */
@Component
@RequiredArgsConstructor
public class ListSecureItemsUseCase {

  private final SecureItemRepository secureItemRepository;
  private final ItemTypeMapper itemTypeMapper;

  public Result<ListSecureItemsResult, VaultError> execute(final ListSecureItemsQuery query) {
    final var items =
        secureItemRepository.findFilteredByAccount(query.accountId(), query.filter()).stream()
            .map(
                item ->
                    new Item(
                        item.getItemId(),
                        itemTypeMapper.toDto(item.getItemType()),
                        item.getSchemaVersion(),
                        item.getDisplayHint(),
                        item.getPayloadVersion(),
                        item.getUpdatedAt(),
                        item.getDeletedAt()))
            .toList();

    return Result.success(new ListSecureItemsResult(items));
  }
}
