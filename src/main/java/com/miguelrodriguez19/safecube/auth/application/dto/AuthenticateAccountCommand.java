package com.miguelrodriguez19.safecube.auth.application.dto;

/**
 * AuthenticateAccountCommand
 *
 * <p>Represents the intent to authenticate an existing account.
 */
public record AuthenticateAccountCommand(String email, String rawPassword) {}
