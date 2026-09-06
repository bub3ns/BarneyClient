/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  lombok.Generated
 */
package moscow.rockstar.auth.device;

import com.google.gson.JsonObject;
import lombok.Generated;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.util.scheduling.TimedState;

public final class DeviceCodeInfo
implements TimedState {
    private final long expiresAtMillis;
    private final long pollIntervalMillis;
    private final String deviceCode;
    private final String userCode;
    private final String verificationUri;

    public static DeviceCodeInfo fromJson(JsonObject jsonObject) {
        return DeviceCodeInfo.fromToken(new JsonObjectToken(jsonObject));
    }

    public static DeviceCodeInfo fromToken(JsonObjectToken json) {
        return new DeviceCodeInfo(json.getLong("expireTimeMs"), json.getLong("intervalMs"), json.getString("deviceCode"), json.getString("userCode"), json.getString("verificationUri"));
    }

    public static JsonObject toJson(DeviceCodeInfo deviceCodeInfo) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)1);
        jsonObject.addProperty("expireTimeMs", (Number)deviceCodeInfo.expiresAtMillis);
        jsonObject.addProperty("intervalMs", (Number)deviceCodeInfo.pollIntervalMillis);
        jsonObject.addProperty("deviceCode", deviceCodeInfo.deviceCode);
        jsonObject.addProperty("userCode", deviceCodeInfo.userCode);
        jsonObject.addProperty("verificationUri", deviceCodeInfo.verificationUri);
        return jsonObject;
    }

    public String getVerificationUrl() {
        return this.verificationUri + "?otc=" + this.userCode;
    }

    @Generated
    public DeviceCodeInfo(long l, long l2, String string, String string2, String string3) {
        this.expiresAtMillis = l;
        this.pollIntervalMillis = l2;
        this.deviceCode = string;
        this.userCode = string2;
        this.verificationUri = string3;
    }

    @Override
    @Generated
    public long getExpiresAtMillis() {
        return this.expiresAtMillis;
    }

    @Generated
    public long getPollIntervalMillis() {
        return this.pollIntervalMillis;
    }

    @Generated
    public String getDeviceCode() {
        return this.deviceCode;
    }

    @Generated
    public String getUserCode() {
        return this.userCode;
    }

    @Generated
    public String getVerificationUri() {
        return this.verificationUri;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DeviceCodeInfo)) {
            return false;
        }
        DeviceCodeInfo deviceCodeInfo = (DeviceCodeInfo)object;
        if (this.getExpiresAtMillis() != deviceCodeInfo.getExpiresAtMillis()) {
            return false;
        }
        if (this.getPollIntervalMillis() != deviceCodeInfo.getPollIntervalMillis()) {
            return false;
        }
        String string = this.getDeviceCode();
        String string2 = deviceCodeInfo.getDeviceCode();
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        String string3 = this.getUserCode();
        String string4 = deviceCodeInfo.getUserCode();
        if (string3 == null ? string4 != null : !string3.equals(string4)) {
            return false;
        }
        String string5 = this.getVerificationUri();
        String string6 = deviceCodeInfo.getVerificationUri();
        return !(string5 == null ? string6 != null : !string5.equals(string6));
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        long l = this.getExpiresAtMillis();
        n2 = n2 * 59 + (int)(l >>> 32 ^ l);
        long l2 = this.getPollIntervalMillis();
        n2 = n2 * 59 + (int)(l2 >>> 32 ^ l2);
        String string = this.getDeviceCode();
        n2 = n2 * 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.getUserCode();
        n2 = n2 * 59 + (string2 == null ? 43 : string2.hashCode());
        String string3 = this.getVerificationUri();
        n2 = n2 * 59 + (string3 == null ? 43 : string3.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "MsaDeviceCode(expireTimeMs=" + this.getExpiresAtMillis() + ", intervalMs=" + this.getPollIntervalMillis() + ", deviceCode=" + this.getDeviceCode() + ", userCode=" + this.getUserCode() + ", verificationUri=" + this.getVerificationUri() + ")";
    }
}
