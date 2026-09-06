package moscow.rockstar.network.http;

import java.net.MalformedURLException;
import java.net.URL;

/** HTTP request variant carrying a request body. */
public class TypedHttpRequest extends HttpRequest {
    private HttpBody body;

    public TypedHttpRequest(String method, String url) throws MalformedURLException {
        super(method, url);
    }

    public TypedHttpRequest(String method, URL url) {
        super(method, url);
    }

    public boolean hasBody() {
        return body != null;
    }

    public HttpBody getBody() {
        return body;
    }

    public TypedHttpRequest setBody(HttpBody body) {
        this.body = body;
        return this;
    }
}
