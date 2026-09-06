package moscow.rockstar.network.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Repeatable or streaming HTTP entity body. */
public abstract class HttpBody {
    private final MediaType mediaType;
    private int contentLength;
    private boolean opened;
    private byte[] cachedBytes;

    protected HttpBody(MediaType mediaType) {
        this(mediaType, -1);
    }

    protected HttpBody(MediaType mediaType, int contentLength) {
        this.mediaType = mediaType;
        this.contentLength = contentLength;
    }

    public static HttpBody fromBytes(byte[] bytes) {
        return fromBytes(MediaTypes.APPLICATION_OCTET_STREAM, bytes);
    }

    public static HttpBody fromBytes(MediaType mediaType, byte[] bytes) {
        byte[] copy = bytes == null ? new byte[0] : bytes.clone();
        return new HttpBody(mediaType, copy.length) {
            @Override protected boolean isRepeatable() { return true; }
            @Override protected InputStream openContentStream() { return new ByteArrayInputStream(copy); }
        };
    }

    public static HttpBody fromBytes(byte[] bytes, int offset, int length) {
        byte[] copy = bytes == null ? new byte[0] : bytes.clone();
        return new HttpBody(MediaTypes.APPLICATION_OCTET_STREAM, length) {
            @Override protected boolean isRepeatable() { return true; }
            @Override protected InputStream openContentStream() { return new ByteArrayInputStream(copy, offset, length); }
        };
    }

    public static HttpBody fromText(String text) {
        return fromText(MediaTypes.TEXT_PLAIN, text);
    }

    public static HttpBody fromText(MediaType mediaType, String text) {
        Charset charset = mediaType.getCharset().orElse(StandardCharsets.UTF_8);
        return fromBytes(mediaType, text.getBytes(charset));
    }

    public static HttpBody fromForm(Map<String, String> fields) {
        return new UrlEncodedFormBody(fields);
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    /** Compatibility name used by older call sites. */
    public MediaType getContentType() {
        return getMediaType();
    }

    public int getContentLength() {
        return contentLength;
    }

    protected final void setContentLength(int length) {
        contentLength = length;
    }

    public final void writeTo(OutputStream output) throws IOException {
        try (InputStream input = openStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                if (count > 0) output.write(buffer, 0, count);
            }
        }
    }

    public final InputStream openStream() throws IOException {
        if (cachedBytes != null) {
            return new ByteArrayInputStream(cachedBytes);
        }
        if (!opened || isRepeatable()) {
            opened = true;
            return wrapContentStream(openContentStream());
        }
        throw new IOException("This content cannot be streamed multiple times");
    }

    public final byte[] readBytes() throws IOException {
        if (cachedBytes == null) {
            int initialSize = getContentLength() > 0 ? getContentLength() : 1024;
            ByteArrayOutputStream output = new ByteArrayOutputStream(initialSize);
            writeTo(output);
            cachedBytes = output.toByteArray();
        }
        return cachedBytes.clone();
    }

    public final String readText() throws IOException {
        return readText(StandardCharsets.UTF_8);
    }

    public final String readText(Charset charset) throws IOException {
        return new String(readBytes(), charset);
    }

    protected abstract boolean isRepeatable();

    protected abstract InputStream openContentStream() throws IOException;

    protected InputStream wrapContentStream(InputStream input) throws IOException {
        return input;
    }
}
