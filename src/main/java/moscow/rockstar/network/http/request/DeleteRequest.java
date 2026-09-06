package moscow.rockstar.network.http.request;

import java.net.MalformedURLException;
import java.net.URL;
import moscow.rockstar.network.http.HttpRequest;

public class DeleteRequest extends HttpRequest {
    public DeleteRequest(String url) throws MalformedURLException { super("DELETE", url); }
    public DeleteRequest(URL url) { super("DELETE", url); }
}
