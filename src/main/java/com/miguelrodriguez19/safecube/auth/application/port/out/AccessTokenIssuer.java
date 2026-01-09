package com.miguelrodriguez19.safecube.auth.application.port.out;

import java.time.Instant;
import java.util.UUID;

/**
 * AccessTokenIssuer
 *
 * <p>Application port responsible for issuing access tokens.
 *
 * <p>The concrete implementation may use JWT or any other
 * token format, but the application layer remains agnostic.
 */
public interface AccessTokenIssuer {

  /**
   * Issues a new access token for the given account.
   *
   * @param accountId authenticated account identifier
   * @param issuedAt issue instant
   * @return serialized access token
   */
  String issue(final UUID accountId, final Instant issuedAt);
}
