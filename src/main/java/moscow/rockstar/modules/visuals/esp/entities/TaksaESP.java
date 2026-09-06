/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  moscow.rockstar.render.esp.TargetRenderModule
 *  net.minecraft.PlayerEntity
 *  net.minecraft.RenderLayer
 *  net.minecraft.Identifier
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumer
 *  net.minecraft.VertexConsumerProvider$Immediate
 *  net.minecraft.OverlayTexture
 */
package moscow.rockstar.modules.visuals.esp.entities;

import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.visuals.esp.entities.TaksaParticleState;
import moscow.rockstar.modules.visuals.esp.targeting.PlayerTargetGroup;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.render.esp.TargetRenderModule;
import moscow.rockstar.settings.BooleanSetting;
import net.minecraft.entity.player.PlayerEntity;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.Render3DEvent;

public class TaksaESP
extends TargetRenderModule
implements ClientAccess {
    private final BooleanSetting taksaSetting = this.createSetting("esp.taksa");
    private final TaksaParticleState particleState = new TaksaParticleState();
    private float animationTime = 0.0f;
    private final EventListener<ClientPlayerTickEvent> playerTickListener = clientPlayerTickEvent -> {
        if (!this.isValid2(PlayerTargetGroup.OTHERS)) {
            return;
        }
        if (minecraftClient.player == null) {
            return;
        }
        this.particleState.setTrackedPlayer(minecraftClient.player);
        this.particleState.updateParticleMotion();
    };
    private final EventListener<Render3DEvent> renderListener = render3DEvent -> {
        if (!this.isValid2(PlayerTargetGroup.OTHERS)) {
            return;
        }
        if (minecraftClient.player == null || minecraftClient.world == null) {
            return;
        }
        this.animationTime += 0.05f;
    };

    public TaksaESP() {
        super("taksa", new TargetGroup[]{TargetGroup.PLAYERS});
    }

    public boolean supportsTargetGroup(PlayerTargetGroup playerTargetGroup) {
        return playerTargetGroup == PlayerTargetGroup.OTHERS;
    }

    @Generated
    public BooleanSetting getTaksaSetting() {
        return this.taksaSetting;
    }

    @Generated
    public TaksaParticleState getParticleState() {
        return this.particleState;
    }

    @Generated
    public float getAnimationTime() {
        return this.animationTime;
    }

    @Generated
    public EventListener<ClientPlayerTickEvent> getPlayerTickListener() {
        return this.playerTickListener;
    }

    @Generated
    public EventListener<Render3DEvent> getRenderListener() {
        return this.renderListener;
    }
}
