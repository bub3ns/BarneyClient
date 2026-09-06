package moscow.rockstar.network.http.response;

import moscow.rockstar.network.http.HttpResponse;

public final class NestedApiException extends ApiResponseException {
    private final String namespace;

    public NestedApiException(HttpResponse response, String namespace, String code, String message) {
        super(response, code, message);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }
}
