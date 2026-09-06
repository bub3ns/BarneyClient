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

public final class XblXstsToken
implements TimedState {
    private final long expiresAtMillis;
    private final String token;
    private final String userHash;

    public static XblXstsToken fromJson(JsonObject jsonObject) {
        return XblXstsToken.fromTokenResponse(new JsonObjectToken(jsonObject));
    }

    public static XblXstsToken fromTokenResponse(JsonObjectToken json) {
        return new XblXstsToken(json.getLong("expireTimeMs"), json.getString("token"), json.getString("userHash"));
    }

    public static JsonObject toJson(XblXstsToken xblXstsToken) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)1);
        jsonObject.addProperty("expireTimeMs", (Number)xblXstsToken.expiresAtMillis);
        jsonObject.addProperty("token", xblXstsToken.token);
        jsonObject.addProperty("userHash", xblXstsToken.userHash);
        return jsonObject;
    }

    @ApiStatus.Internal
    public static XblXstsToken fromXboxResponse(JsonObjectToken json) {
        return new XblXstsToken(Instant.parse(json.getString("NotAfter")).toEpochMilli(), json.getString("Token"), json.getObject("DisplayClaims").getArray("xui").getObjectAt(0).getString("uhs"));
    }

    public String formatAuthorizationHeader() {
        return "XBL3.0 x=" + this.userHash + ';' + this.token;
    }

    @Generated
    public XblXstsToken(long l, String string, String string2) {
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
    public String getToken() {
        return this.token;
    }

    @Generated
    public String getUserHash() {
        return this.userHash;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof XblXstsToken)) {
            return false;
        }
        XblXstsToken xblXstsToken = (XblXstsToken)object;
        if (this.getExpiresAtMillis() != xblXstsToken.getExpiresAtMillis()) {
            return false;
        }
        String string = this.getToken();
        String string2 = xblXstsToken.getToken();
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        String string3 = this.getUserHash();
        String string4 = xblXstsToken.getUserHash();
        return !(string3 == null ? string4 != null : !string3.equals(string4));
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        long l = this.getExpiresAtMillis();
        n2 = n2 * 59 + (int)(l >>> 32 ^ l);
        String string = this.getToken();
        n2 = n2 * 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.getUserHash();
        n2 = n2 * 59 + (string2 == null ? 43 : string2.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "XblXstsToken(expireTimeMs=" + this.getExpiresAtMillis() + ", token=" + this.getToken() + ", userHash=" + this.getUserHash() + ")";
    }
}
