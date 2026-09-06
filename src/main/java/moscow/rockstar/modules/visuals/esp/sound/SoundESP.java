/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.SoundInstance
 *  net.minecraft.VertexFormats
 */
package moscow.rockstar.modules.visuals.esp.sound;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.visuals.esp.sound.SoundMarker;
import moscow.rockstar.modules.visuals.esp.sound.SoundType;
import moscow.rockstar.render.core.ColorRenderContext;
import moscow.rockstar.render.core.FontRenderContext;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.render.VertexFormats;
import pyrock.events.game.SoundEvent;
import pyrock.events.render.PreHudRenderEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Sound ESP", category=ModuleCategory.VISUALS, disableLocked=true, description="modules.descriptions.sound_esp")
public class SoundESP
extends Module {
    private MultiBooleanSetting select;
    private MultiBooleanSetting.Option trident;
    private MultiBooleanSetting.Option tnt;
    private MultiBooleanSetting.Option fireworks;
    private FontMetrics fontMetrics = null;
    private final List<SoundMarker> soundMarkers = new ArrayList<SoundMarker>();
    private final EventListener<SoundEvent> onSoundEvent = soundEvent -> {
        SoundInstance class_11132 = soundEvent.getSound();
        if (this.isTrackedSoundEffect(class_11132)) {
            SoundMarker soundMarker = new SoundMarker(soundEvent.getSound());
            this.soundMarkers.add(soundMarker);
        }
    };
    private final EventListener<PreHudRenderEvent> onPreHudRenderEvent = preHudRenderEvent -> {
        if (this.fontMetrics == null) {
            this.fontMetrics = Font.MEDIUM.metrics(12.0f);
        }
        this.soundMarkers.removeIf(SoundMarker::isExpired);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ColorRenderContext colorRenderContext = new ColorRenderContext(VertexFormats.POSITION_COLOR, preHudRenderEvent.getContext().getMatrices());
        for (SoundMarker object : this.soundMarkers) {
            object.renderLabel(preHudRenderEvent.getContext(), this.fontMetrics, SoundType.TRIDENT);
        }
        colorRenderContext.render();
        for (SoundMarker soundMarker : this.soundMarkers) {
            soundMarker.renderLabel(preHudRenderEvent.getContext(), this.fontMetrics, SoundType.TNT);
        }
        FontRenderContext fontRenderContext = new FontRenderContext(VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, preHudRenderEvent.getContext().getMatrices());
        for (SoundMarker soundMarker : this.soundMarkers) {
            soundMarker.renderLabel(preHudRenderEvent.getContext(), this.fontMetrics, SoundType.FIREWORK);
        }
        fontRenderContext.render();
        RenderSystem.disableBlend();
    };

    public SoundESP() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.select = new MultiBooleanSetting(this, "modules.settings.sound_esp.select");
        this.trident = new MultiBooleanSetting.Option(this.select, "modules.settings.sound_esp.select.trident").select();
        this.tnt = new MultiBooleanSetting.Option(this.select, "modules.settings.sound_esp.select.tnt");
        this.fireworks = new MultiBooleanSetting.Option(this.select, "modules.settings.sound_esp.select.fireworks").select();
    }

    private boolean isTrackedSoundEffect(SoundInstance class_11132) {
        String string = class_11132.getId().toString().toLowerCase();
        if (string.equals("minecraft:entity.generic.explode") && this.tnt.isSelected()) {
            return true;
        }
        if ((string.equals("minecraft:item.trident.throw") || string.equals("minecraft:item.trident.return")) && this.trident.isSelected()) {
            return true;
        }
        return string.equals("minecraft:entity.firework_rocket.launch") && this.fireworks.isSelected();
    }
}
