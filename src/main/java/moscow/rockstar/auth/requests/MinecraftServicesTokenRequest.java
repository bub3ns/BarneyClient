/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package moscow.rockstar.auth.requests;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.MalformedURLException;
import moscow.rockstar.auth.tokens.MinecraftToken;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.auth.tokens.XblXstsToken;
import moscow.rockstar.network.ResourceException;
import moscow.rockstar.network.http.JsonRequestBody;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.response.ClientErrorHandler;

public class MinecraftServicesTokenRequest
extends ResourceException
implements ClientErrorHandler<MinecraftToken> {
    public MinecraftServicesTokenRequest(XblXstsToken xblXstsToken) throws MalformedURLException {
        super("https://api.minecraftservices.com/launcher/login");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("platform", "PC_LAUNCHER");
        jsonObject.addProperty("xtoken", xblXstsToken.formatAuthorizationHeader());
        this.setBody(new JsonRequestBody(jsonObject));
    }

    @Override
    public MinecraftToken parseSuccessResponse(HttpResponse response, JsonObjectToken json) throws IOException {
        return new MinecraftToken(System.currentTimeMillis() + json.getLong("expires_in") * 1000L, json.getString("token_type"), json.getString("access_token"));
    }
}
