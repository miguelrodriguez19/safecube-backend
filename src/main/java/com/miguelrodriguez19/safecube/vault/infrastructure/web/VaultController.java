package com.miguelrodriguez19.safecube.vault.infrastructure.web;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.dto.command.CreateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.command.DeleteSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.command.UpdateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.query.GetSecureItemQuery;
import com.miguelrodriguez19.safecube.vault.application.dto.query.ListSecureItemsFilter.Order;
import com.miguelrodriguez19.safecube.vault.application.dto.query.ListSecureItemsQuery;
import com.miguelrodriguez19.safecube.vault.application.dto.result.CreateSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.dto.result.DeleteSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.dto.result.GetSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.dto.result.ListSecureItemsResult;
import com.miguelrodriguez19.safecube.vault.application.dto.result.UpdateSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.usecase.CreateSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.DeleteSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.GetSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.ListSecureItemsUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.UpdateSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.request.CreateSecureItemRequest;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.request.DeleteSecureItemRequest;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.request.UpdateSecureItemRequest;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.response.ListSecureItemsResponse;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.response.SecureItemResponse;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.response.SecureItemSummaryResponse;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.mapper.ListSecureItemsFilterMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * VaultController
 *
 * <p>HTTP adapter for vault SecureItem operations.
 */
@RestController
@RequestMapping("/vault/items")
@RequiredArgsConstructor
public class VaultController {

  private final CreateSecureItemUseCase createUseCase;
  private final GetSecureItemUseCase getUseCase;
  private final ListSecureItemsUseCase listUseCase;
  private final UpdateSecureItemUseCase updateUseCase;
  private final DeleteSecureItemUseCase deleteUseCase;

  private final ListSecureItemsFilterMapper filterMapper;

  private final Clock clock;

  @PostMapping
  public ResponseEntity<CreateSecureItemResult> create(
      @AuthenticationPrincipal final UUID accountId,
      @Valid @RequestBody final CreateSecureItemRequest request) {

    final var now = Instant.now(clock);
    final var itemType = ItemTypeDto.valueOf(request.itemType());

    final var result =
        createUseCase.execute(
            new CreateSecureItemCommand(
                accountId,
                itemType,
                request.schemaVersion(),
                request.displayHint(),
                request.payload(),
                now));

    return switch (result) {
      case Result.Success<CreateSecureItemResult, VaultError> s ->
          ResponseEntity.status(HttpStatus.CREATED).body(s.success().orElseThrow());

      case Result.Failure<CreateSecureItemResult, VaultError> f ->
          mapVaultError(f.error().orElseThrow());
    };
  }

  @GetMapping("/{itemId}")
  public ResponseEntity<SecureItemResponse> get(
      @AuthenticationPrincipal final UUID accountId, @PathVariable final UUID itemId) {

    final var result = getUseCase.execute(new GetSecureItemQuery(accountId, itemId));

    return switch (result) {
      case Result.Success<GetSecureItemResult, VaultError> s -> {
        final var item = s.success().orElseThrow();

        yield ResponseEntity.ok(
            new SecureItemResponse(
                item.itemId(),
                item.itemType().name(),
                item.schemaVersion(),
                item.displayHint(),
                item.payload(),
                item.payloadVersion(),
                item.updatedAt(),
                item.deletedAt()));
      }

      case Result.Failure<GetSecureItemResult, VaultError> f ->
          mapVaultError(f.error().orElseThrow());
    };
  }

  @GetMapping
  public ResponseEntity<ListSecureItemsResponse> list(
      @AuthenticationPrincipal final UUID accountId,
      @RequestParam(required = false) final Instant since,
      @RequestParam(required = false) final ItemTypeDto type,
      @RequestParam(required = false) final Set<String> labels,
      @RequestParam(required = false, defaultValue = "false") final boolean includeDeleted,
      @RequestParam(required = false) @Positive final Integer limit,
      @RequestParam(required = false, defaultValue = "DISPLAY_NAME_ASC") final Order order) {

    final var filters = filterMapper.from(since, type, labels, includeDeleted, limit, order);

    final var result = listUseCase.execute(new ListSecureItemsQuery(accountId, filters));

    return switch (result) {
      case Result.Success<ListSecureItemsResult, VaultError> s -> {
        final var items =
            s.success().orElseThrow().items().stream()
                .map(
                    item ->
                        new SecureItemSummaryResponse(
                            item.itemId(),
                            item.itemType().name(),
                            item.schemaVersion(),
                            item.displayHint(),
                            item.payloadVersion(),
                            item.updatedAt(),
                            item.deletedAt()))
                .toList();

        yield ResponseEntity.ok(new ListSecureItemsResponse(items));
      }

      case Result.Failure<ListSecureItemsResult, VaultError> f ->
          mapVaultError(f.error().orElseThrow());
    };
  }

  @PutMapping("/{itemId}")
  public ResponseEntity<UpdateSecureItemResult> update(
      @AuthenticationPrincipal final UUID accountId,
      @PathVariable final UUID itemId,
      @Valid @RequestBody final UpdateSecureItemRequest request) {

    final var itemType = ItemTypeDto.valueOf(request.itemType());
    final var result =
        updateUseCase.execute(
            new UpdateSecureItemCommand(
                accountId,
                itemId,
                itemType,
                request.schemaVersion(),
                request.displayHint(),
                request.payload(),
                request.updatedAt()));

    return switch (result) {
      case Result.Success<UpdateSecureItemResult, VaultError> s ->
          ResponseEntity.ok(s.success().orElseThrow());

      case Result.Failure<UpdateSecureItemResult, VaultError> f ->
          mapVaultError(f.error().orElseThrow());
    };
  }

  @DeleteMapping("/{itemId}")
  public ResponseEntity<DeleteSecureItemResult> delete(
      @AuthenticationPrincipal final UUID accountId,
      @PathVariable final UUID itemId,
      @Valid @RequestBody final DeleteSecureItemRequest request) {

    final var result =
        deleteUseCase.execute(new DeleteSecureItemCommand(accountId, itemId, request.deletedAt()));

    return switch (result) {
      case Result.Success<DeleteSecureItemResult, VaultError> s ->
          ResponseEntity.ok(s.success().orElseThrow());

      case Result.Failure<DeleteSecureItemResult, VaultError> f ->
          mapVaultError(f.error().orElseThrow());
    };
  }

  private <T> ResponseEntity<T> mapVaultError(final VaultError error) {
    return switch (error) {
      case VaultError.InvalidPayload e -> ResponseEntity.badRequest().build();

      case VaultError.SecureItemNotFound e -> ResponseEntity.status(HttpStatus.NOT_FOUND).build();

      case VaultError.StaleUpdateRejected e -> ResponseEntity.status(HttpStatus.CONFLICT).build();

      case VaultError.StaleDeleteRejected e -> ResponseEntity.status(HttpStatus.CONFLICT).build();
    };
  }
}
