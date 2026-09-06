/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.auth;

import lombok.Generated;

public enum MicrosoftOAuthProfile {
    LIVE("https://login.live.com/", "oauth20_connect.srf", "oauth20_authorize.srf", "oauth20_token.srf", "oauth20_desktop.srf"),
    MICROSOFT_COMMON("https://login.microsoftonline.com/common/oauth2/", "v2.0/devicecode", "v2.0/authorize", "v2.0/token", "nativeclient"),
    MICROSOFT_CONSUMERS("https://login.microsoftonline.com/consumers/oauth2/", "v2.0/devicecode", "v2.0/authorize", "v2.0/token", "nativeclient");

    private final String baseUrl;
    private final String connectPath;
    private final String authorizePath;
    private final String tokenPath;
    private final String desktopPath;

    public String getConnectEndpoint() {
        return this.baseUrl + this.connectPath;
    }

    public String getAuthorizeEndpoint() {
        return this.baseUrl + this.authorizePath;
    }

    public String getTokenEndpoint() {
        return this.baseUrl + this.tokenPath;
    }

    public String getDesktopEndpoint() {
        return this.baseUrl + this.desktopPath;
    }

    @Generated
    public String getBaseUrl() {
        return this.baseUrl;
    }

    @Generated
    public String getConnectPath() {
        return this.connectPath;
    }

    @Generated
    public String getAuthorizePath() {
        return this.authorizePath;
    }

    @Generated
    public String getTokenPath() {
        return this.tokenPath;
    }

    @Generated
    public String getDesktopPath() {
        return this.desktopPath;
    }

    @Generated
    private MicrosoftOAuthProfile(String string2, String string3, String string4, String string5, String string6) {
        this.baseUrl = string2;
        this.connectPath = string3;
        this.authorizePath = string4;
        this.tokenPath = string5;
        this.desktopPath = string6;
    }
}

