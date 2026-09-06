package moscow.rockstar.network.http.response;

import java.io.IOException;
import moscow.rockstar.auth.tokens.JsonElementToken;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.network.http.HttpResponse;

/** Maps nested namespace/code/message errors and preserves suppressed causes. */
public interface NestedErrorHandler<R> extends JsonResponseParser<R> {
    @Override
    default void handleErrorResponse(HttpResponse response, JsonObjectToken body) throws IOException {
        NestedApiException first = null;
        NestedApiException previous = null;
        JsonObjectToken current = body;
        while (current != null) {
            if (current.containsString("namespace") && current.containsString("code") && current.containsString("message")) {
                NestedApiException error = new NestedApiException(response,
                    current.getString("namespace"), current.getString("code"), current.getString("message"));
                if (first == null) {
                    first = error;
                } else {
                    previous.addSuppressed(error);
                }
                previous = error;
            }
            JsonElementToken inner = current.get("innerError");
            current = inner != null && inner.isObject() ? inner.asObject() : null;
        }
        if (first != null) {
            throw first;
        }
    }
}
