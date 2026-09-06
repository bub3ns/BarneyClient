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
import java.time.Instant;
import java.util.UUID;
import moscow.rockstar.auth.crypto.DeviceKeyPairCodec;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.auth.tokens.XblDeviceToken;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.JsonRequestBody;
import moscow.rockstar.network.http.response.XboxErrorHandler;

public class XblDeviceTokenRequest
extends DeviceKeyPairCodec
implements XboxErrorHandler<XblDeviceToken> {
    public XblDeviceTokenRequest(String string, UUID uUID, KeyPair keyPair) throws MalformedURLException {
        super("https://device.auth.xboxlive.com/device/authenticate");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("DeviceType", string);
        jsonObject.addProperty("Id", "{" + uUID + "}");
        jsonObject.addProperty("AuthMethod", "ProofOfPossession");
        jsonObject.add("ProofKey", (JsonElement)this.buildPublicKeyJson((ECPublicKey)keyPair.getPublic()));
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.add("Properties", (JsonElement)jsonObject);
        jsonObject2.addProperty("RelyingParty", "http://auth.xboxlive.com");
        jsonObject2.addProperty("TokenType", "JWT");
        this.setBody(new JsonRequestBody(jsonObject2));
        this.setHeader("x-xbl-contract-version", "1");
        this.signRequest((ECPrivateKey)keyPair.getPrivate());
    }

    @Override
    public XblDeviceToken parseSuccessResponse(HttpResponse response, JsonObjectToken json) {
        return new XblDeviceToken(Instant.parse(json.getString("NotAfter")).toEpochMilli(), json.getString("Token"), json.getObject("DisplayClaims").getObject("xdi").getString("did"));
    }
}
