package moscow.rockstar.network.http.response;

import java.io.IOException;
import moscow.rockstar.network.http.HttpResponse;

/** Base exception for an HTTP response that cannot be accepted. */
public class HttpResponseException extends IOException {
    private final HttpResponse response;

    public HttpResponseException(HttpResponse response) {
        this(response, "Request failed: " + response.getStatusCode() + " " + response.getReasonPhrase());
    }

    public HttpResponseException(HttpResponse response, String message) {
        super(message);
        this.response = response;
    }

    public HttpResponse getResponse() {
        return response;
    }
}
