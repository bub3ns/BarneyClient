package moscow.rockstar.network.http.response;

import moscow.rockstar.network.http.HttpResponse;

public final class RetryExhaustedException extends HttpResponseException {
    public RetryExhaustedException(HttpResponse response) {
        super(response, "Maximum retry count exceeded");
    }

    public RetryExhaustedException(HttpResponse response, String message) {
        super(response, message);
    }
}
