package moscow.rockstar.network.http.response;

import moscow.rockstar.network.http.HttpResponse;

/** Indicates that a server response was empty, malformed, or of the wrong type. */
public final class EmptyResponseException extends HttpResponseException {
    public EmptyResponseException(HttpResponse response, String message) {
        super(response, "status: " + response.getStatusCode() + " " + response.getReasonPhrase() + ", message: " + message);
    }
}
