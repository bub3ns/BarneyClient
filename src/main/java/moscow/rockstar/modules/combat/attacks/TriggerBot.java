/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.UseAction
 *  net.minecraft.ClientPlayerEntity
 */
package moscow.rockstar.modules.combat.attacks;

import moscow.rockstar.combat.critical.CriticalHitTiming;
import moscow.rockstar.combat.critical.SprintResetPolicy;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.core.TargetFilter;
import moscow.rockstar.entity.targeting.TargetActionQueue;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.modules.combat.attacks.Criticals;
import moscow.rockstar.modules.combat.defense.KnockbackTweaks;
import moscow.rockstar.modules.combat.targeting.AntiBot;
import moscow.rockstar.render.esp.EntityOverlayGeometry;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.consume.UseAction;
import net.minecraft.client.network.ClientPlayerEntity;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Trigger Bot", category=ModuleCategory.COMBAT, description="modules.descriptions.trigger_bot")
public class TriggerBot
extends Module {
    private BooleanSetting onlyCrits;
    private BooleanSetting smartCriticals;
    private BooleanSetting useHit;
    private MultiBooleanSetting targets;
    private MultiBooleanSetting.Option players;
    private MultiBooleanSetting.Option animals;
    private MultiBooleanSetting.Option mobs;
    private MultiBooleanSetting.Option invisibles;
    private MultiBooleanSetting.Option nakedPlayers;
    private MultiBooleanSetting.Option rockUsers;
    private MultiBooleanSetting.Option friends;
    private ModeSetting sprintReset;
    private ModeSetting.Option smart;
    private ModeSetting.Option normal;
    private ModeSetting.Option packet;
    private final Timer cooldownTimer = new Timer();

    public TriggerBot() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.onlyCrits = new BooleanSetting(this, "modules.settings.aura.onlyCrits").enable();
        this.smartCriticals = new BooleanSetting((SettingOwner)this, "modules.settings.aura.smart_criticals", () -> !this.onlyCrits.isEnabled());
        this.useHit = new BooleanSetting(this, "modules.settings.aura.useHit");
        this.targets = new MultiBooleanSetting(this, "modules.settings.aura.targets");
        this.players = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.players").select();
        this.animals = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.animals").select();
        this.mobs = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.mobs").select();
        this.invisibles = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.invisibles").select();
        this.nakedPlayers = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.nakedPlayers").select();
        this.rockUsers = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.rockUsers");
        this.friends = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.friends");
        this.sprintReset = new ModeSetting(this, "modules.settings.aura.sprint_reset");
        this.smart = new ModeSetting.Option(this.sprintReset, "modules.settings.aura.sprint_reset.smart").select();
        this.normal = new ModeSetting.Option(this.sprintReset, "modules.settings.aura.sprint_reset.normal");
        this.packet = new ModeSetting.Option(this.sprintReset, "modules.settings.aura.sprint_reset.packet");
    }

    @Override
    @Compile(obfuscation=1)
    public void onTick() {
        if (TriggerBot.minecraftClient.player == null || TriggerBot.minecraftClient.interactionManager == null) {
            return;
        }
        if (this.isAttackItemReady()) {
            super.onTick();
            return;
        }
        Entity class_12972 = TriggerBot.minecraftClient.targetedEntity;
        if (class_12972 instanceof LivingEntity) {
            LivingEntity class_13092 = (LivingEntity)class_12972;
            if (this.buildTargetFilter().acceptsEntity((Entity)class_13092)) {
                if (TargetActionQueue.hasDeferredAction()) {
                    super.onTick();
                    return;
                }
                if (this.isSmartCriticalReady() && SprintResetPolicy.shouldDeferAttack((PlayerEntity)TriggerBot.minecraftClient.player)) {
                    TargetActionQueue.setCurrentTarget((PlayerEntity)TriggerBot.minecraftClient.player);
                    super.onTick();
                    return;
                }
                if (this.isEntityValid(class_13092)) {
                    if (this.queueTargetAttack(class_13092)) {
                        super.onTick();
                        return;
                    }
                    this.attackTarget(class_13092);
                }
            }
        }
        super.onTick();
    }

    @Compile(obfuscation=1)
    private boolean isEntityValid(LivingEntity class_13092) {
        if (TriggerBot.minecraftClient.player == null) {
            return false;
        }
        if (this.isAttackItemReady()) {
            return false;
        }
        if (AntiBot.isEntityValid(class_13092)) {
            return false;
        }
        if (class_13092 == TriggerBot.minecraftClient.player || class_13092.isRemoved() || !class_13092.isAlive()) {
            return false;
        }
        if (!this.buildTargetFilter().acceptsEntity((Entity)class_13092)) {
            return false;
        }
        if (TriggerBot.minecraftClient.player.getAttackCooldownProgress(0.0f) < 0.8f || !this.cooldownTimer.hasElapsed(500L)) {
            return false;
        }
        Criticals criticals = RockstarClient.create().getModuleRegistry().getModule(Criticals.class);
        if (criticals.isCriticalsEnvironmentReady() && !criticals.isCriticalsTargetReady()) {
            return false;
        }
        return !this.isSmartCriticalReady() || !this.isEntityAlive(class_13092) || EntityOverlayGeometry.isValidTarget(class_13092, true);
    }

    private boolean isAttackItemReady() {
        if (!this.useHit.isEnabled() || TriggerBot.minecraftClient.player == null || !TriggerBot.minecraftClient.player.isUsingItem()) {
            return false;
        }
        return TriggerBot.minecraftClient.player.getActiveItem().getItem().getUseAction(TriggerBot.minecraftClient.player.getActiveItem()) == UseAction.EAT;
    }

    private TargetFilter buildTargetFilter() {
        return new TargetFilter.Builder().players(this.players.isSelected()).animals(this.animals.isSelected()).mobs(this.mobs.isSelected()).invisibles(this.invisibles.isSelected()).nakedPlayers(this.nakedPlayers.isSelected()).friends(this.friends.isSelected()).rockstarUsers(this.rockUsers.isSelected()).excludeTeammates(false).build();
    }

    private boolean isEntityAlive(LivingEntity class_13092) {
        float f = RockstarClient.create().getModuleRegistry().getModule(Aura.class).calculateAttackRotation(class_13092);
        return f <= class_13092.getHealth();
    }

    private void attackTarget(LivingEntity class_13092) {
        TriggerBot.minecraftClient.interactionManager.attackEntity((PlayerEntity)TriggerBot.minecraftClient.player, (Entity)class_13092);
        TriggerBot.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        this.cooldownTimer.reset();
    }

    private boolean isSmartCriticalReady() {
        if (this.smartCriticals.isEnabled()) {
            return TriggerBot.minecraftClient.options != null && TriggerBot.minecraftClient.options.jumpKey.isPressed() || !TriggerBot.minecraftClient.player.isOnGround();
        }
        return this.onlyCrits.isEnabled();
    }

    private boolean queueTargetAttack(LivingEntity class_13092) {
        boolean bl = this.sprintReset.isSelected(this.normal);
        boolean bl2 = this.sprintReset.isSelected(this.packet);
        if (RockstarClient.create().getModuleRegistry().getModule(KnockbackTweaks.class).isEnabled()) {
            return false;
        }
        if (!bl && !bl2 || TriggerBot.minecraftClient.player == null) {
            return false;
        }
        if (TargetActionQueue.hasDeferredAction() || TargetActionQueue.isCurrentTarget((Entity)TriggerBot.minecraftClient.player)) {
            return true;
        }
        if (!TriggerBot.minecraftClient.player.isSprinting()) {
            TargetActionQueue.clearTarget((Entity)TriggerBot.minecraftClient.player);
            return false;
        }
        TargetActionQueue.queueTargetAction((PlayerEntity)TriggerBot.minecraftClient.player, () -> this.resetTarget(class_13092), bl2);
        return true;
    }

    private void resetTarget(LivingEntity class_13092) {
        if (!this.isEnabled() || TriggerBot.minecraftClient.player == null || TriggerBot.minecraftClient.interactionManager == null || class_13092 == null || class_13092.isRemoved() || !class_13092.isAlive()) {
            return;
        }
        if (this.isEntityValid(class_13092)) {
            this.attackTarget(class_13092);
        }
    }

    public boolean isTriggerAttackReady() {
        LivingEntity class_13092;
        Object object;
        block8: {
            block7: {
                if (!this.sprintReset.isSelected(this.smart)) {
                    return false;
                }
                if (RockstarClient.create().getModuleRegistry().getModule(KnockbackTweaks.class).isEnabled()) {
                    return false;
                }
                object = TriggerBot.minecraftClient.targetedEntity;
                if (!(object instanceof LivingEntity)) break block7;
                class_13092 = (LivingEntity)object;
                if (TriggerBot.minecraftClient.player != null) break block8;
            }
            return false;
        }
        if (!this.buildTargetFilter().acceptsEntity((Entity)class_13092)) {
            return false;
        }
        if (TriggerBot.minecraftClient.player.isSubmergedInWater()) {
            return false;
        }
        object = RockstarClient.create().getModuleRegistry().getModule(Criticals.class);
        boolean criticalHitReady = ((Criticals)object).isCriticalsEnvironmentReady() && (((Criticals)object).isCriticalsStateReady() && this.cooldownTimer.hasElapsed(500L) || TriggerBot.minecraftClient.player.isOnGround()) || !TriggerBot.minecraftClient.player.isOnGround() && CriticalHitTiming.isCriticalWindowReady(TriggerBot.minecraftClient.player, EntityOverlayGeometry.getEntityHeight(class_13092), ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) || ServerDetector.isServerProfileSupported(ServerProfile.FUNSKY) || ServerDetector.isInventoryServer() ? MathUtils.RANDOM.nextInt(3) : 1);
        return this.isSmartCriticalReady() && this.isEntityAlive(class_13092) && (criticalHitReady || EntityOverlayGeometry.isValidTarget(class_13092, true) || !this.cooldownTimer.hasElapsed(ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY) || ServerDetector.isInventoryServer() ? (long)MathUtils.interpolateRandomStrategy(50.0f, 150.0f) : 50L));
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (TriggerBot.minecraftClient.player != null) {
            TargetActionQueue.clearTarget((Entity)TriggerBot.minecraftClient.player);
        }
    }
}
