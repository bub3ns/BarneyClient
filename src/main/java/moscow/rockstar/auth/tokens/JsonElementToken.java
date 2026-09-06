package moscow.rockstar.auth.tokens;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Typed view over a Gson value used by the authentication and HTTP codecs.
 *
 * <p>This is the deobfuscated counterpart of the original JSON value wrapper;
 * it is deliberately separate from the PlayFab authentication records.</p>
 */
public class JsonElementToken {
    private final JsonElement value;

    public static JsonElementToken fromJsonElement(JsonElement value) {
        JsonElement normalized = value == null ? JsonNull.INSTANCE : value;
        if (normalized.isJsonObject()) {
            return new JsonObjectToken(normalized.getAsJsonObject());
        }
        if (normalized.isJsonArray()) {
            return new JsonArrayToken(normalized.getAsJsonArray());
        }
        if (normalized.isJsonPrimitive()) {
            return new JsonPrimitiveToken(normalized.getAsJsonPrimitive());
        }
        return new JsonElementToken(normalized);
    }

    protected JsonElementToken(JsonElement value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public JsonElement getValue() {
        return value;
    }

    public JsonElementToken copy() {
        return fromJsonElement(value.deepCopy());
    }

    public boolean isObject() {
        return value.isJsonObject();
    }

    public boolean isArray() {
        return value.isJsonArray();
    }

    public boolean isPrimitive() {
        return value.isJsonPrimitive();
    }

    public boolean isNull() {
        return value.isJsonNull();
    }

    public JsonObjectToken asObject() {
        if (this instanceof JsonObjectToken) {
            return (JsonObjectToken) this;
        }
        return new JsonObjectToken(value.getAsJsonObject());
    }

    public JsonArrayToken asArray() {
        if (this instanceof JsonArrayToken) {
            return (JsonArrayToken) this;
        }
        return new JsonArrayToken(value.getAsJsonArray());
    }

    public JsonPrimitiveToken asPrimitive() {
        if (this instanceof JsonPrimitiveToken) {
            return (JsonPrimitiveToken) this;
        }
        return new JsonPrimitiveToken(value.getAsJsonPrimitive());
    }

    public boolean asBoolean() {
        return value.getAsBoolean();
    }

    public byte asByte() {
        return value.getAsByte();
    }

    public short asShort() {
        return value.getAsShort();
    }

    public int asInt() {
        return value.getAsInt();
    }

    public long asLong() {
        return value.getAsLong();
    }

    public float asFloat() {
        return value.getAsFloat();
    }

    public double asDouble() {
        return value.getAsDouble();
    }

    public BigInteger asBigInteger() {
        return value.getAsBigInteger();
    }

    public BigDecimal asBigDecimal() {
        return value.getAsBigDecimal();
    }

    public Number asNumber() {
        return value.getAsNumber();
    }

    public String asString() {
        return value.getAsString();
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof JsonElementToken token) {
            return value.equals(token.value);
        }
        return value.equals(other);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
