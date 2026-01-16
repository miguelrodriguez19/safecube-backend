package unit.com.miguelrodriguez19.safecube.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.miguelrodriguez19.safecube.user.domain.exception.InvalidDisplayNameException;
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
            UserProfile::getUpdatedAt)
        .containsExactly(
            profile.getUserId(), profile.getAccountId(), profile.getDisplayName(), now, now);
  }

  @Test
  void shouldRestoreUserProfile() {
    final var createdAt = Instant.now();
    final var updatedAt = createdAt.plusSeconds(5);

    final var profile =
        UserProfile.restore(UUID.randomUUID(), UUID.randomUUID(), "John Doe", createdAt, updatedAt);

    assertThat(profile)
        .extracting(
            UserProfile::getUserId,
            UserProfile::getAccountId,
            UserProfile::getDisplayName,
            UserProfile::getCreatedAt,
            UserProfile::getUpdatedAt)
        .containsExactly(
            profile.getUserId(),
            profile.getAccountId(),
            profile.getDisplayName(),
            createdAt,
            updatedAt);
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
  void shouldRejectDisplayNameLongerThanMaxLength() {
    final var now = Instant.now();
    final var tooLongName = "a".repeat(101);

    assertThrows(
        InvalidDisplayNameException.class,
        () -> UserProfile.of(UUID.randomUUID(), UUID.randomUUID(), tooLongName, now));
  }
}
