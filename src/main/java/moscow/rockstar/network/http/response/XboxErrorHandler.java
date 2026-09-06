package moscow.rockstar.network.http.response;

import java.io.IOException;
import java.util.Optional;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.network.http.HttpResponse;

/** Xbox Live response mapping, including the X-Err header path. */
public interface XboxErrorHandler<R> extends JsonResponseParser<R> {
    @Override
    default R parseResponse(HttpResponse response) throws IOException {
        if (response.getStatusCode() >= 300) {
            Optional<String> header = response.getFirstHeader("X-Err");
            if (header.isPresent()) {
                throw new XboxErrorException(response, Long.parseLong(header.get()));
            }
        }
        return JsonResponseParser.super.parseResponse(response);
    }

    @Override
    default void handleErrorResponse(HttpResponse response, JsonObjectToken body) throws IOException {
        if (body.containsNumber("XErr")) {
            throw new XboxErrorException(response, body.getLong("XErr"));
        }
    }
}
