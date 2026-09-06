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

public final class MinecraftToken
implements TimedState {
    private final long expiresAtMillis;
    private final String tokenType;
    private final String token;

    public static MinecraftToken fromJson(JsonObject jsonObject) {
        return MinecraftToken.fromToken(new JsonObjectToken(jsonObject));
    }

    public static MinecraftToken fromToken(JsonObjectToken json) {
        return new MinecraftToken(json.getLong("expireTimeMs"), json.getString("type"), json.getString("token"));
    }

    public static JsonObject toJson(MinecraftToken minecraftToken) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)1);
        jsonObject.addProperty("expireTimeMs", (Number)minecraftToken.expiresAtMillis);
        jsonObject.addProperty("type", minecraftToken.tokenType);
        jsonObject.addProperty("token", minecraftToken.token);
        return jsonObject;
    }

    public String getAuthorizationHeader() {
        return this.tokenType + ' ' + this.token;
    }

    @Generated
    public MinecraftToken(long l, String string, String string2) {
        this.expiresAtMillis = l;
        this.tokenType = string;
        this.token = string2;
    }

    @Override
    @Generated
    public long getExpiresAtMillis() {
        return this.expiresAtMillis;
    }

    @Generated
    public String getTokenType() {
        return this.tokenType;
    }

    @Generated
    public String getToken() {
        return this.token;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof MinecraftToken)) {
            return false;
        }
        MinecraftToken minecraftToken = (MinecraftToken)object;
        if (this.getExpiresAtMillis() != minecraftToken.getExpiresAtMillis()) {
            return false;
        }
        String string = this.getTokenType();
        String string2 = minecraftToken.getTokenType();
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        String string3 = this.getToken();
        String string4 = minecraftToken.getToken();
        return !(string3 == null ? string4 != null : !string3.equals(string4));
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        long l = this.getExpiresAtMillis();
        n2 = n2 * 59 + (int)(l >>> 32 ^ l);
        String string = this.getTokenType();
        n2 = n2 * 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.getToken();
        n2 = n2 * 59 + (string2 == null ? 43 : string2.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "MinecraftToken(expireTimeMs=" + this.getExpiresAtMillis() + ", type=" + this.getTokenType() + ", token=" + this.getToken() + ")";
    }
}
