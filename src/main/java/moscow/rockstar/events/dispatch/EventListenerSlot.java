/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.events.dispatch;

import java.util.Optional;
import moscow.rockstar.api.validation.ScreenStateService;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.events.dispatch.EventPublisher;
import org.jetbrains.annotations.Nullable;
import pyrock.events.player.ClientPlayerTickEvent;

public final class EventListenerSlot {
    @Nullable
    private ScreenStateService pendingScreenState;
    private final EventListener<ClientPlayerTickEvent> tickListener = clientPlayerTickEvent -> {
        if (this.pendingScreenState == null) {
            return;
        }
        try {
            ScreenStateService screenStateService = this.pendingScreenState;
            if (!screenStateService.tickNavigation()) {
                return;
            }
            if (this.pendingScreenState == screenStateService) {
                this.pendingScreenState = null;
            }
            if (screenStateService.isTargetReached()) {
                EventPublisher.publishNewtonFinished(screenStateService.getCommandName());
            } else {
                String string = screenStateService.getErrorMessage();
                EventPublisher.publishNewtonFailed(screenStateService.getCommandName(), string == null ? "\u043f\u0440\u0435\u0440\u0432\u0430\u043d" : string);
            }
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
            this.cancelPendingScreenState();
        }
    };

    public void scheduleScreenState(ScreenStateService screenStateService) {
        this.cancelPendingScreenState();
        this.pendingScreenState = screenStateService;
        EventPublisher.publishNewtonStarted(screenStateService.getCommandName());
    }

    public void cancelPendingScreenState() {
        if (this.pendingScreenState == null) {
            return;
        }
        ScreenStateService screenStateService = this.pendingScreenState;
        this.pendingScreenState = null;
        try {
            screenStateService.stopNavigation();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        EventPublisher.publishNewtonFailed(screenStateService.getCommandName(), "\u043e\u0442\u043c\u0435\u043d\u0451\u043d");
    }

    public Optional<ScreenStateService> getPendingScreenState() {
        return Optional.ofNullable(this.pendingScreenState);
    }

    public boolean hasPendingScreenState() {
        return this.pendingScreenState != null;
    }

    public static EventListenerSlot createRegisteredSlot(ClientServiceRegistry clientServiceRegistry) {
        EventListenerSlot eventListenerSlot = new EventListenerSlot();
        clientServiceRegistry.getEventBus().registerListeners(eventListenerSlot);
        return eventListenerSlot;
    }
}

