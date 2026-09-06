package ua.mintantileak.profile;

import net.minecraft.client.MinecraftClient;
import ua.mintantileak.profile.Role;

public final class Profile {
    public static String username = "";
    public static int uid = 0;
    public static Role role = Role.DEFAULT;
    public static String hwid = "";
    public static String subscriptionEndDate = "";
    public static String avatarUrl = "";

    private Profile() {
    }

    /**
     * The in-game account name. The build this was recovered from had the licence
     * lookup patched out and returned a hardcoded constant; it now reports the
     * Minecraft session name instead, falling back to whatever was assigned to
     * {@link #username} if the session is not up yet (very early startup).
     */
    public static String getUsername() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (minecraftClient != null && minecraftClient.getSession() != null) {
            String string = minecraftClient.getSession().getUsername();
            if (string != null && !string.isEmpty()) {
                return string;
            }
        }
        return username;
    }

    public static int getUid() {
        return uid;
    }

    public static Role getRole() {
        return role;
    }

    public static String getHwid() {
        return hwid;
    }

    public static String getSubscriptionEndDate() {
        return subscriptionEndDate;
    }

    public static String getAvatarUrl() {
        return avatarUrl;
    }
}
