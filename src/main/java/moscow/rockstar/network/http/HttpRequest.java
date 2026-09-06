package moscow.rockstar.network.http;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import moscow.rockstar.api.registry.ServiceCache;
import moscow.rockstar.util.DeferredValue;

/** Base HTTP request with headers, redirect, cookie, and retry settings. */
public class HttpRequest extends HttpHeaders<HttpRequest> {
    private final String httpMethod;
    private final URL requestUrl;
    private boolean requestBodyStreaming;
    private boolean streamingResponse;
    private RedirectPolicy redirectPolicy = RedirectPolicy.FOLLOW_REDIRECTS_CONFIGURED;
    private final DeferredValue<CookieManager> cookieManager = new DeferredValue<>();
    private final DeferredValue<RetryPolicy> retryPolicy = new DeferredValue<>();
    private final DeferredValue<Boolean> responseStreaming = new DeferredValue<>();
    private WeakReference<HttpClientAdapter> adapterReference;

    public HttpRequest(String method, String url) throws MalformedURLException {
        this(method, new URL(url));
    }

    public HttpRequest(String method, URL url) {
        this.httpMethod = Objects.requireNonNull(method, "method");
        this.requestUrl = Objects.requireNonNull(url, "url");
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public URL getUrl() {
        return requestUrl;
    }

    /** Whether a body should be supplied to the transport as a stream. */
    public boolean isRequestBodyStreaming() {
        return requestBodyStreaming;
    }

    public HttpRequest setRequestBodyStreaming(boolean enabled) {
        requestBodyStreaming = enabled;
        return this;
    }

    public boolean isStreamingResponse() {
        return streamingResponse;
    }

    public HttpRequest setStreamingResponse(boolean enabled) {
        streamingResponse = enabled;
        return this;
    }

    public RedirectPolicy getRedirectPolicy() {
        return redirectPolicy;
    }

    public HttpRequest setRedirectsEnabled(boolean enabled) {
        return setRedirectPolicy(enabled
            ? RedirectPolicy.FOLLOW_REDIRECTS_ALWAYS
            : RedirectPolicy.FOLLOW_REDIRECTS_NEVER);
    }

    public HttpRequest setRedirectPolicy(RedirectPolicy policy) {
        redirectPolicy = Objects.requireNonNull(policy, "redirectPolicy");
        return this;
    }

    public boolean hasCookieManager() {
        return cookieManager.isPresent();
    }

    public HttpRequest prepareCookieManager() {
        cookieManager.clear();
        return this;
    }

    public CookieManager getCookieManager() {
        return cookieManager.isPresent() ? cookieManager.get() : null;
    }

    public HttpRequest setCookieManager(CookieManager value) {
        cookieManager.set(value);
        return this;
    }

    public boolean hasRetryPolicy() {
        return retryPolicy.isPresent();
    }

    public HttpRequest prepareRetryPolicy() {
        retryPolicy.clear();
        return this;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy.get();
    }

    public HttpRequest setRetryPolicy(RetryPolicy value) {
        retryPolicy.set(Objects.requireNonNull(value, "retryPolicy"));
        return this;
    }

    public boolean hasResponseFlag() {
        return responseStreaming.isPresent();
    }

    public HttpRequest prepareResponseFlag() {
        responseStreaming.clear();
        return this;
    }

    public boolean isResponseFlagSet() {
        return responseStreaming.get();
    }

    public HttpRequest setResponseFlag(boolean enabled) {
        responseStreaming.set(enabled);
        return this;
    }

    public HttpRequest setAdapter(HttpClientAdapter adapter) {
        adapterReference = adapter == null ? null : new WeakReference<>(adapter);
        return this;
    }

    protected final HttpClientAdapter resolveAdapter() {
        HttpClientAdapter adapter = adapterReference == null ? null : adapterReference.get();
        return adapter == null ? new HttpClientAdapter() : adapter;
    }

    public HttpResponse send() throws IOException {
        return resolveAdapter().sendRequestWithRetries(this);
    }

    public <R> R parseResponse(ServiceCache<R> parser) throws IOException {
        return resolveAdapter().executeRequest(this, parser);
    }

    public enum RedirectPolicy {
        FOLLOW_REDIRECTS_CONFIGURED,
        FOLLOW_REDIRECTS_ALWAYS,
        FOLLOW_REDIRECTS_NEVER;
    }
}
