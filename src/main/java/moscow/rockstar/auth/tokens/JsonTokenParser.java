package moscow.rockstar.auth.tokens;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.Reader;

/** Parser facade for protocol JSON values. */
public final class JsonTokenParser {
    public static JsonElementToken parse(String json) {
        return JsonElementToken.fromJsonElement(JsonParser.parseString(json));
    }

    public static JsonElementToken parse(Reader reader) {
        return JsonElementToken.fromJsonElement(JsonParser.parseReader(reader));
    }

    public static JsonElementToken parse(JsonReader reader) {
        return JsonElementToken.fromJsonElement(JsonParser.parseReader(reader));
    }

    private JsonTokenParser() {
        throw new UnsupportedOperationException("Utility class");
    }
}
