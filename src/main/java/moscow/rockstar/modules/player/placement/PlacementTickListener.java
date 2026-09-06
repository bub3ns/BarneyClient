package moscow.rockstar.modules.player.placement;

import moscow.rockstar.events.EventListener;
import pyrock.events.player.ClientPlayerTickEvent;

final class PlacementTickListener implements EventListener<ClientPlayerTickEvent> {
    private final Scaffold scaffoldModule;

    PlacementTickListener(Scaffold scaffoldModule) {
        this.scaffoldModule = scaffoldModule;
    }

    public void onClientPlayerTick(ClientPlayerTickEvent event) {
        this.scaffoldModule.resetScaffold();
        this.scaffoldModule.resetTargetState();
        this.scaffoldModule.resetPlacementState();
        this.scaffoldModule.resetMotionState();
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public void onEvent(ClientPlayerTickEvent event) {
        this.onClientPlayerTick(event);
    }
}
