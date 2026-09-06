/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.ArmorStandEntity
 *  net.minecraft.ChestBlock
 *  net.minecraft.Vec2f
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.modules.visuals.world;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.visuals.hud.Interface;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

@ModuleInfo(name="Warden Helper", category=ModuleCategory.VISUALS, disableLocked=true)
public class WardenHelper
extends Module {
    private static final Pattern wardenServerPattern = Pattern.compile("(\\d{1,2}):(\\d{2})(?::(\\d{2}))?");
    private static final Pattern wardenItemPattern = Pattern.compile("(\\d+)\\s*(\u0441|s|\u0441\u0435\u043a|sec)");
    private static final Pattern wardenChatPattern = Pattern.compile("(\\d+)\\s*(\u043c|m|\u043c\u0438\u043d|min(?:\\.|ute)?)\\s*(?:(\\d+)\\s*(\u0441|s|\u0441\u0435\u043a|sec(?:\\.|ond)?))?");
    private final EventListener<PreHudRenderEvent> onPreHudRenderEventListener = preHudRenderEvent -> {
        if (WardenHelper.minecraftClient.world == null || WardenHelper.minecraftClient.player == null) {
            return;
        }
        MatrixStack class_45872 = preHudRenderEvent.getContext().getMatrices();
        ColorRGBA colorRGBA = ColorPalette.getAccentColor();
        for (Entity class_12972 : WardenHelper.minecraftClient.world.getEntities()) {
            Vec3d VanillaChestLootTableGenerator;
            Vec2f VanillaAdventureTabAdvancementGenerator;
            String string;
            ArmorStandEntity class_15312;
            if (!(class_12972 instanceof ArmorStandEntity) || !this.isWardenEntity(class_15312 = (ArmorStandEntity)class_12972) || (string = this.formatWardenText(class_15312.getName().getString())) == null || (VanillaAdventureTabAdvancementGenerator = ProjectionUtils.projectToScreen(VanillaChestLootTableGenerator = ProjectionUtils.interpolateEntityPosition((Entity)class_15312, minecraftClient.getRenderTickCounter().getTickDelta(true)).add(0.0, 1.2, 0.0))) == null) continue;
            float f = (float)WardenHelper.minecraftClient.player.getPos().distanceTo(VanillaChestLootTableGenerator);
            float f2 = MathHelper.clamp((float)(1.0f - f / 20.0f), (float)0.5f, (float)1.0f);
            class_45872.push();
            class_45872.translate(VanillaAdventureTabAdvancementGenerator.x, VanillaAdventureTabAdvancementGenerator.y, 0.0f);
            class_45872.scale(f2, f2, 1.0f);
            WardenHelper.renderWardenLabel(preHudRenderEvent.getContext(), string, colorRGBA, 1.0f);
            class_45872.pop();
        }
    };

    public boolean isWardenEntity(ArmorStandEntity class_15312) {
        if (!this.isEnabled()) {
            return false;
        }
        if (WardenHelper.minecraftClient.world == null) {
            return false;
        }
        String string = class_15312.getName().getString();
        if (string == null || string.isBlank()) {
            return false;
        }
        if (this.formatWardenText(string) == null) {
            return false;
        }
        return this.getWardenPosition(class_15312.getBlockPos()) != null;
    }

    private String formatWardenText(String string) {
        Matcher matcher = wardenServerPattern.matcher(string);
        if (matcher.find()) {
            return matcher.group();
        }
        matcher = wardenChatPattern.matcher(string);
        if (matcher.find()) {
            return matcher.group();
        }
        matcher = wardenItemPattern.matcher(string);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private BlockPos getWardenPosition(BlockPos adminsky) {
        if (WardenHelper.minecraftClient.world == null) {
            return null;
        }
        for (int i = 1; i <= 3; ++i) {
            BlockPos adminsky2 = adminsky.down(i);
            if (!(WardenHelper.minecraftClient.world.getBlockState(adminsky2).getBlock() instanceof ChestBlock)) continue;
            return adminsky2;
        }
        return null;
    }

    private static void renderWardenLabel(CustomDrawContext customDrawContext, String string, ColorRGBA colorRGBA, float f) {
        float f2 = MathHelper.clamp((float)f, (float)0.0f, (float)1.0f);
        if (f2 <= 0.01f) {
            return;
        }
        float f3 = f2 * Interface.getLiquidGlassAlpha();
        float f4 = f2 * Interface.getBlurAlpha();
        if (f3 > 0.01f) {
            WardenHelper.renderWardenServerLabel(customDrawContext, string, colorRGBA, f3);
        }
        if (f4 > 0.01f) {
            WardenHelper.renderWardenItemLabel(customDrawContext, string, colorRGBA, f4);
        }
    }

    private static void renderWardenServerLabel(CustomDrawContext customDrawContext, String string, ColorRGBA colorRGBA, float f) {
        FontMetrics fontMetrics = Font.SEMIBOLD.metrics(10.0f);
        float f2 = 6.0f;
        float f3 = 21.0f;
        float f4 = 8.0f;
        float f5 = 6.0f;
        float f6 = fontMetrics.measureText(string);
        float f7 = Math.max(f4 * 2.0f + f2 + f5 + f6, 52.0f) - 1.0f;
        float f8 = -f7 / 2.0f;
        float f9 = 4.0f;
        WidgetState widgetState = WidgetState.uniform(f3 / 2.0f);
        ColorRGBA colorRGBA2 = ColorPalette.getPanelBackgroundColor().withAlpha(60.0f * f);
        ColorRGBA colorRGBA3 = ColorPalette.getPrimaryTextColor().withAlpha(255.0f * f);
        customDrawContext.drawLiquidGlass(f8, f9, f7, f3, 2.0f, 0.08f, widgetState, ColorRGBA.WHITE.withAlpha(191.0f));
        customDrawContext.drawRoundedRect(f8, f9, f7, f3, widgetState, colorRGBA2);
        float f10 = f8 + f4;
        float f11 = f9 + (f3 - f2) / 2.0f;
        customDrawContext.drawRoundedRect(f10, f11, f2, f2, WidgetState.uniform(f2 / 2.0f), colorRGBA.withAlpha(245.0f * f));
        customDrawContext.drawText(fontMetrics, string, f10 + f2 + f5, f9 + (f3 - fontMetrics.getFontTopOffset()) / 2.0f, colorRGBA3);
    }

    private static void renderWardenItemLabel(CustomDrawContext customDrawContext, String string, ColorRGBA colorRGBA, float f) {
        FontMetrics fontMetrics = Font.SEMIBOLD.metrics(9.0f);
        float f2 = 21.0f;
        float f3 = 6.0f;
        float f4 = 8.0f;
        float f5 = 6.0f;
        float f6 = WardenHelper.clampWardenScale(fontMetrics.measureText(string));
        float f7 = f3 + f5 + f6;
        float f8 = WardenHelper.clampWardenScale(f7 + f4 * 2.0f);
        float f9 = WardenHelper.clampWardenScale(-f8 / 2.0f);
        float f10 = 5.5f;
        WidgetState widgetState = WidgetState.uniform(f2 / 2.0f);
        ColorRGBA colorRGBA2 = new ColorRGBA(13.0f, 18.0f, 20.0f, 238.0f * f);
        customDrawContext.drawRoundedRect(f9, f10, f8, f2, widgetState, colorRGBA2);
        float f11 = f9 + f4;
        float f12 = WardenHelper.clampWardenCoordinate(f10, f2, f3);
        customDrawContext.drawRoundedRect(f11, f12, f3, f3, WidgetState.uniform(f3 / 2.0f), colorRGBA.withAlpha(245.0f * f));
        float f13 = f11 + f3 + f5;
        float f14 = WardenHelper.clampWardenScale(f10 + (f2 - fontMetrics.getFontTopOffset()) / 2.0f - 0.5f);
        customDrawContext.drawText(fontMetrics, string, f13, f14, ColorRGBA.WHITE.withAlpha(250.0f * f));
    }

    private static float clampWardenCoordinate(float f, float f2, float f3) {
        return WardenHelper.clampWardenScale(f + (f2 - f3) / 2.0f);
    }

    private static float clampWardenScale(float f) {
        return (float)Math.round(f * 2.0f) / 2.0f;
    }
}
