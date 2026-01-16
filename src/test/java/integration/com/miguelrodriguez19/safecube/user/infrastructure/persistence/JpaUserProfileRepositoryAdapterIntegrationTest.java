package integration.com.miguelrodriguez19.safecube.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.JpaUserProfileRepositoryAdapter;
import integration.annotation.IntegrationTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

@IntegrationTest(profiles = {"jpa"})
class JpaUserProfileRepositoryAdapterIntegrationTest {

  @Autowired private JpaUserProfileRepositoryAdapter target;

  @Test
  void shouldPersistAndRetrieveUserProfileByAccountId() {
    final var now = Instant.now();

    final var profile = UserProfile.of(UUID.randomUUID(), UUID.randomUUID(), "John", now);

    target.save(profile);

    final var loaded = target.findByAccountId(profile.getAccountId());

    assertThat(loaded).isPresent();
    assertThat(loaded.orElseThrow())
        .extracting(UserProfile::getUserId, UserProfile::getAccountId, UserProfile::getDisplayName)
        .containsExactly(profile.getUserId(), profile.getAccountId(), profile.getDisplayName());
  }

  @Test
  void shouldReturnEmpty_whenUserProfileDoesNotExist() {
    final var missingAccountId = UUID.randomUUID();

    final var result = target.findByAccountId(missingAccountId);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldEnforceUniquenessByAccountId() {
    final var accountId = UUID.randomUUID();
    final var now = Instant.now();

    final var firstProfile = UserProfile.of(UUID.randomUUID(), accountId, "First", now);
    final var secondProfile =
        UserProfile.of(UUID.randomUUID(), accountId, "Second", now.plusSeconds(10));

    target.save(firstProfile);

    assertThatThrownBy(() -> target.save(secondProfile))
        .isInstanceOf(DataIntegrityViolationException.class);

    final var loaded = target.findByAccountId(accountId);
    assertThat(loaded).isPresent();
    assertThat(loaded.get().getDisplayName()).isEqualTo("First");
  }
}
