package moscow.rockstar.network.http.response;

import java.io.IOException;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.network.http.HttpResponse;

/** Microsoft OAuth error mapping. */
public interface MicrosoftErrorHandler<R> extends JsonResponseParser<R> {
    @Override
    default void handleErrorResponse(HttpResponse response, JsonObjectToken body) throws IOException {
        if (body.containsNumber("errorCode") && body.containsString("errorMsg")) {
            throw new MicrosoftApiException(response, body.getInt("errorCode"), body.getString("errorMsg"));
        }
    }
}
