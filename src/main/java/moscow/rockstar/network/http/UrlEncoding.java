package moscow.rockstar.network.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** URL form encoding used by query parameters and form bodies. */
public final class UrlEncoding {
    private UrlEncoding() {
    }

    public static String encode(String value) {
        return encode(value, StandardCharsets.UTF_8);
    }

    public static String encode(String value, Charset charset) {
        try {
            return URLEncoder.encode(value, charset.name());
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static String decode(String value) {
        return decode(value, StandardCharsets.UTF_8);
    }

    public static String decode(String value, Charset charset) {
        try {
            return URLDecoder.decode(value, charset.name());
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
