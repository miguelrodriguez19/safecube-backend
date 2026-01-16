package com.miguelrodriguez19.safecube.user.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

/** CreateUserProfileRequest */
public record CreateUserProfileRequest(@NotBlank String displayName) {}
