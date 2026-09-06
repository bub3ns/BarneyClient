package moscow.rockstar.auth.requests;

import java.net.MalformedURLException;
import java.util.HashMap;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.auth.tokens.MicrosoftTokenSet;
import moscow.rockstar.network.ResourceException;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.UrlEncodedFormBody;
import moscow.rockstar.network.http.response.OAuthErrorHandler;

/** Exchanges a Microsoft authorization-code redirect for OAuth tokens. */
public final class MicrosoftAuthorizationCodeRequest extends ResourceException
        implements OAuthErrorHandler<MicrosoftTokenSet> {
    public MicrosoftAuthorizationCodeRequest(MicrosoftClientConfiguration configuration, String authorizationCode)
            throws MalformedURLException {
        super(configuration.getEnvironment().getTokenEndpoint());
        HashMap<String, String> form = new HashMap<>();
        form.put("client_id", configuration.getClientId());
        form.put("scope", configuration.getScope());
        form.put("grant_type", "authorization_code");
        form.put("code", authorizationCode);
        if (configuration.getRedirectUri() != null) {
            form.put("redirect_uri", configuration.getRedirectUri());
        }
        this.setBody(new UrlEncodedFormBody(form));
    }

    @Override
    public MicrosoftTokenSet parseSuccessResponse(HttpResponse response, JsonObjectToken body) {
        return new MicrosoftTokenSet(
                System.currentTimeMillis() + body.getLong("expires_in") * 1000L,
                body.getString("access_token"),
                body.getString("refresh_token", null));
    }
}
