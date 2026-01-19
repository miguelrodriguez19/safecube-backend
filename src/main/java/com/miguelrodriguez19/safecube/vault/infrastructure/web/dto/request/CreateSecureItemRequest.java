package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.request;

import com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.annotation.ValidItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * CreateSecureItemRequest
 *
 * <p>HTTP request payload for creating a new SecureItem.
 */
public record CreateSecureItemRequest(
    @NotNull @ValidItemType String itemType,
    @NotNull Integer schemaVersion,
    @NotBlank @Size(max = 255) String displayHint,
    @NotNull byte[] payload) {}
