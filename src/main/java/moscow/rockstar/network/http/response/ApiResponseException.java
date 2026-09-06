package moscow.rockstar.network.http.response;

import moscow.rockstar.network.http.HttpResponse;

/** Structured service error returned in a JSON response. */
public class ApiResponseException extends HttpResponseException {
    private final String errorCode;
    private final String errorMessage;

    public ApiResponseException(HttpResponse response, String errorCode, String errorMessage) {
        super(response, "status: " + response.getStatusCode() + " " + response.getReasonPhrase()
            + ", error: " + errorCode + ", error message: " + errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
