/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.StatusEffect
 *  net.minecraft.StatusEffects
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.PlayerInventory
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Text
 *  net.minecraft.Packet
 *  net.minecraft.EntityS2CPacket
 *  net.minecraft.ResourcePackStatusC2SPacket
 *  net.minecraft.ResourcePackStatusC2SPacket$Status
 *  net.minecraft.OpenScreenS2CPacket
 *  net.minecraft.RegistryEntry
 *  net.minecraft.GameMessageS2CPacket
 *  net.minecraft.Registries
 *  net.minecraft.ClientCommonNetworkHandler$ConfirmServerResourcePackScreen
 */
package moscow.rockstar.modules.other.assist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.api.commands.NavigationCommandService;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.inventory.ItemSwapManager;
import moscow.rockstar.items.assist.AssistItemProvider;
import moscow.rockstar.items.assist.AssistItemProviderRegistry;
import moscow.rockstar.items.assist.providers.BackpackProvider;
import moscow.rockstar.items.assist.providers.BoomTrapProvider;
import moscow.rockstar.items.assist.providers.DisorientationProvider;
import moscow.rockstar.items.assist.providers.GodAuraProvider;
import moscow.rockstar.items.assist.providers.PilbProvider;
import moscow.rockstar.items.assist.providers.PlastProvider;
import moscow.rockstar.items.assist.providers.SnowballProvider;
import moscow.rockstar.items.assist.providers.StunProvider;
import moscow.rockstar.items.assist.providers.TrapkaProvider;
import moscow.rockstar.items.recipes.RecipeItemResolver;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.mixin.accessors.EntityS2CPacketAccessor;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.player.movement.camera.FreeCamera;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.ActionSetting;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.input.KeyBindingUtil;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.ui.notifications.NotificationType;
import moscow.rockstar.ui.screens.AssistScreen;
import moscow.rockstar.util.SupportProviderRegistry;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.registry.Registries;
import pyrock.events.game.AttackEvent;
import pyrock.events.game.EventSetCooldown;
import pyrock.events.game.FinishEatEvent;
import pyrock.events.game.SendMessageEvent;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.player.InputEvent;
import pyrock.events.render.HudRenderEvent;
import pyrock.events.window.KeyPressEvent;
import pyrock.events.window.MouseEvent;
import pyrock.utility.render.CustomDrawContext;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Assist", category=ModuleCategory.OTHER, description="modules.descriptions.assist")
public class Assist
extends Module {
    private final BooleanSupplier worldReadyCheck = () -> this.isAssistReady() && !ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD_VARIANTS);
    private final BooleanSupplier screenReadyCheck = () -> this.isAssistReady() && !ServerDetector.isServerProfileSupported(ServerProfile.SUPPORTED_NETWORKS);
    private final BooleanSupplier playerReadyCheck = () -> this.isAssistReady() && !ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD_VARIANTS);
    private ActionSetting openMacrosAction;
    private BooleanSetting showRadius;
    private BooleanSetting closeMenu;
    private BooleanSetting autoFix;
    private BooleanSetting chatFilter;
    private BooleanSetting reduceCooldown;
    private BooleanSetting warnArmor;
    private BooleanSetting dragonFly;
    private NumberSetting flySpeedXz;
    private NumberSetting flySpeedY;
    private BooleanSetting autoPiona;
    private BooleanSetting autoChorus;
    private BooleanSetting autoZako;
    private BooleanSetting autoStop;
    private BooleanSetting healHelper;
    private BooleanSetting ctCommands;
    private BooleanSetting autoRct;
    private BooleanSetting buffsLoop;
    private ModeSetting handMode;
    private ModeSetting.Option left;
    private ModeSetting.Option right;
    private ModeSetting.Option packet;
    private final List<AssistItemProvider> assistSettings = AssistItemProviderRegistry.createProviders();
    private final List<AssistItemProvider> assistItems = new ArrayList<AssistItemProvider>();
    private final Set<String> trackedItems = new HashSet<String>(Arrays.asList("\u0430\u043a\u0440\u0438\u0435\u043d(\u0430|\u0443|\u043e\u043c|\u0435|\u0447\u0438\u043a)?", "\u0440\u0438\u0447(\u0430|\u0443|\u043e\u043c|\u0435\u0439|\u0435)?", "\u043d\u044c\u044e\u043a\u043e\u0434(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438|\u0438\u043a|\u0435)?", "\u044d\u043a\u0441\u043f\u0435\u043d\u0441\u0438\u0432(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438|\u0435)?", "\u0438\u043c\u043f\u0430\u043a\u0442(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438|\u0438\u043a|\u0435)?", "\u044d\u043a\u0441\u0435\u043b\u043b\u0435\u043d\u0442(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438|\u0438\u043a|\u0435)?", "\u044d\u043a\u0441\u0435\u043b\u0435\u043d\u0442(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438|\u0438\u043a)?", "\u043a\u0430\u0442\u043b\u0430\u0432\u0430\u043d(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438|\u0447\u0438\u043a)?", "\u043a\u0430\u0442\u043b\u043e\u0432\u0430\u043d(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438|\u0447\u0438\u043a)?", "\u0446\u0435\u043b\u0435\u0441\u0442\u0438\u0430\u043b(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438|\u0435)?", "\u0446\u0435\u043b\u043a(\u043e\u0439|\u0430|\u0443|\u0430\u043c\u0438|\u043e\u0447\u043a\u0430|\u0435)?", "\u043c\u0430\u0442\u0438\u043a\u0441(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438|\u0435)?", "\u0438\u043d\u0435\u0440\u0442\u0438(\u044f|\u0435\u0439|\u044e|\u044f\u043c\u0438|\u0435)?", "\u044d\u043a\u0441\u043f(\u0430|\u043e\u0439|\u043e\u044e|\u0443|\u0443\u043b\u0438\u0447\u043a\u0430|\u0435)?", "\u0444\u043b\u044e\u0433\u0435\u0440(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438)?", "\u0440\u0438\u043a\u0435\u0440(\u0430|\u0443|\u043e\u043c|\u043e\u0447\u0435\u043a)?", "\u0444\u0430\u043d\u043f\u0435(\u0439|\u044e|\u044f|\u0435\u043c|\u0435|\u0439\u0447\u0438\u043a)?", "\u0432\u0435\u043a\u0441\u0430\u0439\u0434(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438|\u0438\u043a|\u0435)?", "\u043d\u0443\u0440\u0441\u0443\u043b\u0442\u0430\u043d(\u0430|\u0443|\u0435|\u043e\u043c|\u0447\u0438\u043a)?", "\u043d\u0443\u0440\u0438\u043a(\u0430|\u0443|\u043e\u043c|\u0435)?", "\u043d\u0443\u0440\u043b\u0430\u043d(\u0430|\u0443|\u043e\u043c|\u0447\u0438\u043a|\u0435)?", "\u0432\u0435\u043a\u0441(\u043e\u043c|\u0443|\u0430|\u0430\u043c\u0438|\u0438\u043a|\u0435)?", "\u0440\u0435\u043b\u0435\u0439\u043a(\u043e\u043c|\u0443|\u0430|\u0430\u043c\u0438|\u0435)?", "\u0430\u0440\u0431\u0443\u0437(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438|\u0438\u043a|\u0435)?", "\u0432\u0438\u043b\u0434(\u043e\u043c|\u0443|\u0430|\u0430\u043c\u0438|\u0438\u043a|\u0435)?", "\u0444\u0430\u043d\u0442\u0430\u0439\u043c(\u0435|\u0430|\u0443)?", "\u0445\u043e\u043b\u0438\u043a(\u0435|\u0430|\u0443)?", "\u0445\u043e\u043b\u0438\u0432\u043e\u0440\u043b\u0434(\u0430|\u0443|\u0435)?", "\u0440\u043e\u043a\u0441\u0442\u0430\u0440(\u043e\u043c|\u0430|\u0443|\u0430\u043c\u0438|\u0447\u0438\u043a|\u0435)?", "\u0440\u043e\u0433\u0430\u043b\u0438\u043a(\u0430|\u0443|\u043e\u043c|\u0435)?", "\u0442\u0430\u043d\u0434\u0435\u0440\u0445\u0430\u043a(\u043e\u043c|\u0443|\u0438|\u0430\u043c\u0438|\u0430|\u0435)?", "\u043b\u0438\u043a\u0432\u0438\u0434\u0431\u0430\u0443\u043d\u0441(\u0430|\u0443|\u0430\u043c\u0438|\u0435)?", "expensive", "celestial", "newcode", "arbuz", "akrien", "nursultan", "relake", "wild", "wurst", "catlovan", "excellent", "rockstar", "catlavan", "impact", "matix", "inertia", "wex", "wexside", "nurik", "nurlan", "rich", "funpay", "fluger", "riker", "funtime", "holyworld", "wwe", "hvh", "rogalik", "thunderhack", "liquidbounce"));
    private final List<Pattern> pendingActions = new ArrayList<Pattern>();
    private final Timer cooldownTimer;
    private final Timer actionTimer;
    private final Timer targetTimer;
    private final Timer updateTimer;
    private final Timer resetTimer;
    private final Timer delayTimer;
    private boolean assistActive;
    private boolean overlayVisible;
    private boolean privilegedMode;
    private boolean disableLocked;
    private int buffCycleStep;
    private boolean alwaysEnabled;
    private boolean mouseOverride;
    private int sourceItemSlot;
    private HotbarSlot selectedSlot;
    private boolean inventoryOpen;
    private boolean chatFiltering;
    private final Set<Integer> processedItems;
    private final EventListener<MouseEvent> onMouseEvent;
    private final EventListener<KeyPressEvent> onKeyPressEvent;
    private final EventListener<EventSetCooldown> onEventSetCooldownListener;
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent;
    private final EventListener<SendMessageEvent> onSendMessageEvent;
    private final EventListener<AttackEvent> onAttackEvent;
    private final EventListener<FinishEatEvent> onFinishEatEvent;
    private final EventListener<FinishEatEvent> onFinishEatEvent1;
    private final EventListener<InputEvent> onInputEvent;
    private ItemRule itemRule;
    private boolean foodActionActive;
    private boolean actionPending;
    private final Timer animationTimer;
    private final EventListener<HudRenderEvent> onHudRenderEvent;
    private final EventListener<FinishEatEvent> onFinishEatEvent2;

    public Assist() {
        for (String string : this.trackedItems) {
            try {
                this.pendingActions.add(Pattern.compile("\\b" + string + "\\b", 322));
            }
            catch (Exception exception) {
                this.pendingActions.add(Pattern.compile(Pattern.quote(string), 258));
            }
        }
        this.cooldownTimer = new Timer();
        this.actionTimer = new Timer();
        this.targetTimer = new Timer();
        this.updateTimer = new Timer();
        this.resetTimer = new Timer();
        this.delayTimer = new Timer();
        this.overlayVisible = true;
        this.privilegedMode = true;
        this.sourceItemSlot = -1;
        this.processedItems = new HashSet<Integer>();
        this.onMouseEvent = mouseEvent -> {
            if (mouseEvent.getAction() != 1) {
                return;
            }
            if (Assist.minecraftClient.currentScreen != null) {
                return;
            }
            this.selectAssistMode(mouseEvent.getButton());
        };
        this.onKeyPressEvent = keyPressEvent -> {
            if (keyPressEvent.getAction() != 1) {
                return;
            }
            if (Assist.minecraftClient.currentScreen != null) {
                return;
            }
            this.selectAssistMode(keyPressEvent.getKey());
        };
        this.onEventSetCooldownListener = eventSetCooldown -> {
            Item class_17922;
            if (this.reduceCooldown.isEnabled() && ((class_17922 = (Item)Registries.ITEM.get(eventSetCooldown.getCooldownGroup())) == Items.ENCHANTED_GOLDEN_APPLE || class_17922 == Items.GOLDEN_APPLE || class_17922 == Items.POTION || class_17922 == Items.CHORUS_FRUIT)) {
                eventSetCooldown.setCooldown(eventSetCooldown.getCooldown() - 32);
            }
        };
        this.onReceivePacketEvent = receivePacketEvent -> {
            GameMessageS2CPacket class_74392;
            Object object;
            if (this.autoPiona.isEnabled() && (object = receivePacketEvent.getPacket()) instanceof GameMessageS2CPacket && (((String)(object = (class_74392 = (GameMessageS2CPacket)object).content().getString().toLowerCase())).contains("10,000 \u0431\u044b\u043b\u043e \u043d\u0430\u0447\u0438\u0441\u043b\u0435\u043d\u043e \u0432\u0430\u043c") || ((String)object).contains("\u043f\u043e\u0432\u0442\u043e\u0440\u0438\u0442\u0435 \u0442\u0435\u043a\u0441\u0442 \u0435\u0449\u0435 \u0440\u0430\u0437"))) {
                this.privilegedMode = false;
                this.cooldownTimer.reset();
            }
            if (this.autoZako.isEnabled() && (object = receivePacketEvent.getPacket()) instanceof GameMessageS2CPacket) {
                class_74392 = (GameMessageS2CPacket)object;
                if (((String)(object = class_74392.content().getString())).contains("\u0412\u044b \u0443\u0436\u0435 \u0430\u043a\u0442\u0438\u0432\u0438\u0440\u043e\u0432\u0430\u043b\u0438 \u044d\u0442\u043e\u0442 \u043f\u0440\u043e\u043c\u043e\u043a\u043e\u0434")) {
                    this.overlayVisible = false;
                    this.cooldownTimer.reset();
                } else if (((String)object).contains("\u041f\u0440\u044f\u043c\u043e \u0441\u0435\u0439\u0447\u0430\u0441 \u0438\u0434\u0435\u0442 \u043d\u0430\u0431\u043e\u0440")) {
                    this.overlayVisible = true;
                }
            }
            if (this.autoStop.isEnabled() && (object = receivePacketEvent.getPacket()) instanceof GameMessageS2CPacket && ((String)(object = (class_74392 = (GameMessageS2CPacket)object).content().getString())).contains("\u041d\u0435 \u0434\u0432\u0438\u0433\u0430\u0439\u0442\u0435\u0441\u044c")) {
                this.assistActive = true;
                this.actionTimer.reset();
                System.out.println("stop");
            }
            if (this.closeMenu.isEnabled() && (object = receivePacketEvent.getPacket()) instanceof OpenScreenS2CPacket) {
                OpenScreenS2CPacket screenPacket = (OpenScreenS2CPacket)object;
                String screenTitle = screenPacket.getName().getString();
                if (screenTitle.contains("\u041c\u0435\u043d\u044e") || screenTitle.contains("\ua201\ua000\ua202\ua301\ua202\ua001")) {
                    Assist.minecraftClient.player.closeScreen();
                    receivePacketEvent.cancel();
                }
            }
            if (this.autoChorus.isEnabled() && this.alwaysEnabled && (object = receivePacketEvent.getPacket()) instanceof EntityS2CPacket) {
                EntityS2CPacket entityPacket = (EntityS2CPacket)object;
                LivingEntity targetEntity = RockstarClient.create().getFriendManager().getTargetLivingEntity();
                if (targetEntity != null && ((EntityS2CPacketAccessor)entityPacket).getId() == targetEntity.getId()) {
                    this.resetTimer.reset();
                }
            }
        };
        this.onSendMessageEvent = sendMessageEvent -> {
            if (this.isChatMessageAllowed(sendMessageEvent.getMessage())) {
                sendMessageEvent.cancel();
                Notification.error(Text.of((String)Localization.translate("assist.ct_command_blocked")));
                return;
            }
            if (!this.chatFilter.isEnabled()) {
                return;
            }
            for (Pattern pattern : this.pendingActions) {
                if (!pattern.matcher(sendMessageEvent.getMessage()).find()) continue;
                sendMessageEvent.cancel();
                Notification.error(Text.of((String)"\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u043d\u0435 \u0431\u044b\u043b\u043e \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u043e \u0442.\u043a. \u0432 \u043d\u0435\u043c \u043f\u0440\u0438\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043d\u043e\u0435 \u0441\u043b\u043e\u0432\u043e"));
                return;
            }
        };
        this.onAttackEvent = attackEvent -> {
            if (!this.autoChorus.isEnabled() || !this.alwaysEnabled) {
                return;
            }
            attackEvent.cancel();
        };
        this.onFinishEatEvent = finishEatEvent -> {
            if (!this.autoChorus.isEnabled()) {
                return;
            }
            if (!this.alwaysEnabled) {
                return;
            }
            if (finishEatEvent.getUser() != Assist.minecraftClient.player) {
                return;
            }
            if (finishEatEvent.getStack().getItem() != Items.CHORUS_FRUIT) {
                return;
            }
            this.resetFoodState();
        };
        this.onFinishEatEvent1 = finishEatEvent -> {
            if (!this.autoRct.isEnabled() || !ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD) || ServerDetector.enabled) {
                return;
            }
            if (finishEatEvent.getUser() != Assist.minecraftClient.player) {
                return;
            }
            if (!finishEatEvent.getStack().isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
                return;
            }
            NavigationCommandService navigationCommandService = RockstarClient.create().getNavigationCommandService();
            if (navigationCommandService == null) {
                return;
            }
            navigationCommandService.executeCommand(navigationCommandService.getCommandPrefix() + "rct");
        };
        this.onInputEvent = inputEvent -> {
            if (this.autoStop.isEnabled() && this.assistActive && !this.actionTimer.hasElapsed(4000L)) {
                inputEvent.setForward(0.0f);
                inputEvent.setJump(false);
                inputEvent.setStrafe(0.0f);
                inputEvent.setSprint(false);
            }
        };
        this.itemRule = null;
        this.foodActionActive = false;
        this.actionPending = false;
        this.animationTimer = new Timer();
        this.onHudRenderEvent = hudRenderEvent -> {
            if (!ServerDetector.enabled || !this.healHelper.isEnabled()) {
                return;
            }
            CustomDrawContext customDrawContext = hudRenderEvent.getContext();
            if (!Assist.minecraftClient.player.hasStatusEffect(StatusEffects.STRENGTH)) {
                ItemRenderUtils.highlightMatchingItem(customDrawContext, class_17992 -> class_17992.getItem() == Items.POTION && RecipeItemResolver.containsEffect(class_17992, (RegistryEntry<StatusEffect>)StatusEffects.STRENGTH), ColorPalette.getAccentColor().mulAlpha(0.85f));
            }
            if (Assist.minecraftClient.player.getHealth() + Assist.minecraftClient.player.getAbsorptionAmount() > 19.0f) {
                return;
            }
            if (this.animationTimer.hasElapsed(ServerDetector.isInventoryServer() ? 10000L : 20000L)) {
                ItemRenderUtils.highlightMatchingItem(customDrawContext, class_17992 -> class_17992.getItem() == Items.POTION && RecipeItemResolver.containsEffect(class_17992, (RegistryEntry<StatusEffect>)StatusEffects.INSTANT_HEALTH), ColorPalette.getAccentColor().mulAlpha(0.85f));
            } else if (Assist.minecraftClient.player.getHungerManager().getFoodLevel() < 20) {
                ItemRenderUtils.highlightItem(customDrawContext, Items.GOLDEN_CARROT, ColorPalette.getAccentColor().mulAlpha(0.85f));
            } else if (!Assist.minecraftClient.player.getItemCooldownManager().isCoolingDown(Items.GOLDEN_APPLE.getDefaultStack())) {
                ItemRenderUtils.highlightItem(customDrawContext, Items.GOLDEN_APPLE, ColorPalette.getAccentColor().mulAlpha(0.85f));
            } else {
                ItemRenderUtils.highlightItem(customDrawContext, Items.ENCHANTED_GOLDEN_APPLE, ColorPalette.getAccentColor().mulAlpha(0.85f));
            }
        };
        this.onFinishEatEvent2 = finishEatEvent -> {
            if (finishEatEvent.getUser() != Assist.minecraftClient.player) {
                return;
            }
            if (RecipeItemResolver.containsEffect(finishEatEvent.getStack(), (RegistryEntry<StatusEffect>)StatusEffects.INSTANT_HEALTH)) {
                this.animationTimer.reset();
            }
        };
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.openMacrosAction = new ActionSetting(this, "Open macros").withAction(() -> minecraftClient.setScreen(new AssistScreen()));
        this.showRadius = new BooleanSetting(this, "modules.settings.assist.show_radius").enable();
        this.closeMenu = new BooleanSetting(this, "modules.settings.assist.close_menu", "modules.settings.assist.close_menu.description", this.worldReadyCheck).enable();
        this.autoFix = new BooleanSetting((SettingOwner)this, "modules.settings.assist.auto_fix", this.worldReadyCheck);
        this.chatFilter = new BooleanSetting((SettingOwner)this, "modules.settings.assist.chat_filter", this.worldReadyCheck);
        this.reduceCooldown = new BooleanSetting(this, "modules.settings.assist.reduce_cooldown");
        this.warnArmor = new BooleanSetting((SettingOwner)this, "modules.settings.assist.warn_armor", "modules.settings.assist.warn_armor.description");
        this.dragonFly = new BooleanSetting((SettingOwner)this, "modules.settings.assist.dragon_fly", "modules.settings.assist.dragon_fly.description").enable();
        this.flySpeedXz = new NumberSetting((SettingOwner)this, "modules.settings.assist.fly_speed_xz", () -> !this.dragonFly.isEnabled()).setValue(1.0f).setMaxValue(5.0f).setMinValue(1.0f).setStep(0.5f).setValue(5.0f);
        this.flySpeedY = new NumberSetting((SettingOwner)this, "modules.settings.assist.fly_speed_y", () -> !this.dragonFly.isEnabled()).setValue(1.0f).setMaxValue(5.0f).setMinValue(1.0f).setStep(0.5f).setValue(5.0f);
        this.autoPiona = new BooleanSetting(this, "modules.settings.assist.auto_piona", "modules.settings.assist.auto_piona.description", this.screenReadyCheck).enable();
        this.autoChorus = new BooleanSetting(this, "modules.settings.assist.auto_chorus");
        this.autoZako = new BooleanSetting((SettingOwner)this, "modules.settings.assist.auto_zako", this.playerReadyCheck);
        this.autoStop = new BooleanSetting((SettingOwner)this, "modules.settings.assist.auto_stop", this.playerReadyCheck);
        this.healHelper = new BooleanSetting(this, "modules.settings.assist.heal_helper");
        this.ctCommands = new BooleanSetting((SettingOwner)this, "modules.settings.assist.ct_commands", "modules.settings.assist.ct_commands.description").enable();
        this.autoRct = new BooleanSetting((SettingOwner)this, "modules.settings.assist.auto_rct", this.playerReadyCheck);
        this.buffsLoop = new BooleanSetting(this, "modules.settings.assist.buffs_loop");
        this.handMode = new ModeSetting((SettingOwner)this, "modules.settings.assist.hand_mode", () -> ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME));
        this.left = new ModeSetting.Option(this.handMode, "modules.settings.assist.hand_mode.left");
        this.right = new ModeSetting.Option(this.handMode, "modules.settings.assist.hand_mode.right");
        this.packet = new ModeSetting.Option(this.handMode, "modules.settings.assist.hand_mode.packet");
    }

    private void selectAssistMode(int n) {
        for (AssistItemProvider provider : this.assistItems) {
            if (!provider.isAvailable() || provider.isActive() || !KeyBindingUtil.matches(provider.getKeyCode(), n)) continue;
            if (this.isAssistEntryValid(provider)) {
                this.processedItems.add(n);
                return;
            }
            this.applyAssistEntry(provider);
            return;
        }
    }

    private void resetAssist() {
        Iterator<Integer> iterator = this.processedItems.iterator();
        block0: while (iterator.hasNext()) {
            int n = iterator.next();
            if (this.isSlotInRange(n)) continue;
            iterator.remove();
            if (Assist.minecraftClient.currentScreen != null) continue;
            for (AssistItemProvider provider : this.assistItems) {
                if (!this.isAssistEntryValid(provider) || !provider.isAvailable() || provider.isActive() || KeyBindingUtil.keyCode(provider.getKeyCode()) != n) continue;
                this.applyAssistEntry(provider);
                continue block0;
            }
        }
    }

    private boolean isAssistEntryValid(AssistItemProvider provider) {
        return provider instanceof TrapkaProvider
            || provider instanceof PlastProvider
            || provider instanceof DisorientationProvider
            || provider instanceof PilbProvider
            || provider instanceof GodAuraProvider
            || provider instanceof StunProvider
            || provider instanceof SnowballProvider
            || provider instanceof BoomTrapProvider;
    }

    private void applyAssistEntry(AssistItemProvider provider) {
        provider.onSelected();
        if (!provider.isSpecial()) {
            if (provider instanceof BackpackProvider backpackProvider) {
                backpackProvider.swapRequiredItems();
            } else {
                ItemSwapManager.getInstance().swap(provider.getItemStack().getItem(), provider::matches, provider.getDisplayName());
            }
        }
    }

    private boolean isSlotInRange(int n) {
        return KeyBindingUtil.isPressed(n);
    }

    public final void replaceAssistEntries(List<AssistItemProvider> list) {
        this.assistItems.clear();
        this.assistItems.addAll(list);
    }

    @Override
    public void onTick() {
        if (Assist.minecraftClient.player == null || Assist.minecraftClient.world == null || Assist.minecraftClient.interactionManager == null) {
            return;
        }
        LivingEntity targetEntity = RockstarClient.create().getFriendManager().getTargetLivingEntity();
        this.resetAssist();
        Iterator<AssistItemProvider> providerIterator = this.assistItems.iterator();
        while (providerIterator.hasNext()) {
            providerIterator.next().tick();
        }
        this.resetInventoryState();
        if (this.autoChorus.isEnabled() && targetEntity != null && Assist.minecraftClient.player.distanceTo(targetEntity) < 5.0f && !targetEntity.getActiveItem().isOf(Items.CHORUS_FRUIT)) {
            this.updateTimer.reset();
        }
        if (this.buffsLoop.isEnabled()) {
            if (this.buffCycleStep == 0) {
                Assist.minecraftClient.player.networkHandler.sendChatMessage("\u041a\u0442\u043e \u0445\u043e\u0447\u0435\u0442 \u0431\u0430\u0444\u044b?");
                this.targetTimer.reset();
                this.buffCycleStep = 1;
            } else if (this.buffCycleStep == 1 && this.targetTimer.hasElapsed(1000L)) {
                Assist.minecraftClient.player.networkHandler.sendChatMessage("\u041a\u0442\u043e \u0441\u043e\u0441\u0430\u043b?");
                this.targetTimer.reset();
                this.buffCycleStep = 2;
            } else if (this.buffCycleStep == 2 && this.targetTimer.hasElapsed(10000L)) {
                this.buffCycleStep = 0;
            }
        } else {
            this.buffCycleStep = 0;
        }
        if (this.warnArmor.isEnabled()) {
            float f = 1.0f;
            for (ItemStack class_17992 : Assist.minecraftClient.player.getAllArmorItems()) {
                if (class_17992.isEmpty()) continue;
                float f2 = class_17992.getMaxDamage();
                float f3 = f2 - (float)class_17992.getDamage();
                f = f3 / f2;
            }
            if ((double)f < 0.36) {
                if (this.disableLocked) {
                    RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.INFO, Localization.translate("assist.break"), Localization.translate("assist.armor_almost_broken"));
                    this.disableLocked = false;
                }
            } else {
                this.disableLocked = true;
            }
        }
        if (this.dragonFly.isEnabled() && Assist.minecraftClient.player.getAbilities().allowFlying && Assist.minecraftClient.player.getAbilities().flying && !RockstarClient.create().getModuleRegistry().getModule(FreeCamera.class).isEnabled()) {
            if (!Assist.minecraftClient.player.isSneaking() && Assist.minecraftClient.options.jumpKey.isPressed()) {
                Assist.minecraftClient.player.setVelocity(Assist.minecraftClient.player.getVelocity().x, (double)this.flySpeedY.getValue(), Assist.minecraftClient.player.getVelocity().z);
            } else if (Assist.minecraftClient.options.sneakKey.isPressed()) {
                Assist.minecraftClient.player.setVelocity(Assist.minecraftClient.player.getVelocity().x, (double)(-this.flySpeedY.getValue()), Assist.minecraftClient.player.getVelocity().z);
            }
            EntityUtils.applyMovement(this.flySpeedXz.getValue(), false);
        }
        if (this.autoFix.isEnabled()) {
            PlayerInventory class_16612 = Assist.minecraftClient.player.getInventory();
            boolean bl = this.isInventoryReady(class_16612);
            if (ServerDetector.enabled && bl) {
                this.chatFiltering = true;
            }
            if (!ServerDetector.enabled && bl) {
                if (this.chatFiltering) {
                    this.resetSelection();
                    this.chatFiltering = false;
                    this.delayTimer.reset();
                }
            } else if (!bl) {
                this.chatFiltering = false;
            }
        } else {
            this.chatFiltering = false;
        }
        if (this.autoPiona.isEnabled()) {
            if (Assist.minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler && Assist.minecraftClient.currentScreen.getTitle().getString().contains("\u0412\u0430\u043c \u043f\u043e\u0434\u0430\u0440\u043e\u043a")) {
                Assist.minecraftClient.interactionManager.clickSlot(Assist.minecraftClient.player.currentScreenHandler.syncId, 13, 0, SlotActionType.PICKUP, (PlayerEntity)Assist.minecraftClient.player);
            }
            if (this.cooldownTimer.hasElapsed(1000L) && !this.privilegedMode) {
                this.privilegedMode = true;
                Assist.minecraftClient.player.networkHandler.sendChatCommand("piona");
            }
        }
        if (this.autoZako.isEnabled() && this.cooldownTimer.hasElapsed(500L) && this.overlayVisible) {
            this.overlayVisible = false;
            Assist.minecraftClient.player.networkHandler.sendChatCommand("zako");
        }
        super.onTick();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.buffCycleStep = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.buffCycleStep = 0;
        this.processedItems.clear();
        this.resetFoodState();
    }

    private void resetSelection() {
        Assist.minecraftClient.player.networkHandler.sendChatCommand("fix all");
    }

    private boolean isInventoryReady(PlayerInventory class_16612) {
        for (int i = 0; i < class_16612.size(); ++i) {
            float f;
            float f2;
            ItemStack class_17992 = class_16612.getStack(i);
            if (class_17992.isEmpty() || !class_17992.isDamageable() || !((f2 = (f = (float)class_17992.getMaxDamage()) - (float)class_17992.getDamage()) / f > 0.5f)) continue;
            return true;
        }
        return false;
    }

    private void resetInventoryState() {
        boolean bl;
        if (!this.autoChorus.isEnabled()) {
            if (this.alwaysEnabled) {
                this.resetFoodState();
            }
            this.updateTimer.reset();
            return;
        }
        if (Assist.minecraftClient.player == null || Assist.minecraftClient.world == null) {
            if (this.alwaysEnabled) {
                this.resetFoodState();
            }
            this.updateTimer.reset();
            return;
        }
        LivingEntity class_13092 = RockstarClient.create().getFriendManager().getTargetLivingEntity();
        if (class_13092 == null) {
            this.updateTimer.reset();
            if (this.alwaysEnabled && !Assist.minecraftClient.player.isUsingItem()) {
                this.resetFoodState();
            }
            return;
        }
        if (!class_13092.isAlive()) {
            this.updateTimer.reset();
            return;
        }
        double d = Assist.minecraftClient.player.squaredDistanceTo((Entity)class_13092);
        boolean bl2 = bl = class_13092.isUsingItem() && class_13092.getActiveItem().getItem() == Items.CHORUS_FRUIT;
        if (!bl || d > 25.0) {
            this.updateTimer.reset();
        } else if (!this.alwaysEnabled && Assist.minecraftClient.currentScreen == null && !Assist.minecraftClient.player.isUsingItem() && this.updateTimer.hasElapsed(200L)) {
            this.resetChatState();
        }
        if (this.alwaysEnabled) {
            if (Assist.minecraftClient.player.isUsingItem()) {
                this.inventoryOpen = true;
            } else if (this.inventoryOpen) {
                this.resetFoodState();
                return;
            }
            if (this.resetTimer.hasElapsed(2500L)) {
                this.resetFoodState();
            }
        }
    }

    private void resetChatState() {
        HotbarSlot hotbarSlot;
        if (Assist.minecraftClient.player == null || Assist.minecraftClient.interactionManager == null || Assist.minecraftClient.options == null) {
            return;
        }
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getHotbarRules().combineRules(ItemRuleSets.getInventoryRules());
        ItemRule itemRule = itemRuleCollection.findByItem(Items.CHORUS_FRUIT);
        if (itemRule == null) {
            return;
        }
        this.selectedSlot = hotbarSlot = InventoryUtils.getSelectedHotbarSlot();
        this.mouseOverride = false;
        this.sourceItemSlot = -1;
        if (itemRule instanceof HotbarSlot) {
            HotbarSlot hotbarSlot2 = (HotbarSlot)itemRule;
            if (hotbarSlot2.getSlotIndex() != hotbarSlot.getSlotIndex()) {
                InventoryUtils.setSelectedHotbarSlot(hotbarSlot2);
            }
        } else {
            this.mouseOverride = true;
            this.sourceItemSlot = itemRule.getClickSlot();
            InventoryUtils.dropItem(this.sourceItemSlot, hotbarSlot.getSlotIndex());
        }
        Assist.minecraftClient.options.useKey.setPressed(true);
        this.alwaysEnabled = true;
        this.resetTimer.reset();
        this.inventoryOpen = false;
    }

    private void resetFoodState() {
        if (!this.alwaysEnabled) {
            this.mouseOverride = false;
            this.selectedSlot = null;
            this.sourceItemSlot = -1;
            this.inventoryOpen = false;
            return;
        }
        if (Assist.minecraftClient.options != null) {
            Assist.minecraftClient.options.useKey.setPressed(false);
        }
        if (Assist.minecraftClient.player != null && Assist.minecraftClient.interactionManager != null) {
            if (this.mouseOverride && this.selectedSlot != null && this.sourceItemSlot != -1) {
                InventoryUtils.dropItem(this.sourceItemSlot, this.selectedSlot.getSlotIndex());
            }
            if (this.selectedSlot != null) {
                InventoryUtils.setSelectedHotbarSlot(this.selectedSlot);
            }
        }
        this.alwaysEnabled = false;
        this.mouseOverride = false;
        this.selectedSlot = null;
        this.sourceItemSlot = -1;
        this.inventoryOpen = false;
        this.updateTimer.reset();
        this.resetTimer.reset();
    }

    public void resetActionState() {
        ItemRule itemRule;
        if (this.foodActionActive) {
            MinecraftClient.getInstance().options.useKey.setPressed(false);
            this.foodActionActive = false;
        }
        if (this.itemRule != null && (itemRule = this.itemRule) instanceof HotbarSlot) {
            HotbarSlot hotbarSlot = (HotbarSlot)itemRule;
            InventoryUtils.setSelectedHotbarSlot(hotbarSlot);
            this.itemRule = null;
        }
        this.actionPending = false;
    }

    private boolean isAssistReady() {
        return ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD_VARIANTS) || ServerDetector.isServerProfileSupported(ServerProfile.SUPPORTED_NETWORKS) || ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD_VARIANTS);
    }

    private boolean isChatMessageAllowed(String string) {
        String[] stringArray;
        if (!this.ctCommands.isEnabled() || !ServerDetector.enabled) {
            return false;
        }
        String string2 = this.formatAssistText(string);
        if (string2 == null) {
            return false;
        }
        for (String string3 : stringArray = new String[]{"hub", "an", "grief", "leave", "limbo", "lobby"}) {
            if (!string2.equals(string3.toLowerCase(Locale.ROOT))) continue;
            return true;
        }
        return false;
    }

    private String formatAssistText(String string) {
        if (string == null) {
            return null;
        }
        String string2 = string.trim();
        if (!string2.startsWith("/")) {
            return null;
        }
        if ((string2 = string2.substring(1).trim()).isEmpty()) {
            return null;
        }
        int n = string2.indexOf(32);
        if (n != -1) {
            string2 = string2.substring(0, n);
        }
        return string2.toLowerCase(Locale.ROOT);
    }

    public boolean isModeReady() {
        return this.handMode.isSelected(this.left);
    }

    public boolean isHandModeReady() {
        return this.handMode.isSelected(this.packet);
    }

    @Generated
    public BooleanSupplier getWorldReadyCheck() {
        return this.worldReadyCheck;
    }

    @Generated
    public BooleanSupplier getScreenReadyCheck() {
        return this.screenReadyCheck;
    }

    @Generated
    public BooleanSupplier getPlayerReadyCheck() {
        return this.playerReadyCheck;
    }

    @Generated
    public ActionSetting getOpenMacrosAction() {
        return this.openMacrosAction;
    }

    @Generated
    public BooleanSetting getShowRadiusSetting() {
        return this.showRadius;
    }

    @Generated
    public BooleanSetting getCloseMenuSetting() {
        return this.closeMenu;
    }

    @Generated
    public BooleanSetting getAutoFixSetting() {
        return this.autoFix;
    }

    @Generated
    public BooleanSetting getChatFilterSetting() {
        return this.chatFilter;
    }

    @Generated
    public BooleanSetting getReduceCooldownSetting() {
        return this.reduceCooldown;
    }

    @Generated
    public BooleanSetting getWarnArmorSetting() {
        return this.warnArmor;
    }

    @Generated
    public BooleanSetting getDragonFlySetting() {
        return this.dragonFly;
    }

    @Generated
    public NumberSetting getFlySpeedXzSetting() {
        return this.flySpeedXz;
    }

    @Generated
    public NumberSetting getFlySpeedYSetting() {
        return this.flySpeedY;
    }

    @Generated
    public BooleanSetting getAutoPiona() {
        return this.autoPiona;
    }

    @Generated
    public BooleanSetting getAutoChorus() {
        return this.autoChorus;
    }

    @Generated
    public BooleanSetting getAutoZako() {
        return this.autoZako;
    }

    @Generated
    public BooleanSetting getAutoStop() {
        return this.autoStop;
    }

    @Generated
    public BooleanSetting getHealHelper() {
        return this.healHelper;
    }

    @Generated
    public BooleanSetting getCtCommands() {
        return this.ctCommands;
    }

    @Generated
    public BooleanSetting getAutoRct() {
        return this.autoRct;
    }

    @Generated
    public BooleanSetting getBuffsLoop() {
        return this.buffsLoop;
    }

    @Generated
    public ModeSetting getHandModeSetting() {
        return this.handMode;
    }

    @Generated
    public ModeSetting.Option getLeftHandOption() {
        return this.left;
    }

    @Generated
    public ModeSetting.Option getRightHandOption() {
        return this.right;
    }

    @Generated
    public ModeSetting.Option getPacketHandOption() {
        return this.packet;
    }

    @Generated
    public Set<String> getTrackedItems() {
        return this.trackedItems;
    }

    @Generated
    public List<Pattern> getAssistEntries() {
        return this.pendingActions;
    }

    @Generated
    public Timer getCooldownTimer() {
        return this.cooldownTimer;
    }

    @Generated
    public Timer getActionTimer() {
        return this.actionTimer;
    }

    @Generated
    public Timer getTargetTimer() {
        return this.targetTimer;
    }

    @Generated
    public Timer getUpdateTimer() {
        return this.updateTimer;
    }

    @Generated
    public Timer getResetTimer() {
        return this.resetTimer;
    }

    @Generated
    public Timer getDelayTimer() {
        return this.delayTimer;
    }

    @Generated
    public boolean isAssistActive() {
        return this.assistActive;
    }

    @Generated
    public boolean isOverlayVisible() {
        return this.overlayVisible;
    }

    @Generated
    public boolean isPrivilegedMode() {
        return this.privilegedMode;
    }

    @Override
    @Generated
    public boolean isDisableLocked() {
        return this.disableLocked;
    }

    /** NOT an override: the original {@code rockstar/ilIlil/IIIIIIIIi} declares no keybind
     *  getter, so the inherited {@link Module#getKeyBind()} must stay visible. */
    @Generated
    public int getBuffCycleStep() {
        return this.buffCycleStep;
    }

    @Override
    @Generated
    public boolean isAlwaysEnabled() {
        return this.alwaysEnabled;
    }

    @Generated
    public boolean isMouseOverride() {
        return this.mouseOverride;
    }

    /** NOT an override: the original {@code rockstar/ilIlil/IIIIIIIIi} declares no keybind
     *  getter, so the inherited {@link Module#getSavedKeyBind()} must stay visible. */
    @Generated
    public int getSourceItemSlot() {
        return this.sourceItemSlot;
    }

    @Generated
    public HotbarSlot getSelectedSlot() {
        return this.selectedSlot;
    }

    @Generated
    public boolean isInventoryOpen() {
        return this.inventoryOpen;
    }

    @Generated
    public boolean isChatFiltering() {
        return this.chatFiltering;
    }

    @Generated
    public Set<Integer> getProcessedItems() {
        return this.processedItems;
    }

    @Generated
    public EventListener<MouseEvent> getMouseListener() {
        return this.onMouseEvent;
    }

    @Generated
    public EventListener<KeyPressEvent> getKeyPressListener() {
        return this.onKeyPressEvent;
    }

    @Generated
    public EventListener<EventSetCooldown> getCooldownEventListener() {
        return this.onEventSetCooldownListener;
    }

    @Generated
    public EventListener<ReceivePacketEvent> getReceivePacketListener() {
        return this.onReceivePacketEvent;
    }

    @Generated
    public EventListener<SendMessageEvent> getSendMessageListener() {
        return this.onSendMessageEvent;
    }

    @Generated
    public EventListener<AttackEvent> getAttackListener() {
        return this.onAttackEvent;
    }

    @Generated
    public EventListener<FinishEatEvent> getFinishEatListener() {
        return this.onFinishEatEvent;
    }

    @Generated
    public EventListener<FinishEatEvent> getFinishEatSecondaryListener() {
        return this.onFinishEatEvent1;
    }

    @Generated
    public EventListener<InputEvent> getInputListener() {
        return this.onInputEvent;
    }

    @Generated
    public ItemRule getItemRule() {
        return this.itemRule;
    }

    @Generated
    public boolean isFoodActionActive() {
        return this.foodActionActive;
    }

    @Generated
    public boolean isActionPending() {
        return this.actionPending;
    }

    @Generated
    public Timer getAnimationTimer() {
        return this.animationTimer;
    }

    @Generated
    public EventListener<HudRenderEvent> getHudRenderListener() {
        return this.onHudRenderEvent;
    }

    @Generated
    public EventListener<FinishEatEvent> getFinishEatTertiaryListener() {
        return this.onFinishEatEvent2;
    }

    @Generated
    public List<AssistItemProvider> getAssistSettings() {
        return this.assistSettings;
    }

    @Generated
    public List<AssistItemProvider> getAssistItems() {
        return this.assistItems;
    }
}
