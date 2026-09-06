/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 */
package moscow.rockstar.network.http;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import moscow.rockstar.network.ProxyType;
import moscow.rockstar.network.http.ProxyAuthenticationSelector;
import moscow.rockstar.network.http.ProxyAuthenticator;

public class ProxySettings {
    private ProxyType proxyType;
    private SocketAddress address;
    private String username;
    private String password;

    public ProxySettings() {
    }

    public ProxySettings(ProxyType proxyType, String string, int n) {
        this(proxyType, string, n, null, null);
    }

    public ProxySettings(ProxyType proxyType, String string, int n, @Nullable String string2, @Nullable String string3) {
        this(proxyType, new InetSocketAddress(string, n), string2, string3);
    }

    public ProxySettings(ProxyType proxyType, SocketAddress socketAddress) {
        this(proxyType, socketAddress, null, null);
    }

    public ProxySettings(ProxyType proxyType, SocketAddress socketAddress, @Nullable String string, @Nullable String string2) {
        this.proxyType = proxyType;
        this.address = socketAddress;
        this.username = string;
        this.password = string2;
    }

    public ProxySettings setProxy(ProxyType proxyType, String string, int n) {
        return this.setProxy(proxyType, new InetSocketAddress(string, n));
    }

    public ProxySettings setProxy(ProxyType proxyType, SocketAddress socketAddress) {
        this.proxyType = proxyType;
        this.address = socketAddress;
        return this;
    }

    public ProxySettings clear() {
        this.proxyType = null;
        this.address = null;
        return this;
    }

    public boolean hasProxy() {
        return this.proxyType != null && this.address != null;
    }

    @Nullable
    public ProxyType getProxyType() {
        return this.proxyType;
    }

    public ProxySettings setProxyType(@Nonnull ProxyType proxyType) {
        this.proxyType = proxyType;
        return this;
    }

    @Nullable
    public SocketAddress getAddress() {
        return this.address;
    }

    public ProxySettings setAddress(@Nonnull SocketAddress socketAddress) {
        this.address = socketAddress;
        return this;
    }

    public boolean hasCredentials() {
        return this.username != null && this.password != null;
    }

    @Nullable
    public String getUsername() {
        return this.username;
    }

    public ProxySettings setUsername(@Nullable String string) {
        this.username = string;
        return this;
    }

    @Nullable
    public String getPassword() {
        return this.password;
    }

    public ProxySettings setPassword(@Nullable String string) {
        this.password = string;
        return this;
    }

    public ProxyAuthenticationSelector createProxyAuthenticator() {
        if (!this.hasProxy()) {
            throw new IllegalStateException("Proxy is not set");
        }
        return new ProxyAuthenticationSelector(this.toProxy(), this.username, this.password);
    }

    public ProxyAuthenticator createAuthenticator() {
        if (!this.hasProxy()) {
            throw new IllegalStateException("Proxy is not set");
        }
        if (!this.hasCredentials()) {
            throw new IllegalStateException("Username or password is not set");
        }
        return new ProxyAuthenticator(this.username, this.password);
    }

    public Proxy toProxy() {
        switch (this.proxyType) {
            case HTTP: {
                return new Proxy(Proxy.Type.HTTP, this.address);
            }
            case DIRECT: {
                try {
                    Class<?> clazz = Class.forName("sun.net.SocksProxy");
                    Method method = clazz.getDeclaredMethod("create", SocketAddress.class, Integer.TYPE);
                    return (Proxy)method.invoke(null, this.address, 4);
                }
                catch (Throwable throwable) {
                    throw new UnsupportedOperationException("SOCKS4 proxy type is not supported", throwable);
                }
            }
            case SOCKS: {
                return new Proxy(Proxy.Type.SOCKS, this.address);
            }
        }
        throw new IllegalStateException("Unknown proxy type: " + this.proxyType.name());
    }
}

