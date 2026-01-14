package com.miguelrodriguez19.safecube.user.infrastructure.web;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.shared.result.Void;
import com.miguelrodriguez19.safecube.user.application.dto.CreateUserProfileCommand;
import com.miguelrodriguez19.safecube.user.application.dto.DeleteUserProfileCommand;
import com.miguelrodriguez19.safecube.user.application.dto.UpdateUserProfileCommand;
import com.miguelrodriguez19.safecube.user.application.dto.UserProfileResponse;
import com.miguelrodriguez19.safecube.user.application.error.UserError;
import com.miguelrodriguez19.safecube.user.application.usecase.CreateUserProfileUseCase;
import com.miguelrodriguez19.safecube.user.application.usecase.DeleteUserProfileUseCase;
import com.miguelrodriguez19.safecube.user.application.usecase.GetUserProfileUseCase;
import com.miguelrodriguez19.safecube.user.application.usecase.UpdateUserProfileUseCase;
import com.miguelrodriguez19.safecube.user.infrastructure.web.dto.CreateUserProfileRequest;
import com.miguelrodriguez19.safecube.user.infrastructure.web.dto.UpdateUserProfileRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * UserProfileController
 *
 * <p>HTTP adapter for authenticated user profile operations.
 */
@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

  private final CreateUserProfileUseCase createUserProfileUseCase;
  private final GetUserProfileUseCase getUserProfileUseCase;
  private final UpdateUserProfileUseCase updateUserProfileUseCase;
  private final DeleteUserProfileUseCase deleteUserProfileUseCase;

  private final Clock clock;

  @PostMapping
  public ResponseEntity<UserProfileResponse> create(
      @Valid @NotNull @RequestBody final CreateUserProfileRequest request,
      @AuthenticationPrincipal final UUID accountId) {

    final var result =
        createUserProfileUseCase.execute(
            new CreateUserProfileCommand(
                UUID.randomUUID(), accountId, request.displayName(), Instant.now(clock)));

    return mapResult(result, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<UserProfileResponse> get(@AuthenticationPrincipal final UUID accountId) {

    final var result = getUserProfileUseCase.execute(accountId);

    return mapResult(result, HttpStatus.OK);
  }

  @PutMapping
  public ResponseEntity<UserProfileResponse> update(
      @Valid @NotNull @RequestBody final UpdateUserProfileRequest request,
      @AuthenticationPrincipal final UUID accountId) {

    final var result =
        updateUserProfileUseCase.execute(
            new UpdateUserProfileCommand(accountId, request.displayName(), Instant.now(clock)));

    return mapResult(result, HttpStatus.OK);
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(@AuthenticationPrincipal final UUID accountId) {

    final var result =
        deleteUserProfileUseCase.execute(
            new DeleteUserProfileCommand(accountId, Instant.now(clock)));

    return switch (result) {
      case Result.Success<Void, UserError> s -> ResponseEntity.noContent().build();

      case Result.Failure<Void, UserError> f -> mapUserError(f.error().orElseThrow());
    };
  }

  // ---- helpers ----

  private ResponseEntity<UserProfileResponse> mapResult(
      final Result<UserProfileResponse, UserError> result, final HttpStatus successStatus) {

    return switch (result) {
      case Result.Success<UserProfileResponse, UserError> s ->
          ResponseEntity.status(successStatus).body(s.success().orElseThrow());

      case Result.Failure<UserProfileResponse, UserError> f ->
          mapUserError(f.error().orElseThrow());
    };
  }

  private <T> ResponseEntity<T> mapUserError(final UserError error) {
    return switch (error) {
      case UserError.UserProfileAlreadyExists e ->
          ResponseEntity.status(HttpStatus.CONFLICT).build();

      case UserError.UserProfileNotFound e -> ResponseEntity.status(HttpStatus.NOT_FOUND).build();

      case UserError.AccountNotFound e -> ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    };
  }
}
