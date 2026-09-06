package moscow.rockstar.modules.player.placement;

import moscow.rockstar.events.EventListener;
import pyrock.events.player.ClientPlayerTickEvent;

final class PlacementUpdateListener implements EventListener<ClientPlayerTickEvent> {
    private final Scaffold scaffoldModule;

    PlacementUpdateListener(Scaffold scaffoldModule) {
        this.scaffoldModule = scaffoldModule;
    }

    public void onClientPlayerTick(ClientPlayerTickEvent event) {
        this.scaffoldModule.resetFallbackState();
    }

    @Override
    public int getPriority() {
        return -2;
    }

    @Override
    public void onEvent(ClientPlayerTickEvent event) {
        this.onClientPlayerTick(event);
    }
}
