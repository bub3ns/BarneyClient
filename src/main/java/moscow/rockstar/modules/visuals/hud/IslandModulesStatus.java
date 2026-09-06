package moscow.rockstar.modules.visuals.hud;

import moscow.rockstar.combat.RotationController;
import moscow.rockstar.combat.rotation.RotationLogWriter;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.modules.player.automation.inventory.AutoSwap;
import moscow.rockstar.modules.player.movement.camera.FreeCamera;
import moscow.rockstar.modules.player.movement.teleport.Blink;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.colors.GradientColors;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.factory.UiNodeFactory;
import moscow.rockstar.ui.hud.DynamicIslandEntry;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.util.NumberFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import pyrock.utility.render.ColorRGBA;

/**
 * The "modules" island status. 1:1 with rockstar/ilIlil/IiiIIIIII, entry [2] of
 * rockstar/ilIlil/IiIiiiIIi#I(Lrockstar/ilIlil/IIiiiiiii;)V.
 *
 * <p>It surfaces whichever of four module states is active, in this priority order (the original
 * {@code I()Lrockstar/ilIlil/IiiIIIIII$I;} tests them top to bottom and returns the first hit):
 * <ol>
 *   <li>{@code NEURO_RECORD} - {@code Aura.getRotationController().getRotationLogger()} is
 *       recording ({@code iiIiIIIi} / {@code iiiIIiii} / {@code iiiIiIiI}).</li>
 *   <li>{@code FREECAM_HEIGHT} - {@code FreeCamera} is enabled ({@code IIIiIIiii}).</li>
 *   <li>{@code BLINK} - {@code Blink} is enabled ({@code IIIiIIIiI}).</li>
 *   <li>{@code AUTO_CERBER} - {@code AutoSwap.isCerberusReady()} ({@code IIIiIIIIi#Iii()Z},
 *       the lombok getter of its first boolean field).</li>
 * </ol>
 *
 * <p>Module identities were pinned by body, not by name:
 * {@code IIIiIIiii#I()Lclass_243;} / {@code #i()Lclass_243;} are the only two no-arg Vec3d members
 * on that class and are used by its own HUD listener as {@code getPosition()} minus the stored
 * {@code position} field, i.e. {@link FreeCamera#getPosition()} and
 * {@link FreeCamera#getCameraVelocity()};
 * {@code IIIiIIIiI} exposes exactly {@code getPulseTimer()} / {@code getPulseSetting()} /
 * {@code getPulseIntervalSetting()}, which is {@link Blink}.
 *
 * <p><b>One substitution, matching what the tree already does elsewhere.</b> The original builds the
 * two numeric readouts with {@code rockstar/ilIlil/Iiii}, a rolling-digit number node. That class
 * has no remapped counterpart, and the two other places that use it -
 * {@code MultiBooleanSetting.buildComponent()} and {@code SettingGroupHeader.content()} - already
 * substitute a plain {@link TextComponent} with {@code textInset(5.0f).interactive(false)}. This
 * class follows that same substitution so the tree stays internally consistent; the numbers are
 * correct, only the digit-roll animation is missing.
 */
public class IslandModulesStatus extends DynamicIslandEntry implements ClientAccess {
    /** ORIGINAL: the static ColorRGBA field {@code I}. */
    private static final ColorRGBA HEIGHT_NEAR_COLOR = new ColorRGBA(34.0f, 187.0f, 94.0f);
    /** ORIGINAL: the static ColorRGBA field {@code i}. */
    private static final ColorRGBA HEIGHT_FAR_COLOR = new ColorRGBA(239.0f, 68.0f, 68.0f);
    /** ORIGINAL: the static ColorRGBA field {@code II} = {@code BLACK.mix(RED, 0.5f)}. */
    private static final ColorRGBA CERBER_COLOR = ColorPalette.BLACK.mix(ColorPalette.RED, 0.5f);
    /** ORIGINAL: the static ColorRGBA field {@code Ii}. */
    private static final ColorRGBA RECORD_COLOR = new ColorRGBA(228.0f, 52.0f, 52.0f);

    /** ORIGINAL: the enum {@code IiiIIIIII$I}, constants in declaration order I, i, II, Ii. */
    enum Mode {
        NEURO_RECORD,
        FREECAM_HEIGHT,
        BLINK,
        AUTO_CERBER
    }

    /** ORIGINAL field {@code I} of type {@code IiiIIIIII$I}. */
    private Mode mode;
    /** ORIGINAL field {@code I:I} - the freecam height delta. */
    private int freecamHeight;
    /** ORIGINAL field {@code iI:ColorRGBA} - the freecam badge colour. */
    private ColorRGBA freecamColor = HEIGHT_NEAR_COLOR;
    /** ORIGINAL field {@code i:I} - recorded minutes. */
    private int recordMinutes;
    /** ORIGINAL field {@code II:I} - recorded seconds. */
    private int recordSeconds;
    /**
     * ORIGINAL field {@code I:F} - the record badge pulse. {@code IiiIIIIII#<init>} initialises it
     * to 1.0f right after the super call ({@code aload this; fconst_1; putfield IiiIIIIII.I F}),
     * next to the {@code iI = HEIGHT_NEAR_COLOR} store.
     */
    private float recordPulse = 1.0f;

    /** ORIGINAL fields {@code I}, {@code i}, {@code II}, {@code Ii} of type {@code iiI}. */
    private UiNode freecamNode;
    private UiNode blinkNode;
    private UiNode cerberNode;
    private UiNode recordNode;

    public IslandModulesStatus(MultiBooleanSetting statuses) {
        super(statuses, "modules");
    }

    /** ORIGINAL: {@code prepare(IiIiiIIII)V}. */
    @Override
    public void prepare(DynamicIslandHud island) {
        this.mode = this.pickMode();
        if (this.mode == null) {
            return;
        }
        switch (this.mode) {
            case NEURO_RECORD -> this.updateRecord();
            case FREECAM_HEIGHT -> this.updateFreecam();
            case BLINK -> {
            }
            case AUTO_CERBER -> {
            }
        }
    }

    /** ORIGINAL: {@code content(IiIiiIIII)Lrockstar/ilIlil/iiI;}. */
    @Override
    public UiNode content(DynamicIslandHud island) {
        if (this.mode == null) {
            return null;
        }
        return switch (this.mode) {
            case NEURO_RECORD -> this.recordNode();
            case FREECAM_HEIGHT -> this.freecamNode();
            case BLINK -> this.blinkNode();
            case AUTO_CERBER -> this.cerberNode();
        };
    }

    /** ORIGINAL: {@code canShow()}; the remap expresses the hook as {@code isVisible()}. */
    @Override
    public boolean isVisible() {
        return this.pickMode() != null;
    }

    /** ORIGINAL: the private {@code I()V} - freecam height + colour ramp. */
    private void updateFreecam() {
        FreeCamera freeCamera = this.freeCamera();
        Vec3d cameraPosition = freeCamera.getPosition();
        Vec3d anchorPosition = freeCamera.getCameraVelocity();
        this.freecamHeight = (int)cameraPosition.y - (int)anchorPosition.y;
        float ramp = MathHelper.clamp((float)((float)Math.abs(this.freecamHeight) / 35.0f),
                (float)0.0f, (float)1.0f);
        this.freecamColor = HEIGHT_NEAR_COLOR.mix(HEIGHT_FAR_COLOR, ramp);
    }

    /** ORIGINAL: the private {@code Ii()V} - recorded time + the sine pulse. */
    private void updateRecord() {
        RotationLogWriter logger = this.rotationLogger();
        int seconds = logger == null ? 0 : logger.getRecordedRowCount() / 20;
        this.recordMinutes = seconds / 60;
        this.recordSeconds = seconds % 60;
        this.recordPulse = 0.8f + 0.2f * (float)Math.sin((double)System.currentTimeMillis() / 260.0);
    }

    /** ORIGINAL: the private {@code I()Lrockstar/ilIlil/iiI;}. */
    private UiNode freecamNode() {
        if (this.freecamNode == null) {
            Component row = UiNodeFactory.createRowComponent(15.0f, Insets.of(0.0f, 4.5f, 0.0f, 6.0f), 2.0f)
                    .alignment(Alignment.CENTER);
            Component badge = UiNodeFactory
                    .createOffsetComponent(() -> -14.0f * (1.0f - this.animation.getValue()))
                    .horizontal()
                    .height(8.0f)
                    .padding(Insets.of(0.0f, 2.5f, 0.0f, 3.0f))
                    .alignment(Alignment.CENTER)
                    .renderHook((drawContext, component) -> drawContext.drawRoundedRect(component.x(),
                            component.y(), component.w(), component.h(), WidgetState.uniform(3.0f),
                            this.freecamColor.withAlpha(255.0f * this.animation.getValue())));
            badge.add(UiNodeFactory.createTextNode(Font.MEDIUM.metrics(6.0f),
                    () -> this.freecamHeight < 0 ? "-" : "",
                    () -> ColorPalette.WHITE.withAlpha(255.0f * this.animation.getValue())));
            badge.add(new TextComponent()
                    .text(Font.MEDIUM.metrics(6.0f), () -> Integer.toString(Math.abs(this.freecamHeight)),
                            textComponent -> ColorPalette.WHITE.withAlpha(255.0f * this.animation.getValue()))
                    .textInset(5.0f)
                    .interactive(false));
            row.add(UiNodeFactory.createTextNode(Font.MEDIUM.metrics(7.0f),
                    () -> Localization.translate("hud.dynamic_island.modules.freecam_height"),
                    () -> ColorPalette.getPrimaryTextColor().withAlpha(255.0f * this.animation.getValue()),
                    () -> -6.0f * (1.0f - this.animation.getValue())));
            row.add(badge);
            this.freecamNode = row;
        }
        return this.freecamNode;
    }

    /** ORIGINAL: the private {@code i()Lrockstar/ilIlil/iiI;}. */
    private UiNode blinkNode() {
        if (this.blinkNode == null) {
            Component row = UiNodeFactory.createRowComponent(15.0f, Insets.of(0.0f, 5.0f, 0.0f, 6.0f), 3.0f)
                    .width(80.0f)
                    .alignment(Alignment.CENTER);
            row.add(UiNodeFactory.createTextNode(Font.MEDIUM.metrics(7.0f), () -> "Blink",
                    () -> ColorPalette.getPrimaryTextColor().withAlpha(255.0f * this.animation.getValue()),
                    () -> -10.0f * (1.0f - this.animation.getValue())));
            row.add(new BlinkBarNode().fillWidth());
            this.blinkNode = row;
        }
        return this.blinkNode;
    }

    /** ORIGINAL: the private {@code II()Lrockstar/ilIlil/iiI;}. */
    private UiNode cerberNode() {
        if (this.cerberNode == null) {
            Component row = UiNodeFactory.createRowComponent(15.0f, Insets.of(0.0f, 5.0f, 0.0f, 4.0f), 4.0f)
                    .alignment(Alignment.CENTER);
            row.add(UiNodeFactory.createPaintNode(7.0f, 7.0f, (drawContext, node, alpha) -> {
                float progress = this.animation.getValue();
                ColorRGBA quarter = CERBER_COLOR.mix(ColorPalette.WHITE, 0.25f);
                ColorRGBA half = CERBER_COLOR.mix(ColorPalette.WHITE, 0.5f);
                drawContext.drawRoundedRect(node.x() - 10.0f * (1.0f - progress), node.y(), node.w(),
                        node.h(), WidgetState.uniform(3.0f),
                        new GradientColors(CERBER_COLOR, half, half, quarter));
            }));
            row.add(UiNodeFactory.createTextNode(Font.MEDIUM.metrics(7.0f),
                    () -> Localization.translate("modules.settings.auto_swap.auto_cerber"),
                    () -> ColorPalette.getPrimaryTextColor().withAlpha(255.0f * this.animation.getValue()),
                    () -> 10.0f * (1.0f - this.animation.getValue())));
            this.cerberNode = row;
        }
        return this.cerberNode;
    }

    /** ORIGINAL: the private {@code Ii()Lrockstar/ilIlil/iiI;}. */
    private UiNode recordNode() {
        if (this.recordNode == null) {
            Component row = UiNodeFactory.createRowComponent(15.0f, Insets.of(0.0f, 4.0f, 0.0f, 4.0f), 3.5f)
                    .alignment(Alignment.CENTER);
            Component badge = UiNodeFactory
                    .createOffsetComponent(() -> -20.0f * (1.0f - this.animation.getValue()))
                    .horizontal()
                    .height(8.0f)
                    .padding(Insets.of(0.0f, 2.5f, 0.0f, 3.0f))
                    .alignment(Alignment.CENTER)
                    .renderHook((drawContext, component) -> drawContext.drawRoundedRect(component.x(),
                            component.y(), component.w(), component.h(), WidgetState.uniform(3.0f),
                            RECORD_COLOR.withAlpha(
                                    255.0f * this.animation.getValue() * this.recordPulse)));
            badge.add(UiNodeFactory.createTextNode(Font.MEDIUM.metrics(6.0f),
                    () -> this.recordMinutes + ":",
                    () -> ColorPalette.blendWithContrastBackground(RECORD_COLOR)
                            .withAlpha(255.0f * this.animation.getValue())));
            badge.add(new TextComponent()
                    .text(Font.MEDIUM.metrics(6.0f), () -> Integer.toString(this.recordSeconds),
                            textComponent -> ColorPalette.blendWithContrastBackground(RECORD_COLOR)
                                    .withAlpha(255.0f * this.animation.getValue()))
                    .textInset(5.0f)
                    .interactive(false));
            row.add(badge);
            row.add(UiNodeFactory.createTextNode(Font.MEDIUM.metrics(7.0f),
                    () -> Localization.translate("hud.dynamic_island.modules.neuro_record"),
                    () -> ColorPalette.getPrimaryTextColor().withAlpha(255.0f * this.animation.getValue()),
                    () -> 10.0f * (1.0f - this.animation.getValue())));
            this.recordNode = row;
        }
        return this.recordNode;
    }

    /** ORIGINAL: the private {@code I()Lrockstar/ilIlil/IiiIIIIII$I;}. */
    private Mode pickMode() {
        if (this.isRecording()) {
            return Mode.NEURO_RECORD;
        }
        if (this.isFreecamActive()) {
            return Mode.FREECAM_HEIGHT;
        }
        if (this.isBlinkActive()) {
            return Mode.BLINK;
        }
        if (this.isCerberActive()) {
            return Mode.AUTO_CERBER;
        }
        return null;
    }

    /** ORIGINAL: the private {@code I()Z}. */
    private boolean isFreecamActive() {
        return IslandModulesStatus.minecraftClient.player != null && this.freeCamera().isEnabled();
    }

    /** ORIGINAL: the private {@code i()Z}. */
    private boolean isBlinkActive() {
        return this.blink().isEnabled() && EntityUtils.isClientWorldReady();
    }

    /** ORIGINAL: the private {@code II()Z}. */
    private boolean isCerberActive() {
        return this.autoSwap().isCerberusReady() && EntityUtils.isClientWorldReady();
    }

    /** ORIGINAL: the private {@code Ii()Z}. */
    private boolean isRecording() {
        RotationLogWriter logger = this.rotationLogger();
        return logger != null && logger.isRecording() && EntityUtils.isClientWorldReady();
    }

    /** ORIGINAL: the private {@code I()Lrockstar/ilIlil/iiiIiIiI;}. */
    private RotationLogWriter rotationLogger() {
        Aura aura = RockstarClient.create().getModuleRegistry().getModule(Aura.class);
        if (aura == null || aura.getRotationController() == null) {
            return null;
        }
        RotationController controller = aura.getRotationController();
        return controller.getRotationLogger();
    }

    /** ORIGINAL: the private {@code I()Lrockstar/ilIlil/IIIiIIiii;}. */
    private FreeCamera freeCamera() {
        return RockstarClient.create().getModuleRegistry().getModule(FreeCamera.class);
    }

    /** ORIGINAL: the package-private {@code I()Lrockstar/ilIlil/IIIiIIIiI;}. */
    Blink blink() {
        return RockstarClient.create().getModuleRegistry().getModule(Blink.class);
    }

    /** ORIGINAL: the private {@code I()Lrockstar/ilIlil/IIIiIIIIi;}. */
    private AutoSwap autoSwap() {
        return RockstarClient.create().getModuleRegistry().getModule(AutoSwap.class);
    }

    /** ORIGINAL: the inner class {@code IiiIIIIII$i} - the blink recharge bar. */
    final class BlinkBarNode extends UiNode {
        BlinkBarNode() {
            this.height(6.0f);
            this.interactive(false);
        }

        @Override
        protected void measure() {
            this.prefW = 0.0f;
            this.prefH = 6.0f;
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            Blink blink = IslandModulesStatus.this.blink();
            FontMetrics font = Font.MEDIUM.metrics(7.0f);
            float progress = IslandModulesStatus.this.animation.getValue();
            if (!blink.getPulseSetting().isEnabled()) {
                String label = NumberFormatting.formatOneDecimal(
                        (double)((float)blink.getPulseTimer().getElapsedMillis() / 1000.0f))
                        + " " + Localization.translate("sec");
                drawContext.drawRightText(font, label, this.x() + this.w() - 1.0f,
                        this.y() + this.h() / 2.0f - font.getFontTopOffset() / 2.0f,
                        ColorPalette.getPrimaryTextColor().withAlpha(255.0f * progress));
                return;
            }
            float total = blink.getPulseIntervalSetting().getValue() * 50.0f;
            float filled = this.w() * ((total - (float)blink.getPulseTimer().getElapsedMillis()) / total);
            drawContext.drawRoundedRect(this.x(), this.y(), this.w(), this.h(),
                    WidgetState.uniform(2.5f),
                    ColorPalette.getPanelBackgroundColor().withAlpha(255.0f * progress));
            drawContext.drawRoundedRect(this.x() + this.w() - filled, this.y(), filled, this.h(),
                    WidgetState.uniform(2.5f),
                    ColorPalette.getAccentColor().withAlpha(255.0f * progress));
        }
    }
}
