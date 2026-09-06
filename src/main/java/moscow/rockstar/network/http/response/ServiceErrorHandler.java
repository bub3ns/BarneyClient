package moscow.rockstar.network.http.response;

import java.io.IOException;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.network.http.HttpResponse;

/** Error mapping for the service API family. */
public interface ServiceErrorHandler<R> extends JsonResponseParser<R> {
    @Override
    default void handleErrorResponse(HttpResponse response, JsonObjectToken body) throws IOException {
        if (body.containsString("error") && body.containsString("errorMessage")) {
            throw new ServiceApiException(response, body.getString("error"), body.getString("errorMessage"));
        }
        if (body.containsString("errorMessage")) {
            throw new ServiceApiException(response, Integer.toString(response.getStatusCode()), body.getString("errorMessage"));
        }
    }
}
