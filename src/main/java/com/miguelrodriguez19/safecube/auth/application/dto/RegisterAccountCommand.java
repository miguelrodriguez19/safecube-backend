package com.miguelrodriguez19.safecube.auth.application.dto;

/**
 * RegisterAccountCommand
 *
 * <p>Represents the intent to register a new authentication account.
 */
public record RegisterAccountCommand(String email, String rawPassword) {}
