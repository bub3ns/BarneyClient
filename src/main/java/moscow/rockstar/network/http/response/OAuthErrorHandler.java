package moscow.rockstar.network.http.response;

import java.io.IOException;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.network.http.HttpResponse;

/** OAuth 2.0 error mapping. */
public interface OAuthErrorHandler<R> extends JsonResponseParser<R> {
    @Override
    default void handleErrorResponse(HttpResponse response, JsonObjectToken body) throws IOException {
        if (body.containsString("error") && body.containsString("error_description")) {
            throw new OAuthApiException(response, body.getString("error"), body.getString("error_description"));
        }
    }
}
