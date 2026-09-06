/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Entity$RemovalReason
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 */
package moscow.rockstar.modules.combat.targeting;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.mixin.accessors.EntityAccessor;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.rotation.BotProfile;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.world.navigation.PathNavigator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import pyrock.events.game.WorldChangeEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Anti Bot", category=ModuleCategory.COMBAT, description="modules.descriptions.anti_bot")
public class AntiBot
extends Module {
    public static List<Entity> botProfiles = new ArrayList<Entity>();
    private BooleanSetting removeFromWorld;
    private final Map<Integer, PlayerEntity> savedSettings = new HashMap<Integer, PlayerEntity>();
    private final Set<Integer> trackedPlayers = new HashSet<Integer>();
    private final EventListener<WorldChangeEvent> onWorldChangeEvent = worldChangeEvent -> this.restoreTrackedPlayers();

    public AntiBot() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.removeFromWorld = new BooleanSetting(this, "modules.settings.anti_bot.remove_from_world").enable();
    }

    @Override
    public void onTick() {
        if (AntiBot.minecraftClient.world == null || AntiBot.minecraftClient.player == null) {
            return;
        }
        this.clearBotProfiles();
        for (PlayerEntity class_16572 : new ArrayList<PlayerEntity>(AntiBot.minecraftClient.world.getPlayers())) {
            if (AntiBot.minecraftClient.player == class_16572) continue;
            boolean bl = this.isRealPlayerName(class_16572);
            boolean bl2 = this.parseBotProfile(class_16572).isNoPrefix();
            boolean bl3 = this.isEntityValid(class_16572);
            if (bl || bl2 || bl3) {
                if (!botProfiles.contains(class_16572)) {
                    botProfiles.add((Entity)class_16572);
                }
                if (!this.removeFromWorld.isEnabled() || this.savedSettings.containsKey(class_16572.getId())) continue;
                this.updateBotProfile(class_16572, bl3);
                continue;
            }
            botProfiles.remove(class_16572);
        }
    }

    private void updateBotProfile(PlayerEntity class_16572, boolean bl) {
        this.savedSettings.put(class_16572.getId(), class_16572);
        if (bl) {
            this.trackedPlayers.add(class_16572.getId());
        }
        assert (AntiBot.minecraftClient.world != null);
        AntiBot.minecraftClient.world.removeEntity(class_16572.getId(), Entity.RemovalReason.DISCARDED);
    }

    private void clearBotProfiles() {
        Iterator<Integer> iterator = this.trackedPlayers.iterator();
        while (iterator.hasNext()) {
            int n = iterator.next();
            PlayerEntity class_16572 = this.savedSettings.get(n);
            if (class_16572 == null) {
                iterator.remove();
                continue;
            }
            if (EntityUtils.getPlayerHealth(class_16572) <= 0.0f) continue;
            ((EntityAccessor)class_16572).invokeUnsetRemoved();
            AntiBot.minecraftClient.world.addEntity((Entity)class_16572);
            botProfiles.remove(class_16572);
            this.savedSettings.remove(n);
            iterator.remove();
        }
    }

    private boolean isEntityValid(PlayerEntity class_16572) {
        if (class_16572 == null) {
            return false;
        }
        return !(EntityUtils.getPlayerHealth(class_16572) > 0.0f);
    }

    private boolean isRealPlayerName(PlayerEntity class_16572) {
        if (class_16572 == null) {
            return false;
        }
        String string = class_16572.getName().getString();
        UUID uUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + string).getBytes(StandardCharsets.UTF_8));
        boolean bl = !class_16572.getUuid().equals(uUID);
        boolean bl2 = !string.contains("NPC") && !string.startsWith("[ZNPC]");
        return bl && bl2;
    }

    private BotProfile parseBotProfile(PlayerEntity class_16572) {
        String string = class_16572.getName().getString();
        String string2 = class_16572.getDisplayName().getString();
        int n = string2.indexOf(string);
        String string3 = n > 0 ? string2.substring(0, n).trim() : "";
        return new BotProfile(string2, string3, string3.isBlank());
    }

    public static boolean isEntityValid(LivingEntity class_13092) {
        return class_13092 instanceof PlayerEntity && botProfiles.contains(class_13092);
    }

    @Override
    public void onDisable() {
        this.restoreTrackedPlayers();
        super.onDisable();
    }

    private void restoreTrackedPlayers() {
        botProfiles.clear();
        this.savedSettings.clear();
        this.trackedPlayers.clear();
    }
}
