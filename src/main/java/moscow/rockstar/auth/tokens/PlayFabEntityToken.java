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

public final class PlayFabEntityToken
implements TimedState {
    private final long expiresAtMillis;
    private final String token;
    private final String entityId;
    private final String entityType;

    public static PlayFabEntityToken fromJson(JsonObject jsonObject) {
        return PlayFabEntityToken.fromModernToken(new JsonObjectToken(jsonObject));
    }

    public static PlayFabEntityToken fromModernToken(JsonObjectToken json) {
        return new PlayFabEntityToken(json.getLong("expireTimeMs"), json.getString("token"), json.getString("entityId"), json.getString("entityType"));
    }

    public static JsonObject toJson(PlayFabEntityToken playFabEntityToken) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)1);
        jsonObject.addProperty("expireTimeMs", (Number)playFabEntityToken.expiresAtMillis);
        jsonObject.addProperty("token", playFabEntityToken.token);
        jsonObject.addProperty("entityId", playFabEntityToken.entityId);
        jsonObject.addProperty("entityType", playFabEntityToken.entityType);
        return jsonObject;
    }

    @ApiStatus.Internal
    public static PlayFabEntityToken fromLegacyToken(JsonObjectToken json) {
        JsonObjectToken entity = json.getObject("Entity");
        return new PlayFabEntityToken(Instant.parse(json.getString("TokenExpiration")).toEpochMilli(), json.getString("EntityToken"), entity.getString("Id"), entity.getString("Type"));
    }

    @Generated
    public PlayFabEntityToken(long l, String string, String string2, String string3) {
        this.expiresAtMillis = l;
        this.token = string;
        this.entityId = string2;
        this.entityType = string3;
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
    public String getEntityId() {
        return this.entityId;
    }

    @Generated
    public String getEntityType() {
        return this.entityType;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PlayFabEntityToken)) {
            return false;
        }
        PlayFabEntityToken playFabEntityToken = (PlayFabEntityToken)object;
        if (this.getExpiresAtMillis() != playFabEntityToken.getExpiresAtMillis()) {
            return false;
        }
        String string = this.getToken();
        String string2 = playFabEntityToken.getToken();
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        String string3 = this.getEntityId();
        String string4 = playFabEntityToken.getEntityId();
        if (string3 == null ? string4 != null : !string3.equals(string4)) {
            return false;
        }
        String string5 = this.getEntityType();
        String string6 = playFabEntityToken.getEntityType();
        return !(string5 == null ? string6 != null : !string5.equals(string6));
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        long l = this.getExpiresAtMillis();
        n2 = n2 * 59 + (int)(l >>> 32 ^ l);
        String string = this.getToken();
        n2 = n2 * 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.getEntityId();
        n2 = n2 * 59 + (string2 == null ? 43 : string2.hashCode());
        String string3 = this.getEntityType();
        n2 = n2 * 59 + (string3 == null ? 43 : string3.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "PlayFabEntityToken(expireTimeMs=" + this.getExpiresAtMillis() + ", token=" + this.getToken() + ", entityId=" + this.getEntityId() + ", entityType=" + this.getEntityType() + ")";
    }
}
