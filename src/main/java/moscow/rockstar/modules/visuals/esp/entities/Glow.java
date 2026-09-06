/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  moscow.rockstar.entity.interpolation.EntityInterpolator
 *  moscow.rockstar.modules.visuals.esp.entities.Glow$Inner
 *  moscow.rockstar.render.esp.TargetRenderModule
 *  moscow.rockstar.render.targets.FramebufferManager
 *  moscow.rockstar.render.targets.FramebufferTarget
 *  moscow.rockstar.render.targets.RenderTargetState
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.AnimalEntity
 *  net.minecraft.ItemEntity
 *  net.minecraft.HostileEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockState
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.VertexConsumerProvider$Immediate
 *  net.minecraft.AbstractClientPlayerEntity
 *  net.minecraft.HeldItemRenderer
 *  net.minecraft.BlockRenderManager
 *  net.minecraft.ModelTransformationMode
 *  net.minecraft.EntityRenderDispatcher
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 */
package moscow.rockstar.modules.visuals.esp.entities;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.visuals.esp.entities.Glow;
import moscow.rockstar.modules.visuals.esp.targeting.ItemTargetType;
import moscow.rockstar.modules.visuals.esp.targeting.PlayerTargetGroup;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.esp.TargetRenderModule;
import moscow.rockstar.render.item.HeldItemRenderCapture;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.target.RenderTarget;
import moscow.rockstar.render.target.RenderTargetState;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorRangeSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public class Glow
extends TargetRenderModule
implements ClientAccess {
    public static boolean entityGlowRendering;
    public static boolean itemGlowRendering;
    public static Entity currentEntity;
    private static PlayerTargetGroup selectedPlayerGroup;
    private static ItemTargetType selectedItemGroup;
    private static TargetGroup selectedEntityType;
    private static final int FULL_BRIGHT_LIGHT = 0xF000F0;
    private static final int GLOW_MAP_SIZE = 10;
    private static final ColorRGBA DEFAULT_GLOW_COLOR;
    private final RenderTarget entityGlowTarget = new RenderTarget(true).enableLinearFiltering().setResolutionScale(1.0f);
    private final RenderTarget heldItemGlowTarget = new RenderTarget(true).enableLinearFiltering().setResolutionScale(1.0f);
    private final RenderTarget itemEntityGlowTarget = new RenderTarget(true).enableLinearFiltering().setResolutionScale(1.0f);
    private final RenderTargetState heldItemGlowState = RenderTargetState.create();
    private final RenderTargetState itemEntityGlowState = RenderTargetState.create();
    private final Timer renderCooldown = new Timer();
    private boolean framePrepared = false;
    private boolean entityGlowCaptured = false;
    private boolean heldItemCaptured = false;
    private boolean itemEntityCaptured = false;
    private int viewportX = -1;
    private int viewportY = -1;
    private int viewportWidth = -1;
    private int viewportHeight = -1;
    private float minScreenX;
    private float minScreenY;
    private float maxScreenX;
    private float maxScreenY;
    private boolean entityOutsideViewport;
    private final BooleanSetting glowEnabled = this.createSetting("esp.glow");
    private final NumberSetting entityGlowStrength = new NumberSetting((SettingOwner)((Object)this), "esp.glow.strength").setMinValue(1.0f).setMaxValue(5.0f).setStep(1.0f).setValue(3.0f);
    private final NumberSetting itemGlowStrength = new NumberSetting((SettingOwner)((Object)this), "esp.glow.strength_items").setMinValue(1.0f).setMaxValue(5.0f).setStep(1.0f).setValue(3.0f);
    private final BooleanSetting entityColorEnabled = this.createSetting((targetRenderModule, moduleEnabled, targetGroup) -> {
        String settingKey = targetGroup == TargetGroup.ITEMS ? "esp.glow.item_color" : "esp.glow.entity_color";
        return new BooleanSetting(targetRenderModule, settingKey, () -> !moduleEnabled.isEnabled()).enable();
    });
    private final BooleanSetting themeSync = this.createSetting("esp.glow.entity_color",
        (targetRenderModule, moduleEnabled, entityColorEnabled) -> new BooleanSetting(targetRenderModule,
            "theme.sync", () -> !moduleEnabled.isEnabled() || entityColorEnabled.isEnabled()).enable());
    private final BooleanSetting gradientEnabled = this.createSetting("esp.glow.entity_color", "theme.sync",
        (targetRenderModule, moduleEnabled, entityColorEnabled, themeSync) -> new BooleanSetting(targetRenderModule,
            "esp.glow.gradient", () -> !moduleEnabled.isEnabled() || entityColorEnabled.isEnabled() || themeSync.isEnabled()));
    private final ColorSetting entityColor = this.createSetting("esp.glow.entity_color", "theme.sync", "esp.glow.gradient",
        (targetRenderModule, moduleEnabled, entityColorEnabled, themeSync, gradientEnabled) -> new ColorSetting(targetRenderModule,
            "esp.glow.color", () -> !moduleEnabled.isEnabled() || entityColorEnabled.isEnabled() || themeSync.isEnabled() || gradientEnabled.isEnabled())
            .setColor(ColorPalette.getAccentColor()));
    private final List<ColorRangeSetting> gradientColorSettings = new ArrayList<ColorRangeSetting>();
    private boolean synchronizingGradientColors;
    private final ColorRangeSetting gradientColors = this.createSetting("esp.glow.entity_color", "theme.sync", "esp.glow.gradient",
        (targetRenderModule, moduleEnabled, entityColorEnabled, themeSync, gradientEnabled) -> {
        ColorRangeSetting setting = new SynchronizedColorRangeSetting(targetRenderModule, "esp.glow.gradient_color",
            () -> !moduleEnabled.isEnabled() || entityColorEnabled.isEnabled() || themeSync.isEnabled() || !gradientEnabled.isEnabled(),
            this::synchronizeGradientColors)
            .setColorRange(new ColorRGBA(255.0f, 80.0f, 200.0f, 255.0f), new ColorRGBA(80.0f, 160.0f, 255.0f, 255.0f));
        this.gradientColorSettings.add(setting);
        return setting;
    });
    private final EventListener<Render3DEvent> render3DEventListener = render3DEvent -> {
        this.heldItemCaptured = false;
        if (this.isValid2(ItemTargetType.HELD)) {
            this.heldItemGlowTarget.beginPass(true);
            this.heldItemGlowTarget.endPass();
        }
        if (!this.isValid() || !this.renderCooldown.hasElapsed(10L)) {
            return;
        }
        boolean bl = this.isValid2(ItemTargetType.DROPPED);
        this.entityGlowTarget.beginPass(true);
        entityGlowRendering = true;
        try {
            this.entityGlowCaptured = false;
            this.minScreenY = Float.POSITIVE_INFINITY;
            this.minScreenX = Float.POSITIVE_INFINITY;
            this.maxScreenY = Float.NEGATIVE_INFINITY;
            this.maxScreenX = Float.NEGATIVE_INFINITY;
            this.entityOutsideViewport = false;
            for (Entity class_12972 : minecraftClient.world.getEntities()) {
                if (class_12972 instanceof ItemEntity || !this.isGlowTarget(class_12972)) continue;
                this.renderEntityGlow(class_12972, (Render3DEvent)render3DEvent);
            }
        }
        finally {
            entityGlowRendering = false;
            this.entityGlowTarget.endPass();
        }
        if (this.entityGlowCaptured && this.isGradientColorReady()) {
            this.drawGlowTexture(this.entityGlowTarget, DEFAULT_GLOW_COLOR);
        }
        this.itemEntityCaptured = false;
        if (bl) {
            this.itemEntityGlowTarget.beginPass(true);
            itemGlowRendering = true;
            try {
                for (Entity class_12972 : minecraftClient.world.getEntities()) {
                    if (!(class_12972 instanceof ItemEntity) || !this.isGlowTarget(class_12972)) continue;
                    this.renderItemEntityGlow(class_12972, (Render3DEvent)render3DEvent);
                }
            }
            finally {
                itemGlowRendering = false;
                this.itemEntityGlowTarget.endPass();
            }
        }
        if (this.entityGlowCaptured) {
            this.updateGlowViewport();
        } else {
            this.viewportHeight = -1;
            this.viewportWidth = -1;
            this.viewportY = -1;
            this.viewportX = -1;
        }
        this.framePrepared = true;
        this.renderCooldown.reset();
    };
    private final EventListener<PreHudRenderEvent> preHudRenderListener = preHudRenderEvent -> {
        boolean bl;
        boolean bl2 = this.isValid();
        if (bl2) {
            if (this.framePrepared) {
                if (this.entityGlowCaptured) {
                    RenderTargetState state = ShaderRenderer.entityGlowState;
                    state.setSamplesPerPass((int)this.entityGlowStrength.getValue());
                    state.setCompositeOffset(7.0f);
                    state.setOutlineStrength(1.0f);
                    state.setOutlineRadius(1.0f);
                    state.apply(this.entityGlowTarget);
                }
                this.framePrepared = false;
            }
            if (this.entityGlowCaptured) {
                int textureId = ShaderRenderer.entityGlowState.getTextureId();
                if (textureId != 0) {
                    this.renderGlowTextureWithColors(textureId, -0.5f, this.isGradientColorReady());
                }
            }
        }
        if (this.isValid2(ItemTargetType.HELD) && this.heldItemCaptured) {
            bl = this.isGradientEnabledForItemType(ItemTargetType.HELD);
            if (bl) {
                this.drawGlowTexture(this.heldItemGlowTarget, DEFAULT_GLOW_COLOR);
            } else {
                ColorRGBA colorRGBA = this.getHeldItemGlowColor();
                if (colorRGBA != null) {
                    this.drawGlowTexture(this.heldItemGlowTarget, colorRGBA);
                }
            }
            this.heldItemGlowState.setSamplesPerPass((int)this.itemGlowStrength.getValue());
            this.heldItemGlowState.setCompositeOffset(7.0f);
            this.heldItemGlowState.setOutlineStrength(1.0f);
            this.heldItemGlowState.setOutlineRadius(1.0f);
            this.heldItemGlowState.setGlowOffsetY(-8.0f);
            this.heldItemGlowState.apply(this.heldItemGlowTarget);
            HeldItemRenderCapture.renderOverlay(this.heldItemGlowState.getOutputTarget());
            int n = this.heldItemGlowState.getTextureId();
            if (n != 0) {
                this.renderGlowTextureWithColors(n, -0.5f, bl);
            }
        }
        if (this.isValid2(ItemTargetType.DROPPED) && this.itemEntityCaptured) {
            bl = this.isGradientEnabledForItemType(ItemTargetType.DROPPED);
            if (bl) {
                this.drawGlowTexture(this.itemEntityGlowTarget, DEFAULT_GLOW_COLOR);
            } else {
                ColorRGBA colorRGBA = this.getItemEntityGlowColor();
                if (colorRGBA != null) {
                    this.drawGlowTexture(this.itemEntityGlowTarget, colorRGBA);
                }
            }
            this.itemEntityGlowState.setSamplesPerPass((int)this.itemGlowStrength.getValue());
            this.itemEntityGlowState.setCompositeOffset(7.0f);
            this.itemEntityGlowState.setOutlineStrength(1.0f);
            this.itemEntityGlowState.setOutlineRadius(1.0f);
            this.itemEntityGlowState.apply(this.itemEntityGlowTarget);
            int n = this.itemEntityGlowState.getTextureId();
            if (n != 0) {
                this.renderGlowTextureWithColors(n, -0.5f, bl);
            }
        }
    };
    private static final int COLOR_CHANNEL_COUNT = 4;
    private static final float GLOW_QUAD_HEIGHT = 80.0f;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void synchronizeGradientColors(ColorRangeSetting colorRangeSetting) {
        if (this.synchronizingGradientColors) {
            return;
        }
        this.synchronizingGradientColors = true;
        try {
            ColorRGBA colorRGBA = colorRangeSetting.getColorRangeSettingColorRGBA();
            ColorRGBA colorRGBA2 = colorRangeSetting.getSecondColor();
            for (ColorRangeSetting colorRangeSetting2 : this.gradientColorSettings) {
                if (colorRangeSetting2 == colorRangeSetting) continue;
                if (colorRGBA != null) {
                    colorRangeSetting2.setFirstColorAndReturn(colorRGBA);
                }
                if (colorRGBA2 == null) continue;
                colorRangeSetting2.setSecondColorAndReturn(colorRGBA2);
            }
        }
        finally {
            this.synchronizingGradientColors = false;
        }
    }

    private boolean isGradientColorReady() {
        for (TargetGroup targetGroup : new TargetGroup[]{TargetGroup.MOBS, TargetGroup.ANIMALS}) {
            if (!this.isGradientEnabledForEntityType(targetGroup)) continue;
            return true;
        }
        for (Enum enum_ : PlayerTargetGroup.values()) {
            if (!this.isGradientEnabledForPlayerGroup((PlayerTargetGroup)enum_)) continue;
            return true;
        }
        return false;
    }

    private boolean isGradientEnabledForEntityType(TargetGroup targetGroup) {
        if (!this.isValid2(targetGroup)) {
            return false;
        }
        BooleanSetting booleanSetting = this.getSettingForScope("esp.glow.entity_color", targetGroup);
        if (booleanSetting != null && booleanSetting.isEnabled()) {
            return false;
        }
        BooleanSetting booleanSetting2 = this.getSettingForScope("theme.sync", targetGroup);
        if (booleanSetting2 != null && booleanSetting2.isEnabled()) {
            return false;
        }
        BooleanSetting booleanSetting3 = this.getSettingForScope("esp.glow.gradient", targetGroup);
        return booleanSetting3 != null && booleanSetting3.isEnabled();
    }

    private boolean isGradientEnabledForPlayerGroup(PlayerTargetGroup playerTargetGroup) {
        if (!this.isValid2(playerTargetGroup)) {
            return false;
        }
        BooleanSetting booleanSetting = this.getSettingForScope("esp.glow.entity_color", playerTargetGroup);
        if (booleanSetting != null && booleanSetting.isEnabled()) {
            return false;
        }
        BooleanSetting booleanSetting2 = this.getSettingForScope("theme.sync", playerTargetGroup);
        if (booleanSetting2 != null && booleanSetting2.isEnabled()) {
            return false;
        }
        BooleanSetting booleanSetting3 = this.getSettingForScope("esp.glow.gradient", playerTargetGroup);
        return booleanSetting3 != null && booleanSetting3.isEnabled();
    }

    private boolean isGradientEnabledForItemType(ItemTargetType itemTargetType) {
        if (!this.isValid2(itemTargetType)) {
            return false;
        }
        BooleanSetting booleanSetting = this.getSettingForScope("esp.glow.entity_color", itemTargetType);
        if (booleanSetting != null && booleanSetting.isEnabled()) {
            return false;
        }
        BooleanSetting booleanSetting2 = this.getSettingForScope("theme.sync", itemTargetType);
        if (booleanSetting2 != null && booleanSetting2.isEnabled()) {
            return false;
        }
        BooleanSetting booleanSetting3 = this.getSettingForScope("esp.glow.gradient", itemTargetType);
        return booleanSetting3 != null && booleanSetting3.isEnabled();
    }

    public Glow() {
        super("glow", new ItemTargetType[]{ItemTargetType.HELD, ItemTargetType.DROPPED},
            new TargetGroup[]{TargetGroup.PLAYERS, TargetGroup.MOBS, TargetGroup.ANIMALS, TargetGroup.ITEMS});
        RenderTargetState.initialize();
        this.attachSettingToTargetGroups(this.entityGlowStrength, TargetGroup.PLAYERS, TargetGroup.MOBS, TargetGroup.ANIMALS);
        this.attachSettingToTargetGroups(this.itemGlowStrength, TargetGroup.ITEMS);
    }

    public boolean isGlowTarget(Entity class_12972) {
        if (class_12972 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            if (selectedPlayerGroup != null) {
                return this.isValid2(selectedPlayerGroup);
            }
            if (class_16572 == minecraftClient.player) {
                return this.isValid2(PlayerTargetGroup.LOCAL_PLAYER);
            }
            if (Glow.isRockstarUser(class_16572)) {
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
            if (selectedItemGroup != null) {
                return this.isValid2(selectedItemGroup);
            }
            return this.isValid2(ItemTargetType.DROPPED);
        }
        return false;
    }

    public ColorRGBA getGlowColor(Entity class_12972) {
        ColorSetting colorSetting;
        BooleanSetting booleanSetting;
        BooleanSetting booleanSetting2;
        if (class_12972 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            PlayerTargetGroup playerTargetGroup = selectedPlayerGroup != null ? selectedPlayerGroup : Glow.getPlayerGroup(class_16572);
            booleanSetting2 = this.getSettingForScope("esp.glow.entity_color", playerTargetGroup);
            booleanSetting = this.getSettingForScope("theme.sync", playerTargetGroup);
            colorSetting = this.getSettingForScope("esp.glow.color", playerTargetGroup);
        } else if (class_12972 instanceof HostileEntity) {
            TargetGroup targetGroup = selectedEntityType != null ? selectedEntityType : TargetGroup.MOBS;
            booleanSetting2 = this.getSettingForScope("esp.glow.entity_color", targetGroup);
            booleanSetting = this.getSettingForScope("theme.sync", targetGroup);
            colorSetting = this.getSettingForScope("esp.glow.color", targetGroup);
        } else if (class_12972 instanceof AnimalEntity) {
            TargetGroup targetGroup = selectedEntityType != null ? selectedEntityType : TargetGroup.ANIMALS;
            booleanSetting2 = this.getSettingForScope("esp.glow.entity_color", targetGroup);
            booleanSetting = this.getSettingForScope("theme.sync", targetGroup);
            colorSetting = this.getSettingForScope("esp.glow.color", targetGroup);
        } else {
            return null;
        }
        if (booleanSetting2 == null || booleanSetting2.isEnabled()) {
            return null;
        }
        if (booleanSetting != null && booleanSetting.isEnabled()) {
            return ColorPalette.getAccentColor();
        }
        return colorSetting != null ? colorSetting.getColor() : ColorPalette.getAccentColor();
    }

    public ColorRGBA getHeldItemGlowColor() {
        return this.getGlowColorForItemType(ItemTargetType.HELD);
    }

    public ColorRGBA getItemEntityGlowColor() {
        return this.getGlowColorForItemType(ItemTargetType.DROPPED);
    }

    private ColorRGBA getGlowColorForItemType(ItemTargetType itemTargetType) {
        BooleanSetting booleanSetting = this.getSettingForScope("esp.glow.entity_color", itemTargetType);
        BooleanSetting booleanSetting2 = this.getSettingForScope("theme.sync", itemTargetType);
        ColorSetting colorSetting = this.getSettingForScope("esp.glow.color", itemTargetType);
        if (booleanSetting == null || booleanSetting.isEnabled()) {
            return null;
        }
        if (booleanSetting2 != null && booleanSetting2.isEnabled()) {
            return ColorPalette.getAccentColor();
        }
        return colorSetting != null ? colorSetting.getColor() : ColorPalette.getAccentColor();
    }

    public void renderHeldItemGlow(HeldItemRenderer Icon, AbstractClientPlayerEntity TrackedPosition, ItemStack class_17992, ModelTransformationMode DeathMessageType, boolean bl, MatrixStack class_45872, int n) {
        if (!this.isValid2(ItemTargetType.HELD)) {
            return;
        }
        if (class_17992 == null || class_17992.isEmpty()) {
            return;
        }
        itemGlowRendering = true;
        try {
            this.heldItemGlowTarget.beginPass(false);
            try {
                VertexConsumerProvider.Immediate class_45982 = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
                Icon.renderItem((LivingEntity)TrackedPosition, class_17992, DeathMessageType, bl, class_45872, (VertexConsumerProvider)class_45982, 0xF000F0);
                class_45982.draw();
                this.heldItemCaptured = true;
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.error("[ESP/Glow] held-item capture failed", exception);
            }
            finally {
                this.heldItemGlowTarget.endPass();
            }
        }
        finally {
            itemGlowRendering = false;
        }
    }

    public void renderBlockGlow(BlockRenderManager class_7762, BlockState class_26802, MatrixStack class_45872, int n) {
        if (!this.isValid2(ItemTargetType.HELD)) {
            return;
        }
        if (class_26802 == null) {
            return;
        }
        itemGlowRendering = true;
        try {
            this.heldItemGlowTarget.beginPass(false);
            try {
                VertexConsumerProvider.Immediate class_45982 = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
                class_7762.renderBlockAsEntity(class_26802, class_45872, (VertexConsumerProvider)class_45982, 0xF000F0, n);
                class_45982.draw();
                this.heldItemCaptured = true;
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.error("[ESP/Glow] held-block capture failed", exception);
            }
            finally {
                this.heldItemGlowTarget.endPass();
            }
        }
        finally {
            itemGlowRendering = false;
        }
    }

    private void renderItemEntityGlow(Entity class_12972, Render3DEvent render3DEvent) {
        if (class_12972 == null || !class_12972.isAlive()) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Camera class_41842 = minecraftClient.gameRenderer.getCamera();
        Vec3d VanillaChestLootTableGenerator = ProjectionUtils.interpolateEntityPosition(class_12972, render3DEvent.getTickDelta());
        Vec3d WallPlayerSkullBlock = class_41842.getPos();
        class_45872.push();
        class_45872.translate(VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x, VanillaChestLootTableGenerator.y - WallPlayerSkullBlock.y, VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        EntityRenderDispatcher entityRenderDispatcher = minecraftClient.getEntityRenderDispatcher();
        VertexConsumerProvider.Immediate class_45982 = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
        currentEntity = class_12972;
        try {
            entityRenderDispatcher.render(class_12972, 0.0, 0.0, 0.0,
                render3DEvent.getTickDelta(), class_45872, class_45982, 0xF000F0);
            class_45982.draw();
            this.itemEntityCaptured = true;
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("[ESP/Glow] dropped-item capture failed", exception);
        }
        currentEntity = null;
        RenderSystem.enableDepthTest();
        class_45872.pop();
    }

    private void renderEntityGlow(Entity class_12972, Render3DEvent render3DEvent) {
        if (class_12972 == null || !class_12972.isAlive()) {
            return;
        }
        if (class_12972 == minecraftClient.player && minecraftClient.options.getPerspective().isFirstPerson()) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Camera class_41842 = minecraftClient.gameRenderer.getCamera();
        Vec3d VanillaChestLootTableGenerator = ProjectionUtils.interpolateEntityPosition(class_12972, render3DEvent.getTickDelta());
        Vec3d WallPlayerSkullBlock = class_41842.getPos();
        class_45872.push();
        class_45872.translate(VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x, VanillaChestLootTableGenerator.y - WallPlayerSkullBlock.y, VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        EntityRenderDispatcher entityRenderDispatcher = minecraftClient.getEntityRenderDispatcher();
        VertexConsumerProvider.Immediate class_45982 = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
        currentEntity = class_12972;
        try {
            entityRenderDispatcher.render(class_12972, 0.0, 0.0, 0.0,
                render3DEvent.getTickDelta(), class_45872, class_45982, 0xF000F0);
            class_45982.draw();
            this.updateScreenBounds(class_12972, WallPlayerSkullBlock);
            this.entityGlowCaptured = true;
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("[ESP/Glow] entity capture failed", exception);
        }
        currentEntity = null;
        RenderSystem.enableDepthTest();
        class_45872.pop();
    }

    private void renderGlowTextureWithColors(int n, float f, boolean bl) {
        ColorRGBA colorRGBA = this.gradientColors.getColorRangeSettingColorRGBA();
        ColorRGBA colorRGBA2 = this.gradientColors.getSecondColor();
        if (bl && colorRGBA != null && colorRGBA2 != null) {
            this.renderGradientGlow(n, f, Glow.toOpaqueArgb(colorRGBA), Glow.toOpaqueArgb(colorRGBA2));
        } else {
            this.renderGlowTexture(n, f);
        }
    }

    private void renderGlowTexture(int n, float f) {
        RenderSystem.setShaderTexture((int)0, (int)n);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
        ShaderRenderer.renderQuadWithBounds(0.0f, f, minecraftClient.getWindow().getScaledWidth(), minecraftClient.getWindow().getScaledHeight());
        ShaderRenderer.renderQuadWithBounds(0.0f, f, minecraftClient.getWindow().getScaledWidth(), minecraftClient.getWindow().getScaledHeight());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
    }

    private void drawGlowTexture(RenderTarget framebufferTarget, ColorRGBA colorRGBA) {
        framebufferTarget.beginWrite(true);
        RenderSystem.enableBlend();
        GlStateManager._blendFuncSeparate((int)772, (int)0, (int)0, (int)1);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        int n = Glow.toPremultipliedArgb(colorRGBA);
        float f = minecraftClient.getWindow().getScaledWidth();
        float f2 = minecraftClient.getWindow().getScaledHeight();
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(0.0f, 0.0f, 0.0f).color(n);
        class_2872.vertex(0.0f, f2, 0.0f).color(n);
        class_2872.vertex(f, f2, 0.0f).color(n);
        class_2872.vertex(f, 0.0f, 0.0f).color(n);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        RenderSystem.defaultBlendFunc();
        minecraftClient.getFramebuffer().beginWrite(true);
    }

    private void renderGradientGlow(int n, float f, int n2, int n3) {
        RenderSystem.setShaderTexture((int)0, (int)n);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
        float f2 = minecraftClient.getWindow().getScaledWidth();
        float f3 = minecraftClient.getWindow().getScaledHeight();
        float f4 = f3 / 4.0f;
        float f5 = 2.0f * f4;
        float f6 = (float)(System.currentTimeMillis() % 1000000L) / 1000.0f;
        float f7 = f6 * 80.0f % f5;
        int n4 = 2;
        for (int i = 0; i < 2; ++i) {
            BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (int j = -n4; j < 4 + n4; ++j) {
                boolean bl = Math.floorMod(j, 2) == 0;
                int n5 = bl ? n2 : n3;
                int n6 = bl ? n3 : n2;
                float f8 = f + (float)j * f4 - f7;
                float f9 = f8 + f4;
                float f10 = 1.0f - (f8 - f) / f3;
                float f11 = 1.0f - (f9 - f) / f3;
                class_2872.vertex(0.0f, f8, 0.0f).texture(0.0f, f10).color(n5);
                class_2872.vertex(0.0f, f9, 0.0f).texture(0.0f, f11).color(n6);
                class_2872.vertex(f2, f9, 0.0f).texture(1.0f, f11).color(n6);
                class_2872.vertex(f2, f8, 0.0f).texture(1.0f, f10).color(n5);
            }
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
    }

    private static int toOpaqueArgb(ColorRGBA colorRGBA) {
        int n = Math.round(colorRGBA.getRed()) & 0xFF;
        int n2 = Math.round(colorRGBA.getGreen()) & 0xFF;
        int n3 = Math.round(colorRGBA.getBlue()) & 0xFF;
        return 0xFF000000 | n << 16 | n2 << 8 | n3;
    }

    private static int toPremultipliedArgb(ColorRGBA colorRGBA) {
        float f = Math.max(0.0f, Math.min(1.0f, colorRGBA.getAlpha() / 255.0f));
        int n = Math.round(colorRGBA.getRed() * f) & 0xFF;
        int n2 = Math.round(colorRGBA.getGreen() * f) & 0xFF;
        int n3 = Math.round(colorRGBA.getBlue() * f) & 0xFF;
        return 0xFF000000 | n << 16 | n2 << 8 | n3;
    }

    private void updateScreenBounds(Entity class_12972, Vec3d VanillaChestLootTableGenerator) {
        if (this.entityOutsideViewport) {
            return;
        }
        Box HorizontalFacingBlock = class_12972.getBoundingBox();
        Matrix4f matrix4f = new Matrix4f((Matrix4fc)RenderSystem.getProjectionMatrix()).mul((Matrix4fc)RenderSystem.getModelViewMatrix());
        double[] dArray = new double[]{HorizontalFacingBlock.minX, HorizontalFacingBlock.maxX};
        double[] dArray2 = new double[]{HorizontalFacingBlock.minY, HorizontalFacingBlock.maxY};
        double[] dArray3 = new double[]{HorizontalFacingBlock.minZ, HorizontalFacingBlock.maxZ};
        Vector4f vector4f = new Vector4f();
        for (double d : dArray) {
            for (double d2 : dArray2) {
                for (double d3 : dArray3) {
                    vector4f.set((float)(d - VanillaChestLootTableGenerator.x), (float)(d2 - VanillaChestLootTableGenerator.y), (float)(d3 - VanillaChestLootTableGenerator.z), 1.0f);
                    vector4f.mul((Matrix4fc)matrix4f);
                    if (vector4f.w <= 1.0E-4f) {
                        this.entityOutsideViewport = true;
                        return;
                    }
                    float f = vector4f.x / vector4f.w;
                    float f2 = vector4f.y / vector4f.w;
                    if (f < this.minScreenX) {
                        this.minScreenX = f;
                    }
                    if (f > this.maxScreenX) {
                        this.maxScreenX = f;
                    }
                    if (f2 < this.minScreenY) {
                        this.minScreenY = f2;
                    }
                    if (!(f2 > this.maxScreenY)) continue;
                    this.maxScreenY = f2;
                }
            }
        }
    }

    private void updateGlowViewport() {
        int n = minecraftClient.getWindow().getFramebufferWidth();
        int n2 = minecraftClient.getWindow().getFramebufferHeight();
        if (this.entityOutsideViewport || this.minScreenX > this.maxScreenX) {
            this.viewportX = 0;
            this.viewportY = 0;
            this.viewportWidth = n;
            this.viewportHeight = n2;
            return;
        }
        int n3 = 96;
        float f = (this.minScreenX * 0.5f + 0.5f) * (float)n;
        float f2 = (this.maxScreenX * 0.5f + 0.5f) * (float)n;
        float f3 = (this.minScreenY * 0.5f + 0.5f) * (float)n2;
        float f4 = (this.maxScreenY * 0.5f + 0.5f) * (float)n2;
        int n4 = (int)Math.floor(f) - n3;
        int n5 = (int)Math.floor(f3) - n3;
        int n6 = (int)Math.ceil(f2 - f) + n3 * 2;
        int n7 = (int)Math.ceil(f4 - f3) + n3 * 2;
        if (n4 < 0) {
            n6 += n4;
            n4 = 0;
        }
        if (n5 < 0) {
            n7 += n5;
            n5 = 0;
        }
        if (n4 >= n || n5 >= n2) {
            this.viewportHeight = -1;
            this.viewportWidth = -1;
            this.viewportY = -1;
            this.viewportX = -1;
            return;
        }
        if (n4 + n6 > n) {
            n6 = n - n4;
        }
        if (n5 + n7 > n2) {
            n7 = n2 - n5;
        }
        this.viewportX = n4;
        this.viewportY = n5;
        this.viewportWidth = n6;
        this.viewportHeight = n7;
    }

    private static PlayerTargetGroup getPlayerGroup(PlayerEntity class_16572) {
        if (class_16572 == minecraftClient.player) {
            return PlayerTargetGroup.LOCAL_PLAYER;
        }
        if (RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString())) {
            return PlayerTargetGroup.FRIENDS;
        }
        if (Glow.isRockstarUser(class_16572)) {
            return PlayerTargetGroup.ROCKSTAR_USERS;
        }
        return PlayerTargetGroup.OTHERS;
    }

    private static boolean isRockstarUser(PlayerEntity player) {
        return RockstarClient.create().getFriendManager().isFriend(player.getName().getString());
    }

    /** Mirrors the original Glow inner setting, which propagates edits to all scopes. */
    private static final class SynchronizedColorRangeSetting extends ColorRangeSetting {
        private final Consumer<ColorRangeSetting> synchronization;

        private SynchronizedColorRangeSetting(SettingOwner owner, String key,
                                               java.util.function.BooleanSupplier visibility,
                                               Consumer<ColorRangeSetting> synchronization) {
            super(owner, key, visibility);
            this.synchronization = synchronization;
        }

        @Override
        public ColorRangeSetting setFirstColorAndReturn(ColorRGBA color) {
            super.setFirstColorAndReturn(color);
            if (this.synchronization != null) {
                this.synchronization.accept(this);
            }
            return this;
        }

        @Override
        public ColorRangeSetting setSecondColorAndReturn(ColorRGBA color) {
            super.setSecondColorAndReturn(color);
            if (this.synchronization != null) {
                this.synchronization.accept(this);
            }
            return this;
        }
    }

    @Generated
    public RenderTarget getEntityGlowTarget() {
        return this.entityGlowTarget;
    }

    @Generated
    public RenderTarget getHeldItemGlowTarget() {
        return this.heldItemGlowTarget;
    }

    @Generated
    public RenderTarget getItemEntityGlowTarget() {
        return this.itemEntityGlowTarget;
    }

    @Generated
    public RenderTargetState getHeldItemGlowState() {
        return this.heldItemGlowState;
    }

    @Generated
    public RenderTargetState getItemEntityGlowState() {
        return this.itemEntityGlowState;
    }

    @Generated
    public Timer getRenderCooldown() {
        return this.renderCooldown;
    }

    @Generated
    public boolean isFramePrepared() {
        return this.framePrepared;
    }

    @Generated
    public boolean isEntityGlowCaptured() {
        return this.entityGlowCaptured;
    }

    @Generated
    public boolean isHeldItemCaptured() {
        return this.heldItemCaptured;
    }

    @Generated
    public boolean isItemEntityCaptured() {
        return this.itemEntityCaptured;
    }

    @Generated
    public int getViewportX() {
        return this.viewportX;
    }

    @Generated
    public int getViewportY() {
        return this.viewportY;
    }

    @Generated
    public int getViewportWidth() {
        return this.viewportWidth;
    }

    @Generated
    public int getViewportHeight() {
        return this.viewportHeight;
    }

    @Generated
    public float getMinScreenX() {
        return this.minScreenX;
    }

    @Generated
    public float getMinScreenY() {
        return this.minScreenY;
    }

    @Generated
    public float getMaxScreenX() {
        return this.maxScreenX;
    }

    @Generated
    public float getMaxScreenY() {
        return this.maxScreenY;
    }

    @Generated
    public boolean isEntityOutsideViewport() {
        return this.entityOutsideViewport;
    }

    @Generated
    public BooleanSetting getGlowEnabledSetting() {
        return this.glowEnabled;
    }

    @Generated
    public NumberSetting getEntityGlowStrength() {
        return this.entityGlowStrength;
    }

    @Generated
    public NumberSetting getItemGlowStrength() {
        return this.itemGlowStrength;
    }

    @Generated
    public BooleanSetting getEntityColorSetting() {
        return this.entityColorEnabled;
    }

    @Generated
    public BooleanSetting getThemeSyncSetting() {
        return this.themeSync;
    }

    @Generated
    public BooleanSetting getGradientSetting() {
        return this.gradientEnabled;
    }

    @Generated
    public ColorSetting getEntityColorSettingValue() {
        return this.entityColor;
    }

    @Generated
    public List<ColorRangeSetting> getGradientColorSettings() {
        return this.gradientColorSettings;
    }

    @Generated
    public boolean isSynchronizingGradientColors() {
        return this.synchronizingGradientColors;
    }

    @Generated
    public ColorRangeSetting getGradientColors() {
        return this.gradientColors;
    }

    @Generated
    public EventListener<Render3DEvent> getRender3DEventListener() {
        return this.render3DEventListener;
    }

    @Generated
    public EventListener<PreHudRenderEvent> getPreHudRenderListener() {
        return this.preHudRenderListener;
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
    public static void setSelectedItemGroup(ItemTargetType itemTargetType) {
        selectedItemGroup = itemTargetType;
    }

    @Generated
    public static ItemTargetType getSelectedItemGroup() {
        return selectedItemGroup;
    }

    @Generated
    public static void setSelectedEntityType(TargetGroup targetGroup) {
        selectedEntityType = targetGroup;
    }

    @Generated
    public static TargetGroup getSelectedEntityType() {
        return selectedEntityType;
    }

    static {
        DEFAULT_GLOW_COLOR = new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f);
    }
}
