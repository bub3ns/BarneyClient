/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.AbstractClientPlayerEntity
 *  net.minecraft.BuiltBuffer
 */
package moscow.rockstar.modules.other.testing;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.movement.speed.Speed;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.esp.EntityRenderContext;
import moscow.rockstar.settings.ColorRangeSetting;
import moscow.rockstar.settings.EasingSetting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.screens.TestCategoryScreen;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BuiltBuffer;
import pyrock.events.render.HudRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Test", category=ModuleCategory.OTHER)
public class Test
extends Module {
    private ColorRangeSetting gradientSetting;
    private EasingSetting easingSetting;
    private final EventListener<Render3DEvent> render3DListener = render3DEvent -> {
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Camera class_41842 = Test.minecraftClient.gameRenderer.getCamera();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Speed speed = RockstarClient.create().getModuleRegistry().getModule(Speed.class);
        for (AbstractClientPlayerEntity TrackedPosition : Test.minecraftClient.world.getPlayers()) {
            if (Test.minecraftClient.player == TrackedPosition) continue;
            EntityRenderContext.renderBoundingBox(class_45872, class_2872, TrackedPosition.getBoundingBox().offset(TrackedPosition.getPos().add(TrackedPosition.getPos().subtract(new Vec3d(TrackedPosition.prevX, TrackedPosition.prevY, TrackedPosition.prevZ)).multiply((double)speed.getDistanceSetting().getValue()))).offset(-TrackedPosition.getX(), -TrackedPosition.getY(), -TrackedPosition.getZ()).offset(-VanillaChestLootTableGenerator.getX(), -VanillaChestLootTableGenerator.getY(), -VanillaChestLootTableGenerator.getZ()), ColorPalette.getAccentColor().withAlpha(100.0f));
        }
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    };
    private final Animation cubeAnimation = new Animation(1000L, Easing.easeInOutCubicPolynomial);
    private final EventListener<HudRenderEvent> hudRenderListener = hudRenderEvent -> {
        CustomDrawContext customDrawContext = hudRenderEvent.getContext();
        this.cubeAnimation.toggleDirection();
        float f = 10.0f + this.cubeAnimation.getValue() * 340.0f;
        customDrawContext.drawIcon("cube", 100.0f, 100.0f, f, ColorRGBA.WHITE);
    };

    public Test() {
        this.initializeTestSettings();
    }

    @Compile(obfuscation=4)
    private void initializeTestSettings() {
        this.gradientSetting = new ColorRangeSetting(this, "Tested gradient").setColorRange(ColorPalette.ACCENT_COLOR, ColorPalette.ACCENT_COLOR);
        this.easingSetting = new EasingSetting(this, "Damn curve");
    }

    @Override
    public void onEnable() {
        if (Test.minecraftClient.currentScreen == null && Test.minecraftClient.world == null) {
            return;
        }
        minecraftClient.setScreen(new TestCategoryScreen());
    }

    @Override
    public void onDisable() {
        if (Test.minecraftClient.currentScreen instanceof TestCategoryScreen) {
            minecraftClient.setScreen(null);
        }
    }
}
