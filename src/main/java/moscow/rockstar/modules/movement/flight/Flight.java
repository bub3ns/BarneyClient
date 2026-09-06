/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.StatusEffects
 *  net.minecraft.PlayerAbilities
 *  net.minecraft.ArmorItem
 *  net.minecraft.ArmorMaterial
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.Vec3d
 *  net.minecraft.Text
 *  net.minecraft.Packet
 *  net.minecraft.PlayerPositionLookS2CPacket
 *  net.minecraft.PlayerMoveC2SPacket
 *  net.minecraft.PlayerMoveC2SPacket$OnGroundOnly
 *  net.minecraft.MathHelper
 *  net.minecraft.RegistryKey
 *  net.minecraft.ClientPlayerEntity
 *  net.minecraft.EquipmentType
 */
package moscow.rockstar.modules.movement.flight;

import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.items.rules.ArmorSlotRule;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.api.access.ArmorItemAccess;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.equipment.EquipmentType;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.network.SendPacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.player.EventMotion;
import pyrock.events.player.EventOnTravelPost;
import pyrock.events.player.InputEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Flight", category=ModuleCategory.MOVEMENT)
public class Flight
extends Module {
    private ModeSetting flightModeSetting;
    private ModeSetting.Option vanillaModeOption;
    private ModeSetting.Option elytraModeOption;
    private ModeSetting.Option elytraExploitModeOption;
    private NumberSetting flightSpeedSetting;
    private boolean flightAbilitiesModified;
    private boolean previousFlying;
    private boolean previousAllowFlying;
    private float previousFlySpeed;
    private int packetCorrectionDelay;
    private boolean correctionPacketReceived;
    private boolean sendingFlightPacket;
    private boolean fireworkSwapInProgress;
    private final ElytraItemSwapController elytraItemSwapController = new ElytraItemSwapController();
    private final EventListener<EventMotion> elytraMotionListener = eventMotion -> {
        if (this.elytraModeOption.isSelected()) {
            RockstarClient.create().getRotationManager().requestRotation(new Rotation(Flight.minecraftClient.player.getYaw(), 0.0f), RotationCorrectionMode.DIRECT, 180.0f, 180.0f, 180.0f, RotationPriority.MAXIMUM_PRIORITY);
            ClientPlayerEntity class_7462 = Flight.minecraftClient.player;
            if (class_7462 != null && class_7462.isAlive() && class_7462.isGliding()) {
                class_7462.setVelocity(class_7462.getVelocity().x, class_7462.getVelocity().y + 0.0305, class_7462.getVelocity().z);
            }
        }
    };
    private final EventListener<InputEvent> elytraInputListener = inputEvent -> {
        if (this.elytraModeOption.isSelected()) {
            if (InventoryUtils.chestplateRule().getItem() != Items.ELYTRA) {
                return;
            }
            if (Flight.minecraftClient.player.isInFluid()) {
                return;
            }
            inputEvent.setJump(Flight.minecraftClient.player.age % 2 == 0);
        }
    };
    private final EventListener<ReceivePacketEvent> correctionPacketListener = receivePacketEvent -> {
        if (!this.elytraExploitModeOption.isSelected()) {
            return;
        }
        if (receivePacketEvent.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.packetCorrectionDelay = 2;
            this.correctionPacketReceived = true;
        }
    };
    private final EventListener<SendPacketEvent> movementPacketListener = sendPacketEvent -> {
        if (!this.elytraExploitModeOption.isSelected() || this.sendingFlightPacket) {
            return;
        }
        if (!(sendPacketEvent.getPacket() instanceof PlayerMoveC2SPacket)) {
            return;
        }
        if (Flight.minecraftClient.player != null && Flight.minecraftClient.player.isGliding() && this.packetCorrectionDelay == 0 && !this.correctionPacketReceived) {
            this.sendingFlightPacket = true;
            try {
                Flight.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.OnGroundOnly(true, true));
            }
            finally {
                this.sendingFlightPacket = false;
            }
            sendPacketEvent.cancel();
        }
        this.correctionPacketReceived = false;
    };
    private final EventListener<ClientPlayerTickEvent> correctionTickListener = clientPlayerTickEvent -> {
        if (this.elytraExploitModeOption.isSelected() && this.packetCorrectionDelay > 0) {
            --this.packetCorrectionDelay;
        }
    };
    private final EventListener<EventOnTravelPost> elytraTravelListener = eventOnTravelPost -> {
        double d;
        if (!this.elytraExploitModeOption.isSelected() || Flight.minecraftClient.player == null) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = Flight.minecraftClient.player.getVelocity();
        Vec3d WallPlayerSkullBlock = Flight.minecraftClient.player.getRotationVector();
        float f = Flight.minecraftClient.player.getPitch() * ((float)Math.PI / 180);
        double d2 = Math.sqrt(WallPlayerSkullBlock.x * WallPlayerSkullBlock.x + WallPlayerSkullBlock.z * WallPlayerSkullBlock.z);
        double d3 = VanillaChestLootTableGenerator.horizontalLength();
        boolean bl = Flight.minecraftClient.player.getVelocity().y <= 0.0;
        double d4 = bl && Flight.minecraftClient.player.hasStatusEffect(StatusEffects.SLOW_FALLING) ? Math.min(Flight.minecraftClient.player.getFinalGravity(), 0.01) : Flight.minecraftClient.player.getFinalGravity();
        double d5 = MathHelper.square((double)Math.cos(f));
        VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add(0.0, d4 * (-1.0 + d5 * 0.75), 0.0);
        if (VanillaChestLootTableGenerator.y < 0.0 && d2 > 0.0) {
            d = VanillaChestLootTableGenerator.y * -0.1 * d5;
            VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock.x * d / d2, d, WallPlayerSkullBlock.z * d / d2);
        }
        if (f < 0.0f && d2 > 0.0) {
            d = d3 * (double)(-MathHelper.sin((float)f)) * (double)0.04f;
            VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add(-WallPlayerSkullBlock.x * d / d2, d * 3.2, -WallPlayerSkullBlock.z * d / d2);
        }
        if (d2 > 0.0) {
            VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add((WallPlayerSkullBlock.x / d2 * d3 - VanillaChestLootTableGenerator.x) * 0.1, 0.0, (WallPlayerSkullBlock.z / d2 * d3 - VanillaChestLootTableGenerator.z) * 0.1);
        }
        d = Math.toRadians(Flight.minecraftClient.player.getYaw());
        double d6 = -Math.sin(d);
        double d7 = Math.cos(d);
        if (this.packetCorrectionDelay >= 1) {
            double d8 = 0.09f;
            eventOnTravelPost.setOldVelocity(VanillaChestLootTableGenerator.multiply((double)0.99f, (double)0.98f, (double)0.99f).add(d6 * d8, (double)0.03f, d7 * d8));
        } else {
            eventOnTravelPost.setOldVelocity(VanillaChestLootTableGenerator.multiply((double)0.3f, (double)0.3f, (double)0.3f));
        }
    };

    public Flight() {
        this.initializeFlightSettings();
    }

    @Compile(obfuscation=4)
    private void initializeFlightSettings() {
        this.flightModeSetting = new ModeSetting(this, "modules.settings.flight.mode");
        this.vanillaModeOption = new ModeSetting.Option(this.flightModeSetting, "modules.settings.flight.vanilla");
        this.elytraModeOption = new ModeSetting.Option(this.flightModeSetting, "modules.settings.flight.elytra_y");
        this.elytraExploitModeOption = new ModeSetting.Option(this.flightModeSetting, "modules.settings.flight.elytra_exploit");
        this.flightSpeedSetting = new NumberSetting((SettingOwner)this, "modules.settings.flight.speed", () -> !this.vanillaModeOption.isSelected()).setValue(1.0f).setMinValue(0.1f).setMaxValue(10.0f).setStep(0.1f);
    }

    @Override
    public final void onEnable() {
        this.fireworkSwapInProgress = false;
        this.resetFlightPacketState();
        if (this.elytraModeOption.isSelected() && Flight.minecraftClient.player != null) {
            if (!Flight.minecraftClient.player.isOnGround()) {
                this.toggle();
                Notification.info(Text.of((String)"\u041d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e \u0432\u043a\u043b\u044e\u0447\u0430\u0442\u044c \u043d\u0430 \u0437\u0435\u043c\u043b\u0435"));
                return;
            }
            if (Flight.minecraftClient.player.getVelocity().length() > (double)0.1f) {
                this.toggle();
                Notification.info(Text.of((String)"\u041d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e \u0441\u0442\u043e\u044f\u0442\u044c"));
                return;
            }
            this.beginFireworkSwap();
        }
        super.onEnable();
    }

    @Override
    @Compile
    public final void onTick() {
        if (Flight.minecraftClient.player == null || Flight.minecraftClient.world == null) {
            return;
        }
        if (this.vanillaModeOption.isSelected()) {
            this.applyFlightAbilities(Flight.minecraftClient.player);
            return;
        }
        this.restoreFlightAbilities();
    }

    @Override
    public final void onDisable() {
        this.restoreFlightAbilities();
        this.resetFlightPacketState();
        if (this.fireworkSwapInProgress && Flight.minecraftClient.player != null) {
            this.restoreFireworkSlot();
        }
        this.fireworkSwapInProgress = false;
        super.onDisable();
    }

    private void beginFireworkSwap() {
        ArmorSlotRule armorSlotRule = InventoryUtils.chestplateRule();
        if (armorSlotRule.getItem() == Items.ELYTRA) {
            return;
        }
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getHotbarRules().combineRules(ItemRuleSets.getInventoryRules()).combineRules(ItemRuleSets.getOffhandRules());
        ItemRule itemRule = itemRuleCollection.findByStack(class_17992 -> class_17992.getItem() == Items.ELYTRA && !class_17992.willBreakNextUse());
        if (itemRule == null) {
            return;
        }
        this.fireworkSwapInProgress = true;
        this.elytraItemSwapController.beginItemSwap(itemRule, armorSlotRule);
    }

    private void restoreFireworkSlot() {
        ArmorSlotRule armorSlotRule = InventoryUtils.chestplateRule();
        if (armorSlotRule.getItem() != Items.ELYTRA) {
            return;
        }
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getHotbarRules().combineRules(ItemRuleSets.getInventoryRules()).combineRules(ItemRuleSets.getOffhandRules());
        ItemRule itemRule = Flight.findBestFireworkRule(itemRuleCollection);
        if (itemRule == null) {
            return;
        }
        this.elytraItemSwapController.beginItemSwap(itemRule, armorSlotRule);
    }

    private static ItemRule findBestFireworkRule(ItemRuleCollection<ItemRule> itemRuleCollection) {
        ItemRule itemRule = null;
        int n = Integer.MIN_VALUE;
        for (ItemRule itemRule2 : itemRuleCollection.getRules()) {
            int n2;
            ArmorItem class_17382;
            Item class_17922;
            ItemStack class_17992 = itemRule2.getItemStack();
            if (class_17992.isEmpty() || !((class_17922 = class_17992.getItem()) instanceof ArmorItem) || ((ArmorItemAccess)(class_17382 = (ArmorItem)class_17922)).rockstar$getType() != EquipmentType.CHESTPLATE || (n2 = Flight.calculateFireworkPriority(class_17382, class_17992)) <= n) continue;
            n = n2;
            itemRule = itemRule2;
        }
        return itemRule;
    }

    private static int calculateFireworkPriority(ArmorItem class_17382, ItemStack class_17992) {
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        if (donorItem != null && "SunHelmet".equals(donorItem.getRawName())) {
            return Integer.MAX_VALUE;
        }
        ArmorMaterial class_17412 = ((ArmorItemAccess)class_17382).rockstar$getMaterial();
        EquipmentType class_80512 = ((ArmorItemAccess)class_17382).rockstar$getType();
        int n = class_17412.defense().getOrDefault(class_80512, 0);
        int n2 = (int)class_17412.toughness();
        int n3 = EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.PROTECTION);
        return n * 5 + n3 * 3 + n2;
    }

    private void applyFlightAbilities(ClientPlayerEntity class_7462) {
        PlayerAbilities class_16562 = class_7462.getAbilities();
        if (!this.flightAbilitiesModified) {
            this.previousFlying = class_16562.allowFlying;
            this.previousAllowFlying = class_16562.flying;
            this.previousFlySpeed = class_16562.getFlySpeed();
            this.flightAbilitiesModified = true;
        }
        if (!class_16562.allowFlying) {
            class_16562.allowFlying = true;
        }
        if (!class_16562.flying) {
            class_16562.flying = true;
        }
        float f = Math.clamp(0.05f * this.flightSpeedSetting.getValue(), 0.0f, 1.0f);
        if (Math.abs(class_16562.getFlySpeed() - f) > 1.0E-4f) {
            class_16562.setFlySpeed(f);
        }
    }

    private void restoreFlightAbilities() {
        if (!this.flightAbilitiesModified) {
            return;
        }
        this.flightAbilitiesModified = false;
        if (Flight.minecraftClient.player == null) {
            return;
        }
        PlayerAbilities class_16562 = Flight.minecraftClient.player.getAbilities();
        if (!Flight.minecraftClient.player.isCreative() && !Flight.minecraftClient.player.isSpectator()) {
            class_16562.allowFlying = this.previousFlying;
            class_16562.flying = this.previousAllowFlying;
        }
        class_16562.setFlySpeed(this.previousFlySpeed);
    }

    private void resetFlightPacketState() {
        this.packetCorrectionDelay = 0;
        this.correctionPacketReceived = false;
        this.sendingFlightPacket = false;
    }

    static class ElytraItemSwapController
    implements ClientAccess {
        private ElytraItemSwapRequest swapRequest;
        private boolean swapInProgress;
        private final EventListener<ClientPlayerTickEvent> tickListener = clientPlayerTickEvent -> this.advanceItemSwap();

        ElytraItemSwapController() {
        }

        void beginItemSwap(ItemRule itemRule, ItemRule itemRule2) {
            this.swapRequest = new ElytraItemSwapRequest(itemRule, itemRule2);
            if (!this.swapInProgress) {
                RockstarClient.create().getEventBus().registerListeners(this);
                this.swapInProgress = true;
            }
        }

        private void advanceItemSwap() {
            if (ElytraItemSwapController.minecraftClient.player == null) {
                this.swapRequest = null;
                this.finishItemSwap();
                return;
            }
            if (this.swapRequest == null) {
                this.finishItemSwap();
                return;
            }
            InventoryMove inventoryMove = RockstarClient.create().getModuleRegistry().getModule(InventoryMove.class);
            if (this.swapRequest.sourceItemRule.getClickSlot() >= 36 && this.swapRequest.sourceItemRule.getClickSlot() <= 44) {
                InventoryUtils.dropItem(this.swapRequest.targetItemRule.getClickSlot(), this.swapRequest.sourceItemRule.getClickSlot() - 36);
                this.swapRequest = null;
            } else if (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)) {
                if (this.swapRequest.swapStep == 0 && inventoryMove.getModeEntries().isEmpty()) {
                    InventoryUtils.dropItem(this.swapRequest.sourceItemRule.getClickSlot(), 8);
                    InventoryUtils.dropItem(this.swapRequest.targetItemRule.getClickSlot(), 8);
                    InventoryUtils.dropItem(this.swapRequest.sourceItemRule.getClickSlot(), 8);
                    ++this.swapRequest.swapStep;
                } else if (this.swapRequest.swapStep == 1 && inventoryMove.getModeEntries().isEmpty()) {
                    ++this.swapRequest.swapStep;
                } else if (this.swapRequest.swapStep == 2 && inventoryMove.getModeEntries().isEmpty()) {
                    ++this.swapRequest.swapStep;
                }
            } else if (this.swapRequest.swapStep == 0 && inventoryMove.getModeEntries().isEmpty()) {
                InventoryUtils.dropItem(this.swapRequest.sourceItemRule.getClickSlot(), 8);
                ++this.swapRequest.swapStep;
            } else if (this.swapRequest.swapStep == 1 && inventoryMove.getModeEntries().isEmpty()) {
                InventoryUtils.dropItem(this.swapRequest.targetItemRule.getClickSlot(), 8);
                ++this.swapRequest.swapStep;
            } else if (this.swapRequest.swapStep == 2 && inventoryMove.getModeEntries().isEmpty()) {
                InventoryUtils.dropItem(this.swapRequest.sourceItemRule.getClickSlot(), 8);
                ++this.swapRequest.swapStep;
            }
            if (this.swapRequest != null && this.swapRequest.swapStep >= 3) {
                this.swapRequest = null;
            }
            if (this.swapRequest == null) {
                this.finishItemSwap();
            }
        }

        private void finishItemSwap() {
            if (this.swapInProgress) {
                RockstarClient.create().getEventBus().unregisterListeners(this);
                this.swapInProgress = false;
            }
        }
    }

    static class ElytraItemSwapRequest {
        int swapStep;
        final ItemRule sourceItemRule;
        final ItemRule targetItemRule;

        ElytraItemSwapRequest(ItemRule itemRule, ItemRule itemRule2) {
            this.sourceItemRule = itemRule;
            this.targetItemRule = itemRule2;
        }
    }
}
