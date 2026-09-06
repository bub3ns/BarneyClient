/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network;

import java.net.MalformedURLException;
import java.net.URL;
import moscow.rockstar.network.http.request.HeadRequest;

public class ConnectionException
extends HeadRequest {
    public ConnectionException(String string) throws MalformedURLException {
        super(string);
    }

    public ConnectionException(URL uRL) {
        super(uRL);
    }
}
