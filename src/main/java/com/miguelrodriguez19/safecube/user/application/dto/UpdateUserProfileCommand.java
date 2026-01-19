package com.miguelrodriguez19.safecube.user.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * UpdateUserProfileCommand
 *
 * <p>Command object carrying the data required to update an existing {@code UserProfile}.
 */
public record UpdateUserProfileCommand(UUID accountId, String displayName, Instant updatedAt) {}
