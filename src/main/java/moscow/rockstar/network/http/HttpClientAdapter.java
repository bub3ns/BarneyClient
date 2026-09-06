package moscow.rockstar.network.http;

import java.io.IOException;
import java.net.CookieManager;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.function.Function;
import javax.net.ssl.SSLException;
import moscow.rockstar.api.registry.ServiceCache;
import moscow.rockstar.network.ResourceExceptionFactory;
import moscow.rockstar.ui.factory.ComponentFactory;

/** Configured HTTP client that delegates actual I/O to a selected transport. */
public class HttpClientAdapter extends HttpHeaders<HttpClientAdapter> implements ResourceExceptionFactory {
    private HttpTransport transport;
    private CookieManager cookieManager = new CookieManager();
    private boolean followRedirects = true;
    private int connectTimeoutMillis = 10000;
    private int readTimeoutMillis = 10000;
    private RetryPolicy retryPolicy = new RetryPolicy();
    private ProxySettings proxySettings = new ProxySettings();
    private boolean streamingResponse;

    public HttpClientAdapter() {
        this(ComponentFactory.AUTO);
    }

    public HttpClientAdapter(ComponentFactory factory) {
        this(factory::createAdapter);
    }

    public HttpClientAdapter(Function<HttpClientAdapter, HttpTransport> transportFactory) {
        configureTransport(transportFactory);
    }

    public HttpClientAdapter configureTransport(Function<HttpClientAdapter, HttpTransport> transportFactory) {
        HttpTransport selected = Objects.requireNonNull(transportFactory, "transportFactory").apply(this);
        if (selected == null) {
            throw new NullPointerException("The transport supplier returned null");
        }
        this.transport = selected;
        return this;
    }

    public HttpTransport getTransport() {
        return transport;
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public HttpClientAdapter setCookieManager(CookieManager value) {
        cookieManager = value;
        return this;
    }

    public boolean shouldFollowRedirects() {
        return followRedirects;
    }

    public HttpClientAdapter setFollowRedirects(boolean enabled) {
        followRedirects = enabled;
        return this;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public HttpClientAdapter setConnectTimeoutMillis(int value) {
        connectTimeoutMillis = value;
        return this;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public HttpClientAdapter setReadTimeoutMillis(int value) {
        readTimeoutMillis = value;
        return this;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public HttpClientAdapter setRetryPolicy(RetryPolicy value) {
        retryPolicy = Objects.requireNonNull(value, "retryPolicy");
        return this;
    }

    @Deprecated
    public HttpClientAdapter setRetryPolicyDeprecated(RetryPolicy value) {
        return setRetryPolicy(value);
    }

    public ProxySettings getProxySettings() {
        return proxySettings;
    }

    public HttpClientAdapter setProxySettings(ProxySettings value) {
        proxySettings = Objects.requireNonNull(value, "proxySettings");
        return this;
    }

    public boolean isStreamingResponseEnabled() {
        return streamingResponse;
    }

    public HttpClientAdapter setStreamingResponse(boolean enabled) {
        streamingResponse = enabled;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends HttpRequest, R> R executeAndParse(T request) throws IOException {
        return executeRequest(request, (ServiceCache<R>) request);
    }

    public <R> R executeRequest(HttpRequest request, ServiceCache<R> parser) throws IOException {
        return parser.parseResponse(sendRequestWithRetries(request));
    }

    public HttpResponse sendRequestWithRetries(HttpRequest request) throws IOException {
        RetryPolicy policy = request.hasRetryPolicy() ? request.getRetryPolicy() : retryPolicy;
        for (int connectionAttempt = 0; connectionAttempt <= policy.getMaxConnectionRetries(); connectionAttempt++) {
            try {
                HttpResponse response = null;
                for (int responseAttempt = 0; responseAttempt <= policy.getMaxResponseRetries(); responseAttempt++) {
                    response = transport.send(request);
                    RetryDecision decision = policy.getRetryAfterPolicy().createDecision(response);
                    if (!decision.isScheduled()) {
                        return response;
                    }
                    decision.await();
                }
                if (response == null) {
                    throw new IllegalStateException("Response not received but no exception was thrown");
                }
                if (policy.getMaxResponseRetries() == 0) {
                    return response;
                }
                throw new moscow.rockstar.network.http.response.RetryExhaustedException(response);
            } catch (InterruptedException exception) {
                throw new IOException(exception);
            } catch (ProtocolException | UnknownHostException | SSLException exception) {
                throw exception;
            } catch (IOException exception) {
                if (connectionAttempt < policy.getMaxConnectionRetries()) {
                    continue;
                }
                throw exception;
            }
        }
        throw new IllegalStateException("Connect retry failed but no exception was thrown");
    }

    @Override
    public <T extends HttpRequest> T attachAdapter(T request) {
        request.setAdapter(this);
        return request;
    }

    @Override
    public String toString() {
        return "HttpClient#" + transport.getClass().getSimpleName() + "@"
            + Integer.toHexString(System.identityHashCode(this));
    }
}
