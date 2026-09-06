/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package moscow.rockstar.api.registry;

import java.io.IOException;
import moscow.rockstar.network.http.HttpResponse;

@FunctionalInterface
public interface ServiceCache<R> {
    public static ServiceCache<HttpResponse> getInstance() {
        return response -> response;
    }

    public R parseResponse(HttpResponse response) throws IOException;
}
