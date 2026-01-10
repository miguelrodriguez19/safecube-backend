package unit.com.miguelrodriguez19.safecube.auth.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.auth.domain.model.AuthAccount;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaEntity;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.mapper.AuthAccountMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import unit.annotation.UnitTest;

@UnitTest
class AuthAccountMapperTest {

  private final AuthAccountMapper target = new AuthAccountMapper();

  @Test
  void shouldMapEntityToDomain() {
    final var accountId = UUID.randomUUID();
    final var now = Instant.parse("2026-01-09T10:00:00Z");

    final var entity =
        new AuthAccountJpaEntity(accountId, "test@safecube.io", "hashed-password", true, now, null);

    final var result = target.toDomain(entity);
    assertThat(result).isNotNull();
    assertThat(result.getAccountId()).isEqualTo(accountId);
    assertThat(result.getEmail()).isEqualTo("test@safecube.io");
    assertThat(result.getPasswordHash()).isEqualTo("hashed-password");
    assertThat(result.isEnabled()).isTrue();
    assertThat(result.getCreatedAt()).isEqualTo(now);
    assertThat(result.getDisabledAt()).isNull();
  }

  @Test
  void shouldMapDomainToEntity() {
    final var now = Instant.parse("2026-01-09T10:00:00Z");

    final var domain =
        AuthAccount.restore(
            UUID.randomUUID(), "domain@safecube.io", "hashed-password", true, now, null);

    final var result = target.toEntity(domain);
    assertThat(result).isNotNull();
    assertThat(result.getAccountId()).isEqualTo(domain.getAccountId());
    assertThat(result.getEmail()).isEqualTo(domain.getEmail());
    assertThat(result.getPasswordHash()).isEqualTo(domain.getPasswordHash());
    assertThat(result.isEnabled()).isEqualTo(domain.isEnabled());
    assertThat(result.getCreatedAt()).isEqualTo(domain.getCreatedAt());
    assertThat(result.getDisabledAt()).isEqualTo(domain.getDisabledAt());
  }
}
