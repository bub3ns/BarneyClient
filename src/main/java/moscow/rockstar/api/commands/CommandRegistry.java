/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package moscow.rockstar.api.commands;

import java.net.MalformedURLException;
import java.net.URL;
import javax.annotation.Nullable;
import moscow.rockstar.api.commands.MiningCommandService;
import moscow.rockstar.network.http.HttpRequest;

public class CommandRegistry
extends HttpRequest {
    @Nullable
    private MiningCommandService commandAdapter;

    public CommandRegistry(String string, String string2) throws MalformedURLException {
        super(string, string2);
    }

    public CommandRegistry(String string, URL uRL) {
        super(string, uRL);
    }

    public boolean isCollectionRegistryReady() {
        return this.commandAdapter != null;
    }

    @Nullable
    public MiningCommandService createResponseAdapter() {
        return this.commandAdapter;
    }

    public CommandRegistry createCollectionProcessorRegistryFromCollectionProcessorAdapter(@Nullable MiningCommandService miningCommandService) {
        this.commandAdapter = miningCommandService;
        return this;
    }
}
