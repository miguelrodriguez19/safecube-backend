package com.miguelrodriguez19.safecube.vault.application.usecase;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.query.ListSecureItemsQuery;
import com.miguelrodriguez19.safecube.vault.application.dto.result.ListSecureItemsResult;
import com.miguelrodriguez19.safecube.vault.application.dto.result.ListSecureItemsResult.Item;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Use case responsible for listing SecureItems for a given account.
 *
 * <p>This use case applies all business-level filtering such as incremental sync ({@code since})
 * and soft-delete exclusion.
 */
@Component
@RequiredArgsConstructor
public class ListSecureItemsUseCase {

  private final SecureItemRepository secureItemRepository;

  public Result<ListSecureItemsResult, VaultError> execute(final ListSecureItemsQuery query) {

    final var allItems = secureItemRepository.findByAccount(query.accountId());

    final var filteredItems =
        allItems.stream()
            .filter(item -> isAfterSince(item, query.since()))
            .filter(item -> includeDeleted(item, query.includeDeleted()))
            .sorted(Comparator.comparing(item -> item.getUpdatedAt()))
            .map(
                item ->
                    new Item(
                        item.getItemId(),
                        item.getItemType(),
                        item.getSchemaVersion(),
                        item.getDisplayHint(),
                        item.getUpdatedAt(),
                        item.getDeletedAt()))
            .collect(Collectors.toList());

    return Result.success(new ListSecureItemsResult(filteredItems));
  }

  private static boolean isAfterSince(
      final com.miguelrodriguez19.safecube.vault.domain.model.SecureItem item,
      final java.time.Instant since) {

    if (since == null) {
      return true;
    }

    return item.getUpdatedAt().isAfter(since);
  }

  private static boolean includeDeleted(
      final com.miguelrodriguez19.safecube.vault.domain.model.SecureItem item,
      final boolean includeDeleted) {

    if (includeDeleted) {
      return true;
    }

    return !item.isDeleted();
  }
}
