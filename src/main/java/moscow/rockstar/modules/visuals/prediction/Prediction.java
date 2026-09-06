/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.AbstractTexture
 *  net.minecraft.StatusEffect
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.Entity
 *  net.minecraft.ExperienceOrbEntity
 *  net.minecraft.LivingEntity
 *  net.minecraft.ItemEntity
 *  net.minecraft.PersistentProjectileEntity
 *  net.minecraft.ArrowEntity
 *  net.minecraft.ProjectileUtil
 *  net.minecraft.ProjectileEntity
 *  net.minecraft.SnowballEntity
 *  net.minecraft.EnderPearlEntity
 *  net.minecraft.TridentEntity
 *  net.minecraft.PotionEntity
 *  net.minecraft.BowItem
 *  net.minecraft.CrossbowItem
 *  net.minecraft.EnderPearlItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.SnowballItem
 *  net.minecraft.TridentItem
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.ItemConvertible
 *  net.minecraft.World
 *  net.minecraft.Position
 *  net.minecraft.Box
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec2f
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferBuilder
 *  net.minecraft.PlayerInteractItemC2SPacket
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.MathHelper
 *  net.minecraft.ThrownItemEntity
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  net.minecraft.EntityHitResult
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.RegistryKey
 *  net.minecraft.AbstractClientPlayerEntity
 *  net.minecraft.RotationAxis
 *  org.lwjgl.opengl.GL30
 */
package moscow.rockstar.modules.visuals.prediction;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.Generated;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.items.recipes.RecipeItemResolver;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.visuals.object.CollisionResult;
import moscow.rockstar.modules.visuals.prediction.PredictionEntry;
import moscow.rockstar.modules.visuals.prediction.TrajectoryResult;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.render.assets.RemoteAssetCache;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.TridentItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemConvertible;
import net.minecraft.world.World;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.opengl.GL30;
import pyrock.events.network.SendPacketEvent;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Prediction", category=ModuleCategory.VISUALS, disableLocked=true)
public class Prediction
extends Module {
    private final List<TrajectoryResult> predictionSettings = new ArrayList<TrajectoryResult>();
    private final List<CollisionResult> predictionItems = new ArrayList<CollisionResult>();
    private final List<PredictionEntry> predictionEntries = new ArrayList<PredictionEntry>();
    private MultiBooleanSetting entities;
    private MultiBooleanSetting.Option pearlEntities;
    private MultiBooleanSetting.Option tridentEntities;
    private MultiBooleanSetting.Option snowballEntities;
    private MultiBooleanSetting.Option arrowEntities;
    private MultiBooleanSetting.Option potionEntities;
    private MultiBooleanSetting.Option itemEntities;
    private ModeSetting renderMode;
    private ModeSetting.Option defaultOption;
    private ModeSetting.Option glowOption;
    private BooleanSetting hand;
    private BooleanSetting walls;
    private BooleanSetting hud;
    private BooleanSetting helper;
    private BooleanSetting assist;
    private BooleanSetting sync;
    private ColorSetting color;
    private Rotation currentRotation;
    private Vec3d position;
    private static final int MAX_MIPMAP_LEVEL = 4;
    private int boundTextureId = -1;
    private final EventListener<SendPacketEvent> onSendPacketEventListener = sendPacketEvent -> {
        net.minecraft.network.packet.Packet<?> packet = sendPacketEvent.getPacket();
        if (!(packet instanceof PlayerInteractItemC2SPacket)) {
            return;
        }
        PlayerInteractItemC2SPacket class_28862 = (PlayerInteractItemC2SPacket)packet;
        if (Prediction.minecraftClient.player == null || this.currentRotation == null) {
            return;
        }
        ItemStack class_17992 = Prediction.minecraftClient.player.getMainHandStack();
        if (!this.isPredictableItem(class_17992.getItem())) {
            return;
        }
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        if (rotationManager == null || rotationManager.isIdle()) {
            return;
        }
        Rotation rotation = rotationManager.getEffectiveRotation();
        float f = Math.abs(AimRotationMath.getWrappedAngleDifference(rotation.getYaw(), this.currentRotation.getYaw()));
        float f2 = Math.abs(rotation.getPitch() - this.currentRotation.getPitch());
        float f3 = 35.0f;
        if (f > f3 || f2 > f3) {
            return;
        }
        if (!this.helper.isEnabled() || this.assist.isEnabled()) {
            return;
        }
        rotationManager.requestRotation(this.currentRotation, RotationCorrectionMode.UNSPECIFIED, 180.0f, 180.0f, 180.0f, RotationPriority.ITEM_USE_PRIORITY);
    };
    private final EventListener<PreHudRenderEvent> onPreHudRenderEventListener = preHudRenderEvent -> {
        Vec3d cameraPosition;
        float f;
        String string;
        String string2;
        String string3;
        int n;
        int n2;
        float f2;
        float f3;
        float f4;
        Vec2f screenPosition;
        CustomDrawContext customDrawContext = preHudRenderEvent.getContext();
        MatrixStack class_45872 = customDrawContext.getMatrices();
        for (TrajectoryResult object3 : this.predictionSettings) {
            Entity trajectoryEntity = object3.getEntity();
            if (object3.getTicks() <= 0 && object3.getCollidedEntity() == null || (screenPosition = ProjectionUtils.projectToScreen(object3.getVectors().getLast())) == null) continue;
            float f5 = screenPosition.x;
            float f6 = screenPosition.y;
            FontMetrics fontMetrics = Font.MEDIUM.metrics(13.0f);
            f4 = fontMetrics.getFontTopOffset() + 6.0f;
            float f7 = -f4;
            String string4 = this.formatEntityName(object3.getEntity());
            string4 = this.formatPredictionLabel(string4.replace("] ", "").replace("[", ""), object3.getTicks());
            ItemStack displayStack = Items.ARROW.getDefaultStack();
            if (trajectoryEntity instanceof ThrownItemEntity thrownItem) {
                displayStack = thrownItem.getStack();
            } else if (trajectoryEntity instanceof PersistentProjectileEntity projectile) {
                displayStack = projectile.getItemStack();
            } else if (trajectoryEntity instanceof ItemEntity itemEntity) {
                displayStack = itemEntity.getStack();
            }
            float f8 = (float)object3.getVectors().getLast().distanceTo(Prediction.minecraftClient.player.getEyePos());
            f3 = MathHelper.clamp((float)(1.0f - f8 / 20.0f), (float)0.5f, (float)1.0f);
            class_45872.push();
            class_45872.translate(f5, f6, 0.0f);
            class_45872.scale(f3, f3, 1.0f);
            f2 = fontMetrics.measureText(string4) + 20.0f;
            customDrawContext.drawRect(-f2 / 2.0f, f7, f2, f4, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
            customDrawContext.drawItem(displayStack, -f2 / 2.0f, f7, 1.0f);
            customDrawContext.drawText(fontMetrics, string4, -f2 / 2.0f + 17.0f, f7 + 3.0f, ColorPalette.WHITE);
            f7 += f4;
            if (trajectoryEntity instanceof ProjectileEntity projectile && projectile.getOwner() instanceof AbstractClientPlayerEntity owner) {
                String ownerLabel = Localization.translate("modules.prediction.from") + " " + (projectile.getOwner() == Prediction.minecraftClient.player ? Localization.translate("modules.prediction.you") : projectile.getOwner().getName().getString());
                float f9 = fontMetrics.measureText(ownerLabel) + 22.0f;
                customDrawContext.drawRect(-f9 / 2.0f, f7, f9, f4, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
                customDrawContext.drawHead(owner, -f9 / 2.0f, f7, f4, WidgetState.NONE, ColorPalette.WHITE);
                customDrawContext.drawText(fontMetrics, ownerLabel, -f9 / 2.0f + 19.0f, f7 + 3.0f, ColorPalette.WHITE);
                f7 += f4;
            }
            if (trajectoryEntity instanceof PotionEntity potionEntity) {
                for (StatusEffectInstance effect : RecipeItemResolver.getEffects(potionEntity.getStack())) {
                    String string5 = effect.getEffectType().value().getName().getString();
                    n2 = effect.getAmplifier();
                    n = effect.getDuration();
                    string3 = n2 > 0 ? " " + (n2 + 1) : "";
                    string2 = this.formatPredictionTicks(n);
                    string = string5 + string3 + " (" + string2 + ")";
                    f = fontMetrics.measureText(string) + 6.0f;
                    customDrawContext.drawRect(-f / 2.0f, f7 + 5.0f, f, f4, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
                    customDrawContext.drawText(fontMetrics, string, -f / 2.0f + 3.0f, f7 + 8.0f, ColorRGBA.fromInt(effect.getEffectType().value().getColor()).withAlpha(255.0f));
                    f7 += f4;
                }
            }
            class_45872.pop();
        }
        if (this.helper.isEnabled() && this.position != null && this.isPredictableItem(Prediction.minecraftClient.player.getMainHandStack().getItem())) {
            cameraPosition = Prediction.minecraftClient.player.getCameraPosVec(preHudRenderEvent.getTickDelta());
            Rotation f18 = this.calculateThrowRotation(cameraPosition, this.position, Prediction.minecraftClient.player.getMainHandStack().getItem());
            screenPosition = f18 == null ? null : ProjectionUtils.projectRayToScreen(Prediction.minecraftClient.player.getRotationVector(f18.getPitch(), f18.getYaw()));
            if (screenPosition != null) {
                RotationManager rotationManager = RockstarClient.create().getRotationManager();
                Rotation rotation = rotationManager != null ? rotationManager.getCurrentRotation() : new Rotation(Prediction.minecraftClient.player.getYaw(), Prediction.minecraftClient.player.getPitch());
                float f10 = Math.abs(AimRotationMath.getWrappedAngleDifference(rotation.getYaw(), f18.getYaw()));
                f4 = Math.abs(rotation.getPitch() - f18.getPitch());
                ColorRGBA colorRGBA = (f10 < 10.0f && f4 < 10.0f ? ColorPalette.GREEN : ColorPalette.WHITE).withAlpha(200.0f);
                class_45872.push();
                class_45872.translate(screenPosition.x, screenPosition.y, 0.0f);
                float f11 = (float)(System.currentTimeMillis() % 2000L) / 2000.0f * 360.0f;
                class_45872.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f11));
                float f12 = 14.0f;
                customDrawContext.drawRect(-f12 / 2.0f, -0.75f, f12, 1.5f, colorRGBA);
                customDrawContext.drawRect(-0.75f, -f12 / 2.0f, 1.5f, f12, colorRGBA);
                class_45872.pop();
            }
        }
        for (PredictionEntry predictionEntry : this.predictionEntries) {
            Vec3d entryPosition = predictionEntry.getPosition();
            Vec2f entryScreenPosition = ProjectionUtils.projectToScreen(entryPosition);
            if (entryScreenPosition == null) continue;
            float f13 = entryScreenPosition.x;
            float f14 = entryScreenPosition.y;
            FontMetrics fontMetrics = Font.MEDIUM.metrics(13.0f);
            float f15 = fontMetrics.getFontTopOffset() + 6.0f;
            float f16 = -f15;
            String string6 = predictionEntry.getDisplayLabel();
            ItemStack class_17993 = predictionEntry.getStack();
            f3 = (float)entryPosition.distanceTo(Prediction.minecraftClient.player.getEyePos());
            f2 = MathHelper.clamp((float)(1.0f - f3 / 20.0f), (float)0.5f, (float)1.0f);
            class_45872.push();
            class_45872.translate(f13, f14, 0.0f);
            class_45872.scale(f2, f2, 1.0f);
            float f17 = fontMetrics.measureText(string6) + 20.0f;
            customDrawContext.drawRect(-f17 / 2.0f, f16, f17, f15, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
            customDrawContext.drawItem(class_17993, -f17 / 2.0f, f16, 1.0f);
            customDrawContext.drawText(fontMetrics, string6, -f17 / 2.0f + 17.0f, f16 + 3.0f, ColorPalette.WHITE);
            f16 += f15;
            for (StatusEffectInstance effect : RecipeItemResolver.getEffects(class_17993)) {
                String string7 = effect.getEffectType().value().getName().getString();
                n2 = effect.getAmplifier();
                n = effect.getDuration();
                string3 = n2 > 0 ? " " + (n2 + 1) : "";
                string2 = this.formatPredictionTicks(n);
                string = string7 + string3 + " (" + string2 + ")";
                f = fontMetrics.measureText(string) + 6.0f;
                customDrawContext.drawRect(-f / 2.0f, f16 + 5.0f, f, f15, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
                customDrawContext.drawText(fontMetrics, string, -f / 2.0f + 3.0f, f16 + 8.0f, ColorRGBA.fromInt(effect.getEffectType().value().getColor()).withAlpha(255.0f));
                f16 += f15;
            }
            class_45872.pop();
        }
        if (this.hud.isEnabled()) {
            FontMetrics hudMetrics = Font.MEDIUM.metrics(10.0f);
            float f18 = 0.0f;
            for (TrajectoryResult trajectoryResult : this.predictionSettings) {
                if (trajectoryResult.getCollidedEntity() != Prediction.minecraftClient.player || trajectoryResult.getEntity() instanceof EnderPearlEntity) continue;
                String string8 = this.formatPredictionLabel(this.formatEntityName(trajectoryResult.getEntity()), trajectoryResult.getTicks());
                String string9 = Localization.translateFormatted("modules.prediction.warning", string8);
                customDrawContext.drawCenteredText(hudMetrics, string9, INSTANCE.width() / 2.0f, INSTANCE.height() / 2.0f + 20.0f + f18, ColorPalette.WHITE);
                f18 += hudMetrics.getFontTopOffset() + 3.0f;
            }
        }
    };
    private final EventListener<Render3DEvent> onRender3DEventListener = render3DEvent -> {
        BufferBuilder vertexBuffer;
        Vec3d interpolatedPosition;
        ColorRGBA colorRGBA = this.sync.isEnabled() ? ColorPalette.getAccentColor() : this.color.getColor();
        this.updateRotationInput(render3DEvent.getTickDelta());
        MatrixStack class_45872 = render3DEvent.getMatrices();
        class_45872.push();
        ItemRenderUtils.beginOverlayRendering(true);
        ItemRenderUtils.translateToCamera(class_45872);
        RenderSystem.enableDepthTest();
        if (this.walls.isEnabled()) {
            RenderSystem.disableDepthTest();
        }
        if (this.defaultOption.isSelected()) {
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            vertexBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for (TrajectoryResult object32 : this.predictionSettings) {
                Vec3d WallPlayerSkullBlock = object32.getVectors().getFirst();
                RenderUtils.drawLineSegment(class_45872, vertexBuffer, ProjectionUtils.interpolateEntityPosition(object32.getEntity(), render3DEvent.getTickDelta()), WallPlayerSkullBlock, colorRGBA);
                Vec3d previousPoint = WallPlayerSkullBlock;
                for (Vec3d currentPoint : object32.getVectors()) {
                    RenderUtils.drawLineSegment(class_45872, vertexBuffer, previousPoint, currentPoint, colorRGBA);
                    previousPoint = currentPoint;
                }
            }
            ItemRenderUtils.flushVertexConsumer(vertexBuffer);
        } else {
            Identifier bloomTexture = RockstarClient.resourceId("textures/bloom.png");
            RenderSystem.setShaderTexture((int)0, bloomTexture);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            vertexBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (TrajectoryResult trajectoryResult : this.predictionSettings) {
                Vec3d PlayerSkullBlock = trajectoryResult.getVectors().getFirst();
                interpolatedPosition = ProjectionUtils.interpolateEntityPosition(trajectoryResult.getEntity(), render3DEvent.getTickDelta());
                if (interpolatedPosition.distanceTo(Prediction.minecraftClient.player.getEyePos()) > 2.0) {
                    for (int i = 0; i < 10; ++i) {
                        float f = (float)i / 10.0f;
                        Vec3d RedstoneBlock = interpolatedPosition.add(PlayerSkullBlock.subtract(interpolatedPosition).multiply((double)f));
                        this.drawPredictionMarker(class_45872, RedstoneBlock, vertexBuffer, (float)PlayerSkullBlock.distanceTo(interpolatedPosition) / 3.0f, 1.0f);
                        this.drawPredictionMarker(class_45872, RedstoneBlock, vertexBuffer, (float)PlayerSkullBlock.distanceTo(interpolatedPosition) * 2.0f, 0.05f);
                    }
                }
                Vec3d previousPoint = PlayerSkullBlock;
                for (Vec3d VanillaFishingLootTableGenerator : trajectoryResult.getVectors()) {
                    if (VanillaFishingLootTableGenerator.distanceTo(Prediction.minecraftClient.player.getEyePos()) > 2.0) {
                        for (int i = 0; i < 10; ++i) {
                            float f = (float)i / 10.0f;
                            Vec3d LootTableProvider = previousPoint.add(VanillaFishingLootTableGenerator.subtract(previousPoint).multiply((double)f));
                            this.drawPredictionMarker(class_45872, LootTableProvider, vertexBuffer, (float)VanillaFishingLootTableGenerator.distanceTo(previousPoint) / 3.0f, 1.0f);
                            this.drawPredictionMarker(class_45872, LootTableProvider, vertexBuffer, (float)VanillaFishingLootTableGenerator.distanceTo(previousPoint) * 2.0f, 0.05f);
                        }
                    }
                    previousPoint = VanillaFishingLootTableGenerator;
                }
                float f = 9.0f;
                if (!(trajectoryResult.getEntity() instanceof PotionEntity)) continue;
                class_45872.push();
                class_45872.translate(trajectoryResult.getVectors().getLast());
                class_45872.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-90.0f));
                ShaderRenderer.appendTexturedQuadVertices(class_45872, vertexBuffer, -f / 2.0f, -f / 2.0f, 0.0, f, f, colorRGBA.withAlpha(255.0f));
                class_45872.pop();
            }
            ItemRenderUtils.flushVertexConsumer(vertexBuffer);
        }
        float f = 1.0f;
        Identifier hitTexture = RockstarClient.resourceId("textures/hit.png");
        this.setPredictionTexture(hitTexture);
        RenderSystem.setShaderTexture((int)0, hitTexture);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder collisionMarkerBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (CollisionResult collisionResult : this.predictionItems) {
            if (collisionResult.getCollidedEntity() != null) continue;
            class_45872.push();
            class_45872.translate(collisionResult.getHitResult().getPos());
            class_45872.multiply(collisionResult.getHitResult().getSide().getRotationQuaternion());
            class_45872.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-90.0f));
            ShaderRenderer.appendTexturedQuadVertices(class_45872, collisionMarkerBuffer, -f / 2.0f, -f / 2.0f, 0.0, f, f, colorRGBA.withAlpha(255.0f));
            class_45872.pop();
        }
        ItemRenderUtils.flushVertexConsumer(collisionMarkerBuffer);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        Camera class_41842 = Prediction.minecraftClient.gameRenderer.getCamera();
        Vec3d class_24310 = class_41842.getPos();
        vertexBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (CollisionResult collisionResult : this.predictionItems) {
            if (collisionResult.getCollidedEntity() == null) continue;
            Box HorizontalFacingBlock = collisionResult.getCollidedEntity().getBoundingBox().offset(-class_24310.getX(), -class_24310.getY(), -class_24310.getZ());
            class_45872.push();
            class_45872.translate(class_24310.getX(), class_24310.getY(), class_24310.getZ());
            RenderUtils.drawFilledBox(class_45872, vertexBuffer, HorizontalFacingBlock, colorRGBA.mulAlpha(0.5f));
            class_45872.pop();
        }
        ItemRenderUtils.flushVertexConsumer(vertexBuffer);
        BufferBuilder UpdateStructureBlockC2SPacket = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (CollisionResult collisionResult : this.predictionItems) {
            if (collisionResult.getCollidedEntity() == null) continue;
            Box InfestedBlock = collisionResult.getCollidedEntity().getBoundingBox().offset(-class_24310.getX(), -class_24310.getY(), -class_24310.getZ());
            class_45872.push();
            class_45872.translate(class_24310.getX(), class_24310.getY(), class_24310.getZ());
            RenderUtils.drawBoxOutline(class_45872, UpdateStructureBlockC2SPacket, InfestedBlock, colorRGBA);
            class_45872.pop();
        }
        ItemRenderUtils.flushVertexConsumer(UpdateStructureBlockC2SPacket);
        ItemRenderUtils.endOverlayRendering();
        class_45872.pop();
    };

    public Prediction() {
        this.resetPredictionState();
    }

    @Compile(obfuscation=4)
    private void resetPredictionState() {
        this.entities = new MultiBooleanSetting(this, "modules.settings.prediction.entities");
        this.renderMode = new ModeSetting(this, "modules.settings.prediction.render_mode");
        this.defaultOption = new ModeSetting.Option(this.renderMode, "modules.settings.prediction.render_mode.default");
        this.glowOption = new ModeSetting.Option(this.renderMode, "modules.settings.prediction.render_mode.glow").select();
        this.hand = new BooleanSetting(this, "modules.settings.prediction.hand").enable();
        this.walls = new BooleanSetting(this, "modules.settings.prediction.walls").enable();
        this.hud = new BooleanSetting(this, "modules.settings.prediction.hud").enable();
        this.helper = new BooleanSetting(this, "modules.settings.prediction.helper");
        this.assist = new BooleanSetting((SettingOwner)this, "modules.settings.prediction.assist", () -> !this.helper.isEnabled());
        this.sync = new BooleanSetting(this, "theme.sync").enable();
        this.color = new ColorSetting(this, "modules.settings.chams.color", this.sync::isEnabled).setColor(ColorPalette.getAccentColor());
        this.pearlEntities = new MultiBooleanSetting.Option(this.entities, "modules.settings.prediction.entities.pearls").select();
        this.tridentEntities = new MultiBooleanSetting.Option(this.entities, "modules.settings.prediction.entities.tridents").select();
        this.snowballEntities = new MultiBooleanSetting.Option(this.entities, "modules.settings.prediction.entities.snowballs").select();
        this.arrowEntities = new MultiBooleanSetting.Option(this.entities, "modules.settings.prediction.entities.arrows").select();
        this.potionEntities = new MultiBooleanSetting.Option(this.entities, "modules.settings.prediction.entities.potions").select();
        this.itemEntities = new MultiBooleanSetting.Option(this.entities, "modules.settings.prediction.entities.items");
    }

    @Override
    public void onTick() {
        this.predictionSettings.clear();
        long l = System.currentTimeMillis();
        this.predictionEntries.removeIf(predictionEntry -> predictionEntry.isActive(l));
        for (Entity class_12972 : Prediction.minecraftClient.world.getEntities()) {
            this.updateEntityPrediction(class_12972, false);
        }
        this.clearPredictionPath();
    }

    private void updateRotationInput(float f) {
        ProjectileEntity projectile;
        int projectileIndex;
        this.predictionItems.clear();
        if (!this.hand.isEnabled() || Prediction.minecraftClient.player == null || Prediction.minecraftClient.world == null) {
            return;
        }
        Rotation rotation = this.calculatePitchRotation(f);
        ItemStack class_17992 = Prediction.minecraftClient.player.getMainHandStack();
        ArrayList<ProjectileEntity> arrayList = new ArrayList<ProjectileEntity>();
        ProjectileEntity primaryProjectile = null;
        if (class_17992.getItem() instanceof EnderPearlItem) {
            primaryProjectile = new EnderPearlEntity((World)Prediction.minecraftClient.world, (LivingEntity)Prediction.minecraftClient.player, class_17992);
        } else if (class_17992.getItem() instanceof TridentItem && Prediction.minecraftClient.player.isUsingItem()) {
            primaryProjectile = new TridentEntity((World)Prediction.minecraftClient.world, (LivingEntity)Prediction.minecraftClient.player, class_17992);
        } else if (class_17992.getItem() instanceof SnowballItem) {
            primaryProjectile = new SnowballEntity((World)Prediction.minecraftClient.world, (LivingEntity)Prediction.minecraftClient.player, class_17992);
        } else if (class_17992.getItem() instanceof BowItem && Prediction.minecraftClient.player.isUsingItem()) {
            ItemStack arrowStack = new ItemStack((ItemConvertible)Items.ARROW);
            primaryProjectile = new ArrowEntity((World)Prediction.minecraftClient.world, (LivingEntity)Prediction.minecraftClient.player, arrowStack, class_17992);
        } else if (class_17992.getItem() instanceof CrossbowItem && CrossbowItem.isCharged((ItemStack)class_17992)) {
            boolean bl = EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.MULTISHOT) > 0;
            ItemStack arrowStack = new ItemStack((ItemConvertible)Items.ARROW);
            if (bl) {
                for (projectileIndex = 0; projectileIndex < 3; ++projectileIndex) {
                    projectile = new ArrowEntity((World)Prediction.minecraftClient.world, (LivingEntity)Prediction.minecraftClient.player, arrowStack, class_17992);
                    arrayList.add(projectile);
                }
            } else {
                primaryProjectile = new ArrowEntity((World)Prediction.minecraftClient.world, (LivingEntity)Prediction.minecraftClient.player, arrowStack, class_17992);
            }
        }
        if (primaryProjectile != null) {
            projectile = primaryProjectile;
            float f2 = 1.5f;
            if (primaryProjectile instanceof TridentEntity) {
                f2 = 2.5f;
            } else if (primaryProjectile instanceof ArrowEntity) {
                f2 = 3.0f;
            }
            this.updateEntityTrajectory(projectile, (Entity)Prediction.minecraftClient.player, rotation.getPitch(), rotation.getYaw(), 0.0f, f2, 1.0f);
            this.resetProjectilePrediction(projectile, f);
            this.updateEntityPrediction(projectile, true);
        }
        if (!arrayList.isEmpty()) {
            float f3 = 3.15f;
            float f4 = 10.0f;
            for (projectileIndex = 0; projectileIndex < arrayList.size(); ++projectileIndex) {
                projectile = arrayList.get(projectileIndex);
                float f5 = 0.0f;
                if (projectileIndex == 0) {
                    f5 = -f4;
                } else if (projectileIndex == 2) {
                    f5 = f4;
                }
                this.updateEntityTrajectory(projectile, (Entity)Prediction.minecraftClient.player, rotation.getPitch(), rotation.getYaw() + f5, 0.0f, f3, 1.0f);
                this.resetProjectilePrediction(projectile, f);
                this.updateEntityPrediction(projectile, true);
            }
        }
    }

    private Rotation calculatePitchRotation(float f) {
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        if (rotationManager != null && !rotationManager.isIdle()) {
            return rotationManager.getAppliedRotation();
        }
        return new Rotation(Prediction.minecraftClient.player.getYaw(f), Prediction.minecraftClient.player.getPitch(f));
    }

    private void resetProjectilePrediction(ProjectileEntity class_16762, float f) {
        Vec3d VanillaChestLootTableGenerator = Prediction.minecraftClient.player.getCameraPosVec(f);
        class_16762.setPosition(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y - 0.1, VanillaChestLootTableGenerator.z);
    }

    private void clearPredictionPath() {
        float f;
        Vec3d VanillaChestLootTableGenerator;
        Rotation rotation;
        this.currentRotation = null;
        this.position = null;
        if (!this.helper.isEnabled() || Prediction.minecraftClient.player == null || Prediction.minecraftClient.world == null) {
            return;
        }
        Item class_17922 = Prediction.minecraftClient.player.getMainHandStack().getItem();
        if (!this.isPredictableItem(class_17922)) {
            return;
        }
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        Rotation rotation2 = rotationManager.getEffectiveRotation();
        Vec3d WallPlayerSkullBlock = Prediction.minecraftClient.player.getEyePos();
        float f2 = Float.MAX_VALUE;
        Rotation rotation3 = null;
        for (TrajectoryResult record : this.predictionSettings) {
            if (!(record.getEntity() instanceof EnderPearlEntity) || (rotation = this.calculateThrowRotation(WallPlayerSkullBlock, VanillaChestLootTableGenerator = this.calculateImpactPosition(record.getVectors().getLast(), class_17922), class_17922)) == null || !((f = rotation.angleDistanceTo(rotation2)) < f2)) continue;
            f2 = f;
            rotation3 = rotation;
            this.position = VanillaChestLootTableGenerator;
        }
        for (CollisionResult collisionResult : this.predictionItems) {
            if (collisionResult.isFromHand() || !(collisionResult.getEntity() instanceof EnderPearlEntity) || (rotation = this.calculateThrowRotation(WallPlayerSkullBlock, VanillaChestLootTableGenerator = this.calculateImpactPosition(collisionResult.getPos(), class_17922), class_17922)) == null || !((f = rotation.angleDistanceTo(rotation2)) < f2)) continue;
            f2 = f;
            rotation3 = rotation;
            this.position = VanillaChestLootTableGenerator;
        }
        if (rotation3 == null) {
            return;
        }
        this.currentRotation = AimRotationMath.snapRotationToMouseStep(rotationManager.getPlayerRotation(), rotation3);
        if (this.assist.isEnabled()) {
            Rotation rotation4 = rotationManager.getCurrentRotation();
            float f3 = Math.abs(AimRotationMath.getWrappedAngleDifference(rotation4.getYaw(), this.currentRotation.getYaw()));
            float f4 = Math.abs(rotation4.getPitch() - this.currentRotation.getPitch());
            if (f3 < 10.0f && f4 < 10.0f) {
                rotationManager.requestRotation(this.currentRotation, RotationCorrectionMode.UNSPECIFIED, 180.0f, 180.0f, 180.0f, RotationPriority.ITEM_USE_PRIORITY);
            }
        }
    }

    private Vec3d calculateImpactPosition(Vec3d VanillaChestLootTableGenerator, Item class_17922) {
        if (!(class_17922 instanceof EnderPearlItem)) {
            return VanillaChestLootTableGenerator;
        }
        BlockPos adminsky = BlockPos.ofFloored((Position)VanillaChestLootTableGenerator);
        return new Vec3d((double)adminsky.getX() + 0.5, (double)adminsky.getY() + 1.0, (double)adminsky.getZ() + 0.5);
    }

    private Rotation calculateThrowRotation(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, Item class_17922) {
        Rotation rotation = AimRotationMath.getRotationBetweenPoints(VanillaChestLootTableGenerator, WallPlayerSkullBlock);
        if (class_17922 instanceof EnderPearlItem) {
            Float f = this.calculateTargetDistance(VanillaChestLootTableGenerator, WallPlayerSkullBlock);
            if (f == null || Float.isNaN(f.floatValue())) {
                return null;
            }
            return new Rotation(rotation.getYaw(), f.floatValue());
        }
        if (class_17922 instanceof TridentItem) {
            double d = VanillaChestLootTableGenerator.distanceTo(WallPlayerSkullBlock);
            double d2 = Prediction.minecraftClient.player.getVelocity().y;
            float f = rotation.getPitch() - (float)(d * (double)0.22f) + (float)(d2 * d * (double)(d2 > 0.0 ? 0.5f : 1.0f));
            return new Rotation(rotation.getYaw(), f);
        }
        return null;
    }

    private Float calculateTargetDistance(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock) {
        double d = WallPlayerSkullBlock.x - VanillaChestLootTableGenerator.x;
        double d2 = WallPlayerSkullBlock.z - VanillaChestLootTableGenerator.z;
        double d3 = Math.hypot(d, d2);
        if (d3 < 0.001) {
            return null;
        }
        double d4 = 6.125 * (WallPlayerSkullBlock.y - VanillaChestLootTableGenerator.y);
        double d5 = (double)0.05f * ((double)0.05f * (d3 * d3) + d4);
        double d6 = 9.37890625 - d5;
        if (d6 < 0.0) {
            return null;
        }
        double d7 = Math.sqrt(d6);
        double d8 = 3.0625 - d7;
        double d9 = Math.atan2(d8 * d8 + d7, (double)0.05f * d3);
        double d10 = Math.atan2(d8, (double)0.05f * d3);
        double d11 = Math.min(d9, d10);
        return Float.valueOf((float)(-Math.toDegrees(d11)));
    }

    private boolean isPredictableItem(Item class_17922) {
        return class_17922 instanceof EnderPearlItem || class_17922 instanceof TridentItem;
    }

    private void updateEntityPrediction(Entity class_12972, boolean bl) {
        if (!this.isPredictionTarget(class_12972)) {
            return;
        }
        if (class_12972 instanceof ProjectileEntity projectile && projectile.getOwner() == null) {
            List<AbstractClientPlayerEntity> players = Prediction.minecraftClient.world.getPlayers();
            if (!players.isEmpty()) {
                players.sort(Comparator.comparingDouble(player -> Prediction.getProjectileDistance(projectile, player)));
                projectile.setOwner(players.getFirst());
            }
        }
        List<Vec3d> trajectory = new ArrayList<>();
        Vec3d currentPosition = class_12972.getPos();
        Vec3d currentVelocity = class_12972.getVelocity();
        Entity collidedEntity = null;
        int ticks = 0;
        BlockHitResult blockHit = null;
        while (ticks < 150) {
            Vec3d nextVelocity = this.calculateEntityVelocity(class_12972, currentVelocity);
            Vec3d nextPosition = currentPosition.add(nextVelocity);
            ticks++;
            blockHit = Prediction.minecraftClient.world.raycast(new RaycastContext(currentPosition, nextPosition, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, class_12972));
            Entity hitEntity = this.findPredictedEntity(class_12972, nextPosition);
            if (hitEntity != null) {
                trajectory.add(nextPosition);
                collidedEntity = hitEntity;
                break;
            }
            if (blockHit.getType() != HitResult.Type.MISS) {
                trajectory.add(blockHit.getPos());
                break;
            }
            trajectory.add(nextPosition);
            currentPosition = nextPosition;
            currentVelocity = nextVelocity;
        }
        if (!trajectory.isEmpty()) {
            if (bl) {
                this.predictionItems.add(new CollisionResult(class_12972, trajectory.getLast(), ticks, collidedEntity, blockHit, bl));
            } else {
                this.predictionSettings.add(new TrajectoryResult(class_12972, trajectory, ticks, collidedEntity));
                if (collidedEntity != null && class_12972 instanceof PotionEntity) {
                    PotionEntity class_16862 = (PotionEntity)class_12972;
                    this.updateProjectilePath(class_16862, collidedEntity, trajectory.getLast());
                }
            }
        }
    }

    public Vec3d getPredictedPosition() {
        if (Prediction.minecraftClient.player == null || Prediction.minecraftClient.world == null) {
            return null;
        }
        Rotation rotation = RockstarClient.create().getRotationManager().getEffectiveRotation();
        float f = rotation != null ? rotation.getPitch() : Prediction.minecraftClient.player.getPitch();
        float f2 = rotation != null ? rotation.getYaw() : Prediction.minecraftClient.player.getYaw();
        SnowballEntity class_16802 = new SnowballEntity((World)Prediction.minecraftClient.world, (LivingEntity)Prediction.minecraftClient.player, Items.SNOWBALL.getDefaultStack());
        this.updateEntityTrajectory((ProjectileEntity)class_16802, (Entity)Prediction.minecraftClient.player, f, f2, 0.0f, 1.5f, 1.0f);
        return this.predictEntityPosition((Entity)class_16802);
    }

    public Vec3d calculateEntityPosition(Entity class_12972) {
        if (class_12972 == null || Prediction.minecraftClient.world == null) {
            return null;
        }
        return this.predictEntityPosition(class_12972);
    }

    private Vec3d predictEntityPosition(Entity class_12972) {
        Vec3d VanillaChestLootTableGenerator = class_12972.getPos();
        Vec3d WallPlayerSkullBlock = class_12972.getVelocity();
        for (int i = 0; i < 150; ++i) {
            Vec3d VanillaEntityLootTableGenerator = this.calculateEntityVelocity(class_12972, WallPlayerSkullBlock);
            Vec3d PlayerSkullBlock = VanillaChestLootTableGenerator.add(VanillaEntityLootTableGenerator);
            BlockHitResult class_39652 = Prediction.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, PlayerSkullBlock, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, class_12972));
            Entity class_12973 = this.findPredictedEntity(class_12972, PlayerSkullBlock);
            if (class_12973 != null) {
                return PlayerSkullBlock;
            }
            if (class_39652.getType() != HitResult.Type.MISS) {
                return class_39652.getPos();
            }
            VanillaChestLootTableGenerator = PlayerSkullBlock;
            WallPlayerSkullBlock = VanillaEntityLootTableGenerator;
        }
        return VanillaChestLootTableGenerator;
    }

    private void setPredictionTexture(Identifier class_29602) {
        AbstractTexture ItemModelManager = minecraftClient.getTextureManager().getTexture(class_29602);
        if (ItemModelManager == null) {
            return;
        }
        if (ItemModelManager.getGlId() != this.boundTextureId) {
            this.boundTextureId = ItemModelManager.getGlId();
            ItemModelManager.bindTexture();
            GlStateManager._texParameter((int)3553, (int)33085, (int)4);
            GL30.glGenerateMipmap((int)3553);
        }
        ItemModelManager.setFilter(true, true);
    }

    private void drawPredictionMarker(MatrixStack class_45872, Vec3d VanillaChestLootTableGenerator, BufferBuilder class_2872, float f, float f2) {
        ColorRGBA colorRGBA = this.sync.isEnabled() ? ColorPalette.getAccentColor() : this.color.getColor();
        class_45872.push();
        class_45872.translate(VanillaChestLootTableGenerator);
        class_45872.multiply(Prediction.minecraftClient.gameRenderer.getCamera().getRotation());
        ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f / 2.0f, -f / 2.0f, 0.0, f, f, colorRGBA.withAlpha(255.0f * f2));
        class_45872.pop();
    }

    private boolean isPredictionTarget(Entity class_12972) {
        boolean bl = false;
        for (MultiBooleanSetting.Option option : this.entities.getSelectedOptions()) {
            boolean selectedEntityType = option == this.pearlEntities && class_12972 instanceof EnderPearlEntity
                || option == this.tridentEntities && class_12972 instanceof TridentEntity
                || option == this.snowballEntities && class_12972 instanceof SnowballEntity
                || option == this.arrowEntities && class_12972 instanceof ArrowEntity
                || option == this.potionEntities && class_12972 instanceof PotionEntity
                || option == this.itemEntities && class_12972 instanceof ItemEntity;
            if (!selectedEntityType) continue;
            bl = true;
        }
        if (class_12972 instanceof TridentEntity) {
            TridentEntity class_16852 = (TridentEntity)class_12972;
            if (class_16852.returnTimer > 0) {
                return false;
            }
        }
        return bl && (Math.abs(class_12972.getVelocity().x + class_12972.getVelocity().z) > (double)0.01f || Math.abs(class_12972.getVelocity().y) > (double)0.2f);
    }

    private void updateProjectilePath(PotionEntity class_16862, Entity class_12972, Vec3d VanillaChestLootTableGenerator) {
        long l = System.currentTimeMillis() + 2000L;
        for (PredictionEntry existingEntry : this.predictionEntries) {
            if (existingEntry.slot != class_16862.getId()) continue;
            existingEntry.recordPrediction(class_12972, VanillaChestLootTableGenerator, l);
            return;
        }
        String string = this.formatEntityName((Entity)class_16862);
        PredictionEntry newEntry = new PredictionEntry(class_16862.getId(), class_16862.getStack().copy(), string, class_12972, VanillaChestLootTableGenerator, l);
        this.predictionEntries.add(newEntry);
    }

    private Entity findPredictedEntity(Entity class_12972, Vec3d VanillaChestLootTableGenerator) {
        Vec3d WallPlayerSkullBlock = class_12972.getPos();
        Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator.subtract(WallPlayerSkullBlock);
        if (VanillaEntityLootTableGenerator.lengthSquared() == 0.0) {
            return null;
        }
        EntityHitResult class_39662 = ProjectileUtil.raycast((Entity)class_12972, (Vec3d)WallPlayerSkullBlock, (Vec3d)VanillaChestLootTableGenerator, (Box)class_12972.getBoundingBox().stretch(VanillaEntityLootTableGenerator).expand(0.5), class_12973 -> Prediction.minecraftClient.player != class_12973 && class_12973.isAlive() && !(class_12973 instanceof ItemEntity) && !(class_12973 instanceof SnowballEntity) && !(class_12973 instanceof ExperienceOrbEntity) && class_12973 != class_12972, (double)VanillaEntityLootTableGenerator.lengthSquared());
        return class_39662 != null ? class_39662.getEntity() : null;
    }

    private String formatEntityName(Entity class_12972) {
        if (class_12972 instanceof EnderPearlEntity) {
            return Items.ENDER_PEARL.getName().getString();
        }
        if (class_12972 instanceof PotionEntity) {
            PotionEntity class_16862 = (PotionEntity)class_12972;
            return class_16862.getStack().getFormattedName().getString();
        }
        return class_12972.getName().getString();
    }

    private void updateProjectileTrajectory(ProjectileEntity class_16762, double d, double d2, double d3, float f) {
        Vec3d VanillaChestLootTableGenerator = this.calculateProjectilePosition(class_16762, d, d2, d3, f);
        class_16762.setVelocity(VanillaChestLootTableGenerator);
        class_16762.velocityDirty = true;
        double d4 = VanillaChestLootTableGenerator.horizontalLength();
        class_16762.setYaw((float)(MathHelper.atan2((double)VanillaChestLootTableGenerator.x, (double)VanillaChestLootTableGenerator.z) * 57.2957763671875));
        class_16762.setPitch((float)(MathHelper.atan2((double)VanillaChestLootTableGenerator.y, (double)d4) * 57.2957763671875));
        class_16762.prevYaw = class_16762.getYaw();
        class_16762.prevPitch = class_16762.getPitch();
    }

    private void updateEntityTrajectory(ProjectileEntity class_16762, Entity class_12972, float f, float f2, float f3, float f4, float f5) {
        float f6 = -MathHelper.sin((float)(f2 * ((float)Math.PI / 180))) * MathHelper.cos((float)(f * ((float)Math.PI / 180)));
        float f7 = -MathHelper.sin((float)((f + f3) * ((float)Math.PI / 180)));
        float f8 = MathHelper.cos((float)(f2 * ((float)Math.PI / 180))) * MathHelper.cos((float)(f * ((float)Math.PI / 180)));
        this.updateProjectileTrajectory(class_16762, f6, f7, f8, f4);
        Vec3d VanillaChestLootTableGenerator = class_12972.getMovement();
        class_16762.setVelocity(class_16762.getVelocity().add(VanillaChestLootTableGenerator.x, class_12972.isOnGround() ? 0.0 : VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z));
    }

    private Vec3d calculateProjectilePosition(ProjectileEntity class_16762, double d, double d2, double d3, float f) {
        return new Vec3d(d, d2, d3).normalize().multiply((double)f);
    }

    private Vec3d calculateEntityVelocity(Entity class_12972, Vec3d VanillaChestLootTableGenerator) {
        return VanillaChestLootTableGenerator.multiply(0.99).add(0.0, -class_12972.getFinalGravity(), 0.0);
    }

    private String formatPredictionLabel(String string, int n) {
        if (n <= 0) {
            return string;
        }
        return string + "(" + moscow.rockstar.util.NumberFormatting.formatOneDecimal((float)n / 20.0f) + " " + Localization.translate("sec") + ")";
    }

    private String formatPredictionTicks(int n) {
        int n2 = n / 20;
        int n3 = n2 / 60;
        int n4 = n2 % 60;
        if (n3 > 0) {
            return String.format("%d:%02d", n3, n4);
        }
        return String.format("0:%02d", n4);
    }

    @Generated
    public List<TrajectoryResult> getPredictionEntries() {
        return this.predictionSettings;
    }

    private static /* synthetic */ double getProjectileDistance(ProjectileEntity class_16762, AbstractClientPlayerEntity TrackedPosition) {
        return TrackedPosition.distanceTo((Entity)class_16762);
    }
}
