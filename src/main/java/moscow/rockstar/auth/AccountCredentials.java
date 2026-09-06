/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  lombok.Generated
 */
package moscow.rockstar.auth;

import com.google.gson.JsonObject;
import lombok.Generated;
import moscow.rockstar.auth.tokens.JsonObjectToken;

public final class AccountCredentials {
    private final String email;
    private final String password;

    public static AccountCredentials fromJson(JsonObject jsonObject) {
        return AccountCredentials.fromToken(new JsonObjectToken(jsonObject));
    }

    public static AccountCredentials fromToken(JsonObjectToken json) {
        return new AccountCredentials(json.getString("email"), json.getString("password"));
    }

    public static JsonObject toJson(AccountCredentials accountCredentials) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)1);
        jsonObject.addProperty("email", accountCredentials.email);
        jsonObject.addProperty("password", accountCredentials.password);
        return jsonObject;
    }

    @Generated
    public AccountCredentials(String string, String string2) {
        this.email = string;
        this.password = string2;
    }

    @Generated
    public String getEmail() {
        return this.email;
    }

    @Generated
    public String getPassword() {
        return this.password;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof AccountCredentials)) {
            return false;
        }
        AccountCredentials accountCredentials = (AccountCredentials)object;
        String string = this.getEmail();
        String string2 = accountCredentials.getEmail();
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        String string3 = this.getPassword();
        String string4 = accountCredentials.getPassword();
        return !(string3 == null ? string4 != null : !string3.equals(string4));
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        String string = this.getEmail();
        n2 = n2 * 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.getPassword();
        n2 = n2 * 59 + (string2 == null ? 43 : string2.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "MsaCredentials(email=" + this.getEmail() + ", password=" + this.getPassword() + ")";
    }
}
