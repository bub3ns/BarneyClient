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
 *  net.minecraft.CrossbowItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Box
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.MathHelper
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.modules.combat.targeting;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.FriendManager;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.core.TargetFilter;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.rotation.AimSolution;
import moscow.rockstar.modules.combat.targeting.TargetPoint;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.GameRendererEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Aim Bot", category=ModuleCategory.COMBAT, description="modules.descriptions.aim_bot")
public class AimBot
extends Module {
    private static final float MIN_AIM_DISTANCE = 0.05f;
    private static final float MAX_AIM_FACTOR = 0.99f;
    private static final float MAX_PREDICTION_TICKS = 3.0f;
    private static final float MAX_TARGET_DISTANCE = 3.15f;
    private static final float MIN_HITBOX_SCALE = 2.5f;
    private static final int MAX_RAYCAST_STEPS = 80;
    private static final int MAX_AIM_SAMPLES = 6;
    private static final int MAX_TARGET_COUNT = 6;
    private static final float MIN_ROTATION_STEP = 0.08f;
    private static final float MAX_ROTATION_FACTOR = 0.98f;
    private static final float PREDICTION_SCALE = 1.0f;
    private static final double DEFAULT_TARGET_DISTANCE = 12.0;
    private static final double MAX_ROTATION_DELTA = 8.0;
    private static final double ROTATION_EPSILON = 0.05;
    private static final double MAX_PREDICTION_TIME = 1000.0;
    private static final int MAX_HISTORY_LENGTH = 3;
    private MultiBooleanSetting.Option bow;
    private MultiBooleanSetting.Option crossbow;
    private MultiBooleanSetting.Option trident;
    private NumberSetting distance;
    private NumberSetting fov;
    private BooleanSetting predict;
    private BooleanSetting drawPredictedBox;
    private BooleanSetting silentAim;
    private MultiBooleanSetting.Option players;
    private MultiBooleanSetting.Option animals;
    private MultiBooleanSetting.Option mobs;
    private MultiBooleanSetting.Option invisiblesOption;
    private MultiBooleanSetting.Option nakedplayers;
    private MultiBooleanSetting.Option friends;
    private MultiBooleanSetting.Option rockusersOption;
    private boolean aiming = false;
    private int targetTicks = 0;
    private Rotation currentRotation = new Rotation(0.0f, 0.0f);
    private Rotation previousRotation = null;
    private long lastTargetTime = 0L;
    private int targetIndex = -1;
    private int selectedSlot = -1;
    private final Deque<Vec3d> history = new ArrayDeque<Vec3d>();
    private Box targetBounds = null;
    private boolean targetVisible = false;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        if (AimBot.minecraftClient.player == null || AimBot.minecraftClient.world == null) {
            return;
        }
        if (!this.isModeReady()) {
            this.aiming = false;
            this.targetTicks = 0;
            this.previousRotation = null;
            this.targetBounds = null;
            this.selectedSlot = -1;
            return;
        }
        boolean bl = AimBot.minecraftClient.player.isUsingItem();
        if (bl && !this.aiming) {
            this.targetTicks = 1;
            this.currentRotation = new Rotation(AimBot.minecraftClient.player.getYaw(), AimBot.minecraftClient.player.getPitch());
        }
        this.aiming = bl;
        if (this.targetTicks > 0) {
            --this.targetTicks;
            this.resetRotation(this.currentRotation);
            this.targetBounds = null;
            return;
        }
        TargetFilter targetFilter = new TargetFilter.Builder().players(this.players.isSelected()).animals(this.animals.isSelected()).mobs(this.mobs.isSelected()).invisibles(this.invisiblesOption.isSelected()).nakedPlayers(this.nakedplayers.isSelected()).friends(this.friends.isSelected()).rockstarUsers(this.rockusersOption.isSelected()).range(this.distance.getValue()).build();
        AimSolution aimSolution = this.selectAimSolution(targetFilter);
        if (aimSolution == null) {
            this.previousRotation = null;
            this.targetBounds = null;
            return;
        }
        this.resetRotation(aimSolution.getRotation());
    };
    private final EventListener<GameRendererEvent> onGameRendererEvent = gameRendererEvent -> {
        if (AimBot.minecraftClient.player == null) {
            return;
        }
        if (this.silentAim.isEnabled()) {
            return;
        }
        if (this.previousRotation == null) {
            this.lastTargetTime = 0L;
            return;
        }
        long l = System.nanoTime();
        float f = this.lastTargetTime == 0L ? 0.016666668f : MathHelper.clamp((float)((float)(l - this.lastTargetTime) / 1.0E9f), (float)0.004166667f, (float)0.1f);
        this.lastTargetTime = l;
        float f2 = 20.0f;
        float f3 = MathHelper.clamp((float)(1.0f - (float)Math.exp(-f2 * f)), (float)0.01f, (float)0.95f);
        float f4 = MathHelper.wrapDegrees((float)(this.previousRotation.getYaw() - AimBot.minecraftClient.player.getYaw()));
        float f5 = MathHelper.clamp((float)this.previousRotation.getPitch(), (float)-89.9f, (float)89.9f) - AimBot.minecraftClient.player.getPitch();
        float f6 = AimBot.minecraftClient.player.getYaw() + f4 * f3;
        float f7 = MathHelper.clamp((float)(AimBot.minecraftClient.player.getPitch() + f5 * f3), (float)-90.0f, (float)90.0f);
        AimBot.minecraftClient.player.setYaw(f6);
        AimBot.minecraftClient.player.setPitch(f7);
        AimBot.minecraftClient.player.setHeadYaw(f6);
        AimBot.minecraftClient.player.setBodyYaw(f6);
    };
    private final EventListener<Render3DEvent> onRender3DEvent = render3DEvent -> {
        if (!this.drawPredictedBox.isEnabled() || !this.predict.isEnabled()) {
            return;
        }
        if (this.targetBounds == null || AimBot.minecraftClient.player == null) {
            return;
        }
        ColorRGBA colorRGBA = ColorPalette.getAccentColor();
        MatrixStack class_45872 = render3DEvent.getMatrices();
        class_45872.push();
        ItemRenderUtils.beginOverlayRendering(true);
        ItemRenderUtils.translateToCamera(class_45872);
        RenderSystem.enableDepthTest();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        Camera class_41842 = AimBot.minecraftClient.gameRenderer.getCamera();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        Box HorizontalFacingBlock = this.targetBounds.offset(-VanillaChestLootTableGenerator.getX(), -VanillaChestLootTableGenerator.getY(), -VanillaChestLootTableGenerator.getZ());
        class_45872.push();
        class_45872.translate(VanillaChestLootTableGenerator.getX(), VanillaChestLootTableGenerator.getY(), VanillaChestLootTableGenerator.getZ());
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        RenderUtils.drawFilledBox(class_45872, class_2872, HorizontalFacingBlock, colorRGBA.mulAlpha(0.25f));
        ItemRenderUtils.flushVertexConsumer(class_2872);
        BufferBuilder CreativeInventoryActionC2SPacket = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        RenderUtils.drawBoxOutline(class_45872, CreativeInventoryActionC2SPacket, HorizontalFacingBlock, colorRGBA);
        ItemRenderUtils.flushVertexConsumer(CreativeInventoryActionC2SPacket);
        class_45872.pop();
        ItemRenderUtils.endOverlayRendering();
        class_45872.pop();
    };

    public AimBot() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        MultiBooleanSetting multiBooleanSetting = new MultiBooleanSetting(this, "modules.settings.aim_bot.items");
        this.bow = new MultiBooleanSetting.Option(multiBooleanSetting, "modules.settings.aim_bot.bow").select();
        this.crossbow = new MultiBooleanSetting.Option(multiBooleanSetting, "modules.settings.aim_bot.crossbow").select();
        this.trident = new MultiBooleanSetting.Option(multiBooleanSetting, "modules.settings.aim_bot.trident").select();
        this.predict = new BooleanSetting(this, "modules.settings.aim_bot.predict").enable();
        this.drawPredictedBox = new BooleanSetting((SettingOwner)this, "modules.settings.aim_bot.draw_predicted_box", () -> !this.predict.isEnabled()).enable();
        this.silentAim = new BooleanSetting(this, "modules.settings.aim_bot.silent_aim");
        this.distance = new NumberSetting(this, "modules.settings.aim_bot.distance").setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f).setValue(30.0f);
        this.fov = new NumberSetting(this, "modules.settings.aim_bot.fov").setMinValue(1.0f).setMaxValue(180.0f).setStep(1.0f).setValue(90.0f);
        MultiBooleanSetting multiBooleanSetting2 = new MultiBooleanSetting(this, "targets");
        this.players = new MultiBooleanSetting.Option(multiBooleanSetting2, "players").select();
        this.animals = new MultiBooleanSetting.Option(multiBooleanSetting2, "animals").select();
        this.mobs = new MultiBooleanSetting.Option(multiBooleanSetting2, "mobs").select();
        this.invisiblesOption = new MultiBooleanSetting.Option(multiBooleanSetting2, "invisibles").select();
        this.nakedplayers = new MultiBooleanSetting.Option(multiBooleanSetting2, "nakedPlayers").select();
        this.rockusersOption = new MultiBooleanSetting.Option(multiBooleanSetting2, "rockUsers");
        this.friends = new MultiBooleanSetting.Option(multiBooleanSetting2, "friends");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.previousRotation = null;
        this.targetBounds = null;
        this.history.clear();
        this.targetIndex = -1;
        this.selectedSlot = -1;
        this.lastTargetTime = 0L;
    }

    private AimSolution selectAimSolution(TargetFilter targetFilter) {
        float f = this.calculateRotation();
        if (f < 0.5f) {
            this.targetBounds = null;
            return null;
        }
        List<LivingEntity> list = this.collectAimTargets(targetFilter);
        if (list.isEmpty()) {
            this.selectedSlot = -1;
            this.targetBounds = null;
            return null;
        }
        AimSolution aimSolution = null;
        int n = Math.min(list.size(), 3);
        for (int i = 0; i < n; ++i) {
            LivingEntity class_13092 = list.get(i);
            boolean bl = this.selectedSlot != -1 ? class_13092.getId() == this.selectedSlot : i == 0;
            AimSolution aimSolution2 = this.calculateAimSolution(class_13092, f, bl);
            if (aimSolution2 == null) continue;
            if (aimSolution2.hitsTarget()) {
                this.selectedSlot = aimSolution2.getTargetEntityId();
                this.targetBounds = aimSolution2.getTargetBounds();
                return aimSolution2;
            }
            if (aimSolution != null) continue;
            aimSolution = aimSolution2;
        }
        if (aimSolution == null) {
            this.targetBounds = null;
            return null;
        }
        this.selectedSlot = aimSolution.getTargetEntityId();
        this.targetBounds = aimSolution.getTargetBounds();
        return aimSolution;
    }

    private List<LivingEntity> collectAimTargets(TargetFilter targetFilter) {
        Vec3d VanillaChestLootTableGenerator = AimBot.minecraftClient.player.getEyePos();
        Vec3d WallPlayerSkullBlock = AimBot.minecraftClient.player.getRotationVec(1.0f).normalize();
        double d = (double)MathHelper.clamp((float)this.fov.getValue(), (float)1.0f, (float)180.0f) * 0.5;
        FriendManager friendManager = RockstarClient.create().getFriendManager();
        ArrayList<TargetPoint> arrayList = new ArrayList<TargetPoint>();
        for (Object object : AimBot.minecraftClient.world.getEntities()) {
            if (!(object instanceof LivingEntity)) continue;
            LivingEntity object2 = (LivingEntity)object;
            if (!targetFilter.acceptsEntity((Entity)object)) continue;
            boolean bl = object2.getId() == this.selectedSlot;
            double d2 = this.calculateRotation(VanillaChestLootTableGenerator, WallPlayerSkullBlock, object2);
            if (d2 > (bl ? d + 12.0 : d)) continue;
            double d3 = d2 + VanillaChestLootTableGenerator.distanceTo(object2.getBoundingBox().getCenter()) * 0.05;
            if (bl) {
                d3 -= 8.0;
            }
            if (friendManager != null && friendManager.isFriend(object2.getName().getString())) {
                d3 -= 1000.0;
            }
            arrayList.add(new TargetPoint(object2, d3));
        }
        arrayList.sort(Comparator.comparingDouble(TargetPoint::getScore));
        ArrayList arrayList2 = new ArrayList(arrayList.size());
        for (TargetPoint targetPoint : arrayList) {
            arrayList2.add(targetPoint.getEntity());
        }
        return arrayList2;
    }

    private double calculateRotation(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, LivingEntity class_13092) {
        Vec3d VanillaEntityLootTableGenerator = class_13092.getBoundingBox().getCenter().subtract(VanillaChestLootTableGenerator);
        double d = VanillaEntityLootTableGenerator.length();
        if (d < 1.0E-4) {
            return 0.0;
        }
        double d2 = MathHelper.clamp((double)WallPlayerSkullBlock.dotProduct(VanillaEntityLootTableGenerator.multiply(1.0 / d)), (double)-1.0, (double)1.0);
        double d3 = Math.toDegrees(Math.acos(d2));
        double d4 = Math.toDegrees(Math.atan2(Math.max((double)class_13092.getWidth() * 0.5, (double)class_13092.getHeight() * 0.35), d));
        return Math.max(0.0, d3 - d4);
    }

    private AimSolution calculateAimSolution(LivingEntity class_13092, float f, boolean bl) {
        Vec3d VanillaChestLootTableGenerator = AimBot.minecraftClient.player.getEyePos();
        Vec3d WallPlayerSkullBlock = this.predict.isEnabled() ? this.getPosition(class_13092, VanillaChestLootTableGenerator, f, bl).subtract(class_13092.getPos()) : Vec3d.ZERO;
        Box HorizontalFacingBlock = class_13092.getBoundingBox().offset(WallPlayerSkullBlock.x, WallPlayerSkullBlock.y, WallPlayerSkullBlock.z);
        Rotation rotation = null;
        Rotation rotation2 = null;
        double d = Double.MAX_VALUE;
        double d2 = Double.MAX_VALUE;
        List<Vec3d> list = this.collectAimPoints(class_13092, WallPlayerSkullBlock);
        for (int i = 0; i < list.size(); ++i) {
            Rotation rotation3 = this.getRotation(VanillaChestLootTableGenerator, list.get(i), f);
            if (rotation3 == null) continue;
            double d3 = (double)this.calculateRotation(rotation3) + (double)i * 0.75;
            if (d3 < d2) {
                d2 = d3;
                rotation2 = rotation3;
            }
            if (!(d3 < d) || !this.isEntityValid(VanillaChestLootTableGenerator, rotation3, f, class_13092, WallPlayerSkullBlock)) continue;
            d = d3;
            rotation = rotation3;
        }
        if (rotation != null) {
            return new AimSolution(rotation, HorizontalFacingBlock, true, class_13092.getId());
        }
        if (rotation2 != null) {
            return new AimSolution(rotation2, HorizontalFacingBlock, false, class_13092.getId());
        }
        return null;
    }

    private Vec3d getPosition(LivingEntity class_13092, Vec3d VanillaChestLootTableGenerator, float f, boolean bl) {
        Vec3d WallPlayerSkullBlock = bl ? this.getPosition(class_13092) : new Vec3d(class_13092.getX() - class_13092.prevX, class_13092.getY() - class_13092.prevY, class_13092.getZ() - class_13092.prevZ);
        Vec3d VanillaEntityLootTableGenerator = class_13092.getPos();
        boolean bl2 = !class_13092.isOnGround() && !class_13092.isClimbing() && !class_13092.isTouchingWater() && !class_13092.hasNoGravity();
        double d = bl ? this.calculateMagnitude() : 0.9;
        Vec3d PlayerSkullBlock = new Vec3d(WallPlayerSkullBlock.x * d, 0.0, WallPlayerSkullBlock.z * d);
        double d2 = WallPlayerSkullBlock.y;
        double d3 = VanillaChestLootTableGenerator.distanceTo(VanillaEntityLootTableGenerator) / (double)f;
        Vec3d RedstoneBlock = VanillaEntityLootTableGenerator;
        for (int i = 0; i < 6; ++i) {
            Vec3d VanillaFishingLootTableGenerator = this.getPosition(class_13092, VanillaEntityLootTableGenerator, PlayerSkullBlock, d3);
            double d4 = this.calculateMagnitude(d2, d3, bl2);
            RedstoneBlock = VanillaEntityLootTableGenerator.add(VanillaFishingLootTableGenerator.x, d4, VanillaFishingLootTableGenerator.z);
            double d5 = this.calculateMagnitude(VanillaChestLootTableGenerator, RedstoneBlock, f);
            if (Double.isNaN(d5) || d5 < 0.0) {
                d3 = VanillaChestLootTableGenerator.distanceTo(RedstoneBlock) / (double)f;
                continue;
            }
            if (Math.abs(d5 - d3) < 0.05) {
                d3 = d5;
                break;
            }
            d3 = d5;
        }
        return RedstoneBlock;
    }

    private double calculateMagnitude() {
        if (this.targetVisible) {
            return 0.9;
        }
        int n = this.history.size();
        if (n < 3) {
            return 0.9;
        }
        Vec3d[] class_243Array = this.history.toArray(new Vec3d[0]);
        double d = 0.0;
        for (int i = 1; i < n; ++i) {
            double d2 = class_243Array[i].x - class_243Array[i - 1].x;
            double d3 = class_243Array[i].z - class_243Array[i - 1].z;
            d += Math.sqrt(d2 * d2 + d3 * d3);
        }
        double d4 = class_243Array[n - 1].x - class_243Array[0].x;
        double d5 = class_243Array[n - 1].z - class_243Array[0].z;
        double d6 = Math.sqrt(d4 * d4 + d5 * d5);
        if (d < 0.05) {
            return 1.0;
        }
        return MathHelper.clamp((double)(d6 / d), (double)0.2, (double)1.0);
    }

    private double calculateMagnitude(double d, double d2, boolean bl) {
        if (!bl) {
            return d * d2;
        }
        int n = (int)Math.floor(d2);
        double d3 = d2 - (double)n;
        double d4 = d;
        double d5 = 0.0;
        for (int i = 0; i < n; ++i) {
            d5 += (d4 -= (double)0.08f);
            d4 *= (double)0.98f;
        }
        if (d3 > 0.001) {
            double d6 = d4 - (double)0.08f;
            d5 += d6 * d3;
        }
        return d5;
    }

    private Vec3d getPosition(LivingEntity class_13092, Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, double d) {
        Vec3d VanillaEntityLootTableGenerator;
        double d2 = Math.sqrt(WallPlayerSkullBlock.x * WallPlayerSkullBlock.x + WallPlayerSkullBlock.z * WallPlayerSkullBlock.z) * d;
        if (d2 < 0.05) {
            return new Vec3d(WallPlayerSkullBlock.x * d, 0.0, WallPlayerSkullBlock.z * d);
        }
        Vec3d PlayerSkullBlock = VanillaChestLootTableGenerator.add(0.0, (double)class_13092.getHeight() * 0.5, 0.0);
        BlockHitResult class_39652 = AimBot.minecraftClient.world.raycast(new RaycastContext(PlayerSkullBlock, VanillaEntityLootTableGenerator = PlayerSkullBlock.add(WallPlayerSkullBlock.x * d, 0.0, WallPlayerSkullBlock.z * d), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)class_13092));
        if (class_39652.getType() == HitResult.Type.BLOCK) {
            double d3 = class_39652.getPos().distanceTo(PlayerSkullBlock) - 0.3;
            if (d3 <= 0.0) {
                return Vec3d.ZERO;
            }
            double d4 = d3 / d2;
            return new Vec3d(WallPlayerSkullBlock.x * d * d4, 0.0, WallPlayerSkullBlock.z * d * d4);
        }
        return new Vec3d(WallPlayerSkullBlock.x * d, 0.0, WallPlayerSkullBlock.z * d);
    }

    private Vec3d getPosition(LivingEntity class_13092) {
        double d;
        if (this.targetIndex != class_13092.getId()) {
            this.history.clear();
            this.targetVisible = false;
            this.targetIndex = class_13092.getId();
        }
        this.history.addLast(class_13092.getPos());
        while (this.history.size() > 6) {
            this.history.removeFirst();
        }
        if (this.history.size() < 2) {
            this.targetVisible = false;
            return new Vec3d(class_13092.getX() - class_13092.prevX, class_13092.getY() - class_13092.prevY, class_13092.getZ() - class_13092.prevZ);
        }
        Vec3d[] class_243Array = this.history.toArray(new Vec3d[0]);
        int n = class_243Array.length;
        Vec3d VanillaChestLootTableGenerator = this.getPosition(class_243Array);
        Vec3d WallPlayerSkullBlock = class_243Array[n - 1].subtract(class_243Array[n - 2]);
        double d2 = WallPlayerSkullBlock.x * WallPlayerSkullBlock.x + WallPlayerSkullBlock.z * WallPlayerSkullBlock.z;
        double d3 = VanillaChestLootTableGenerator.x * VanillaChestLootTableGenerator.x + VanillaChestLootTableGenerator.z * VanillaChestLootTableGenerator.z;
        if (d2 > 0.005 && d3 > 0.0025 && (d = (WallPlayerSkullBlock.x * VanillaChestLootTableGenerator.x + WallPlayerSkullBlock.z * VanillaChestLootTableGenerator.z) / (Math.sqrt(d2) * Math.sqrt(d3))) < 0.3) {
            this.targetVisible = true;
            return WallPlayerSkullBlock;
        }
        this.targetVisible = false;
        return VanillaChestLootTableGenerator;
    }

    private Vec3d getPosition(Vec3d[] class_243Array) {
        int n = class_243Array.length;
        double d = 0.0;
        double d2 = 0.0;
        double d3 = 0.0;
        double d4 = 0.0;
        for (int i = 1; i < n; ++i) {
            double d5 = i;
            d += (class_243Array[i].x - class_243Array[i - 1].x) * d5;
            d2 += (class_243Array[i].y - class_243Array[i - 1].y) * d5;
            d3 += (class_243Array[i].z - class_243Array[i - 1].z) * d5;
            d4 += d5;
        }
        if (d4 < 1.0E-6) {
            return Vec3d.ZERO;
        }
        double d6 = 1.0 / d4;
        return new Vec3d(d * d6, d2 * d6, d3 * d6);
    }

    private double calculateMagnitude(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, double d) {
        double d2 = WallPlayerSkullBlock.x - VanillaChestLootTableGenerator.x;
        double d3 = WallPlayerSkullBlock.z - VanillaChestLootTableGenerator.z;
        double d4 = WallPlayerSkullBlock.y - VanillaChestLootTableGenerator.y;
        double d5 = Math.sqrt(d2 * d2 + d3 * d3);
        if (d5 < 1.0E-4) {
            return Double.NaN;
        }
        double d6 = d * d;
        double d7 = 0.05f;
        double d8 = d6 * d6 - d7 * (d7 * d5 * d5 + 2.0 * d4 * d6);
        if (d8 < 0.0) {
            return d5 / d;
        }
        double d9 = (d6 - Math.sqrt(d8)) / (d7 * d5);
        double d10 = 1.0 / Math.sqrt(1.0 + d9 * d9);
        double d11 = d * d10;
        if (d11 < 1.0E-4) {
            return d5 / d;
        }
        double d12 = 0.00999999f;
        double d13 = d5 * d12 / d11;
        if (d13 >= 0.999) {
            return d5 / d11 * 1.5;
        }
        return Math.log(1.0 - d13) / Math.log(0.99f);
    }

    private Rotation getRotation(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, float f) {
        double d = WallPlayerSkullBlock.x - VanillaChestLootTableGenerator.x;
        double d2 = WallPlayerSkullBlock.z - VanillaChestLootTableGenerator.z;
        double d3 = WallPlayerSkullBlock.y - VanillaChestLootTableGenerator.y;
        double d4 = Math.sqrt(d * d + d2 * d2);
        if (d4 < 1.0E-4) {
            return null;
        }
        double d5 = f * f;
        double d6 = 0.05f;
        double d7 = d5 * d5 - d6 * (d6 * d4 * d4 + 2.0 * d3 * d5);
        if (d7 < 0.0) {
            return null;
        }
        double d8 = (d5 - Math.sqrt(d7)) / (d6 * d4);
        float f2 = (float)(Math.toDegrees(Math.atan2(d2, d)) - 90.0);
        float f3 = (float)(-Math.toDegrees(Math.atan(d8)));
        f3 = MathHelper.clamp((float)f3, (float)-89.9f, (float)89.9f);
        return new Rotation(f2, f3);
    }

    private boolean isEntityValid(Vec3d VanillaChestLootTableGenerator, Rotation rotation, float f, LivingEntity class_13092, Vec3d WallPlayerSkullBlock) {
        double d = Math.toRadians(rotation.getYaw());
        double d2 = Math.toRadians(rotation.getPitch());
        double d3 = Math.cos(d2);
        Vec3d VanillaEntityLootTableGenerator = new Vec3d(-Math.sin(d) * d3, -Math.sin(d2), Math.cos(d) * d3).multiply((double)f);
        Vec3d PlayerSkullBlock = VanillaChestLootTableGenerator;
        Box HorizontalFacingBlock = class_13092.getBoundingBox().offset(WallPlayerSkullBlock.x, WallPlayerSkullBlock.y, WallPlayerSkullBlock.z).expand(0.1);
        int n = MathHelper.clamp((int)((int)(VanillaChestLootTableGenerator.distanceTo(HorizontalFacingBlock.getCenter()) / (double)f * 3.0) + 10), (int)20, (int)80);
        double d4 = HorizontalFacingBlock.minY - 5.0;
        for (int i = 0; i < n; ++i) {
            Vec3d RedstoneBlock = PlayerSkullBlock.add(VanillaEntityLootTableGenerator);
            if (RedstoneBlock.y < d4 && VanillaEntityLootTableGenerator.y < 0.0) {
                return false;
            }
            Optional optional = HorizontalFacingBlock.raycast(PlayerSkullBlock, RedstoneBlock);
            BlockHitResult class_39652 = AimBot.minecraftClient.world.raycast(new RaycastContext(PlayerSkullBlock, RedstoneBlock, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)AimBot.minecraftClient.player));
            if (optional.isPresent()) {
                double d5;
                double d6;
                return class_39652.getType() != HitResult.Type.BLOCK || !((d6 = class_39652.getPos().squaredDistanceTo(PlayerSkullBlock)) < (d5 = ((Vec3d)optional.get()).squaredDistanceTo(PlayerSkullBlock)));
            }
            if (class_39652.getType() == HitResult.Type.BLOCK) {
                return false;
            }
            PlayerSkullBlock = RedstoneBlock;
            VanillaEntityLootTableGenerator = VanillaEntityLootTableGenerator.multiply((double)0.99f);
            VanillaEntityLootTableGenerator = new Vec3d(VanillaEntityLootTableGenerator.x, VanillaEntityLootTableGenerator.y - (double)0.05f, VanillaEntityLootTableGenerator.z);
        }
        return false;
    }

    private List<Vec3d> collectAimPoints(LivingEntity class_13092, Vec3d VanillaChestLootTableGenerator) {
        Box HorizontalFacingBlock = class_13092.getBoundingBox().offset(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z);
        Vec3d WallPlayerSkullBlock = HorizontalFacingBlock.getCenter();
        double d = class_13092.getY() + VanillaChestLootTableGenerator.y;
        double d2 = class_13092.getHeight();
        ArrayList<Vec3d> arrayList = new ArrayList<Vec3d>(8);
        arrayList.add(new Vec3d(WallPlayerSkullBlock.x, d + d2 * 0.85, WallPlayerSkullBlock.z));
        arrayList.add(new Vec3d(WallPlayerSkullBlock.x, d + d2 * 0.65, WallPlayerSkullBlock.z));
        arrayList.add(new Vec3d(WallPlayerSkullBlock.x, d + d2 * 0.5, WallPlayerSkullBlock.z));
        arrayList.add(new Vec3d(WallPlayerSkullBlock.x, d + d2 * 0.3, WallPlayerSkullBlock.z));
        arrayList.add(new Vec3d(HorizontalFacingBlock.minX + 0.1, d + d2 * 0.55, WallPlayerSkullBlock.z));
        arrayList.add(new Vec3d(HorizontalFacingBlock.maxX - 0.1, d + d2 * 0.55, WallPlayerSkullBlock.z));
        arrayList.add(new Vec3d(WallPlayerSkullBlock.x, d + d2 * 0.55, HorizontalFacingBlock.minZ + 0.1));
        arrayList.add(new Vec3d(WallPlayerSkullBlock.x, d + d2 * 0.55, HorizontalFacingBlock.maxZ - 0.1));
        return arrayList;
    }

    private float calculateRotation(Rotation rotation) {
        double d = Math.toRadians(rotation.getYaw());
        double d2 = Math.toRadians(rotation.getPitch());
        double d3 = Math.cos(d2);
        Vec3d VanillaChestLootTableGenerator = new Vec3d(-Math.sin(d) * d3, -Math.sin(d2), Math.cos(d) * d3);
        Vec3d WallPlayerSkullBlock = AimBot.minecraftClient.player.getRotationVec(1.0f).normalize();
        double d4 = MathHelper.clamp((double)WallPlayerSkullBlock.dotProduct(VanillaChestLootTableGenerator), (double)-1.0, (double)1.0);
        return (float)Math.toDegrees(Math.acos(d4));
    }

    private float calculateRotation() {
        Item class_17922 = AimBot.minecraftClient.player.getMainHandStack().getItem();
        if (class_17922 == Items.BOW) {
            return 3.0f;
        }
        if (class_17922 == Items.CROSSBOW) {
            return 3.15f;
        }
        if (class_17922 == Items.TRIDENT) {
            return 2.5f;
        }
        return 3.0f;
    }

    private void resetRotation(Rotation rotation) {
        float f = MathHelper.clamp((float)rotation.getPitch(), (float)-89.9f, (float)89.9f);
        if (this.silentAim.isEnabled()) {
            float f2 = RockstarClient.create().getRotationManager().getCurrentRotation().getYaw();
            float f3 = f2 + MathHelper.wrapDegrees((float)(rotation.getYaw() - f2));
            RockstarClient.create().getRotationManager().requestRotation(new Rotation(f3, f), RotationCorrectionMode.DIRECT, 120.0f, 120.0f, 120.0f, RotationPriority.TARGET_PRIORITY);
            this.previousRotation = null;
        } else {
            this.previousRotation = new Rotation(rotation.getYaw(), f);
        }
    }

    private boolean isModeReady() {
        ItemStack class_17992 = AimBot.minecraftClient.player.getMainHandStack();
        Item class_17922 = class_17992.getItem();
        if (class_17922 == Items.BOW && this.bow.isSelected()) {
            return AimBot.minecraftClient.player.isUsingItem();
        }
        if (class_17922 == Items.CROSSBOW && this.crossbow.isSelected()) {
            return AimBot.minecraftClient.player.isUsingItem() || CrossbowItem.isCharged((ItemStack)class_17992);
        }
        if (class_17922 == Items.TRIDENT && this.trident.isSelected()) {
            return AimBot.minecraftClient.player.isUsingItem();
        }
        return false;
    }
}
