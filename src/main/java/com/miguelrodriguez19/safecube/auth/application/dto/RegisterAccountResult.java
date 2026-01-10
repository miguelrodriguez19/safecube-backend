package com.miguelrodriguez19.safecube.auth.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * RegisterAccountResult
 *
 * <p>Result returned after a successful account registration.
 */
public record RegisterAccountResult(UUID accountId, Instant createdAt) {}
