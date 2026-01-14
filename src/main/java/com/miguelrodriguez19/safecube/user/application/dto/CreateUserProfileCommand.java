package com.miguelrodriguez19.safecube.user.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * CreateUserProfileCommand
 *
 * <p>Represents the intent of create a user
 */
public record CreateUserProfileCommand(
    UUID userId, UUID accountId, String displayName, Instant now) {}
