package unit.com.miguelrodriguez19.safecube.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.auth.infrastructure.security.JwtTokenParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import unit.annotation.UnitTest;

@UnitTest
class JwtTokenParserTest {

  private static final String SECRET = "this-is-a-very-long-secret-key-for-hmac-sha";

  @Test
  void shouldExtractAccountId_fromValidToken() {
    final var accountId = UUID.randomUUID();

    final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    final var token =
        Jwts.builder()
            .subject(accountId.toString())
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(60)))
            .signWith(key)
            .compact();

    final var target = new JwtTokenParser(SECRET);

    final Optional<UUID> result = target.extractAccountId(token);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(accountId);
  }

  @Test
  void shouldReturnEmpty_whenTokenIsInvalid() {
    final var target = new JwtTokenParser(SECRET);

    final var result = target.extractAccountId("invalid.token.value");

    assertThat(result).isEmpty();
  }
}
