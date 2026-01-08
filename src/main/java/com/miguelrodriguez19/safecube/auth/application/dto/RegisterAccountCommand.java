package com.miguelrodriguez19.safecube.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * RegisterAccountCommand
 *
 * <p>Represents the intent to register a new authentication account.
 */
public record RegisterAccountCommand(@NotBlank @Email String email, @NotBlank String rawPassword) {}
