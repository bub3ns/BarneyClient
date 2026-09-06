/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.auth.requests;

import java.net.MalformedURLException;
import java.util.HashMap;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.tokens.MicrosoftTokenSet;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.network.ResourceException;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.UrlEncodedFormBody;
import moscow.rockstar.network.http.response.OAuthErrorHandler;

public class MicrosoftRefreshTokenRequest
extends ResourceException
implements OAuthErrorHandler<MicrosoftTokenSet> {
    public MicrosoftRefreshTokenRequest(MicrosoftClientConfiguration microsoftClientConfiguration, MicrosoftTokenSet microsoftTokenSet) throws MalformedURLException {
        this(microsoftClientConfiguration, microsoftTokenSet.getRefreshToken());
    }

    public MicrosoftRefreshTokenRequest(MicrosoftClientConfiguration microsoftClientConfiguration, String string) throws MalformedURLException {
        super(microsoftClientConfiguration.getEnvironment().getTokenEndpoint());
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("client_id", microsoftClientConfiguration.getClientId());
        hashMap.put("scope", microsoftClientConfiguration.getScope());
        if (microsoftClientConfiguration.getClientSecret() != null) {
            hashMap.put("client_secret", microsoftClientConfiguration.getClientSecret());
        }
        hashMap.put("grant_type", "refresh_token");
        hashMap.put("refresh_token", string);
        this.setBody(new UrlEncodedFormBody(hashMap));
    }

    @Override
    public MicrosoftTokenSet parseSuccessResponse(HttpResponse response, JsonObjectToken json) {
        return new MicrosoftTokenSet(System.currentTimeMillis() + json.getLong("expires_in") * 1000L, json.getString("access_token"), json.getString("refresh_token", null));
    }
}
