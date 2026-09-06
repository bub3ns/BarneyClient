/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.auth.flows;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.device.DeviceCodeInfo;
import moscow.rockstar.auth.flows.AuthenticationFlow;
import moscow.rockstar.auth.requests.MicrosoftDeviceCodeRequest;
import moscow.rockstar.auth.requests.MicrosoftDeviceCodeTokenRequest;
import moscow.rockstar.auth.tokens.MicrosoftTokenSet;
import moscow.rockstar.network.http.HttpClientAdapter;
import moscow.rockstar.network.http.response.OAuthApiException;

public class DeviceCodeLoginFlow
extends AuthenticationFlow {
    private final Consumer<DeviceCodeInfo> onDeviceCodeReceived;
    private final int timeoutMillis;

    public DeviceCodeLoginFlow(HttpClientAdapter httpClientAdapter, MicrosoftClientConfiguration microsoftClientConfiguration, Consumer<DeviceCodeInfo> consumer) {
        this(httpClientAdapter, microsoftClientConfiguration, consumer, 300000);
    }

    public DeviceCodeLoginFlow(HttpClientAdapter httpClientAdapter, MicrosoftClientConfiguration microsoftClientConfiguration, Consumer<DeviceCodeInfo> consumer, int n) {
        super(httpClientAdapter, microsoftClientConfiguration);
        this.onDeviceCodeReceived = consumer;
        this.timeoutMillis = n;
    }

    @Override
    public MicrosoftTokenSet authenticate() throws IOException, InterruptedException, TimeoutException {
        DeviceCodeInfo deviceCodeInfo = this.requestDeviceCode();
        this.onDeviceCodeReceived.accept(deviceCodeInfo);
        return this.pollDeviceCode(deviceCodeInfo);
    }

    public DeviceCodeInfo requestDeviceCode() throws IOException {
        return (DeviceCodeInfo)this.uiAdapter.executeAndParse(new MicrosoftDeviceCodeRequest(this.applicationConfig));
    }

    public MicrosoftTokenSet pollDeviceCode(DeviceCodeInfo deviceCodeInfo) throws IOException, InterruptedException, TimeoutException {
        long l = System.currentTimeMillis();
        while (!deviceCodeInfo.isPrimitiveValue() && System.currentTimeMillis() - l <= (long)this.timeoutMillis) {
            try {
                return (MicrosoftTokenSet)this.uiAdapter.executeAndParse(new MicrosoftDeviceCodeTokenRequest(this.applicationConfig, deviceCodeInfo));
            }
            catch (OAuthApiException oauthException) {
                if ("authorization_pending".equals(oauthException.getErrorCode())) {
                    Thread.sleep(deviceCodeInfo.getPollIntervalMillis());
                    continue;
                }
                throw oauthException;
            }
        }
        throw new TimeoutException("Login timed out");
    }
}
