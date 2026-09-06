/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.events.render;

import moscow.rockstar.events.EventListener;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.hud.HudElementRegistry;
import pyrock.events.render.HudRenderEvent;

public class HudRenderListener
implements EventListener<HudRenderEvent> {
    @Override
    public void onEvent(HudRenderEvent hudRenderEvent) {
        HudElementRegistry registry = RockstarClient.create().getHudElementRegistry();
        if (registry != null) {
            registry.render(hudRenderEvent.getContext(), hudRenderEvent.getTickDelta());
        }
    }

}

