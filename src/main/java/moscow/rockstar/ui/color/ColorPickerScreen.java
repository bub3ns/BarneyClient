/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 */
package moscow.rockstar.ui.color;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import moscow.rockstar.api.commands.ModuleActionCommand;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.visuals.hud.Interface;
import moscow.rockstar.network.http.client.JavaNetHttpClient;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingComponent;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.color.ColorPickerScreenAction;
import moscow.rockstar.ui.core.BooleanAction;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.FloatValueListener;
import moscow.rockstar.ui.core.InteractiveComponent;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingSnapshotCache;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.ui.text.ValueFormatter;
import moscow.rockstar.ui.widgets.controls.Slider;
import moscow.rockstar.ui.widgets.settings.KeyBindingControl;
import pyrock.utility.render.ColorRGBA;

public class ColorPickerScreen
extends SettingWidget {
    private final Animation openAnimation = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
    private boolean open;
    private float scalePivot = 2.0f;
    private Runnable closeCallback = () -> {};
    private boolean closeCallbackInvoked;
    private boolean drawBackground = true;
    private boolean constrainToScreen;
    private boolean liquidGlassStyle;
    private float verticalOffset = -1.0f;
    private long lastFrameTimeMillis;
    private final Set<SettingOwner> settingOwners = new LinkedHashSet<SettingOwner>();
    private final Component rootComponent = new Component().vertical().padding(Insets.symmetric(4.0f, 0.0f)).scrollable().configureLayoutState(scrollBar2 -> scrollBar2.offset(2.0f).padding(1.0f, 4.0f).thickness(2.5f).thumbColor(scrollBar -> ColorRGBA.BLACK.mix(ColorRGBA.WHITE, 0.3f).withAlpha(255.0f * (0.32f + 0.28f * scrollBar.hoverProgress() + 0.3f * scrollBar.dragProgress()))));
    public static final List<UiNode> transientNodes = new LinkedList<UiNode>();
    public static boolean transientNodesInitialized;

    public static <T extends UiNode> T registerTransientNode(T t) {
        transientNodes.add(t);
        return t;
    }

    public ColorPickerScreen(float f, float f2) {
        this(f, f2, 90.0f);
    }

    public ColorPickerScreen(float f, float f2, float f3) {
        this(f, f2, f3, 2.0f);
    }

    public ColorPickerScreen(float f, float f2, float f3, float f4) {
        this.x = f;
        this.y = f2;
        this.width = f3;
        this.scalePivot = f4;
        this.open = true;
    }

    @Override
    protected void renderContent(RockstarDrawContext drawContext) {
        this.openAnimation.setEasing(this.open ? Easing.easeOutBack : Easing.easeInBack);
        this.openAnimation.setReverse(this.open);
        long l = System.currentTimeMillis();
        float f = this.lastFrameTimeMillis == 0L ? 16.0f : Math.min(64.0f, (float)(l - this.lastFrameTimeMillis));
        this.lastFrameTimeMillis = l;
        this.rootComponent.width(this.width);
        if (this.constrainToScreen) {
            this.rootComponent.maxSize(this.width, this.height);
        }
        this.rootComponent.prepareRoot();
        if (this.verticalOffset >= 0.0f) {
            float f2 = Math.max(this.verticalOffset, (float)minecraftClient.getWindow().getScaledWidth() - this.verticalOffset - this.width);
            float f3 = Math.max(this.verticalOffset, (float)minecraftClient.getWindow().getScaledHeight() - this.verticalOffset - this.rootComponent.h());
            this.x = Math.clamp(this.x, this.verticalOffset, f2);
            this.y = Math.clamp(this.y, this.verticalOffset, f3);
        }
        this.rootComponent.snapAt(this.x, this.y);
        if (this.openAnimation.getValue() < 0.999f) {
            this.rootComponent.snapSubtree();
        }
        this.rootComponent.tick(f, drawContext.mouseX(), drawContext.mouseY());
        this.height = this.rootComponent.h();
        boolean bl = true;
        for (UiNode object : this.rootComponent.children()) {
            if (!object.inFlow()) continue;
            bl = false;
            break;
        }
        float f4 = Math.min(1.0f, this.openAnimation.getValue());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f4);
        ItemRenderUtils.translateAndScale(drawContext.getMatrices(), this.x + this.width / this.scalePivot, this.y + this.height / this.scalePivot, 0.5f + this.openAnimation.getValue() * 0.5f);
        if (this.drawBackground && !bl) {
            if (this.liquidGlassStyle) {
                drawContext.drawShadow(this.x, this.y, this.width, this.height, 25.0f, WidgetState.uniform(11.0f), ColorPalette.BLACK.mulAlpha(0.5f));
                drawContext.drawBlurredRect(this.x, this.y, this.width, this.height, 5.0f, 3.0f, WidgetState.uniform(11.0f), ColorPalette.WHITE);
                drawContext.drawSquircle(this.x, this.y, this.width, this.height, 3.0f, WidgetState.uniform(11.0f), ColorPalette.PANEL_COLOR);
                drawContext.drawSquircleBorder(this.x, this.y, this.width, this.height, 0.5f, 3.0f, WidgetState.uniform(11.0f), ColorPalette.BORDER_COLOR);
            } else {
                drawContext.drawShadow(this.x, this.y, this.width, this.height, 15.0f, WidgetState.uniform(6.0f), ColorRGBA.BLACK.withAlpha(127.5f));
                if (Interface.isBlurEnabled()) {
                    drawContext.drawBlurredRect(this.x, this.y, this.width, this.height, 45.0f, 7.0f, WidgetState.uniform(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.openAnimation.getValue() * Interface.getBlurAlpha()));
                }
                if (Interface.isLiquidGlassEnabled()) {
                    drawContext.drawLiquidGlass(this.x, this.y, this.width, this.height, 7.0f, 0.08f, WidgetState.uniform(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.openAnimation.getValue() * Interface.getLiquidGlassAlpha()));
                }
                drawContext.drawSquircle(this.x, this.y, this.width, this.height, 7.0f, WidgetState.uniform(6.0f), ColorPalette.getPanelColor().withAlpha(255.0f * MathUtils.interpolateDouble(ColorPalette.getThemeColorSettings().getOverlayAlphaMinimum(), ColorPalette.getThemeColorSettings().getOverlayAlphaMaximum(), Interface.getLiquidGlassAlpha())));
            }
        }
        if (!bl) {
            this.rootComponent.draw(drawContext, f4);
        }
        ItemRenderUtils.popMatrix(drawContext.getMatrices());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        if (!transientNodesInitialized) {
            transientNodes.removeIf(uiNode -> !uiNode.alive());
            for (UiNode uiNode2 : transientNodes) {
                uiNode2.prepareRoot();
                uiNode2.tick(f, drawContext.mouseX(), drawContext.mouseY());
            }
            for (UiNode uiNode2 : transientNodes) {
                uiNode2.draw(drawContext, 1.0f);
            }
            transientNodesInitialized = true;
        }
    }

    private ColorPickerScreen addChild(UiNode uiNode) {
        this.rootComponent.add(uiNode);
        return this;
    }

    public ColorPickerScreen addSetting(Setting setting) {
        if (setting instanceof AbstractSetting) {
            AbstractSetting abstractSetting = (AbstractSetting)setting;
            this.settingOwners.add(abstractSetting.getOwner());
        }
        // ORIGINAL: rockstar/ilIlil/IiIIiiIii#addSetting returns this.add(IiiIiiIIi.I(setting)),
        // i.e. the row goes through the shared wrapper
        //     rawComponent(setting).collapse().visibleWhen(setting::isVisible, IiiiIiiII.IIii, 220L)
        // Adding it bare dropped the per-setting visibility gate, so conditional rows stayed
        // visible in this popup even when their predicate said to hide them.
        return this.addChild(setting.buildComponent().fillWidth().padding(0.0f, 9.0f).collapse().visibleWhen(setting::hasValidSettingValue, Easing.easeOutQuart, 220L));
    }

    public boolean isColorPickerReady() {
        return SettingSnapshotCache.isCollectionValid(this.settingOwners);
    }

    public boolean isColorPickerScreenTargetReady() {
        return SettingSnapshotCache.acceptsCollectionForCollectionProcessorCacheTargetReady(this.settingOwners);
    }

    public ColorPickerScreen addSettingWidget(final SettingWidget settingWidget) {
        return this.addChild(new Component(){

            @Override
            public float desiredH() {
                return settingWidget.getHeight();
            }

            @Override
            protected void onTick(float f, float f2, float f3) {
            }

            @Override
            protected void drawSelf(RockstarDrawContext drawContext, float f) {
                SettingComponent settingComponent;
                settingWidget.setBounds(this.x(), this.y(), this.w(), settingWidget.getHeight());
                if (settingWidget instanceof SettingComponent) {
                    settingComponent = (SettingComponent)settingWidget;
                    settingComponent.setVisibleRegionTop(this.y());
                    settingComponent.setVisibleRegionHeight(settingWidget.getHeight());
                }
                settingWidget.render(drawContext);
            }

            @Override
            public boolean mouseClicked(float f, float f2, PointerAction pointerAction) {
                if (!this.contains(f, f2)) {
                    return false;
                }
                settingWidget.mouseClicked(f, f2, pointerAction);
                return true;
            }

            @Override
            public void mouseReleased(float f, float f2, PointerAction pointerAction) {
                settingWidget.mouseReleased(f, f2, pointerAction);
                super.mouseReleased(f, f2, pointerAction);
            }

            @Override
            public boolean mouseScrolled(float f, float f2, float f3, float f4) {
                if (!this.contains(f, f2)) {
                    return false;
                }
                settingWidget.mouseScrolled(f, f2, f3, f4);
                return false;
            }

            @Override
            public boolean keyPressed(int n, int n2, int n3) {
                settingWidget.keyPressed(n, n2, n3);
                return false;
            }

            @Override
            public boolean charTyped(char c, int n) {
                return settingWidget.charTyped(c, n);
            }
        }.fillWidth());
    }

    public ColorPickerScreen addLabelRow(String string) {
        return this.addChild(new Component().height(14.0f).fillWidth().layout(Layout.ROW).alignment(Alignment.CENTER).padding(Insets.symmetric(0.0f, 9.0f)).add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> Localization.translate(string)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f)).setLeftFadeWidth(0.75f).fill()));
    }

    public ColorPickerScreen addTextRow(String string) {
        return this.addTextRow(string, 7, false);
    }

    public ColorPickerScreen addTextRow(String string, int n, boolean bl) {
        Component component = new Component().height(16.0f).fillWidth().layout(Layout.ROW).alignment(Alignment.CENTER).padding(Insets.symmetric(0.0f, 9.0f)).add(new ScrollingTextComponent(Font.SEMIBOLD.metrics(n), () -> Localization.translate(string)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR).setLeftFadeWidth(0.75f).fill());
        if (bl) {
            component.sticky();
        }
        return this.addChild(component);
    }

    public ColorPickerScreen addSeparator() {
        return this.addChild(new TextComponent().fillWidth().height(4.0f).paint((drawContext, textComponent) -> drawContext.drawRect(textComponent.x() + 6.0f, textComponent.y() + textComponent.h() / 2.0f - 0.25f, textComponent.w() - 12.0f, 0.5f, ColorPalette.BORDER_COLOR)));
    }

    public ColorPickerScreen addBooleanOption(String string, boolean bl) {
        return this.addBooleanOption(string, bl, null);
    }

    public ColorPickerScreen addBooleanOption(String string, boolean bl, BooleanAction booleanAction) {
        boolean[] blArray = new boolean[]{bl};
        return this.addChild(new Component().height(17.0f).fillWidth().gap(5.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER).padding(Insets.symmetric(0.0f, 9.0f)).add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> string).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).setLeftFadeWidth(0.75f).fill()).add(new InteractiveComponent(() -> blArray[0]).setActiveColorProvider(() -> ColorPalette.MUTED_PANEL_COLOR).size(13.0f, 8.0f)).onClick(() -> {
            boolean enabled = blArray[0] = !blArray[0];
            if (booleanAction != null) {
                booleanAction.accept(enabled);
            }
        }).cursor(Cursor.HAND));
    }

    public ColorPickerScreen addActionOption(String string, String string2, ColorPickerScreenAction colorPickerScreenAction) {
        boolean bl = string.equals(Localization.translate("remove"));
        ColorRGBA colorRGBA = bl ? ColorRGBA.RED.mix(ColorRGBA.WHITE, 0.3f) : ColorPalette.PRIMARY_TEXT_COLOR;
        return this.addChild(new Component().height(17.0f).fillWidth().gap(5.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER).padding(Insets.symmetric(0.0f, 9.0f)).add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> string).setColorProvider(scrollingTextComponent -> colorRGBA.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).setLeftFadeWidth(0.75f).fill()).add(new TextComponent().size(8.0f, 8.0f).interactive(false).paint((drawContext, textComponent) -> drawContext.drawIcon(string2, textComponent.x(), textComponent.y(), textComponent.w(), colorRGBA.mulAlpha(0.75f + 0.25f * textComponent.hover())))).onClick(() -> colorPickerScreenAction.run(this)).cursor(Cursor.HAND));
    }

    public ColorPickerScreen addIntegerOption(String string, int n) {
        return this.addIntegerOption(string, n, null);
    }

    public ColorPickerScreen addIntegerOption(String string, int n2, ModuleActionCommand moduleActionCommand) {
        int[] nArray = new int[]{n2};
        KeyBindingControl keyBindingControl = new KeyBindingControl(Font.REGULAR.metrics(7.0f), () -> nArray[0], n -> {
            nArray[0] = n;
            if (moduleActionCommand != null) {
                moduleActionCommand.handleAction(n);
            }
        });
        return this.addChild(((Component)new Component().height(17.0f).fillWidth().gap(5.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER).padding(Insets.symmetric(0.0f, 9.0f)).add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> string).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).setLeftFadeWidth(0.75f).fill()).add(keyBindingControl).onClick(keyBindingControl::handlePointerClick)).cursor(Cursor.HAND));
    }

    public ColorPickerScreen addIntegerSetting(IntegerSetting integerSetting) {
        return this.addIntegerOption(Localization.translate(integerSetting.getName()), integerSetting.getValue(), integerSetting::setValue);
    }

    public ColorPickerScreen addFloatSlider(String string, float f, float f2, float f3, float f4) {
        return this.addFloatSlider(string, f, f2, f3, f4, null);
    }

    public ColorPickerScreen addFloatSlider(String string, float f2, float f3, float f4, float f5, FloatValueListener floatValueListener) {
        float[] fArray = new float[]{f4};
        Component component = new Component().gap(6.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER).fillWidth().add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> string).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).setLeftFadeWidth(0.75f).fill()).add(new ValueFormatter(Font.REGULAR.metrics(7.0f), () -> fArray[0], f -> {
            fArray[0] = f;
            if (floatValueListener != null) {
                floatValueListener.accept(f);
            }
        }, f2, f3).setDisplayValue(() -> moscow.rockstar.util.NumberFormatting.formatDecimal(fArray[0])).setTextColorProvider(valueFormatter -> ColorPalette.ACCENT_COLOR));
        return this.addChild(new Component().layout(Layout.COLUMN).fillWidth().gap(5.0f).padding(Insets.symmetric(0.0f, 9.0f)).add(component).add(new Slider(() -> fArray[0], f -> {
            fArray[0] = f;
            if (floatValueListener != null) {
                floatValueListener.accept(f);
            }
        }, f2, f3).setStepSize(f5).fillWidthNode().setHeight(6.0f).setTrackHeight(3.0f).setThumbRadius(3.0f).setThumbInset(1.5f).setTrackColorProvider(slider -> ColorPalette.MUTED_PANEL_COLOR).setFilledTrackColorProvider(slider -> ColorPalette.ACCENT_COLOR.mulAlpha(1.0f - 0.25f * slider.hover())).setAnimationMotion(Motion.resolveMotionMotionFromLongAndEasing(300L, Easing.easeOutOvershoot))));
    }

    public ColorPickerScreen setCloseCallback(Runnable runnable) {
        this.closeCallback = runnable;
        return this;
    }

    public ColorPickerScreen setDrawBackground(boolean bl) {
        this.drawBackground = bl;
        return this;
    }

    public ColorPickerScreen useLiquidGlass() {
        this.liquidGlassStyle = true;
        return this;
    }

    public ColorPickerScreen setVerticalOffset(float f) {
        this.verticalOffset = Math.max(0.0f, f);
        return this;
    }

    public ColorPickerScreen setHeightAndConstrain(float f) {
        this.height = f;
        this.constrainToScreen = true;
        return this;
    }

    public void setOpen(boolean bl) {
        this.open = bl;
        if (!bl && !this.closeCallbackInvoked) {
            this.closeCallback.run();
            this.closeCallbackInvoked = true;
        }
    }

    @Override
    public void mouseClicked(double d, double d2, PointerAction pointerAction) {
        for (int i = transientNodes.size() - 1; i >= 0; --i) {
            if (!transientNodes.get(i).mouseClicked((float)d, (float)d2, pointerAction)) continue;
            return;
        }
        this.rootComponent.mouseClicked((float)d, (float)d2, pointerAction);
        super.mouseClicked(d, d2, pointerAction);
    }

    @Override
    public void mouseReleased(double d, double d2, PointerAction pointerAction) {
        for (UiNode uiNode : transientNodes) {
            uiNode.mouseReleased((float)d, (float)d2, pointerAction);
        }
        this.rootComponent.mouseReleased((float)d, (float)d2, pointerAction);
        super.mouseReleased(d, d2, pointerAction);
    }

    @Override
    public void mouseScrolled(double d, double d2, double d3, double d4) {
        for (int i = transientNodes.size() - 1; i >= 0; --i) {
            if (!transientNodes.get(i).mouseScrolled((float)d, (float)d2, (float)d3, (float)d4)) continue;
            return;
        }
        this.rootComponent.mouseScrolled((float)d, (float)d2, (float)d3, (float)d4);
        super.mouseScrolled(d, d2, d3, d4);
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        if (this.dispatchKeyPressed(n, n2, n3)) {
            return;
        }
        super.keyPressed(n, n2, n3);
    }

    public boolean dispatchKeyPressed(int n, int n2, int n3) {
        for (int i = transientNodes.size() - 1; i >= 0; --i) {
            if (!transientNodes.get(i).keyPressed(n, n2, n3)) continue;
            return true;
        }
        return this.rootComponent.keyPressed(n, n2, n3);
    }

    @Override
    public boolean charTyped(char c, int n) {
        for (int i = transientNodes.size() - 1; i >= 0; --i) {
            if (!transientNodes.get(i).charTyped(c, n)) continue;
            return true;
        }
        if (this.rootComponent.charTyped(c, n)) {
            return true;
        }
        return super.charTyped(c, n);
    }

    @Generated
    public Animation getOpenAnimation() {
        return this.openAnimation;
    }

    @Generated
    public boolean isOpen() {
        return this.open;
    }
}
