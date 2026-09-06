/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network.http;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.Authenticator;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import moscow.rockstar.network.http.ProxyAuthenticator;

public class ProxyAuthenticationSelector
extends ProxySelector {
    private static final MethodHandle AUTHENTICATOR_CONSTRUCTOR;
    private final Proxy proxy;
    private final String proxyUsername;
    private final String proxyPassword;
    private final ProxySelector previousSelector;
    private final Authenticator previousAuthenticator;

    public ProxyAuthenticationSelector(Proxy proxy, String string, String string2) {
        this.proxy = proxy;
        this.proxyUsername = string;
        this.proxyPassword = string2;
        this.previousSelector = ProxySelector.getDefault();
        this.previousAuthenticator = ProxyAuthenticationSelector.readDefaultAuthenticator();
    }

    public ProxyAuthenticationSelector installProxy(boolean bl) {
        if (bl) {
            ProxySelector.setDefault(this);
        }
        if (this.proxyUsername != null && this.proxyPassword != null) {
            Authenticator.setDefault(new ProxyAuthenticator(this.proxyUsername, this.proxyPassword));
        }
        return this;
    }

    public ProxyAuthenticationSelector restoreProxy(boolean bl) {
        if (bl) {
            ProxySelector.setDefault(this.previousSelector);
        }
        if (this.proxyUsername != null && this.proxyPassword != null) {
            Authenticator.setDefault(this.previousAuthenticator);
        }
        return this;
    }

    @Override
    public List<Proxy> select(URI uRI) {
        return Collections.singletonList(this.proxy);
    }

    @Override
    public void connectFailed(URI uRI, SocketAddress socketAddress, IOException iOException) {
    }

    private static Authenticator readDefaultAuthenticator() {
        try {
            return (Authenticator) AUTHENTICATOR_CONSTRUCTOR.invokeExact();
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    static {
        MethodHandle methodHandle;
        try {
            methodHandle = MethodHandles.lookup().findStatic(Authenticator.class, "getDefault", MethodType.methodType(Authenticator.class));
        }
        catch (Throwable throwable) {
            try {
                Field field = Authenticator.class.getDeclaredField("theAuthenticator");
                field.setAccessible(true);
                methodHandle = MethodHandles.lookup().unreflectGetter(field);
            }
            catch (Throwable throwable2) {
                methodHandle = MethodHandles.constant(Authenticator.class, null);
            }
        }
        AUTHENTICATOR_CONSTRUCTOR = methodHandle;
    }
}
