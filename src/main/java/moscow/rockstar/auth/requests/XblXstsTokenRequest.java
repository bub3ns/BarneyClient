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
import moscow.rockstar.auth.tokens.JsonArrayToken;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.auth.tokens.XblDeviceToken;
import moscow.rockstar.auth.tokens.XblTitleToken;
import moscow.rockstar.auth.tokens.XblUserToken;
import moscow.rockstar.auth.tokens.XblXstsToken;
import moscow.rockstar.network.ResourceException;
import moscow.rockstar.network.http.JsonRequestBody;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.response.XboxErrorHandler;

public class XblXstsTokenRequest
extends ResourceException
implements XboxErrorHandler<XblXstsToken> {
    public XblXstsTokenRequest(XblDeviceToken xblDeviceToken, XblUserToken xblUserToken, XblTitleToken xblTitleToken, String string) throws MalformedURLException {
        super("https://xsts.auth.xboxlive.com/xsts/authorize");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("SandboxId", "RETAIL");
        if (xblDeviceToken != null) {
            jsonObject.addProperty("DeviceToken", xblDeviceToken.getToken());
        }
        jsonObject.add("UserTokens", (JsonElement)new JsonArrayToken().addString(xblUserToken.formatAuthorizationHeader()).getValues());
        if (xblTitleToken != null) {
            jsonObject.addProperty("TitleToken", xblTitleToken.getToken());
        }
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.add("Properties", (JsonElement)jsonObject);
        jsonObject2.addProperty("RelyingParty", string);
        jsonObject2.addProperty("TokenType", "JWT");
        this.setBody(new JsonRequestBody(jsonObject2));
        this.setHeader("x-xbl-contract-version", "1");
    }

    @Override
    public XblXstsToken parseSuccessResponse(HttpResponse response, JsonObjectToken json) {
        return XblXstsToken.fromXboxResponse(json);
    }
}
