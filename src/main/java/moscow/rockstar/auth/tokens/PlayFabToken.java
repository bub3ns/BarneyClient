/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  lombok.Generated
 */
package moscow.rockstar.auth.tokens;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Generated;
import moscow.rockstar.auth.tokens.PlayFabEntityToken;
import moscow.rockstar.util.scheduling.TimedState;

public final class PlayFabToken
implements TimedState {
    private final PlayFabEntityToken entityToken;
    private final String playFabId;
    private final String sessionTicket;

    public static PlayFabToken fromJson(JsonObject jsonObject) {
        return PlayFabToken.fromSavedData(new JsonObjectToken(jsonObject));
    }

    public static PlayFabToken fromSavedData(JsonObjectToken json) {
        if (json.getInt("_saveVersion", 0) == 1) {
            PlayFabEntityToken playFabEntityToken = new PlayFabEntityToken(json.getLong("expireTimeMs"), json.getString("entityToken"), json.getString("entityId"), "title_player_account");
            return new PlayFabToken(playFabEntityToken, json.getString("playFabId"), json.getString("sessionTicket"));
        }
        return new PlayFabToken(PlayFabEntityToken.fromModernToken(json.get("entityToken").asObject()), json.getString("playFabId"), json.getString("sessionTicket"));
    }

    public static JsonObject toJson(PlayFabToken playFabToken) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)2);
        jsonObject.add("entityToken", (JsonElement)PlayFabEntityToken.toJson(playFabToken.entityToken));
        jsonObject.addProperty("playFabId", playFabToken.playFabId);
        jsonObject.addProperty("sessionTicket", playFabToken.sessionTicket);
        return jsonObject;
    }

    @Override
    public long getExpiresAtMillis() {
        return this.entityToken.getExpiresAtMillis();
    }

    @Deprecated
    public String getEntityId() {
        return this.entityToken.getEntityId();
    }

    @Generated
    public PlayFabToken(PlayFabEntityToken playFabEntityToken, String string, String string2) {
        this.entityToken = playFabEntityToken;
        this.playFabId = string;
        this.sessionTicket = string2;
    }

    @Generated
    public PlayFabEntityToken getEntityToken() {
        return this.entityToken;
    }

    @Generated
    public String getPlayFabId() {
        return this.playFabId;
    }

    @Generated
    public String getSessionTicket() {
        return this.sessionTicket;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PlayFabToken)) {
            return false;
        }
        PlayFabToken playFabToken = (PlayFabToken)object;
        PlayFabEntityToken playFabEntityToken = this.getEntityToken();
        PlayFabEntityToken playFabEntityToken2 = playFabToken.getEntityToken();
        if (playFabEntityToken == null ? playFabEntityToken2 != null : !((Object)playFabEntityToken).equals(playFabEntityToken2)) {
            return false;
        }
        String string = this.getPlayFabId();
        String string2 = playFabToken.getPlayFabId();
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        String string3 = this.getSessionTicket();
        String string4 = playFabToken.getSessionTicket();
        return !(string3 == null ? string4 != null : !string3.equals(string4));
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        PlayFabEntityToken playFabEntityToken = this.getEntityToken();
        n2 = n2 * 59 + (playFabEntityToken == null ? 43 : ((Object)playFabEntityToken).hashCode());
        String string = this.getPlayFabId();
        n2 = n2 * 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.getSessionTicket();
        n2 = n2 * 59 + (string2 == null ? 43 : string2.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "PlayFabToken(entityToken=" + this.getEntityToken() + ", playFabId=" + this.getPlayFabId() + ", sessionTicket=" + this.getSessionTicket() + ")";
    }
}
