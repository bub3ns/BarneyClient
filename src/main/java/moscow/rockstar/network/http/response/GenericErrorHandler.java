package moscow.rockstar.network.http.response;

import java.io.IOException;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.network.http.HttpResponse;

/** Generic JSON error mapping used by services with error/errorMessage fields. */
public interface GenericErrorHandler<R> extends JsonResponseParser<R> {
    @Override
    default void handleErrorResponse(HttpResponse response, JsonObjectToken body) throws IOException {
        if (body.containsString("error") && body.containsString("errorMessage")) {
            throw new GenericApiException(response, body.getString("error"), body.getString("errorMessage"));
        }
    }
}
