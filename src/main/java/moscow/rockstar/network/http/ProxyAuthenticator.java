/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network.http;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class ProxyAuthenticator
extends Authenticator {
    private final PasswordAuthentication passwordAuthentication;

    public ProxyAuthenticator(String string, String string2) {
        this.passwordAuthentication = new PasswordAuthentication(string, string2.toCharArray());
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return this.passwordAuthentication;
    }
}

