/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  lombok.Generated
 */
package moscow.rockstar.auth;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.auth.MicrosoftOAuthProfile;
import moscow.rockstar.auth.tokens.JsonObjectToken;

public final class MicrosoftClientConfiguration {
    private final String clientId;
    private final String scope;
    private final String clientSecret;
    private final String redirectUri;
    private final MicrosoftOAuthProfile environment;

    public static MicrosoftClientConfiguration fromJson(JsonObject jsonObject) {
        return MicrosoftClientConfiguration.fromToken(new JsonObjectToken(jsonObject));
    }

    public static MicrosoftClientConfiguration fromToken(JsonObjectToken json) {
        return new MicrosoftClientConfiguration(json.getString("clientId"), json.getString("scope"), json.getString("clientSecret", null), json.getString("redirectUri", null), MicrosoftOAuthProfile.valueOf(json.getString("environment", MicrosoftOAuthProfile.LIVE.name())));
    }

    public static JsonObject toJson(MicrosoftClientConfiguration microsoftClientConfiguration) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)1);
        jsonObject.addProperty("clientId", microsoftClientConfiguration.clientId);
        jsonObject.addProperty("scope", microsoftClientConfiguration.scope);
        jsonObject.addProperty("clientSecret", microsoftClientConfiguration.clientSecret);
        jsonObject.addProperty("redirectUri", microsoftClientConfiguration.redirectUri);
        jsonObject.addProperty("environment", microsoftClientConfiguration.environment.name());
        return jsonObject;
    }

    public MicrosoftClientConfiguration(String string, String string2) {
        this(string, string2, null, null, MicrosoftOAuthProfile.LIVE);
    }

    public boolean isValid() {
        return this.clientId != null && !this.clientId.isBlank() && this.scope != null && !this.scope.isBlank();
    }

    public Map<String, String> toMap() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("client_id", this.clientId);
        hashMap.put("scope", this.scope);
        if (this.redirectUri != null) {
            hashMap.put("redirect_uri", this.redirectUri);
        }
        hashMap.put("response_type", "code");
        hashMap.put("response_mode", "query");
        return hashMap;
    }

    @Generated
    public String getClientId() {
        return this.clientId;
    }

    @Generated
    public String getScope() {
        return this.scope;
    }

    @Generated
    public String getClientSecret() {
        return this.clientSecret;
    }

    @Generated
    public String getRedirectUri() {
        return this.redirectUri;
    }

    @Generated
    public MicrosoftOAuthProfile getEnvironment() {
        return this.environment;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof MicrosoftClientConfiguration)) {
            return false;
        }
        MicrosoftClientConfiguration microsoftClientConfiguration = (MicrosoftClientConfiguration)object;
        String string = this.getClientId();
        String string2 = microsoftClientConfiguration.getClientId();
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        String string3 = this.getScope();
        String string4 = microsoftClientConfiguration.getScope();
        if (string3 == null ? string4 != null : !string3.equals(string4)) {
            return false;
        }
        String string5 = this.getClientSecret();
        String string6 = microsoftClientConfiguration.getClientSecret();
        if (string5 == null ? string6 != null : !string5.equals(string6)) {
            return false;
        }
        String string7 = this.getRedirectUri();
        String string8 = microsoftClientConfiguration.getRedirectUri();
        if (string7 == null ? string8 != null : !string7.equals(string8)) {
            return false;
        }
        MicrosoftOAuthProfile microsoftOAuthProfile = this.getEnvironment();
        MicrosoftOAuthProfile microsoftOAuthProfile2 = microsoftClientConfiguration.getEnvironment();
        return !(microsoftOAuthProfile == null ? microsoftOAuthProfile2 != null : !((Object)((Object)microsoftOAuthProfile)).equals((Object)microsoftOAuthProfile2));
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        String string = this.getClientId();
        n2 = n2 * 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.getScope();
        n2 = n2 * 59 + (string2 == null ? 43 : string2.hashCode());
        String string3 = this.getClientSecret();
        n2 = n2 * 59 + (string3 == null ? 43 : string3.hashCode());
        String string4 = this.getRedirectUri();
        n2 = n2 * 59 + (string4 == null ? 43 : string4.hashCode());
        MicrosoftOAuthProfile microsoftOAuthProfile = this.getEnvironment();
        n2 = n2 * 59 + (microsoftOAuthProfile == null ? 43 : ((Object)((Object)microsoftOAuthProfile)).hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "MsaApplicationConfig(clientId=" + this.getClientId() + ", scope=" + this.getScope() + ", clientSecret=" + this.getClientSecret() + ", redirectUri=" + this.getRedirectUri() + ", environment=" + (Object)((Object)this.getEnvironment()) + ")";
    }

    @Generated
    public MicrosoftClientConfiguration withClientId(String string) {
        return this.clientId == string ? this : new MicrosoftClientConfiguration(string, this.scope, this.clientSecret, this.redirectUri, this.environment);
    }

    @Generated
    public MicrosoftClientConfiguration withScope(String string) {
        return this.scope == string ? this : new MicrosoftClientConfiguration(this.clientId, string, this.clientSecret, this.redirectUri, this.environment);
    }

    @Generated
    public MicrosoftClientConfiguration withClientSecret(String string) {
        return this.clientSecret == string ? this : new MicrosoftClientConfiguration(this.clientId, this.scope, string, this.redirectUri, this.environment);
    }

    @Generated
    public MicrosoftClientConfiguration withRedirectUri(String string) {
        return this.redirectUri == string ? this : new MicrosoftClientConfiguration(this.clientId, this.scope, this.clientSecret, string, this.environment);
    }

    @Generated
    public MicrosoftClientConfiguration withEnvironment(MicrosoftOAuthProfile microsoftOAuthProfile) {
        return this.environment == microsoftOAuthProfile ? this : new MicrosoftClientConfiguration(this.clientId, this.scope, this.clientSecret, this.redirectUri, microsoftOAuthProfile);
    }

    @Generated
    public MicrosoftClientConfiguration(String string, String string2, String string3, String string4, MicrosoftOAuthProfile microsoftOAuthProfile) {
        this.clientId = string;
        this.scope = string2;
        this.clientSecret = string3;
        this.redirectUri = string4;
        this.environment = microsoftOAuthProfile;
    }
}
