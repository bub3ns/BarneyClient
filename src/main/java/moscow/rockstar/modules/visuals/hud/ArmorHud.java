/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ItemConvertible
 *  net.minecraft.VertexFormats
 *  net.minecraft.MathHelper
 *  net.minecraft.ChatScreen
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.modules.visuals.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.visuals.hud.Interface;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.FontRenderContext;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.render.state.UiScissorStack;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemConvertible;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

public class ArmorHud
extends HudElement {
    private static final float SINGLE_ROW_WIDTH = 16.0f;
    private static final float STACK_ROW_HEIGHT = 21.5f;
    private static final float ICON_GAP = 6.0f;
    private static final float COMPACT_PADDING = 5.5f;
    private static final float COMPACT_BOX_WIDTH = 27.0f;
    private static final float LABEL_FONT_SIZE = 8.0f;
    private static final float DURABILITY_BAR_RADIUS = 1.5f;
    private final ModeSetting armorDisplayMode = new ModeSetting((SettingOwner)((Object)this), "hud.targethud.armor");
    private final ModeSetting.Option showDurabilityNumbers = new ModeSetting.Option(this.armorDisplayMode, "hud.targethud.armor.number").select();
    private final ModeSetting.Option showDurabilityIcons = new ModeSetting.Option(this.armorDisplayMode, "hud.targethud.armor.icon");
    private final ModeSetting layoutMode = new ModeSetting((SettingOwner)((Object)this), "Background");
    private final ModeSetting.Option horizontalLayout = new ModeSetting.Option(this.layoutMode, "Old");
    private final ModeSetting.Option verticalLayout = new ModeSetting.Option(this.layoutMode, "New").select();
    private final ModeSetting positionMode = new ModeSetting((SettingOwner)((Object)this), "Position");
    private final ModeSetting.Option positionHorizontal = new ModeSetting.Option(this.positionMode, "Horizontal");
    private final ModeSetting.Option positionVertical = new ModeSetting.Option(this.positionMode, "Vertical");
    private final ModeSetting.Option positionAuto = new ModeSetting.Option(this.positionMode, "Auto").select();
    private final Animation containerAnimation = new Animation(300L, 0.0f, Easing.easeOutBack);
    private final Animation contentAnimation = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
    private final Animation[] slotAnimations = new Animation[4];
    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private static final ItemStack[][] armorSets = new ItemStack[][]{{new ItemStack((ItemConvertible)Items.LEATHER_BOOTS), new ItemStack((ItemConvertible)Items.LEATHER_LEGGINGS), new ItemStack((ItemConvertible)Items.LEATHER_CHESTPLATE), new ItemStack((ItemConvertible)Items.LEATHER_HELMET)}, {new ItemStack((ItemConvertible)Items.CHAINMAIL_BOOTS), new ItemStack((ItemConvertible)Items.CHAINMAIL_LEGGINGS), new ItemStack((ItemConvertible)Items.CHAINMAIL_CHESTPLATE), new ItemStack((ItemConvertible)Items.CHAINMAIL_HELMET)}, {new ItemStack((ItemConvertible)Items.IRON_BOOTS), new ItemStack((ItemConvertible)Items.IRON_LEGGINGS), new ItemStack((ItemConvertible)Items.IRON_CHESTPLATE), new ItemStack((ItemConvertible)Items.IRON_HELMET)}, {new ItemStack((ItemConvertible)Items.GOLDEN_BOOTS), new ItemStack((ItemConvertible)Items.GOLDEN_LEGGINGS), new ItemStack((ItemConvertible)Items.GOLDEN_CHESTPLATE), new ItemStack((ItemConvertible)Items.GOLDEN_HELMET)}, {new ItemStack((ItemConvertible)Items.DIAMOND_BOOTS), new ItemStack((ItemConvertible)Items.DIAMOND_LEGGINGS), new ItemStack((ItemConvertible)Items.DIAMOND_CHESTPLATE), new ItemStack((ItemConvertible)Items.DIAMOND_HELMET)}, {new ItemStack((ItemConvertible)Items.NETHERITE_BOOTS), new ItemStack((ItemConvertible)Items.NETHERITE_LEGGINGS), new ItemStack((ItemConvertible)Items.NETHERITE_CHESTPLATE), new ItemStack((ItemConvertible)Items.NETHERITE_HELMET)}};

    public ArmorHud() {
        super("modules.settings.name_tags.elementsToDisplay.armor", "hud/armor");
        for (int i = 0; i < this.slotAnimations.length; ++i) {
            this.slotAnimations[i] = new Animation(300L, 0.0f, Easing.easeOutBack);
        }
    }

    @Override
    public void renderComponent(RockstarDrawContext drawContext) {
        if (this.minecraftClient.player == null) {
            return;
        }
        float f = RenderSystem.getShaderColor()[3];
        if (this.horizontalLayout.isSelected()) {
            this.renderArmorIconsHorizontal(drawContext, f);
        } else {
            this.renderArmorIconsVertical(drawContext, f);
        }
    }

    private void renderArmorIconsHorizontal(RockstarDrawContext drawContext, float f) {
        boolean bl = this.showDurabilityNumbers.isSelected();
        FontMetrics fontMetrics = Font.SEMIBOLD.metrics(6.0f);
        ColorRGBA colorRGBA = ColorPalette.getPanelColor().withAlpha(255.0f * MathUtils.interpolateDouble(ColorPalette.getThemeColorSettings().getOverlayAlphaMinimum(), ColorPalette.getThemeColorSettings().getOverlayAlphaMaximum(), Interface.getLiquidGlassAlpha()));
        this.contentAnimation.setReverse(this.animation.getValue() * this.visible.getValue() >= 1.0f);
        float f2 = 0.0f;
        int n = 0;
        List<ArmorSlot> list = this.collectArmorSlots();
        for (ArmorSlot armorSlot : list) {
            this.slotAnimations[n].setReverse(armorSlot.isVisible());
            f2 += (float)(bl ? 23 : 20) * this.slotAnimations[n].getValue();
            ++n;
        }
        boolean bl2 = this.isCompactLayoutValid(drawContext, this.calculateVerticalLayoutHeight(bl));
        this.width = this.calculateHudWidth(drawContext);
        this.height = this.calculateHudHeight(drawContext);
        this.containerAnimation.update(f2);
        if (this.containerAnimation.getValue() <= 0.001f || this.contentAnimation.getValue() <= 0.001f) {
            return;
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        drawContext.drawItem(Items.DIAMOND_CHESTPLATE, -992.0f, 994.0f, 1.0f);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f);
        float f3 = 0.0f;
        ArrayList<ArmorLabel> arrayList = new ArrayList<ArmorLabel>();
        for (int i = 0; i < list.size(); ++i) {
            float f4;
            int n2 = bl2 ? list.size() - 1 - i : i;
            ArmorSlot armorSlot = list.get(n2);
            Animation animation = this.slotAnimations[n2];
            float f5 = this.contentAnimation.getValue() * animation.getValue();
            String durabilityLabel = this.formatDurabilityLabel(armorSlot.primaryItem);
            float f6 = bl ? 23.0f : 20.0f;
            float f7 = bl2 ? this.x : this.x + (this.width - this.containerAnimation.getValue()) / 2.0f + f3;
            float f8 = f4 = bl2 ? this.y + (this.height - this.containerAnimation.getValue()) / 2.0f + f3 : this.y;
            if (f5 <= 0.001f) {
                f3 += f6 * animation.getValue();
                continue;
            }
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(f * f5));
            drawContext.drawRoundedRect(f7, f4, bl ? 20.0f : 16.0f, bl ? 9.0f : 16.0f, WidgetState.uniform(1.5f), colorRGBA.withAlpha(colorRGBA.getAlpha() * f5));
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f);
            UiScissorStack.push((MatrixStack)drawContext.getMatrices(), f7, f4, 16.0f, bl ? 8.5f : 16.0f);
            float f9 = f * f5 * (bl ? 0.5f : 1.0f);
            if (!armorSlot.animated || armorSlot.blendProgress < 1.0f) {
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(f9 * (armorSlot.animated ? 1.0f - armorSlot.blendProgress : 1.0f)));
                if (bl) {
                    this.drawArmorItem(drawContext, armorSlot.primaryItem, f7 + 2.0f, f4 - 4.0f, 1.0f);
                } else {
                    this.drawArmorItem(drawContext, armorSlot.primaryItem, f7, f4, 1.0f);
                }
            }
            if (armorSlot.animated && armorSlot.blendProgress > 0.0f) {
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(f9 * armorSlot.blendProgress));
                if (bl) {
                    this.drawArmorItem(drawContext, armorSlot.secondaryItem, f7 + 2.0f, f4 - 4.0f, 1.0f);
                } else {
                    this.drawArmorItem(drawContext, armorSlot.secondaryItem, f7, f4, 1.0f);
                }
            }
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f);
            if (this.showDurabilityIcons.isSelected()) {
                this.drawLegacyDurabilityBar(drawContext, armorSlot.primaryItem, f7, f4, f5);
            }
            UiScissorStack.pop();
            if (bl) {
                arrayList.add(new ArmorLabel(durabilityLabel, f7 + 10.5f, f4 + 2.5f, 255.0f * f5));
            }
            f3 += f6 * animation.getValue();
        }
        this.drawArmorLabels(drawContext, fontMetrics, arrayList);
    }

    private void renderArmorIconsVertical(RockstarDrawContext drawContext, float f) {
        boolean bl = this.showDurabilityNumbers.isSelected();
        FontMetrics fontMetrics = Font.SEMIBOLD.metrics(6.0f);
        this.contentAnimation.setReverse(this.animation.getValue() * this.visible.getValue() >= 1.0f);
        float f2 = 0.0f;
        int n = 0;
        List<ArmorSlot> list = this.collectArmorSlots();
        for (ArmorSlot armorSlot : list) {
            this.slotAnimations[n].setReverse(armorSlot.isVisible());
            f2 += 21.5f * this.slotAnimations[n].getValue();
            ++n;
        }
        float f3 = f2 <= 0.0f ? 0.0f : f2 - 5.5f;
        boolean bl2 = this.isCompactLayoutValid(drawContext, this.getHorizontalLayoutWidth());
        this.width = this.calculateHudWidth(drawContext);
        this.height = this.calculateHudHeight(drawContext);
        if (f3 <= 0.001f) {
            return;
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        drawContext.drawItem(Items.DIAMOND_CHESTPLATE, -992.0f, 994.0f, 1.0f);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f);
        float f4 = bl2 ? 27.0f : Math.max(12.0f, 12.0f + f3);
        float f5 = bl2 ? Math.max(12.0f, 12.0f + f3) : 27.0f;
        float f6 = this.x + (this.width - f4) / 2.0f;
        float f7 = this.y + (this.height - f5) / 2.0f;
        drawContext.drawClientRect(f6, f7, f4, f5, 1.0f, 0.0f, 7.0f, 8.0f);
        float f8 = 0.0f;
        ArrayList<ArmorLabel> arrayList = new ArrayList<ArmorLabel>();
        ArrayList<DurabilityBar> arrayList2 = new ArrayList<DurabilityBar>();
        CustomDrawContext.ItemBatch itemBatch = drawContext.beginItemBatch();
        try {
            for (int i = 0; i < list.size(); ++i) {
                int n2 = bl2 ? list.size() - 1 - i : i;
                ArmorSlot armorSlot = list.get(n2);
                Animation animation = this.slotAnimations[n2];
                float f9 = this.contentAnimation.getValue() * animation.getValue();
                String durabilityLabel = this.formatDurabilityLabel(armorSlot.primaryItem);
                float f10 = bl2 ? f6 + 5.5f : f6 + 6.0f + f8;
                float f11 = bl2 ? f7 + 6.0f + f8 : f7 + 5.5f;
                float f12 = 0.12f * animation.getValue();
                if (f9 <= 0.001f) {
                    f8 += 21.5f * animation.getValue();
                    continue;
                }
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(f * f9));
                ItemRenderUtils.translateAndScale(drawContext.getMatrices(), f10 + 8.0f, f11 + 8.0f, 1.0f + f12);
                float f13 = f * f9;
                if (!armorSlot.animated || armorSlot.blendProgress < 1.0f) {
                    RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(f13 * (armorSlot.animated ? 1.0f - armorSlot.blendProgress : 1.0f)));
                    this.drawArmorItem(drawContext, armorSlot.primaryItem, f10, f11, 1.0f);
                }
                if (armorSlot.animated && armorSlot.blendProgress > 0.0f) {
                    RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(f13 * armorSlot.blendProgress));
                    this.drawArmorItem(drawContext, armorSlot.secondaryItem, f10, f11, 1.0f);
                }
                ItemRenderUtils.popMatrix(drawContext.getMatrices());
                if (this.showDurabilityIcons.isSelected() && armorSlot.primaryItem.isItemBarVisible()) {
                    arrayList2.add(new DurabilityBar(armorSlot.primaryItem, f10, f11, f9));
                }
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f);
                if (bl) {
                    arrayList.add(new ArmorLabel(durabilityLabel, f10 + 8.0f + 1.5f, f11 + 14.0f, 255.0f * f9));
                }
                f8 += 21.5f * animation.getValue();
            }
        }
        finally {
            if (itemBatch != null) {
                itemBatch.restoreEnabledFlag();
            }
        }
        for (DurabilityBar durabilityBar : arrayList2) {
            this.drawDurabilityBar(drawContext, durabilityBar.getItemStack(), durabilityBar.getX(), durabilityBar.getY(), durabilityBar.getAlpha());
        }
        this.drawArmorLabels(drawContext, fontMetrics, arrayList);
    }

    private void drawArmorItem(RockstarDrawContext drawContext, ItemStack class_17992, float f, float f2, float f3) {
        drawContext.drawBatchItem(class_17992, f, f2, f3, 80);
    }

    private void drawArmorLabels(RockstarDrawContext drawContext, FontMetrics fontMetrics, List<ArmorLabel> list) {
        if (list.isEmpty()) {
            return;
        }
        FontRenderContext pass = new FontRenderContext(
            VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, drawContext.getMatrices());
        for (ArmorLabel armorLabel : list) {
            drawContext.drawCenteredText(fontMetrics, armorLabel.getText(), armorLabel.getX(), armorLabel.getY(), ColorPalette.getPrimaryTextColor().withAlpha(armorLabel.getAlpha()));
        }
        pass.render();
    }

    private boolean isCompactLayoutValid(RockstarDrawContext drawContext, float f) {
        if (this.positionVertical.isSelected()) {
            return true;
        }
        if (this.positionHorizontal.isSelected()) {
            return false;
        }
        float f2 = drawContext.getScaledWindowWidth();
        return this.x <= 8.0f || this.x + f >= f2 - 8.0f;
    }

    @Override
    public void update(RockstarDrawContext drawContext) {
        this.width = this.calculateHudWidth(drawContext);
        this.height = this.calculateHudHeight(drawContext);
        super.update(drawContext);
    }

    private float calculateHudWidth(RockstarDrawContext drawContext) {
        boolean bl = this.showDurabilityNumbers.isSelected();
        if (this.horizontalLayout.isSelected()) {
            return this.isCompactLayoutValid(drawContext, this.calculateVerticalLayoutHeight(bl)) ? (float)(bl ? 20 : 16) : this.calculateVerticalLayoutHeight(bl);
        }
        return this.isCompactLayoutValid(drawContext, this.getHorizontalLayoutWidth()) ? 27.0f : this.getHorizontalLayoutWidth();
    }

    private float calculateHudHeight(RockstarDrawContext drawContext) {
        boolean bl = this.showDurabilityNumbers.isSelected();
        if (this.horizontalLayout.isSelected()) {
            return this.isCompactLayoutValid(drawContext, this.calculateVerticalLayoutHeight(bl)) ? this.calculateVerticalLayoutHeight(bl) : (float)(bl ? 9 : 16);
        }
        return this.isCompactLayoutValid(drawContext, this.getHorizontalLayoutWidth()) ? this.getHorizontalLayoutWidth() : 27.0f;
    }

    private float calculateVerticalLayoutHeight(boolean bl) {
        return Math.max(12, (bl ? 23 : 20) * this.slotAnimations.length);
    }

    private float getHorizontalLayoutWidth() {
        return 28.0f + 21.5f * (float)(this.slotAnimations.length - 1);
    }

    private String formatDurabilityLabel(ItemStack itemStack) {
        return TargetHud.formatDurability(itemStack);
    }

    private List<ArmorSlot> collectArmorSlots() {
        ArrayList<ItemStack> armorItems = new ArrayList<>();
        minecraftClient.player.getArmorItems().forEach(armorItems::add);
        boolean bl = armorItems.stream().allMatch(ItemStack::isEmpty) && minecraftClient.currentScreen instanceof ChatScreen;
        if (!bl) {
            ArrayList<ArmorSlot> arrayList2 = new ArrayList<ArmorSlot>();
            for (ItemStack class_17992 : armorItems) {
                arrayList2.add(new ArmorSlot(class_17992, ItemStack.EMPTY, 0.0f, false));
            }
            return arrayList2;
        }
        CyclingArmorSet cyclingArmorSet = this.getCyclingArmorSet();
        ArrayList<ArmorSlot> arrayList3 = new ArrayList<ArmorSlot>();
        for (int i = 0; i < cyclingArmorSet.getCurrentItems().length; ++i) {
            arrayList3.add(new ArmorSlot(cyclingArmorSet.getCurrentItems()[i], cyclingArmorSet.getNextItems()[i], cyclingArmorSet.getBlendProgress(), true));
        }
        return arrayList3;
    }

    private CyclingArmorSet getCyclingArmorSet() {
        float f = (float)(System.currentTimeMillis() % (2200L * (long)armorSets.length)) / (float)(2200L * (long)armorSets.length);
        float f2 = f * (float)armorSets.length;
        int n = (int)Math.floor(f2) % armorSets.length;
        int n2 = (n + 1) % armorSets.length;
        float f3 = MathHelper.clamp((float)(f2 - (float)Math.floor(f2)), (float)0.0f, (float)1.0f);
        float f4 = (float)(0.5 - 0.5 * Math.cos(Math.PI * (double)f3));
        return new CyclingArmorSet(armorSets[n], armorSets[n2], f4);
    }

    private void drawDurabilityBar(RockstarDrawContext drawContext, ItemStack class_17992, float f, float f2, float f3) {
        if (!class_17992.isItemBarVisible()) {
            return;
        }
        float f4 = f + 2.0f;
        float f5 = f2 + 13.0f;
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(0.0f, 0.0f, 200.0f);
        drawContext.drawRoundedRect(f4, f5, 13.0f, 1.5f, WidgetState.uniform(0.25f), ColorRGBA.WHITE.mulAlpha(0.25f * f3));
        drawContext.drawRoundedRect(f4, f5, (float)class_17992.getItemBarStep(), 1.5f, WidgetState.uniform(0.25f), ColorPalette.getAccentColor().mulAlpha(f3));
        drawContext.getMatrices().pop();
    }

    private void drawLegacyDurabilityBar(RockstarDrawContext drawContext, ItemStack class_17992, float f, float f2, float f3) {
        if (!class_17992.isItemBarVisible()) {
            return;
        }
        float f4 = f + 2.0f;
        float f5 = f2 + 13.0f;
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(0.0f, 0.0f, 200.0f);
        drawContext.drawRect(f4, f5, 13.0f, 2.0f, ColorRGBA.BLACK.withAlpha(255.0f * f3));
        drawContext.drawRect(f4, f5, class_17992.getItemBarStep(), 1.0f, ColorRGBA.fromInt(0xFF000000 | class_17992.getItemBarColor()).withAlpha(255.0f * f3));
        drawContext.getMatrices().pop();
    }

    static final class ArmorSlot {
        final ItemStack primaryItem;
        final ItemStack secondaryItem;
        final float blendProgress;
        final boolean animated;

        ArmorSlot(ItemStack class_17992, ItemStack class_17993, float f, boolean bl) {
            this.primaryItem = class_17992;
            this.secondaryItem = class_17993;
            this.blendProgress = f;
            this.animated = bl;
        }

        boolean isVisible() {
            return this.animated || !this.primaryItem.isEmpty();
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "primaryItem", "secondaryItem", "blendProgress", "animated");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "primaryItem", "secondaryItem", "blendProgress", "animated");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "primaryItem", "secondaryItem", "blendProgress", "animated");
        }

        public ItemStack getPrimaryItem() {
            return this.primaryItem;
        }

        public ItemStack getSecondaryItem() {
            return this.secondaryItem;
        }

        public float getBlendProgress() {
            return this.blendProgress;
        }

        public boolean isAnimated() {
            return this.animated;
        }
    }

    static final class ArmorLabel {
        private final String text;
        private final float x;
        private final float y;
        private final float alpha;

        ArmorLabel(String string, float f, float f2, float f3) {
            this.text = string;
            this.x = f;
            this.y = f2;
            this.alpha = f3;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "text", "x", "y", "alpha");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "text", "x", "y", "alpha");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "text", "x", "y", "alpha");
        }

        public String getText() {
            return this.text;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public float getAlpha() {
            return this.alpha;
        }
    }

    static final class DurabilityBar {
        private final ItemStack itemStack;
        private final float x;
        private final float y;
        private final float alpha;

        DurabilityBar(ItemStack class_17992, float f, float f2, float f3) {
            this.itemStack = class_17992;
            this.x = f;
            this.y = f2;
            this.alpha = f3;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "itemStack", "x", "y", "alpha");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "itemStack", "x", "y", "alpha");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "itemStack", "x", "y", "alpha");
        }

        public ItemStack getItemStack() {
            return this.itemStack;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public float getAlpha() {
            return this.alpha;
        }
    }

    static final class CyclingArmorSet {
        private final ItemStack[] currentItems;
        private final ItemStack[] nextItems;
        private final float blendProgress;

        CyclingArmorSet(ItemStack[] class_1799Array, ItemStack[] class_1799Array2, float f) {
            this.currentItems = class_1799Array;
            this.nextItems = class_1799Array2;
            this.blendProgress = f;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "currentItems", "nextItems", "blendProgress");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "currentItems", "nextItems", "blendProgress");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "currentItems", "nextItems", "blendProgress");
        }

        public ItemStack[] getCurrentItems() {
            return this.currentItems;
        }

        public ItemStack[] getNextItems() {
            return this.nextItems;
        }

        public float getBlendProgress() {
            return this.blendProgress;
        }
    }
}
