/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Text
 *  net.minecraft.DrawContext
 *  net.minecraft.TextFieldWidget
 *  net.minecraft.ChatScreen
 *  net.minecraft.Screen
 *  net.minecraft.ChatInputSuggestor
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
import moscow.rockstar.api.commands.NavigationCommandService;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.hud.HudElementRegistry;
import moscow.rockstar.modules.ModuleRegistry;
import moscow.rockstar.modules.visuals.camera.Beautifully;
import moscow.rockstar.modules.visuals.hand.ViewModel;
import moscow.rockstar.ui.animation.Easing;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyrock.events.game.SendMessageEvent;
import pyrock.events.render.ChatRenderEvent;
import pyrock.events.window.ChatClickEvent;
import pyrock.events.window.ChatKeyPressEvent;
import pyrock.events.window.ChatReleaseEvent;
import pyrock.events.window.ChatScrollEvent;
import pyrock.utility.render.CustomDrawContext;

@Mixin(value={ChatScreen.class})
public class ChatScreenMixin
extends Screen
implements ClientAccess {
    @Shadow
    protected TextFieldWidget chatField;
    @Shadow
    private ChatInputSuggestor chatInputSuggestor;
    @Unique
    private long rockstar$openTime;
    @Unique
    private boolean rockstar$barShifted;

    protected ChatScreenMixin(Text class_25612) {
        super(class_25612);
    }

    @Inject(method={"init"}, at={@At(value="HEAD")})
    private void rockstar$startOpenAnimation(CallbackInfo callbackInfo) {
        this.rockstar$openTime = System.currentTimeMillis();
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/DrawContext;IIIZ)V", shift=At.Shift.AFTER)})
    private void rockstar$pushInputAnimation(DrawContext ServerConfigException, int n, int n2, float f, CallbackInfo callbackInfo) {
        this.rockstar$barShifted = false;
        if (!Beautifully.isChatAnimationEnabled()) {
            return;
        }
        float f2 = (float)(System.currentTimeMillis() - this.rockstar$openTime) / 200.0f;
        if (f2 >= 1.0f || f2 < 0.0f) {
            return;
        }
        float f3 = Easing.easeOutCubic.ease(f2, 0.0f, 1.0f, 1.0f);
        ServerConfigException.draw();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f3);
        ServerConfigException.getMatrices().push();
        ServerConfigException.getMatrices().translate(0.0f, (1.0f - f3) * 16.0f, 0.0f);
        this.rockstar$barShifted = true;
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/util/math/MatrixStack;pop()V", ordinal=0, shift=At.Shift.AFTER)})
    private void rockstar$popInputAnimation(DrawContext ServerConfigException, int n, int n2, float f, CallbackInfo callbackInfo) {
        if (!this.rockstar$barShifted) {
            return;
        }
        this.rockstar$barShifted = false;
        ServerConfigException.draw();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        ServerConfigException.getMatrices().pop();
    }

    @Inject(method={"sendMessage(Ljava/lang/String;Z)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSendMessage(String string, boolean bl, CallbackInfo callbackInfo) {
        NavigationCommandService navigationCommandService = RockstarClient.create().getNavigationCommandService();
        String string2 = navigationCommandService.getCommandPrefix();
        if (!string2.isEmpty() && string.startsWith(string2 + string2)) {
            SendMessageEvent sendMessageEvent = new SendMessageEvent(string.substring(string2.length()));
            RockstarClient.create().getEventBus().post(sendMessageEvent);
            if (!sendMessageEvent.isCancelled()) {
                ChatScreenMixin.minecraftClient.player.networkHandler.sendChatMessage(sendMessageEvent.getMessage());
            }
            ChatScreenMixin.minecraftClient.inGameHud.getChatHud().addToMessageHistory(string);
            callbackInfo.cancel();
            return;
        }
        if (!string2.isEmpty() && string.startsWith(string2)) {
            navigationCommandService.executeCommand(string);
            ChatScreenMixin.minecraftClient.inGameHud.getChatHud().addToMessageHistory(string);
            callbackInfo.cancel();
            return;
        }
        SendMessageEvent sendMessageEvent = new SendMessageEvent(string);
        RockstarClient.create().getEventBus().post(sendMessageEvent);
        if (sendMessageEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"keyPressed(III)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void onTab(int n, int n2, int n3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        // ORIGINAL: an open HUD popup consumes the key before the chat screen sees it - first the
        // Ctrl+Z/Ctrl+Y undo leg, then the general leg (which ESC is excluded from so it still
        // closes the screen). Both were dropped in the remap, leaving popup text fields unusable.
        HudElementRegistry hudElementRegistry = RockstarClient.create().getHudElementRegistry();
        if (hudElementRegistry != null && hudElementRegistry.keyPressed(n, n3)) {
            callbackInfoReturnable.setReturnValue(true);
            return;
        }
        if (n != 256 && hudElementRegistry != null && hudElementRegistry.keyPressed(n, n2, n3)) {
            callbackInfoReturnable.setReturnValue(true);
            return;
        }
        RockstarClient.create().getEventBus().post(new ChatKeyPressEvent(n, n2, n3));
    }

    @Inject(method={"render"}, at={@At(value="RETURN")})
    public void render(DrawContext ServerConfigException, int n, int n2, float f, CallbackInfo callbackInfo) {
        RockstarClient.create().getEventBus().post(new ChatRenderEvent(CustomDrawContext.of(ServerConfigException), f));
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")})
    private void onMouseClick(double d, double d2, int n, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        RockstarClient.create().getEventBus().post(new ChatClickEvent((float)d, (float)d2, n));
    }

    @Inject(method={"mouseScrolled"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseScroll(double d, double d2, double d3, double d4, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        ViewModel viewModel;
        RockstarClient.create().getEventBus().post(new ChatScrollEvent((float)d, (float)d2, (float)d3, (float)d4));
        ModuleRegistry moduleRegistry = RockstarClient.create().getModuleRegistry();
        ViewModel viewModel2 = viewModel = moduleRegistry == null ? null : moduleRegistry.getModule(ViewModel.class);
        if (viewModel != null && viewModel.isChatDragPositionValid((float)d, (float)d2, (float)d4)) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    public boolean mouseReleased(double d, double d2, int n) {
        RockstarClient.create().getEventBus().post(new ChatReleaseEvent((float)d, (float)d2, n));
        return true;
    }
}
