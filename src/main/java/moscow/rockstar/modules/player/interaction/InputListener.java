package moscow.rockstar.modules.player.interaction;

import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.player.placement.Scaffold;
import pyrock.events.player.InputEvent;

public final class InputListener implements EventListener<InputEvent> {
    private final Scaffold scaffoldModule;

    public InputListener(Scaffold scaffoldModule) {
        this.scaffoldModule = scaffoldModule;
    }

    public void onInput(InputEvent inputEvent) {
        this.scaffoldModule.processInput(inputEvent);
    }

    @Override
    public int getPriority() {
        return -1;
    }

    @Override
    public void onEvent(InputEvent event) {
        this.onInput(event);
    }
}
