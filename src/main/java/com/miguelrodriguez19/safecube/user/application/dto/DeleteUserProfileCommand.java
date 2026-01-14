package com.miguelrodriguez19.safecube.user.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DeleteUserProfileCommand
 *
 * <p>Command object carrying the data required to perform a logical deletion of an existing {@code
 * UserProfile}.
 */
public record DeleteUserProfileCommand(UUID accountId, Instant now) {}
