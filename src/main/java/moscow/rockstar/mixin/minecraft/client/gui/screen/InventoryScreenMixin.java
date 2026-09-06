/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.RecipeBookScreen
 *  net.minecraft.PlayerEntity
 *  net.minecraft.PlayerInventory
 *  net.minecraft.SlotActionType
 *  net.minecraft.PlayerScreenHandler
 *  net.minecraft.AbstractRecipeScreenHandler
 *  net.minecraft.Text
 *  net.minecraft.ButtonWidget
 *  net.minecraft.InventoryScreen
 *  net.minecraft.RecipeBookWidget
 *  net.minecraft.StringVisitable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.ArmorSlotRules;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.mixin.accessors.ScreenAccessor;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.ui.localization.Localization;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.text.StringVisitable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={InventoryScreen.class})
public abstract class InventoryScreenMixin
extends RecipeBookScreen<PlayerScreenHandler>
implements ClientAccess {
    public InventoryScreenMixin(PlayerScreenHandler class_17232, RecipeBookWidget<?> class_5072, PlayerInventory class_16612, Text class_25612) {
        super(class_17232, class_5072, class_16612, class_25612);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void dropButton(CallbackInfo callbackInfo) {
        if (RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }
        Text class_25612 = Text.of((String)Localization.translate("inventory.button.drop_all"));
        Text class_25613 = Text.of((String)Localization.translate("\u041e\u0447\u0438\u0449\u0430\u0442\u044c \u0432\u0441\u0435"));
        int n = InventoryScreenMixin.minecraftClient.textRenderer.getWidth((StringVisitable)class_25612) + 20;
        int n2 = InventoryScreenMixin.minecraftClient.textRenderer.getWidth((StringVisitable)class_25612) + 20;
        int n3 = 80;
        int n4 = 200;
        int n5 = Math.max(n3, Math.min(n4, n));
        int n6 = Math.max(n3, Math.min(n4, n2));
        ButtonWidget class_41853 = ButtonWidget.builder((Text)class_25612, class_41852 -> this.dropAll()).dimensions(this.x + this.backgroundWidth / 2 - n5 / 2, this.y - 20, n5, 18).build();
        ButtonWidget class_41854 = ButtonWidget.builder((Text)class_25613, class_41852 -> this.clearAll()).dimensions(this.x + this.backgroundWidth / 2 - n6 / 2, this.y - 40, n6, 18).build();
        ((ScreenAccessor)((Object)this)).invokeAddDrawableChild(class_41853);
        ((ScreenAccessor)((Object)this)).invokeAddDrawableChild(class_41854);
    }

    @Unique
    private void dropAll() {
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules()).combineRules(ItemRuleSets.getOffhandRules()).combineRules(new ArmorSlotRules());
        for (ItemRule itemRule : itemRuleCollection.getRules()) {
            if (itemRule.isEmpty()) continue;
            InventoryScreenMixin.minecraftClient.interactionManager.clickSlot(InventoryScreenMixin.minecraftClient.player.currentScreenHandler.syncId, itemRule.getClickSlot(), 1, SlotActionType.THROW, (PlayerEntity)InventoryScreenMixin.minecraftClient.player);
        }
    }

    @Unique
    private void clearAll() {
        if (InventoryScreenMixin.minecraftClient.player == null || InventoryScreenMixin.minecraftClient.interactionManager == null) {
            return;
        }
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules()).combineRules(ItemRuleSets.getOffhandRules()).combineRules(ItemRuleSets.getArmorRules());
        int n = InventoryScreenMixin.minecraftClient.player.currentScreenHandler.syncId;
        for (ItemRule itemRule : itemRuleCollection.getRules()) {
            if (itemRule.isEmpty()) continue;
            if (ServerDetector.isInventoryServer()) {
                InventoryUtils.dropItem(itemRule.getClickSlot(), 45);
                continue;
            }
            InventoryScreenMixin.minecraftClient.interactionManager.clickSlot(n, itemRule.getClickSlot(), 1, SlotActionType.THROW, (PlayerEntity)InventoryScreenMixin.minecraftClient.player);
        }
    }
}
