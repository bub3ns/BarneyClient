/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  lombok.Generated
 */
package moscow.rockstar.auth.tokens;

import com.google.gson.JsonObject;
import java.util.UUID;
import lombok.Generated;
import moscow.rockstar.auth.tokens.PlayFabToken;
import moscow.rockstar.util.scheduling.TimedState;

public final class MinecraftProfile
implements TimedState {
    private final UUID identifier;
    private final String username;

    public static MinecraftProfile fromJson(JsonObject jsonObject) {
        return MinecraftProfile.fromToken(new JsonObjectToken(jsonObject));
    }

    public static MinecraftProfile fromToken(JsonObjectToken json) {
        return new MinecraftProfile(UUID.fromString(json.getString("id")), json.getString("name"));
    }

    public static JsonObject toJson(MinecraftProfile minecraftProfile) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)1);
        jsonObject.addProperty("id", minecraftProfile.identifier.toString());
        jsonObject.addProperty("name", minecraftProfile.username);
        return jsonObject;
    }

    @Override
    public long getExpiresAtMillis() {
        return Long.MAX_VALUE;
    }

    @Generated
    public MinecraftProfile(UUID uUID, String string) {
        this.identifier = uUID;
        this.username = string;
    }

    @Generated
    public UUID getIdentifier() {
        return this.identifier;
    }

    @Generated
    public String getUsername() {
        return this.username;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof MinecraftProfile)) {
            return false;
        }
        MinecraftProfile minecraftProfile = (MinecraftProfile)object;
        UUID uUID = this.getIdentifier();
        UUID uUID2 = minecraftProfile.getIdentifier();
        if (uUID == null ? uUID2 != null : !((Object)uUID).equals(uUID2)) {
            return false;
        }
        String string = this.getUsername();
        String string2 = minecraftProfile.getUsername();
        return !(string == null ? string2 != null : !string.equals(string2));
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        UUID uUID = this.getIdentifier();
        n2 = n2 * 59 + (uUID == null ? 43 : ((Object)uUID).hashCode());
        String string = this.getUsername();
        n2 = n2 * 59 + (string == null ? 43 : string.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "MinecraftProfile(id=" + this.getIdentifier() + ", name=" + this.getUsername() + ")";
    }
}
