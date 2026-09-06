/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package moscow.rockstar.auth.migration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.security.KeyPair;
import java.util.Map;
import java.util.UUID;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.crypto.DeviceKeyPairStore;
import moscow.rockstar.auth.tokens.MicrosoftTokenSet;
import moscow.rockstar.auth.tokens.PlayFabEntityToken;
import moscow.rockstar.auth.tokens.JsonElementToken;
import moscow.rockstar.auth.tokens.JsonObjectToken;

public class TokenMigration {
    public static JsonObject migrateLegacyToken(JsonObject jsonObject) {
        return TokenMigration.migrateLegacyClientData(jsonObject, new MicrosoftClientConfiguration("00000000402b5328", "service::user.auth.xboxlive.com::MBI_SSL"));
    }

    public static JsonObject migrateLegacyClientData(JsonObject jsonObject, MicrosoftClientConfiguration microsoftClientConfiguration) {
        String string = TokenMigration.formatTokenIdentifier(new JsonObjectToken(jsonObject));
        if (string == null) {
            throw new IllegalArgumentException("Failed to find refresh token in the provided save data");
        }
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("_saveVersion", (Number)1);
        jsonObject2.add("msaApplicationConfig", (JsonElement)MicrosoftClientConfiguration.toJson(microsoftClientConfiguration));
        jsonObject2.addProperty("deviceType", "Win32");
        jsonObject2.add("deviceKeyPair", (JsonElement)DeviceKeyPairStore.toJson(DeviceKeyPairStore.generate()));
        jsonObject2.addProperty("deviceId", UUID.randomUUID().toString());
        jsonObject2.add("msaToken", (JsonElement)MicrosoftTokenSet.toJson(new MicrosoftTokenSet(0L, "", string)));
        return jsonObject2;
    }

    public static JsonObject migrateTokenData(JsonObject jsonObject) {
        return TokenMigration.migrateClientData(jsonObject, new MicrosoftClientConfiguration("0000000048183522", "service::user.auth.xboxlive.com::MBI_SSL"));
    }

    public static JsonObject migrateClientData(JsonObject jsonObject, MicrosoftClientConfiguration microsoftClientConfiguration) {
        String string = TokenMigration.formatTokenIdentifier(new JsonObjectToken(jsonObject));
        if (string == null) {
            throw new IllegalArgumentException("Failed to find refresh token in the provided save data");
        }
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("_saveVersion", (Number)1);
        jsonObject2.add("msaApplicationConfig", (JsonElement)MicrosoftClientConfiguration.toJson(microsoftClientConfiguration));
        jsonObject2.addProperty("deviceType", "Android");
        jsonObject2.add("deviceKeyPair", (JsonElement)DeviceKeyPairStore.toJson(DeviceKeyPairStore.generate()));
        jsonObject2.addProperty("deviceId", UUID.randomUUID().toString());
        jsonObject2.add("sessionKeyPair", (JsonElement)DeviceKeyPairStore.toJson(DeviceKeyPairStore.generate()));
        jsonObject2.add("msaToken", (JsonElement)MicrosoftTokenSet.toJson(new MicrosoftTokenSet(0L, "", string)));
        return jsonObject2;
    }

    public static String formatTokenIdentifier(JsonObjectToken json) {
        if (json.containsString("refreshToken")) {
            return json.getString("refreshToken");
        }
        for (Map.Entry<String, JsonElementToken> entry : json) {
            if (!entry.getValue().isObject()) continue;
            String string = TokenMigration.formatTokenIdentifier(entry.getValue().asObject());
            if (string != null) return string;
        }
        return null;
    }
}
