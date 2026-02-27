package com.nicked.nick;

import java.util.Objects;

/**
 * Immutable representation of a Minecraft player skin sourced from the Mojang API.
 *
 * <p>{@code value} is the Base64-encoded JSON texture blob. {@code signature} is
 * the Mojang-signed cryptographic signature of the value; it is required for the
 * client to accept and render the skin. When the API does not return a signature
 * (e.g., for offline-mode players or unsigned profiles) an empty string is stored
 * and the client may fall back to the default skin.</p>
 */
public record SkinData(String value, String signature) {

    public SkinData {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(signature, "signature");
    }
}
