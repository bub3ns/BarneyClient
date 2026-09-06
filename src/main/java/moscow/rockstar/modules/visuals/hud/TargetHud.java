package moscow.rockstar.modules.visuals.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.render.batch.OverlayBatchBuilder;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.FontRenderContext;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.state.UiScissorStack;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.theme.ColorTheme;
import moscow.rockstar.util.NumberFormatting;
import moscow.rockstar.util.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import pyrock.utility.render.ColorRGBA;

/** Target information panel. 1:1 with rockstar/ilIlil/IiIiIiiiI. */
public class TargetHud extends HudElement {
    private final BooleanSetting lookAt = new BooleanSetting(this, "hud.targethud.look");
    private final ModeSetting armorMode = new ModeSetting((SettingOwner)((Object)this), "hud.targethud.armor");
    private final ModeSetting.Option noneOption = new ModeSetting.Option(this.armorMode, "hud.targethud.armor.none");
    private final ModeSetting.Option numberOption;
    private final ModeSetting.Option iconOption;
    private final Animation headSlide;
    private final Animation nameSlide;
    private final Animation healthSlide;
    private final Animation valueSlide;
    private final Animation armorSlide;
    private final Animation healthBar;
    private final Animation absorptionBar;
    private final Animation healthNumber;
    private final Animation armorWidth;
    private final Animation hoverCopy;
    private final Animation copiedFlip;
    private final Animation eatingPulse;
    private final Animation eatingSine;
    private final Animation[] slotAnimations;
    private LivingEntity lastTarget;
    private final Timer copyTimer;
    private boolean copied;

    public TargetHud() {
        super("hud.targethud", "hud/target");
        this.numberOption = new ModeSetting.Option(this.armorMode, "hud.targethud.armor.number").select();
        this.iconOption = new ModeSetting.Option(this.armorMode, "hud.targethud.armor.icon");
        this.headSlide = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
        this.nameSlide = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
        this.healthSlide = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
        this.valueSlide = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
        this.armorSlide = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
        this.healthBar = new Animation(300L, 0.0f, Easing.easeOutBack);
        this.absorptionBar = new Animation(300L, 0.0f, Easing.easeOutBack);
        this.healthNumber = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
        this.armorWidth = new Animation(300L, 0.0f, Easing.easeOutBack);
        this.hoverCopy = new Animation(300L, 0.0f, Easing.easeOutBack);
        this.copiedFlip = new Animation(500L, 0.0f, Easing.easeOutOvershootSoft);
        this.eatingPulse = new Animation(150L, 0.0f, Easing.easeOutBack);
        this.eatingSine = new Animation(50L, 0.0f, Easing.easeInOutSine);
        this.slotAnimations = new Animation[4];
        this.copyTimer = new Timer();
        for (int i = 0; i < this.slotAnimations.length; ++i) {
            this.slotAnimations[i] = new Animation(300L, 0.0f, Easing.easeOutBack);
        }
    }

    @Override
    public void update(RockstarDrawContext drawContext) {
        super.update(drawContext);
        this.width = 91.0f;
        this.height = 27.0f;
    }

    @Override
    public void renderComponent(RockstarDrawContext drawContext) {
        LivingEntity resolved = this.resolveTarget();
        if (resolved != null) {
            this.lastTarget = resolved;
        }
        if (this.lastTarget == null) {
            return;
        }
        FontMetrics nameFont = Font.REGULAR.metrics(7.0f);
        FontMetrics armorFont = Font.SEMIBOLD.metrics(6.0f);
        FontMetrics healthFont = Font.SEMIBOLD.metrics(7.0f);
        ColorRGBA panel = ColorPalette.getPanelColor().withAlpha(255.0f * MathUtils.interpolateDouble(
            ColorPalette.getThemeColorSettings().getOverlayAlphaMinimum(),
            ColorPalette.getThemeColorSettings().getOverlayAlphaMaximum(),
            Interface.getLiquidGlassAlpha()));
        boolean hoveringName = UiUtils.contains(this.x + 26.0f,
            this.y + 2.0f + 6.0f * this.nameSlide.getValue(),
            Math.min(30.0f, Font.REGULAR.metrics(7.0f).measureText(this.lastTarget.getName().getString())),
            6.0, drawContext);
        if (!hoveringName || this.copyTimer.hasElapsed(1000L)) {
            this.copied = false;
        }
        boolean eating = this.lastTarget.isUsingItem()
            && this.lastTarget.getActiveItem().contains(DataComponentTypes.FOOD);
        this.eatingPulse.setReverse(eating);
        if (eating) {
            float pulse = (float)Math.sin((double)System.currentTimeMillis() / 100.0) * 0.5f + 0.5f;
            this.eatingSine.update(pulse);
        }
        this.hoverCopy.setReverse(hoveringName);
        this.copiedFlip.setReverse(this.copied);
        this.headSlide.setReverse(this.animation.getValue() * this.visible.getValue() >= 1.0f);
        this.nameSlide.setReverse(this.headSlide.getValue() >= 0.7f);
        this.healthSlide.setReverse(this.nameSlide.getValue() >= 0.7f);
        this.valueSlide.setReverse(this.healthSlide.getValue() >= 0.7f);
        this.armorSlide.setReverse(this.headSlide.getValue() >= 0.7f);
        float barHealth = this.lastTarget instanceof PlayerEntity playerEntity
            ? EntityUtils.getPlayerHealth(playerEntity)
            : this.lastTarget.getHealth();
        this.healthBar.update(barHealth / this.lastTarget.getMaxHealth());
        this.absorptionBar.update(this.lastTarget.getAbsorptionAmount() / 20.0f);
        float rawHealth = this.lastTarget instanceof PlayerEntity playerEntity2
            ? EntityUtils.getPlayerHealth(playerEntity2)
            : this.lastTarget.getHealth();
        this.healthNumber.update(rawHealth);
        if (this.animation.getValue() == 0.0f) {
            return;
        }
        if (!this.noneOption.isSelected()) {
            float shaderAlpha = RenderSystem.getShaderColor()[3];
            ItemStack[] hands = new ItemStack[]{this.lastTarget.getMainHandStack(), this.lastTarget.getOffHandStack()};
            boolean anyItem = Arrays.stream(hands).anyMatch(stack -> !stack.isEmpty());
            for (ItemStack stack : this.lastTarget.getArmorItems()) {
                if (stack.isEmpty()) continue;
                anyItem = true;
                break;
            }
            if (anyItem) {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                drawContext.drawItem(Items.DIAMOND_CHESTPLATE, -992.0f, 994.0f, 1.0f);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, shaderAlpha);
            }
            float total = 0.0f;
            int slot = 0;
            for (ItemStack stack : this.lastTarget.getArmorItems()) {
                if (stack.isEmpty()) continue;
                total += (this.iconOption.isSelected()
                    ? 11.0f
                    : 5.0f + armorFont.measureText(TargetHud.formatDurability(stack))) + 2.0f;
            }
            final float handChip = 11.0f;
            if (this.iconOption.isSelected()) {
                for (ItemStack stack : hands) {
                    if (stack.isEmpty()) continue;
                    total += 13.0f;
                }
            }
            this.armorWidth.update(total - 2.0f);
            float cursor = -this.armorWidth.getValue() / 2.0f;
            List<ArmorLabel> labels = new ArrayList<>();
            for (ItemStack stack : this.lastTarget.getArmorItems()) {
                this.slotAnimations[slot].setReverse(!stack.isEmpty());
                float progress = this.armorSlide.getValue() * this.slotAnimations[slot].getValue();
                if (progress <= 0.001f) {
                    ++slot;
                    continue;
                }
                String label = TargetHud.formatDurability(stack);
                boolean icon = this.iconOption.isSelected();
                float chipWidth = icon ? 11.0f : 5.0f + armorFont.measureText(label);
                float chipHeight = icon ? 11.0f : 9.0f;
                float chipX = this.x + this.width / 2.0f + cursor;
                float chipY = this.y + this.height - 4.0f + 6.0f * progress;
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, shaderAlpha * progress);
                drawContext.drawBlurredRect(chipX, chipY, chipWidth, chipHeight, 5.0f,
                    WidgetState.uniform(1.5f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue()));
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, shaderAlpha);
                drawContext.drawRoundedRect(chipX, chipY, chipWidth, chipHeight,
                    WidgetState.uniform(1.5f), panel.withAlpha(panel.getAlpha() * progress));
                UiScissorStack.push(drawContext.getMatrices(), chipX, chipY, chipWidth, chipHeight);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, shaderAlpha * progress * 0.5f);
                if (this.numberOption.isSelected()) {
                    drawContext.drawItem(stack, chipX - 11.0f + chipWidth / 2.0f + 2.0f, chipY - 4.0f, 1.0f);
                } else {
                    drawContext.drawItem(stack, chipX - 11.0f + chipWidth / 2.0f + 5.5f, chipY, 0.7f);
                }
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, shaderAlpha);
                UiScissorStack.pop();
                if (this.numberOption.isSelected()) {
                    labels.add(new ArmorLabel(label, chipX + 3.0f, chipY + 2.5f, 255.0f * progress));
                }
                cursor += (chipWidth + 2.0f) * progress;
                ++slot;
            }
            if (!labels.isEmpty()) {
                FontRenderContext pass = new FontRenderContext(
                    VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, drawContext.getMatrices());
                for (ArmorLabel armorLabel : labels) {
                    drawContext.drawText(armorFont, armorLabel.text(), armorLabel.x(), armorLabel.y(),
                        ColorPalette.getPrimaryTextColor().withAlpha(armorLabel.alpha()));
                }
                pass.render();
            }
            float handsWidth = Arrays.stream(hands)
                .mapToInt(stack -> stack.isEmpty() ? 0 : (int)(handChip + 2.0f)).sum() - 2;
            float handCursor = this.numberOption.isSelected() ? -handsWidth / 2.0f : cursor;
            boolean anyArmor = false;
            for (ItemStack stack : this.lastTarget.getArmorItems()) {
                if (stack.isEmpty()) continue;
                anyArmor = true;
                break;
            }
            for (ItemStack stack : hands) {
                if (stack.isEmpty()) continue;
                float chipX = this.x + this.width / 2.0f + handCursor;
                float chipY = this.y + this.height - 4.0f + 6.0f * this.armorSlide.getValue()
                    + (float)(this.numberOption.isSelected() && anyArmor ? 12 : 0);
                float progress = this.armorSlide.getValue()
                    * (eating && this.lastTarget.getActiveItem() == stack
                        ? 0.5f + 0.7f * this.eatingSine.getValue()
                        : 1.0f);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, shaderAlpha * progress);
                drawContext.drawBlurredRect(chipX, chipY, handChip, handChip, 5.0f,
                    WidgetState.uniform(1.5f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue()));
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, shaderAlpha);
                drawContext.drawRoundedRect(chipX, chipY, handChip, handChip,
                    WidgetState.uniform(1.5f), panel.withAlpha(panel.getAlpha() * progress));
                UiScissorStack.push(drawContext.getMatrices(), chipX, chipY, handChip, handChip);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, shaderAlpha * progress);
                drawContext.drawItem(stack, chipX - 11.0f + handChip / 2.0f + 5.5f, chipY, 0.7f);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, shaderAlpha);
                UiScissorStack.pop();
                handCursor += handChip + 2.0f;
                if (!this.iconOption.isSelected()) continue;
                cursor += (handChip + 2.0f) * this.armorSlide.getValue();
            }
        }
        if (this.dragAnim.getValue() > 0.0f) {
            drawContext.drawShadow(this.x - 5.0f, this.y - 5.0f, this.width + 10.0f, this.height + 10.0f, 15.0f,
                WidgetState.uniform(ColorPalette.getThemeColorSettings().getCornerRadius()),
                ColorRGBA.BLACK.withAlpha(63.75f * this.dragAnim.getValue()));
        }
        drawContext.drawClientRect(this.x, this.y, this.width, this.height,
            this.animation.getValue(), this.dragAnim.getValue(), 7.0f);
        float headAlpha = 255.0f * this.headSlide.getValue();
        float nameAlpha = 255.0f * this.nameSlide.getValue();
        float valueAlpha = 255.0f * this.valueSlide.getValue();
        float barAlpha = 255.0f * this.healthSlide.getValue();
        UiScissorStack.push(drawContext.getMatrices(), this.x, this.y, this.width, this.height);
        if (this.lastTarget instanceof AbstractClientPlayerEntity clientPlayer) {
            drawContext.drawHead(clientPlayer, this.x + 5.0f * this.headSlide.getValue(), this.y + 5.0f, 17.0f,
                WidgetState.uniform(4.0f), ColorPalette.WHITE.withAlpha(headAlpha));
        } else {
            drawContext.drawRoundedTexture(RockstarClient.resourceId(
                    Interface.isLiquidGlassEnvironmentReady()
                        ? "icons/hud/whoglass.png"
                        : (RockstarClient.create().getColorTheme() == ColorTheme.DARK
                            ? "icons/hud/whodark.png"
                            : "icons/hud/who.png")),
                this.x + 5.0f * this.headSlide.getValue(), this.y + 5.0f, 17.0f, 17.0f,
                WidgetState.uniform(4.0f), ColorPalette.WHITE.withAlpha(headAlpha));
        }
        NameProtect nameProtect = RockstarClient.create().getModuleRegistry().getModule(NameProtect.class);
        String targetName = this.lastTarget.getName().getString();
        if (nameProtect.isEnabled()) {
            targetName = nameProtect.replacePlayerOrServerName(targetName);
        }
        String healthText = rawHealth == 1000.0f
            ? "?"
            : NumberFormatting.formatOneDecimal(this.healthNumber.getValue()).replace(",", ".");
        drawContext.drawFadeoutText(nameFont, targetName,
            this.x + 26.0f + 8.0f * this.hoverCopy.getValue(),
            this.y + 2.0f + 6.0f * this.nameSlide.getValue(),
            ColorPalette.getPrimaryTextColor().withAlpha(nameAlpha), 0.7f, 1.0f,
            this.width - 37.0f - 8.0f * this.hoverCopy.getValue() - healthFont.measureText(healthText));
        float copyAlpha = nameAlpha * this.hoverCopy.getValue() * (1.0f - this.copiedFlip.getValue());
        if (copyAlpha > 0.5f) {
            ItemRenderUtils.translateAndRotate(drawContext.getMatrices(),
                this.x + 24.0f + 5.0f * this.hoverCopy.getValue(),
                this.y + 5.0f + 6.0f * this.nameSlide.getValue(),
                90.0f * this.copiedFlip.getValue());
            drawContext.drawIcon("copy",
                this.x + 21.0f + 5.0f * this.hoverCopy.getValue(),
                this.y + 2.0f + 6.0f * this.nameSlide.getValue(), 6.0f,
                ColorPalette.getPrimaryTextColor().withAlpha(copyAlpha));
            ItemRenderUtils.popMatrix(drawContext.getMatrices());
        }
        float checkAlpha = nameAlpha * this.hoverCopy.getValue() * this.copiedFlip.getValue();
        if (checkAlpha > 0.5f) {
            ItemRenderUtils.translateAndRotate(drawContext.getMatrices(),
                this.x + 24.0f + 5.0f * this.hoverCopy.getValue(),
                this.y + 5.0f + 6.0f * this.nameSlide.getValue(),
                -90.0f + 90.0f * this.copiedFlip.getValue());
            drawContext.drawIcon("check",
                this.x + 21.0f + 5.0f * this.hoverCopy.getValue(),
                this.y + 2.0f + 6.0f * this.nameSlide.getValue(), 6.0f,
                ColorPalette.GREEN.withAlpha(checkAlpha));
            ItemRenderUtils.popMatrix(drawContext.getMatrices());
        }
        float barY = this.y + this.height - 5.0f - 6.0f * this.healthSlide.getValue();
        float healthWidth = 57.0f * Math.clamp(this.healthBar.getValue(), 0.0f, 1.0f);
        float absorptionWidth = 57.0f * Math.clamp(this.absorptionBar.getValue(), 0.0f, 1.0f);
        OverlayBatchBuilder batch = new OverlayBatchBuilder(Font.SEMIBOLD, 1.5f);
        batch.queueText(drawContext.getMatrices().peek().getPositionMatrix(), healthText,
            armorFont.getFontScale(),
            this.x + this.width - 7.0f - armorFont.measureText(healthText),
            this.y + 2.0f + 6.0f * this.valueSlide.getValue(), 0.0f,
            ColorPalette.getAccentColor().withAlpha(valueAlpha).getRGB());
        batch.queueColoredOverlay(drawContext.getMatrices().peek().getPositionMatrix(),
            this.x + 26.0f, barY, 57.0f, 3.0f,
            ColorPalette.getPanelBackgroundColor().withAlpha(barAlpha * (1.0f - 0.7f * Interface.getLiquidGlassAlpha())));
        batch.queueColoredOverlay(drawContext.getMatrices().peek().getPositionMatrix(),
            this.x + 26.0f, barY, healthWidth, 3.0f,
            ColorPalette.getAccentColor().withAlpha(barAlpha));
        if (!ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) && absorptionWidth > 0.0f) {
            batch.queueColoredOverlay(drawContext.getMatrices().peek().getPositionMatrix(),
                this.x + 83.0f - absorptionWidth, barY, absorptionWidth, 3.0f,
                new ColorRGBA(255.0f, 220.0f, 81.0f, barAlpha));
        }
        batch.flush();
        UiScissorStack.pop();
    }

    public static String formatDurability(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageable()) {
            return "100%";
        }
        int max = stack.getMaxDamage();
        int damage = stack.getDamage();
        if (damage >= max) {
            return "0%";
        }
        double percent = 100.0 - (double)damage / (double)max * 100.0;
        return String.format("%.0f%%", percent);
    }

    private LivingEntity resolveTarget() {
        Entity friendTarget = RockstarClient.create().getFriendManager().getTargetEntity();
        LivingEntity living = friendTarget instanceof LivingEntity livingEntity ? livingEntity : null;
        if (living != null) {
            return living;
        }
        if (this.lookAt.isEnabled()
                && MinecraftClient.getInstance().targetedEntity instanceof LivingEntity targeted) {
            return targeted;
        }
        if (MinecraftClient.getInstance().currentScreen instanceof ChatScreen) {
            return MinecraftClient.getInstance().player;
        }
        return null;
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, PointerAction action) {
        if (this.lastTarget != null && UiUtils.contains(this.x + 26.0f,
                this.y + 2.0f + 6.0f * this.nameSlide.getValue(),
                Math.min(30.0f, Font.REGULAR.metrics(7.0f).measureText(this.lastTarget.getName().getString())),
                6.0, mouseX, mouseY)) {
            MinecraftClient.getInstance().keyboard.setClipboard(
                MinecraftClient.getInstance().player.getName().getString());
            this.copyTimer.reset();
            this.copied = true;
            return;
        }
        super.onMouseClicked(mouseX, mouseY, action);
    }

    @Override
    public boolean show() {
        return this.resolveTarget() != null;
    }

    /** rockstar/ilIlil/IiIiIiiiI$I */
    record ArmorLabel(String text, float x, float y, float alpha) {
    }
}
