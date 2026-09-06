/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.modules.player.automation.survival;

import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.mixin.minecraft.client.IMinecraftClient;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.NumberSetting;
import net.minecraft.component.DataComponentTypes;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Eat", category=ModuleCategory.PLAYER)
public class AutoEat
extends Module {
    private boolean eatingActive;
    private NumberSetting hungerThreshold;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if ((float)AutoEat.minecraftClient.player.getHungerManager().getFoodLevel() <= this.hungerThreshold.getValue()) {
            ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules());
            ItemRule itemRule = itemRuleCollection.findByStack(class_17992 -> class_17992.getItem().getDefaultStack().contains(DataComponentTypes.FOOD));
            if (!AutoEat.minecraftClient.player.getOffHandStack().contains(DataComponentTypes.FOOD) && itemRule != null) {
                itemRule.click();
            }
            this.eatingActive = true;
            if (AutoEat.minecraftClient.currentScreen != null && !AutoEat.minecraftClient.player.isUsingItem()) {
                ((IMinecraftClient)((Object)minecraftClient)).idoItemUse();
            } else {
                AutoEat.minecraftClient.options.useKey.setPressed(true);
            }
        } else if (this.eatingActive) {
            this.eatingActive = false;
            AutoEat.minecraftClient.options.useKey.setPressed(false);
        }
    };

    public AutoEat() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.hungerThreshold = new NumberSetting(this, "modules.settings.auto_eat.food").setStep(1.0f).setMinValue(1.0f).setMaxValue(20.0f).setValue(15.0f);
    }
}

