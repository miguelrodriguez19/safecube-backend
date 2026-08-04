package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ListSecureItemChangesResponse(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<SecureItemChangeResponse> items,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long nextCursor,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean hasMore) {}
