package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.response;

import java.util.List;

/**
 * ListSecureItemsResponse
 *
 * <p>HTTP response wrapper for listing SecureItems without payload.
 */
public record ListSecureItemsResponse(List<SecureItemSummaryResponse> items) {}
