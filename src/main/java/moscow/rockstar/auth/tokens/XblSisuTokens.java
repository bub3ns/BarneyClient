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
import moscow.rockstar.auth.tokens.PlayFabToken;
import moscow.rockstar.auth.tokens.XblTitleToken;
import moscow.rockstar.auth.tokens.XblUserToken;
import moscow.rockstar.auth.tokens.XblXstsToken;

public final class XblSisuTokens {
    private final XblUserToken userToken;
    private final XblTitleToken titleToken;
    private final XblXstsToken xstsToken;

    public static XblSisuTokens fromJson(JsonObject jsonObject) {
        return XblSisuTokens.fromTokenResponse(new JsonObjectToken(jsonObject));
    }

    public static XblSisuTokens fromTokenResponse(JsonObjectToken json) {
        return new XblSisuTokens(XblUserToken.fromTokenResponse(json.getObject("userToken")), XblTitleToken.fromTokenResponse(json.getObject("titleToken")), XblXstsToken.fromTokenResponse(json.getObject("xstsToken")));
    }

    public static JsonObject toJson(XblSisuTokens xblSisuTokens) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_saveVersion", (Number)1);
        jsonObject.add("userToken", (JsonElement)XblUserToken.toJson(xblSisuTokens.userToken));
        jsonObject.add("titleToken", (JsonElement)XblTitleToken.toJson(xblSisuTokens.titleToken));
        jsonObject.add("xstsToken", (JsonElement)XblXstsToken.toJson(xblSisuTokens.xstsToken));
        return jsonObject;
    }

    @Generated
    public XblSisuTokens(XblUserToken xblUserToken, XblTitleToken xblTitleToken, XblXstsToken xblXstsToken) {
        this.userToken = xblUserToken;
        this.titleToken = xblTitleToken;
        this.xstsToken = xblXstsToken;
    }

    @Generated
    public XblUserToken getUserToken() {
        return this.userToken;
    }

    @Generated
    public XblTitleToken getTitleToken() {
        return this.titleToken;
    }

    @Generated
    public XblXstsToken getXstsToken() {
        return this.xstsToken;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof XblSisuTokens)) {
            return false;
        }
        XblSisuTokens xblSisuTokens = (XblSisuTokens)object;
        XblUserToken xblUserToken = this.getUserToken();
        XblUserToken xblUserToken2 = xblSisuTokens.getUserToken();
        if (xblUserToken == null ? xblUserToken2 != null : !((Object)xblUserToken).equals(xblUserToken2)) {
            return false;
        }
        XblTitleToken xblTitleToken = this.getTitleToken();
        XblTitleToken xblTitleToken2 = xblSisuTokens.getTitleToken();
        if (xblTitleToken == null ? xblTitleToken2 != null : !((Object)xblTitleToken).equals(xblTitleToken2)) {
            return false;
        }
        XblXstsToken xblXstsToken = this.getXstsToken();
        XblXstsToken xblXstsToken2 = xblSisuTokens.getXstsToken();
        return !(xblXstsToken == null ? xblXstsToken2 != null : !((Object)xblXstsToken).equals(xblXstsToken2));
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        XblUserToken xblUserToken = this.getUserToken();
        n2 = n2 * 59 + (xblUserToken == null ? 43 : ((Object)xblUserToken).hashCode());
        XblTitleToken xblTitleToken = this.getTitleToken();
        n2 = n2 * 59 + (xblTitleToken == null ? 43 : ((Object)xblTitleToken).hashCode());
        XblXstsToken xblXstsToken = this.getXstsToken();
        n2 = n2 * 59 + (xblXstsToken == null ? 43 : ((Object)xblXstsToken).hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "XblSisuTokens(userToken=" + this.getUserToken() + ", titleToken=" + this.getTitleToken() + ", xstsToken=" + this.getXstsToken() + ")";
    }
}
