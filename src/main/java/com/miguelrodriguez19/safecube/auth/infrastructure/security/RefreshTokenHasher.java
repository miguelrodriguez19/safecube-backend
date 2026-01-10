package com.miguelrodriguez19.safecube.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RefreshTokenHasher
 *
 * <p>Hashes refresh tokens using HMAC-SHA256 with a server-side secret.
 */
@Component
public class RefreshTokenHasher {

  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private final byte[] secret;

  public RefreshTokenHasher(
          @Value("${security.refresh-token.secret}") final String secret) {
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
  }

  @SneakyThrows
  public String hash(final String rawToken) {
    final Mac mac = Mac.getInstance(HMAC_ALGORITHM);
    mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
    final byte[] result =
            mac.doFinal(rawToken.getBytes(StandardCharsets.UTF_8));
    return bytesToHex(result);
  }

  private String bytesToHex(final byte[] bytes) {
    final var sb = new StringBuilder(bytes.length * 2);
    for (final byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
