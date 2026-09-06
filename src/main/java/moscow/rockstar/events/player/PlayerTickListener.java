/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.events.player;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.network.bot.BotSessionManager;
import pyrock.events.player.ClientPlayerTickEvent;

public class PlayerTickListener
implements EventListener<ClientPlayerTickEvent> {
    @Override
    public void onEvent(ClientPlayerTickEvent clientPlayerTickEvent) {
        for (ModuleContract moduleContract : RockstarClient.create().getModuleRegistry().getModules()) {
            if (!moduleContract.isEnabled()) continue;
            moduleContract.onTick();
        }
        BotSessionManager.getInstance().tick();
    }

}

