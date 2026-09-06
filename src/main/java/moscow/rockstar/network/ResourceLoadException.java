/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network;

import java.net.MalformedURLException;
import java.net.URL;
import moscow.rockstar.network.http.request.PutRequest;

public class ResourceLoadException
extends PutRequest {
    public ResourceLoadException(String string) throws MalformedURLException {
        super(string);
    }

    public ResourceLoadException(URL uRL) {
        super(uRL);
    }
}
