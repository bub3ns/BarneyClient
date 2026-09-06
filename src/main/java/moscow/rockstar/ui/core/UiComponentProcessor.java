/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.ui.notifications.NotificationRenderer;
import moscow.rockstar.ui.notifications.NotificationRequest;
import moscow.rockstar.ui.notifications.NotificationToast;
import moscow.rockstar.ui.notifications.NotificationType;
import moscow.rockstar.ui.notifications.TimedNotification;
import pyrock.events.client.NotificationEvent;
import pyrock.events.render.HudRenderEvent;

public class UiComponentProcessor {
    private final List<TimedNotification> notifications = new CopyOnWriteArrayList<TimedNotification>();
    private final EventListener<HudRenderEvent> hudRenderListener = hudRenderEvent -> {
        float f2 = 0.0f;
        for (TimedNotification timedNotification : this.notifications) {
            timedNotification.updateVisibility();
            if (timedNotification instanceof NotificationRequest) {
                timedNotification.render(hudRenderEvent.getContext(), 0.0f);
                continue;
            }
            if (!(timedNotification instanceof NotificationRenderer) && !(timedNotification instanceof NotificationToast)) continue;
            timedNotification.render(hudRenderEvent.getContext(), f2);
            if (!(timedNotification.fadeAnimation.getValue() >= 0.5f)) continue;
            f2 += timedNotification.getHeight();
        }
        this.notifications.removeIf(TimedNotification::isFinished);
    };

    public UiComponentProcessor() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public void enqueueNotification(TimedNotification timedNotification) {
        this.runOnClientThread(() -> {
            this.notifications.add(timedNotification);
            this.publishNotificationEvent(timedNotification);
        });
    }

    public void enqueueNotificationRequest(NotificationType notificationType, String string) {
        this.enqueueNotification(new NotificationRequest(notificationType, string));
    }

    public void enqueueToast(NotificationType notificationType, String string, String string2) {
        this.enqueueNotification(new NotificationToast(notificationType, string, string2));
    }

    private void runOnClientThread(Runnable runnable) {
        if (ClientAccess.minecraftClient.isOnThread()) {
            runnable.run();
            return;
        }
        ClientAccess.minecraftClient.execute(runnable);
    }

    private void publishNotificationEvent(TimedNotification timedNotification) {
        String string;
        String string2 = "info";
        String string3 = "";
        String string4 = "";
        if (timedNotification instanceof NotificationRequest) {
            NotificationRequest notificationRequest = (NotificationRequest)timedNotification;
            string = "island";
            string2 = notificationRequest.getNotificationType().getKey();
            string4 = notificationRequest.getMessage();
        } else if (timedNotification instanceof NotificationToast) {
            NotificationToast notificationToast = (NotificationToast)timedNotification;
            string = "crosshair";
            string2 = notificationToast.getNotificationType().getKey();
            string3 = notificationToast.getTitle();
            string4 = notificationToast.getSubtitle();
        } else if (timedNotification instanceof NotificationRenderer) {
            NotificationRenderer notificationRenderer = (NotificationRenderer)timedNotification;
            string = "mini";
            string4 = notificationRenderer.getMessage();
            string3 = notificationRenderer.getHighlightedText() == null ? "" : notificationRenderer.getHighlightedText();
        } else {
            string = "other";
        }
        try {
            RockstarClient.create().getEventBus().post(new NotificationEvent(string, string2, string3 == null ? "" : string3, string4 == null ? "" : string4));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Generated
    public List<TimedNotification> getNotifications() {
        return this.notifications;
    }

    @Generated
    public EventListener<HudRenderEvent> getHudRenderListener() {
        return this.hudRenderListener;
    }


}

