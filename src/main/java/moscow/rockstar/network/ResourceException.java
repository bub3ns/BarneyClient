/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network;

import java.net.MalformedURLException;
import java.net.URL;
import moscow.rockstar.network.http.request.PostRequest;

public class ResourceException
extends PostRequest {
    public ResourceException(String string) throws MalformedURLException {
        super(string);
    }

    public ResourceException(URL uRL) {
        super(uRL);
    }
}
