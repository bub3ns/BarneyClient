package moscow.rockstar.network.http.response;

import moscow.rockstar.network.http.HttpResponse;

public final class GenericApiException extends ApiResponseException {
    public GenericApiException(HttpResponse response, String code, String message) {
        super(response, code, message);
    }
}
