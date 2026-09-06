package moscow.rockstar.network.http.response;

import moscow.rockstar.network.http.HttpResponse;

public final class ClientApiException extends ApiResponseException {
    public ClientApiException(HttpResponse response, String code, String message) {
        super(response, code, message);
    }
}
