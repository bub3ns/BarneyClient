package moscow.rockstar.network.http;

import com.google.gson.JsonObject;

/** JSON entity body used by the authentication requests. */
public final class JsonRequestBody extends HttpBody {
    private final byte[] payload;

    public JsonRequestBody(JsonObject json) {
        this(json == null ? "null" : json.toString());
    }

    public JsonRequestBody(String json) {
        super(MediaTypes.APPLICATION_JSON, json.getBytes(MediaTypes.APPLICATION_JSON.getCharset().orElse(java.nio.charset.StandardCharsets.UTF_8)).length);
        this.payload = json.getBytes(MediaTypes.APPLICATION_JSON.getCharset().orElse(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Override
    protected boolean isRepeatable() {
        return true;
    }

    @Override
    protected java.io.InputStream openContentStream() {
        return new java.io.ByteArrayInputStream(payload);
    }
}
