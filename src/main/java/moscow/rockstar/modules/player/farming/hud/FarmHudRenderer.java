/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ItemConvertible
 *  net.minecraft.Vec2f
 */
package moscow.rockstar.modules.player.farming.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.ModuleRegistry;
import moscow.rockstar.modules.player.farming.core.AutoFarm;
import moscow.rockstar.modules.player.farming.core.FarmModeBase;
import moscow.rockstar.modules.player.farming.hud.FarmDisplayCategory;
import moscow.rockstar.modules.player.farming.hud.FarmHudMetrics;
import moscow.rockstar.modules.player.farming.hud.FarmMetric;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.animation.AnimatedValue;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.modules.visuals.hud.DynamicIslandHud;
import moscow.rockstar.ui.hud.DynamicIslandExpandableEntry;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.Vec2f;
import pyrock.utility.render.ColorRGBA;

public class FarmHudRenderer
extends DynamicIslandExpandableEntry
implements ClientAccess {
    private static final float HUD_EXPANDED_WIDTH = 138.0f;
    private static final float HUD_COLLAPSED_HEIGHT = 15.0f;
    private static final float HUD_MAX_CONTENT_WIDTH = 128.0f;
    private static final float ICON_VERTICAL_OFFSET = 10.0f;
    private static final float ICON_HORIZONTAL_OFFSET = 7.0f;
    private static final float TEXT_BASELINE_OFFSET = 8.0f;
    private static final float LABEL_VALUE_GAP = 4.0f;
    private static final float COMPACT_ICON_PADDING = 7.0f;
    private static final float METRIC_LABEL_OFFSET = 9.0f;
    private static final float METRIC_ROW_GAP = 10.0f;
    private static final float ICON_ROW_OFFSET = 24.0f;
    private static final float GRAPH_BASELINE_PADDING = 6.0f;
    private static final float GRAPH_TOP_OFFSET = 38.0f;
    private static final float GRAPH_HEIGHT = 46.0f;
    private static final float GRAPH_BORDER_PADDING = 6.0f;
    private static final float GRAPH_BACKGROUND_ALPHA = 0.62f;
    private static final String STATUS_LABEL_SEPARATOR = "  ";
    private static final float GRAPH_POINT_RADIUS = 7.0f;
    private static final float GRAPH_CANVAS_WIDTH = 78.0f;
    private static final float GRAPH_CANVAS_HEIGHT = 54.0f;
    private static final float GRAPH_POINT_PADDING = 7.0f;
    private static final float GRAPH_SAMPLE_LIMIT = 71.0f;
    private static final float GRAPH_SMOOTHING_RADIUS = 6.0f;
    private static final int MAX_GRAPH_SAMPLES = 16;
    private static final int GRAPH_INTERPOLATION_STEPS = 3;
    private static final int GRAPH_SMOOTHING_WINDOW = 6;
    private static final float GRAPH_MIN_ALPHA = 0.25f;
    private static final float GRAPH_MAX_ALPHA = 0.75f;
    private Component dashboardComponent;

    public FarmHudRenderer(MultiBooleanSetting multiBooleanSetting) {
        super(multiBooleanSetting, "auto_farm");
    }

    /** ORIGINAL: rockstar/ilIlil/IiIiiiiII#I(Lrockstar/ilIlil/IiIiiIIII;)Lrockstar/ilIlil/iii; */
    @Override
    public Component content(DynamicIslandHud island) {
        if (this.dashboardComponent == null) {
            this.dashboardComponent = new FarmDashboardComponent(island);
        }
        return this.dashboardComponent;
    }

    /** ORIGINAL: rockstar/ilIlil/IiIiiiiII#canShow()Z */
    @Override
    public boolean isVisible() {
        AutoFarm autoFarm = this.findAutoFarmModule();
        return autoFarm != null && autoFarm.isEnabled() && EntityUtils.isClientWorldReady();
    }

    void drawFarmItemIcon(RockstarDrawContext drawContext, ItemStack class_17992, float f, float f2, float f3, float f4) {
        if (class_17992 == null || class_17992.isEmpty() || f4 <= 0.02f) {
            return;
        }
        float[] fArray = (float[])RenderSystem.getShaderColor().clone();
        RenderSystem.setShaderColor((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)(fArray[3] * f4));
        drawContext.drawItem(class_17992, f, f2, f3 / 16.0f);
        RenderSystem.setShaderColor((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
    }

    void drawClippedFarmText(RockstarDrawContext drawContext, FontMetrics fontMetrics, String string, float f, float f2, float f3, ColorRGBA colorRGBA) {
        if (string == null || string.isBlank() || f3 <= 1.0f) {
            return;
        }
        if (fontMetrics.measureText(string) <= f3 + 0.5f) {
            drawContext.drawText(fontMetrics, string, f, f2, colorRGBA);
            return;
        }
        drawContext.drawFadeText(fontMetrics, string, f, f2, colorRGBA, 0.0f, Math.min(7.0f, f3), f3);
    }

    static float measureTextWidth(FontMetrics fontMetrics, String string) {
        if (string == null || string.isEmpty()) {
            return 0.0f;
        }
        float f = 0.0f;
        for (int i = 0; i < string.length(); ++i) {
            f += FarmHudRenderer.measureCharacterWidth(fontMetrics, string.charAt(i));
        }
        return f;
    }

    static float measureCharacterWidth(FontMetrics fontMetrics, char c) {
        return c >= '0' && c <= '9' ? FarmHudRenderer.measureDigitWidth(fontMetrics) : fontMetrics.measureCharacter(c);
    }

    private static float measureDigitWidth(FontMetrics fontMetrics) {
        float f = 0.0f;
        for (char c = '0'; c <= '9'; c = (char)(c + '\u0001')) {
            f = Math.max(f, fontMetrics.measureCharacter(c));
        }
        return f;
    }

    float snapToPixelGrid(float f) {
        double d = minecraftClient.getWindow().getScaleFactor();
        return d <= 0.0 ? f : (float)((double)Math.round((double)f * d) / d);
    }

    String getFarmStatusText() {
        FarmModeBase farmModeBase = this.getActiveFarmMode();
        if (farmModeBase == null) {
            return "";
        }
        String string = farmModeBase.getFarmState().getStateKey();
        String string2 = farmModeBase.getDisplayName();
        return string2 == null || string2.isBlank() ? string : string + " " + string2;
    }

    ItemStack getFarmDisplayItem() {
        FarmModeBase farmModeBase = this.getActiveFarmMode();
        ItemStack class_17992 = farmModeBase == null ? ItemStack.EMPTY : farmModeBase.getFarmDisplayItem();
        return class_17992 == null || class_17992.isEmpty() ? new ItemStack((ItemConvertible)Items.WHEAT) : class_17992;
    }

    FarmDisplayCategory getDisplayCategory() {
        FarmModeBase farmModeBase = this.getActiveFarmMode();
        return farmModeBase == null ? FarmDisplayCategory.BLOCKS : farmModeBase.getFarmDisplayCategory();
    }

    FarmMetric getPrimaryMetric() {
        FarmModeBase farmModeBase = this.getActiveFarmMode();
        return farmModeBase == null ? FarmMetric.BLOCK_COUNT : farmModeBase.getDisplayMetric();
    }

    FarmHudMetrics getFarmHudMetrics() {
        AutoFarm autoFarm = this.findAutoFarmModule();
        return autoFarm == null ? null : autoFarm.getFarmStateManager();
    }

    FarmModeBase getActiveFarmMode() {
        AutoFarm autoFarm = this.findAutoFarmModule();
        return autoFarm == null ? null : autoFarm.getInitialFarmState();
    }

    private AutoFarm findAutoFarmModule() {
        ModuleRegistry moduleRegistry = RockstarClient.create().getModuleRegistry();
        return moduleRegistry == null ? null : moduleRegistry.getModule(AutoFarm.class);
    }


    final class FarmDashboardComponent
    extends Component {
        private final DynamicIslandHud island;
        private final FarmTextLine farmTextLine = new FarmTextLine();

        FarmDashboardComponent(DynamicIslandHud island) {
            this.island = island;
            this.size(48.0f, 15.0f);
            this.interactive(false);
            this.snapPosition();
            this.snapSize();
        }

        @Override
        protected void measure() {
            boolean keyCaptureActive = this.island.isExpanded();
            this.prefW = keyCaptureActive ? 138.0f : this.getIconAnimationProgress();
            this.prefH = keyCaptureActive ? 78.0f : 15.0f;
        }

        @Override
        protected void onTick(float f, float f2, float f3) {
            super.onTick(f, f2, f3);
            this.farmTextLine.setLineOpacity(f);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float f) {
            FarmHudMetrics farmHudMetrics = FarmHudRenderer.this.getFarmHudMetrics();
            if (farmHudMetrics == null) {
                return;
            }
            float f2 = this.island.getExpandAnim().getValue();
            float f3 = this.x();
            float f4 = this.y();
            float f5 = this.w();
            this.farmTextLine.setLineText(FarmHudMetrics.formatElapsedTime(farmHudMetrics.getElapsedMillis()));
            this.drawProgressBar(drawContext, f3, f4, f2, f);
            if (f2 < 0.999f) {
                this.drawMetricValue(drawContext, farmHudMetrics, f3, f4, f5, f2, f);
            }
            if (f2 > 0.001f) {
                this.drawMetricRow(drawContext, farmHudMetrics, f3, f4, f5, f2, f);
            }
        }

        @Override
        protected void drawChildren(RockstarDrawContext drawContext, float f) {
        }

        private void drawProgressBar(RockstarDrawContext drawContext, float f, float f2, float f3, float f4) {
            float f5 = MathUtils.interpolateDouble(8.0, 10.0, f3);
            float f6 = f + MathUtils.interpolateDouble(7.0, 10.0, f3);
            float f7 = f2 + MathUtils.interpolateDouble(this.scaleMetricValue(8.0f), 9.0, f3);
            if (this.isMetricValueVisible(f3)) {
                f6 = FarmHudRenderer.this.snapToPixelGrid(f6);
                f7 = FarmHudRenderer.this.snapToPixelGrid(f7);
            }
            FarmHudRenderer.this.drawFarmItemIcon(drawContext, FarmHudRenderer.this.getFarmDisplayItem(), f6, f7, f5, f4);
        }

        private boolean isMetricValueVisible(float f) {
            return f <= 0.002f && Math.abs(this.w() - this.prefW) <= 0.05f;
        }

        private float scaleMetricValue(float f) {
            return (15.0f - f) / 2.0f;
        }

        private float getFontHeight(FontMetrics fontMetrics) {
            return 9.0f + (10.0f - fontMetrics.getFontTopOffset()) / 2.0f;
        }

        private void drawMetricValue(RockstarDrawContext drawContext, FarmHudMetrics farmHudMetrics, float f, float f2, float f3, float f4, float f5) {
            float f6 = f5 * (1.0f - f4);
            if (f6 <= 0.004f) {
                return;
            }
            FontMetrics fontMetrics = Font.MEDIUM.metrics(7.0f);
            ColorRGBA colorRGBA = ColorPalette.getPrimaryTextColor().withAlpha(255.0f * f6);
            float f7 = f + 7.0f + 8.0f + 4.0f;
            float f8 = f2 + this.scaleMetricValue(fontMetrics.getFontTopOffset());
            String string = this.getMetricLabel();
            float f9 = FarmHudRenderer.measureTextWidth(fontMetrics, string);
            float f10 = FarmHudRenderer.measureTextWidth(fontMetrics, this.farmTextLine.getLineText());
            float f11 = f + f3 - 7.0f - f10;
            FarmHudRenderer.this.drawClippedFarmText(drawContext, fontMetrics, string, f7, f8, f11 - f7, colorRGBA);
            this.farmTextLine.drawAnimatedText(drawContext, fontMetrics, Math.min(f7 + f9, f11), f8, colorRGBA);
        }

        private String getMetricLabel() {
            String string = FarmHudRenderer.this.getFarmStatusText();
            return string.isBlank() ? "" : string + FarmHudRenderer.STATUS_LABEL_SEPARATOR;
        }

        private void drawMetricRow(RockstarDrawContext drawContext, FarmHudMetrics farmHudMetrics, float f, float f2, float f3, float f4, float f5) {
            float f6 = f5 * f4;
            if (f6 <= 0.004f) {
                return;
            }
            ColorRGBA colorRGBA = ColorPalette.getPrimaryTextColor();
            FontMetrics fontMetrics = Font.MEDIUM.metrics(7.0f);
            this.drawMetricDetails(drawContext, farmHudMetrics, f, f2, f3, f6);
            String string = Localization.translate("hud.dynamic_island.statuses.auto_farm");
            float f7 = FarmHudRenderer.measureTextWidth(fontMetrics, this.farmTextLine.getLineText());
            float f8 = f + 10.0f + 10.0f + 4.0f;
            this.farmTextLine.drawAnimatedText(drawContext, fontMetrics, f + f3 - 10.0f - f7, f2 + this.getFontHeight(fontMetrics), colorRGBA.withAlpha(255.0f * f6));
            FarmHudRenderer.this.drawClippedFarmText(drawContext, fontMetrics, string, f8, f2 + this.getFontHeight(fontMetrics), f + f3 - 10.0f - f7 - 6.0f - f8, colorRGBA.withAlpha(255.0f * f6));
            this.drawMetricIcon(drawContext, f, f2, f3, f6);
            this.drawMetricText(drawContext, farmHudMetrics, f, f2, f3, f6);
        }

        private void drawMetricIcon(RockstarDrawContext drawContext, float f, float f2, float f3, float f4) {
            float f5;
            FarmModeBase farmModeBase = FarmHudRenderer.this.getActiveFarmMode();
            if (farmModeBase == null) {
                return;
            }
            FontMetrics fontMetrics = Font.REGULAR.metrics(6.0f);
            ColorRGBA colorRGBA = ColorPalette.getAccentColor();
            float f6 = f2 + 24.0f;
            float f7 = f + f3 - 10.0f;
            String string = FarmHudRenderer.this.getFarmStatusText();
            float f8 = f5 = string.isBlank() ? 0.0f : fontMetrics.measureText(string);
            if (f5 > 0.0f) {
                drawContext.drawText(fontMetrics, string, f7 - f5, f6, colorRGBA.withAlpha(153.0f * f4));
            }
            float f9 = f + 10.0f;
            float f10 = f7 - f5 - (f5 > 0.0f ? 6.0f : 0.0f) - f9;
            FarmHudRenderer.this.drawClippedFarmText(drawContext, fontMetrics, Localization.translate(farmModeBase.getName()), f9, f6, f10, colorRGBA.withAlpha(255.0f * f4));
        }

        private void drawMetricText(RockstarDrawContext drawContext, FarmHudMetrics farmHudMetrics, float f, float f2, float f3, float f4) {
            FarmDisplayCategory farmDisplayCategory = FarmHudRenderer.this.getDisplayCategory();
            float f5 = f + 10.0f;
            float f6 = f + f3 / 2.0f + 2.0f;
            this.drawLabelValuePair(drawContext, f5, f2, farmDisplayCategory.getDisplayKey(), FarmHudMetrics.formatCurrency(farmHudMetrics.getBlocksMined()), ColorPalette.getPrimaryTextColor(), f4);
            long l = farmHudMetrics.getProfitChange();
            this.drawLabelValuePair(drawContext, f6, f2, Localization.translate("modules.auto_farm.stats.income"), FarmHudMetrics.formatSignedCurrency(l), l < 0L ? ColorPalette.RED : ColorPalette.getPrimaryTextColor(), f4);
        }

        private void drawLabelValuePair(RockstarDrawContext drawContext, float f, float f2, String string, String string2, ColorRGBA colorRGBA, float f3) {
            ColorRGBA colorRGBA2 = ColorPalette.getPrimaryTextColor().withAlpha(158.1f * f3);
            drawContext.drawText(Font.MEDIUM.metrics(6.0f), string, f, f2 + 38.0f, colorRGBA2);
            drawContext.drawText(Font.MEDIUM.metrics(9.0f), string2, f, f2 + 46.0f, colorRGBA.withAlpha(255.0f * f3));
        }

        private void drawMetricDetails(RockstarDrawContext drawContext, FarmHudMetrics farmHudMetrics, float f, float f2, float f3, float f4) {
            float f5 = f + 6.0f;
            float f6 = Math.max(1.0f, f3 - 12.0f);
            float f7 = f2 + 71.0f;
            float f8 = 3.0f;
            drawContext.drawRoundedRect(f5 + f8, f7 - 0.25f, Math.max(1.0f, f6 - 6.0f), 0.5f, WidgetState.uniform(0.25f), ColorPalette.getPrimaryTextColor().withAlpha(30.599998f * f4));
            float[] fArray = this.extractMetricValues(farmHudMetrics);
            if (fArray.length < 2) {
                return;
            }
            float f9 = 0.0f;
            for (float f10 : fArray) {
                f9 = Math.max(f9, f10);
            }
            if (f9 <= 0.0f) {
                f9 = 1.0f;
            }
            float f11 = 17.0f;
            float f12 = (f6 - 6.0f) / (float)(fArray.length - 1);
            Vec2f[] class_241Array = new Vec2f[fArray.length];
            for (int i = 0; i < fArray.length; ++i) {
                class_241Array[i] = new Vec2f(f5 + f8 + f12 * (float)i, f7 - f11 * (fArray[i] / f9));
            }
            ColorRGBA colorRGBA = ColorPalette.getAccentColor();
            this.drawGlyphSequence(drawContext, class_241Array, f7, colorRGBA, f4);
            this.drawAnimatedGlyphs(drawContext, class_241Array, f5, f2 + 54.0f - 2.0f, f6, f11 + 4.0f, colorRGBA.withAlpha(191.25f * f4));
            Vec2f VanillaAdventureTabAdvancementGenerator = class_241Array[class_241Array.length - 1];
            drawContext.drawRoundedRect(VanillaAdventureTabAdvancementGenerator.x - 1.5f, VanillaAdventureTabAdvancementGenerator.y - 1.5f, 3.0f, 3.0f, WidgetState.uniform(1.5f), colorRGBA.withAlpha(255.0f * f4));
        }

        private void drawGlyphSequence(RockstarDrawContext drawContext, Vec2f[] class_241Array, float f, ColorRGBA colorRGBA, float f2) {
            int n = (class_241Array.length - 1) * 6 + 1;
            float[] fArray = new float[n];
            float[] fArray2 = new float[n];
            int n2 = 0;
            for (int i = 0; i < class_241Array.length - 1; ++i) {
                Vec2f[] class_241Array2 = this.createGlyphSequence(class_241Array, i);
                for (int j = 0; j < 6; ++j) {
                    float f3 = (float)j / 6.0f;
                    fArray[n2] = (float)MathUtils.interpolateCubicBezier(f3, class_241Array[i].x, class_241Array2[0].x, class_241Array2[1].x, class_241Array[i + 1].x);
                    fArray2[n2] = (float)MathUtils.interpolateCubicBezier(f3, class_241Array[i].y, class_241Array2[0].y, class_241Array2[1].y, class_241Array[i + 1].y);
                    ++n2;
                }
            }
            fArray[n2] = class_241Array[class_241Array.length - 1].x;
            fArray2[n2] = class_241Array[class_241Array.length - 1].y;
            drawContext.drawAreaGradient(fArray, fArray2, f, colorRGBA.withAlpha(63.75f * f2), colorRGBA.withAlpha(0.0f));
        }

        private Vec2f[] createGlyphSequence(Vec2f[] class_241Array, int n) {
            Vec2f VanillaAdventureTabAdvancementGenerator = class_241Array[Math.max(0, n - 1)];
            Vec2f MagmaBlock = class_241Array[n];
            Vec2f VanillaHusbandryTabAdvancementGenerator = class_241Array[n + 1];
            Vec2f BlockMirror = class_241Array[Math.min(class_241Array.length - 1, n + 2)];
            return new Vec2f[]{new Vec2f(MagmaBlock.x + (VanillaHusbandryTabAdvancementGenerator.x - VanillaAdventureTabAdvancementGenerator.x) / 6.0f, MagmaBlock.y + (VanillaHusbandryTabAdvancementGenerator.y - VanillaAdventureTabAdvancementGenerator.y) / 6.0f), new Vec2f(VanillaHusbandryTabAdvancementGenerator.x - (BlockMirror.x - MagmaBlock.x) / 6.0f, VanillaHusbandryTabAdvancementGenerator.y - (BlockMirror.y - MagmaBlock.y) / 6.0f)};
        }

        private void drawAnimatedGlyphs(RockstarDrawContext drawContext, Vec2f[] class_241Array, float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
            for (int i = 0; i < class_241Array.length - 1; ++i) {
                Vec2f VanillaAdventureTabAdvancementGenerator = class_241Array[i];
                Vec2f MagmaBlock = class_241Array[i + 1];
                Vec2f[] class_241Array2 = this.createGlyphSequence(class_241Array, i);
                Vec2f VanillaHusbandryTabAdvancementGenerator = class_241Array2[0];
                Vec2f BlockMirror = class_241Array2[1];
                float f5 = Math.max(f, Math.min(Math.min(VanillaAdventureTabAdvancementGenerator.x, MagmaBlock.x), Math.min(VanillaHusbandryTabAdvancementGenerator.x, BlockMirror.x)) - 2.0f);
                float f6 = Math.min(f + f3, Math.max(Math.max(VanillaAdventureTabAdvancementGenerator.x, MagmaBlock.x), Math.max(VanillaHusbandryTabAdvancementGenerator.x, BlockMirror.x)) + 2.0f);
                float f7 = Math.max(f2, Math.min(Math.min(VanillaAdventureTabAdvancementGenerator.y, MagmaBlock.y), Math.min(VanillaHusbandryTabAdvancementGenerator.y, BlockMirror.y)) - 2.0f);
                float f8 = Math.min(f2 + f4, Math.max(Math.max(VanillaAdventureTabAdvancementGenerator.y, MagmaBlock.y), Math.max(VanillaHusbandryTabAdvancementGenerator.y, BlockMirror.y)) + 2.0f);
                drawContext.drawSmoothBezier(f5, f7, f6 - f5, f8 - f7, VanillaAdventureTabAdvancementGenerator, VanillaHusbandryTabAdvancementGenerator, BlockMirror, MagmaBlock, 1.0f, colorRGBA);
            }
        }

        private float[] extractMetricValues(FarmHudMetrics farmHudMetrics) {
            int n;
            int n2;
            int n3;
            long[] lArray = farmHudMetrics.getMetricHistory(FarmHudRenderer.this.getPrimaryMetric());
            if (lArray.length < 2) {
                return new float[0];
            }
            int n4 = Math.min(16, lArray.length);
            float[] fArray = new float[n4];
            for (int i = 0; i < n4; ++i) {
                n3 = (int)((long)(i + 1) * (long)lArray.length / (long)n4);
                n2 = (int)((long)i * (long)lArray.length / (long)n4);
                if (n3 <= n2) {
                    n3 = n2 + 1;
                }
                long l = 0L;
                for (n = n2; n < n3 && n < lArray.length; ++n) {
                    l += lArray[n];
                }
                fArray[i] = (float)l / (float)(n3 - n2);
            }
            float[] fArray2 = new float[n4];
            n2 = Math.max(0, 1);
            for (n3 = 0; n3 < n4; ++n3) {
                float f = 0.0f;
                int n5 = 0;
                for (n = n3 - n2; n <= n3 + n2; ++n) {
                    if (n < 0 || n >= n4) continue;
                    f += fArray[n];
                    ++n5;
                }
                fArray2[n3] = n5 == 0 ? fArray[n3] : f / (float)n5;
            }
            return fArray2;
        }

        private float getIconAnimationProgress() {
            FarmHudMetrics farmHudMetrics = FarmHudRenderer.this.getFarmHudMetrics();
            if (farmHudMetrics == null) {
                return 22.0f;
            }
            FontMetrics fontMetrics = Font.MEDIUM.metrics(7.0f);
            float f = 19.0f + FarmHudRenderer.measureTextWidth(fontMetrics, this.getMetricLabel()) + FarmHudRenderer.measureTextWidth(fontMetrics, FarmHudMetrics.formatElapsedTime(farmHudMetrics.getElapsedMillis())) + 7.0f;
            return Math.min(f, 128.0f);
        }
    }

    static final class FarmTextLine {
        private static final float LINE_HEIGHT = 4.0f;
        private static final int MAX_VISIBLE_LINES = 400;
        private final List<GlyphAnimationState> lineEntries = new ArrayList<GlyphAnimationState>();
        private String lineText = "";

        FarmTextLine() {
        }

        String getLineText() {
            return this.lineText;
        }

        void setLineText(String string) {
            if (string == null) {
                string = "";
            }
            if (string.equals(this.lineText)) {
                return;
            }
            boolean bl = string.length() != this.lineText.length();
            this.lineText = string;
            while (this.lineEntries.size() < string.length()) {
                this.lineEntries.add(new GlyphAnimationState());
            }
            while (this.lineEntries.size() > string.length()) {
                this.lineEntries.remove(this.lineEntries.size() - 1);
            }
            for (int i = 0; i < string.length(); ++i) {
                GlyphAnimationState glyphAnimationState = this.lineEntries.get(i);
                char c = string.charAt(i);
                if (c == glyphAnimationState.targetGlyph) continue;
                glyphAnimationState.currentGlyph = bl ? (char)'\u0000' : glyphAnimationState.targetGlyph;
                glyphAnimationState.targetGlyph = c;
                glyphAnimationState.glyphAnimation.snapTo(bl ? 1.0f : 0.0f);
                if (bl) continue;
                glyphAnimationState.glyphAnimation.setTarget(1.0f);
            }
        }

        void setLineOpacity(float f) {
            for (GlyphAnimationState glyphAnimationState : this.lineEntries) {
                glyphAnimationState.glyphAnimation.update(f);
            }
        }

        void drawAnimatedText(RockstarDrawContext drawContext, FontMetrics fontMetrics, float f, float f2, ColorRGBA colorRGBA) {
            float f3 = f;
            for (GlyphAnimationState glyphAnimationState : this.lineEntries) {
                float f4 = glyphAnimationState.glyphAnimation.getCurrent();
                this.drawAnimatedCharacter(drawContext, fontMetrics, glyphAnimationState.currentGlyph, f3, f2, colorRGBA, 1.0f - f4, 4.0f * f4);
                this.drawAnimatedCharacter(drawContext, fontMetrics, glyphAnimationState.targetGlyph, f3, f2, colorRGBA, f4, 4.0f * (f4 - 1.0f));
                f3 += FarmHudRenderer.measureCharacterWidth(fontMetrics, glyphAnimationState.targetGlyph);
            }
        }

        private void drawAnimatedCharacter(RockstarDrawContext drawContext, FontMetrics fontMetrics, char c, float f, float f2, ColorRGBA colorRGBA, float f3, float f4) {
            if (c == '\u0000' || f3 <= 0.004f) {
                return;
            }
            drawContext.drawText(fontMetrics, String.valueOf(c), f, f2 + f4, colorRGBA.withAlpha(colorRGBA.getAlpha() * f3));
        }
    }

    static final class GlyphAnimationState {
        char currentGlyph;
        char targetGlyph;
        final AnimatedValue glyphAnimation = new AnimatedValue(1.0f, Motion.resolveMotionMotionFromLongAndEasing(400L, Easing.easeOutBack));

        GlyphAnimationState() {
        }
    }
}

