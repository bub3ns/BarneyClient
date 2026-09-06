/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.AnimalEntity
 *  net.minecraft.ItemEntity
 *  net.minecraft.HostileEntity
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.AbstractClientPlayerEntity
 *  org.joml.Quaternionf
 */
package moscow.rockstar.modules.visuals.esp.entities;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.visuals.esp.targeting.PlayerTargetGroup;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.esp.TargetRenderModule;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.social.FriendListManager;
import moscow.rockstar.ui.screens.EspSettingsScreen;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.joml.Quaternionf;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public class EntityBoxRenderer
extends TargetRenderModule {
    private static final Identifier BLOOM_TEXTURE = RockstarClient.resourceId("textures/bloom.png");
    private final BooleanSetting enabledSetting = this.createSetting("esp.boxes");
    private final BooleanSetting themeSyncSetting = (BooleanSetting)this.createSetting((targetRenderModule, booleanSetting) -> new BooleanSetting((SettingOwner)targetRenderModule, "theme.sync", () -> !booleanSetting.isEnabled()).enable());
    private final ColorSetting boxColorSetting = (ColorSetting)this.createSetting("theme.sync", (targetRenderModule, booleanSetting, booleanSetting2) -> new ColorSetting((SettingOwner)targetRenderModule, "esp.boxes.color", () -> !booleanSetting.isEnabled() || booleanSetting2.isEnabled()).setColor(ColorPalette.getAccentColor()));
    private final MultiBooleanSetting boxModeSetting = (MultiBooleanSetting)this.createSetting((targetRenderModule, booleanSetting) -> {
        MultiBooleanSetting multiBooleanSetting = new MultiBooleanSetting((SettingOwner)targetRenderModule, "esp.boxes.mode", () -> !booleanSetting.isEnabled());
        new MultiBooleanSetting.Option(multiBooleanSetting, "esp.boxes.mode.fill").select();
        new MultiBooleanSetting.Option(multiBooleanSetting, "esp.boxes.mode.outline").select();
        return multiBooleanSetting;
    });
    private final EventListener<Render3DEvent> render3DEventListener = render3DEvent -> {
        if (!this.isValid()) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Camera class_41842 = minecraftClient.gameRenderer.getCamera();
        float f = render3DEvent.getTickDelta();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.depthMask((boolean)false);
        RenderSystem.setShaderTexture((int)0, (Identifier)BLOOM_TEXTURE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (Entity entity : minecraftClient.world.getEntities()) {
            if (!this.shouldRenderEntityBox(entity)) continue;
            Vec3d entityPosition = ProjectionUtils.interpolateEntityPosition(entity, f);
            float f2 = (entity.getWidth() + entity.getHeight()) * 1.2f;
            ColorRGBA colorRGBA2 = this.resolveEntityBoxColor(entity);
            if (this.hasEntityBoxMode(entity, "esp.boxes.mode.fill")) {
                class_45872.push();
                ItemRenderUtils.translateToWorldPosition(class_45872, entityPosition);
                class_45872.translate(0.0f, entity.getHeight() / 4.0f, 0.0f);
                class_45872.multiply(class_41842.getRotation());
                ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f2 / 2.0f, -f2 / 2.0f, 0.0, f2, f2, colorRGBA2.mulAlpha(0.5f));
                class_45872.pop();
                continue;
            }
            class_45872.push();
            ItemRenderUtils.translateToWorldPosition(class_45872, entityPosition);
            class_45872.translate(0.0, (double)entity.getHeight() / 3.0, 0.0);
            class_45872.multiply(class_41842.getRotation());
            ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f2 / 2.0f, -f2 / 2.0f, 0.0, f2, f2, colorRGBA2.mulAlpha(0.5f));
            class_45872.pop();
            class_45872.push();
            ItemRenderUtils.translateToWorldPosition(class_45872, entityPosition);
            class_45872.translate(0.0, (double)entity.getHeight() / 1.5, 0.0);
            class_45872.multiply(class_41842.getRotation());
            ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f2 / 2.0f, -f2 / 2.0f, 0.0, f2, f2, colorRGBA2.mulAlpha(0.5f));
            class_45872.pop();
        }
        ItemRenderUtils.flushVertexConsumer(class_2872);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder filledBoxBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (Entity entity : minecraftClient.world.getEntities()) {
            if (!this.shouldRenderEntityBox(entity)) continue;
            MultiBooleanSetting multiBooleanSetting = this.getEntityBoxMode(entity);
            float f3 = entity.getWidth() / 2.0f;
            Box entityBox = new Box((double)(-f3), 0.0, (double)(-f3), (double)f3, (double)entity.getHeight(), (double)f3);
            class_45872.push();
            ItemRenderUtils.translateToWorldPosition(class_45872, ProjectionUtils.interpolateEntityPosition(entity, f));
            boolean bl = multiBooleanSetting != null && this.hasSelectedBoxMode(multiBooleanSetting, "esp.boxes.mode.fill");
            ColorRGBA colorRGBA = this.resolveEntityBoxColor(entity).mulAlpha(bl && !this.hasSelectedBoxMode(multiBooleanSetting, "esp.boxes.mode.outline") ? 0.35f : 0.1f);
            if (bl) {
                RenderUtils.drawGradientBox(class_45872, filledBoxBuffer, entityBox, colorRGBA, colorRGBA.mulAlpha(0.0f));
            } else {
                RenderUtils.drawFilledBox(class_45872, filledBoxBuffer, entityBox, colorRGBA);
            }
            class_45872.pop();
        }
        ItemRenderUtils.flushVertexConsumer(filledBoxBuffer);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder outlineBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (Entity class_12972 : minecraftClient.world.getEntities()) {
            if (!this.shouldRenderEntityBox(class_12972)) continue;
            MultiBooleanSetting multiBooleanSetting = this.getEntityBoxMode(class_12972);
            float f4 = class_12972.getWidth() / 2.0f;
            Box InfestedBlock = new Box((double)(-f4), 0.0, (double)(-f4), (double)f4, (double)class_12972.getHeight(), (double)f4);
            class_45872.push();
            ItemRenderUtils.translateToWorldPosition(class_45872, ProjectionUtils.interpolateEntityPosition(class_12972, f));
            ColorRGBA colorRGBA = this.resolveEntityBoxColor(class_12972);
            if (multiBooleanSetting != null && this.hasSelectedBoxMode(multiBooleanSetting, "esp.boxes.mode.outline")) {
                RenderUtils.drawGradientBoxOutline(class_45872, outlineBuffer, InfestedBlock, colorRGBA, colorRGBA.mulAlpha(0.0f));
            } else {
                RenderUtils.drawBoxOutline(class_45872, outlineBuffer, InfestedBlock, colorRGBA);
            }
            class_45872.pop();
        }
        ItemRenderUtils.flushVertexConsumer(outlineBuffer);
        RenderSystem.depthMask((boolean)true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    };

    public EntityBoxRenderer() {
        super("boxes", new TargetGroup[]{TargetGroup.PLAYERS, TargetGroup.MOBS, TargetGroup.ANIMALS, TargetGroup.ITEMS});
    }

    @Override
    public void renderPreviewOverlay(RockstarDrawContext drawContext, Entity class_12972, float f, float f2, TargetGroup targetGroup, PlayerTargetGroup playerTargetGroup) {
        float f3 = class_12972.getHeight();
        float f4 = class_12972.getWidth() / 2.0f;
        MatrixStack class_45872 = drawContext.getMatrices();
        ColorRGBA colorRGBA = this.resolveBoxColor(targetGroup, playerTargetGroup);
        float f5 = EspSettingsScreen.getPreviewScale();
        float f6 = EspSettingsScreen.getPreviewOffset();
        float f7 = class_12972 instanceof LivingEntity ? EspSettingsScreen.getPreviewPitch() : 0.0f;
        float f8 = 45.0f;
        Quaternionf quaternionf = new Quaternionf().rotateZ((float)Math.PI).rotateX((float)Math.toRadians(f7));
        float f9 = (f4 + f3) * 1.4f;
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.depthMask((boolean)false);
        class_45872.push();
        class_45872.translate(f, f2 + 36.0f, 50.0f);
        class_45872.scale(f5, f5, -f5);
        class_45872.multiply(quaternionf);
        class_45872.translate(0.0f, -f6, 0.0f);
        RenderSystem.setShaderTexture((int)0, (Identifier)BLOOM_TEXTURE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        if (this.hasEntityBoxMode(class_12972, "esp.boxes.mode.fill")) {
            ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f9 / 2.0f, -f3 / 5.0f - f9 / 2.0f, 0.0, f9, f9, colorRGBA.mulAlpha(0.5f));
        } else {
            ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f9 / 2.0f, f3 / 6.0f - f9 / 2.0f, 0.0, f9, f9, colorRGBA.mulAlpha(0.5f));
            ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f9 / 2.0f, -f3 / 6.0f - f9 / 2.0f, 0.0, f9, f9, colorRGBA.mulAlpha(0.5f));
        }
        ItemRenderUtils.flushVertexConsumer(class_2872);
        RenderSystem.setShaderTexture((int)0, (int)0);
        class_45872.pop();
        class_45872.push();
        class_45872.translate(f, f2 + 36.0f, 50.0f);
        class_45872.scale(f5, f5, -f5);
        class_45872.multiply(quaternionf);
        class_45872.translate(0.0f, -f3 / 2.0f - f6, 0.0f);
        class_45872.translate(0.0f, f3 / 2.0f, 0.0f);
        class_45872.multiply(new Quaternionf().rotateY((float)Math.toRadians(f8)));
        class_45872.translate(0.0f, -f3 / 2.0f, 0.0f);
        Box HorizontalFacingBlock = new Box((double)(-f4), 0.0, (double)(-f4), (double)f4, (double)f3, (double)f4);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder CreativeInventoryActionC2SPacket = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        MultiBooleanSetting multiBooleanSetting = this.getEntityBoxMode(class_12972);
        boolean bl = multiBooleanSetting != null && this.hasSelectedBoxMode(multiBooleanSetting, "esp.boxes.mode.fill");
        ColorRGBA colorRGBA2 = this.resolveEntityBoxColor(class_12972).mulAlpha(bl && !this.hasSelectedBoxMode(multiBooleanSetting, "esp.boxes.mode.outline") ? 0.35f : 0.1f);
        if (bl) {
            RenderUtils.drawGradientBox(class_45872, CreativeInventoryActionC2SPacket, HorizontalFacingBlock, colorRGBA2, colorRGBA2.mulAlpha(0.0f));
        } else {
            RenderUtils.drawFilledBox(class_45872, CreativeInventoryActionC2SPacket, HorizontalFacingBlock, colorRGBA2);
        }
        ItemRenderUtils.flushVertexConsumer(CreativeInventoryActionC2SPacket);
        BufferBuilder UpdateStructureBlockC2SPacket = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        ColorRGBA colorRGBA3 = this.resolveEntityBoxColor(class_12972);
        if (multiBooleanSetting != null && this.hasSelectedBoxMode(multiBooleanSetting, "esp.boxes.mode.outline")) {
            RenderUtils.drawGradientBoxOutline(class_45872, UpdateStructureBlockC2SPacket, HorizontalFacingBlock, colorRGBA3, colorRGBA3.mulAlpha(0.0f));
        } else {
            RenderUtils.drawBoxOutline(class_45872, UpdateStructureBlockC2SPacket, HorizontalFacingBlock, colorRGBA3);
        }
        ItemRenderUtils.flushVertexConsumer(UpdateStructureBlockC2SPacket);
        class_45872.pop();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private ColorRGBA resolveBoxColor(TargetGroup targetGroup, PlayerTargetGroup playerTargetGroup) {
        ColorSetting colorSetting;
        BooleanSetting booleanSetting;
        if (targetGroup == TargetGroup.PLAYERS) {
            booleanSetting = this.getSettingForScope("theme.sync", playerTargetGroup);
            colorSetting = this.getSettingForScope("esp.boxes.color", playerTargetGroup);
        } else {
            booleanSetting = this.getSettingForScope("theme.sync", targetGroup);
            colorSetting = this.getSettingForScope("esp.boxes.color", targetGroup);
        }
        return booleanSetting != null && booleanSetting.isEnabled() ? ColorPalette.getAccentColor() : (colorSetting != null ? colorSetting.getColor() : ColorPalette.getAccentColor());
    }

    private ColorRGBA resolveEntityBoxColor(Entity class_12972) {
        ColorSetting colorSetting;
        BooleanSetting booleanSetting;
        if (class_12972 instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity TrackedPosition = (AbstractClientPlayerEntity)class_12972;
            PlayerTargetGroup playerTargetGroup = this.resolvePlayerGroup(TrackedPosition);
            booleanSetting = this.getSettingForScope("theme.sync", playerTargetGroup);
            colorSetting = this.getSettingForScope("esp.boxes.color", playerTargetGroup);
        } else if (class_12972 instanceof ItemEntity) {
            booleanSetting = this.getSettingForScope("theme.sync", TargetGroup.ITEMS);
            colorSetting = this.getSettingForScope("esp.boxes.color", TargetGroup.ITEMS);
        } else if (class_12972 instanceof HostileEntity) {
            booleanSetting = this.getSettingForScope("theme.sync", TargetGroup.MOBS);
            colorSetting = this.getSettingForScope("esp.boxes.color", TargetGroup.MOBS);
        } else if (class_12972 instanceof AnimalEntity) {
            booleanSetting = this.getSettingForScope("theme.sync", TargetGroup.ANIMALS);
            colorSetting = this.getSettingForScope("esp.boxes.color", TargetGroup.ANIMALS);
        } else {
            return ColorPalette.getAccentColor();
        }
        return booleanSetting != null && booleanSetting.isEnabled() ? ColorPalette.getAccentColor() : (colorSetting != null ? colorSetting.getColor() : ColorPalette.getAccentColor());
    }

    private MultiBooleanSetting getEntityBoxMode(Entity class_12972) {
        if (class_12972 instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity TrackedPosition = (AbstractClientPlayerEntity)class_12972;
            return this.getSettingForScope("esp.boxes.mode", this.resolvePlayerGroup(TrackedPosition));
        }
        if (class_12972 instanceof ItemEntity) {
            return this.getSettingForScope("esp.boxes.mode", TargetGroup.ITEMS);
        }
        if (class_12972 instanceof HostileEntity) {
            return this.getSettingForScope("esp.boxes.mode", TargetGroup.MOBS);
        }
        if (class_12972 instanceof AnimalEntity) {
            return this.getSettingForScope("esp.boxes.mode", TargetGroup.ANIMALS);
        }
        return null;
    }

    private boolean hasEntityBoxMode(Entity class_12972, String string) {
        MultiBooleanSetting multiBooleanSetting = this.getEntityBoxMode(class_12972);
        return multiBooleanSetting != null && this.hasSelectedBoxMode(multiBooleanSetting, string);
    }

    private boolean hasSelectedBoxMode(MultiBooleanSetting multiBooleanSetting, String string) {
        return multiBooleanSetting.getSelectedOptions().stream().anyMatch(option -> option.getName().equals(string));
    }

    private boolean shouldRenderEntityBox(Entity class_12972) {
        if (class_12972 instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity TrackedPosition = (AbstractClientPlayerEntity)class_12972;
            if (TrackedPosition == minecraftClient.player && minecraftClient.options.getPerspective().isFirstPerson()) {
                return false;
            }
            return this.isValid2(this.resolvePlayerGroup(TrackedPosition));
        }
        if (class_12972 instanceof ItemEntity) {
            return this.isValid2(TargetGroup.ITEMS);
        }
        if (class_12972 instanceof HostileEntity) {
            return this.isValid2(TargetGroup.MOBS);
        }
        if (class_12972 instanceof AnimalEntity) {
            return this.isValid2(TargetGroup.ANIMALS);
        }
        return false;
    }

    private PlayerTargetGroup resolvePlayerGroup(AbstractClientPlayerEntity TrackedPosition) {
        if (TrackedPosition == minecraftClient.player) {
            return PlayerTargetGroup.LOCAL_PLAYER;
        }
        FriendListManager friendListManager = RockstarClient.create().getFriendListManager();
        if (friendListManager.containsFriend(TrackedPosition.getName().getString())) {
            return PlayerTargetGroup.FRIENDS;
        }
        return PlayerTargetGroup.OTHERS;
    }
}
