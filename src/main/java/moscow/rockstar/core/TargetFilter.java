/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Formatting
 *  net.minecraft.Entity
 *  net.minecraft.MobEntity
 *  net.minecraft.LivingEntity
 *  net.minecraft.AnimalEntity
 *  net.minecraft.ArmorStandEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.Team
 */
package moscow.rockstar.core;

import java.util.Comparator;
import java.util.function.Function;
import lombok.Generated;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.EntityProcessor;
import moscow.rockstar.modules.combat.targeting.AntiBot;
import net.minecraft.util.Formatting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;

public class TargetFilter
implements ClientAccess {
    boolean playersEnabled;
    boolean animalsEnabled;
    boolean mobsEnabled;
    boolean rockstarUsersEnabled;
    boolean invisiblesEnabled;
    boolean nakedPlayersEnabled;
    boolean friendsEnabled;
    boolean armorStandsEnabled;
    boolean teammateExclusionEnabled;
    float maxRange = -1.0f;
    Function<Entity, Float> rangeProvider = class_12972 -> Float.valueOf(this.maxRange);
    Comparator<Entity> sortComparator = EntityProcessor.DISTANCE_COMPARATOR;

    public boolean acceptsEntity(Entity class_12972) {
        if (TargetFilter.minecraftClient.player == null || TargetFilter.minecraftClient.world == null || class_12972 == null) {
            return false;
        }
        if (!(class_12972 instanceof LivingEntity livingEntity) || class_12972 == TargetFilter.minecraftClient.player) {
            return false;
        }
        if (!livingEntity.isAlive()) {
            return false;
        }
        if (!this.withinRange(class_12972)) {
            return false;
        }
        if (class_12972 instanceof ArmorStandEntity) {
            return this.armorStandsEnabled && (this.invisiblesEnabled || !class_12972.isInvisible());
        }
        if (class_12972 instanceof PlayerEntity class_16572) {
                if (AntiBot.isEntityValid((LivingEntity)class_16572)) {
                    return false;
                }
                boolean bl = RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString());
                if (!this.friendsEnabled && bl) {
                    return false;
                }
                if (this.teammateExclusionEnabled && TargetFilter.areTeammates((PlayerEntity)TargetFilter.minecraftClient.player, class_16572)) {
                    return false;
                }
                boolean bl2 = this.hasEmptyInventory(class_16572);
                boolean bl3 = class_16572.isInvisible();
                if (bl3 && !this.invisiblesEnabled && (!this.playersEnabled || bl2)) {
                    return false;
                }
                if (this.playersEnabled || this.nakedPlayersEnabled) {
                    if (this.playersEnabled && this.nakedPlayersEnabled) {
                        return true;
                    }
                    if (!this.playersEnabled) {
                        return false;
                    }
                    return !bl2;
                }
                return false;
        }
        if (class_12972 instanceof AnimalEntity class_14292) {
                if (class_14292.isInvisible() && !this.invisiblesEnabled) {
                    return false;
                }
                return this.animalsEnabled;
        }
        if (class_12972 instanceof MobEntity class_13082) {
                if (class_13082.isInvisible() && !this.invisiblesEnabled) {
                    return false;
                }
                return this.mobsEnabled;
        }
        return false;
    }

    public final boolean withinRange(Entity class_12972) {
        if (TargetFilter.minecraftClient.player == null) {
            return false;
        }
        float f = this.rangeProvider.apply(class_12972).floatValue();
        if (f <= 0.0f) {
            return true;
        }
        return TargetFilter.minecraftClient.player.getEyePos().distanceTo(AimRotationMath.getClosestPointOnEntityBounds(class_12972)) <= (double)f;
    }

    private boolean hasEmptyInventory(PlayerEntity class_16572) {
        for (ItemStack class_17992 : class_16572.getAllArmorItems()) {
            if (class_17992 == null || class_17992.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public static boolean areTeammates(PlayerEntity class_16572, PlayerEntity class_16573) {
        boolean bl;
        if (class_16572 == null || class_16573 == null || class_16572 == class_16573) {
            return false;
        }
        Team EmptyBlockView = class_16572.getScoreboardTeam();
        Team MapUpdateS2CPacket = class_16573.getScoreboardTeam();
        if (EmptyBlockView == null || MapUpdateS2CPacket == null) {
            return false;
        }
        Formatting Schema1483 = EmptyBlockView.getColor();
        Formatting Schema1510 = MapUpdateS2CPacket.getColor();
        boolean bl2 = Schema1483 != null && Schema1483 != Formatting.RESET;
        boolean bl3 = bl = Schema1510 != null && Schema1510 != Formatting.RESET;
        if (bl2 && bl) {
            return Schema1483 == Schema1510;
        }
        String string = EmptyBlockView.getName();
        String string2 = MapUpdateS2CPacket.getName();
        return string != null && !string.isBlank() && string.equals(string2);
    }

    @Generated
    public boolean isPlayersEnabled() {
        return this.playersEnabled;
    }

    @Generated
    public boolean isAnimalsEnabled() {
        return this.animalsEnabled;
    }

    @Generated
    public boolean isMobsEnabled() {
        return this.mobsEnabled;
    }

    @Generated
    public boolean isRockstarUsersEnabled() {
        return this.rockstarUsersEnabled;
    }

    @Generated
    public boolean isInvisiblesEnabled() {
        return this.invisiblesEnabled;
    }

    @Generated
    public boolean isNakedPlayersEnabled() {
        return this.nakedPlayersEnabled;
    }

    @Generated
    public boolean isFriendsEnabled() {
        return this.friendsEnabled;
    }

    @Generated
    public boolean isArmorStandsEnabled() {
        return this.armorStandsEnabled;
    }

    @Generated
    public boolean isTeammateExclusionEnabled() {
        return this.teammateExclusionEnabled;
    }

    @Generated
    public float getMaxRange() {
        return this.maxRange;
    }

    @Generated
    public Function<Entity, Float> getRangeProvider() {
        return this.rangeProvider;
    }

    @Generated
    public Comparator<Entity> getSortComparator() {
        return this.sortComparator;
    }

    public static class Builder {
        private final TargetFilter filter = new TargetFilter();

        public Builder players(boolean bl) {
            this.filter.playersEnabled = bl;
            return this;
        }

        public Builder animals(boolean bl) {
            this.filter.animalsEnabled = bl;
            return this;
        }

        public Builder mobs(boolean bl) {
            this.filter.mobsEnabled = bl;
            return this;
        }

        public Builder rockstarUsers(boolean bl) {
            this.filter.rockstarUsersEnabled = bl;
            return this;
        }

        public Builder invisibles(boolean bl) {
            this.filter.invisiblesEnabled = bl;
            return this;
        }

        public Builder nakedPlayers(boolean bl) {
            this.filter.nakedPlayersEnabled = bl;
            return this;
        }

        public Builder friends(boolean bl) {
            this.filter.friendsEnabled = bl;
            return this;
        }

        public Builder armorStands(boolean bl) {
            this.filter.armorStandsEnabled = bl;
            return this;
        }

        public Builder excludeTeammates(boolean bl) {
            this.filter.teammateExclusionEnabled = bl;
            return this;
        }

        public Builder range(float f) {
            this.filter.maxRange = f;
            this.filter.rangeProvider = class_12972 -> Float.valueOf(f);
            return this;
        }

        public Builder rangeProvider(Function<Entity, Float> function) {
            this.filter.rangeProvider = function;
            return this;
        }

        public Builder sortComparator(Comparator<Entity> comparator) {
            this.filter.sortComparator = comparator;
            return this;
        }

        public Builder ascendingSort(Function<Entity, Double> function) {
            this.filter.sortComparator = EntityProcessor.createAscendingComparator(function);
            return this;
        }

        public Builder descendingSort(Function<Entity, Double> function) {
            this.filter.sortComparator = EntityProcessor.createDescendingComparator(function);
            return this;
        }

        public TargetFilter build() {
            return this.filter;
        }
    }
}
