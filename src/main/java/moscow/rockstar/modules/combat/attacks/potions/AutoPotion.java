/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.StatusEffect
 *  net.minecraft.StatusEffects
 *  net.minecraft.ItemStack
 *  net.minecraft.SplashPotionItem
 *  net.minecraft.Packet
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.PlayerInteractItemC2SPacket
 *  net.minecraft.RegistryEntry
 */
package moscow.rockstar.modules.combat.attacks.potions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.recipes.RecipeItemResolver;
import moscow.rockstar.items.rules.InventorySlotRule;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.attacks.potions.PotionCandidate;
import moscow.rockstar.settings.MultiBooleanSetting;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Potion", category=ModuleCategory.COMBAT, description="modules.descriptions.auto_potion")
public class AutoPotion
extends Module {
    private MultiBooleanSetting potions;
    private PotionCandidate strengthOption;
    private PotionCandidate speedOption;
    private PotionCandidate fireResistanceOption;
    private int ticksSincePotionCheck;
    private int rotationAlignedTicks;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (AutoPotion.minecraftClient.player == null || AutoPotion.minecraftClient.world == null || AutoPotion.minecraftClient.interactionManager == null || minecraftClient.getNetworkHandler() == null || AutoPotion.minecraftClient.player.isGliding()) {
            return;
        }
        ++this.ticksSincePotionCheck;
        List<ItemRule> list = this.collectPotionRules();
        if (this.ticksSincePotionCheck < 20 || list.isEmpty()) {
            this.rotationAlignedTicks = 0;
            return;
        }
        float f = AutoPotion.minecraftClient.player.getYaw();
        Rotation rotation = new Rotation(f, 90.0f);
        float f2 = ThreadLocalRandom.current().nextFloat(275.0f, 444.0f);
        RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, f2, f2, f2, RotationPriority.ITEM_USE_PRIORITY);
        if (RockstarClient.create().getRotationManager().getEffectiveRotation().angleDistanceTo(rotation) > 1.0f) {
            this.rotationAlignedTicks = 0;
            return;
        }
        if (this.rotationAlignedTicks++ < 1) {
            return;
        }
        int n2 = AutoPotion.minecraftClient.player.getInventory().selectedSlot;
        boolean bl = false;
        for (ItemRule itemRule : list) {
            if (!this.isPotionRuleEnabled(itemRule)) continue;
            if (itemRule instanceof HotbarSlot) {
                int n3;
                HotbarSlot hotbarSlot = (HotbarSlot)itemRule;
                AutoPotion.minecraftClient.player.getInventory().selectedSlot = n3 = hotbarSlot.getSlotIndex();
                minecraftClient.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(n3));
                ((moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)AutoPotion.minecraftClient.interactionManager).rockstar$sendSequencedPacket(AutoPotion.minecraftClient.world, n -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, n, f, 90.0f));
            } else {
                InventoryUtils.dropItem(itemRule.getClickSlot(), n2);
                minecraftClient.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(n2));
                ((moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)AutoPotion.minecraftClient.interactionManager).rockstar$sendSequencedPacket(AutoPotion.minecraftClient.world, n -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, n, f, 90.0f));
                InventoryUtils.dropItem(itemRule.getClickSlot(), n2);
            }
            bl = true;
        }
        AutoPotion.minecraftClient.player.getInventory().selectedSlot = n2;
        minecraftClient.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(n2));
        this.rotationAlignedTicks = 0;
        if (bl) {
            this.ticksSincePotionCheck = 0;
            RockstarClient.create().getRotationManager().setCurrentRotation(new Rotation(AutoPotion.minecraftClient.player.getYaw(), AutoPotion.minecraftClient.player.getPitch()));
        }
    };

    public AutoPotion() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.potions = new MultiBooleanSetting(this, "modules.settings.auto_potion.potions");
        this.strengthOption = new PotionCandidate(this.potions, "modules.settings.auto_potion.potions.strength", (RegistryEntry<StatusEffect>)StatusEffects.STRENGTH);
        this.strengthOption.select();
        this.speedOption = new PotionCandidate(this.potions, "modules.settings.auto_potion.potions.speed", (RegistryEntry<StatusEffect>)StatusEffects.SPEED);
        this.speedOption.select();
        this.fireResistanceOption = new PotionCandidate(this.potions, "modules.settings.auto_potion.potions.fire_resistance", (RegistryEntry<StatusEffect>)StatusEffects.FIRE_RESISTANCE);
        this.fireResistanceOption.select();
    }

    @Override
    public void onEnable() {
        this.ticksSincePotionCheck = 20;
        this.rotationAlignedTicks = 0;
    }

    @Override
    public void onDisable() {
        this.rotationAlignedTicks = 0;
        if (AutoPotion.minecraftClient.player != null) {
            RockstarClient.create().getRotationManager().setCurrentRotation(new Rotation(AutoPotion.minecraftClient.player.getYaw(), AutoPotion.minecraftClient.player.getPitch()));
        }
    }

    private List<ItemRule> collectPotionRules() {
        ArrayList<ItemRule> arrayList = new ArrayList<ItemRule>();
        Predicate<ItemStack> predicate = class_17992 -> !class_17992.isEmpty() && class_17992.getItem() instanceof SplashPotionItem;
        for (PotionCandidate potionCandidate : this.getSelectedPotionCandidates()) {
            RegistryEntry<StatusEffect> class_68802 = potionCandidate.potionType;
            if (AutoPotion.minecraftClient.player.hasStatusEffect(class_68802)) continue;
            Predicate<ItemStack> predicate2 = this.getPredicate(class_68802);
            HotbarSlot hotbarSlot = ItemRuleSets.getHotbarRules().findByStack(class_17992 -> predicate.test((ItemStack)class_17992) && predicate2.test((ItemStack)class_17992));
            if (hotbarSlot != null && this.isPotionRuleEnabled(hotbarSlot)) {
                arrayList.add(hotbarSlot);
                continue;
            }
            InventorySlotRule inventorySlotRule = ItemRuleSets.getInventoryRules().findByStack(class_17992 -> predicate.test((ItemStack)class_17992) && predicate2.test((ItemStack)class_17992));
            if (inventorySlotRule == null || !this.isPotionRuleEnabled(inventorySlotRule)) continue;
            arrayList.add(inventorySlotRule);
        }
        return arrayList;
    }

    private List<PotionCandidate> getSelectedPotionCandidates() {
        List<PotionCandidate> list = List.of(this.strengthOption, this.speedOption, this.fireResistanceOption);
        List<PotionCandidate> list2 = this.potions.getSelectedOptions().stream().map(PotionCandidate.class::cast).toList();
        ArrayList<PotionCandidate> arrayList = new ArrayList<PotionCandidate>();
        for (PotionCandidate potionCandidate : list) {
            if (!list2.contains(potionCandidate)) continue;
            arrayList.add(potionCandidate);
        }
        return arrayList;
    }

    private boolean isPotionRuleEnabled(ItemRule itemRule) {
        ItemStack class_17992 = itemRule.getItemStack();
        return !class_17992.isEmpty() && class_17992.getItem() instanceof SplashPotionItem;
    }

    private Predicate<ItemStack> getPredicate(RegistryEntry<StatusEffect> class_68802) {
        return class_17992 -> {
            if (class_17992.isEmpty() || !(class_17992.getItem() instanceof SplashPotionItem)) {
                return false;
            }
            return RecipeItemResolver.getEffects(class_17992).stream().anyMatch(class_12932 -> class_12932.getEffectType() == class_68802);
        };
    }
}
