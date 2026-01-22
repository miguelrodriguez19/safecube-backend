package integration.com.miguelrodriguez19.safecube.auth.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.auth.application.port.out.RefreshTokenRecord;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.JpaRefreshTokenRepositoryAdapter;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaEntity;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaRepository;
import integration.annotation.IntegrationTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest(profiles = {"jpa"})
class JpaRefreshTokenRepositoryAdapterIntegrationTest {

  @Autowired private AuthAccountJpaRepository authAccountJpaRepository;
  @Autowired private JpaRefreshTokenRepositoryAdapter target;

  @Test
  void shouldPersistAndLoadRefreshToken() {
    final var tokenId = UUID.randomUUID();
    final var accountId = insertAuthAccount();
    final var now = Instant.now();

    target.save(tokenId, accountId, "hash", now.plusSeconds(3600), now);

    final var result = target.findByTokenHash("hash");

    assertThat(result).isPresent();

    final RefreshTokenRecord token = result.orElseThrow();
    assertThat(token.tokenId()).isEqualTo(tokenId);
    assertThat(token.accountId()).isEqualTo(accountId);
  }

  @Test
  void shouldRevokeAllTokensForAccount() {
    final var accountId = insertAuthAccount();
    final var now = Instant.now();

    target.save(UUID.randomUUID(), accountId, "hash-1", now.plusSeconds(3600), now);

    target.revokeAllByAccountId(accountId, now);

    final var token = target.findByTokenHash("hash-1").orElseThrow();
    assertThat(token.revokedAt()).isNotNull();
  }

  private UUID insertAuthAccount() {
    final var accountId = UUID.randomUUID();
    final var email = "%s@safecube.io".formatted(accountId);
    final var now = Instant.now();

    final var authAccountJpaEntity =
        new AuthAccountJpaEntity(accountId, email, "password", true, now, null);
    authAccountJpaRepository.save(authAccountJpaEntity);

    return accountId;
  }
}
