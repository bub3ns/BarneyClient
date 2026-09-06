package moscow.rockstar.network.http.response;

import moscow.rockstar.network.http.HttpResponse;

public final class OAuthApiException extends ApiResponseException {
    public OAuthApiException(HttpResponse response, String code, String message) {
        super(response, code, message);
    }
}
