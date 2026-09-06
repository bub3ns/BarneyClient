/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.Hand
 *  net.minecraft.Arm
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.CrossbowItem
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.World
 *  net.minecraft.Position
 *  net.minecraft.MathHelper
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.VertexConsumerProvider$Immediate
 *  net.minecraft.AbstractClientPlayerEntity
 *  net.minecraft.ClientPlayerEntity
 *  net.minecraft.HeldItemRenderer
 *  net.minecraft.RotationAxis
 *  net.minecraft.ModelTransformationMode
 *  net.minecraft.ItemRenderer
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.render.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.esp.entities.Fill;
import moscow.rockstar.modules.visuals.esp.entities.Flame;
import moscow.rockstar.modules.visuals.esp.entities.Glow;
import moscow.rockstar.modules.ModuleRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.visuals.esp.targeting.ItemTargetType;
import moscow.rockstar.modules.visuals.hand.SwingAnimation;
import moscow.rockstar.modules.visuals.hand.ViewModel;
import moscow.rockstar.render.esp.OverlayRegistry;
import moscow.rockstar.render.item.HeldItemRenderCapture;
import moscow.rockstar.render.world.DynamicLightGrid;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.events.render.HandRenderEvent;

@Mixin(value={HeldItemRenderer.class})
public abstract class HeldItemRendererMixin {
    @Shadow
    @Final
    private ItemRenderer itemRenderer;
    @Shadow
    private ItemStack mainHand;
    @Shadow
    private ItemStack offHand;
    @Shadow
    private float equipProgressMainHand;
    @Shadow
    private float prevEquipProgressMainHand;
    @Shadow
    private float equipProgressOffHand;
    @Shadow
    private float prevEquipProgressOffHand;
    @Unique
    private static boolean rockstar$handRenderHijacked;
    @Unique
    private static boolean rockstar$firstPersonItemRendered;
    @Unique
    private static boolean rockstar$tookOverHandRender;
    @Unique
    private static boolean rockstar$decoratingHeldItem;

    @Shadow
    protected abstract void applyEatOrDrinkTransformation(MatrixStack var1, float var2, Arm var3, ItemStack var4, PlayerEntity var5);

    @Shadow
    protected abstract void applyBrushTransformation(MatrixStack var1, float var2, Arm var3, ItemStack var4, PlayerEntity var5, float var6);

    @Shadow
    protected abstract void renderFirstPersonItem(AbstractClientPlayerEntity var1, float var2, float var3, Hand var4, float var5, ItemStack var6, float var7, MatrixStack var8, VertexConsumerProvider var9, int var10);

    @ModifyVariable(method={"renderFirstPersonItem"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0)
    private int rockstar$applyDynamicLight(int n) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return n;
        }
        return DynamicLightGrid.apply(BlockPos.ofFloored(client.player.getEyePos()), n);
    }

    @Inject(method={"renderFirstPersonItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderFirstPersonItem(AbstractClientPlayerEntity TrackedPosition, float f, float f2, Hand class_12682, float f3, ItemStack class_17992, float f4, MatrixStack class_45872, VertexConsumerProvider class_45972, int n, CallbackInfo callbackInfo) {
        rockstar$firstPersonItemRendered = true;
        n = DynamicLightGrid.apply(BlockPos.ofFloored(TrackedPosition.getEyePos()), n);
        boolean bl = class_12682 == Hand.MAIN_HAND;
        Arm class_13062 = bl ? TrackedPosition.getMainArm() : TrackedPosition.getMainArm().getOpposite();
        boolean bl2 = class_13062 == Arm.RIGHT;
        HeldItemRenderCapture.leftHand = !bl2;
        class_45872.push();
        HandRenderEvent handRenderEvent = new HandRenderEvent(class_13062, f3, class_17992, f4, class_45872);
        RockstarClient.create().getEventBus().post(handRenderEvent);
        if (handRenderEvent.isCancelled()) {
            callbackInfo.cancel();
            float f5 = -0.4f * MathHelper.sin((float)(MathHelper.sqrt((float)0.0f) * (float)Math.PI));
            float f6 = 0.2f * MathHelper.sin((float)(MathHelper.sqrt((float)0.0f) * ((float)Math.PI * 2)));
            float f7 = -0.2f * MathHelper.sin((float)0.0f);
            class_45872.translate((float)(class_13062 == Arm.RIGHT ? 1 : -1) * f5, f6, f7);
            int n2 = class_13062 == Arm.RIGHT ? 1 : -1;
            class_45872.translate((float)n2 * 0.56f, -0.52f, -0.72f);
            if (!class_17992.isEmpty()) {
                HeldItemRenderer heldItemRenderer = (HeldItemRenderer)(Object)this;
                ModelTransformationMode DeathMessageType = bl2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND;
                heldItemRenderer.renderItem((LivingEntity)TrackedPosition, class_17992, DeathMessageType, !bl2, class_45872, class_45972, n);
            }
            class_45872.pop();
        }
    }

    @Inject(method={"renderFirstPersonItem"}, at={@At(value="RETURN")})
    private void onRenderFirstPersonItemEnd(AbstractClientPlayerEntity TrackedPosition, float f, float f2, Hand class_12682, float f3, ItemStack class_17992, float f4, MatrixStack class_45872, VertexConsumerProvider class_45972, int n, CallbackInfo callbackInfo) {
        class_45872.pop();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapOperation(method={"renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V")})
    private void rockstar$decorateHeldItem(ItemRenderer HorseChestIndexingFix, LivingEntity class_13092, ItemStack class_17992, ModelTransformationMode DeathMessageType, boolean bl, MatrixStack class_45872, VertexConsumerProvider class_45972, World class_19372, int n, int n2, int n3, Operation<Void> operation) {
        VertexConsumerProvider.Immediate class_45982;
        boolean bl2;
        ViewModel viewModel;
        if (!rockstar$decoratingHeldItem && HeldItemRenderCapture.capturing && !ViewModel.renderingHands && HeldItemRendererMixin.rockstar$isHeldInFirstPerson(class_13092, DeathMessageType) && (viewModel = HeldItemRendererMixin.rockstar$viewModel()) != null) {
            viewModel.renderHandItem(HorseChestIndexingFix, class_13092, class_17992, DeathMessageType, bl, class_45872, class_19372, n, n2, n3);
        }
        if (rockstar$decoratingHeldItem || !HeldItemRenderCapture.capturing || !HeldItemRendererMixin.rockstar$isHeldInFirstPerson(class_13092, DeathMessageType)) {
            operation.call(new Object[]{HorseChestIndexingFix, class_13092, class_17992, DeathMessageType, bl, class_45872, class_45972, class_19372, n, n2, n3});
            return;
        }
        HeldItemRenderCapture.captureItemPose(class_17992, DeathMessageType, bl, class_45872);
        Glow glowRenderer = OverlayRegistry.getInstance().findOverlayByType(Glow.class);
        Fill fillRenderer = OverlayRegistry.getInstance().findOverlayByType(Fill.class);
        Flame flameRenderer = OverlayRegistry.getInstance().findOverlayByType(Flame.class);
        boolean glowEnabled = glowRenderer != null && glowRenderer.isValid2(ItemTargetType.HELD);
        boolean fillEnabled = fillRenderer != null && fillRenderer.isValid2(ItemTargetType.HELD);
        boolean flameEnabled = flameRenderer != null && flameRenderer.isValid2(ItemTargetType.HELD);
        // The original path always keeps the vanilla item pass unless a
        // renderer explicitly reports that it blocks it.  Fill currently
        // reports no such block; using isValid() here would silently remove
        // the item whenever Fill is enabled and would change the source
        // client's render composition.
        boolean fillRenderingBlocked = fillRenderer != null && fillRenderer.isFillRenderingBlocked();
        if (!fillRenderingBlocked) {
            operation.call(new Object[]{HorseChestIndexingFix, class_13092, class_17992, DeathMessageType, bl, class_45872, class_45972, class_19372, n, n2, n3});
        }
        if (!(glowEnabled || fillEnabled || flameEnabled) || !(class_13092 instanceof AbstractClientPlayerEntity)) {
            return;
        }
        AbstractClientPlayerEntity TrackedPosition = (AbstractClientPlayerEntity)class_13092;
        if (class_45972 instanceof VertexConsumerProvider.Immediate) {
            class_45982 = (VertexConsumerProvider.Immediate)class_45972;
            class_45982.draw();
        }
        HeldItemRenderer heldItemRenderer = (HeldItemRenderer)(Object)this;
        rockstar$decoratingHeldItem = true;
        try {
            if (glowEnabled) {
                glowRenderer.renderHeldItemGlow(heldItemRenderer, TrackedPosition, class_17992, DeathMessageType, bl, class_45872, n);
            }
            if (flameEnabled) {
                flameRenderer.renderItemWithFlame(heldItemRenderer, TrackedPosition, class_17992, DeathMessageType, bl, class_45872, n);
            }
            if (fillEnabled) {
                fillRenderer.renderHeldItemFill(heldItemRenderer, TrackedPosition, class_17992, DeathMessageType, bl, class_45872, n);
            }
        }
        finally {
            rockstar$decoratingHeldItem = false;
        }
    }

    @Inject(method={"renderArmHoldingItem"}, at={@At(value="RETURN")})
    private void rockstar$captureArmMask(MatrixStack class_45872, VertexConsumerProvider class_45972, int n, float f, float f2, Arm class_13062, CallbackInfo callbackInfo) {
        if (!HeldItemRenderCapture.capturing || rockstar$decoratingHeldItem || HeldItemRenderCapture.rendering || ViewModel.renderingHands) {
            return;
        }
        HeldItemRenderCapture.captureArmPose(class_13062, class_45872);
        ViewModel viewModel = HeldItemRendererMixin.rockstar$viewModel();
        if (viewModel != null) {
            viewModel.applyHandTransform(class_13062, class_45872);
        }
    }

    @Unique
    private static ViewModel rockstar$viewModel() {
        ModuleRegistry moduleRegistry = RockstarClient.create().getModuleRegistry();
        return moduleRegistry == null ? null : moduleRegistry.getModule(ViewModel.class);
    }

    @Inject(method={"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$takeOverHandRender(float f, MatrixStack class_45872, VertexConsumerProvider.Immediate class_45982, ClientPlayerEntity class_7462, int n, CallbackInfo callbackInfo) {
        rockstar$firstPersonItemRendered = false;
        rockstar$tookOverHandRender = false;
        HeldItemRenderCapture.beginCapture();
        if (!rockstar$handRenderHijacked || !this.rockstar$needsHandRenderEvent()) {
            return;
        }
        rockstar$tookOverHandRender = true;
        callbackInfo.cancel();
        float f2 = class_7462.getHandSwingProgress(f);
        Hand class_12682 = class_7462.preferredHand == null ? Hand.MAIN_HAND : class_7462.preferredHand;
        float f3 = class_7462.getLerpedPitch(f);
        float f4 = MathHelper.lerp((float)f, (float)class_7462.lastRenderPitch, (float)class_7462.renderPitch);
        float f5 = MathHelper.lerp((float)f, (float)class_7462.lastRenderYaw, (float)class_7462.renderYaw);
        float f6 = (class_7462.getYaw(f) - f5) % 360.0f;
        class_45872.multiply(RotationAxis.POSITIVE_X.rotationDegrees((class_7462.getPitch(f) - f4) * 0.1f));
        class_45872.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f6 * 0.1f));
        boolean bl = true;
        boolean bl2 = true;
        ItemStack class_17992 = class_7462.getMainHandStack();
        ItemStack class_17993 = class_7462.getOffHandStack();
        if (class_17992.isOf(Items.BOW) || class_17993.isOf(Items.BOW) || class_17992.isOf(Items.CROSSBOW) || class_17993.isOf(Items.CROSSBOW)) {
            if (class_7462.isUsingItem()) {
                ItemStack class_17994 = class_7462.getActiveItem();
                Hand class_12683 = class_7462.getActiveHand();
                if (class_17994.isOf(Items.BOW) || class_17994.isOf(Items.CROSSBOW)) {
                    bl = class_12683 == Hand.MAIN_HAND;
                    bl2 = !bl;
                } else if (class_12683 == Hand.MAIN_HAND && HeldItemRendererMixin.rockstar$isChargedCrossbow(class_17993)) {
                    bl2 = false;
                }
            } else if (HeldItemRendererMixin.rockstar$isChargedCrossbow(class_17992)) {
                bl2 = false;
            }
        }
        if (bl) {
            float f7 = class_12682 == Hand.MAIN_HAND ? f2 : 0.0f;
            float f8 = 1.0f - MathHelper.lerp((float)f, (float)this.prevEquipProgressMainHand, (float)this.equipProgressMainHand);
            this.renderFirstPersonItem((AbstractClientPlayerEntity)class_7462, f, f3, Hand.MAIN_HAND, f7, this.mainHand, f8, class_45872, (VertexConsumerProvider)class_45982, n);
        }
        if (bl2) {
            float f9 = class_12682 == Hand.OFF_HAND ? f2 : 0.0f;
            float f10 = 1.0f - MathHelper.lerp((float)f, (float)this.prevEquipProgressOffHand, (float)this.equipProgressOffHand);
            this.renderFirstPersonItem((AbstractClientPlayerEntity)class_7462, f, f3, Hand.OFF_HAND, f9, this.offHand, f10, class_45872, (VertexConsumerProvider)class_45982, n);
        }
        class_45982.draw();
        HeldItemRenderCapture.renderCaptured();
        HeldItemRenderCapture.endCapture();
    }

    @Inject(method={"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"}, at={@At(value="RETURN")})
    private void rockstar$detectHijackedHandRender(float f, MatrixStack class_45872, VertexConsumerProvider.Immediate class_45982, ClientPlayerEntity class_7462, int n, CallbackInfo callbackInfo) {
        HeldItemRenderCapture.renderCaptured();
        HeldItemRenderCapture.endCapture();
        if (rockstar$tookOverHandRender) {
            return;
        }
        rockstar$handRenderHijacked = !rockstar$firstPersonItemRendered;
    }

    @Unique
    private static boolean rockstar$isChargedCrossbow(ItemStack class_17992) {
        return class_17992.isOf(Items.CROSSBOW) && CrossbowItem.isCharged((ItemStack)class_17992);
    }

    @Unique
    private static boolean rockstar$isHeldInFirstPerson(LivingEntity class_13092, ModelTransformationMode DeathMessageType) {
        return class_13092 instanceof ClientPlayerEntity && (DeathMessageType == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND || DeathMessageType == ModelTransformationMode.FIRST_PERSON_LEFT_HAND);
    }

    @Unique
    private boolean rockstar$needsHandRenderEvent() {
        ModuleRegistry moduleRegistry = RockstarClient.create().getModuleRegistry();
        if (moduleRegistry == null) {
            return false;
        }
        ViewModel viewModel = moduleRegistry.getModule(ViewModel.class);
        if (viewModel != null && viewModel.isEnabled()) {
            return true;
        }
        SwingAnimation swingAnimation = moduleRegistry.getModule(SwingAnimation.class);
        return swingAnimation != null && swingAnimation.isEnabled() && swingAnimation.isSwingItem(this.mainHand);
    }
}
