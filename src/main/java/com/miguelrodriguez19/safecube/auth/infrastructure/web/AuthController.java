package com.miguelrodriguez19.safecube.auth.infrastructure.web;

import com.miguelrodriguez19.safecube.auth.application.dto.AuthenticateAccountCommand;
import com.miguelrodriguez19.safecube.auth.application.dto.AuthenticateAccountResult;
import com.miguelrodriguez19.safecube.auth.application.dto.RegisterAccountCommand;
import com.miguelrodriguez19.safecube.auth.application.dto.RegisterAccountResult;
import com.miguelrodriguez19.safecube.auth.application.error.AuthError;
import com.miguelrodriguez19.safecube.auth.application.usecase.AuthenticateAccountUseCase;
import com.miguelrodriguez19.safecube.auth.application.usecase.RegisterAccountUseCase;
import com.miguelrodriguez19.safecube.auth.infrastructure.web.dto.AuthenticateAccountRequest;
import com.miguelrodriguez19.safecube.auth.infrastructure.web.dto.RegisterAccountRequest;
import com.miguelrodriguez19.safecube.shared.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController
 *
 * <p>HTTP adapter for authentication-related operations.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final RegisterAccountUseCase registerAccountUseCase;
  private final AuthenticateAccountUseCase authenticateAccountUseCase;

  @PostMapping("/register")
  public ResponseEntity<RegisterAccountResult> register(
      @Valid @NotNull @RequestBody final RegisterAccountRequest request) {

    final var result =
        registerAccountUseCase.execute(
            new RegisterAccountCommand(request.email(), request.password()));

    return switch (result) {
      case Result.Success<RegisterAccountResult, AuthError> s ->
          ResponseEntity.ok(s.success().orElseThrow());

      case Result.Failure<RegisterAccountResult, AuthError> f ->
          mapAuthError(f.error().orElseThrow());
    };
  }

  @PostMapping("/login")
  public ResponseEntity<AuthenticateAccountResult> login(
      @Valid @NotNull @RequestBody final AuthenticateAccountRequest request) {
    final var result =
        authenticateAccountUseCase.execute(
            new AuthenticateAccountCommand(request.email(), request.password()));

    return switch (result) {
      case Result.Success<AuthenticateAccountResult, AuthError> s ->
          ResponseEntity.ok(s.success().orElseThrow());

      case Result.Failure<AuthenticateAccountResult, AuthError> f ->
          mapAuthError(f.error().orElseThrow());
    };
  }

  private <T> ResponseEntity<T> mapAuthError(final AuthError error) {
    return switch (error) {
      case AuthError.AccountAlreadyExists e -> ResponseEntity.status(HttpStatus.CONFLICT).build();
      case AuthError.InvalidCredentials e -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      case AuthError.AccountNotFound e -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      case AuthError.AccountDisabled e -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    };
  }
}
