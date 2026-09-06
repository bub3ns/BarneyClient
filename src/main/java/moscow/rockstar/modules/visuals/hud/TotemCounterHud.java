package moscow.rockstar.modules.visuals.hud;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.modules.combat.defense.AutoTotem;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.Items;

/** Totem counter. 1:1 with rockstar/ilIlil/IiIiIiiii. */
public class TotemCounterHud extends HudElement {
    private static final float NEW_WIDTH = 28.0f;
    private static final float NEW_HEIGHT = 27.0f;
    private static final float ITEM_OFFSET_X = 6.0f;
    private static final float ITEM_OFFSET_Y = 5.5f;
    private static final float TEXT_NUDGE = 1.5f;
    private static final float OLD_WIDTH = 28.0f;
    private static final float OLD_HEIGHT = 24.0f;

    private final ModeSetting style = new ModeSetting((SettingOwner)((Object)this), "Type");
    private final ModeSetting.Option oldStyle = new ModeSetting.Option(this.style, "Old");
    private final ModeSetting.Option newStyle;
    private boolean placed;

    public TotemCounterHud() {
        super("hud.totem_counter", "hud/hotbar");
        this.newStyle = new ModeSetting.Option(this.style, "New").select();
        this.showing = true;
    }

    @Override
    public void update(RockstarDrawContext drawContext) {
        if (this.newStyle.isSelected()) {
            this.width = NEW_WIDTH;
            this.height = NEW_HEIGHT;
        } else {
            this.width = OLD_WIDTH;
            this.height = OLD_HEIGHT;
        }
        if (!this.placed && this.x == 0.0f && this.y == 0.0f) {
            this.x = (float)drawContext.getScaledWindowWidth() / 2.0f + 101.0f;
            this.y = (float)drawContext.getScaledWindowHeight() - 21.5f;
        }
        this.placed = true;
        super.update(drawContext);
    }

    @Override
    public void renderComponent(RockstarDrawContext drawContext) {
        int count = this.displayCount();
        if (count <= 0) {
            return;
        }
        if (this.newStyle.isSelected()) {
            this.renderNew(drawContext, count);
        } else {
            this.renderOld(drawContext, count);
        }
    }

    private void renderNew(RockstarDrawContext drawContext, int count) {
        FontMetrics font = Font.SEMIBOLD.metrics(6.0f);
        String text = String.valueOf(count);
        float itemX = this.x + ITEM_OFFSET_X;
        float itemY = this.y + ITEM_OFFSET_Y;
        drawContext.drawClientRect(this.x, this.y, NEW_WIDTH, NEW_HEIGHT, 1.0f, 0.0f, 7.0f, 8.0f);
        drawContext.drawItem(Items.TOTEM_OF_UNDYING, itemX, itemY, 1.0f);
        drawContext.drawCenteredText(font, text, itemX + 8.0f + TEXT_NUDGE, itemY + 14.0f,
            ColorPalette.getPrimaryTextColor());
    }

    private void renderOld(RockstarDrawContext drawContext, int count) {
        FontMetrics font = Font.BOLD.metrics(7.0f);
        String text = String.valueOf(count);
        float itemX = this.x + ITEM_OFFSET_X;
        float itemY = this.y + 3.0f;
        drawContext.drawItem(Items.TOTEM_OF_UNDYING, itemX, itemY, 1.0f);
        drawContext.drawCenteredText(font, text, itemX + 16.1f, itemY + 12.3f,
            ColorPalette.getPrimaryTextColor());
    }

    @Override
    public boolean show() {
        return MinecraftClient.getInstance().currentScreen instanceof ChatScreen
            || (this.isAutoTotemEnabled() && this.totemCount() > 0);
    }

    private int displayCount() {
        int count = this.totemCount();
        if (count > 0) {
            return count;
        }
        return MinecraftClient.getInstance().currentScreen instanceof ChatScreen ? 1 : 0;
    }

    private int totemCount() {
        if (MinecraftClient.getInstance().player == null) {
            return 0;
        }
        return ItemRuleSets.getInventoryRules()
            .combineRules(ItemRuleSets.getHotbarRules())
            .combineRules(ItemRuleSets.getOffhandRules())
            .combineRules(ItemRuleSets.getArmorRules())
            .findAllByItem(Items.TOTEM_OF_UNDYING).size();
    }

    private boolean isAutoTotemEnabled() {
        AutoTotem autoTotem = RockstarClient.create().getModuleRegistry().getModule(AutoTotem.class);
        return autoTotem != null && autoTotem.isEnabled();
    }
}
