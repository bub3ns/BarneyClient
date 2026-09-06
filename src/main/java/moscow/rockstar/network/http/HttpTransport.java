package moscow.rockstar.network.http;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Transport implementation used by {@link HttpClientAdapter}.
 *
 * <p>The original client kept configuration on the public adapter and put the
 * concrete URLConnection/Java HTTP/Reactor implementations in this separate
 * executor layer.  Keeping that split is important: a request may be retried
 * by the adapter without rebuilding the transport.</p>
 */
public abstract class HttpTransport {
    protected final HttpClientAdapter client;

    protected HttpTransport(HttpClientAdapter client) {
        this.client = client;
    }

    public abstract HttpResponse send(HttpRequest request) throws IOException, InterruptedException;

    protected final boolean shouldFollowRedirects(HttpRequest request) {
        return switch (request.getRedirectPolicy()) {
            case FOLLOW_REDIRECTS_CONFIGURED -> client.shouldFollowRedirects();
            case FOLLOW_REDIRECTS_ALWAYS -> true;
            case FOLLOW_REDIRECTS_NEVER -> false;
        };
    }

    protected final CookieManager resolveCookieManager(HttpRequest request) {
        return request.hasCookieManager() ? request.getCookieManager() : client.getCookieManager();
    }

    protected final boolean isStreamingResponse(HttpRequest request) {
        return request.hasResponseFlag() ? request.isResponseFlagSet() : client.isStreamingResponseEnabled();
    }

    protected final Map<String, List<String>> buildRequestHeaders(HttpRequest request, CookieManager cookies) throws IOException {
        return buildRequestHeaders(request, cookies, true);
    }

    protected final Map<String, List<String>> buildRequestHeaders(
        HttpRequest request,
        CookieManager cookies,
        boolean includeBodyHeaders
    ) throws IOException {
        Map<String, List<String>> headers = new HashMap<>();
        if (cookies != null) {
            try {
                Map<String, List<String>> cookieHeaders = cookies.get(request.getUrl().toURI(), Collections.emptyMap());
                for (Map.Entry<String, List<String>> entry : cookieHeaders.entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        headers.put(entry.getKey().toLowerCase(), entry.getValue());
                    }
                }
            } catch (URISyntaxException exception) {
                throw new IOException("Failed to parse URL as URI", exception);
            }
        }

        if (includeBodyHeaders && request instanceof TypedHttpRequest typed && typed.getBody() != null) {
            HttpBody body = typed.getBody();
            headers.put("content-type", Collections.singletonList(body.getMediaType().toString()));
            if (body.getContentLength() < 0) {
                headers.put("content-length", Collections.singletonList(String.valueOf(body.getContentLength())));
            }
        }

        mergeHeaders(headers, client.getHeaders());
        mergeHeaders(headers, request.getHeaders());
        return headers;
    }

    private static void mergeHeaders(Map<String, List<String>> target, Map<String, List<String>> source) {
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                target.put(entry.getKey().toLowerCase(), entry.getValue());
            }
        }
    }

    protected final void storeCookies(CookieManager cookies, URL url, Map<String, List<String>> headers) throws IOException {
        if (cookies == null) {
            return;
        }
        try {
            cookies.put(url.toURI(), headers);
        } catch (URISyntaxException exception) {
            throw new IOException("Failed to parse URL as URI", exception);
        }
    }

    protected final void applyHeaders(
        Map<String, List<String>> headers,
        BiConsumer<String, String> firstValue,
        BiConsumer<String, String> additionalValue
    ) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if ("cookie".equalsIgnoreCase(entry.getKey())) {
                firstValue.accept(entry.getKey(), String.join("; ", entry.getValue()));
                continue;
            }
            boolean first = true;
            for (String value : entry.getValue()) {
                if (first) {
                    firstValue.accept(entry.getKey(), value);
                    first = false;
                } else {
                    additionalValue.accept(entry.getKey(), value);
                }
            }
        }
    }
}
