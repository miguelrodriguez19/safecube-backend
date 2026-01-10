package unit.com.miguelrodriguez19.safecube.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.auth.infrastructure.security.JwtAccessTokenIssuer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import unit.annotation.UnitTest;

@UnitTest
class JwtAccessTokenIssuerTest {

  private static final String SECRET = "this-is-a-very-long-secret-key-for-hmac-sha";
  private static final String ISSUER = "safecube-test";
  private static final long TTL_SECONDS = 900L;

  private JwtAccessTokenIssuer target;

  @BeforeEach
  void setUp() {
    target = new JwtAccessTokenIssuer(SECRET, ISSUER, TTL_SECONDS);
  }

  @Test
  void shouldIssueValidJwtToken() {
    final var accountId = UUID.randomUUID();
    final var issuedAt = Instant.now();

    final var token = target.issue(accountId, issuedAt);

    final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    final Claims claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

    assertThat(claims.getSubject()).isEqualTo(accountId.toString());
    assertThat(claims.getIssuer()).isEqualTo(ISSUER);
    assertThat(claims.getIssuedAt()).isNotNull();
    assertThat(claims.getExpiration()).isNotNull();
  }
}
