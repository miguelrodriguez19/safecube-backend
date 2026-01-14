package unit.com.miguelrodriguez19.safecube.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.miguelrodriguez19.safecube.user.domain.exception.InvalidDisplayNameException;
import com.miguelrodriguez19.safecube.user.domain.exception.UserProfileDeletedException;
import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserProfileTest {

  @Test
  void shouldCreateUserProfile() {
    final var now = Instant.now();

    final var profile = UserProfile.of(UUID.randomUUID(), UUID.randomUUID(), "John Doe", now);

    assertThat(profile)
        .extracting(
            UserProfile::getUserId,
            UserProfile::getAccountId,
            UserProfile::getDisplayName,
            UserProfile::getCreatedAt,
            UserProfile::getUpdatedAt,
            UserProfile::getDeletedAt)
        .containsExactly(
            profile.getUserId(), profile.getAccountId(), profile.getDisplayName(), now, now, null);
  }

  @Test
  void shouldRestoreUserProfile() {
    final var createdAt = Instant.now();
    final var updatedAt = createdAt.plusSeconds(5);

    final var profile =
        UserProfile.restore(
            UUID.randomUUID(), UUID.randomUUID(), "John Doe", createdAt, updatedAt, null);

    assertThat(profile)
        .extracting(
            UserProfile::getUserId,
            UserProfile::getAccountId,
            UserProfile::getDisplayName,
            UserProfile::getCreatedAt,
            UserProfile::getUpdatedAt,
            UserProfile::getDeletedAt)
        .containsExactly(
            profile.getUserId(),
            profile.getAccountId(),
            profile.getDisplayName(),
            createdAt,
            updatedAt,
            null);
  }

  @Test
  void shouldUpdateDisplayName() {
    final var now = Instant.now();
    final var later = now.plusSeconds(10);

    final var profile = UserProfile.of(UUID.randomUUID(), UUID.randomUUID(), "John", now);

    final var newDisplayName = "John Doe";
    profile.updateDisplayName(newDisplayName, later);

    assertEquals(newDisplayName, profile.getDisplayName());
    assertEquals(later, profile.getUpdatedAt());
  }

  @Test
  void shouldDeleteUserProfile() {
    final var now = Instant.now();
    final var later = now.plusSeconds(5);

    final var profile = UserProfile.of(UUID.randomUUID(), UUID.randomUUID(), "John Doe", now);

    profile.delete(later);

    assertTrue(profile.isDeleted());
    assertEquals(later, profile.getDeletedAt());
    assertEquals(later, profile.getUpdatedAt());
  }

  @Test
  void shouldNotAllowUpdate_whenProfileIsDeleted() {
    final var now = Instant.now();

    final var profile = UserProfile.of(UUID.randomUUID(), UUID.randomUUID(), "John Doe", now);

    profile.delete(now.plusSeconds(5));

    assertThrows(
        UserProfileDeletedException.class,
        () -> profile.updateDisplayName("John Miller", now.plusSeconds(10)));
  }

  @Test
  void shouldNotAllowDeleteTwice() {
    final var now = Instant.now();

    final var profile = UserProfile.of(UUID.randomUUID(), UUID.randomUUID(), "John Doe", now);

    profile.delete(now.plusSeconds(5));

    assertThrows(UserProfileDeletedException.class, () -> profile.delete(now.plusSeconds(10)));
  }

  @Test
  void shouldRejectDisplayNameLongerThanMaxLength() {
    final var now = Instant.now();
    final var tooLongName = "a".repeat(101);

    assertThrows(
        InvalidDisplayNameException.class,
        () -> UserProfile.of(UUID.randomUUID(), UUID.randomUUID(), tooLongName, now));
  }
}
