package com.miguelrodriguez19.safecube.auth.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * AuthenticateAccountResult
 *
 * <p>Result returned after a successful authentication.
 */
public record AuthenticateAccountResult(UUID accountId, Instant authenticatedAt) {}
