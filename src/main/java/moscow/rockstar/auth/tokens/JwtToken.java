package moscow.rockstar.auth.tokens;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import moscow.rockstar.util.scheduling.TimedState;

/** Decoded JWT parts used by the authentication flow. */
public final class JwtToken implements TimedState {
    private final JsonObjectToken header;
    private final JsonObjectToken payload;
    private final byte[] signature;

    public static JwtToken fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("JWT string is null");
        }
        String[] parts = value.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("JWT must have at least header and payload");
        }
        JsonObjectToken header = JsonTokenParser.parse(new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8)).asObject();
        JsonObjectToken payload = JsonTokenParser.parse(new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)).asObject();
        byte[] signature = parts.length > 2 ? Base64.getUrlDecoder().decode(parts[2]) : null;
        return new JwtToken(header, payload, signature);
    }

    @Override
    public long getExpiresAtMillis() {
        return payload.containsNumber("exp") ? payload.getLong("exp") * 1000L : Long.MAX_VALUE;
    }

    public JwtToken(JsonObjectToken header, JsonObjectToken payload, byte[] signature) {
        this.header = header;
        this.payload = payload;
        this.signature = signature;
    }

    public JsonObjectToken getHeader() {
        return header;
    }

    public JsonObjectToken getPayload() {
        return payload;
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof JwtToken jwt)) return false;
        return header.equals(jwt.header) && payload.equals(jwt.payload) && Arrays.equals(signature, jwt.signature);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * header.hashCode() + payload.hashCode()) + Arrays.hashCode(signature);
    }

    @Override
    public String toString() {
        return "Jwt(header=" + header + ", payload=" + payload + ", signature=" + Arrays.toString(signature) + ")";
    }
}
