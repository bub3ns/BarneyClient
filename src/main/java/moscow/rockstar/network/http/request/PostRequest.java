package moscow.rockstar.network.http.request;

import java.net.MalformedURLException;
import java.net.URL;
import moscow.rockstar.network.http.TypedHttpRequest;

public class PostRequest extends TypedHttpRequest {
    public PostRequest(String url) throws MalformedURLException { super("POST", url); }
    public PostRequest(URL url) { super("POST", url); }
}
