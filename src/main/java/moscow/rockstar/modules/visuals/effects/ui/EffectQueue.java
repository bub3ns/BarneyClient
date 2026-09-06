package moscow.rockstar.modules.visuals.effects.ui;

import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.core.UiComponentProcessor;
import moscow.rockstar.ui.notifications.ItemNotification;
import moscow.rockstar.ui.notifications.NotificationSeverity;
import moscow.rockstar.ui.notifications.NotificationToast;
import moscow.rockstar.ui.notifications.NotificationType;
import moscow.rockstar.ui.notifications.StatusEffectNotification;
import moscow.rockstar.ui.notifications.TimedNotification;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import pyrock.utility.render.ColorRGBA;

/** Compatibility facade for the client's single notification processor. */
public final class EffectQueue {
    private final UiComponentProcessor notificationProcessor;

    public EffectQueue() {
        this.notificationProcessor = RockstarClient.create().getUiComponentProcessor();
    }

    public void enqueueSeverityNotification(NotificationSeverity severity, String message) {
        NotificationType type = NotificationType.fromKey(severity == null ? "info" : severity.getSeverityKey());
        this.notificationProcessor.enqueueNotification(new NotificationToast(type, message, ""));
    }

    public void enqueueNotificationWithSubtitle(NotificationSeverity severity, String title, String subtitle) {
        NotificationType type = NotificationType.fromKey(severity == null ? "info" : severity.getSeverityKey());
        this.notificationProcessor.enqueueNotification(new NotificationToast(type, title, subtitle));
    }

    public void enqueueItemEffect(String message, ItemStack itemStack) {
        this.notificationProcessor.enqueueNotification(new ItemNotification(message, itemStack));
    }

    public void enqueueStatusEffect(String message, String highlightedText, RegistryEntry<StatusEffect> statusEffect) {
        this.notificationProcessor.enqueueNotification(new StatusEffectNotification(message, statusEffect)
            .withHighlightedText(highlightedText));
    }

    public void enqueueColoredItemEffect(String message, String highlightedText, ItemStack itemStack, ColorRGBA highlightColor) {
        this.notificationProcessor.enqueueNotification(new ItemNotification(message, itemStack)
            .withHighlightedText(highlightedText)
            .withHighlightColor(highlightColor));
    }

    public void enqueueItemEffect(String message, String highlightedText, ItemStack itemStack) {
        this.notificationProcessor.enqueueNotification(new ItemNotification(message, itemStack)
            .withHighlightedText(highlightedText));
    }

    public List<TimedNotification> getActiveNotifications() {
        return this.notificationProcessor.getNotifications();
    }
}
