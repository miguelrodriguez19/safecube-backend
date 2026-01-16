package com.miguelrodriguez19.safecube.user.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * UserProfileResponse
 *
 * <p>HTTP response representation of a user profile.
 */
public record UserProfileResponse(
    UUID userId, UUID accountId, String displayName, Instant createdAt, Instant updatedAt) {}
