package moscow.rockstar.auth.tokens;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Typed JSON array value. */
public final class JsonArrayToken extends JsonElementToken implements Iterable<JsonElementToken> {
    private final JsonArray values;

    public JsonArrayToken() {
        this(new JsonArray());
    }

    public JsonArrayToken(JsonArray values) {
        super(values);
        this.values = values;
    }

    public JsonArray getValues() {
        return values;
    }

    public JsonArrayToken add(JsonElement value) {
        values.add(value);
        return this;
    }

    public JsonArrayToken add(JsonElementToken value) {
        values.add(value == null ? null : value.getValue());
        return this;
    }

    public JsonArrayToken addBoolean(boolean value) {
        values.add(value);
        return this;
    }

    public JsonArrayToken addNumber(Number value) {
        values.add(value);
        return this;
    }

    public JsonArrayToken addString(String value) {
        values.add(value);
        return this;
    }

    public JsonArrayToken addAll(JsonArray value) {
        values.addAll(value);
        return this;
    }

    public JsonArrayToken addAll(JsonArrayToken value) {
        return addAll(value.values);
    }

    public JsonElementToken setElementAt(int index, JsonElement value) {
        return wrap(values.set(index, value));
    }

    public JsonElementToken setElementAt(int index, JsonElementToken value) {
        return setElementAt(index, value == null ? null : value.getValue());
    }

    public JsonElementToken setBooleanAt(int index, boolean value) {
        return setElementAt(index, new JsonPrimitive(value));
    }

    public JsonElementToken setNumberAt(int index, Number value) {
        return setElementAt(index, new JsonPrimitive(value));
    }

    public JsonElementToken setStringAt(int index, String value) {
        return setElementAt(index, new JsonPrimitive(value));
    }

    public JsonElementToken getElementAt(int index) {
        return wrap(values.get(index));
    }

    public JsonObjectToken getObjectAt(int index) {
        return getElementAt(index).asObject();
    }

    public JsonArrayToken getArrayAt(int index) {
        return getElementAt(index).asArray();
    }

    public JsonPrimitiveToken getPrimitiveAt(int index) {
        return getElementAt(index).asPrimitive();
    }

    public boolean getBooleanAt(int index) {
        return getElementAt(index).asBoolean();
    }

    public byte getByteAt(int index) {
        return getElementAt(index).asByte();
    }

    public short getShortAt(int index) {
        return getElementAt(index).asShort();
    }

    public int getIntAt(int index) {
        return getElementAt(index).asInt();
    }

    public long getLongAt(int index) {
        return getElementAt(index).asLong();
    }

    public float getFloatAt(int index) {
        return getElementAt(index).asFloat();
    }

    public double getDoubleAt(int index) {
        return getElementAt(index).asDouble();
    }

    public Number getNumberAt(int index) {
        return getElementAt(index).asNumber();
    }

    public String getStringAt(int index) {
        return getElementAt(index).asString();
    }

    public JsonElementToken removeElementAt(int index) {
        return wrap(values.remove(index));
    }

    public boolean remove(JsonElement value) {
        return values.remove(value == null ? JsonNull.INSTANCE : value);
    }

    public boolean remove(JsonElementToken value) {
        return remove(value == null ? null : value.getValue());
    }

    public boolean removeBoolean(boolean value) {
        return remove(new JsonPrimitive(value));
    }

    public boolean removeNumber(Number value) {
        return remove(new JsonPrimitive(value));
    }

    public boolean removeString(String value) {
        return remove(new JsonPrimitive(value));
    }

    public boolean removeAll(JsonArray value) {
        boolean changed = false;
        for (JsonElement element : value) {
            changed |= values.remove(element);
        }
        return changed;
    }

    public boolean removeAll(JsonArrayToken value) {
        return removeAll(value.values);
    }

    public JsonArrayToken clear() {
        values.asList().clear();
        return this;
    }

    public boolean acceptsIndex(int index) {
        return index >= 0 && index < values.size();
    }

    public boolean contains(JsonElement value) {
        return values.contains(value == null ? JsonNull.INSTANCE : value);
    }

    public boolean contains(JsonElementToken value) {
        return contains(value == null ? null : value.getValue());
    }

    public int size() {
        return values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public Iterator<JsonElementToken> iterator() {
        Iterator<JsonElement> delegate = values.iterator();
        return new Iterator<>() {
            @Override public boolean hasNext() { return delegate.hasNext(); }
            @Override public JsonElementToken next() { return wrap(delegate.next()); }
            @Override public void remove() { delegate.remove(); }
        };
    }

    public Stream<JsonElementToken> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public List<JsonElementToken> toList() {
        List<JsonElementToken> result = new ArrayList<>();
        for (JsonElement element : values) {
            result.add(wrap(element));
        }
        return result;
    }

    public <T> List<T> map(Function<JsonElementToken, T> mapper) {
        List<T> result = new ArrayList<>();
        for (JsonElement element : values) {
            result.add(mapper.apply(wrap(element)));
        }
        return result;
    }

    private static JsonElementToken wrap(JsonElement value) {
        return value == null ? null : JsonElementToken.fromJsonElement(value);
    }
}
