/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Text
 *  net.minecraft.MutableText
 */
package moscow.rockstar.ui.notifications;

import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import pyrock.utility.render.ColorRGBA;

public final class Notification
implements ClientAccess {
    private static final Text NOTIFICATION_PREFIX = Text.literal((String)"[%s]".formatted("Barney")).styled(class_25832 -> class_25832.withColor(new ColorRGBA(140.0f, 80.0f, 255.0f).getRGB()));

    public static void notify(Level level, Text class_25612) {
        Notification.sendNotification(level, class_25612, true);
    }

    public static void info(Text class_25612) {
        if (Notification.minecraftClient.player == null) {
            return;
        }
        Notification.sendNotification(Level.INFO, class_25612, false);
    }

    public static void warning(Text class_25612) {
        Notification.sendNotification(Level.WARNING, class_25612, false);
    }

    public static void error(Text class_25612) {
        Notification.sendNotification(Level.ERROR, class_25612, false);
    }

    private static void sendNotification(Level level, Text class_25612, boolean bl) {
        if (Notification.minecraftClient.player == null) {
            return;
        }
        MutableText class_52502 = Text.literal((String)"").append((Text)class_25612.copy()).styled(class_25832 -> class_25832.withColor(level.getColor().getRGB()));
        Notification.minecraftClient.player.sendMessage((Text)NOTIFICATION_PREFIX.copy().append(" ").append((Text)class_52502), bl);
    }

    public static void sendChat(Text class_25612) {
        if (Notification.minecraftClient.player != null) {
            Notification.minecraftClient.player.sendMessage(class_25612, false);
        }
    }

    @Generated
    private Notification() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static enum Level {
        WARNING("Warning", new ColorRGBA(247.0f, 206.0f, 59.0f)),
        ERROR("Error", new ColorRGBA(242.0f, 79.0f, 68.0f)),
        INFO("Info", new ColorRGBA(87.0f, 126.0f, 255.0f));
        private final String label;
        private final ColorRGBA color;

        @Generated
        public String getLabel() {
            return this.label;
        }

        @Generated
        public ColorRGBA getColor() {
            return this.color;
        }

        @Generated
        private Level(String string2, ColorRGBA colorRGBA) {
            this.label = string2;
            this.color = colorRGBA;
        }
}
}

