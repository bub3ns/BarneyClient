/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SlimeEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Items
 *  net.minecraft.Text
 */
package moscow.rockstar.modules.player.interaction;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.social.FriendListManager;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import pyrock.events.window.KeyPressEvent;
import pyrock.events.window.MouseEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Middle Click", category=ModuleCategory.PLAYER, description="modules.descriptions.middle_click")
public class MiddleClick
extends Module {
    private MultiBooleanSetting actions;
    private MultiBooleanSetting.Option pearl;
    private MultiBooleanSetting.Option friend;
    private IntegerSetting friendKey;
    private IntegerSetting pearlKey;
    private final EventListener<KeyPressEvent> onKeyPressEvent = keyPressEvent -> this.executeAction(keyPressEvent.getKey(), keyPressEvent.getAction());
    private final EventListener<MouseEvent> onMouseEvent = mouseEvent -> this.executeAction(mouseEvent.getButton(), mouseEvent.getAction());

    public MiddleClick() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.actions = new MultiBooleanSetting(this, "modules.settings.middle_click.actions").setMaxSelections(1);
        this.pearl = new MultiBooleanSetting.Option(this.actions, "modules.settings.middle_click.pearl").select();
        this.friend = new MultiBooleanSetting.Option(this.actions, "modules.settings.middle_click.friend");
        this.friendKey = new IntegerSetting(this, "modules.settings.middle_click.friend_key", () -> !this.friend.isSelected());
        this.pearlKey = new IntegerSetting(this, "modules.settings.middle_click.pearl_key", () -> !this.pearl.isSelected());
    }

    private void executeAction(int n, int n2) {
        if (MiddleClick.minecraftClient.currentScreen == null && n2 == 1) {
            if (this.friend.isSelected() && this.friendKey.isIntValid(n)) {
                if (MiddleClick.minecraftClient.targetedEntity instanceof PlayerEntity) {
                    String string = MiddleClick.minecraftClient.targetedEntity.getName().getString();
                    FriendListManager friendListManager = RockstarClient.create().getFriendListManager();
                    if (friendListManager.containsFriend(string)) {
                        friendListManager.removeFriend(string);
                    } else {
                        friendListManager.addFriend(string);
                    }
                } else if (MiddleClick.minecraftClient.targetedEntity instanceof SlimeEntity) {
                    Notification.error(Text.of((String)Localization.translate("middle_click.slime_error")));
                }
            }
            if (this.pearl.isSelected() && this.pearlKey.isIntValid(n)) {
                InventoryUtils.selectHotbarItem(Items.ENDER_PEARL);
            }
        }
    }
}
