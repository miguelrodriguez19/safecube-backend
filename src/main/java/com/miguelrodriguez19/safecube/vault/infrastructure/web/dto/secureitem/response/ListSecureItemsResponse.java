package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * ListSecureItemsResponse
 *
 * <p>HTTP response wrapper for listing SecureItems without payload.
 */
public record ListSecureItemsResponse(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<SecureItemSummaryResponse> items) {}
