package com.miguelrodriguez19.safecube.auth.infrastructure.web;

import com.miguelrodriguez19.safecube.auth.application.dto.AuthenticateAccountCommand;
import com.miguelrodriguez19.safecube.auth.application.dto.RegisterAccountCommand;
import com.miguelrodriguez19.safecube.auth.application.dto.RegisterAccountResult;
import com.miguelrodriguez19.safecube.auth.application.error.AuthError;
import com.miguelrodriguez19.safecube.auth.application.usecase.AuthenticateAccountUseCase;
import com.miguelrodriguez19.safecube.auth.application.usecase.IssueTokensUseCase;
import com.miguelrodriguez19.safecube.auth.application.usecase.LogoutUseCase;
import com.miguelrodriguez19.safecube.auth.application.usecase.RefreshTokensUseCase;
import com.miguelrodriguez19.safecube.auth.application.usecase.RegisterAccountUseCase;
import com.miguelrodriguez19.safecube.auth.infrastructure.security.RefreshTokenHasher;
import com.miguelrodriguez19.safecube.auth.infrastructure.web.dto.AuthTokensResponse;
import com.miguelrodriguez19.safecube.auth.infrastructure.web.dto.AuthenticateAccountRequest;
import com.miguelrodriguez19.safecube.auth.infrastructure.web.dto.RefreshTokenRequest;
import com.miguelrodriguez19.safecube.auth.infrastructure.web.dto.RegisterAccountRequest;
import com.miguelrodriguez19.safecube.shared.result.Result;
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

  private final IssueTokensUseCase issueTokensUseCase;
  private final RefreshTokensUseCase refreshTokensUseCase;
  private final LogoutUseCase logoutUseCase;

  private final RefreshTokenHasher refreshTokenHasher;
  private final Clock clock;

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
  public ResponseEntity<AuthTokensResponse> login(
      @Valid @RequestBody final AuthenticateAccountRequest request) {

    final var authResult =
        authenticateAccountUseCase.execute(
            new AuthenticateAccountCommand(request.email(), request.password()));

    if (authResult.isFailure()) {
      return mapAuthError(authResult.error().orElseThrow());
    }

    final var accountId = authResult.success().orElseThrow().accountId();

    final var issuedAt = Instant.now(clock);

    final var rawRefreshToken = UUID.randomUUID().toString();
    final var refreshTokenHash = refreshTokenHasher.hash(rawRefreshToken);

    final var tokenResult =
        issueTokensUseCase.execute(
            accountId,
            rawRefreshToken,
            refreshTokenHash,
            issuedAt,
            issuedAt.plusSeconds(60 * 60 * 24 * 30L)); // 30 days

    final var tokens = tokenResult.success().orElseThrow();

    return ResponseEntity.ok(
        new AuthTokensResponse(tokens.accessToken(), tokens.refreshToken(), tokens.issuedAt()));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthTokensResponse> refresh(
      @Valid @RequestBody final RefreshTokenRequest request) {

    final var issuedAt = Instant.now(clock);

    final var refreshTokenHash = refreshTokenHasher.hash(request.refreshToken());

    final var newRawRefreshToken = UUID.randomUUID().toString();
    final var newRefreshTokenHash = refreshTokenHasher.hash(newRawRefreshToken);

    final var result =
        refreshTokensUseCase.execute(
            refreshTokenHash,
            newRawRefreshToken,
            newRefreshTokenHash,
            issuedAt,
            issuedAt.plusSeconds(60 * 60 * 24 * 30L)); // 30 days

    if (result.isFailure()) {
      return mapAuthError(result.error().orElseThrow());
    }

    final var tokens = result.success().orElseThrow();

    return ResponseEntity.ok(
        new AuthTokensResponse(tokens.accessToken(), tokens.refreshToken(), tokens.issuedAt()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @AuthenticationPrincipal(expression = "this") final UUID accountId) {

    logoutUseCase.execute(accountId, Instant.now(clock));
    return ResponseEntity.noContent().build();
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
