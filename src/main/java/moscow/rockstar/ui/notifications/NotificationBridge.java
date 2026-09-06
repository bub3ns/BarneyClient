/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 */
package moscow.rockstar.ui.notifications;

import moscow.rockstar.core.ClientFeatureFlags;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;

public final class NotificationBridge {
    private NotificationBridge() {
    }

    public static void showNotification(Text class_25612) {
        Notification.info(class_25612);
    }

    public static void showMessage(String string) {
        Notification.info((Text)Text.literal((String)string));
    }

    public static void showPersistentNotification(Text class_25612) {
        Notification.error(class_25612);
    }

    public static void showPersistentMessage(String string) {
        Notification.error((Text)Text.literal((String)string));
    }

    public static void showLoggedMessage(String string) {
        if (!ClientFeatureFlags.loggingEnabled) {
            return;
        }
        Notification.info((Text)Text.literal((String)string));
    }
}

