package com.miguelrodriguez19.safecube.user.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** CreateUserProfileRequest */
@Schema(description = "Request to create a user profile for the authenticated account.")
public record CreateUserProfileRequest(
    @Schema(description = "Public display name shown in the UI.", minLength = 1, maxLength = 100)
        @NotBlank String displayName) {}
