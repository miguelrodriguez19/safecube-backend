package com.miguelrodriguez19.safecube.auth.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JwtTokenParser
 *
 * <p>Parses and validates JWT access tokens.
 */
@Slf4j
@Component
public class JwtTokenParser {

  private final SecretKey key;

  public JwtTokenParser(@Value("${security.jwt.secret}") final String secret) {

    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Extracts accountId from a JWT token.
   *
   * @param token raw JWT
   * @return accountId if token is valid
   */
  public Optional<UUID> extractAccountId(final String token) {
    try {
      final Claims claims =
          Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      return Optional.of(UUID.fromString(claims.getSubject()));
    } catch (final Exception ex) {
      log.debug("Invalid JWT token", ex);
      return Optional.empty();
    }
  }
}
