/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.notifications;

import lombok.Generated;
import moscow.rockstar.ui.notifications.NotificationType;
import moscow.rockstar.ui.notifications.TimedNotification;
import pyrock.utility.render.CustomDrawContext;

public class NotificationRequest
extends TimedNotification {
    private final NotificationType notificationType;
    private final String message;

    public NotificationRequest(NotificationType notificationType, String string) {
        super(1000L);
        this.notificationType = notificationType;
        this.message = string;
    }

    @Override
    public void render(CustomDrawContext customDrawContext, float f) {
    }

    @Generated
    public NotificationType getNotificationType() {
        return this.notificationType;
    }

    @Generated
    public String getMessage() {
        return this.message;
    }
}

