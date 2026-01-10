package com.miguelrodriguez19.safecube.auth.infrastructure.security;

import com.miguelrodriguez19.safecube.auth.application.port.out.AccessTokenIssuer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JwtAccessTokenIssuer
 *
 * <p>JWT-based implementation of {@link AccessTokenIssuer}.
 */
@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuer {

  private final SecretKey key;

  private final String issuer;
  private final long ttlSeconds;

  public JwtAccessTokenIssuer(
      @Value("${security.jwt.secret}") final String secret,
      @Value("${security.jwt.issuer}") final String issuer,
      @Value("${security.jwt.ttl-seconds}") final long ttlSeconds) {

    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.issuer = issuer;
    this.ttlSeconds = ttlSeconds;
  }

  @Override
  public String issue(final UUID accountId, final Instant issuedAt) {

    return Jwts.builder()
        .issuer(issuer)
        .subject(accountId.toString())
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(issuedAt.plusSeconds(ttlSeconds)))
        .signWith(key)
        .compact();
  }
}
