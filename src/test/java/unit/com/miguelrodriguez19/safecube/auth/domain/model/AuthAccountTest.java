package unit.com.miguelrodriguez19.safecube.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.auth.domain.model.AuthAccount;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import unit.annotation.UnitTest;

/**
 * AuthAccountTest
 *
 * <p>Unit tests for {@link AuthAccount}.
 */
@UnitTest
class AuthAccountTest {

  @Test
  void shouldCreateEnabledAccount_whenUsingOfFactory() {
    final var email = "user@safecube.io";
    final var passwordHash = "hashed-password";
    final var createdAt = Instant.parse("2026-01-09T10:00:00Z");

    final var account = AuthAccount.of(email, passwordHash, createdAt);

    assertThat(account)
        .extracting(
            AuthAccount::getEmail,
            AuthAccount::getPasswordHash,
            AuthAccount::isEnabled,
            AuthAccount::getCreatedAt,
            AuthAccount::getDisabledAt)
        .containsExactly(email, passwordHash, true, createdAt, null);

    assertThat(account.getAccountId()).isNotNull();
  }

  @Test
  void shouldRestoreAccountWithProvidedState() {
    final var accountId = UUID.randomUUID();
    final var email = "restored@safecube.io";
    final var passwordHash = "hashed-password";
    final var createdAt = Instant.parse("2026-01-01T00:00:00Z");
    final var disabledAt = Instant.parse("2026-01-05T00:00:00Z");

    final var account =
        AuthAccount.restore(accountId, email, passwordHash, false, createdAt, disabledAt);

    assertThat(account)
        .extracting(
            AuthAccount::getAccountId,
            AuthAccount::getEmail,
            AuthAccount::getPasswordHash,
            AuthAccount::isEnabled,
            AuthAccount::getCreatedAt,
            AuthAccount::getDisabledAt)
        .containsExactly(accountId, email, passwordHash, false, createdAt, disabledAt);
  }

  @Test
  void shouldDisableAccount_whenEnabled() {
    final var account =
        AuthAccount.of(
            "disable@safecube.io", "hashed-password", Instant.parse("2026-01-09T10:00:00Z"));

    final var disabledAt = Instant.parse("2026-01-10T10:00:00Z");

    account.disable(disabledAt);

    assertThat(account)
        .extracting(AuthAccount::isEnabled, AuthAccount::getDisabledAt)
        .containsExactly(false, disabledAt);
  }

  @Test
  void shouldNotChangeState_whenDisablingAlreadyDisabledAccount() {
    final var originalDisabledAt = Instant.parse("2026-01-05T00:00:00Z");

    final var account =
        AuthAccount.restore(
            UUID.randomUUID(),
            "already-disabled@safecube.io",
            "hashed-password",
            false,
            Instant.parse("2026-01-01T00:00:00Z"),
            originalDisabledAt);

    final var newDisabledAt = Instant.parse("2026-01-10T00:00:00Z");

    account.disable(newDisabledAt);

    assertThat(account)
        .extracting(AuthAccount::isEnabled, AuthAccount::getDisabledAt)
        .containsExactly(false, originalDisabledAt);
  }
}
