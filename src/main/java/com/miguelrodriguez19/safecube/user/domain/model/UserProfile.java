package com.miguelrodriguez19.safecube.user.domain.model;

import com.miguelrodriguez19.safecube.user.domain.exception.InvalidDisplayNameException;
import com.miguelrodriguez19.safecube.user.domain.exception.UserProfileDeletedException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public final class UserProfile {

  private static final int MAX_DISPLAY_NAME_LENGTH = 100;

  private final UUID userId;
  private final UUID accountId;
  private String displayName;
  private final Instant createdAt;
  private Instant updatedAt;
  private Instant deletedAt;

  private UserProfile(
      final UUID userId,
      final UUID accountId,
      final String displayName,
      final Instant createdAt,
      final Instant updatedAt,
      final Instant deletedAt) {
    this.userId = userId;
    this.accountId = accountId;
    this.displayName = displayName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.deletedAt = deletedAt;
  }

  public static UserProfile of(
      @NotNull final UUID userId,
      @NotNull final UUID accountId,
      @NotBlank final String displayName,
      @NotNull final Instant now) {

    validateDisplayName(displayName);

    return new UserProfile(userId, accountId, displayName, now, now, null);
  }

  public static UserProfile restore(
      @NotNull final UUID userId,
      @NotNull final UUID accountId,
      @NotBlank final String displayName,
      @NotNull final Instant createdAt,
      @NotNull final Instant updatedAt,
      final Instant deletedAt) {

    return new UserProfile(userId, accountId, displayName, createdAt, updatedAt, deletedAt);
  }

  public void updateDisplayName(@NotBlank final String newDisplayName, @NotNull final Instant now) {
    assertNotDeleted();
    this.displayName = newDisplayName;
    this.updatedAt = now;
  }

  public void delete(@NotNull final Instant now) {
    assertNotDeleted();
    this.deletedAt = now;
    this.updatedAt = now;
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  private void assertNotDeleted() {
    if (isDeleted()) {
      throw new UserProfileDeletedException();
    }
  }

  private static String validateDisplayName(final String name) {
    if (name.length() > MAX_DISPLAY_NAME_LENGTH) {
      throw new InvalidDisplayNameException();
    }
    return name;
  }
}
