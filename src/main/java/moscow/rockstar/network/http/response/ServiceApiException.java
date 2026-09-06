package moscow.rockstar.network.http.response;

import moscow.rockstar.network.http.HttpResponse;

public final class ServiceApiException extends ApiResponseException {
    public ServiceApiException(HttpResponse response, String code, String message) {
        super(response, code, message);
    }
}
