package com.miguelrodriguez19.safecube.vault.domain.model.secureitem;

import com.miguelrodriguez19.safecube.vault.domain.exception.InvalidPayloadException;
import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = "itemId")
public class SecureItem {

  private static final long MAX_PAYLOAD_SIZE_BYTES = 1_048_576; // 1 MB

  private final UUID itemId;
  private final UUID accountId;
  private final ItemType itemType;
  private final int schemaVersion;
  private final String displayHint;
  private final byte[] payload;
  private final long payloadVersion;
  private final Instant createdAt;
  private final Instant updatedAt;
  private final Instant deletedAt;

  private SecureItem(
      final UUID itemId,
      final UUID accountId,
      final ItemType itemType,
      final int schemaVersion,
      final String displayHint,
      final byte[] payload,
      final long payloadVersion,
      final Instant createdAt,
      final Instant updatedAt,
      final Instant deletedAt) {
    validatePayload(payload);

    this.itemId = itemId;
    this.accountId = accountId;
    this.itemType = itemType;
    this.schemaVersion = schemaVersion;
    this.displayHint = displayHint;
    this.payload = payload;
    this.payloadVersion = payloadVersion;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.deletedAt = deletedAt;
  }

  public static SecureItem of(
      final UUID itemId,
      final UUID accountId,
      final ItemType itemType,
      final int schemaVersion,
      final String displayHint,
      final byte[] payload,
      final Instant createdAt) {
    return new SecureItem(
        itemId,
        accountId,
        itemType,
        schemaVersion,
        displayHint,
        payload,
        1L,
        createdAt,
        createdAt,
        null);
  }

  public static SecureItem restore(
      final UUID itemId,
      final UUID accountId,
      final ItemType itemType,
      final int schemaVersion,
      final String displayHint,
      final byte[] payload,
      final long payloadVersion,
      final Instant createdAt,
      final Instant updatedAt,
      final Instant deletedAt) {
    return new SecureItem(
        itemId,
        accountId,
        itemType,
        schemaVersion,
        displayHint,
        payload,
        payloadVersion,
        createdAt,
        updatedAt,
        deletedAt);
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  private static void validatePayload(final byte[] payload) {
    if (payload == null) {
      throw new InvalidPayloadException("Payload must not be null");
    }

    if (payload.length == 0) {
      throw new InvalidPayloadException("Payload must not be empty");
    }

    if (payload.length > MAX_PAYLOAD_SIZE_BYTES) {
      throw new InvalidPayloadException("Payload exceeds maximum allowed size");
    }
  }
}
