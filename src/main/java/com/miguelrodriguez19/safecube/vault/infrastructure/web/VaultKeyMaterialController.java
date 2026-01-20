package com.miguelrodriguez19.safecube.vault.infrastructure.web;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.shared.result.Void;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.GetVaultKeyMaterialQuery;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.GetVaultKeyMaterialResult;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.InitVaultKeyMaterialCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.UpdateMasterWrappedKekCommand;
import com.miguelrodriguez19.safecube.vault.application.error.VaultKeyMaterialError;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.GetVaultKeyMaterialUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.InitVaultKeyMaterialUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial.UpdateMasterWrappedKekUseCase;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial.InitVaultKeyMaterialRequest;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial.UpdateMasterWrappedKekRequest;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.keymaterial.VaultKeyMaterialResponse;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  @PostMapping
  public ResponseEntity<Void> init(
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

  @GetMapping
  public ResponseEntity<VaultKeyMaterialResponse> get(
      @AuthenticationPrincipal final UUID accountId) {

    final var result = getUseCase.execute(new GetVaultKeyMaterialQuery(accountId));

    return switch (result) {
      case Result.Success<GetVaultKeyMaterialResult, VaultKeyMaterialError> s -> {
        final var vaultKeyMaterial = s.success().orElseThrow();
        yield ResponseEntity.ok(
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

  @PutMapping("/master")
  public ResponseEntity<Void> updateMaster(
      @AuthenticationPrincipal final UUID accountId,
      @Valid @RequestBody final UpdateMasterWrappedKekRequest request) {

    final var now = Instant.now(clock);

    final var command =
        new UpdateMasterWrappedKekCommand(accountId, request.newKekEncMaster(), now);

    final var result = updateMasterUseCase.execute(command);

    return switch (result) {
      case Result.Success<Void, VaultKeyMaterialError> s -> ResponseEntity.ok().build();

      case Result.Failure<Void, VaultKeyMaterialError> f -> mapError(f.error().orElseThrow());
    };
  }

  private <T> ResponseEntity<T> mapError(final VaultKeyMaterialError error) {
    return switch (error) {
      case VaultKeyMaterialError.VaultAlreadyInitialized e ->
          ResponseEntity.status(HttpStatus.CONFLICT).build();

      case VaultKeyMaterialError.VaultNotInitialized e ->
          ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    };
  }
}
