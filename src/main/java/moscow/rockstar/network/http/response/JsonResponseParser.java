package moscow.rockstar.network.http.response;

import java.io.IOException;
import moscow.rockstar.api.registry.ServiceCache;
import moscow.rockstar.auth.tokens.JsonElementToken;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.auth.tokens.JsonTokenParser;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.MediaTypes;

/** Parses JSON HTTP responses while retaining the original status/error flow. */
public interface JsonResponseParser<R> extends ServiceCache<R> {
    @Override
    default R parseResponse(HttpResponse response) throws IOException {
        HttpResponse decodedResponse = response.decompress();
        String body = decodedResponse.getBodyText();
        if (body.isEmpty() && decodedResponse.getStatusCode() == 204) {
            return null;
        }
        if (body.isEmpty() && decodedResponse.getStatusCode() >= 300) {
            throw new EmptyResponseException(decodedResponse, "Empty response");
        }
        if (decodedResponse.getBody().getMediaType() == null
            || !MediaTypes.APPLICATION_JSON.getMediaType().equals(decodedResponse.getBody().getMediaType().getMediaType())) {
            throw new EmptyResponseException(decodedResponse, "Wrong content type");
        }
        JsonElementToken parsed = JsonTokenParser.parse(body);
        if (!parsed.isObject()) {
            throw new EmptyResponseException(decodedResponse, "Expected a JSON object response");
        }
        JsonObjectToken object = parsed.asObject();
        if (decodedResponse.getStatusCode() >= 300) {
            handleErrorResponse(decodedResponse, object);
            throw new EmptyResponseException(decodedResponse, body);
        }
        return parseSuccessResponse(decodedResponse, object);
    }

    R parseSuccessResponse(HttpResponse response, JsonObjectToken body) throws IOException;

    void handleErrorResponse(HttpResponse response, JsonObjectToken body) throws IOException;
}
