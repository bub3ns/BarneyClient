/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package moscow.rockstar.network.http;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

public class MediaType {
    private final String mediaType;
    private final Charset charset;
    private final String boundary;

    public static MediaType parse(String string) {
        if (!string.contains(";")) {
            return new MediaType(string.toLowerCase(Locale.ROOT), null, null);
        }
        String[] stringArray = string.split(";");
        String string2 = stringArray[0].toLowerCase(Locale.ROOT);
        Charset charset = null;
        String string3 = null;
        for (int i = 1; i < stringArray.length; ++i) {
            String string4 = stringArray[i].trim();
            if (string4.startsWith("charset=")) {
                try {
                    charset = Charset.forName(string4.substring(8));
                }
                catch (UnsupportedCharsetException unsupportedCharsetException) {}
                continue;
            }
            if (!string4.startsWith("boundary=")) continue;
            string3 = string4.substring(9);
        }
        return new MediaType(string2, charset, string3);
    }

    public MediaType(String string) {
        this(string, null, null);
    }

    public MediaType(String string, @Nullable Charset charset) {
        this(string, charset, null);
    }

    public MediaType(String string, @Nullable String string2) {
        this(string, null, string2);
    }

    public MediaType(String string, @Nullable Charset charset, @Nullable String string2) {
        this.mediaType = string;
        this.charset = charset;
        this.boundary = string2;
    }

    public String getMediaType() {
        return this.mediaType;
    }

    public Optional<Charset> getCharset() {
        return Optional.ofNullable(this.charset);
    }

    public Optional<String> getBoundary() {
        return Optional.ofNullable(this.boundary);
    }

    public String toString() {
        return this.mediaType + (this.charset != null ? "; charset=" + this.charset.name() : "") + (this.boundary != null ? "; boundary=" + this.boundary : "");
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        MediaType mediaType = (MediaType)object;
        return Objects.equals(this.mediaType, mediaType.mediaType) && Objects.equals(this.charset, mediaType.charset) && Objects.equals(this.boundary, mediaType.boundary);
    }

    public int hashCode() {
        return Objects.hash(this.mediaType, this.charset, this.boundary);
    }
}

