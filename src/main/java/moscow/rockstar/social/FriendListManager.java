/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 */
package moscow.rockstar.social;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;

public class FriendListManager
implements ClientAccess {
    private final List<String> customFriends = new ArrayList<String>();
    private Set<String> allFriends = Set.of();
    private boolean friendListDirty = true;

    public final void addFriend(String string) {
        if (string == null || string.isBlank()) {
            return;
        }
        if (RockstarClient.create().getFriendManager().getFriends().contains(string)) {
            Notification.error(Text.of((String)Localization.translate("commands.friends.target")));
            return;
        }
        if (this.getCurrentFriends().contains(string)) {
            Notification.info(Text.of((String)Localization.translateFormatted("commands.friends.exists", string)));
            return;
        }
        if (string.equalsIgnoreCase(minecraftClient.getSession().getUsername())) {
            Notification.error(Text.of((String)Localization.translate("commands.friends.self")));
            return;
        }
        this.customFriends.add(string);
        this.friendListDirty = true;
        Notification.info(Text.of((String)Localization.translateFormatted("commands.friends.added", string)));
        if (EntityUtils.isClientWorldReady()) {
            moscow.rockstar.modules.config.ModuleConfigurationStore.saveConfiguration();
        }
    }

    public final void removeFriend(String string) {
        if (string == null) {
            return;
        }
        if (this.customFriends.contains(string)) {
            this.customFriends.remove(string);
            this.friendListDirty = true;
            Notification.info(Text.of((String)Localization.translateFormatted("commands.friends.removed", string)));
        } else {
            Notification.info(Text.of((String)Localization.translateFormatted("commands.friends.not_exists", string)));
        }
        moscow.rockstar.modules.config.ModuleConfigurationStore.saveConfiguration();
    }

    public final void clearFriends() {
        if (this.customFriends.isEmpty()) {
            Notification.error(Text.of((String)Localization.translate("commands.friends.empty")));
        } else {
            this.customFriends.clear();
            this.friendListDirty = true;
            Notification.info(Text.of((String)Localization.translate("commands.friends.cleared")));
            moscow.rockstar.modules.config.ModuleConfigurationStore.saveConfiguration();
        }
    }

    public final boolean replaceCustomFriends(List<String> list) {
        this.customFriends.clear();
        this.friendListDirty = true;
        if (list == null) {
            return false;
        }
        boolean bl = false;
        for (String string : list) {
            if (string != null && !string.isBlank() && !this.customFriends.contains(string)) {
                this.customFriends.add(string);
                continue;
            }
            bl = true;
        }
        return bl;
    }

    private Set<String> getAllFriends() {
        if (!this.friendListDirty) {
            return this.allFriends;
        }
        this.allFriends = new LinkedHashSet<String>(this.customFriends);
        this.friendListDirty = false;
        return this.allFriends;
    }

    private List<String> getCurrentFriends() {
        return new ArrayList<String>(this.getAllFriends());
    }

    public final List<String> getFriends() {
        return List.copyOf(this.getAllFriends());
    }

    public final boolean containsFriend(String string) {
        if (string == null) {
            return false;
        }
        return this.getAllFriends().contains(string);
    }
}
