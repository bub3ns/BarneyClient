package moscow.rockstar.network.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/** HTTP response with URL, status, headers, and a response entity body. */
public final class HttpResponse extends HttpHeaders<HttpResponse> {
    private static final Map<String, Function<InputStream, InputStream>> DECODERS = new HashMap<>();

    private final URL url;
    private final int statusCode;
    private final HttpBody body;

    public HttpResponse(URL url, int statusCode, byte[] bytes, Map<String, List<String>> headers) {
        this(url, statusCode, new ByteArrayResponseBody(responseMediaType(headers), bytes), headers);
    }

    public HttpResponse(URL url, int statusCode, InputStream input, Map<String, List<String>> headers) {
        this(url, statusCode, new StreamingResponseBody(responseMediaType(headers), input, responseLength(headers)), headers);
    }

    public HttpResponse(URL url, int statusCode, HttpBody body, Map<String, List<String>> headers) {
        super(headers);
        this.url = url;
        this.statusCode = statusCode;
        this.body = body;
    }

    public URL getUrl() {
        return url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return STATUS_REASONS.getOrDefault(statusCode, "Unknown");
    }

    public HttpBody getBody() {
        return body;
    }

    /** Return a response whose body transparently decodes Content-Encoding. */
    public HttpResponse decompress() {
        return decompress(DECODERS);
    }

    public HttpResponse decompress(Map<String, Function<InputStream, InputStream>> decoders) {
        String encoding = getFirstHeader("Content-Encoding").orElse(null);
        if (encoding == null) return this;
        String[] values = encoding.split(",\\s*");
        List<Function<InputStream, InputStream>> chain = new ArrayList<>(values.length);
        for (String value : values) {
            Function<InputStream, InputStream> decoder = decoders.get(value.toLowerCase(Locale.ROOT));
            if (decoder == null) return this;
            chain.add(decoder);
        }
        removeHeader("Content-Encoding");
        setHeader("Original-Content-Encoding", encoding);
        HttpBody decoded = new HttpBody(body.getMediaType()) {
            @Override protected boolean isRepeatable() { return body.getContentLength() >= 0; }
            @Override protected InputStream openContentStream() throws IOException {
                InputStream stream = body.openStream();
                for (int index = chain.size() - 1; index >= 0; index--) {
                    try {
                        stream = chain.get(index).apply(stream);
                    } catch (java.io.UncheckedIOException exception) {
                        throw exception.getCause();
                    } catch (RuntimeException exception) {
                        throw new IOException("Failed to create content decoder", exception);
                    }
                }
                return stream;
            }
        };
        return new HttpResponse(url, statusCode, decoded, getHeaders());
    }

    @Deprecated
    public InputStream getBodyStream() throws IOException { return body.openStream(); }

    @Deprecated
    public String getBodyText() throws IOException { return body.readText(); }

    @Deprecated
    public String getBodyText(Charset charset) throws IOException { return body.readText(charset); }

    public Optional<MediaType> getContentType() {
        return getFirstHeader("Content-Type").map(MediaType::parse);
    }

    private static MediaType responseMediaType(Map<String, List<String>> headers) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase("Content-Type")
                    && entry.getValue() != null && !entry.getValue().isEmpty()) {
                    return MediaType.parse(entry.getValue().get(0));
                }
            }
        }
        return MediaTypes.APPLICATION_OCTET_STREAM;
    }

    private static int responseLength(Map<String, List<String>> headers) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase("Content-Length")
                    && entry.getValue() != null && !entry.getValue().isEmpty()) {
                    try { return Integer.parseInt(entry.getValue().get(0)); }
                    catch (NumberFormatException ignored) { return -1; }
                }
            }
        }
        return -1;
    }

    private static final Map<Integer, String> STATUS_REASONS = Map.ofEntries(
        Map.entry(200, "OK"), Map.entry(201, "Created"), Map.entry(202, "Accepted"),
        Map.entry(204, "No Content"), Map.entry(301, "Moved Permanently"),
        Map.entry(302, "Moved Temporarily"), Map.entry(303, "See Other"),
        Map.entry(304, "Not Modified"), Map.entry(307, "Temporary Redirect"),
        Map.entry(308, "Permanent Redirect"), Map.entry(400, "Bad Request"),
        Map.entry(401, "Unauthorized"), Map.entry(403, "Forbidden"),
        Map.entry(404, "Not Found"), Map.entry(405, "Method Not Allowed"),
        Map.entry(408, "Request Timeout"), Map.entry(409, "Conflict"),
        Map.entry(413, "Payload Too Large"), Map.entry(415, "Unsupported Media Type"),
        Map.entry(429, "Too Many Requests"), Map.entry(500, "Internal Server Error"),
        Map.entry(501, "Not Implemented"), Map.entry(502, "Bad Gateway"),
        Map.entry(503, "Service Unavailable"), Map.entry(504, "Gateway Timeout")
    );

    private static final class ByteArrayResponseBody extends HttpBody {
        private final byte[] bytes;

        private ByteArrayResponseBody(MediaType mediaType, byte[] bytes) {
            super(mediaType, bytes == null ? 0 : bytes.length);
            this.bytes = bytes == null ? new byte[0] : bytes;
        }

        @Override protected boolean isRepeatable() { return true; }
        @Override protected InputStream openContentStream() { return new ByteArrayInputStream(bytes); }
    }

    private static final class StreamingResponseBody extends HttpBody {
        private final InputStream input;

        private StreamingResponseBody(MediaType mediaType, InputStream input, int length) {
            super(mediaType, length);
            this.input = input;
        }

        @Override protected boolean isRepeatable() { return false; }
        @Override protected InputStream openContentStream() { return input; }
    }

    static {
        DECODERS.put("identity", Function.identity());
        DECODERS.put("gzip", stream -> {
            try { return new GZIPInputStream(stream); }
            catch (IOException exception) { throw new java.io.UncheckedIOException(exception); }
        });
        DECODERS.put("x-gzip", DECODERS.get("gzip"));
        DECODERS.put("deflate", InflaterInputStream::new);
        registerOptionalDecoder("br", "com.aayushatharva.brotli4j.decoder.BrotliInputStream");
        registerOptionalDecoder("br", "org.brotli.dec.BrotliInputStream");
        registerOptionalDecoder("zstd", "io.airlift.compress.zstd.ZstdInputStream");
        registerOptionalDecoder("zstd", "io.airlift.compress.v3.zstd.ZstdInputStream");
        registerOptionalDecoder("zstd", "com.github.luben.zstd.ZstdInputStream");
    }

    private static void registerOptionalDecoder(String name, String className) {
        if (DECODERS.containsKey(name)) return;
        try {
            Constructor<? extends InputStream> constructor = Class.forName(className)
                .asSubclass(InputStream.class)
                .getDeclaredConstructor(InputStream.class);
            constructor.setAccessible(true);
            DECODERS.put(name, stream -> {
                try { return constructor.newInstance(stream); }
                catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException("Failed to create decoder input stream", exception);
                }
            });
        } catch (Throwable ignored) {
        }
    }
}
