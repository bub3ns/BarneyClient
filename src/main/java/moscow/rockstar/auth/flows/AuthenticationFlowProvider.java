/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.auth.flows;

import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.flows.AuthenticationFlow;
import moscow.rockstar.network.http.HttpClientAdapter;

@FunctionalInterface
public interface AuthenticationFlowProvider {
    public AuthenticationFlow createFlow(HttpClientAdapter var1, MicrosoftClientConfiguration var2);
}

