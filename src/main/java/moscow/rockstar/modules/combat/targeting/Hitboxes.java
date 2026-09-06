/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.ClientPlayerEntity
 */
package moscow.rockstar.modules.combat.targeting;

import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.core.TargetFilter;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Hitboxes", category=ModuleCategory.COMBAT, description="modules.descriptions.hitboxes")
public class Hitboxes
extends Module {
    private NumberSetting size;
    private MultiBooleanSetting targets;
    private MultiBooleanSetting.Option players;
    private MultiBooleanSetting.Option animals;
    private MultiBooleanSetting.Option mobs;
    private MultiBooleanSetting.Option invisibles;
    private MultiBooleanSetting.Option nakedPlayers;
    private MultiBooleanSetting.Option rockusersOption;
    private MultiBooleanSetting.Option friends;

    public Hitboxes() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.size = new NumberSetting(this, "modules.settings.hitboxes.size").setMinValue(0.0f).setMaxValue(1.0f).setStep(0.1f).setValue(0.3f);
        this.targets = new MultiBooleanSetting(this, "modules.settings.hitboxes.targets");
        this.players = new MultiBooleanSetting.Option(this.targets, "modules.settings.hitboxes.targets.players").select();
        this.animals = new MultiBooleanSetting.Option(this.targets, "modules.settings.hitboxes.targets.animals").select();
        this.mobs = new MultiBooleanSetting.Option(this.targets, "modules.settings.hitboxes.targets.mobs").select();
        this.invisibles = new MultiBooleanSetting.Option(this.targets, "modules.settings.hitboxes.targets.invisibles").select();
        this.nakedPlayers = new MultiBooleanSetting.Option(this.targets, "modules.settings.hitboxes.targets.naked_players").select();
        this.rockusersOption = new MultiBooleanSetting.Option(this.targets, "rockUsers");
        this.friends = new MultiBooleanSetting.Option(this.targets, "modules.settings.hitboxes.targets.friends");
    }

    public boolean isEntityAlive(LivingEntity class_13092) {
        if (class_13092 == null) {
            return false;
        }
        TargetFilter targetFilter = new TargetFilter.Builder().players(this.players.isSelected()).animals(this.animals.isSelected()).mobs(this.mobs.isSelected()).invisibles(this.invisibles.isSelected()).nakedPlayers(this.nakedPlayers.isSelected()).friends(this.friends.isSelected()).rockstarUsers(this.rockusersOption.isSelected()).build();
        if (class_13092 instanceof ClientPlayerEntity) {
            return false;
        }
        if (class_13092.isDead()) {
            return false;
        }
        if (RockstarClient.create().isPanicMode()) {
            return false;
        }
        return targetFilter.acceptsEntity((Entity)class_13092);
    }

    @Generated
    public NumberSetting getSize() {
        return this.size;
    }
}
