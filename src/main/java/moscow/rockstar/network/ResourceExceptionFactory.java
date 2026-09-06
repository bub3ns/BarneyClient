/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network;

import java.net.MalformedURLException;
import java.net.URL;
import moscow.rockstar.network.ConnectionException;
import moscow.rockstar.network.HttpException;
import moscow.rockstar.network.ResourceException;
import moscow.rockstar.network.ResourceLoadException;
import moscow.rockstar.network.UrlException;
import moscow.rockstar.network.http.HttpRequest;

public interface ResourceExceptionFactory {
    default public <T extends HttpRequest> T attachAdapter(T t) {
        return t;
    }

    default public HttpException createHttpException(String string) throws MalformedURLException {
        return this.attachAdapter(new HttpException(string));
    }

    default public HttpException createHttpException(URL uRL) {
        return this.attachAdapter(new HttpException(uRL));
    }

    default public ConnectionException createConnectionException(String string) throws MalformedURLException {
        return this.attachAdapter(new ConnectionException(string));
    }

    default public ConnectionException createConnectionException(URL uRL) {
        return this.attachAdapter(new ConnectionException(uRL));
    }

    default public UrlException createUrlException(String string) throws MalformedURLException {
        return this.attachAdapter(new UrlException(string));
    }

    default public UrlException createUrlException(URL uRL) {
        return this.attachAdapter(new UrlException(uRL));
    }

    default public ResourceException createResourceException(String string) throws MalformedURLException {
        return this.attachAdapter(new ResourceException(string));
    }

    default public ResourceException createResourceException(URL uRL) {
        return this.attachAdapter(new ResourceException(uRL));
    }

    default public ResourceLoadException createResourceLoadException(String string) throws MalformedURLException {
        return this.attachAdapter(new ResourceLoadException(string));
    }

    default public ResourceLoadException createResourceLoadException(URL uRL) {
        return this.attachAdapter(new ResourceLoadException(uRL));
    }

    default public HttpRequest createHttpRequest(String string, String string2) throws MalformedURLException {
        return this.attachAdapter(new HttpRequest(string, string2));
    }

    default public HttpRequest createHttpRequest(String string, URL uRL) {
        return this.attachAdapter(new HttpRequest(string, uRL));
    }

}
