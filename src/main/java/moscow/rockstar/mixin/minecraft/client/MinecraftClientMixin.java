/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Identifier
 *  net.minecraft.Screen
 *  net.minecraft.RunArgs
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client;

import moscow.rockstar.combat.rotation.AimAdjustment;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.targeting.TargetActionQueue;
import moscow.rockstar.mixin.accessors.ItemCooldownEntryAccessor;
import moscow.rockstar.mixin.accessors.ItemCooldownManagerAccessor;
import moscow.rockstar.modules.ModuleCategoryInfo;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.player.movement.NoDelay;
import moscow.rockstar.render.hand.HandSwingSettings;
import moscow.rockstar.render.text.TextCaptureController;
import moscow.rockstar.render.texture.TextureAnimationAtlas;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.ItemNotification;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.events.game.GameTickEvent;

@Mixin(value={MinecraftClient.class})
public class MinecraftClientMixin {
    @Shadow
    private int itemUseCooldown;
    @Unique
    private long rockstar$lastUseCooldownAlertMs;
    @Unique
    private Item rockstar$lastUseCooldownAlertItem;

    @Inject(method={"onResolutionChanged"}, at={@At(value="RETURN")})
    public void onResolutionChanged(CallbackInfo callbackInfo) {
        UiNode.invalidateLayout();
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    public void tick(CallbackInfo callbackInfo) {
        TargetActionQueue.runDeferredAction();
        RockstarClient.create().getEventBus().post(new GameTickEvent());
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/GameRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;Z)V", shift=At.Shift.BEFORE)})
    private void rockstar$beginCaptureFrame(boolean bl, CallbackInfo callbackInfo) {
        TextCaptureController.beginCaptureFrame();
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/GameRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;Z)V", shift=At.Shift.AFTER)})
    private void rockstar$applyRemainingPatches(boolean bl, CallbackInfo callbackInfo) {
        TextCaptureController.applyCapturePhase(2);
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/util/Window;swapBuffers(Lnet/minecraft/client/util/tracy/TracyFrameCapturer;)V", shift=At.Shift.BEFORE)})
    private void rockstar$finishCaptureFrame(boolean bl, CallbackInfo callbackInfo) {
        TextCaptureController.finishCaptureFrame();
    }

    @Inject(method={"<init>"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/MinecraftClient;onResolutionChanged()V")})
    public void initializeClient(RunArgs class_5422, CallbackInfo callbackInfo) {
        RockstarClient.create().initializeClient();
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    public void endInitialize(RunArgs class_5422, CallbackInfo callbackInfo) {
        TextureAnimationAtlas textureAnimationAtlas = TextureAnimationAtlas.getOrCreateAtlas(16, 16);
        textureAnimationAtlas.loadAnimationArchive(RockstarClient.resourceId("penises/combat.penis"));
        textureAnimationAtlas.loadAnimationArchive(RockstarClient.resourceId("penises/movement.penis"));
        textureAnimationAtlas.loadAnimationArchive(RockstarClient.resourceId("penises/visuals.penis"));
        textureAnimationAtlas.loadAnimationArchive(RockstarClient.resourceId("penises/player.penis"));
        textureAnimationAtlas.loadAnimationArchive(RockstarClient.resourceId("penises/other.penis"));
        textureAnimationAtlas.loadAnimationArchive(RockstarClient.resourceId("penises/search.penis"));
        textureAnimationAtlas.buildAtlas();
        TextureAnimationAtlas textureAnimationAtlas2 = TextureAnimationAtlas.getOrCreateAtlas(12, 12);
        textureAnimationAtlas2.loadAnimationArchive(RockstarClient.resourceId("penises/check_enable.penis"));
        textureAnimationAtlas2.loadAnimationArchive(RockstarClient.resourceId("penises/check_disable.penis"));
        textureAnimationAtlas2.buildAtlas();
        for (ModuleCategoryInfo moduleCategoryInfo : ModuleCategoryInfo.values()) {
            try {
                moduleCategoryInfo.setCategorySwingSettings(new HandSwingSettings());
            }
            catch (RuntimeException runtimeException) {
                // empty catch block
            }
        }
    }

    @Inject(method={"cleanUpAfterCrash"}, at={@At(value="HEAD")})
    private void saveConfigAfterCrash(CallbackInfo callbackInfo) {
        RockstarClient rockstarClient = RockstarClient.INSTANCE;
        if (rockstarClient.isPanicMode()) {
            return;
        }
        moscow.rockstar.modules.config.ModuleConfigurationStore.saveConfiguration();
    }

    @Inject(method={"stop"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/MinecraftClient;close()V", shift=At.Shift.AFTER)})
    public void shutdownClient(CallbackInfo callbackInfo) {
        TextCaptureController.shutdown();
        RockstarClient.create().shutdownClient();
    }

    @Inject(method={"doItemUse"}, at={@At(value="TAIL")})
    private void resetItemUseCooldown(CallbackInfo callbackInfo) {
        NoDelay noDelay = RockstarClient.create().getModuleRegistry().getModule(NoDelay.class);
        if (noDelay.isEnabled() && noDelay.getRightClickSetting().isEnabled()) {
            this.itemUseCooldown = noDelay.getUseDelay();
        }
    }

    @Inject(method={"doItemUse"}, at={@At(value="HEAD")})
    private void rockstar$alertItemUseCooldown(CallbackInfo callbackInfo) {
        MinecraftClient client = (MinecraftClient)((Object)this);
        if (client.player == null) {
            return;
        }
        ItemStack class_17992 = client.player.getMainHandStack();
        if (class_17992.isEmpty() || !client.player.getItemCooldownManager().isCoolingDown(class_17992)) {
            class_17992 = client.player.getOffHandStack();
        }
        if (class_17992.isEmpty() || !client.player.getItemCooldownManager().isCoolingDown(class_17992)) {
            return;
        }
        float f = this.rockstar$getRemainingCooldownSeconds(client, class_17992);
        if (f <= 0.01f) {
            return;
        }
        long l = System.currentTimeMillis();
        Item class_17922 = class_17992.getItem();
        if (class_17922 == this.rockstar$lastUseCooldownAlertItem && l - this.rockstar$lastUseCooldownAlertMs < 600L) {
            return;
        }
        this.rockstar$lastUseCooldownAlertMs = l;
        this.rockstar$lastUseCooldownAlertItem = class_17922;
        RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translateFormatted("alerts.cooldown", Float.valueOf(f)), class_17992));
    }

    @Unique
    private float rockstar$getRemainingCooldownSeconds(MinecraftClient client, ItemStack class_17992) {
        ItemCooldownManagerAccessor itemCooldownManagerAccessor = (ItemCooldownManagerAccessor)client.player.getItemCooldownManager();
        Identifier class_29602 = itemCooldownManagerAccessor.rockstar$getGroup(class_17992);
        Object object = itemCooldownManagerAccessor.rockstar$getEntries().get(class_29602);
        if (object == null) {
            return 0.0f;
        }
        int n = ((ItemCooldownEntryAccessor)object).rockstar$getEndTick() - itemCooldownManagerAccessor.rockstar$getTick();
        return Math.max(0.0f, (float)n / 20.0f);
    }
}
