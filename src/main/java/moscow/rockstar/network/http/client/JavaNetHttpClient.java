package moscow.rockstar.network.http.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import moscow.rockstar.network.ProxyType;
import moscow.rockstar.network.http.HttpBody;
import moscow.rockstar.network.http.HttpClientAdapter;
import moscow.rockstar.network.http.HttpRequest;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.HttpTransport;
import moscow.rockstar.network.http.TypedHttpRequest;
import moscow.rockstar.network.http.UrlBuilder;

/** Java 11 HttpClient transport from the original client stack. */
public final class JavaNetHttpClient extends HttpTransport {
    public JavaNetHttpClient(HttpClientAdapter client) {
        super(client);
    }

    @Override
    public HttpResponse send(HttpRequest request) throws IOException {
        ExecutorService executor = Executors.newCachedThreadPool();
        java.net.http.HttpClient httpClient = null;
        boolean closeClient = true;
        try {
            httpClient = buildClient(request, executor);
            java.net.http.HttpRequest javaRequest = buildRequest(request);
            if (isStreamingResponse(request)) {
                java.net.http.HttpResponse<InputStream> response = send(
                    httpClient,
                    javaRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofInputStream()
                );
                java.net.http.HttpClient streamingClient = httpClient;
                InputStream managed = new ManagedInputStream(response.body(), () -> close(executor, streamingClient));
                closeClient = false;
                return new HttpResponse(
                    toUrl(response.uri()),
                    response.statusCode(),
                    managed,
                    response.headers().map()
                );
            }
            java.net.http.HttpResponse<byte[]> response = send(
                httpClient,
                javaRequest,
                java.net.http.HttpResponse.BodyHandlers.ofByteArray()
            );
            return new HttpResponse(toUrl(response.uri()), response.statusCode(), response.body(), response.headers().map());
        } finally {
            if (closeClient) close(executor, httpClient);
        }
    }

    private java.net.http.HttpClient buildClient(HttpRequest request, Executor executor) throws IOException {
        java.net.http.HttpClient.Builder builder = java.net.http.HttpClient.newBuilder().executor(executor);
        CookieManager cookies = resolveCookieManager(request);
        if (cookies != null) builder.cookieHandler(cookies);
        builder.connectTimeout(Duration.ofMillis(client.getConnectTimeoutMillis()));
        builder.followRedirects(shouldFollowRedirects(request)
            ? java.net.http.HttpClient.Redirect.NORMAL
            : java.net.http.HttpClient.Redirect.NEVER);
        if (client.getProxySettings().hasProxy()) {
            if (!ProxyType.HTTP.equals(client.getProxySettings().getProxyType())) {
                throw new UnsupportedOperationException("The Java 11 HttpClient only supports HTTP proxies");
            }
            builder.proxy(client.getProxySettings().createProxyAuthenticator());
            if (client.getProxySettings().hasCredentials()) {
                builder.authenticator(client.getProxySettings().createAuthenticator());
            }
        }
        return builder.build();
    }

    private java.net.http.HttpRequest buildRequest(HttpRequest request) throws IOException {
        java.net.http.HttpRequest.BodyPublisher publisher = java.net.http.HttpRequest.BodyPublishers.noBody();
        if (request instanceof TypedHttpRequest typed && typed.getBody() != null) {
            HttpBody body = typed.getBody();
            if (request.isRequestBodyStreaming()) {
                publisher = java.net.http.HttpRequest.BodyPublishers.ofInputStream(() -> {
                    try {
                        return body.openStream();
                    } catch (IOException exception) {
                        throw new java.io.UncheckedIOException(exception);
                    }
                });
            } else {
                publisher = java.net.http.HttpRequest.BodyPublishers.ofByteArray(body.readBytes());
            }
        }
        java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder()
            .uri(UrlBuilder.fromUrlObject(request.getUrl()).toUri())
            .timeout(Duration.ofMillis(client.getReadTimeoutMillis()))
            .method(request.getHttpMethod(), publisher);
        for (Map.Entry<String, List<String>> entry : buildRequestHeaders(request, null).entrySet()) {
            if (entry.getKey().equalsIgnoreCase("Content-Length")) continue;
            for (String value : entry.getValue()) builder.header(entry.getKey(), value);
        }
        return builder.build();
    }

    private <T> java.net.http.HttpResponse<T> send(
        java.net.http.HttpClient client,
        java.net.http.HttpRequest request,
        java.net.http.HttpResponse.BodyHandler<T> handler
    ) throws IOException {
        try {
            return client.send(request, handler);
        } catch (InterruptedException exception) {
            throw new IOException("Request interrupted", exception);
        }
    }

    private java.net.URL toUrl(URI uri) throws IOException {
        try {
            return uri.toURL();
        } catch (java.net.MalformedURLException exception) {
            throw new IOException("Invalid response URL", exception);
        }
    }

    private void close(ExecutorService executor, java.net.http.HttpClient client) {
        executor.shutdownNow();
        if (client instanceof Closeable closeable) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static final class ManagedInputStream extends InputStream {
        private final InputStream delegate;
        private final Runnable closeAction;
        private boolean closed;

        private ManagedInputStream(InputStream delegate, Runnable closeAction) {
            this.delegate = delegate;
            this.closeAction = closeAction;
        }

        @Override public int read() throws IOException { return delegate.read(); }
        @Override public int read(byte[] bytes, int offset, int length) throws IOException { return delegate.read(bytes, offset, length); }
        @Override public long skip(long count) throws IOException { return delegate.skip(count); }
        @Override public int available() throws IOException { return delegate.available(); }
        @Override public void close() throws IOException {
            if (!closed) {
                closed = true;
                try { delegate.close(); } finally { closeAction.run(); }
            }
        }
    }
}
