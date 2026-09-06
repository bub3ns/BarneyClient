/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ItemConvertible
 *  net.minecraft.Text
 */
package moscow.rockstar.modules.player.farming.core;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.modules.player.farming.core.AutoFarm;
import moscow.rockstar.modules.player.farming.core.FarmState;
import moscow.rockstar.modules.player.farming.hud.FarmDisplayCategory;
import moscow.rockstar.modules.player.farming.hud.FarmHudMetrics;
import moscow.rockstar.modules.player.farming.hud.FarmMetric;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemConvertible;
import net.minecraft.text.Text;

public abstract class FarmModeBase
extends ModeSetting.Option
implements ClientAccess {
    protected final AutoFarm autoFarm;

    public FarmModeBase(AutoFarm autoFarm, ModeSetting modeSetting, String string) {
        super(modeSetting, string);
        this.autoFarm = autoFarm;
    }

    public void startFarmAutomation() {
    }

    public void resetBrewingState() {
    }

    public void onModeSelected() {
    }

    public FarmState getFarmState() {
        return FarmState.WORKING;
    }

    public String getDisplayName() {
        return null;
    }

    public FarmDisplayCategory getFarmDisplayCategory() {
        return FarmDisplayCategory.BLOCKS;
    }

    public FarmMetric getDisplayMetric() {
        return this.getFarmDisplayCategory() == FarmDisplayCategory.BLOCKS ? FarmMetric.BLOCK_COUNT : FarmMetric.PROFIT_CHANGE;
    }

    public ItemStack getFarmDisplayItem() {
        return new ItemStack((ItemConvertible)Items.WHEAT);
    }

    protected final FarmHudMetrics getFarmMetrics() {
        return this.autoFarm.getFarmStateManager();
    }

    protected final void disableAutoFarm() {
        this.autoFarm.disable();
    }

    protected final void showError(String string) {
        Notification.error(Text.of((String)Localization.translate(string)));
    }

    protected final void showWarning(String string) {
        Notification.warning(Text.of((String)Localization.translate(string)));
    }

    protected final void showInfo(String string) {
        Notification.info(Text.of((String)Localization.translate(string)));
    }
}

