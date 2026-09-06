package moscow.rockstar.auth.tokens;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/** Typed JSON object wrapper used by the original protocol codecs. */
public class JsonObjectToken extends JsonElementToken implements Iterable<Map.Entry<String, JsonElementToken>> {
    private final JsonObject values;

    public JsonObjectToken() {
        this(new JsonObject());
    }

    public JsonObjectToken(JsonObject values) {
        super(values);
        this.values = values;
    }

    public JsonObject getValues() {
        return values;
    }

    public JsonObjectToken put(String name, JsonElement value) {
        values.add(name, value);
        return this;
    }

    public JsonObjectToken put(String name, JsonElementToken value) {
        values.add(name, value == null ? null : value.getValue());
        return this;
    }

    public JsonObjectToken put(String name, boolean value) {
        values.addProperty(name, value);
        return this;
    }

    public JsonObjectToken put(String name, Number value) {
        values.addProperty(name, value);
        return this;
    }

    public JsonObjectToken put(String name, String value) {
        values.addProperty(name, value);
        return this;
    }

    public JsonObjectToken putAll(JsonObject other) {
        other.asMap().forEach(values::add);
        return this;
    }

    public JsonObjectToken putAll(JsonObjectToken other) {
        return putAll(other.values);
    }

    public JsonElementToken remove(String name) {
        JsonElement removed = values.remove(name);
        return removed == null ? null : JsonElementToken.fromJsonElement(removed);
    }

    public JsonObjectToken clear() {
        values.asMap().clear();
        return this;
    }

    public boolean contains(String name) {
        return values.has(name);
    }

    public boolean containsObject(String name) {
        return contains(name) && values.get(name).isJsonObject();
    }

    public boolean containsArray(String name) {
        return contains(name) && values.get(name).isJsonArray();
    }

    public boolean containsPrimitive(String name) {
        return contains(name) && values.get(name).isJsonPrimitive();
    }

    public boolean containsBoolean(String name) {
        return containsPrimitive(name) && values.get(name).getAsJsonPrimitive().isBoolean();
    }

    public boolean containsNumber(String name) {
        return containsPrimitive(name) && values.get(name).getAsJsonPrimitive().isNumber();
    }

    public boolean containsString(String name) {
        return containsPrimitive(name) && values.get(name).getAsJsonPrimitive().isString();
    }

    public JsonElementToken get(String name) {
        JsonElement value = values.get(name);
        return value == null ? null : JsonElementToken.fromJsonElement(value);
    }

    public JsonElementToken getOrDefault(String name, JsonElementToken fallback) {
        JsonElementToken value = get(name);
        return value == null ? fallback : value;
    }

    public JsonElementToken require(String name) {
        JsonElementToken value = get(name);
        if (value == null) {
            throw new NoSuchElementException("No JSON value found for key: " + name);
        }
        return value;
    }

    public JsonObjectToken getObject(String name) {
        return containsObject(name) ? get(name).asObject() : null;
    }

    public JsonArrayToken getArray(String name) {
        return containsArray(name) ? get(name).asArray() : null;
    }

    public JsonPrimitiveToken getPrimitive(String name) {
        return containsPrimitive(name) ? get(name).asPrimitive() : null;
    }

    public Optional<JsonElementToken> find(String name) {
        return Optional.ofNullable(get(name));
    }

    public String getString(String name) {
        return require(name).asString();
    }

    public String getString(String name, String fallback) {
        return containsPrimitive(name) ? values.get(name).getAsString() : fallback;
    }

    public int getInt(String name) {
        return require(name).asInt();
    }

    public int getInt(String name, int fallback) {
        return containsNumber(name) ? values.get(name).getAsInt() : fallback;
    }

    public long getLong(String name) {
        return require(name).asLong();
    }

    public long getLong(String name, long fallback) {
        return containsNumber(name) ? values.get(name).getAsLong() : fallback;
    }

    public boolean getBoolean(String name) {
        return require(name).asBoolean();
    }

    public boolean getBoolean(String name, boolean fallback) {
        return containsBoolean(name) ? values.get(name).getAsBoolean() : fallback;
    }

    public Iterable<Map.Entry<String, JsonElement>> rawEntries() {
        return values.entrySet();
    }

    @Override
    public java.util.Iterator<Map.Entry<String, JsonElementToken>> iterator() {
        java.util.Iterator<Map.Entry<String, JsonElement>> delegate = values.entrySet().iterator();
        return new java.util.Iterator<>() {
            @Override public boolean hasNext() { return delegate.hasNext(); }
            @Override public Map.Entry<String, JsonElementToken> next() {
                Map.Entry<String, JsonElement> entry = delegate.next();
                return new java.util.AbstractMap.SimpleImmutableEntry<>(entry.getKey(), JsonElementToken.fromJsonElement(entry.getValue()));
            }
            @Override public void remove() { delegate.remove(); }
        };
    }

    public Map<String, JsonElementToken> toMap() {
        Map<String, JsonElementToken> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            result.put(entry.getKey(), JsonElementToken.fromJsonElement(entry.getValue()));
        }
        return result;
    }
}
