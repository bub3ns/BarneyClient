/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package moscow.rockstar.network.http;

import java.util.Objects;
import javax.annotation.Nonnull;

public class HttpHeader {
    @Nonnull
    private final String name;
    @Nonnull
    private final String headerValue;

    public HttpHeader(@Nonnull String string, @Nonnull String string2) {
        this.name = string;
        this.headerValue = string2;
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Nonnull
    public String getHeaderValue() {
        return this.headerValue;
    }

    public String toString() {
        return "HttpHeader{name='" + this.name + '\'' + ", value='" + this.headerValue + '\'' + '}';
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        HttpHeader httpHeader = (HttpHeader)object;
        return Objects.equals(this.name, httpHeader.name) && Objects.equals(this.headerValue, httpHeader.headerValue);
    }

    public int hashCode() {
        return Objects.hash(this.name, this.headerValue);
    }
}

