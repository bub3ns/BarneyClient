/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network;

import java.net.MalformedURLException;
import java.net.URL;
import moscow.rockstar.network.http.request.DeleteRequest;

public class UrlException
extends DeleteRequest {
    public UrlException(String string) throws MalformedURLException {
        super(string);
    }

    public UrlException(URL uRL) {
        super(uRL);
    }
}
