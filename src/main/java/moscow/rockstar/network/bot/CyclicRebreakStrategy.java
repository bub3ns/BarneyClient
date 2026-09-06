/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network.bot;

import moscow.rockstar.network.bot.AuctionAutomationStrategy;
import moscow.rockstar.network.bot.BotBehaviorStrategy;
import moscow.rockstar.network.bot.BotController;
import moscow.rockstar.network.bot.WoodRebreakStrategy;

public class CyclicRebreakStrategy
implements BotBehaviorStrategy {
    private final AuctionAutomationStrategy rebreakState;
    private int rebreakCycle;
    private boolean cycleActive;

    public CyclicRebreakStrategy(int n) {
        this.rebreakState = new AuctionAutomationStrategy(n, 1);
    }

    @Override
    public void applyBehavior(BotController botController) {
        if (botController == null || !botController.isConnected()) {
            return;
        }
        if (this.cycleActive) {
            botController.applyMotionDamping();
            ++this.rebreakCycle;
            if (this.rebreakCycle >= Math.max(1, botController.getControlState().getRebreakCycleLimit())) {
                botController.setBehaviorStrategy(new WoodRebreakStrategy());
            }
            return;
        }
        this.rebreakState.applyBehavior(botController);
        if (botController.getBehaviorStrategy() instanceof IdleBotBehaviorStrategy) {
            this.cycleActive = true;
            this.rebreakCycle = 0;
            botController.setBehaviorStrategy(this);
        }
    }

    @Override
    public String getDescription() {
        return "CyclicRebreakWithRejoin";
    }
}
