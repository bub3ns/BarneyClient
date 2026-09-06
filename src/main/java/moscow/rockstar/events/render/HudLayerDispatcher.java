package moscow.rockstar.events.render;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.core.RockstarDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import pyrock.events.render.HudLayerRenderEvent;
import pyrock.events.render.PostHudLayerRenderEvent;

/** 1:1 with rockstar/ilIlil/IiIiIIiIi. */
public final class HudLayerDispatcher {
    private HudLayerDispatcher() {
    }

    public static void pre(RockstarDrawContext drawContext, int elementCount) {
        WidgetBatchRenderer.flushCurrentBatch();
        RockstarClient.create().getEventBus().post(new HudLayerRenderEvent(
                drawContext, drawContext.tickDelta(), elementCount, HudLayerDispatcher.isEditing()));
        WidgetBatchRenderer.flushCurrentBatch();
    }

    public static void post(RockstarDrawContext drawContext, int elementCount) {
        WidgetBatchRenderer.flushCurrentBatch();
        RockstarClient.create().getEventBus().post(new PostHudLayerRenderEvent(
                drawContext, drawContext.tickDelta(), elementCount, HudLayerDispatcher.isEditing()));
        WidgetBatchRenderer.flushCurrentBatch();
    }

    private static boolean isEditing() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.currentScreen instanceof ChatScreen;
    }
}
