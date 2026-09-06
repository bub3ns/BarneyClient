/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Text
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.core;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.core.TargetFilter;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class FriendManager
implements ClientAccess {
    @Nullable
    private Entity targetEntity = null;
    private final Set<String> friendNames = new LinkedHashSet<String>();

    public final void updateTarget(TargetFilter targetFilter) {
        this.targetEntity = this.findBestTarget(targetFilter);
    }

    public final void addFriend(String string) {
        if (RockstarClient.create().getFriendListManager().getFriends().contains(string)) {
            Notification.error(Text.of((String)Localization.translate("commands.target.friend_error")));
            return;
        }
        if (this.friendNames.contains(string)) {
            Notification.error(Text.of((String)Localization.translateFormatted("commands.target.already_exists", string)));
            return;
        }
        if (string.equalsIgnoreCase(minecraftClient.getSession().getUsername())) {
            Notification.error(Text.of((String)Localization.translate("commands.target.self_error")));
            return;
        }
        this.friendNames.add(string);
        Notification.info(Text.of((String)Localization.translateFormatted("commands.target.added", string)));
    }

    public final void removeFriend(String string) {
        if (!this.friendNames.contains(string)) {
            Notification.error(Text.of((String)Localization.translateFormatted("commands.target.not_found", string)));
            return;
        }
        this.friendNames.remove(string);
        Notification.info(Text.of((String)Localization.translateFormatted("commands.target.removed", string)));
    }

    public final void clearFriends() {
        if (this.friendNames.isEmpty()) {
            Notification.info(Text.of((String)Localization.translate("commands.target.empty")));
            return;
        }
        this.friendNames.clear();
        Notification.info(Text.of((String)Localization.translate("commands.target.cleared")));
    }

    public final void listFriends() {
        if (this.friendNames.isEmpty()) {
            Notification.info(Text.of((String)Localization.translate("commands.target.empty")));
            return;
        }
        int n = 1;
        for (String string : this.friendNames) {
            Notification.info(Text.of((String)String.format(Localization.translate("commands.target.list_entry"), n++, string)));
        }
    }

    @Nullable
    public final Entity findBestTarget(TargetFilter targetFilter) {
        if (FriendManager.minecraftClient.world == null) {
            return null;
        }
        Entity class_12972 = null;
        boolean bl = false;
        for (Entity class_12973 : FriendManager.minecraftClient.world.getEntities()) {
            boolean bl2;
            if (!targetFilter.acceptsEntity(class_12973)) continue;
            boolean bl3 = bl2 = !this.friendNames.isEmpty() && this.friendNames.contains(class_12973.getName().getString());
            if (class_12972 == null) {
                class_12972 = class_12973;
                bl = bl2;
                continue;
            }
            if (bl2 && !bl) {
                class_12972 = class_12973;
                bl = true;
                continue;
            }
            if (bl2 != bl || targetFilter.getSortComparator().compare(class_12973, class_12972) >= 0) continue;
            class_12972 = class_12973;
        }
        return class_12972;
    }

    public final void clearTarget() {
        this.targetEntity = null;
    }

    public final boolean isFriend(String string) {
        return this.friendNames.contains(string);
    }

    public final LivingEntity getTargetLivingEntity() {
        LivingEntity class_13092;
        Entity class_12972 = RockstarClient.create().getFriendManager().getTargetEntity();
        return class_12972 instanceof LivingEntity ? (class_13092 = (LivingEntity)class_12972) : null;
    }

    @Nullable
    @Generated
    public Entity getTargetEntity() {
        return this.targetEntity;
    }

    @Generated
    public Set<String> getFriends() {
        return this.friendNames;
    }
}

