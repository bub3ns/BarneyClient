package moscow.rockstar.network.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

/** Mutable, case-insensitive HTTP header collection used by requests and responses. */
public abstract class HttpHeaders<T extends HttpHeaders<T>> {
    private final Map<String, List<String>> values = new HashMap<>();

    protected HttpHeaders() {
    }

    protected HttpHeaders(Map<String, List<String>> headers) {
        if (headers != null) {
            headers.forEach((name, entries) -> {
                if (name != null && entries != null) {
                    values.put(normalize(name), new ArrayList<>(entries));
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    protected final T self() {
        return (T) this;
    }

    public Map<String, List<String>> getHeaders() {
        Map<String, List<String>> copy = new HashMap<>();
        values.forEach((name, entries) -> copy.put(name, new ArrayList<>(entries)));
        return Collections.unmodifiableMap(copy);
    }

    public List<String> getHeaderValues(String name) {
        List<String> entries = values.get(normalize(name));
        return entries == null ? null : Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public java.util.Optional<String> getFirstHeader(String name) {
        List<String> entries = values.get(normalize(name));
        return entries == null || entries.isEmpty() ? java.util.Optional.empty() : java.util.Optional.ofNullable(entries.get(0));
    }

    public java.util.Optional<String> getLastHeader(String name) {
        List<String> entries = values.get(normalize(name));
        return entries == null || entries.isEmpty() ? java.util.Optional.empty() : java.util.Optional.ofNullable(entries.get(entries.size() - 1));
    }

    public T addHeader(String name, String value) {
        values.computeIfAbsent(normalize(name), ignored -> new ArrayList<>()).add(value);
        return self();
    }

    public T addHeaders(HttpHeader... headers) {
        for (HttpHeader header : headers) {
            addHeader(header.getName(), header.getHeaderValue());
        }
        return self();
    }

    public T addHeaders(Collection<HttpHeader> headers) {
        for (HttpHeader header : headers) {
            addHeader(header.getName(), header.getHeaderValue());
        }
        return self();
    }

    public T setHeader(String name, String value) {
        List<String> entries = new ArrayList<>();
        entries.add(value);
        values.put(normalize(name), entries);
        return self();
    }

    public T setHeaders(HttpHeader... headers) {
        for (HttpHeader header : headers) {
            setHeader(header.getName(), header.getHeaderValue());
        }
        return self();
    }

    public T setHeaders(Collection<HttpHeader> headers) {
        for (HttpHeader header : headers) {
            setHeader(header.getName(), header.getHeaderValue());
        }
        return self();
    }

    public T removeHeader(String name) {
        values.remove(normalize(name));
        return self();
    }

    public T clearHeaders() {
        values.clear();
        return self();
    }

    public boolean hasHeader(String name) {
        return values.containsKey(normalize(name));
    }

    public boolean hasHeader(String name, String value) {
        List<String> entries = values.get(normalize(name));
        return entries != null && entries.contains(value);
    }

    public boolean hasHeader(HttpHeader header) {
        return hasHeader(header.getName(), header.getHeaderValue());
    }

    public T forEachHeader(BiConsumer<String, String> consumer) {
        values.forEach((name, entries) -> entries.forEach(value -> consumer.accept(name, value)));
        return self();
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
