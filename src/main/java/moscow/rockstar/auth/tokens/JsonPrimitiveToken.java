package moscow.rockstar.auth.tokens;

import com.google.gson.JsonPrimitive;

/** Typed JSON primitive value. */
public final class JsonPrimitiveToken extends JsonElementToken {
    private final JsonPrimitive value;

    public JsonPrimitiveToken(String value) {
        this(new JsonPrimitive(value));
    }

    public JsonPrimitiveToken(boolean value) {
        this(new JsonPrimitive(value));
    }

    public JsonPrimitiveToken(Number value) {
        this(new JsonPrimitive(value));
    }

    public JsonPrimitiveToken(JsonPrimitive value) {
        super(value);
        this.value = value;
    }

    public JsonPrimitive getPrimitive() {
        return value;
    }

    public boolean isBooleanValue() {
        return value.isBoolean();
    }

    public boolean isNumberValue() {
        return value.isNumber();
    }

    public boolean isStringValue() {
        return value.isString();
    }
}
