package moscow.rockstar.network.http.request;

import java.net.MalformedURLException;
import java.net.URL;
import moscow.rockstar.network.http.HttpRequest;

public class GetRequest extends HttpRequest {
    public GetRequest(String url) throws MalformedURLException { super("GET", url); }
    public GetRequest(URL url) { super("GET", url); }
}
