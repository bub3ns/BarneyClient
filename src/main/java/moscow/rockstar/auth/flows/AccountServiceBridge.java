/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.auth.flows;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import moscow.rockstar.auth.flows.BrowserLoginFlow;
import moscow.rockstar.auth.tokens.MinecraftToken;
import moscow.rockstar.network.http.HttpClientAdapter;
import moscow.rockstar.network.http.HttpRequest;
import moscow.rockstar.server.ServerListRequest;

public abstract class AccountServiceBridge {
    protected final HttpClientAdapter httpClient;
    protected final String serviceName;

    public AccountServiceBridge(HttpClientAdapter httpClientAdapter, String string) {
        this.httpClient = httpClientAdapter;
        this.serviceName = string;
    }

    public boolean isServiceCompatible() throws IOException {
        String string = (String)this.httpClient.executeAndParse(this.prepareRequest(new HttpRequest("GET", "https://" + this.serviceName + "/compatibility")));
        return string.equals("COMPATIBLE");
    }

    public boolean isServiceCompatibleSafely() {
        try {
            return this.isServiceCompatible();
        } catch (IOException exception) {
            return false;
        }
    }

    public CompletableFuture<Boolean> checkServiceCompatibilityAsync() {
        return CompletableFuture.supplyAsync(this::isServiceCompatibleSafely);
    }

    public List<MinecraftToken> fetchServiceEntries() throws IOException {
        return (List)this.httpClient.executeAndParse(this.prepareRequest(new ServerListRequest(this.serviceName)));
    }

    public List<MinecraftToken> fetchServiceEntriesSafely() {
        try {
            return this.fetchServiceEntries();
        } catch (IOException exception) {
            return List.of();
        }
    }

    public CompletableFuture<List<MinecraftToken>> fetchServiceEntriesAsync() {
        return CompletableFuture.supplyAsync(this::fetchServiceEntriesSafely);
    }

    protected abstract <T extends HttpRequest> T prepareRequest(T var1) throws IOException;

    @Generated
    public HttpClientAdapter getHttpClient() {
        return this.httpClient;
    }

    @Generated
    public String getServiceName() {
        return this.serviceName;
    }
}
