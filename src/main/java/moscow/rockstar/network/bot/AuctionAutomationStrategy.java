/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 */
package moscow.rockstar.network.bot;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Locale;
import moscow.rockstar.network.bot.BotBehaviorStrategy;
import moscow.rockstar.network.bot.BotController;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class AuctionAutomationStrategy
implements BotBehaviorStrategy {
    private final int targetIndex;
    private final long retryIntervalMillis;
    private final long initialDelayMillis;
    private long lastEvaluationMillis;
    private long lastTargetActionMillis;
    private long lastNavigationClickMillis;
    private long lastTargetClickMillis;
    private boolean initialized;

    public AuctionAutomationStrategy(int n, int n2) {
        this(n, n2, 0L);
    }

    public AuctionAutomationStrategy(int n, int n2, long l) {
        this.targetIndex = n;
        this.retryIntervalMillis = (long)Math.max(1, n2) * 1000L;
        this.initialDelayMillis = Math.max(0L, l);
    }

    @Override
    public void applyBehavior(BotController botController) {
        if (botController == null || !botController.isConnected()) {
            return;
        }
        long l = System.currentTimeMillis();
        if (!this.initialized) {
            this.initialized = true;
            this.lastEvaluationMillis = l;
            this.lastTargetActionMillis = l - this.retryIntervalMillis + this.initialDelayMillis + this.calculateServerJitter(botController);
            return;
        }
        if (l - this.lastEvaluationMillis < this.initialDelayMillis) {
            return;
        }
        if (botController.getInventoryState().hasOpenContainer()) {
            if (l - this.lastNavigationClickMillis >= botController.getControlState().getAuctionNavigationDelayMillis()) {
                this.handleAuctionScreen(botController, l);
                this.lastNavigationClickMillis = l;
            }
            return;
        }
        if (l - this.lastTargetActionMillis < this.retryIntervalMillis) {
            return;
        }
        if (botController.selectHotbarItem(Items.COMPASS)) {
            botController.sendLookPacket();
        } else {
            botController.sendChatOrCommand(botController.getControlState().getAuxiliaryCommand() + this.targetIndex);
        }
        this.lastTargetActionMillis = l;
    }

    private void handleAuctionScreen(BotController botController, long l) {
        if (l - this.lastTargetClickMillis < botController.getControlState().getAuctionClickDelayMillis()) {
            return;
        }
        AuctionScreenState auctionScreenState = this.inspectAuctionScreen(botController);
        int n = this.selectNavigationSlot(auctionScreenState);
        if (n < 0) {
            return;
        }
        if (botController.clickInventorySlot(n)) {
            this.lastTargetClickMillis = l;
            if (n == auctionScreenState.getMainMenuSlot()) {
                botController.setBehaviorStrategy(new IdleBotBehaviorStrategy());
            }
        }
    }

    private int selectNavigationSlot(AuctionScreenState auctionScreenState) {
        boolean bl;
        if (auctionScreenState.hasHints() && auctionScreenState.getBackSlot() >= 0 && auctionScreenState.getNextPageSlot() < 0) {
            return auctionScreenState.getBackSlot();
        }
        if (auctionScreenState.getNextPageSlot() >= 0) {
            return auctionScreenState.getNextPageSlot();
        }
        boolean bl2 = bl = this.targetIndex > 36;
        if (bl && !auctionScreenState.isOnSecondPage()) {
            return auctionScreenState.getPreviousPageSlot();
        }
        if (!bl && auctionScreenState.isOnSecondPage()) {
            return auctionScreenState.getTargetGriefSlot();
        }
        return auctionScreenState.getMainMenuSlot();
    }

    private AuctionScreenState inspectAuctionScreen(BotController botController) {
        boolean bl = false;
        boolean bl2 = false;
        int n = -1;
        int n2 = -1;
        int n3 = -1;
        int n4 = -1;
        int n5 = -1;
        ItemStack[] class_1799Array = botController.getInventoryState().getContainerItems();
        for (int i = 0; i < class_1799Array.length; ++i) {
            String string;
            ItemStack class_17992 = class_1799Array[i];
            if (class_17992 == null || class_17992.isEmpty() || (string = this.stripFormatting(class_17992.getName().getString())).isBlank()) continue;
            if (string.contains("\u0433\u0440\u0438\u0444\u0435\u0440\u0441\u043a\u043e\u0435 \u0432\u044b\u0436\u0438\u0432\u0430\u043d\u0438\u0435") || string.contains("grief survival")) {
                n2 = this.firstValidSlot(n2, i);
            }
            if (string.contains("\u043f\u043e\u0434\u0441\u043a\u0430\u0437") || string.contains("hint")) {
                bl2 = true;
            }
            if (this.isBackOrMenuButton(class_17992, string)) {
                n = this.firstValidSlot(n, i);
            }
            if (this.isPreviousPageButton(string)) {
                bl = true;
                n4 = this.firstValidSlot(n4, i);
            }
            if (this.isNextPageButton(class_17992, string)) {
                n3 = this.firstValidSlot(n3, i);
            }
            if (n5 >= 0 || !this.isTargetItemName(string)) continue;
            n5 = i;
        }
        return new AuctionScreenState(bl, bl2, n, n2, n3, n4, n5);
    }

    private boolean isTargetItemName(String string) {
        String string2 = String.valueOf(this.targetIndex);
        return string.contains("\u0433\u0440\u0438\u0444 #" + string2) || string.contains("\u0433\u0440\u0438\u0444 \u2116" + string2) || string.contains("\u0433\u0440\u0438\u0444 " + string2) || string.contains("grief #" + string2) || string.contains("grief " + string2);
    }

    private boolean isNextPageButton(ItemStack class_17992, String string) {
        return string.contains("\u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0430\u044f") || string.contains("next") || class_17992.getItem() == Items.ARROW && !string.contains("\u043f\u0440\u0435\u0434\u044b\u0434\u0443\u0449") && !string.contains("\u043d\u0430\u0437\u0430\u0434") && !string.contains("back");
    }

    private boolean isPreviousPageButton(String string) {
        return string.contains("\u043f\u0440\u0435\u0434\u044b\u0434\u0443\u0449") || string.contains("previous");
    }

    private boolean isBackOrMenuButton(ItemStack class_17992, String string) {
        if (class_17992.getItem() != Items.ARROW && class_17992.getItem() != Items.BARRIER) {
            return false;
        }
        return string.contains("\u043d\u0430\u0437\u0430\u0434") || string.contains("back") || string.contains("\u043c\u0435\u043d\u044e") || string.contains("\u0432\u044b\u0445\u043e\u0434") || string.contains("\u0437\u0430\u043a\u0440\u044b\u0442\u044c");
    }

    private int firstValidSlot(int n, int n2) {
        return n < 0 ? n2 : n;
    }

    private String stripFormatting(String string) {
        if (string == null) {
            return "";
        }
        return string.replaceAll("\u00a7.", "").toLowerCase(Locale.ROOT);
    }

    private long calculateServerJitter(BotController botController) {
        String string = botController.getBotName();
        return string == null ? 0L : Math.floorMod((long)string.hashCode(), Math.max(1L, this.retryIntervalMillis));
    }

    @Override
    public String getDescription() {
        return "AutoJoinGrief " + this.targetIndex;
    }

    static final class AuctionScreenState {
        private final boolean onSecondPage;
        private final boolean hasHints;
        private final int backSlot;
        private final int mainMenuSlot;
        private final int nextPageSlot;
        private final int previousPageSlot;
        private final int targetGriefSlot;

        AuctionScreenState(boolean bl, boolean bl2, int n, int n2, int n3, int n4, int n5) {
            this.onSecondPage = bl;
            this.hasHints = bl2;
            this.backSlot = n;
            this.mainMenuSlot = n2;
            this.nextPageSlot = n3;
            this.previousPageSlot = n4;
            this.targetGriefSlot = n5;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "onSecondPage", "hasHints", "backSlot", "mainMenuSlot", "nextPageSlot", "previousPageSlot", "targetGriefSlot");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "onSecondPage", "hasHints", "backSlot", "mainMenuSlot", "nextPageSlot", "previousPageSlot", "targetGriefSlot");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "onSecondPage", "hasHints", "backSlot", "mainMenuSlot", "nextPageSlot", "previousPageSlot", "targetGriefSlot");
        }

        public boolean isOnSecondPage() {
            return this.onSecondPage;
        }

        public boolean hasHints() {
            return this.hasHints;
        }

        public int getBackSlot() {
            return this.backSlot;
        }

        public int getNextPageSlot() {
            return this.nextPageSlot;
        }

        public int getPreviousPageSlot() {
            return this.previousPageSlot;
        }

        public int getTargetGriefSlot() {
            return this.targetGriefSlot;
        }

        public int getMainMenuSlot() {
            return this.mainMenuSlot;
        }
    }
}
