package moscow.rockstar.network.http.response;

import moscow.rockstar.network.http.HttpResponse;

public final class MicrosoftApiException extends ApiResponseException {
    private final int numericCode;

    public MicrosoftApiException(HttpResponse response, int code, String message) {
        super(response, Integer.toString(code), message);
        this.numericCode = code;
    }

    public int getNumericCode() {
        return numericCode;
    }
}
