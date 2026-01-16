package com.miguelrodriguez19.safecube.user.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

/** UpdateUserProfileRequest */
public record UpdateUserProfileRequest(@NotBlank String displayName) {}
