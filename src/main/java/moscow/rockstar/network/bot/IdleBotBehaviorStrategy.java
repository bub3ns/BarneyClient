package moscow.rockstar.network.bot;

/** Keeps a connected bot stationary when no behavior has been selected. */
public final class IdleBotBehaviorStrategy implements BotBehaviorStrategy {
    @Override
    public void applyBehavior(BotController controller) {
        if (controller != null) {
            controller.applyMotionDamping();
        }
    }

    @Override
    public String getDescription() {
        return "Idle";
    }
}
