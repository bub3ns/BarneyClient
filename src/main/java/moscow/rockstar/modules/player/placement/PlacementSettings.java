package moscow.rockstar.modules.player.placement;

public record PlacementSettings(boolean jump, int sneakTime, boolean stopInput, boolean stepBack) {
    static final PlacementSettings DEFAULT = new PlacementSettings(false, 0, false, false);

    public boolean shouldJump() {
        return this.jump;
    }

    public int getSneakTime() {
        return this.sneakTime;
    }

    public boolean shouldStopInput() {
        return this.stopInput;
    }

    public boolean shouldStepBack() {
        return this.stepBack;
    }
}
