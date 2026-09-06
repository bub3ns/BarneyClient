package moscow.rockstar.network.http.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import java.io.IOException;
import java.net.CookieManager;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import moscow.rockstar.network.http.HttpClientAdapter;
import moscow.rockstar.network.http.HttpRequest;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.HttpTransport;
import moscow.rockstar.network.http.ProxySettings;
import moscow.rockstar.network.http.TypedHttpRequest;
import moscow.rockstar.network.http.UrlBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;
import reactor.netty.transport.ProxyProvider;

/** Reactor Netty transport from the original HTTP client stack. */
public final class ReactorNettyClient extends HttpTransport {
    private static final byte[] EMPTY_RESPONSE_BODY = new byte[0];

    public ReactorNettyClient(HttpClientAdapter client) {
        super(client);
    }

    @Override
    public HttpResponse send(HttpRequest request) throws IOException {
        CookieManager cookies = resolveCookieManager(request);
        HttpClient httpClient = buildClient(request, cookies);
        HttpClient.RequestSender sender = httpClient
            .request(HttpMethod.valueOf(request.getHttpMethod()))
            .uri(UrlBuilder.fromUrlObject(request.getUrl()).toUri());
        HttpClient.ResponseReceiver<?> responseReceiver = sender;
        if (request instanceof TypedHttpRequest typed && typed.getBody() != null) {
            byte[] requestBytes = typed.getBody().readBytes();
            Flux<ByteBuf> requestBody = Flux.just(Unpooled.wrappedBuffer(requestBytes));
            responseReceiver = sender.send(requestBody);
        }
        try {
            return responseReceiver.responseSingle((response, bytes) -> {
                try {
                    URL url = new URL(response.resourceUrl());
                    Map<String, List<String>> headers = copyHeaders(response.responseHeaders());
                    storeCookies(cookies, url, headers);
                    return bytes.asByteArray()
                        .defaultIfEmpty(EMPTY_RESPONSE_BODY)
                        .map(body -> new HttpResponse(url, response.status().code(), body, headers));
                } catch (Throwable throwable) {
                    return Mono.error(throwable);
                }
            }).blockOptional().orElseThrow(() -> new IOException("Response is null"));
        } catch (Throwable throwable) {
            for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
                if (cause instanceof IOException exception) throw exception;
            }
            throw new IOException("Failed to execute request", throwable);
        }
    }

    private HttpClient buildClient(HttpRequest request, CookieManager cookies) throws IOException {
        Map<String, List<String>> headers = buildRequestHeaders(request, cookies);
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(client.getReadTimeoutMillis()))
            .followRedirect(shouldFollowRedirects(request))
            .headers(nettyHeaders -> applyHeaders(
                headers,
                nettyHeaders::set,
                nettyHeaders::add
            ));
        if (client.getProxySettings().hasProxy()) {
            ProxySettings settings = client.getProxySettings();
            httpClient = httpClient.proxy(spec -> {
                ProxyProvider.AddressSpec address;
                switch (settings.getProxyType()) {
                    case HTTP -> address = spec.type(ProxyProvider.Proxy.HTTP);
                    case DIRECT -> address = spec.type(ProxyProvider.Proxy.SOCKS4);
                    case SOCKS -> address = spec.type(ProxyProvider.Proxy.SOCKS5);
                    default -> throw new IllegalArgumentException("Unsupported proxy type: " + settings.getProxyType());
                }
                ProxyProvider.Builder builder = address.socketAddress(settings.getAddress());
                if (settings.getUsername() != null) builder.username(settings.getUsername());
                if (settings.getPassword() != null) builder.password(value -> settings.getPassword());
            });
        }
        return httpClient;
    }

    private Map<String, List<String>> copyHeaders(HttpHeaders source) {
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, String> entry : source.entries()) {
            result.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>()).add(entry.getValue());
        }
        return result;
    }
}
