package com.miguelrodriguez19.safecube.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * AuthenticateAccountRequest
 *
 * <p>HTTP request payload for account authentication.
 */
@Schema(description = "Authentication request using email and password.")
public record AuthenticateAccountRequest(
    @Schema(description = "Account email address.", format = "email") @NotBlank @Email String email,
    @Schema(description = "Account password.", accessMode = Schema.AccessMode.WRITE_ONLY) @NotBlank String password) {}
