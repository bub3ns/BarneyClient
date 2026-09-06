/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  org.jetbrains.annotations.Nullable
 */
package pyrock.classes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import moscow.rockstar.core.FriendManager;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.core.TargetFilter;
import moscow.rockstar.entity.EntityProcessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class PyTarget {
    public Query query() {
        return new Query();
    }

    @Nullable
    public Entity best(Query query) {
        if (query == null) {
            return null;
        }
        return PyTarget.manager().findBestTarget(query.build());
    }

    public List<Entity> all(Query query) {
        ArrayList<Entity> arrayList = new ArrayList<Entity>();
        if (query == null || PyTarget.mc().world == null) {
            return arrayList;
        }
        TargetFilter targetFilter = query.build();
        for (Entity class_12972 : PyTarget.mc().world.getEntities()) {
            if (!targetFilter.acceptsEntity(class_12972)) continue;
            arrayList.add(class_12972);
        }
        arrayList.sort(targetFilter.getSortComparator());
        return arrayList;
    }

    public boolean valid(Query query, Entity class_12972) {
        return query != null && class_12972 != null && query.build().acceptsEntity(class_12972);
    }

    @Nullable
    public Entity current() {
        return PyTarget.manager().getTargetEntity();
    }

    @Nullable
    public LivingEntity living() {
        return PyTarget.manager().getTargetLivingEntity();
    }

    public void add(String string) {
        if (string != null && !string.isBlank()) {
            PyTarget.manager().addFriend(string);
        }
    }

    public void remove(String string) {
        if (string != null && !string.isBlank()) {
            PyTarget.manager().removeFriend(string);
        }
    }

    public void clear() {
        PyTarget.manager().clearFriends();
    }

    public boolean isTarget(String string) {
        return string != null && PyTarget.manager().isFriend(string);
    }

    public List<String> list() {
        return new ArrayList<String>(PyTarget.manager().getFriends());
    }

    static Comparator<Entity> comparator(String string) {
        return switch (string.toLowerCase(Locale.ROOT)) {
            case "health", "hp" -> EntityProcessor.HEALTH_COMPARATOR;
            case "fov", "angle", "crosshair" -> EntityProcessor.FIELD_OF_VIEW_COMPARATOR;
            case "bad_armor", "weakest" -> EntityProcessor.ARMOR_VALUE_COMPARATOR;
            case "good_armor", "strongest" -> EntityProcessor.ARMOR_VALUE_DESCENDING_COMPARATOR;
            default -> EntityProcessor.DISTANCE_COMPARATOR;
        };
    }

    private static FriendManager manager() {
        return RockstarClient.create().getFriendManager();
    }

    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    public static final class Query {
        private boolean players = true;
        private boolean animals;
        private boolean mobs;
        private boolean users;
        private boolean invisibles;
        private boolean naked = true;
        private boolean friends;
        private boolean armorStands;
        private boolean excludeTeammates;
        private float range = -1.0f;
        private String sort = "distance";

        public Query players(boolean bl) {
            this.players = bl;
            return this;
        }

        public Query animals(boolean bl) {
            this.animals = bl;
            return this;
        }

        public Query mobs(boolean bl) {
            this.mobs = bl;
            return this;
        }

        public Query users(boolean bl) {
            this.users = bl;
            return this;
        }

        public Query invisibles(boolean bl) {
            this.invisibles = bl;
            return this;
        }

        public Query naked(boolean bl) {
            this.naked = bl;
            return this;
        }

        public Query friends(boolean bl) {
            this.friends = bl;
            return this;
        }

        public Query armorStands(boolean bl) {
            this.armorStands = bl;
            return this;
        }

        public Query excludeTeammates(boolean bl) {
            this.excludeTeammates = bl;
            return this;
        }

        public Query range(double d) {
            this.range = (float)d;
            return this;
        }

        public Query sort(String string) {
            this.sort = string == null ? "distance" : string;
            return this;
        }

        TargetFilter build() {
            return new TargetFilter.Builder().players(this.players).animals(this.animals).mobs(this.mobs).rockstarUsers(this.users).invisibles(this.invisibles).nakedPlayers(this.naked).friends(this.friends).armorStands(this.armorStands).excludeTeammates(this.excludeTeammates).range(this.range).sortComparator(PyTarget.comparator(this.sort)).build();
        }
    }
}

