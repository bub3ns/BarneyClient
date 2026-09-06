/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network;

import java.net.MalformedURLException;
import java.net.URL;
import moscow.rockstar.network.http.request.GetRequest;

public class HttpException
extends GetRequest {
    public HttpException(String string) throws MalformedURLException {
        super(string);
    }

    public HttpException(URL uRL) {
        super(uRL);
    }
}
