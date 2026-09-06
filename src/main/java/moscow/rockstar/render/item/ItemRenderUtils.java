/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.RotationAxis
 *  net.minecraft.BuiltBuffer
 */
package moscow.rockstar.render.item;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.hud.CustomHotbarHud;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.colors.GradientColors;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.hud.HudElementRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.BuiltBuffer;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

public final class ItemRenderUtils
implements ClientAccess,
WindowHandle {
    private static final int STANDARD_SLOT_SPACING = 20;
    private static final int COMPACT_SLOT_OFFSET = 3;

    public static void drawHotbarHighlight(CustomDrawContext customDrawContext, int n, ColorRGBA colorRGBA) {
        float f;
        float f2;
        if (n < 0 || n > 8 || ItemRenderUtils.minecraftClient.player == null) {
            return;
        }
        RenderSystem.enableBlend();
        int n2 = minecraftClient.getWindow().getScaledWidth();
        int n3 = minecraftClient.getWindow().getScaledHeight();
        float f7 = CustomHotbarHud.verticalOffset();
        if (ItemRenderUtils.isCustomHotbarActive()) {
            float f8 = (float)n2 / 2.0f - 101.0f;
            float f9 = (float)n3 - 30.0f - f7;
            f2 = f8 + 6.0f + (float)n * 21.5f;
            f = f9 + 5.5f;
        } else {
            int n4 = n2 / 2 - 91;
            float f9 = (float)(n3 - 22) - f7;
            f2 = n4 + n * STANDARD_SLOT_SPACING + COMPACT_SLOT_OFFSET;
            f = f9 + 3.0f;
        }
        customDrawContext.drawRoundedRect(f2, f, 18.0f, 18.0f, WidgetState.uniform(4.0f), new GradientColors(colorRGBA.mulAlpha(0.0f), colorRGBA, colorRGBA.mulAlpha(0.0f), colorRGBA));
    }

    /**
     * ORIGINAL: rockstar/ilIlil/iIiiiIIiI#I ()Z -
     * {@code registry.getCustomHotbarHud() != null && hud.isShowing() && hud.show()}.
     * HudElementRegistry has no getCustomHotbarHud() in this tree, so the element is resolved out
     * of the live element list the way CustomHotbarHud.verticalOffset() already does.
     */
    private static boolean isCustomHotbarActive() {
        HudElementRegistry hudElementRegistry = RockstarClient.create().getHudElementRegistry();
        if (hudElementRegistry == null) {
            return false;
        }
        CustomHotbarHud customHotbarHud = null;
        for (HudElement hudElement : hudElementRegistry.elements()) {
            if (!(hudElement instanceof CustomHotbarHud)) continue;
            customHotbarHud = (CustomHotbarHud)hudElement;
            break;
        }
        return customHotbarHud != null && customHotbarHud.isShowing() && customHotbarHud.show();
    }

    public static boolean highlightItem(CustomDrawContext customDrawContext, Item class_17922, ColorRGBA colorRGBA) {
        if (ItemRenderUtils.minecraftClient.player == null) {
            return false;
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack class_17992 = ItemRenderUtils.minecraftClient.player.getInventory().getStack(i);
            if (class_17992.getItem() != class_17922) continue;
            ItemRenderUtils.drawHotbarHighlight(customDrawContext, i, colorRGBA);
            return true;
        }
        return false;
    }

    public static boolean highlightMatchingItem(CustomDrawContext customDrawContext, Predicate<ItemStack> predicate, ColorRGBA colorRGBA) {
        if (ItemRenderUtils.minecraftClient.player == null) {
            return false;
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack class_17992 = ItemRenderUtils.minecraftClient.player.getInventory().getStack(i);
            if (class_17992.isEmpty() || !predicate.test(class_17992)) continue;
            ItemRenderUtils.drawHotbarHighlight(customDrawContext, i, colorRGBA);
            return true;
        }
        return false;
    }

    public static int countMatchingItems(CustomDrawContext customDrawContext, Item class_17922, ColorRGBA colorRGBA) {
        if (ItemRenderUtils.minecraftClient.player == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < 9; ++i) {
            ItemStack class_17992 = ItemRenderUtils.minecraftClient.player.getInventory().getStack(i);
            if (class_17992.getItem() != class_17922) continue;
            ItemRenderUtils.drawHotbarHighlight(customDrawContext, i, colorRGBA);
            ++n;
        }
        return n;
    }

    public static int countMatchingItems(CustomDrawContext customDrawContext, Predicate<ItemStack> predicate, ColorRGBA colorRGBA) {
        if (ItemRenderUtils.minecraftClient.player == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < 9; ++i) {
            ItemStack class_17992 = ItemRenderUtils.minecraftClient.player.getInventory().getStack(i);
            if (class_17992.isEmpty() || !predicate.test(class_17992)) continue;
            ItemRenderUtils.drawHotbarHighlight(customDrawContext, i, colorRGBA);
            ++n;
        }
        return n;
    }

    public static void translateAndRotate(MatrixStack class_45872, float f, float f2, float f3) {
        class_45872.push();
        class_45872.translate(f, f2, 0.0f);
        class_45872.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f3));
        class_45872.translate(-f, -f2, 0.0f);
    }

    public static void translateAndRotateAtDepth(MatrixStack class_45872, float f, float f2, float f3) {
        class_45872.push();
        class_45872.translate(f, f2, 150.0f);
        class_45872.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f3));
        class_45872.translate(-f, -f2, -150.0f);
    }

    public static void translateAndScale(MatrixStack class_45872, float f, float f2, float f3) {
        class_45872.push();
        class_45872.translate(f, f2, 0.0f);
        class_45872.scale(f3, f3, 1.0f);
        class_45872.translate(-f, -f2, 0.0f);
    }

    public static void popMatrix(MatrixStack class_45872) {
        class_45872.pop();
    }

    public static void translateToCamera(MatrixStack class_45872) {
        Camera class_41842 = ItemRenderUtils.minecraftClient.gameRenderer.getCamera();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        Vec3d WallPlayerSkullBlock = Vec3d.ZERO.subtract(VanillaChestLootTableGenerator);
        class_45872.translate(WallPlayerSkullBlock.getX(), WallPlayerSkullBlock.getY(), WallPlayerSkullBlock.getZ());
    }

    public static void translateToWorldPosition(MatrixStack class_45872, Vec3d VanillaChestLootTableGenerator) {
        Camera class_41842 = ItemRenderUtils.minecraftClient.gameRenderer.getCamera();
        Vec3d WallPlayerSkullBlock = class_41842.getPos();
        Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator.subtract(WallPlayerSkullBlock);
        class_45872.translate(VanillaEntityLootTableGenerator.getX(), VanillaEntityLootTableGenerator.getY(), VanillaEntityLootTableGenerator.getZ());
    }

    public static void beginOverlayRendering(boolean bl) {
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask((boolean)false);
        if (bl) {
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }
    }

    public static void endOverlayRendering() {
        RenderSystem.depthMask((boolean)true);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    public static void flushVertexConsumer(BufferBuilder class_2872) {
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
    }

    @Generated
    private ItemRenderUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
