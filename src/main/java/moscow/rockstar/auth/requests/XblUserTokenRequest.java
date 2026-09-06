/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package moscow.rockstar.auth.requests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.MalformedURLException;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.tokens.MicrosoftTokenSet;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.auth.tokens.XblUserToken;
import moscow.rockstar.network.ResourceException;
import moscow.rockstar.network.http.JsonRequestBody;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.response.XboxErrorHandler;

public class XblUserTokenRequest
extends ResourceException
implements XboxErrorHandler<XblUserToken> {
    public XblUserTokenRequest(MicrosoftClientConfiguration microsoftClientConfiguration, MicrosoftTokenSet microsoftTokenSet) throws MalformedURLException {
        super("https://user.auth.xboxlive.com/user/authenticate");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("SiteName", "user.auth.xboxlive.com");
        jsonObject.addProperty("AuthMethod", "RPS");
        jsonObject.addProperty("RpsTicket", (microsoftClientConfiguration.isValid() ? "t=" : "d=") + microsoftTokenSet.getAccessToken());
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.add("Properties", (JsonElement)jsonObject);
        jsonObject2.addProperty("RelyingParty", "http://auth.xboxlive.com");
        jsonObject2.addProperty("TokenType", "JWT");
        this.setBody(new JsonRequestBody(jsonObject2));
        this.setHeader("x-xbl-contract-version", "1");
    }

    @Override
    public XblUserToken parseSuccessResponse(HttpResponse response, JsonObjectToken json) {
        return XblUserToken.fromXboxResponse(json);
    }
}
