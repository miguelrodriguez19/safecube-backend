package com.miguelrodriguez19.safecube.vault.infrastructure.web;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.shared.result.Void;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.GetVaultKeyMaterialQuery;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.GetVaultKeyMaterialResult;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.InitVaultKeyMaterialCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.UpdateMasterWrappedKekCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.UpdateMasterWrappedKekResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultKeyMaterialError;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.GetVaultKeyMaterialUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.InitVaultKeyMaterialUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.UpdateMasterWrappedKekUseCase;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial.InitVaultKeyMaterialRequest;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial.UpdateMasterWrappedKekRequest;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial.VaultKeyMaterialResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.Instant;
import java.util.Enumeration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * VaultKeyMaterialController
 *
 * <p>HTTP adapter for vault key material operations.
 */
@RestController
@RequestMapping("/vault/keys")
@RequiredArgsConstructor
public class VaultKeyMaterialController {

  private final InitVaultKeyMaterialUseCase initUseCase;
  private final GetVaultKeyMaterialUseCase getUseCase;
  private final UpdateMasterWrappedKekUseCase updateMasterUseCase;

  private final Clock clock;

  @Operation(
      operationId = "initVaultKeyMaterial",
      summary = "Initialize vault key material",
      description =
          "Stores wrapped key material and KDF parameters. All fields are opaque; the backend never derives or decrypts keys.")
  @PostMapping
  public ResponseEntity<java.lang.Void> init(
      @AuthenticationPrincipal final UUID accountId,
      @Valid @RequestBody final InitVaultKeyMaterialRequest request) {

    final var now = Instant.now(clock);

    final var command =
        new InitVaultKeyMaterialCommand(
            accountId,
            request.kekEncMaster(),
            request.kekEncRecovery(),
            request.kdfAlgorithm(),
            request.kdfSalt(),
            request.kdfMemoryKib(),
            request.kdfIterations(),
            request.kdfParallelism(),
            request.kdfOutputLen(),
            request.cryptoVersion(),
            now);

    final var result = initUseCase.execute(command);

    return switch (result) {
      case Result.Success<Void, VaultKeyMaterialError> s ->
          ResponseEntity.status(HttpStatus.CREATED).build();

      case Result.Failure<Void, VaultKeyMaterialError> f -> mapError(f.error().orElseThrow());
    };
  }

  @Operation(
      operationId = "getVaultKeyMaterial",
      summary = "Get vault key material",
      description =
          "Returns the stored wrapped key material and crypto/KDF parameters for the authenticated account.")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      headers = {
        @Header(
            name = "ETag",
            description = "Strong server-owned master key revision.",
            required = true,
            schema = @Schema(type = "string", example = "\"master-1\"")),
        @Header(
            name = "Cache-Control",
            description =
                "Responses containing vault key material must not be cached or transformed by intermediaries.",
            required = true,
            schema = @Schema(type = "string", example = "no-store, no-transform"))
      })
  @GetMapping
  public ResponseEntity<VaultKeyMaterialResponse> get(
      @AuthenticationPrincipal final UUID accountId) {

    final var result = getUseCase.execute(new GetVaultKeyMaterialQuery(accountId));

    return switch (result) {
      case Result.Success<GetVaultKeyMaterialResult, VaultKeyMaterialError> s -> {
        final var vaultKeyMaterial = s.success().orElseThrow();
        yield ResponseEntity.ok()
            .eTag(formatMasterEtag(vaultKeyMaterial.masterKeyRevision()))
            .cacheControl(CacheControl.noStore().noTransform())
            .body(
                new VaultKeyMaterialResponse(
                    vaultKeyMaterial.accountId(),
                    vaultKeyMaterial.kekEncMaster(),
                    vaultKeyMaterial.kekEncRecovery(),
                    vaultKeyMaterial.kdfAlgorithm(),
                    vaultKeyMaterial.kdfSalt(),
                    vaultKeyMaterial.kdfMemoryKib(),
                    vaultKeyMaterial.kdfIterations(),
                    vaultKeyMaterial.kdfParallelism(),
                    vaultKeyMaterial.kdfOutputLen(),
                    vaultKeyMaterial.cryptoVersion(),
                    vaultKeyMaterial.createdAt(),
                    vaultKeyMaterial.updatedAt()));
      }

      case Result.Failure<GetVaultKeyMaterialResult, VaultKeyMaterialError> f ->
          mapError(f.error().orElseThrow());
    };
  }

  @Operation(
      operationId = "updateMasterWrappedKek",
      summary = "Update master-wrapped KEK",
      description =
          "Updates the master-wrapped key material after passphrase change. Does not modify existing vault items.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        headers = {
          @Header(
              name = "ETag",
              description = "Strong server-owned incremented master key revision.",
              required = true,
              schema = @Schema(type = "string", example = "\"master-2\"")),
          @Header(
              name = "Cache-Control",
              description =
                  "Responses containing vault key material must not be cached or transformed by intermediaries.",
              required = true,
              schema = @Schema(type = "string", example = "no-store, no-transform"))
        }),
    @ApiResponse(responseCode = "412", description = "The If-Match revision is stale."),
    @ApiResponse(responseCode = "428", description = "If-Match is required.")
  })
  @PutMapping("/master")
  public ResponseEntity<java.lang.Void> updateMaster(
      @AuthenticationPrincipal final UUID accountId,
      @Parameter(
              name = "If-Match",
              required = true,
              description = "Exactly one strong ETag returned by GET /vault/keys.",
              schema = @Schema(type = "string", pattern = "\"master-[1-9][0-9]*\""))
          @RequestHeader(value = "If-Match", required = false)
          final String ifMatch,
      final HttpServletRequest httpRequest,
      @Valid @RequestBody final UpdateMasterWrappedKekRequest request) {

    final var expectedRevision = parseMasterEtag(ifMatch, httpRequest.getHeaders("If-Match"));
    if (expectedRevision == null) {
      if (ifMatch == null) {
        return ResponseEntity.status(428).build();
      }
      return ResponseEntity.badRequest().build();
    }

    final var now = Instant.now(clock);

    final var command =
        new UpdateMasterWrappedKekCommand(
            accountId, request.newKekEncMaster(), expectedRevision, now);

    final var result = updateMasterUseCase.execute(command);

    return switch (result) {
      case Result.Success<UpdateMasterWrappedKekResult, VaultKeyMaterialError> s ->
          ResponseEntity.ok()
              .eTag(formatMasterEtag(s.success().orElseThrow().masterKeyRevision()))
              .cacheControl(CacheControl.noStore().noTransform())
              .build();

      case Result.Failure<UpdateMasterWrappedKekResult, VaultKeyMaterialError> f ->
          mapError(f.error().orElseThrow());
    };
  }

  private <T> ResponseEntity<T> mapError(final VaultKeyMaterialError error) {
    return switch (error) {
      case VaultKeyMaterialError.VaultAlreadyInitialized e ->
          ResponseEntity.status(HttpStatus.CONFLICT).build();

      case VaultKeyMaterialError.VaultNotInitialized e ->
          ResponseEntity.status(HttpStatus.NOT_FOUND).build();

      case VaultKeyMaterialError.StaleMasterWrappedKekUpdate e ->
          ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
    };
  }

  private Long parseMasterEtag(final String ifMatch, final Enumeration<String> headerValues) {
    if (ifMatch == null || headerValues == null) {
      return null;
    }

    var headerCount = 0;
    while (headerValues.hasMoreElements()) {
      headerValues.nextElement();
      headerCount++;
    }
    if (headerCount != 1) {
      return null;
    }

    final var normalized = ifMatch.trim();
    if (!normalized.matches("\"master-[1-9][0-9]*\"")) {
      return null;
    }

    try {
      final var prefix = "\"master-";
      return Long.parseLong(normalized.substring(prefix.length(), normalized.length() - 1));
    } catch (final NumberFormatException exception) {
      return null;
    }
  }

  private String formatMasterEtag(final long masterKeyRevision) {
    return "\"master-" + masterKeyRevision + "\"";
  }
}
