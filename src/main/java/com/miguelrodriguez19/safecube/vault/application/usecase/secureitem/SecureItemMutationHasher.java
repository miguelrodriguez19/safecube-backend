package com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.CreateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.DeleteSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.UpdateSecureItemCommand;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class SecureItemMutationHasher {

  public String hash(final CreateSecureItemCommand command) {
    final var digest = newDigest();
    update(digest, "CREATE");
    update(digest, command.accountId().toString());
    update(digest, command.itemTypeDto().name());
    update(digest, command.schemaVersion());
    update(digest, command.displayHint());
    update(digest, command.payload());
    update(digest, command.payloadVersion());
    return HexFormat.of().formatHex(digest.digest());
  }

  public String hash(final UpdateSecureItemCommand command) {
    final var digest = newDigest();
    update(digest, "UPDATE");
    update(digest, command.accountId().toString());
    update(digest, command.itemId().toString());
    update(digest, command.itemTypeDto().name());
    update(digest, command.schemaVersion());
    update(digest, command.displayHint());
    update(digest, command.payload());
    update(digest, command.payloadVersion());
    update(digest, command.expectedItemRevision());
    return HexFormat.of().formatHex(digest.digest());
  }

  public String hash(final DeleteSecureItemCommand command) {
    final var digest = newDigest();
    update(digest, "DELETE");
    update(digest, command.accountId().toString());
    update(digest, command.itemId().toString());
    update(digest, command.expectedItemRevision());
    return HexFormat.of().formatHex(digest.digest());
  }

  private MessageDigest newDigest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (final NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  private void update(final MessageDigest digest, final String value) {
    update(digest, value.getBytes(StandardCharsets.UTF_8));
  }

  private void update(final MessageDigest digest, final byte[] value) {
    digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(value.length).array());
    digest.update(value);
  }

  private void update(final MessageDigest digest, final long value) {
    digest.update(ByteBuffer.allocate(Long.BYTES).putLong(value).array());
  }
}
