/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  moscow.rockstar.render.RenderGate
 *  moscow.rockstar.render.esp.TargetRenderModule
 *  net.minecraft.Entity
 *  net.minecraft.AnimalEntity
 *  net.minecraft.ItemEntity
 *  net.minecraft.HostileEntity
 *  net.minecraft.PlayerEntity
 */
package moscow.rockstar.render.esp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.visuals.esp.targeting.ItemTargetType;
import moscow.rockstar.modules.visuals.esp.targeting.PlayerTargetGroup;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.render.esp.TargetRenderModule;
import moscow.rockstar.settings.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import pyrock.events.render.Render3DEvent;

public class EntityTargetRenderer
extends TargetRenderModule {
    private final Object owner;
    private final BooleanSetting enabledSetting;
    private volatile EntityRenderer entityRenderer;
    private volatile EntityBatchRenderer entityBatchRenderer;
    private volatile Predicate<Entity> targetFilter;
    private final EventListener<Render3DEvent> render3DEventListener = render3DEvent -> {
        if (this.entityRenderer == null && this.entityBatchRenderer == null) {
            return;
        }
        if (ClientAccess.minecraftClient.world == null || ClientAccess.minecraftClient.player == null) {
            return;
        }
        if (!OverlayRegistry.isEspEnabled() || !this.isValid()) {
            return;
        }
        ArrayList<Entity> arrayList = new ArrayList<Entity>();
        for (Entity entity : ClientAccess.minecraftClient.world.getEntities()) {
            if (!this.isTargetEnabled(entity) || this.targetFilter != null && !this.targetFilter.test(entity)) continue;
            arrayList.add(entity);
        }
        if (arrayList.isEmpty()) {
            return;
        }
        EntityBatchRenderer entityBatchRenderer = this.entityBatchRenderer;
        if (entityBatchRenderer != null) {
            entityBatchRenderer.render((List<Entity>)arrayList, (Render3DEvent)render3DEvent);
            return;
        }
        EntityRenderer renderer = this.entityRenderer;
        if (renderer == null) {
            return;
        }
        for (Entity class_12972 : arrayList) {
            renderer.render(class_12972, render3DEvent);
        }
    };

    public EntityTargetRenderer(Object object, String string, TargetGroup ... targetGroupArray) {
        super(string, new ItemTargetType[]{ItemTargetType.DROPPED}, targetGroupArray);
        this.owner = object;
        this.enabledSetting = this.createSetting("esp." + string);
    }

    public Object getOwner() {
        return this.owner;
    }

    public BooleanSetting getEnabledSetting() {
        return this.enabledSetting;
    }

    public void setEntityRenderer(EntityRenderer entityRenderer) {
        this.entityRenderer = entityRenderer;
    }

    public void setEntityBatchRenderer(EntityBatchRenderer entityBatchRenderer) {
        this.entityBatchRenderer = entityBatchRenderer;
    }

    public void setTargetFilter(Predicate<Entity> predicate) {
        this.targetFilter = predicate;
    }

    public void enableTargetGroup(TargetGroup targetGroup) {
        super.enableTargetGroup(targetGroup);
    }

    public void enablePlayerGroup(PlayerTargetGroup playerTargetGroup) {
        super.enablePlayerGroup(playerTargetGroup);
    }

    private boolean isTargetEnabled(Entity class_12972) {
        if (class_12972 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            if (!this.isValid(TargetGroup.PLAYERS)) {
                return false;
            }
            if (class_16572 == ClientAccess.minecraftClient.player) {
                return this.isValid2(PlayerTargetGroup.LOCAL_PLAYER);
            }
            if (EntityTargetRenderer.isRockstarUser(class_16572)) {
                return this.isValid2(PlayerTargetGroup.ROCKSTAR_USERS);
            }
            if (RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString())) {
                return this.isValid2(PlayerTargetGroup.FRIENDS);
            }
            return this.isValid2(PlayerTargetGroup.OTHERS);
        }
        if (class_12972 instanceof HostileEntity) {
            return this.isValid2(TargetGroup.MOBS);
        }
        if (class_12972 instanceof AnimalEntity) {
            return this.isValid2(TargetGroup.ANIMALS);
        }
        if (class_12972 instanceof ItemEntity) {
            return this.isValid2(ItemTargetType.DROPPED);
        }
        return false;
    }

    private static boolean isRockstarUser(PlayerEntity player) {
        return RockstarClient.create().getFriendManager().isFriend(player.getName().getString());
    }

    public static interface EntityRenderer {
        public void render(Entity var1, Render3DEvent var2);
    }

    public static interface EntityBatchRenderer {
        public void render(List<Entity> var1, Render3DEvent var2);
    }
}
