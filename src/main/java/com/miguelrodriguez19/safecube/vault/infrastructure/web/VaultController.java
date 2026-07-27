package com.miguelrodriguez19.safecube.vault.infrastructure.web;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.CreateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.DeleteSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.UpdateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.GetSecureItemQuery;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsQuery;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.CreateSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.DeleteSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.GetSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.ListSecureItemsResult;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.UpdateSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.CreateSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.DeleteSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.GetSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.ListSecureItemChangesUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.ListSecureItemsUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.UpdateSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.request.CreateSecureItemRequest;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.request.UpdateSecureItemRequest;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.response.ListSecureItemChangesResponse;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.response.ListSecureItemsResponse;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.response.SecureItemChangeResponse;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.response.SecureItemResponse;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.response.SecureItemSummaryResponse;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.mapper.ListSecureItemsFilterMapper;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.annotation.ValidItemType;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.annotation.ValidOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
  private final ListSecureItemChangesUseCase listChangesUseCase;
  private final UpdateSecureItemUseCase updateUseCase;
  private final DeleteSecureItemUseCase deleteUseCase;

  private final ListSecureItemsFilterMapper filterMapper;

  private final Clock clock;

  @Operation(
      operationId = "createVaultItem",
      summary = "Create vault item",
      description =
          "Creates a new vault item for the authenticated account. The payload is opaque encrypted data.")
  @ApiResponse(
      responseCode = "201",
      description = "Created",
      headers = @Header(name = "ETag", schema = @Schema(type = "string")))
  @PostMapping
  public ResponseEntity<CreateSecureItemResult> create(
      @AuthenticationPrincipal final UUID accountId,
      @RequestHeader("Idempotency-Key") final UUID mutationId,
      @Valid @RequestBody final CreateSecureItemRequest request) {

    final var now = serverTimestamp();
    final var itemType = ItemTypeDto.valueOf(request.itemType());

    final var result =
        createUseCase.execute(
            new CreateSecureItemCommand(
                accountId,
                itemType,
                request.schemaVersion(),
                request.displayHint(),
                request.payload(),
                request.payloadVersion(),
                mutationId,
                now));

    return switch (result) {
      case Result.Success<CreateSecureItemResult, VaultError> s ->
          withEtag(HttpStatus.CREATED, s.success().orElseThrow().itemRevision())
              .body(s.success().orElseThrow());

      case Result.Failure<CreateSecureItemResult, VaultError> f ->
          mapVaultError(f.error().orElseThrow());
    };
  }

  @Operation(
      operationId = "getVaultItem",
      summary = "Get vault item",
      description = "Returns a vault item by id. The payload is opaque encrypted data.")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      headers = @Header(name = "ETag", schema = @Schema(type = "string")))
  @GetMapping("/{itemId}")
  public ResponseEntity<SecureItemResponse> get(
      @AuthenticationPrincipal final UUID accountId, @PathVariable("itemId") final UUID itemId) {

    final var result = getUseCase.execute(new GetSecureItemQuery(accountId, itemId));

    return switch (result) {
      case Result.Success<GetSecureItemResult, VaultError> s -> {
        final var item = s.success().orElseThrow();

        yield ResponseEntity.ok()
            .eTag(formatEtag(item.itemRevision()))
            .body(
                new SecureItemResponse(
                    item.itemId(),
                    item.itemType().name(),
                    item.schemaVersion(),
                    item.displayHint(),
                    item.payload(),
                    item.payloadVersion(),
                    item.itemRevision(),
                    item.changeSequence(),
                    item.updatedAt(),
                    item.deletedAt()));
      }

      case Result.Failure<GetSecureItemResult, VaultError> f ->
          mapVaultError(f.error().orElseThrow());
    };
  }

  @Operation(
      operationId = "listVaultItems",
      summary = "List vault items",
      description =
          "Lists vault items for sync/listing. Supports filters and optional inclusion of deleted items.")
  @GetMapping
  public ResponseEntity<ListSecureItemsResponse> list(
      @AuthenticationPrincipal final UUID accountId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          final Instant createdAfter,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          final Instant updatedAfter,
      @RequestParam(required = false) @ValidItemType final String type,
      @RequestParam(required = false) final Set<String> labels,
      @RequestParam(required = false, defaultValue = "false") final boolean includeDeleted,
      @RequestParam(required = false) @Positive final Integer limit,
      @RequestParam(required = false, defaultValue = "DISPLAY_NAME_ASC") @ValidOrder
          final String order) {

    final var filters =
        filterMapper.from(createdAfter, updatedAfter, type, labels, includeDeleted, limit, order);

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
                            item.itemRevision(),
                            item.changeSequence(),
                            item.updatedAt(),
                            item.deletedAt()))
                .toList();

        yield ResponseEntity.ok(new ListSecureItemsResponse(items));
      }

      case Result.Failure<ListSecureItemsResult, VaultError> f ->
          mapVaultError(f.error().orElseThrow());
    };
  }

  @Operation(
      operationId = "listVaultItemChanges",
      summary = "List ordered vault item changes",
      description = "Returns complete encrypted snapshots ordered by a server-owned change cursor.")
  @GetMapping("/changes")
  public ResponseEntity<ListSecureItemChangesResponse> listChanges(
      @AuthenticationPrincipal final UUID accountId,
      @RequestParam(defaultValue = "0") final long after,
      @RequestParam(defaultValue = "100") @Positive final int limit) {
    final var result = listChangesUseCase.execute(accountId, after, Math.min(limit, 500));
    return switch (result) {
      case Result.Success<
                  com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result
                      .ListSecureItemChangesResult,
                  VaultError>
              s -> {
        final var page = s.success().orElseThrow();
        final var items =
            page.items().stream()
                .map(
                    item ->
                        new SecureItemChangeResponse(
                            item.itemId(),
                            item.itemType().name(),
                            item.schemaVersion(),
                            item.displayHint(),
                            item.payload(),
                            item.payloadVersion(),
                            item.itemRevision(),
                            item.changeSequence(),
                            item.updatedAt(),
                            item.deletedAt()))
                .toList();
        yield ResponseEntity.ok(
            new ListSecureItemChangesResponse(items, page.nextCursor(), page.hasMore()));
      }
      case Result.Failure<
                      com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result
                          .ListSecureItemChangesResult,
                      VaultError>
                  f ->
          mapVaultError(f.error().orElseThrow());
    };
  }

  @Operation(
      operationId = "updateVaultItem",
      summary = "Update vault item",
      description =
          "Updates an existing vault item. The payload is opaque encrypted data. Conflicts may occur on stale updates.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        headers = @Header(name = "ETag", schema = @Schema(type = "string"))),
    @ApiResponse(responseCode = "412", description = "Precondition failed"),
    @ApiResponse(responseCode = "428", description = "Precondition required")
  })
  @PutMapping("/{itemId}")
  public ResponseEntity<UpdateSecureItemResult> update(
      @AuthenticationPrincipal final UUID accountId,
      @PathVariable("itemId") final UUID itemId,
      @RequestHeader("Idempotency-Key") final UUID mutationId,
      @Parameter(required = true, description = "Quoted server-owned item revision.")
          @RequestHeader(value = "If-Match", required = false)
          final String ifMatch,
      @Valid @RequestBody final UpdateSecureItemRequest request) {
    if (ifMatch == null) {
      return ResponseEntity.status(428).build();
    }
    final var expectedItemRevision = parseEtag(ifMatch);
    if (expectedItemRevision == null) {
      return ResponseEntity.badRequest().build();
    }

    final var itemType = ItemTypeDto.valueOf(request.itemType());
    final var now = serverTimestamp();

    final var result =
        updateUseCase.execute(
            new UpdateSecureItemCommand(
                accountId,
                itemId,
                itemType,
                request.schemaVersion(),
                request.displayHint(),
                request.payload(),
                request.payloadVersion(),
                expectedItemRevision,
                mutationId,
                now));

    return switch (result) {
      case Result.Success<UpdateSecureItemResult, VaultError> s ->
          ResponseEntity.ok()
              .eTag(formatEtag(s.success().orElseThrow().itemRevision()))
              .body(s.success().orElseThrow());

      case Result.Failure<UpdateSecureItemResult, VaultError> f ->
          mapVaultError(f.error().orElseThrow());
    };
  }

  @Operation(
      operationId = "deleteVaultItem",
      summary = "Delete vault item (soft delete)",
      description = "Soft-deletes a vault item for the authenticated account.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        headers = @Header(name = "ETag", schema = @Schema(type = "string"))),
    @ApiResponse(responseCode = "412", description = "Precondition failed"),
    @ApiResponse(responseCode = "428", description = "Precondition required")
  })
  @DeleteMapping("/{itemId}")
  public ResponseEntity<DeleteSecureItemResult> delete(
      @AuthenticationPrincipal final UUID accountId,
      @PathVariable("itemId") final UUID itemId,
      @RequestHeader("Idempotency-Key") final UUID mutationId,
      @Parameter(required = true, description = "Quoted server-owned item revision.")
          @RequestHeader(value = "If-Match", required = false)
          final String ifMatch) {
    if (ifMatch == null) {
      return ResponseEntity.status(428).build();
    }
    final var expectedItemRevision = parseEtag(ifMatch);
    if (expectedItemRevision == null) {
      return ResponseEntity.badRequest().build();
    }

    final var deletedAt = serverTimestamp();
    final var result =
        deleteUseCase.execute(
            new DeleteSecureItemCommand(
                accountId, itemId, expectedItemRevision, mutationId, deletedAt));

    return switch (result) {
      case Result.Success<DeleteSecureItemResult, VaultError> s ->
          ResponseEntity.ok()
              .eTag(formatEtag(s.success().orElseThrow().itemRevision()))
              .body(s.success().orElseThrow());

      case Result.Failure<DeleteSecureItemResult, VaultError> f ->
          mapVaultError(f.error().orElseThrow());
    };
  }

  private <T> ResponseEntity<T> mapVaultError(final VaultError error) {
    return switch (error) {
      case VaultError.InvalidPayload e -> ResponseEntity.badRequest().build();

      case VaultError.SecureItemNotFound e -> ResponseEntity.status(HttpStatus.NOT_FOUND).build();

      case VaultError.StaleUpdateRejected e ->
          ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();

      case VaultError.StaleDeleteRejected e ->
          ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();

      case VaultError.IdempotencyConflict e -> ResponseEntity.status(HttpStatus.CONFLICT).build();
    };
  }

  private Long parseEtag(final String value) {
    final var normalized = value.trim();
    if (normalized.length() < 3
        || normalized.charAt(0) != '"'
        || normalized.charAt(normalized.length() - 1) != '"') {
      return null;
    }
    try {
      final var revision = Long.parseLong(normalized.substring(1, normalized.length() - 1));
      return revision > 0 ? revision : null;
    } catch (final NumberFormatException exception) {
      return null;
    }
  }

  private String formatEtag(final long itemRevision) {
    return "\"" + itemRevision + "\"";
  }

  private Instant serverTimestamp() {
    return clock.instant().truncatedTo(ChronoUnit.MICROS);
  }

  private <T> ResponseEntity.BodyBuilder withEtag(
      final HttpStatus status, final long itemRevision) {
    return ResponseEntity.status(status).eTag(formatEtag(itemRevision));
  }
}
