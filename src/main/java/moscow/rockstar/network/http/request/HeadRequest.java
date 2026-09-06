package moscow.rockstar.network.http.request;

import java.net.MalformedURLException;
import java.net.URL;
import moscow.rockstar.network.http.HttpRequest;

public class HeadRequest extends HttpRequest {
    public HeadRequest(String url) throws MalformedURLException { super("HEAD", url); }
    public HeadRequest(URL url) { super("HEAD", url); }
}
