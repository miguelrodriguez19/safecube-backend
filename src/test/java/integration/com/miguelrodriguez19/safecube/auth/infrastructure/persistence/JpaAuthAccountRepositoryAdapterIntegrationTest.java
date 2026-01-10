package integration.com.miguelrodriguez19.safecube.auth.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.auth.domain.model.AuthAccount;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.JpaAuthAccountRepositoryAdapter;
import integration.annotation.IntegrationTest;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * JpaAuthAccountRepositoryAdapterIntegrationTest
 *
 * <p>Integration tests for {@link JpaAuthAccountRepositoryAdapter}.
 *
 * <p>Each test is fully self-contained and relies on a real PostgreSQL container.
 */
@IntegrationTest(profiles = {"jpa"})
class JpaAuthAccountRepositoryAdapterIntegrationTest {

  @Autowired private JpaAuthAccountRepositoryAdapter target;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void shouldPersistAndRetrieveAccountByEmail() {
    // given
    final var account = AuthAccount.of("integration@safecube.io", "hash", Instant.now());

    // when
    target.save(account);

    // then
    final var loaded = target.findByEmail(account.getEmail());

    assertThat(loaded).isPresent();
    assertThat(loaded.orElseThrow().getAccountId()).isEqualTo(account.getAccountId());
  }

  @Test
  void shouldReturnTrue_whenAccountExistsByEmail() {
    // given
    final var email = "exists@safecube.io";

    target.save(AuthAccount.of(email, "hash", Instant.now()));

    // when
    final var exists = target.existsByEmail(email);

    // then
    assertThat(exists).isTrue();
  }

  @Test
  void shouldReturnEmpty_whenAccountDoesNotExist() {
    // when
    final var result = target.findByEmail("missing@safecube.io");

    // then
    assertThat(result).isEmpty();
  }
}
