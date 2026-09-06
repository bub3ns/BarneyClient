/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.factory;

import java.lang.reflect.Constructor;
import moscow.rockstar.network.http.HttpClientAdapter;
import moscow.rockstar.network.http.HttpTransport;
import moscow.rockstar.network.http.client.UrlConnectionClient;

public enum ComponentFactory {
    AUTO{

        @Override
        public HttpTransport createAdapterFor(HttpClientAdapter httpClientAdapter) {
            for (int i = ComponentFactory.values().length - 1; i >= 0; --i) {
                HttpTransport httpClientAdapter2;
                ComponentFactory componentFactory = ComponentFactory.values()[i];
                if (AUTO.equals((Object)componentFactory) || !componentFactory.isAvailable() || (httpClientAdapter2 = componentFactory.createAdapter(httpClientAdapter)) == null) continue;
                return httpClientAdapter2;
            }
            throw new IllegalStateException("Failed to find a suitable executor. This should never happen. Please report this to the developer.");
        }
    }
    ,
    URL_CONNECTION{

        @Override
        public HttpTransport createAdapterFor(HttpClientAdapter httpClientAdapter) {
            return new UrlConnectionClient(httpClientAdapter);
        }
    }
    ,
    REACTOR_NETTY{
        private Constructor<?> adapterConstructor;

        @Override
        protected void initialize() throws Throwable {
            Class.forName("reactor.netty.http.client.HttpClient");
            Class<?> clazz = Class.forName("moscow.rockstar.network.http.client.ReactorNettyClient");
            this.adapterConstructor = clazz.getDeclaredConstructor(HttpClientAdapter.class);
        }

        @Override
        protected HttpTransport createAdapterFor(HttpClientAdapter httpClientAdapter) throws Throwable {
            return (HttpTransport)this.adapterConstructor.newInstance(httpClientAdapter);
        }
    }
    ,
    JAVA_HTTP_CLIENT{
        private Constructor<?> adapterConstructor;

        @Override
        protected void initialize() throws Throwable {
            Class.forName("java.net.http.HttpClient");
            Class<?> clazz = Class.forName("moscow.rockstar.network.http.client.JavaNetHttpClient");
            this.adapterConstructor = clazz.getDeclaredConstructor(HttpClientAdapter.class);
        }

        @Override
        protected HttpTransport createAdapterFor(HttpClientAdapter httpClientAdapter) throws Throwable {
            return (HttpTransport)this.adapterConstructor.newInstance(httpClientAdapter);
        }
    };

    private boolean available;

    private ComponentFactory() {
        try {
            this.initialize();
            this.available = true;
        }
        catch (Throwable throwable) {
            this.available = false;
        }
    }

    public final boolean isAvailable() {
        return this.available;
    }

    public final HttpTransport createAdapter(HttpClientAdapter httpClientAdapter) {
        try {
            return this.createAdapterFor(httpClientAdapter);
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    protected void initialize() throws Throwable {
    }

    protected abstract HttpTransport createAdapterFor(HttpClientAdapter var1) throws Throwable;
}
