/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.StatusEffect
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.StatusEffects
 *  net.minecraft.PlayerEntity
 *  net.minecraft.SlotActionType
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.RegistryEntry
 */
package moscow.rockstar.modules.player.automation.survival;

import java.util.Map;
import java.util.TreeMap;
import lombok.Generated;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.recipes.RecipeItemResolver;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.items.rules.OffhandRule;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.BooleanSetting;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Invisible", category=ModuleCategory.PLAYER, description="modules.descriptions.auto_invisible")
public class AutoInvisible
extends Module {
    private final Map<String, StatusEffectInstance> savedPotionState = new TreeMap<String, StatusEffectInstance>();
    private boolean invisibilityActive;
    private BooleanSetting preDrink;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> this.resetInvisibility();

    public AutoInvisible() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.preDrink = new BooleanSetting(this, "modules.settings.auto_invisible.pre_drink");
    }

    @Compile(obfuscation=1)
    private void resetInvisibility() {
        boolean bl;
        boolean bl2 = AutoInvisible.minecraftClient.player.hasStatusEffect(StatusEffects.INVISIBILITY);
        StatusEffectInstance class_12932 = bl2 ? AutoInvisible.minecraftClient.player.getStatusEffect(StatusEffects.INVISIBILITY) : null;
        boolean bl3 = bl = !bl2;
        if (this.preDrink.isEnabled() && class_12932 != null && class_12932.getDuration() <= 200) {
            bl = true;
        }
        if (bl) {
            ItemStack class_17992 = AutoInvisible.minecraftClient.player.getOffHandStack();
            boolean bl4 = this.isItemValid(class_17992);
            ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules());
            ItemRule itemRule = itemRuleCollection.findByStack(this::isItemValid);
            OffhandRule offhandRule = new OffhandRule();
            if (itemRule != null && !bl4) {
                InventoryUtils.swapItemRules(itemRule, offhandRule);
            }
            if (bl4) {
                this.invisibilityActive = true;
                AutoInvisible.minecraftClient.options.useKey.setPressed(true);
            }
        } else if (this.invisibilityActive) {
            AutoInvisible.minecraftClient.options.useKey.setPressed(false);
            this.invisibilityActive = false;
            ItemStack class_17993 = AutoInvisible.minecraftClient.player.getOffHandStack();
            if (class_17993.getItem() == Items.GLASS_BOTTLE) {
                AutoInvisible.minecraftClient.interactionManager.clickSlot(0, 45, 1, SlotActionType.THROW, (PlayerEntity)AutoInvisible.minecraftClient.player);
            }
        }
    }

    private boolean isItemValid(ItemStack class_17992) {
        return RecipeItemResolver.containsEffect(class_17992, (RegistryEntry<StatusEffect>)StatusEffects.INVISIBILITY);
    }

    @Generated
    public Map<String, StatusEffectInstance> getSavedPotionState() {
        return this.savedPotionState;
    }
}

