package com.miguelrodriguez19.safecube.auth.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/**
 * AuthAccount
 *
 * <p>Domain entity representing an authenticatable account. Models credentials and access status,
 * not user profile.
 */
@Getter
public class AuthAccount {

  private final UUID accountId;
  private final String email;
  private final String passwordHash;
  private boolean enabled;
  private final Instant createdAt;
  private Instant disabledAt;

  private AuthAccount(
      final UUID accountId,
      final String email,
      final String passwordHash,
      final boolean enabled,
      final Instant createdAt,
      final Instant disabledAt) {
    this.accountId = accountId;
    this.email = email;
    this.passwordHash = passwordHash;
    this.enabled = enabled;
    this.createdAt = createdAt;
    this.disabledAt = disabledAt;
  }

  public static AuthAccount of(
      @NotBlank final String email,
      @NotBlank final String passwordHash,
      @NotNull final Instant createdAt) {

    return new AuthAccount(UUID.randomUUID(), email, passwordHash, true, createdAt, null);
  }

  public void disable(@NotNull final Instant disabledAt) {
    if (!this.enabled) {
      return;
    }
    this.enabled = false;
    this.disabledAt = disabledAt;
  }
}
