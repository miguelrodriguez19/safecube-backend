package com.miguelrodriguez19.safecube.auth.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * AuthenticateAccountRequest
 *
 * <p>HTTP request payload for account authentication.
 */
public record AuthenticateAccountRequest(
    @NotBlank @Email String email, @NotBlank String password) {}
