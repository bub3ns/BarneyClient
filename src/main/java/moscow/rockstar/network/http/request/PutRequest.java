package moscow.rockstar.network.http.request;

import java.net.MalformedURLException;
import java.net.URL;
import moscow.rockstar.network.http.TypedHttpRequest;

public class PutRequest extends TypedHttpRequest {
    public PutRequest(String url) throws MalformedURLException { super("PUT", url); }
    public PutRequest(URL url) { super("PUT", url); }
}
