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
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.crypto.DeviceKeyPairCodec;
import moscow.rockstar.auth.tokens.MicrosoftTokenSet;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.auth.tokens.XblDeviceToken;
import moscow.rockstar.auth.tokens.XblSisuTokens;
import moscow.rockstar.auth.tokens.XblTitleToken;
import moscow.rockstar.auth.tokens.XblUserToken;
import moscow.rockstar.auth.tokens.XblXstsToken;
import moscow.rockstar.network.http.JsonRequestBody;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.response.XboxErrorHandler;

public class XblSisuTokenRequest
extends DeviceKeyPairCodec
implements XboxErrorHandler<XblSisuTokens> {
    public XblSisuTokenRequest(MicrosoftClientConfiguration microsoftClientConfiguration, MicrosoftTokenSet microsoftTokenSet, XblDeviceToken xblDeviceToken, KeyPair keyPair, String string) throws MalformedURLException {
        super("https://sisu.xboxlive.com/authorize");
        if (!microsoftClientConfiguration.isValid()) {
            throw new IllegalArgumentException("Client id must be a title client id for XBL SISU authentication");
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Sandbox", "RETAIL");
        jsonObject.addProperty("UseModernGamertag", Boolean.valueOf(true));
        jsonObject.addProperty("AppId", microsoftClientConfiguration.getClientId());
        jsonObject.addProperty("AccessToken", "t=" + microsoftTokenSet.getAccessToken());
        jsonObject.addProperty("DeviceToken", xblDeviceToken.getToken());
        jsonObject.add("ProofKey", (JsonElement)this.buildPublicKeyJson((ECPublicKey)keyPair.getPublic()));
        jsonObject.addProperty("RelyingParty", string);
        this.setBody(new JsonRequestBody(jsonObject));
        this.signRequest((ECPrivateKey)keyPair.getPrivate());
    }

    @Override
    public XblSisuTokens parseSuccessResponse(HttpResponse response, JsonObjectToken json) {
        return new XblSisuTokens(XblUserToken.fromXboxResponse(json.getObject("UserToken")), XblTitleToken.fromXboxResponse(json.getObject("TitleToken")), XblXstsToken.fromXboxResponse(json.getObject("AuthorizationToken")));
    }
}
