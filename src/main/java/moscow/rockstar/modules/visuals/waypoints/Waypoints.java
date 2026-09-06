/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Entity
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec2f
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.MathHelper
 *  net.minecraft.BlockHitResult
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.BuiltBuffer
 */
package moscow.rockstar.modules.visuals.waypoints;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.render.world.SkyboxRenderer;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.util.Timer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BuiltBuffer;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.events.window.KeyPressEvent;
import pyrock.events.window.MouseEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Waypoints", category=ModuleCategory.VISUALS)
public class Waypoints
extends Module {
    private IntegerSetting hotkeySetting;
    private NumberSetting playerWaypointLifetimeSetting;
    private NumberSetting blockWaypointLifetimeSetting;
    private BooleanSetting syncThemeSetting;
    private ColorSetting waypointColorSetting;
    private static final String SELF_WAYPOINT_KEY = "%self%";
    private final Map<String, Marker> waypoints = new HashMap<String, Marker>();
    private final EventListener<KeyPressEvent> keyPressListener = keyPressEvent -> {
        if (this.hotkeySetting.isIntValid(keyPressEvent.getKey()) && keyPressEvent.getAction() == 1 && Waypoints.minecraftClient.currentScreen == null) {
            this.createWaypointAtTarget();
        }
    };
    private final EventListener<MouseEvent> mouseListener = mouseEvent -> {
        if (this.hotkeySetting.isIntValid(mouseEvent.getButton()) && Waypoints.minecraftClient.currentScreen == null) {
            this.createWaypointAtTarget();
        }
    };
    private final EventListener<Render3DEvent> render3DListener = render3DEvent -> {
        MatrixStack class_45872 = render3DEvent.getMatrices();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        Camera class_41842 = Waypoints.minecraftClient.gameRenderer.getCamera();
        Identifier class_29602 = RockstarClient.resourceId("textures/bloom.png");
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (Marker marker : this.waypoints.values()) {
            Entity class_12972 = null;
            ColorRGBA colorRGBA = this.getMarkerColor(marker);
            for (Entity class_12973 : Waypoints.minecraftClient.world.getEntities()) {
                if (!class_12973.getName().getString().equals(marker.getDisplayText())) continue;
                class_12972 = class_12973;
            }
            if (class_12972 == Waypoints.minecraftClient.player) continue;
            class_45872.push();
            ItemRenderUtils.translateToWorldPosition(class_45872, marker.position);
            boolean bl = Waypoints.minecraftClient.player.getPos().distanceTo(marker.position) > 60.0;
            int n = bl ? 150 : 10;
            for (int i = 0; i < n; ++i) {
                float f = 0.6f;
                if (bl) {
                    f *= 2.0f;
                }
                float f2 = f * 5.0f;
                class_45872.push();
                class_45872.translate(0.0f, (float)i / 5.0f, 0.0f);
                class_45872.multiply(class_41842.getRotation());
                ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f2 / 2.0f, -f2 / 2.0f, -f / 2.0f, f2, f2, colorRGBA.mulAlpha((0.4f + marker.fadeInAnimation.getValue() * 0.7f) * marker.fadeOutAnimation.getValue() / 5.0f));
                ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f / 2.0f, -f / 2.0f, -f / 2.0f, f, f, colorRGBA.mulAlpha((0.4f + marker.fadeInAnimation.getValue() * 0.7f) * marker.fadeOutAnimation.getValue()));
                class_45872.pop();
            }
            class_45872.pop();
        }
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
    };
    private final EventListener<PreHudRenderEvent> preHudRenderListener = preHudRenderEvent -> {
        MatrixStack class_45872 = preHudRenderEvent.getContext().getMatrices();
        this.waypoints.values().removeIf(marker -> marker.expirationTimer.hasElapsed(marker.lifetimeMillis) && marker.fadeOutAnimation.getValue() <= 0.01f);
        for (Marker marker2 : this.waypoints.values()) {
            NamedMarker namedMarker;
            if (marker2 instanceof NamedMarker) {
                namedMarker = (NamedMarker)marker2;
                Entity class_12972 = null;
                for (Entity class_12973 : Waypoints.minecraftClient.world.getEntities()) {
                    if (!class_12973.getName().getString().equals(namedMarker.getDisplayText())) continue;
                    class_12972 = class_12973;
                }
                if (class_12972 != null) {
                    namedMarker.position = ProjectionUtils.interpolateEntityPosition(class_12972, minecraftClient.getRenderTickCounter().getTickDelta(true));
                }
            }
            Vec2f screenPosition = ProjectionUtils.projectToScreen(marker2.position.add(0.0, 0.5, 0.0));
            if (screenPosition != null) {
                float f = (float)Waypoints.minecraftClient.player.getPos().distanceTo(marker2.position);
                float f2 = MathHelper.clamp((float)(1.0f - f / 20.0f), (float)0.5f, (float)1.0f);
                float f3 = (0.4f + marker2.fadeInAnimation.getValue() * 0.7f) * marker2.fadeOutAnimation.getValue();
                class_45872.push();
                class_45872.translate(screenPosition.x, screenPosition.y, 0.0f);
                class_45872.scale(f2, f2, 1.0f);
                CustomDrawContext context = preHudRenderEvent.getContext();
                FontMetrics markerFont = Font.MEDIUM.metrics(8.0f);
                context.drawCenteredText(markerFont, marker2.getDisplayText(), 0.0f, -markerFont.getFontTopOffset() / 2.0f, this.getMarkerColor(marker2).withAlpha(255.0f * f3));
                class_45872.pop();
            }
            this.updateMarkerAnimation(marker2);
        }
    };

    public Waypoints() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.hotkeySetting = new IntegerSetting(this, "modules.settings.waypoints.key").withValue(86);
        this.playerWaypointLifetimeSetting = new NumberSetting(this, "modules.settings.waypoints.player_time").setStep(1.0f).setMinValue(5.0f).setMaxValue(100.0f).setValue(20.0f);
        this.blockWaypointLifetimeSetting = new NumberSetting(this, "modules.settings.waypoints.block_time").setStep(1.0f).setMinValue(5.0f).setMaxValue(20.0f).setValue(10.0f);
        this.syncThemeSetting = new BooleanSetting(this, "theme.sync").enable();
        this.waypointColorSetting = new ColorSetting(this, "modules.settings.waypoints.color", this.syncThemeSetting::isEnabled).setColor(ColorPalette.getAccentColor()).setAlphaEnabled(false);
    }

    private void createWaypointAtTarget() {
        if (Waypoints.minecraftClient.player == null || Waypoints.minecraftClient.world == null) {
            return;
        }
        ColorRGBA waypointColor = this.getWaypointColor();
        Vec3d lookDirection = Waypoints.minecraftClient.player.getRotationVec(minecraftClient.getRenderTickCounter().getTickDelta(true));
        Vec3d rayEnd = Waypoints.minecraftClient.player.getEyePos().add(lookDirection.multiply(200.0));
        Entity targetedEntity = null;
        for (Entity entity : Waypoints.minecraftClient.world.getEntities()) {
            Vec3d hitPoint = entity.getBoundingBox().expand(0.3).raycast(Waypoints.minecraftClient.player.getEyePos(), rayEnd).orElse(null);
            if (hitPoint == null || Waypoints.minecraftClient.player.getEyePos().distanceTo(hitPoint) > 200.0) continue;
            targetedEntity = entity;
        }
        Marker waypoint;
        if (targetedEntity != null) {
            waypoint = new NamedMarker(waypointColor, targetedEntity.getName().getString(), targetedEntity.getPos(), (long)this.playerWaypointLifetimeSetting.getValue() * 1000L);
        } else {
            HitResult hitResult = Waypoints.minecraftClient.player.raycast(200.0, minecraftClient.getRenderTickCounter().getTickDelta(true), false);
            if (!(hitResult instanceof BlockHitResult blockHit) || hitResult.getType() != HitResult.Type.BLOCK) {
                return;
            }
            waypoint = new Marker(waypointColor, blockHit.getPos(), (long)this.blockWaypointLifetimeSetting.getValue() * 1000L);
        }
        this.waypoints.put(SELF_WAYPOINT_KEY, waypoint);
    }

    @Override
    public void onTick() {
        if (Waypoints.minecraftClient.player == null || Waypoints.minecraftClient.world == null || this.waypoints.isEmpty()) {
            return;
        }
    }

    private void updateMarkerAnimation(Marker marker) {
        if (marker.fadeOutAnimation.getValue() == 1.0f) {
            marker.fadeInAnimation.toggleDirection();
            marker.fadeInAnimation.setDuration(Math.max((marker.lifetimeMillis - marker.expirationTimer.getElapsedMillis()) / 4L, 450L));
        } else {
            marker.fadeInAnimation.setDuration(0L);
            marker.fadeInAnimation.update(1.0f);
        }
        marker.fadeInAnimation.setDuration(Math.max((marker.lifetimeMillis - marker.expirationTimer.getElapsedMillis()) / 4L, 450L));
        if (marker.expirationTimer.getElapsedMillis() >= marker.lifetimeMillis) {
            marker.fadeOutAnimation.update(0.0f);
        } else {
            marker.fadeOutAnimation.update(1.0f);
        }
    }

    private ColorRGBA getWaypointColor() {
        return this.syncThemeSetting.isEnabled() ? ColorPalette.getAccentColor() : this.waypointColorSetting.getColor();
    }

    private ColorRGBA getMarkerColor(Marker marker) {
        return this.syncThemeSetting.isEnabled() ? ColorPalette.getAccentColor() : marker.color;
    }

    static class NamedMarker
    extends Marker {
        private final String waypointName;

        public NamedMarker(ColorRGBA colorRGBA, String string, Vec3d VanillaChestLootTableGenerator, long l) {
            super(colorRGBA, VanillaChestLootTableGenerator, l);
            this.waypointName = string;
        }

        @Override
        public String getDisplayText() {
            return this.waypointName;
        }
    }

    static class Marker {
        final ColorRGBA color;
        public Vec3d position;
        final Timer expirationTimer = new Timer();
        final long lifetimeMillis;
        final Animation fadeInAnimation = new Animation(300L, Easing.easeInOutCubicBezier);
        final Animation fadeOutAnimation = new Animation(300L, Easing.easeInOutCubicBezier);

        public String getDisplayText() {
            return String.format("%s %s %s", Math.round(this.position.x), Math.round(this.position.y), Math.round(this.position.z));
        }

        @Generated
        public Marker(ColorRGBA colorRGBA, Vec3d VanillaChestLootTableGenerator, long l) {
            this.color = colorRGBA;
            this.position = VanillaChestLootTableGenerator;
            this.lifetimeMillis = l;
        }

        @Generated
        public void setPosition(Vec3d VanillaChestLootTableGenerator) {
            this.position = VanillaChestLootTableGenerator;
        }

        @Generated
        public long getWaypointLifetimeMillis() {
            return this.lifetimeMillis;
        }
    }
}
