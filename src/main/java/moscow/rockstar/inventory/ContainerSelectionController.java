/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ScreenHandler
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.Item$TooltipContext
 *  net.minecraft.ItemStack
 *  net.minecraft.TooltipType
 *  net.minecraft.World
 *  net.minecraft.Text
 */
package moscow.rockstar.inventory;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.api.commands.CommandBuilder;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.window.KeyPressEvent;

public class ContainerSelectionController
implements ClientAccess {
    private static final long CLICK_DELAY_MILLIS = 150L;
    private static final long ACTION_CONFIRMATION_DELAY_MILLIS = 900L;
    private static final long MENU_POLL_INTERVAL_MILLIS = 1200L;
    private static final long SCREEN_TIMEOUT_MILLIS = 8000L;
    private static final int MAX_SLOT_RETRY_COUNT = 4;
    private static final int MAX_MENU_OPEN_ATTEMPTS = 3;
    private static final int MAX_STALLED_ATTEMPTS = 2;
    private static final int MAX_PURCHASE_ACTIONS = 64;
    private static final int EMPTY_COUNT = 0;
    private static final int FIRST_CONTAINER_SLOT = 1;
    private static final int CONFIRMATION_SLOT = 13;
    private static final int[][] BUILT_IN_QUANTITY_RULES = new int[][]{{17, 50}, {16, 25}, {15, 10}, {14, 5}};
    private static final int CONTAINER_SLOT_COUNT = 66;
    private static final Pattern BALANCE_PATTERN = Pattern.compile("\u0412\u0430\u0448 \u0431\u0430\u043b\u0430\u043d\u0441\\D*(\\d[\\d\\s.,]*)", 66);
    private static final Pattern PENDING_COINS_PATTERN = Pattern.compile("\u041e\u0436\u0438\u0434\u0430\u0435\u0442\u0441\u044f \u043a\u043e\u0438\u043d\u043e\u0432\\D*(\\d[\\d\\s.,]*)", 66);
    private static final Pattern ADDITIONAL_AMOUNT_PATTERN = Pattern.compile("\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c\\D*(\\d[\\d\\s.,]*)", 66);
    private boolean exchangeActive;
    private int exchangeLimit;
    private int spentCoins;
    private int completedExchangeCount;
    private int availableBalance;
    private int lastObservedBalance;
    private int pendingCoins;
    private int minimumPurchaseAmount;
    private int purchaseAmountStep;
    private boolean purchasePending;
    private boolean awaitingConfirmation;
    private int purchasedAmount;
    private int targetPurchaseAmount;
    private int purchaseActionCount;
    private int slotRetryCount;
    private int menuOpenAttempts;
    private int stalledConfirmationAttempts;
    private final Timer clickTimer = new Timer();
    private final Timer menuTimer = new Timer();
    private final EventListener<KeyPressEvent> cancelKeyListener = keyPressEvent -> {
        if (!this.exchangeActive) {
            return;
        }
        if (keyPressEvent.getAction() != 1 || keyPressEvent.getKey() != 256) {
            return;
        }
        this.notifyExchangeState("commands.exchange.cancelled", false, this.spentCoins);
    };
    private final EventListener<ClientPlayerTickEvent> playerTickListener = clientPlayerTickEvent -> {
        GenericContainerScreenHandler containerHandler = null;
        ScreenHandler currentHandler;
        if (!this.exchangeActive) {
            return;
        }
        if (ContainerSelectionController.minecraftClient.player == null || ContainerSelectionController.minecraftClient.world == null || ContainerSelectionController.minecraftClient.interactionManager == null) {
            this.exchangeActive = false;
            return;
        }
        if (ContainerSelectionController.minecraftClient.currentScreen != null
            && (currentHandler = ContainerSelectionController.minecraftClient.player.currentScreenHandler) instanceof GenericContainerScreenHandler) {
            containerHandler = (GenericContainerScreenHandler)currentHandler;
        }
        if (containerHandler == null) {
            if (!this.awaitingConfirmation && this.exchangeLimit > 0 && this.spentCoins >= this.exchangeLimit) {
                this.finishExchange();
                return;
            }
            if (!this.menuTimer.hasElapsed(1200L)) {
                return;
            }
            if (this.menuOpenAttempts >= 3) {
                if (this.spentCoins > 0) {
                    this.finishExchange();
                } else {
                    this.notifyExchangeState("commands.exchange.no_menu", true, new Object[0]);
                }
                return;
            }
            ++this.menuOpenAttempts;
            this.menuTimer.reset();
            this.clickTimer.reset();
            ContainerSelectionController.minecraftClient.player.networkHandler.sendChatCommand("exchange");
            return;
        }
        String screenTitle = this.normalizeText(ContainerSelectionController.minecraftClient.currentScreen.getTitle().getString());
        if (screenTitle.contains("\u0431\u0438\u0440\u0436\u0430")) {
            this.menuOpenAttempts = 0;
            this.updateFromBalanceScreen(containerHandler);
        } else if (screenTitle.contains("\u043f\u043e\u043a\u0443\u043f\u043a\u0430")) {
            this.menuOpenAttempts = 0;
            this.executePurchaseScreen(containerHandler);
        } else if (this.menuTimer.hasElapsed(8000L)) {
            this.notifyExchangeState("commands.exchange.no_menu", true, new Object[0]);
        }
    };

    public ContainerSelectionController() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("exchange")
            .aliases("exc")
            .description("commands.exchange.description")
            .argument("args", argument -> argument.optional().vararg().validator(PluginResolver::resolveValue).choices("stop", "dump"))
            .handler(this::executeExchangeCommand)
            .build();
    }

    private void executeExchangeCommand(DispatchContext dispatchContext) {
        String string;
        if (ContainerSelectionController.minecraftClient.player == null || ContainerSelectionController.minecraftClient.world == null) {
            return;
        }
        List list = dispatchContext.getArguments().isEmpty() || dispatchContext.getArguments().getFirst() == null ? List.of() : (List)dispatchContext.getArguments().getFirst();
        String string2 = string = list.isEmpty() ? "" : ((String)list.getFirst()).toLowerCase(Locale.ROOT);
        if (string.equals("stop") || string.equals("cancel")) {
            if (this.exchangeActive) {
                this.notifyExchangeState("commands.exchange.cancelled", false, this.spentCoins);
            } else {
                Notification.error(Text.of((String)Localization.translate("commands.exchange.not_active")));
            }
            return;
        }
        if (string.equals("dump")) {
            this.dumpExchangeState();
            return;
        }
        int n = 0;
        if (!string.isEmpty()) {
            try {
                n = Integer.parseInt(string);
            }
            catch (NumberFormatException numberFormatException) {
                n = -1;
            }
            if (n <= 0) {
                Notification.error(Text.of((String)Localization.translate("commands.exchange.usage")));
                return;
            }
        }
        if (!ServerDetector.isInventoryServer()) {
            Notification.error(Text.of((String)Localization.translate("commands.exchange.only_hw")));
            return;
        }
        this.startExchange(n);
    }

    private void startExchange(int n) {
        this.exchangeActive = true;
        this.exchangeLimit = n;
        this.spentCoins = 0;
        this.completedExchangeCount = 0;
        this.availableBalance = 0;
        this.lastObservedBalance = -1;
        this.pendingCoins = 0;
        this.minimumPurchaseAmount = 0;
        this.purchaseAmountStep = 0;
        this.purchasePending = false;
        this.awaitingConfirmation = false;
        this.purchasedAmount = 0;
        this.targetPurchaseAmount = 0;
        this.purchaseActionCount = 0;
        this.slotRetryCount = 0;
        this.menuOpenAttempts = 0;
        this.stalledConfirmationAttempts = 0;
        this.clickTimer.reset();
        this.menuTimer.reset();
        ContainerSelectionController.minecraftClient.player.networkHandler.sendChatCommand("exchange");
        Notification.info(Text.of((String)(this.exchangeLimit > 0 ? Localization.translateFormatted("commands.exchange.started_limit", this.exchangeLimit) : Localization.translate("commands.exchange.started"))));
    }

    private void updateFromBalanceScreen(GenericContainerScreenHandler class_17072) {
        int n;
        int n2;
        int n3 = this.getPlayerInventoryStartSlot(class_17072);
        int n4 = -1;
        Integer n5 = null;
        this.pendingCoins = 0;
        for (n2 = 0; n2 < n3; ++n2) {
            Integer n6;
            ItemStack class_17992 = class_17072.getSlot(n2).getStack();
            if (class_17992.isEmpty()) continue;
            String string = this.stripFormatting(class_17992.getName().getString());
            if (n5 == null && (n6 = this.parseCurrencyAmount(BALANCE_PATTERN, string)) != null) {
                n5 = n6;
                n4 = n2;
                continue;
            }
            n6 = this.parseCurrencyAmount(PENDING_COINS_PATTERN, string);
            if (n6 == null) continue;
            this.pendingCoins = n6;
        }
        if (n5 == null) {
            if (this.menuTimer.hasElapsed(8000L)) {
                this.notifyExchangeState("commands.exchange.no_balance", true, new Object[0]);
            }
            return;
        }
        n2 = n5;
        if (this.awaitingConfirmation) {
            if (n2 != this.lastObservedBalance) {
                int n7 = Math.max(0, this.lastObservedBalance - n2);
                this.spentCoins += n7;
                if (n7 > 0) {
                    ++this.completedExchangeCount;
                    this.stalledConfirmationAttempts = 0;
                } else {
                    ++this.stalledConfirmationAttempts;
                }
                this.awaitingConfirmation = false;
                this.slotRetryCount = 0;
                this.menuTimer.reset();
            } else if (this.clickTimer.hasElapsed(900L)) {
                if (this.slotRetryCount < 4 && n4 >= 0) {
                    ++this.slotRetryCount;
                    this.clickContainerSlot(class_17072, n4, 0);
                    return;
                }
                this.awaitingConfirmation = false;
                this.slotRetryCount = 0;
                ++this.stalledConfirmationAttempts;
                this.menuTimer.reset();
            } else {
                return;
            }
        }
        this.availableBalance = n2;
        if (this.stalledConfirmationAttempts >= 2) {
            this.notifyExchangeState("commands.exchange.stalled", true, this.spentCoins);
            return;
        }
        int n8 = n = this.exchangeLimit > 0 ? Math.min(this.exchangeLimit - this.spentCoins, this.availableBalance) : this.availableBalance;
        if (this.pendingCoins > 0 && (this.minimumPurchaseAmount <= 0 || this.pendingCoins >= this.minimumPurchaseAmount)) {
            n = Math.min(n, this.pendingCoins);
        }
        if (this.purchaseAmountStep > 1) {
            n -= n % this.purchaseAmountStep;
        }
        if (n <= 0 || this.minimumPurchaseAmount > 0 && n < this.minimumPurchaseAmount) {
            this.finishExchange();
            return;
        }
        if (!this.clickTimer.hasElapsed(150L)) {
            return;
        }
        if (this.purchasePending && !this.menuTimer.hasElapsed(8000L)) {
            return;
        }
        this.purchasePending = false;
        if (!class_17072.getSlot(0).hasStack()) {
            if (this.slotRetryCount < 4 && n4 >= 0) {
                ++this.slotRetryCount;
                this.clickContainerSlot(class_17072, n4, 0);
                return;
            }
            this.notifyExchangeState("commands.exchange.empty", false, this.spentCoins);
            return;
        }
        this.slotRetryCount = 0;
        this.lastObservedBalance = this.availableBalance;
        this.targetPurchaseAmount = n;
        this.purchasedAmount = 0;
        this.purchaseActionCount = 0;
        this.purchasePending = true;
        this.clickContainerSlot(class_17072, 0, 1);
    }

    private void executePurchaseScreen(GenericContainerScreenHandler class_17072) {
        int n;
        this.purchasePending = false;
        if (this.awaitingConfirmation) {
            if (this.menuTimer.hasElapsed(8000L)) {
                this.menuTimer.reset();
                ContainerSelectionController.minecraftClient.player.closeHandledScreen();
            }
            return;
        }
        if (!this.clickTimer.hasElapsed(150L)) {
            return;
        }
        int n2 = this.getPlayerInventoryStartSlot(class_17072);
        int n3 = this.targetPurchaseAmount - this.purchasedAmount;
        int n4 = -1;
        int n5 = 0;
        int n6 = 0;
        int n7 = 0;
        boolean bl = false;
        for (n = 0; n < n2; ++n) {
            Integer n8;
            ItemStack class_17992 = class_17072.getSlot(n).getStack();
            if (class_17992.isEmpty() || (n8 = this.parseCurrencyAmount(ADDITIONAL_AMOUNT_PATTERN, this.stripFormatting(class_17992.getName().getString()))) == null || n8 <= 0) continue;
            bl = true;
            n7 = this.greatestCommonDivisor(n7, n8);
            if (n6 == 0 || n8 < n6) {
                n6 = n8;
            }
            if (n8 > n3 || n8 <= n5) continue;
            n5 = n8;
            n4 = n;
        }
        if (!bl) {
            for (int[] nArray : BUILT_IN_QUANTITY_RULES) {
                int n9 = nArray[0];
                int n10 = nArray[1];
                if (n9 >= n2 || !class_17072.getSlot(n9).hasStack()) continue;
                n7 = this.greatestCommonDivisor(n7, n10);
                if (n6 == 0 || n10 < n6) {
                    n6 = n10;
                }
                if (n10 > n3 || n10 <= n5) continue;
                n5 = n10;
                n4 = n9;
            }
        }
        if (n6 > 0) {
            this.minimumPurchaseAmount = n6;
            this.purchaseAmountStep = n7;
        }
        if (n4 >= 0 && this.purchaseActionCount < 64) {
            this.purchasedAmount += n5;
            ++this.purchaseActionCount;
            this.clickContainerSlot(class_17072, n4, 0);
            return;
        }
        if (this.purchasedAmount <= 0) {
            this.notifyExchangeState("commands.exchange.empty", false, this.spentCoins);
            ContainerSelectionController.minecraftClient.player.closeHandledScreen();
            return;
        }
        n = this.findPurchaseActionSlot(class_17072, n2);
        if (n < 0) {
            this.notifyExchangeState("commands.exchange.no_confirm", true, this.spentCoins);
            return;
        }
        this.awaitingConfirmation = true;
        this.clickContainerSlot(class_17072, n, 0);
    }

    private int findPurchaseActionSlot(GenericContainerScreenHandler class_17072, int n) {
        if (13 < n && class_17072.getSlot(13).hasStack()) {
            return 13;
        }
        for (int i = 0; i < n; ++i) {
            String string;
            ItemStack class_17992 = class_17072.getSlot(i).getStack();
            if (class_17992.isEmpty() || !(string = this.normalizeText(class_17992.getName().getString())).contains("\u043a\u0443\u043f\u0438\u0442\u044c") && !string.contains("\u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u0442\u044c") && !string.contains("\u043e\u0431\u043c\u0435\u043d\u044f\u0442\u044c") && !string.contains("\u043e\u0444\u043e\u0440\u043c\u0438\u0442\u044c")) continue;
            return i;
        }
        return -1;
    }

    private void clickContainerSlot(GenericContainerScreenHandler class_17072, int n, int n2) {
        if (ContainerSelectionController.minecraftClient.interactionManager == null || n < 0 || n >= class_17072.slots.size()) {
            return;
        }
        ContainerSelectionController.minecraftClient.interactionManager.clickSlot(class_17072.syncId, n, n2, SlotActionType.PICKUP, (PlayerEntity)ContainerSelectionController.minecraftClient.player);
        this.clickTimer.reset();
        this.menuTimer.reset();
    }

    private void finishExchange() {
        if (this.exchangeLimit > 0) {
            this.notifyExchangeState("commands.exchange.finished_limit", false, this.spentCoins, this.exchangeLimit, this.completedExchangeCount);
        } else {
            this.notifyExchangeState("commands.exchange.finished", false, this.spentCoins, this.completedExchangeCount);
        }
    }

    private void notifyExchangeState(String string, boolean bl, Object ... objectArray) {
        this.exchangeActive = false;
        Text class_25612 = Text.of((String)(objectArray.length == 0 ? Localization.translate(string) : Localization.translateFormatted(string, objectArray)));
        if (bl) {
            Notification.error(class_25612);
        } else {
            Notification.info(class_25612);
        }
    }

    private int getPlayerInventoryStartSlot(GenericContainerScreenHandler class_17072) {
        return Math.max(0, class_17072.slots.size() - 36);
    }

    private Integer parseCurrencyAmount(Pattern pattern, String string) {
        if (string == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(string);
        if (!matcher.find()) {
            return null;
        }
        String string2 = matcher.group(1).replaceAll("\\D", "");
        if (string2.isEmpty() || string2.length() > 9) {
            return null;
        }
        return Integer.parseInt(string2);
    }

    private int greatestCommonDivisor(int n, int n2) {
        while (n2 != 0) {
            int n3 = n % n2;
            n = n2;
            n2 = n3;
        }
        return n;
    }

    private String stripFormatting(String string) {
        return string == null ? "" : string.replaceAll("\u00a7.", "");
    }

    private String normalizeText(String string) {
        return this.stripFormatting(string).replace('\u0451', '\u0435').replace('\u0401', '\u0415').toLowerCase(Locale.ROOT);
    }

    private void dumpExchangeState() {
        ScreenHandler class_17032;
        if (ContainerSelectionController.minecraftClient.currentScreen == null || !((class_17032 = ContainerSelectionController.minecraftClient.player.currentScreenHandler) instanceof GenericContainerScreenHandler)) {
            Notification.error(Text.of((String)Localization.translate("commands.exchange.no_screen")));
            return;
        }
        GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)class_17032;
        RockstarClient.LOGGER.info("[Exchange] '{}', \u0441\u043b\u043e\u0442\u043e\u0432 {}", (Object)ContainerSelectionController.minecraftClient.currentScreen.getTitle().getString(), (Object)class_17072.slots.size());
        int n = this.getPlayerInventoryStartSlot(class_17072);
        for (int i = 0; i < n; ++i) {
            ItemStack class_17992 = class_17072.getSlot(i).getStack();
            if (class_17992.isEmpty()) continue;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                for (Text class_25612 : class_17992.getTooltip(Item.TooltipContext.create((World)ContainerSelectionController.minecraftClient.world), (PlayerEntity)ContainerSelectionController.minecraftClient.player, (TooltipType)TooltipType.BASIC)) {
                    stringBuilder.append(" | ").append(class_25612.getString());
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            RockstarClient.LOGGER.info("[Exchange] {} x{} {}{}", new Object[]{i, class_17992.getCount(), class_17992.getName().getString(), stringBuilder});
        }
        Notification.info(Text.of((String)Localization.translate("commands.exchange.dumped")));
    }
}
