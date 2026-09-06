/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.auth.flows;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import lombok.Generated;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.tokens.MicrosoftTokenSet;
import moscow.rockstar.network.http.HttpClientAdapter;

public abstract class AuthenticationFlow {
    protected final HttpClientAdapter uiAdapter;
    protected final MicrosoftClientConfiguration applicationConfig;

    public AuthenticationFlow(HttpClientAdapter httpClientAdapter, MicrosoftClientConfiguration microsoftClientConfiguration) {
        this.uiAdapter = httpClientAdapter;
        this.applicationConfig = microsoftClientConfiguration;
    }

    public abstract MicrosoftTokenSet authenticate() throws IOException, InterruptedException, TimeoutException;

    public MicrosoftTokenSet authenticateSynchronously() {
        try {
            return this.authenticate();
        }
        catch (IOException | InterruptedException | TimeoutException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Authentication failed", exception);
        }
    }

    public CompletableFuture<MicrosoftTokenSet> authenticateAsync() {
        return CompletableFuture.supplyAsync(this::authenticateSynchronously);
    }

    @Generated
    public HttpClientAdapter getUiAdapter() {
        return this.uiAdapter;
    }

    @Generated
    public MicrosoftClientConfiguration getApplicationConfig() {
        return this.applicationConfig;
    }
}
