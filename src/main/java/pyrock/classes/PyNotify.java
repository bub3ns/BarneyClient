/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Item
 *  net.minecraft.Items
 *  net.minecraft.Identifier
 *  net.minecraft.Registries
 *  org.jetbrains.annotations.Nullable
 */
package pyrock.classes;

import java.util.Locale;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.audio.SoundEffectPlayer;
import moscow.rockstar.ui.core.UiComponentProcessor;
import moscow.rockstar.ui.notifications.ItemNotification;
import moscow.rockstar.ui.notifications.NotificationType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;
import pyrock.utility.render.ColorRGBA;

public class PyNotify {
    public void island(String string, String string2) {
        PyNotify.manager().enqueueNotificationRequest(PyNotify.type(string2), string == null ? "" : string);
    }

    public void crosshair(String string, String string2, String string3) {
        PyNotify.manager().enqueueToast(PyNotify.type(string3), string == null ? "" : string, string2 == null ? "" : string2);
    }

    public void item(String string, String string2, String string3, @Nullable ColorRGBA colorRGBA) {
        Item class_17922 = PyNotify.item(string2);
        ItemNotification itemNotification = new ItemNotification(string == null ? "" : string, class_17922);
        if (string3 != null && !string3.isEmpty()) {
            itemNotification.withHighlightedText(string3);
            if (colorRGBA != null) {
                itemNotification.withHighlightColor(colorRGBA);
            }
        }
        PyNotify.manager().enqueueNotification(itemNotification);
    }

    public void sound() {
        try {
            SoundEffectPlayer.playNotification(1.0f);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public int count() {
        return PyNotify.manager().getNotifications().size();
    }

    private static NotificationType type(String string) {
        if (string == null) {
            return NotificationType.INFO;
        }
        return switch (string.toLowerCase(Locale.ROOT)) {
            case "success", "ok", "good" -> NotificationType.SUCCESS;
            case "error", "fail", "bad" -> NotificationType.ERROR;
            default -> NotificationType.INFO;
        };
    }

    private static Item item(String string) {
        Identifier class_29602;
        if (string == null || string.isBlank()) {
            return Items.PAPER;
        }
        Identifier class_29603 = class_29602 = string.contains(":") ? Identifier.tryParse((String)string) : Identifier.tryParse((String)("minecraft:" + string));
        if (class_29602 == null) {
            return Items.PAPER;
        }
        Item class_17922 = (Item)Registries.ITEM.get(class_29602);
        return class_17922 == Items.AIR ? Items.PAPER : class_17922;
    }

    private static UiComponentProcessor manager() {
        return RockstarClient.create().getUiComponentProcessor();
    }
}
