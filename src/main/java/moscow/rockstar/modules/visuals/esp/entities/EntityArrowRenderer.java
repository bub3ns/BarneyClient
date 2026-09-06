/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.AnimalEntity
 *  net.minecraft.ItemEntity
 *  net.minecraft.HostileEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.Vec2f
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.modules.visuals.esp.entities;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.visuals.esp.targeting.PlayerTargetGroup;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.TextureRenderContext;
import moscow.rockstar.render.esp.TargetRenderModule;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

public class EntityArrowRenderer
extends TargetRenderModule {
    private static final Identifier ARROW_TEXTURE = RockstarClient.resourceId("textures/arrow.png");
    private static final float ARROW_ICON_SIZE = 35.0f;
    private static PlayerTargetGroup selectedPlayerGroup;
    private static TargetGroup selectedEntityGroup;
    private final BooleanSetting arrowsSetting = this.createSetting("esp.arrows");
    private final BooleanSetting linesSetting = (BooleanSetting)this.createSetting((targetRenderModule, booleanSetting) -> new BooleanSetting((SettingOwner)targetRenderModule, "esp.arrows.lines", () -> !booleanSetting.isEnabled()));
    private final BooleanSetting themeSyncSetting = (BooleanSetting)this.createSetting((targetRenderModule, booleanSetting) -> new BooleanSetting((SettingOwner)targetRenderModule, "theme.sync", () -> !booleanSetting.isEnabled()).enable());
    private final ColorSetting arrowColorSetting = (ColorSetting)this.createSetting("theme.sync", (targetRenderModule, booleanSetting, booleanSetting2) -> new ColorSetting((SettingOwner)targetRenderModule, "esp.arrows.color", () -> !booleanSetting.isEnabled() || booleanSetting2.isEnabled()).setColor(ColorPalette.getAccentColor()));
    private final NumberSetting arrowDistanceSetting = (NumberSetting)this.createSetting("esp.arrows.lines", (targetRenderModule, booleanSetting, booleanSetting2) -> new NumberSetting((SettingOwner)targetRenderModule, "esp.arrows.distance", () -> !booleanSetting.isEnabled() || booleanSetting2.isEnabled()).setStep(0.1f).setMinValue(1.5f).setMaxValue(10.0f).setValue(5.0f));
    private final BooleanSetting hideOnScreenSetting = (BooleanSetting)this.createSetting("esp.arrows.lines", (targetRenderModule, booleanSetting, booleanSetting2) -> new BooleanSetting((SettingOwner)targetRenderModule, "esp.arrows.hide_on_screen", "esp.arrows.hide_on_screen.desc", () -> !booleanSetting.isEnabled() || booleanSetting2.isEnabled()));
    private BooleanSetting hideNakedSetting;
    private final Map<Integer, ArrowRenderState> arrowStates = new HashMap<Integer, ArrowRenderState>();
    private final EventListener<PreHudRenderEvent> hudRenderListener = preHudRenderEvent -> {
        if (minecraftClient.player == null || minecraftClient.world == null || !this.isValid()) {
            return;
        }
        boolean bl = false;
        for (Entity entity : minecraftClient.world.getEntities()) {
            if (!this.shouldRenderArrowForEntity(entity) || this.isLineRenderingEnabled(entity)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            this.updateArrowAnimations(preHudRenderEvent.getTickDelta());
            return;
        }
        for (Entity entity : minecraftClient.world.getEntities()) {
            if (!this.shouldRenderArrowForEntity(entity) || this.isLineRenderingEnabled(entity)) continue;
            boolean hiddenOnScreen = this.isHiddenOnScreen(entity) && this.isEntityOnScreen(entity, preHudRenderEvent.getTickDelta());
            int entityId = entity.getId();
            ArrowRenderState state = this.arrowStates.get(entityId);
            if (state == null) {
                if (hiddenOnScreen) continue;
                state = new ArrowRenderState();
                this.arrowStates.put(entityId, state);
            }
            state.hidden = hiddenOnScreen;
            state.arrowLength = this.getArrowLength(entity, preHudRenderEvent.getTickDelta());
            state.arrowColor = this.getArrowColor(entity);
            state.arrowScale = this.getArrowDistance(entity);
        }
        this.updateArrowAnimations(preHudRenderEvent.getTickDelta());
        CustomDrawContext customDrawContext = preHudRenderEvent.getContext();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.disableCull();
        MatrixStack matrices = customDrawContext.getMatrices();
        matrices.push();
        matrices.translate(minecraftClient.getWindow().getScaledWidth() / 2.0f, minecraftClient.getWindow().getScaledHeight() / 2.0f, 0.0f);
        TextureRenderContext textureRenderContext = new TextureRenderContext(VertexFormats.POSITION_TEXTURE_COLOR, matrices);
        for (Map.Entry<Integer, ArrowRenderState> entry : this.arrowStates.entrySet()) {
            ArrowRenderState arrowRenderState = entry.getValue();
            float f = arrowRenderState.visibilityAnimation.getValue();
            if (f <= 0.0f) continue;
            ItemRenderUtils.translateAndRotate(matrices, 0.0f, 0.0f, arrowRenderState.arrowLength);
            ItemRenderUtils.translateAndScale(matrices, 0.0f, 0.0f, 2.0f - f);
            customDrawContext.drawTexture(ARROW_TEXTURE, -17.5f, -17.5f + arrowRenderState.arrowScale * 10.0f, 35.0f, 35.0f, arrowRenderState.arrowColor.mulAlpha(f));
            ItemRenderUtils.popMatrix(matrices);
            ItemRenderUtils.popMatrix(matrices);
        }
        textureRenderContext.render();
        matrices.pop();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    };
    private final EventListener<Render3DEvent> worldRenderListener = render3DEvent -> {
        if (minecraftClient.player == null || minecraftClient.world == null || !this.isValid()) {
            return;
        }
        boolean bl = false;
        for (Entity entity : minecraftClient.world.getEntities()) {
            if (!this.shouldRenderArrowForEntity(entity) || !this.isLineRenderingEnabled(entity)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        ItemRenderUtils.beginOverlayRendering(false);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder vertexBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (Entity class_12973 : minecraftClient.world.getEntities()) {
            float f;
            if (!this.shouldRenderArrowForEntity(class_12973) || !this.isLineRenderingEnabled(class_12973)) continue;
            Vec3d VanillaChestLootTableGenerator = ProjectionUtils.interpolateEntityPosition(class_12973, render3DEvent.getTickDelta());
            ColorRGBA colorRGBA = this.getArrowColor(class_12973);
            if (class_12973 instanceof LivingEntity) {
                LivingEntity class_13092 = (LivingEntity)class_12973;
                f = class_13092.getHeight() / 2.0f;
            } else {
                f = 0.25f;
            }
            float f2 = f;
            RenderUtils.drawWorldLineToPoint(class_45872, vertexBuffer, VanillaChestLootTableGenerator.add(0.0, (double)f2, 0.0), colorRGBA);
        }
        ItemRenderUtils.flushVertexConsumer(vertexBuffer);
        ItemRenderUtils.endOverlayRendering();
    };

    public EntityArrowRenderer() {
        super("arrows", new TargetGroup[]{TargetGroup.PLAYERS, TargetGroup.MOBS, TargetGroup.ANIMALS, TargetGroup.ITEMS});
        ColorSetting colorSetting;
        BooleanSetting booleanSetting3;
        this.enablePlayerGroup(PlayerTargetGroup.OTHERS, PlayerTargetGroup.FRIENDS, PlayerTargetGroup.ROCKSTAR_USERS);
        BooleanSetting booleanSetting4 = this.getPlayerActivationSetting(PlayerTargetGroup.OTHERS);
        this.hideNakedSetting = new BooleanSetting((SettingOwner)((Object)this), "esp.arrows.hide_naked", () -> !booleanSetting4.isEnabled());
        this.registerScopedSetting("esp.arrows.hide_naked", PlayerTargetGroup.OTHERS, this.hideNakedSetting);
        BooleanSetting booleanSetting5 = this.getSettingForScope("theme.sync", PlayerTargetGroup.FRIENDS);
        if (booleanSetting5 != null) {
            booleanSetting5.setValueInternal(false);
        }
        if ((booleanSetting3 = this.getSettingForScope("theme.sync", PlayerTargetGroup.ROCKSTAR_USERS)) != null) {
            booleanSetting3.setValueInternal(false);
        }
        if ((colorSetting = this.getSettingForScope("esp.arrows.color", PlayerTargetGroup.FRIENDS)) != null) {
            colorSetting.setColor(new ColorRGBA(52.0f, 199.0f, 88.0f));
        }
    }

    public boolean isPlayerGroupRenderable(PlayerTargetGroup playerTargetGroup) {
        return playerTargetGroup != PlayerTargetGroup.LOCAL_PLAYER;
    }

    public boolean shouldRenderArrowForEntity(Entity class_12972) {
        if (class_12972 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            if (class_16572 == minecraftClient.player) {
                return false;
            }
            if (RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString())) {
                return this.isValid2(PlayerTargetGroup.FRIENDS);
            }
            if (!this.isValid2(PlayerTargetGroup.OTHERS)) {
                return false;
            }
            return !this.hideNakedSetting.isEnabled() || !this.isNakedPlayer(class_16572);
        }
        if (class_12972 instanceof HostileEntity) {
            return this.isValid2(TargetGroup.MOBS);
        }
        if (class_12972 instanceof AnimalEntity) {
            return this.isValid2(TargetGroup.ANIMALS);
        }
        if (class_12972 instanceof ItemEntity) {
            return this.isValid2(TargetGroup.ITEMS);
        }
        return false;
    }

    private boolean isNakedPlayer(PlayerEntity class_16572) {
        for (ItemStack class_17992 : class_16572.getAllArmorItems()) {
            if (class_17992 == null || class_17992.isEmpty()) continue;
            return false;
        }
        return true;
    }

    private PlayerTargetGroup getPlayerTargetGroup(PlayerEntity class_16572) {
        if (class_16572 == minecraftClient.player) {
            return PlayerTargetGroup.LOCAL_PLAYER;
        }
        if (RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString())) {
            return PlayerTargetGroup.FRIENDS;
        }
        return PlayerTargetGroup.OTHERS;
    }

    private ColorRGBA getArrowColor(Entity class_12972) {
        ColorSetting colorSetting;
        BooleanSetting booleanSetting;
        ColorRGBA arrowColor;
        if (class_12972 instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)class_12972;
            PlayerTargetGroup playerTargetGroup = selectedPlayerGroup != null ? selectedPlayerGroup : this.getPlayerTargetGroup(player);
        booleanSetting = this.getSettingForScope("theme.sync", playerTargetGroup);
        colorSetting = this.getSettingForScope("esp.arrows.color", playerTargetGroup);
        } else if (class_12972 instanceof HostileEntity) {
            TargetGroup targetGroup = selectedEntityGroup != null ? selectedEntityGroup : TargetGroup.MOBS;
        booleanSetting = this.getSettingForScope("theme.sync", targetGroup);
        colorSetting = this.getSettingForScope("esp.arrows.color", targetGroup);
        } else if (class_12972 instanceof AnimalEntity) {
            TargetGroup targetGroup = selectedEntityGroup != null ? selectedEntityGroup : TargetGroup.ANIMALS;
        booleanSetting = this.getSettingForScope("theme.sync", targetGroup);
        colorSetting = this.getSettingForScope("esp.arrows.color", targetGroup);
        } else if (class_12972 instanceof ItemEntity) {
            TargetGroup targetGroup = selectedEntityGroup != null ? selectedEntityGroup : TargetGroup.ITEMS;
        booleanSetting = this.getSettingForScope("theme.sync", targetGroup);
        colorSetting = this.getSettingForScope("esp.arrows.color", targetGroup);
        } else {
            return ColorPalette.getAccentColor();
        }
        arrowColor = booleanSetting != null && booleanSetting.isEnabled() ? ColorPalette.getAccentColor() : (colorSetting != null ? colorSetting.getColor() : ColorPalette.getAccentColor());
        return arrowColor;
    }

    private float getArrowDistance(Entity class_12972) {
        NumberSetting numberSetting;
        if (class_12972 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            PlayerTargetGroup playerTargetGroup = selectedPlayerGroup != null ? selectedPlayerGroup : this.getPlayerTargetGroup(class_16572);
        numberSetting = this.getSettingForScope("esp.arrows.distance", playerTargetGroup);
        } else if (class_12972 instanceof HostileEntity) {
            TargetGroup targetGroup = selectedEntityGroup != null ? selectedEntityGroup : TargetGroup.MOBS;
        numberSetting = this.getSettingForScope("esp.arrows.distance", targetGroup);
        } else if (class_12972 instanceof AnimalEntity) {
            TargetGroup targetGroup = selectedEntityGroup != null ? selectedEntityGroup : TargetGroup.ANIMALS;
        numberSetting = this.getSettingForScope("esp.arrows.distance", targetGroup);
        } else if (class_12972 instanceof ItemEntity) {
            TargetGroup targetGroup = selectedEntityGroup != null ? selectedEntityGroup : TargetGroup.ITEMS;
        numberSetting = this.getSettingForScope("esp.arrows.distance", targetGroup);
        } else {
            return 3.3f;
        }
        return numberSetting != null ? numberSetting.getValue() : 3.3f;
    }

    private boolean isLineRenderingEnabled(Entity class_12972) {
        BooleanSetting booleanSetting;
        if (class_12972 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            PlayerTargetGroup playerTargetGroup = selectedPlayerGroup != null ? selectedPlayerGroup : this.getPlayerTargetGroup(class_16572);
        booleanSetting = this.getSettingForScope("esp.arrows.lines", playerTargetGroup);
        } else if (class_12972 instanceof HostileEntity) {
            TargetGroup targetGroup = selectedEntityGroup != null ? selectedEntityGroup : TargetGroup.MOBS;
        booleanSetting = this.getSettingForScope("esp.arrows.lines", targetGroup);
        } else if (class_12972 instanceof AnimalEntity) {
            TargetGroup targetGroup = selectedEntityGroup != null ? selectedEntityGroup : TargetGroup.ANIMALS;
        booleanSetting = this.getSettingForScope("esp.arrows.lines", targetGroup);
        } else if (class_12972 instanceof ItemEntity) {
            TargetGroup targetGroup = selectedEntityGroup != null ? selectedEntityGroup : TargetGroup.ITEMS;
        booleanSetting = this.getSettingForScope("esp.arrows.lines", targetGroup);
        } else {
            return false;
        }
        return booleanSetting != null && booleanSetting.isEnabled();
    }

    private boolean isHiddenOnScreen(Entity class_12972) {
        BooleanSetting booleanSetting;
        if (class_12972 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            PlayerTargetGroup playerTargetGroup = selectedPlayerGroup != null ? selectedPlayerGroup : this.getPlayerTargetGroup(class_16572);
        booleanSetting = this.getSettingForScope("esp.arrows.hide_on_screen", playerTargetGroup);
        } else if (class_12972 instanceof HostileEntity) {
            TargetGroup targetGroup = selectedEntityGroup != null ? selectedEntityGroup : TargetGroup.MOBS;
        booleanSetting = this.getSettingForScope("esp.arrows.hide_on_screen", targetGroup);
        } else if (class_12972 instanceof AnimalEntity) {
            TargetGroup targetGroup = selectedEntityGroup != null ? selectedEntityGroup : TargetGroup.ANIMALS;
        booleanSetting = this.getSettingForScope("esp.arrows.hide_on_screen", targetGroup);
        } else if (class_12972 instanceof ItemEntity) {
            TargetGroup targetGroup = selectedEntityGroup != null ? selectedEntityGroup : TargetGroup.ITEMS;
        booleanSetting = this.getSettingForScope("esp.arrows.hide_on_screen", targetGroup);
        } else {
            return false;
        }
        return booleanSetting != null && booleanSetting.isEnabled();
    }

    private boolean isEntityOnScreen(Entity class_12972, float f) {
        Vec3d VanillaChestLootTableGenerator = ProjectionUtils.interpolateEntityPosition(class_12972, f);
        float f2 = class_12972.getWidth() / 2.0f;
        float f3 = class_12972.getHeight();
        float f4 = Float.MAX_VALUE;
        float f5 = Float.MAX_VALUE;
        float f6 = -3.4028235E38f;
        float f7 = -3.4028235E38f;
        boolean bl = false;
        for (int i = 0; i < 8; ++i) {
            Vec2f VanillaAdventureTabAdvancementGenerator = ProjectionUtils.projectToScreen(VanillaChestLootTableGenerator.add((i & 1) == 0 ? (double)(-f2) : (double)f2, (i & 2) == 0 ? 0.0 : (double)f3, (i & 4) == 0 ? (double)(-f2) : (double)f2));
            if (VanillaAdventureTabAdvancementGenerator == null) continue;
            bl = true;
            f4 = Math.min(f4, VanillaAdventureTabAdvancementGenerator.x);
            f6 = Math.max(f6, VanillaAdventureTabAdvancementGenerator.x);
            f5 = Math.min(f5, VanillaAdventureTabAdvancementGenerator.y);
            f7 = Math.max(f7, VanillaAdventureTabAdvancementGenerator.y);
        }
        if (!bl) {
            return false;
        }
        return f6 >= 0.0f && f4 <= minecraftClient.getWindow().getScaledWidth() && f7 >= 0.0f && f5 <= minecraftClient.getWindow().getScaledHeight();
    }

    private void updateArrowAnimations(float f) {
        Iterator<Map.Entry<Integer, ArrowRenderState>> iterator = this.arrowStates.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ArrowRenderState> entry = iterator.next();
            int n = entry.getKey();
            ArrowRenderState arrowRenderState = entry.getValue();
            Entity class_12972 = minecraftClient.world.getEntityById(n);
            boolean bl = class_12972 != null && this.shouldRenderArrowForEntity(class_12972) && !this.isLineRenderingEnabled(class_12972) && !arrowRenderState.hidden;
            arrowRenderState.visibilityAnimation.setDuration(500L);
            arrowRenderState.visibilityAnimation.setReverse(bl);
            if (bl || arrowRenderState.visibilityAnimation.getValue() != 0.0f) continue;
            iterator.remove();
        }
    }

    private float getArrowLength(Entity class_12972, float f) {
        Vec3d VanillaChestLootTableGenerator = ProjectionUtils.interpolateEntityPosition(class_12972, f);
        Vec3d WallPlayerSkullBlock = minecraftClient.gameRenderer.getCamera().getPos();
        double d = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d2 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        float f2 = minecraftClient.gameRenderer.getCamera().getYaw();
        double d3 = Math.toDegrees(Math.atan2(d2, d));
        return (float)(d3 - (double)(f2 - 90.0f));
    }

    @Generated
    public BooleanSetting getArrowsSetting() {
        return this.arrowsSetting;
    }

    @Generated
    public BooleanSetting getLinesSetting() {
        return this.linesSetting;
    }

    @Generated
    public BooleanSetting getThemeSyncSetting() {
        return this.themeSyncSetting;
    }

    @Generated
    public ColorSetting getArrowColorSetting() {
        return this.arrowColorSetting;
    }

    @Generated
    public NumberSetting getArrowDistanceSetting() {
        return this.arrowDistanceSetting;
    }

    @Generated
    public BooleanSetting getHideOnScreenSetting() {
        return this.hideOnScreenSetting;
    }

    @Generated
    public BooleanSetting getHideNakedSetting() {
        return this.hideNakedSetting;
    }

    @Generated
    public Map<Integer, ArrowRenderState> getSettingsByKey() {
        return this.arrowStates;
    }

    @Generated
    public EventListener<PreHudRenderEvent> getHudRenderListener() {
        return this.hudRenderListener;
    }

    @Generated
    public EventListener<Render3DEvent> getWorldRenderListener() {
        return this.worldRenderListener;
    }

    @Generated
    public static void setSelectedPlayerGroup(PlayerTargetGroup playerTargetGroup) {
        selectedPlayerGroup = playerTargetGroup;
    }

    @Generated
    public static PlayerTargetGroup getSelectedPlayerGroup() {
        return selectedPlayerGroup;
    }

    @Generated
    public static void setSelectedEntityGroup(TargetGroup targetGroup) {
        selectedEntityGroup = targetGroup;
    }

    @Generated
    public static TargetGroup getSelectedEntityGroup() {
        return selectedEntityGroup;
    }

    static class ArrowRenderState {
        Animation visibilityAnimation = new Animation(300L, Easing.easeOutBack);
        boolean hidden;
        float arrowLength;
        ColorRGBA arrowColor = ColorPalette.getAccentColor();
        float arrowScale = 3.3f;

        ArrowRenderState() {
        }
    }
}
