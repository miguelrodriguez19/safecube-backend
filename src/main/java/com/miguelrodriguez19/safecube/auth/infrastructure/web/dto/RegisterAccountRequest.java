package com.miguelrodriguez19.safecube.auth.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * RegisterAccountRequest
 *
 * <p>HTTP request payload for account registration.
 */
public record RegisterAccountRequest(@NotBlank @Email String email, @NotBlank String password) {}
