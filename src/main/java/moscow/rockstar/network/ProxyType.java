/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network;

import java.net.Proxy;

public enum ProxyType {
    HTTP,
    DIRECT,
    SOCKS;


    public static ProxyType fromJavaProxyType(Proxy.Type type) {
        switch (type) {
            case HTTP: {
                return HTTP;
            }
            case SOCKS: {
                return SOCKS;
            }
        }
        throw new IllegalArgumentException("Unknown proxy type: " + type.name());
    }
}

