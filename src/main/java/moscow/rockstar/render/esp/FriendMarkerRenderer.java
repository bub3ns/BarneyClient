/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.MatrixStack
 *  net.minecraft.AbstractClientPlayerEntity
 *  net.minecraft.BuiltBuffer
 */
package moscow.rockstar.render.esp;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.visuals.esp.targeting.PlayerTargetGroup;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.esp.TargetRenderModule;
import moscow.rockstar.render.geometry.CubeRenderer;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BuiltBuffer;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public class FriendMarkerRenderer
extends TargetRenderModule implements ClientAccess {
    private static final ColorRGBA FRIEND_MARKER_COLOR = new ColorRGBA(52.0f, 199.0f, 89.0f);
    private static boolean friendMarkersRendering = false;
    private static boolean friendModelRendering = false;
    private final BooleanSetting friendMarkerSetting = this.createSetting("esp.friend_markers");
    private final ModeSetting friendMarkerTypeSetting = new ModeSetting((SettingOwner)((Object)this), "esp.friend_markers.type", () -> !this.isValid2(PlayerTargetGroup.FRIENDS));
    private final ModeSetting.Option headMarkerOption = new ModeSetting.Option(this.friendMarkerTypeSetting, "esp.friend_markers.heads");
    private final ModeSetting.Option playerModelOption = new ModeSetting.Option(this.friendMarkerTypeSetting, "esp.friend_markers.sims").select();
    private final EventListener<Render3DEvent> friendMarkerListener = render3DEvent -> {
        if (!this.isValid2(PlayerTargetGroup.FRIENDS)) {
            return;
        }
        if (!this.playerModelOption.isSelected()) {
            return;
        }
        ItemRenderUtils.beginOverlayRendering(true);
        MatrixStack class_45872 = render3DEvent.getMatrices();
        BufferBuilder class_2872 = CubeRenderer.beginCubeBatch();
        for (AbstractClientPlayerEntity player : minecraftClient.world.getPlayers()) {
            if (!RockstarClient.create().getFriendListManager().containsFriend(player.getName().getString()) || player == minecraftClient.player) continue;
            class_45872.push();
            ItemRenderUtils.translateToWorldPosition(class_45872, ProjectionUtils.interpolateEntityPosition(player, render3DEvent.getTickDelta()));
            float f = 0.1f;
            CubeRenderer.drawCube(class_45872, class_2872, 0.0f, player.getHeight() + 0.4f, 0.0f, f, FRIEND_MARKER_COLOR.withAlpha(255.0f));
            class_45872.pop();
        }
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        ItemRenderUtils.endOverlayRendering();
    };

    public FriendMarkerRenderer() {
        super("friend_markers", new TargetGroup[]{TargetGroup.PLAYERS});
        this.enablePlayerGroup(PlayerTargetGroup.FRIENDS);
    }

    public boolean supportsTargetGroup(PlayerTargetGroup playerTargetGroup) {
        return playerTargetGroup == PlayerTargetGroup.FRIENDS;
    }

    @Override
    public void renderPreviewOverlay(RockstarDrawContext drawContext, Entity class_12972, float f, float f2, TargetGroup targetGroup, PlayerTargetGroup playerTargetGroup) {
        if (playerTargetGroup != PlayerTargetGroup.FRIENDS) {
            return;
        }
        if (!(class_12972 instanceof LivingEntity)) {
            return;
        }
        LivingEntity class_13092 = (LivingEntity)class_12972;
        if (!this.playerModelOption.isSelected()) {
            return;
        }
        MatrixStack class_45872 = drawContext.getMatrices();
        class_45872.push();
        class_45872.translate(f, f2 - class_13092.getHeight() * 15.0f, 50.0f);
        class_45872.scale(48.0f, 48.0f, -100.0f);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        BufferBuilder class_2872 = CubeRenderer.beginCubeBatch();
        CubeRenderer.drawCube(class_45872, class_2872, 0.0f, 0.0f, 0.0f, 0.1f, FRIEND_MARKER_COLOR.withAlpha(255.0f));
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        RenderSystem.enableDepthTest();
        class_45872.pop();
    }

    public boolean shouldRenderHeadMarker() {
        return this.isValid2(PlayerTargetGroup.FRIENDS) && this.headMarkerOption.isSelected();
    }

    @Generated
    public BooleanSetting getFriendMarkerSetting() {
        return this.friendMarkerSetting;
    }

    @Generated
    public ModeSetting getFriendMarkerTypeSetting() {
        return this.friendMarkerTypeSetting;
    }

    @Generated
    public ModeSetting.Option getHeadMarkerOption() {
        return this.headMarkerOption;
    }

    @Generated
    public ModeSetting.Option getPlayerModelOption() {
        return this.playerModelOption;
    }

    @Generated
    public EventListener<Render3DEvent> getFriendMarkerListener() {
        return this.friendMarkerListener;
    }

    @Generated
    public static boolean isFriendMarkersRendering() {
        return friendMarkersRendering;
    }

    @Generated
    public static void setFriendMarkersRendering(boolean bl) {
        friendMarkersRendering = bl;
    }

    @Generated
    public static boolean isFriendModelRendering() {
        return friendModelRendering;
    }

    @Generated
    public static void setFriendModelRendering(boolean bl) {
        friendModelRendering = bl;
    }
}
