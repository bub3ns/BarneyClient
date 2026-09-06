/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.PlayerEntity
 *  net.minecraft.SlotActionType
 *  net.minecraft.Slot
 *  net.minecraft.DefaultedList
 *  net.minecraft.Text
 *  net.minecraft.DrawContext
 *  net.minecraft.MathHelper
 *  net.minecraft.InputUtil
 *  net.minecraft.ButtonWidget
 *  net.minecraft.HandledScreen
 *  net.minecraft.AnvilScreen
 *  net.minecraft.CraftingScreen
 *  net.minecraft.CreativeInventoryScreen
 *  net.minecraft.InventoryScreen
 *  net.minecraft.StringVisitable
 *  org.lwjgl.glfw.GLFW
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.mixin.accessors.ScreenAccessor;
import moscow.rockstar.modules.player.inventory.InventoryUtils;
import moscow.rockstar.modules.visuals.camera.Beautifully;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.text.StringVisitable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyrock.events.render.ScreenRenderEvent;
import pyrock.events.window.ContainerClickEvent;
import pyrock.events.window.ContainerReleaseEvent;
import pyrock.utility.render.CustomDrawContext;

@Mixin(value={HandledScreen.class})
public abstract class HandledScreenMixin
implements ClientAccess {
    @Unique
    private Timer timer;
    @Unique
    private long rockstar$openTime;
    @Unique
    private boolean rockstar$scaled;
    @Unique
    private float rockstar$progress;
    @Shadow
    protected int x;
    @Shadow
    protected int y;
    @Shadow
    protected int backgroundWidth;
    @Shadow
    protected int backgroundHeight;

    @Unique
    private Timer rockstar$timer() {
        if (this.timer == null) {
            this.timer = new Timer();
        }
        return this.timer;
    }

    @Shadow
    protected abstract boolean isPointOverSlot(Slot var1, double var2, double var4);

    @Shadow
    protected abstract void onMouseClick(Slot var1, int var2, int var3, SlotActionType var4);

    @Inject(method={"init"}, at={@At(value="HEAD")})
    private void rockstar$startOpenAnimation(CallbackInfo callbackInfo) {
        this.rockstar$openTime = System.currentTimeMillis();
    }

    @Inject(method={"renderBackground"}, at={@At(value="HEAD")})
    private void rockstar$beginOpenAnimation(DrawContext ServerConfigException, int n, int n2, float f, CallbackInfo callbackInfo) {
        this.rockstar$scaled = false;
        if (!Beautifully.isInventoryAnimationEnabled()) {
            return;
        }
        float f2 = (float)(System.currentTimeMillis() - this.rockstar$openTime) / 200.0f;
        if (f2 >= 1.0f || f2 < 0.0f) {
            return;
        }
        this.rockstar$progress = Easing.easeOutBackSoft.ease(f2, 0.0f, 1.0f, 1.0f);
        this.rockstar$scaled = true;
        ServerConfigException.draw();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)MathHelper.clamp((float)(f2 * 2.0f), (float)0.0f, (float)1.0f));
    }

    @Inject(method={"renderBackground"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screen/ingame/HandledScreen;renderInGameBackground(Lnet/minecraft/client/gui/DrawContext;)V", shift=At.Shift.AFTER)})
    private void rockstar$pushOpenAnimation(DrawContext ServerConfigException, int n, int n2, float f, CallbackInfo callbackInfo) {
        if (!this.rockstar$scaled) {
            return;
        }
        float f2 = (float)this.x + (float)this.backgroundWidth / 2.0f;
        float f3 = (float)this.y + (float)this.backgroundHeight / 2.0f;
        float f4 = 0.88f + 0.12f * this.rockstar$progress;
        ServerConfigException.getMatrices().push();
        ServerConfigException.getMatrices().translate(f2, f3 + (1.0f - this.rockstar$progress) * 10.0f, 0.0f);
        ServerConfigException.getMatrices().scale(f4, f4, 1.0f);
        ServerConfigException.getMatrices().translate(-f2, -f3, 0.0f);
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/util/math/MatrixStack;pop()V", shift=At.Shift.AFTER)})
    private void rockstar$popOpenAnimation(DrawContext ServerConfigException, int n, int n2, float f, CallbackInfo callbackInfo) {
        if (!this.rockstar$scaled) {
            return;
        }
        this.rockstar$scaled = false;
        ServerConfigException.draw();
        ServerConfigException.getMatrices().pop();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void init(CallbackInfo callbackInfo) {
        if (!(RockstarClient.create().isPanicMode() || HandledScreenMixin.minecraftClient.currentScreen instanceof InventoryScreen || HandledScreenMixin.minecraftClient.currentScreen instanceof CreativeInventoryScreen || HandledScreenMixin.minecraftClient.currentScreen instanceof CraftingScreen || HandledScreenMixin.minecraftClient.currentScreen instanceof AnvilScreen || HandledScreenMixin.minecraftClient.currentScreen.getTitle().getString().toLowerCase().contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d\u044b") || HandledScreenMixin.minecraftClient.currentScreen.getTitle().getString().toLowerCase().contains("\u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435"))) {
            Text class_25612 = Text.of((String)Localization.translate("inventory.button.move"));
            int n = HandledScreenMixin.minecraftClient.textRenderer.getWidth((StringVisitable)class_25612) + 20;
            int n2 = 80;
            int n3 = 200;
            int n4 = Math.max(n2, Math.min(n3, n));
            ButtonWidget class_41853 = ButtonWidget.builder((Text)class_25612, class_41852 -> this.stealItems()).dimensions(this.x + this.backgroundWidth / 2 - n4 / 2, this.y - 20, n4, 18).build();
            ((ScreenAccessor)((Object)this)).invokeAddDrawableChild(class_41853);
            Text class_25613 = Text.of((String)Localization.translate("inventory.button.steal"));
            int n5 = HandledScreenMixin.minecraftClient.textRenderer.getWidth((StringVisitable)class_25613) + 20;
            int n6 = 80;
            int n7 = 200;
            int n8 = Math.max(n6, Math.min(n7, n5));
            ButtonWidget class_41854 = ButtonWidget.builder((Text)class_25613, class_41852 -> this.moveItems()).dimensions(this.x + this.backgroundWidth / 2 - n8 / 2, this.y - 40, n8, 18).build();
            ((ScreenAccessor)((Object)this)).invokeAddDrawableChild(class_41854);
        }
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void onRender(DrawContext ServerConfigException, int n, int n2, float f, CallbackInfo callbackInfo) {
        CustomDrawContext customDrawContext = CustomDrawContext.of(ServerConfigException);
        RockstarClient.create().getEventBus().post(new ScreenRenderEvent(customDrawContext, f));
        DefaultedList<Slot> class_23712 = HandledScreenMixin.minecraftClient.player.currentScreenHandler.slots;
        for (Slot class_17352 : class_23712) {
            InventoryUtils inventoryUtils = RockstarClient.create().getModuleRegistry().getModule(InventoryUtils.class);
            if (!this.isPointOverSlot(class_17352, n, n2) || !class_17352.isEnabled() || !inventoryUtils.isEnabled() || !inventoryUtils.getItemScrollerOption().isSelected() || !this.rockstar$timer().hasElapsed((long)inventoryUtils.getScrollDelaySetting().getValue()) || !InputUtil.isKeyPressed((long)minecraftClient.getWindow().getHandle(), (int)340) || GLFW.glfwGetMouseButton((long)minecraftClient.getWindow().getHandle(), (int)0) != 1) continue;
            this.onMouseClick(class_17352, class_17352.id, 0, SlotActionType.QUICK_MOVE);
            this.rockstar$timer().reset();
        }
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")})
    private void onMouseClick(double d, double d2, int n, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        RockstarClient.create().getEventBus().post(new ContainerClickEvent((float)d, (float)d2, n));
    }

    @Inject(method={"mouseReleased"}, at={@At(value="HEAD")})
    public void mouseReleased(double d, double d2, int n, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        RockstarClient.create().getEventBus().post(new ContainerReleaseEvent((float)d, (float)d2, n));
    }

    @Unique
    private void moveItems() {
        if (HandledScreenMixin.minecraftClient.player != null && HandledScreenMixin.minecraftClient.interactionManager != null) {
            int n = HandledScreenMixin.minecraftClient.player.currentScreenHandler.slots.size() - 36;
            int n2 = HandledScreenMixin.minecraftClient.player.currentScreenHandler.slots.size() - 1;
            for (int i = n; i <= n2; ++i) {
                Slot class_17352 = HandledScreenMixin.minecraftClient.player.currentScreenHandler.getSlot(i);
                if (class_17352 == null) continue;
                HandledScreenMixin.minecraftClient.interactionManager.clickSlot(HandledScreenMixin.minecraftClient.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)HandledScreenMixin.minecraftClient.player);
            }
        }
    }

    @Unique
    private void stealItems() {
        if (HandledScreenMixin.minecraftClient.player != null && HandledScreenMixin.minecraftClient.interactionManager != null) {
            int n = HandledScreenMixin.minecraftClient.player.currentScreenHandler.slots.size() - 36;
            for (int i = 0; i < n; ++i) {
                Slot class_17352 = HandledScreenMixin.minecraftClient.player.currentScreenHandler.getSlot(i);
                if (class_17352 == null) continue;
                HandledScreenMixin.minecraftClient.interactionManager.clickSlot(HandledScreenMixin.minecraftClient.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)HandledScreenMixin.minecraftClient.player);
            }
        }
    }
}
