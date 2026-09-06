/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  lombok.Generated
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package moscow.rockstar.auth.tokens;

import com.google.gson.JsonObject;
import java.time.Instant;
import lombok.Generated;
import moscow.rockstar.auth.tokens.PlayFabToken;
import moscow.rockstar.util.scheduling.TimedState;
import org.jetbrains.annotations.ApiStatus;

public final class XblUserToken
implements TimedState {
    private final long expiresAtMillis;
    private final String token;
    private final String userHash;

    public static XblUserToken fromJson(JsonObject jsonObject) {
        return XblUserToken.fromTokenResponse(new JsonObjectToken(jsonObject));
    }

    public static XblUserToken fromTokenResponse(JsonObjectToken json) {
        return new XblUserToken(json.getLong("expireTimeMs"), json.getString("token"), json.getString("userHash"));
    }

    public static JsonObject toJson(XblUserToken xblUserToken) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)1);
        jsonObject.addProperty("expireTimeMs", (Number)xblUserToken.expiresAtMillis);
        jsonObject.addProperty("token", xblUserToken.token);
        jsonObject.addProperty("userHash", xblUserToken.userHash);
        return jsonObject;
    }

    @ApiStatus.Internal
    public static XblUserToken fromXboxResponse(JsonObjectToken json) {
        return new XblUserToken(Instant.parse(json.getString("NotAfter")).toEpochMilli(), json.getString("Token"), json.getObject("DisplayClaims").getArray("xui").getObjectAt(0).getString("uhs"));
    }

    @Generated
    public XblUserToken(long l, String string, String string2) {
        this.expiresAtMillis = l;
        this.token = string;
        this.userHash = string2;
    }

    @Override
    @Generated
    public long getExpiresAtMillis() {
        return this.expiresAtMillis;
    }

    @Generated
    public String formatAuthorizationHeader() {
        return this.token;
    }

    @Generated
    public String getToken() {
        return this.userHash;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof XblUserToken)) {
            return false;
        }
        XblUserToken xblUserToken = (XblUserToken)object;
        if (this.getExpiresAtMillis() != xblUserToken.getExpiresAtMillis()) {
            return false;
        }
        String string = this.formatAuthorizationHeader();
        String string2 = xblUserToken.formatAuthorizationHeader();
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        String string3 = this.getToken();
        String string4 = xblUserToken.getToken();
        return !(string3 == null ? string4 != null : !string3.equals(string4));
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        long l = this.getExpiresAtMillis();
        n2 = n2 * 59 + (int)(l >>> 32 ^ l);
        String string = this.formatAuthorizationHeader();
        n2 = n2 * 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.getToken();
        n2 = n2 * 59 + (string2 == null ? 43 : string2.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "XblUserToken(expireTimeMs=" + this.getExpiresAtMillis() + ", token=" + this.formatAuthorizationHeader() + ", userHash=" + this.getToken() + ")";
    }
}
