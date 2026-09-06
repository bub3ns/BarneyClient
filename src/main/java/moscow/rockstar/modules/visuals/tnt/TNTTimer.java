/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.TntEntity
 *  net.minecraft.VertexFormats
 */
package moscow.rockstar.modules.visuals.tnt;

import java.util.ArrayList;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.visuals.tnt.TntMarker;
import moscow.rockstar.modules.visuals.tnt.TntType;
import moscow.rockstar.render.core.ColorRenderContext;
import moscow.rockstar.render.core.FontRenderContext;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.entity.TntEntity;
import net.minecraft.client.render.VertexFormats;
import pyrock.events.render.PreHudRenderEvent;

@ModuleInfo(name="TNT Timer", category=ModuleCategory.VISUALS, disableLocked=true, description="modules.descriptions.tnt_timer")
public class TNTTimer
extends Module {
    private FontMetrics fontMetrics = null;
    private final EventListener<PreHudRenderEvent> onPreHudRenderEvent = preHudRenderEvent -> {
        if (this.fontMetrics == null) {
            this.fontMetrics = Font.MEDIUM.metrics(12.0f);
        }
        ArrayList<TntMarker> markers = new ArrayList<>();
        for (Object entity : TNTTimer.minecraftClient.world.getEntities()) {
            if (entity instanceof TntEntity tntEntity) {
                markers.add(new TntMarker(tntEntity));
            }
        }
        ColorRenderContext colorRenderContext = new ColorRenderContext(VertexFormats.POSITION_COLOR, preHudRenderEvent.getContext().getMatrices());
        for (TntMarker tntMarker : markers) {
            tntMarker.renderLabel(preHudRenderEvent.getContext(), this.fontMetrics, TntType.PRIMED);
        }
        colorRenderContext.render();
        for (TntMarker tntMarker : markers) {
            tntMarker.renderLabel(preHudRenderEvent.getContext(), this.fontMetrics, TntType.MINECART);
        }
        FontRenderContext fontRenderContext = new FontRenderContext(VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, preHudRenderEvent.getContext().getMatrices());
        for (TntMarker tntMarker : markers) {
            tntMarker.renderLabel(preHudRenderEvent.getContext(), this.fontMetrics, TntType.BLOCK);
        }
        fontRenderContext.render();
    };
}
