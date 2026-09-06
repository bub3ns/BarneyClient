/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.auth.requests;

import java.net.MalformedURLException;
import java.util.HashMap;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.MicrosoftOAuthProfile;
import moscow.rockstar.auth.device.DeviceCodeInfo;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.network.ResourceException;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.UrlEncodedFormBody;
import moscow.rockstar.network.http.response.OAuthErrorHandler;

public class MicrosoftDeviceCodeRequest
extends ResourceException
implements OAuthErrorHandler<DeviceCodeInfo> {
    public MicrosoftDeviceCodeRequest(MicrosoftClientConfiguration microsoftClientConfiguration) throws MalformedURLException {
        super(microsoftClientConfiguration.getEnvironment().getConnectEndpoint());
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("client_id", microsoftClientConfiguration.getClientId());
        hashMap.put("scope", microsoftClientConfiguration.getScope());
        if (microsoftClientConfiguration.getEnvironment() == MicrosoftOAuthProfile.LIVE) {
            hashMap.put("response_type", "device_code");
        }
        this.setBody(new UrlEncodedFormBody(hashMap));
    }

    @Override
    public DeviceCodeInfo parseSuccessResponse(HttpResponse response, JsonObjectToken json) {
        return new DeviceCodeInfo(System.currentTimeMillis() + json.getLong("expires_in") * 1000L, json.getLong("interval") * 1000L, json.getString("device_code"), json.getString("user_code"), json.getString("verification_uri"));
    }
}
