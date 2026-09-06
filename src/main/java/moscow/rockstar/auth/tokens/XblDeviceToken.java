/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  lombok.Generated
 */
package moscow.rockstar.auth.tokens;

import com.google.gson.JsonObject;
import lombok.Generated;
import moscow.rockstar.auth.tokens.PlayFabToken;
import moscow.rockstar.util.scheduling.TimedState;

public final class XblDeviceToken
implements TimedState {
    private final long expiresAtMillis;
    private final String token;
    private final String deviceId;

    public static XblDeviceToken fromJson(JsonObject jsonObject) {
        return XblDeviceToken.fromTokenResponse(new JsonObjectToken(jsonObject));
    }

    public static XblDeviceToken fromTokenResponse(JsonObjectToken json) {
        return new XblDeviceToken(json.getLong("expireTimeMs"), json.getString("token"), json.getString("deviceId"));
    }

    public static JsonObject toJson(XblDeviceToken xblDeviceToken) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)1);
        jsonObject.addProperty("expireTimeMs", (Number)xblDeviceToken.expiresAtMillis);
        jsonObject.addProperty("token", xblDeviceToken.token);
        jsonObject.addProperty("deviceId", xblDeviceToken.deviceId);
        return jsonObject;
    }

    @Generated
    public XblDeviceToken(long l, String string, String string2) {
        this.expiresAtMillis = l;
        this.token = string;
        this.deviceId = string2;
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
    public String getDeviceId() {
        return this.deviceId;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof XblDeviceToken)) {
            return false;
        }
        XblDeviceToken xblDeviceToken = (XblDeviceToken)object;
        if (this.getExpiresAtMillis() != xblDeviceToken.getExpiresAtMillis()) {
            return false;
        }
        String string = this.getToken();
        String string2 = xblDeviceToken.getToken();
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        String string3 = this.getDeviceId();
        String string4 = xblDeviceToken.getDeviceId();
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
        String string2 = this.getDeviceId();
        n2 = n2 * 59 + (string2 == null ? 43 : string2.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "XblDeviceToken(expireTimeMs=" + this.getExpiresAtMillis() + ", token=" + this.getToken() + ", deviceId=" + this.getDeviceId() + ")";
    }
}
