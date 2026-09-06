package moscow.rockstar.network.http.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import moscow.rockstar.network.http.HttpBody;
import moscow.rockstar.network.http.HttpRequest;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.HttpTransport;
import moscow.rockstar.network.http.HttpClientAdapter;
import moscow.rockstar.network.http.ProxyAuthenticationSelector;
import moscow.rockstar.network.http.ProxySettings;
import moscow.rockstar.network.http.TypedHttpRequest;

/** URLConnection transport from the original HTTP client stack. */
public final class UrlConnectionClient extends HttpTransport {
    public UrlConnectionClient(HttpClientAdapter client) {
        super(client);
    }

    @Override
    public HttpResponse send(HttpRequest request) throws IOException {
        CookieManager cookies = resolveCookieManager(request);
        ProxySettings proxySettings = client.getProxySettings();
        ProxyAuthenticationSelector selector = proxySettings.hasProxy()
            ? proxySettings.createProxyAuthenticator()
            : null;
        try {
            if (selector != null) selector.installProxy(false);
            HttpURLConnection connection = openConnection(
                request,
                cookies,
                selector == null ? null : proxySettings
            );
            return readResponse(connection, cookies, request);
        } finally {
            if (selector != null) selector.restoreProxy(false);
        }
    }

    private HttpURLConnection openConnection(
        HttpRequest request,
        CookieManager cookies,
        ProxySettings proxySettings
    ) throws IOException {
        URL url = request.getUrl();
        HttpURLConnection connection = proxySettings == null
            ? (HttpURLConnection) url.openConnection()
            : (HttpURLConnection) url.openConnection(proxySettings.toProxy());
        if (connection instanceof HttpsURLConnection) {
            try {
                ((HttpsURLConnection) connection).setSSLSocketFactory(
                    SSLContext.getDefault().getSocketFactory()
                );
            } catch (NoSuchAlgorithmException exception) {
                throw new IOException("The default SSL context is unavailable", exception);
            }
        }
        configure(connection, cookies, request);
        connection.connect();
        return connection;
    }

    private void configure(HttpURLConnection connection, CookieManager cookies, HttpRequest request) throws IOException {
        applyHeaders(buildRequestHeaders(request, cookies), connection::setRequestProperty, connection::addRequestProperty);
        TypedHttpRequest typed = request instanceof TypedHttpRequest value ? value : null;
        HttpBody body = typed == null ? null : typed.getBody();
        connection.setConnectTimeout(client.getConnectTimeoutMillis());
        connection.setReadTimeout(client.getReadTimeoutMillis());
        connection.setRequestMethod(request.getHttpMethod());
        connection.setDoInput(true);
        connection.setDoOutput(body != null);
        if (body != null && request.isRequestBodyStreaming()) {
            if (body.getContentLength() >= 0) {
                connection.setFixedLengthStreamingMode(body.getContentLength());
            } else {
                connection.setChunkedStreamingMode(0);
            }
        }
        connection.setInstanceFollowRedirects(shouldFollowRedirects(request));
    }

    private HttpResponse readResponse(
        HttpURLConnection connection,
        CookieManager cookies,
        HttpRequest request
    ) throws IOException {
        boolean disconnect = true;
        try {
            if (connection.getDoOutput() && request instanceof TypedHttpRequest typed && typed.getBody() != null) {
                try (OutputStream output = connection.getOutputStream()) {
                    typed.getBody().writeTo(output);
                }
            }

            Map<String, List<String>> headers = new HashMap<>(connection.getHeaderFields());
            headers.remove(null);
            InputStream input = openResponseStream(connection);
            HttpResponse response;
            if (isStreamingResponse(request)) {
                response = new HttpResponse(request.getUrl(), connection.getResponseCode(), input, headers);
                disconnect = false;
            } else {
                response = new HttpResponse(
                    request.getUrl(),
                    connection.getResponseCode(),
                    readResponseBytes(input, connection.getContentLength()),
                    headers
                );
            }
            storeCookies(cookies, request.getUrl(), connection.getHeaderFields());
            return response;
        } finally {
            if (disconnect) connection.disconnect();
        }
    }

    private InputStream openResponseStream(HttpURLConnection connection) throws IOException {
        InputStream input = connection.getResponseCode() >= 400
            ? connection.getErrorStream()
            : connection.getInputStream();
        return input == null ? new ByteArrayInputStream(new byte[0]) : input;
    }

    private byte[] readResponseBytes(InputStream input, int length) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(length >= 0 ? length : 1024);
        byte[] buffer = new byte[1024];
        int count;
        while ((count = input.read(buffer)) >= 0) {
            if (count > 0) output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }
}
