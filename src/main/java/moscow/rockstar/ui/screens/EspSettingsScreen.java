/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Entity
 *  net.minecraft.EntityType
 *  net.minecraft.EquipmentSlot
 *  net.minecraft.LivingEntity
 *  net.minecraft.PigEntity
 *  net.minecraft.ItemEntity
 *  net.minecraft.ZombieEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ItemConvertible
 *  net.minecraft.World
 *  net.minecraft.Screen
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.VertexConsumerProvider$Immediate
 *  net.minecraft.OtherClientPlayerEntity
 *  net.minecraft.EntityRenderer
 *  net.minecraft.EntityRenderDispatcher
 *  org.joml.Quaternionf
 *  org.lwjgl.opengl.GL11
 */
package moscow.rockstar.ui.screens;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.esp.entities.Glow;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.modules.visuals.esp.targeting.ItemTargetType;
import moscow.rockstar.modules.visuals.esp.targeting.PlayerTargetGroup;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.network.session.BotPacketListener;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.esp.FriendMarkerRenderer;
import moscow.rockstar.render.esp.OverlayRegistry;
import moscow.rockstar.render.esp.TargetRenderModule;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.target.RenderTarget;
import moscow.rockstar.render.target.RenderTargetState;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.color.ColorPickerHost;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.InteractiveComponent;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingSnapshotCache;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.ui.widgets.settings.KeyBindingControl;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemConvertible;
import net.minecraft.world.World;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

public class EspSettingsScreen
extends ColorPickerHost
implements ClientAccess,
WindowMetricsProvider,
WindowHandle {
    private static final float PREVIEW_PANEL_WIDTH = 447.0f;
    private static final float PREVIEW_PANEL_HALF_WIDTH = 223.0f;
    private static final float TARGET_SELECTOR_HEIGHT = 165.0f;
    private static final float TARGET_OPTION_HEIGHT = 13.0f;
    private static final int DEFAULT_LIGHT_LEVEL = 7;
    private static final int FULL_BRIGHT_LIGHT = 0xF000F0;
    private static final float SETTINGS_PANEL_WIDTH = 288.0f;
    private static final float SETTINGS_COLUMN_WIDTH = 142.0f;
    private final RenderTarget previewRenderTarget = new RenderTarget(true).enableLinearFiltering().setResolutionScale(1.0f);
    private final RenderTargetState previewGlowState = RenderTargetState.create();
    private TargetGroup selectedTargetGroup = TargetGroup.PLAYERS;
    private TargetGroup defaultTargetGroup = TargetGroup.PLAYERS;
    private PlayerTargetGroup selectedPlayerTargetGroup = PlayerTargetGroup.OTHERS;
    private ItemTargetType selectedItemTargetType = ItemTargetType.DROPPED;
    private int mouseX;
    private int mouseY;
    private boolean inputActive;
    private ZombieEntity mobPreviewEntity;
    private ItemEntity itemPreviewEntity;
    private Entity animalPreviewEntity;
    private LivingEntity playerPreviewEntity;
    private boolean transitionForward = true;
    private Entity previousPreviewEntity = null;
    private Component settingsPanel;
    private Component playerSettingsColumn;
    private Component itemSettingsColumn;
    private IntegerSetting selectedIntegerSetting;
    private static float previewYaw = 45.0f;
    private static float previewPitch = 0.0f;
    private static float previewScale = 50.0f;
    private static float previewOffset = 0.0f;
    private float previewYawAngle = 45.0f;
    private float previewPitchAngle = 0.0f;
    private float itemPreviewRotation = 0.0f;
    private boolean draggingPreview = false;
    private int dragStartX;
    private int dragStartY;
    private float previewPanelX;
    private float previewPanelY;
    private float previewPanelWidth;
    private float previewPanelHeight;
    private final Animation previewTransitionAnimation = new Animation(400L, Easing.easeOutBack);
    private final Animation entitySlideAnimation = new Animation(400L, 1.0f, Easing.easeInOutCubicPolynomial);
    private final Animation selectionHighlightAnimation = new Animation(400L, Easing.easeOutBack);
    private final Animation previewHeightAnimation = new Animation(400L, 200.0f, Easing.easeOutBack);
    private final Animation previewScaleAnimation = new Animation(400L, 50.0f, Easing.easeOutBack);
    private final Animation contentSlideAnimation = new Animation(300L, Easing.easeOutBack);
    private static final float ANIMATION_DURATION_MILLIS = 300.0f;
    private float hideProgress;
    private long lastFrameTimeMillis;

    @Override
    @Compile(obfuscation=4)
    protected void initializeScreen() {
        super.initializeScreen();
        this.clearRoots();
        RenderTargetState.initialize();
        this.playerSettingsColumn = new Component().vertical().gap(4.0f).width(142.0f);
        this.itemSettingsColumn = new Component().vertical().gap(4.0f).width(142.0f);
        this.settingsPanel = new Component().vertical().width(288.0f).scrollable().configureLayoutState(scrollBar2 -> scrollBar2.offset(-5.0f).padding(1.0f, 6.0f).thickness(2.5f).thumbColor(scrollBar -> ColorRGBA.BLACK.mix(ColorRGBA.WHITE, 0.3f).withAlpha(255.0f * (0.32f + 0.28f * scrollBar.hoverProgress() + 0.3f * scrollBar.dragProgress())))).add(new Component().horizontal().gap(4.0f).fillWidth().add(this.playerSettingsColumn).add(this.itemSettingsColumn));
        this.settingsPanel.snapSize();
        this.add(this.settingsPanel);
        this.rebuildOverlaySettings();
    }

    private void rebuildOverlaySettings() {
        List<TargetRenderModule> list = this.getVisibleOverlays();
        ArrayList<Component> arrayList = new ArrayList<Component>();
        ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < list.size(); ++i) {
            (i % 2 == 0 ? arrayList : arrayList2).add(this.createOverlaySettingsComponent(list.get(i)));
        }
        this.playerSettingsColumn.updateChildren(arrayList);
        this.itemSettingsColumn.updateChildren(arrayList2);
        this.settingsPanel.resetScroll();
    }

    private Component createOverlaySettingsComponent(TargetRenderModule overlay) {
        BooleanSetting booleanSetting = this.getOverlayBooleanSetting(overlay);
        IntegerSetting integerSetting = this.getOverlayIntegerSetting(overlay);
        TextComponent textComponent2 = new TextComponent().text(Font.REGULAR.metrics(6.0f), () -> integerSetting == null ? "" : moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(integerSetting.getValue()), textComponent -> (integerSetting != null && this.selectedIntegerSetting == integerSetting ? ColorPalette.ACCENT_COLOR : ColorPalette.PRIMARY_TEXT_COLOR).mulAlpha(0.75f)).textInset(3.0f).height(10.0f).radius(2.5f).background(textComponent -> ColorPalette.MUTED_PANEL_COLOR).visibleWhen(() -> integerSetting != null && integerSetting.getValue() != -1).interactive(false);
        Component component2 = new Component().height(18.0f).gap(5.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER).padding(Insets.symmetric(0.0f, 9.0f)).fillWidth().cursor(Cursor.HAND).add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> integerSetting != null && this.selectedIntegerSetting == integerSetting ? (moscow.rockstar.ui.input.KeyBindingUtil.currentModifiers() != 0 ? moscow.rockstar.ui.input.KeyBindingUtil.modifierPrefix(moscow.rockstar.ui.input.KeyBindingUtil.currentModifiers()) + "..." : Localization.translate("menu.binding")) : (booleanSetting == null ? overlay.getName() : Localization.translate(booleanSetting.getName()))).setColorProvider(scrollingTextComponent -> (integerSetting != null && this.selectedIntegerSetting == integerSetting ? ColorPalette.ACCENT_COLOR : ColorPalette.PRIMARY_TEXT_COLOR).mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).setLeftFadeWidth(0.75f).fill()).add(textComponent2).add(new InteractiveComponent(() -> booleanSetting != null && booleanSetting.isEnabled()).setActiveColorProvider(() -> ColorPalette.MUTED_PANEL_COLOR).size(13.0f, 8.0f).minSize(13.0f, 8.0f).snapSize().transition(Transition.NO_OP)).onClick((pointerAction, f, f2) -> {
            if (pointerAction == PointerAction.LEFT_CLICK) {
                if (booleanSetting != null) {
                    booleanSetting.toggle();
                }
                if (this.selectedIntegerSetting == integerSetting) {
                    this.selectedIntegerSetting = null;
                }
            } else if (integerSetting != null) {
                this.selectedIntegerSetting = this.selectedIntegerSetting == integerSetting ? null : integerSetting;
            }
        });
        Component component3 = new Component().vertical().fillWidth();
        for (Setting setting : this.getOverlaySettings(overlay)) {
            component3.add(EspSettingsScreen.createSettingComponent(setting));
        }
        return new Component().vertical().fillWidth().padding(Insets.of(3.0f, 0.0f, 3.0f, 0.0f)).hoverMotion(Motion.withLinearEasing(60L)).renderHook((drawContext, component) -> {
            drawContext.drawRoundedRect(component.x(), component.y(), component.w(), component.h(), WidgetState.uniform(5.0f), ColorPalette.getPanelBackgroundColor().mulAlpha(0.3f));
            if (overlay instanceof FriendMarkerRenderer) {
                FriendMarkerRenderer.setFriendModelRendering(component.hover() > 0.05f);
            }
        }).add(component2).add(component3);
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiiIiiIIi#I (Lrockstar/ilIlil/IIiiiIIII;)Lrockstar/ilIlil/iii;.
     * Every setting row goes through the shared wrapper, which collapses the row out of the
     * flow and drives it from the setting's own visibility predicate.
     */
    private static Component createSettingComponent(Setting setting) {
        return EspSettingsScreen.createRawSettingComponent(setting).collapse().visibleWhen(setting::hasValidSettingValue, Easing.easeOutQuart, 220L);
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiiIiiIIi#i (Lrockstar/ilIlil/IIiiiIIII;)Lrockstar/ilIlil/iii;.
     * The setting's own component is stretched to the column width and inset by 9px
     * horizontally; without this the row measures at its natural width and its
     * label/value text runs past the card edge.
     */
    private static Component createRawSettingComponent(Setting setting) {
        Component component = setting.buildComponent();
        if (component == null) {
            // ORIGINAL falls back to `new IiiIiiIII(setting)`, an adapter around the
            // legacy SettingWidget-based renderer. Every Setting here overrides
            // buildComponent(), so this branch is unreachable in practice.
            return new Component();
        }
        return component.fillWidth().padding(0.0f, 9.0f);
    }

    public void tick() {
        InventoryMove.resetInputState();
        if (!this.draggingPreview && this.previewPitchAngle != 0.0f) {
            this.previewPitchAngle *= 0.85f;
            if (Math.abs(this.previewPitchAngle) < 0.5f) {
                this.previewPitchAngle = 0.0f;
            }
        }
        this.itemPreviewRotation += 3.0f;
        if (this.itemPreviewRotation >= 360.0f) {
            this.itemPreviewRotation -= 360.0f;
        }
        super.tick();
    }

    @Override
    public void onMouseClicked(double d, double d2, PointerAction pointerAction) {
        if (this.selectedIntegerSetting != null && pointerAction != PointerAction.LEFT_CLICK) {
            // ORIGINAL rockstar/ilIlil/IiiiiIIi#onMouseClicked emits
            //   PointerAction.getButtonCode() -> KeyBindingUtil.withCurrentModifiers(int)
            // encode(button, 0) hard-zeroes the modifier nibble, so a modified mouse
            // rebind (e.g. Ctrl+RMB) silently lost its modifiers.
            this.selectedIntegerSetting.setValue(moscow.rockstar.ui.input.KeyBindingUtil.withCurrentModifiers(pointerAction.getButtonCode()));
            this.selectedIntegerSetting = null;
            return;
        }
        if (pointerAction == PointerAction.LEFT_CLICK) {
            this.inputActive = true;
            if (this.isInsidePreviewPanel((float)d, (float)d2)) {
                this.draggingPreview = true;
                this.dragStartX = (int)d;
                this.dragStartY = (int)d2;
            }
        }
        super.onMouseClicked(d, d2, pointerAction);
    }

    @Override
    public void onMouseReleased(double d, double d2, PointerAction pointerAction) {
        if (pointerAction == PointerAction.LEFT_CLICK) {
            this.draggingPreview = false;
        }
        super.onMouseReleased(d, d2, pointerAction);
    }

    private void updatePreviewRotation(int n, int n2) {
        if (this.draggingPreview) {
            float f = n - this.dragStartX;
            float f2 = n2 - this.dragStartY;
            this.previewYawAngle -= f * 1.5f;
            this.previewPitchAngle -= f2 * 0.3f;
            this.previewPitchAngle = Math.max(-60.0f, Math.min(60.0f, this.previewPitchAngle));
            this.dragStartX = n;
            this.dragStartY = n2;
        }
    }

    private boolean isInsidePreviewPanel(float f, float f2) {
        return f >= this.previewPanelX && f <= this.previewPanelX + this.previewPanelWidth && f2 >= this.previewPanelY && f2 <= this.previewPanelY + this.previewPanelHeight;
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (this.selectedIntegerSetting == null && !KeyBindingControl.isControlActive()) {
            if (Screen.hasControlDown() && n == 90 && SettingSnapshotCache.isCollectionCacheReady()) {
                return true;
            }
            if (Screen.hasControlDown() && n == 89 && SettingSnapshotCache.isCollectionProcessorCacheTargetReady()) {
                return true;
            }
        }
        if (this.isMouseInputValid(n, n3)) {
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public boolean keyReleased(int n, int n2, int n3) {
        int n4;
        if (this.selectedIntegerSetting != null && (n4 = moscow.rockstar.ui.input.KeyBindingUtil.encodeKeyRelease(n, n3)) != Integer.MIN_VALUE) {
            this.selectedIntegerSetting.setValue(n4);
            this.selectedIntegerSetting = null;
            return true;
        }
        return super.keyReleased(n, n2, n3);
    }

    private boolean isMouseInputValid(int n, int n2) {
        if (this.selectedIntegerSetting == null) {
            return false;
        }
        if (n == 256 || n == 261) {
            this.selectedIntegerSetting.setValue(-1);
        } else {
            int n3 = moscow.rockstar.ui.input.KeyBindingUtil.encodeKeyPress(n, n2);
            if (n3 == Integer.MIN_VALUE) {
                return true;
            }
            this.selectedIntegerSetting.setValue(n3);
        }
        this.selectedIntegerSetting = null;
        return true;
    }

    private void updateContentVisibility() {
        long l = System.currentTimeMillis();
        float f = this.lastFrameTimeMillis == 0L ? 16.0f : Math.min(64.0f, (float)(l - this.lastFrameTimeMillis));
        this.lastFrameTimeMillis = l;
        Menu menu = RockstarClient.create().getModuleRegistry().getModule(Menu.class);
        int n = menu == null || menu.getHideKeySetting() == null ? -1 : menu.getHideKeySetting().getValue();
        float f2 = EspSettingsScreen.isValidKeyBinding(n) ? 1.0f : 0.0f;
        float f3 = f / 300.0f;
        if (this.hideProgress < f2) {
            this.hideProgress = Math.min(f2, this.hideProgress + f3);
        } else if (this.hideProgress > f2) {
            this.hideProgress = Math.max(f2, this.hideProgress - f3);
        }
        this.contentAlpha = 1.0f - Easing.easeInOutCubicBezier.ease(this.hideProgress, 0.0f, 1.0f, 1.0f);
    }

    private static boolean isValidKeyBinding(int n) {
        return moscow.rockstar.ui.input.KeyBindingUtil.isPressed(n);
    }

    @Override
    public void render(RockstarDrawContext drawContext) {
        float f;
        boolean bl;
        this.updateContentVisibility();
        this.mouseX = drawContext.mouseX();
        this.mouseY = drawContext.mouseY();
        this.updatePreviewRotation(this.mouseX, this.mouseY);
        float f2 = INSTANCE.width() / 2.0f - 223.5f;
        float f3 = INSTANCE.height() / 2.0f - 111.5f;
        boolean bl2 = bl = this.contentAlpha > 0.01f;
        if (bl) {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)this.contentAlpha);
        }
        if (bl) {
            this.drawPreviewPanel(drawContext, f2, f3, 302.0f, 223.0f);
        }
        FriendMarkerRenderer friendMarkerRenderer = OverlayRegistry.getInstance().findOverlayByType(FriendMarkerRenderer.class);
        boolean bl3 = this.selectedTargetGroup == TargetGroup.PLAYERS && this.selectedPlayerTargetGroup == PlayerTargetGroup.FRIENDS && friendMarkerRenderer != null && friendMarkerRenderer.shouldRenderHeadMarker() && FriendMarkerRenderer.isFriendModelRendering();
        this.previewTransitionAnimation.setReverse(bl3);
        float f4 = 1.8f;
        Entity class_12972 = this.getSelectedPreviewEntity();
        float f5 = class_12972 != null ? class_12972.getHeight() : f4;
        float f6 = f5 / f4;
        float f7 = 200.0f;
        float f8 = f = bl3 ? 223.0f : f7 * f6;
        if (this.selectedTargetGroup == TargetGroup.ANIMALS) {
            f += 50.0f;
        }
        if (this.selectedTargetGroup == TargetGroup.ITEMS) {
            f += 100.0f;
        }
        this.selectionHighlightAnimation.update(bl3 ? 1.4f : (this.selectedTargetGroup == TargetGroup.ITEMS ? 0.3f : 0.0f));
        this.previewHeightAnimation.update(f);
        float f9 = 132.0f;
        float f10 = f9 + 20.0f;
        float f11 = f9 + (f10 - f9) * this.previewTransitionAnimation.getValue();
        float f12 = this.previewHeightAnimation.getValue();
        float f13 = this.selectedTargetGroup == TargetGroup.ITEMS ? 70.0f : 50.0f;
        this.previewScaleAnimation.update(f13);
        int n = (int)this.previewScaleAnimation.getValue();
        if (bl) {
            this.drawPreviewPanel(drawContext, f2 + 245.0f + 65.0f, f3, f11, f12);
        }
        this.previewPanelX = f2 + 245.0f + 65.0f;
        this.previewPanelY = f3;
        this.previewPanelWidth = f11;
        this.previewPanelHeight = f12;
        float f14 = this.selectionHighlightAnimation.getValue();
        if (bl) {
            moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)this.previewPanelX, (float)this.previewPanelY, (float)f11, (float)f12);
            this.renderPreviewTransition(drawContext, (int)this.previewPanelX, (int)this.previewPanelY, (int)f11, (int)f12, n, f14);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)this.contentAlpha);
            this.renderOverlayElements(drawContext, (int)this.previewPanelX, (int)this.previewPanelY, (int)f11, (int)f12);
            moscow.rockstar.render.state.UiScissorStack.pop();
            this.drawTargetSelectors(drawContext, f2, f3);
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.contentSlideAnimation.update(this.selectedTargetGroup.isEnabledAtStartup() ? 1.0f : 0.0f);
        float f15 = f3 + 24.0f + 16.0f * this.contentSlideAnimation.getValue();
        float f16 = 185.0f - 10.0f * this.contentSlideAnimation.getValue();
        this.settingsPanel.snapAt(f2 + 7.0f, f15);
        this.settingsPanel.height(f16);
        super.render(drawContext);
        this.inputActive = false;
    }

    private boolean isMouseInsideRectangle(float f, float f2, float f3, float f4) {
        return (float)this.mouseX >= f && (float)this.mouseX <= f + f3 && (float)this.mouseY >= f2 && (float)this.mouseY <= f2 + f4;
    }

    private void drawTargetSelectors(RockstarDrawContext drawContext, float f, float f2) {
        block4: {
            float f3;
            float f4;
            block3: {
                f4 = f + 7.0f;
                f3 = f2 + 7.0f;
                for (TargetGroup enum_ : TargetGroup.values()) {
                    f4 = this.drawTargetOption(drawContext, f4, f3, enum_.getTranslationKey(), this.selectedTargetGroup == enum_, () -> {
                        if (this.selectedTargetGroup == enum_) {
                            return;
                        }
                        this.previousPreviewEntity = this.getSelectedPreviewEntity();
                        this.transitionForward = enum_.ordinal() > this.selectedTargetGroup.ordinal();
                        this.entitySlideAnimation.setValue(0.0f);
                        this.selectedIntegerSetting = null;
                        this.selectedTargetGroup = enum_;
                        if (enum_ == TargetGroup.PLAYERS) {
                            this.selectedPlayerTargetGroup = PlayerTargetGroup.OTHERS;
                        }
                        if (enum_ == TargetGroup.ITEMS) {
                            this.selectedItemTargetType = ItemTargetType.DROPPED;
                        }
                        this.rebuildOverlaySettings();
                    });
                }
                if (this.selectedTargetGroup != TargetGroup.PLAYERS) break block3;
                f4 = f + 7.0f;
                f3 = f2 + 24.0f;
                for (Enum enum_ : PlayerTargetGroup.values()) {
                    f4 = this.drawTargetOption(drawContext, f4, f3, ((PlayerTargetGroup)enum_).getTranslationKey(), this.selectedPlayerTargetGroup == enum_, () -> this.selectPlayerTargetGroup((PlayerTargetGroup)enum_));
                }
                break block4;
            }
            if (this.selectedTargetGroup != TargetGroup.ITEMS) break block4;
            f4 = f + 7.0f;
            f3 = f2 + 24.0f;
            for (Enum enum_ : ItemTargetType.values()) {
                f4 = this.drawTargetOption(drawContext, f4, f3, ((ItemTargetType)enum_).getTranslationKey(), this.selectedItemTargetType == enum_, () -> this.selectItemTargetType((ItemTargetType)enum_));
            }
        }
    }

    private float drawTargetOption(RockstarDrawContext drawContext, float f, float f2, String string, boolean bl, Runnable runnable) {
        String string2 = Localization.translate(string);
        float f3 = Font.REGULAR.measure(string2, 7.0f);
        float f4 = f3 + 8.0f;
        boolean bl2 = this.isMouseInsideRectangle(f, f2, f4, 13.0f);
        float f5 = bl ? 1.0f : (bl2 ? 0.8f : 0.4f);
        drawContext.drawRoundedRect(f, f2, f4, 13.0f, WidgetState.uniform(3.0f), ColorPalette.getPanelBackgroundColor().mulAlpha(f5));
        drawContext.drawText(Font.REGULAR.metrics(7.0f), string2, f + 4.0f, f2 + 4.0f, bl ? ColorPalette.getPrimaryTextColor() : ColorPalette.getPrimaryTextColor().mulAlpha(0.75f));
        if (bl2 && this.inputActive) {
            runnable.run();
        }
        return f + f4 + 4.0f;
    }

    private void selectPlayerTargetGroup(PlayerTargetGroup playerTargetGroup) {
        if (this.selectedPlayerTargetGroup == playerTargetGroup) {
            return;
        }
        this.selectedIntegerSetting = null;
        this.selectedPlayerTargetGroup = playerTargetGroup;
        this.rebuildOverlaySettings();
    }

    private void selectItemTargetType(ItemTargetType itemTargetType) {
        if (this.selectedItemTargetType == itemTargetType) {
            return;
        }
        this.selectedIntegerSetting = null;
        this.selectedItemTargetType = itemTargetType;
        this.rebuildOverlaySettings();
    }

    private List<TargetRenderModule> getVisibleOverlays() {
        ArrayList<TargetRenderModule> arrayList = new ArrayList<TargetRenderModule>();
        for (TargetRenderModule overlay : OverlayRegistry.getInstance().getRegisteredOverlays()) {
            if (!overlay.supportsTargetGroup(this.selectedTargetGroup)) continue;
            if (this.selectedTargetGroup == TargetGroup.PLAYERS && !overlay.supportsTargetGroup(this.selectedPlayerTargetGroup)) continue;
            if (this.selectedTargetGroup == TargetGroup.ITEMS && overlay.getSupportedItemTypes().length > 0 && !overlay.supportsItemType(this.selectedItemTargetType)) continue;
            arrayList.add(overlay);
        }
        return arrayList;
    }

    private List<Setting> getOverlaySettings(TargetRenderModule overlay) {
        ArrayList<Setting> arrayList = new ArrayList<Setting>();
        if (this.selectedTargetGroup == TargetGroup.PLAYERS) {
            arrayList.addAll(overlay.getSettingsForScope(this.selectedPlayerTargetGroup));
        } else if (this.selectedTargetGroup == TargetGroup.ITEMS && overlay.getSupportedItemTypes().length > 0) {
            arrayList.addAll(overlay.getSettingsForScope(this.selectedItemTargetType));
        } else {
            arrayList.addAll(overlay.getSettingsForScope(this.selectedTargetGroup));
        }
        return arrayList;
    }

    private BooleanSetting getOverlayBooleanSetting(TargetRenderModule overlay) {
        if (this.selectedTargetGroup == TargetGroup.PLAYERS) {
            return overlay.getEnabledSetting(this.selectedPlayerTargetGroup);
        }
        if (this.selectedTargetGroup == TargetGroup.ITEMS && overlay.getSupportedItemTypes().length > 0) {
            return overlay.getEnabledSetting(this.selectedItemTargetType);
        }
        return overlay.getEnabledSetting(this.selectedTargetGroup);
    }

    private IntegerSetting getOverlayIntegerSetting(TargetRenderModule overlay) {
        if (this.selectedTargetGroup == TargetGroup.PLAYERS) {
            return overlay.getBindingSetting(this.selectedPlayerTargetGroup);
        }
        if (this.selectedTargetGroup == TargetGroup.ITEMS
                && overlay.getSupportedItemTypes().length > 0) {
            return overlay.getBindingSetting(this.selectedItemTargetType);
        }
        return overlay.getBindingSetting(this.selectedTargetGroup);
    }

    private void renderPreviewTransition(RockstarDrawContext drawContext, int n, int n2, int n3, int n4, int n5, float f) {
        Entity class_12972;
        this.entitySlideAnimation.update(1.0f);
        float f2 = this.entitySlideAnimation.getValue();
        float f3 = (float)n3 * 1.5f;
        if (f2 < 1.0f && this.previousPreviewEntity != null) {
            float f4 = this.transitionForward ? -f3 * f2 : f3 * f2;
            this.renderPreviewEntity(drawContext, n, n2, n3, n4, n5, f, this.previousPreviewEntity, f4);
        }
        if ((class_12972 = this.getSelectedPreviewEntity()) == null) {
            return;
        }
        float f5 = 0.0f;
        if (f2 < 1.0f) {
            f5 = this.transitionForward ? f3 * (1.0f - f2) : -f3 * (1.0f - f2);
        }
        this.renderPreviewEntity(drawContext, n, n2, n3, n4, n5, f, class_12972, f5);
    }

    private void renderPreviewEntity(RockstarDrawContext drawContext, int n, int n2, int n3, int n4, int n5, float f, Entity class_12972, float f2) {
        Glow glowRenderer = OverlayRegistry.getInstance().findOverlayByType(Glow.class);
        boolean bl = glowRenderer == null ? false : (this.selectedTargetGroup == TargetGroup.PLAYERS ? glowRenderer.isValid2(this.selectedPlayerTargetGroup) : (this.selectedTargetGroup == TargetGroup.ITEMS ? glowRenderer.isValid2(this.selectedItemTargetType) : glowRenderer.isValid2(this.selectedTargetGroup)));
        float f3 = (float)n + (float)n3 / 2.0f + f2;
        float f4 = (float)n2 + (float)n4 / 2.0f;
        float f5 = n5;
        Quaternionf quaternionf = new Quaternionf().rotateZ((float)Math.PI);
        quaternionf.rotateX((float)Math.toRadians(this.previewPitchAngle));
        previewYaw = this.previewYawAngle;
        previewPitch = this.previewPitchAngle;
        previewScale = f5;
        previewOffset = f;
        if (class_12972 instanceof LivingEntity) {
            boolean bl2;
            LivingEntity class_13092 = (LivingEntity)class_12972;
            float[] fArray = this.capturePlayerModelProperties(class_13092);
            float f6 = 180.0f + this.previewYawAngle;
            this.applyPreviewModelScale(class_13092, f6);
            if (this.selectedTargetGroup == TargetGroup.PLAYERS) {
                Glow.setSelectedPlayerGroup(this.selectedPlayerTargetGroup);
            }
            if (this.selectedTargetGroup == TargetGroup.ITEMS) {
                Glow.setSelectedItemGroup(this.selectedItemTargetType);
            }
            Glow.setSelectedEntityType(this.selectedTargetGroup);
            FriendMarkerRenderer friendMarkerRenderer = OverlayRegistry.getInstance().findOverlayByType(FriendMarkerRenderer.class);
            boolean bl3 = bl2 = this.selectedTargetGroup == TargetGroup.PLAYERS && this.selectedPlayerTargetGroup == PlayerTargetGroup.FRIENDS && friendMarkerRenderer != null && friendMarkerRenderer.shouldRenderHeadMarker() && FriendMarkerRenderer.isFriendModelRendering();
            if (bl2) {
                FriendMarkerRenderer.setFriendMarkersRendering(true);
            }
            this.drawEntityModel(drawContext.getMatrices(), f3, f4, f5, quaternionf, f, 1.0f, 0.0f, class_12972);
            if (bl2) {
                FriendMarkerRenderer.setFriendMarkersRendering(false);
            }
            Glow.setSelectedPlayerGroup(null);
            Glow.setSelectedItemGroup(null);
            Glow.setSelectedEntityType(null);
            if (bl && f2 == 0.0f) {
                this.renderPreviewEntityModel(drawContext, f3, f4, f5, quaternionf, f, class_12972);
            }
            this.restorePlayerModelProperties(class_13092, fArray);
        } else {
            Quaternionf quaternionf2 = new Quaternionf().rotateZ((float)Math.PI);
            quaternionf2.rotateY((float)Math.toRadians(this.itemPreviewRotation));
            this.drawEntityModel(drawContext.getMatrices(), f3, f4, f5, quaternionf2, f, 1.0f, 0.0f, class_12972);
        }
    }

    private float[] capturePlayerModelProperties(LivingEntity class_13092) {
        return new float[]{class_13092.bodyYaw, class_13092.prevBodyYaw, class_13092.getYaw(), class_13092.prevYaw, class_13092.getPitch(), class_13092.prevPitch, class_13092.prevHeadYaw, class_13092.headYaw};
    }

    private void applyPreviewModelScale(LivingEntity class_13092, float f) {
        class_13092.bodyYaw = class_13092.prevBodyYaw = f;
        class_13092.setYaw(f);
        class_13092.prevYaw = f;
        class_13092.setPitch(0.0f);
        class_13092.prevPitch = 0.0f;
        class_13092.headYaw = class_13092.prevHeadYaw = f;
    }

    private void restorePlayerModelProperties(LivingEntity class_13092, float[] fArray) {
        class_13092.bodyYaw = fArray[0];
        class_13092.prevBodyYaw = fArray[1];
        class_13092.setYaw(fArray[2]);
        class_13092.prevYaw = fArray[3];
        class_13092.setPitch(fArray[4]);
        class_13092.prevPitch = fArray[5];
        class_13092.prevHeadYaw = fArray[6];
        class_13092.headYaw = fArray[7];
    }

    private void renderPreviewEntityModel(RockstarDrawContext drawContext, float f, float f2, float f3, Quaternionf quaternionf, float f4, Entity class_12972) {
        int n;
        boolean bl = GL11.glIsEnabled((int)3089);
        if (bl) {
            GL11.glDisable((int)3089);
        }
        this.previewRenderTarget.beginPass(true);
        if (bl) {
            GL11.glEnable((int)3089);
        }
        Glow.entityGlowRendering = true;
        Glow.currentEntity = class_12972;
        if (this.selectedTargetGroup == TargetGroup.PLAYERS) {
            Glow.setSelectedPlayerGroup(this.selectedPlayerTargetGroup);
        }
        if (this.selectedTargetGroup == TargetGroup.ITEMS) {
            Glow.setSelectedItemGroup(this.selectedItemTargetType);
        }
        Glow.setSelectedEntityType(this.selectedTargetGroup);
        this.drawEntityModel(drawContext.getMatrices(), f, f2, f3, quaternionf, f4, 1.0f, 0.0f, class_12972);
        Glow.setSelectedPlayerGroup(null);
        Glow.setSelectedItemGroup(null);
        Glow.setSelectedEntityType(null);
        Glow.currentEntity = null;
        Glow.entityGlowRendering = false;
        this.previewRenderTarget.endPass();
        Glow glowRenderer = OverlayRegistry.getInstance().findOverlayByType(Glow.class);
        this.previewGlowState.setSamplesPerPass((int)glowRenderer.getEntityGlowStrength().getValue());
        this.previewGlowState.setCompositeOffset(7.0f);
        this.previewGlowState.setOutlineRadius(1.0f);
        this.previewGlowState.setOutlineStrength(1.0f);
        if (bl) {
            GL11.glDisable((int)3089);
        }
        this.previewGlowState.apply(this.previewRenderTarget);
        if (bl) {
            GL11.glEnable((int)3089);
        }
        if ((n = this.previewGlowState.getTextureId()) == 0) {
            return;
        }
        RenderSystem.setShaderTexture((int)0, (int)n);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)this.contentAlpha);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
        ShaderRenderer.renderQuadWithBounds(0.0f, -0.5f, WINDOW.getScaledWidth(), WINDOW.getScaledHeight());
        ShaderRenderer.renderQuadWithBounds(0.0f, -0.5f, WINDOW.getScaledWidth(), WINDOW.getScaledHeight());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
    }

    private void renderOverlayElements(RockstarDrawContext drawContext, int n, int n2, int n3, int n4) {
        Entity class_12972 = this.getSelectedPreviewEntity();
        if (class_12972 == null) {
            return;
        }
        float f = (float)n + (float)n3 / 2.0f;
        float f2 = (float)n2 + (float)n4 / 2.0f - 35.0f;
        for (TargetRenderModule overlay : OverlayRegistry.getInstance().getRegisteredOverlays()) {
            boolean bl;
            // ORIGINAL: rockstar/ilIlil/IiiiiIIi#I(Lrockstar/ilIlil/III;IIII)V - the outer filter is
            // IiiiIiiI#I(TargetGroup)Z (scope support, Set.contains) but the INNER predicate is
            // IiiiIiiI#i(...)Z, i.e. isValid2: "ESP module enabled && this scope's activation
            // BooleanSetting is enabled". Using supports* here made every preview overlay draw
            // unconditionally, so EntityBoxRenderer#renderPreviewOverlay (bloom quads + box)
            // rendered in the ESP screen preview even with "esp.boxes" switched off.
            if (!overlay.supportsTargetGroup(this.selectedTargetGroup) || !(bl = this.selectedTargetGroup == TargetGroup.PLAYERS ? overlay.isValid2(this.selectedPlayerTargetGroup) : (this.selectedTargetGroup == TargetGroup.ITEMS && overlay.getSupportedItemTypes().length > 0 ? overlay.isValid2(this.selectedItemTargetType) : overlay.isValid2(this.selectedTargetGroup)))) continue;
            overlay.renderPreviewOverlay(drawContext, class_12972, f, f2, this.selectedTargetGroup, this.selectedPlayerTargetGroup);
        }
    }

    private Entity getSelectedPreviewEntity() {
        if (EspSettingsScreen.minecraftClient.world == null) {
            return null;
        }
        return switch (this.selectedTargetGroup) {
            default -> throw new MatchException(null, null);
            case TargetGroup.PLAYERS -> this.getPlayerPreviewEntity();
            case TargetGroup.MOBS -> this.getMobPreviewEntity();
            case TargetGroup.ANIMALS -> this.getAnimalPreviewEntity();
            case TargetGroup.ITEMS -> this.getItemPreviewEntity();
        };
    }

    private LivingEntity getPlayerPreviewEntity() {
        if (this.playerPreviewEntity == null && EspSettingsScreen.minecraftClient.world != null) {
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "Preview");
            OtherClientPlayerEntity SonicBoomParticle = new OtherClientPlayerEntity(EspSettingsScreen.minecraftClient.world, gameProfile);
            SonicBoomParticle.equipStack(EquipmentSlot.HEAD, new ItemStack((ItemConvertible)Items.NETHERITE_HELMET));
            SonicBoomParticle.equipStack(EquipmentSlot.FEET, new ItemStack((ItemConvertible)Items.NETHERITE_BOOTS));
            SonicBoomParticle.equipStack(EquipmentSlot.MAINHAND, new ItemStack((ItemConvertible)Items.NETHERITE_SWORD));
            this.playerPreviewEntity = SonicBoomParticle;
        }
        return this.playerPreviewEntity;
    }

    private ZombieEntity getMobPreviewEntity() {
        if (this.mobPreviewEntity == null && EspSettingsScreen.minecraftClient.world != null) {
            this.mobPreviewEntity = new ZombieEntity(EntityType.ZOMBIE, (World)EspSettingsScreen.minecraftClient.world);
        }
        return this.mobPreviewEntity;
    }

    private Entity getAnimalPreviewEntity() {
        if (this.animalPreviewEntity == null && EspSettingsScreen.minecraftClient.world != null) {
            this.animalPreviewEntity = new PigEntity(EntityType.PIG, (World)EspSettingsScreen.minecraftClient.world);
        }
        return this.animalPreviewEntity;
    }

    private ItemEntity getItemPreviewEntity() {
        if (this.itemPreviewEntity == null && EspSettingsScreen.minecraftClient.world != null) {
            this.itemPreviewEntity = new ItemEntity(EntityType.ITEM, (World)EspSettingsScreen.minecraftClient.world);
            this.itemPreviewEntity.setStack(new ItemStack((ItemConvertible)Items.NETHERITE_SWORD));
        }
        return this.itemPreviewEntity;
    }

    private void drawEntityModel(MatrixStack class_45872, float f, float f2, float f3, Quaternionf quaternionf, float f4, float f5, float f6, Entity class_12972) {
        class_45872.push();
        class_45872.translate(f, f2, 50.0f);
        class_45872.scale(f3, f3, -f3);
        class_45872.multiply(quaternionf);
        class_45872.translate(0.0f, -class_12972.getHeight() / 2.0f - f4 * f5, 0.0f);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)this.contentAlpha);
        EntityRenderDispatcher dispatcher = minecraftClient.getEntityRenderDispatcher();
        VertexConsumerProvider.Immediate class_45982 = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
        try {
            EspSettingsScreen.renderPreviewEntity(dispatcher, class_12972, f6, class_45872, (VertexConsumerProvider)class_45982);
            class_45982.draw();
        }
        catch (Exception exception) {
            // the original swallows preview render failures
        }
        RenderSystem.enableDepthTest();
        class_45872.pop();
    }

    /**
     * Binds the entity and render-state type variables the access-widened nine-argument
     * {@code EntityRenderDispatcher#render} requires. {@code getRenderer} hands back an
     * {@code EntityRenderer<? super E, ?>}, whose wildcard cannot satisfy the method's
     * {@code S} parameter without this capture.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <E extends Entity, S extends net.minecraft.client.render.entity.state.EntityRenderState> void renderPreviewEntity(EntityRenderDispatcher entityRenderDispatcher, E e, float f, MatrixStack class_45872, VertexConsumerProvider class_4597) {
        EntityRenderer entityRenderer = entityRenderDispatcher.getRenderer(e);
        EntityRenderer<E, S> entityRenderer2 = (EntityRenderer<E, S>)entityRenderer;
        entityRenderDispatcher.render(e, 0.0, 0.0, 0.0, f, class_45872, class_4597, 0xF000F0, entityRenderer2);
    }

    private void drawPreviewPanel(RockstarDrawContext drawContext, float f, float f2, float f3, float f4) {
        drawContext.drawShadow(f, f2, f3, f4, 25.0f, WidgetState.uniform(11.0f), ColorPalette.BLACK.mulAlpha(0.5f));
        drawContext.drawBlurredRect(f, f2, f3, f4, 5.0f, 3.0f, WidgetState.uniform(11.0f), ColorPalette.WHITE);
        drawContext.drawSquircle(f, f2, f3, f4, 3.0f, WidgetState.uniform(11.0f), ColorPalette.PANEL_COLOR);
        drawContext.drawSquircleBorder(f, f2, f3, f4, 0.5f, 3.0f, WidgetState.uniform(11.0f), ColorPalette.BORDER_COLOR);
    }

    public void close() {
        this.selectedIntegerSetting = null;
        super.close();
        Menu.updateMenuState();
    }

    @Generated
    public static float getPreviewYaw() {
        return previewYaw;
    }

    @Generated
    public static float getPreviewPitch() {
        return previewPitch;
    }

    @Generated
    public static float getPreviewScale() {
        return previewScale;
    }

    @Generated
    public static float getPreviewOffset() {
        return previewOffset;
    }


}

