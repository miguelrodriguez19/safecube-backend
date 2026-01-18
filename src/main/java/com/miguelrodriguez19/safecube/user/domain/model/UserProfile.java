package com.miguelrodriguez19.safecube.user.domain.model;

import com.miguelrodriguez19.safecube.user.domain.exception.InvalidDisplayNameException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = "userId")
public final class UserProfile {

  private static final int MAX_DISPLAY_NAME_LENGTH = 100;

  private final UUID userId;
  private final UUID accountId;
  private String displayName;
  private final Instant createdAt;
  private Instant updatedAt;

  private UserProfile(
      final UUID userId,
      final UUID accountId,
      final String displayName,
      final Instant createdAt,
      final Instant updatedAt) {
    this.userId = userId;
    this.accountId = accountId;
    this.displayName = displayName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static UserProfile of(
      @NotNull final UUID userId,
      @NotNull final UUID accountId,
      @NotBlank final String displayName,
      @NotNull final Instant now) {

    final var validatedName = validateDisplayName(displayName);

    return new UserProfile(userId, accountId, validatedName, now, now);
  }

  public static UserProfile restore(
      @NotNull final UUID userId,
      @NotNull final UUID accountId,
      @NotBlank final String displayName,
      @NotNull final Instant createdAt,
      @NotNull final Instant updatedAt) {

    return new UserProfile(userId, accountId, displayName, createdAt, updatedAt);
  }

  public void updateDisplayName(@NotBlank final String newDisplayName, @NotNull final Instant now) {
    this.displayName = validateDisplayName(newDisplayName);
    this.updatedAt = now;
  }

  private static String validateDisplayName(final String name) {
    final var trimmedName = name.trim();
    if (trimmedName.length() > MAX_DISPLAY_NAME_LENGTH) {
      throw new InvalidDisplayNameException();
    }
    return trimmedName;
  }
}
