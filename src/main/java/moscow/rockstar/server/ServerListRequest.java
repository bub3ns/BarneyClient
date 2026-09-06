/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.auth.tokens.MinecraftToken;
import moscow.rockstar.auth.tokens.JsonElementToken;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.network.HttpException;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.response.MicrosoftErrorHandler;

public class ServerListRequest
extends HttpException
implements MicrosoftErrorHandler<List<MinecraftToken>> {
    public ServerListRequest(String string) throws MalformedURLException {
        super("https://" + string + "/worlds");
    }

    @Override
    public List<MinecraftToken> parseSuccessResponse(HttpResponse response, JsonObjectToken json) throws IOException {
        ArrayList<MinecraftToken> arrayList = new ArrayList<MinecraftToken>();
        for (JsonElementToken element : json.getArray("servers")) {
            arrayList.add(MinecraftToken.fromToken(element.asObject()));
        }
        return arrayList;
    }
}
