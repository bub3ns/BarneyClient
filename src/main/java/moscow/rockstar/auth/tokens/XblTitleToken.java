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

public final class XblTitleToken
implements TimedState {
    private final long expiresAtMillis;
    private final String token;
    private final String titleId;

    public static XblTitleToken fromJson(JsonObject jsonObject) {
        return XblTitleToken.fromTokenResponse(new JsonObjectToken(jsonObject));
    }

    public static XblTitleToken fromTokenResponse(JsonObjectToken json) {
        return new XblTitleToken(json.getLong("expireTimeMs"), json.getString("token"), json.getString("titleId"));
    }

    public static JsonObject toJson(XblTitleToken xblTitleToken) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)1);
        jsonObject.addProperty("expireTimeMs", (Number)xblTitleToken.expiresAtMillis);
        jsonObject.addProperty("token", xblTitleToken.token);
        jsonObject.addProperty("titleId", xblTitleToken.titleId);
        return jsonObject;
    }

    @ApiStatus.Internal
    public static XblTitleToken fromXboxResponse(JsonObjectToken json) {
        return new XblTitleToken(Instant.parse(json.getString("NotAfter")).toEpochMilli(), json.getString("Token"), json.getObject("DisplayClaims").getObject("xti").getString("tid"));
    }

    @Generated
    public XblTitleToken(long l, String string, String string2) {
        this.expiresAtMillis = l;
        this.token = string;
        this.titleId = string2;
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
    public String getTitleId() {
        return this.titleId;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof XblTitleToken)) {
            return false;
        }
        XblTitleToken xblTitleToken = (XblTitleToken)object;
        if (this.getExpiresAtMillis() != xblTitleToken.getExpiresAtMillis()) {
            return false;
        }
        String string = this.getToken();
        String string2 = xblTitleToken.getToken();
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        String string3 = this.getTitleId();
        String string4 = xblTitleToken.getTitleId();
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
        String string2 = this.getTitleId();
        n2 = n2 * 59 + (string2 == null ? 43 : string2.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "XblTitleToken(expireTimeMs=" + this.getExpiresAtMillis() + ", token=" + this.getToken() + ", titleId=" + this.getTitleId() + ")";
    }
}
