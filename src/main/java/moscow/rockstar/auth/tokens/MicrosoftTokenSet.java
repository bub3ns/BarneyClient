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

public final class MicrosoftTokenSet
implements TimedState {
    private final long expiresAtMillis;
    private final String accessToken;
    private final String refreshToken;

    public static MicrosoftTokenSet fromJson(JsonObject jsonObject) {
        return MicrosoftTokenSet.fromToken(new JsonObjectToken(jsonObject));
    }

    public static MicrosoftTokenSet fromToken(JsonObjectToken json) {
        return new MicrosoftTokenSet(json.getLong("expireTimeMs"), json.getString("accessToken"), json.getString("refreshToken", null));
    }

    public static JsonObject toJson(MicrosoftTokenSet microsoftTokenSet) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)1);
        jsonObject.addProperty("expireTimeMs", (Number)microsoftTokenSet.expiresAtMillis);
        jsonObject.addProperty("accessToken", microsoftTokenSet.accessToken);
        jsonObject.addProperty("refreshToken", microsoftTokenSet.refreshToken);
        return jsonObject;
    }

    @Generated
    public MicrosoftTokenSet(long l, String string, String string2) {
        this.expiresAtMillis = l;
        this.accessToken = string;
        this.refreshToken = string2;
    }

    @Override
    @Generated
    public long getExpiresAtMillis() {
        return this.expiresAtMillis;
    }

    @Generated
    public String getAccessToken() {
        return this.accessToken;
    }

    @Generated
    public String getRefreshToken() {
        return this.refreshToken;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof MicrosoftTokenSet)) {
            return false;
        }
        MicrosoftTokenSet microsoftTokenSet = (MicrosoftTokenSet)object;
        if (this.getExpiresAtMillis() != microsoftTokenSet.getExpiresAtMillis()) {
            return false;
        }
        String string = this.getAccessToken();
        String string2 = microsoftTokenSet.getAccessToken();
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        String string3 = this.getRefreshToken();
        String string4 = microsoftTokenSet.getRefreshToken();
        return !(string3 == null ? string4 != null : !string3.equals(string4));
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        long l = this.getExpiresAtMillis();
        n2 = n2 * 59 + (int)(l >>> 32 ^ l);
        String string = this.getAccessToken();
        n2 = n2 * 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.getRefreshToken();
        n2 = n2 * 59 + (string2 == null ? 43 : string2.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "MsaToken(expireTimeMs=" + this.getExpiresAtMillis() + ", accessToken=" + this.getAccessToken() + ", refreshToken=" + this.getRefreshToken() + ")";
    }
}
