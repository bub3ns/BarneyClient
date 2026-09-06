/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ScoreboardObjective
 *  net.minecraft.Scoreboard
 *  net.minecraft.Identifier
 *  net.minecraft.InGameHud
 *  net.minecraft.DrawContext
 *  net.minecraft.ChatHud
 *  net.minecraft.MathHelper
 *  net.minecraft.PlayerListHud
 *  net.minecraft.ClientPlayerEntity
 *  net.minecraft.ScoreboardDisplaySlot
 *  net.minecraft.RenderTickCounter
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.camera.Beautifully;
import moscow.rockstar.modules.visuals.hud.CustomHotbarHud;
import moscow.rockstar.modules.visuals.hud.EffectsHud;
import moscow.rockstar.modules.visuals.overlay.Removals;
import moscow.rockstar.render.batch.OverlayBatchBuilder;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.diagnostics.DrawCallCounter;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.text.TextCaptureController;
import moscow.rockstar.render.target.RenderTargetManager;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.color.ColorPickerScreen;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.hud.HudElementRegistry;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import pyrock.events.render.HudRenderEvent;
import pyrock.events.render.PostHudRenderEvent;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

@Mixin(value={InGameHud.class})
public class InGameHudMixin
implements ClientAccess {
    @Shadow
    @Final
    private PlayerListHud playerListHud;
    @Unique
    private Animation rockstar$tabAnimation;
    @Unique
    private boolean rockstar$mainHudShifted;
    @Unique
    private boolean rockstar$expLevelShifted;

    @Unique
    private Animation rockstar$tabAnimation() {
        if (this.rockstar$tabAnimation == null) {
            this.rockstar$tabAnimation = new Animation(200L, Easing.easeOutCubic);
        }
        return this.rockstar$tabAnimation;
    }

    @Inject(method={"renderPlayerList"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$animatePlayerList(DrawContext ServerConfigException, RenderTickCounter class_97792, CallbackInfo callbackInfo) {
        if (!Beautifully.isTabAnimationEnabled()) {
            return;
        }
        if (InGameHudMixin.minecraftClient.world == null || InGameHudMixin.minecraftClient.player == null || InGameHudMixin.minecraftClient.player.networkHandler == null) {
            return;
        }
        Scoreboard VehicleMoveS2CPacket = InGameHudMixin.minecraftClient.world.getScoreboard();
        ScoreboardObjective class_2662 = VehicleMoveS2CPacket.getObjectiveForSlot(ScoreboardDisplaySlot.LIST);
        boolean bl = InGameHudMixin.minecraftClient.options.playerListKey.isPressed() && (!minecraftClient.isInSingleplayer() || InGameHudMixin.minecraftClient.player.networkHandler.getListedPlayerListEntries().size() > 1 || class_2662 != null);
        callbackInfo.cancel();
        Animation animation = this.rockstar$tabAnimation();
        animation.setDuration(200L);
        animation.setEasing(bl ? Easing.easeOutBackSoft : Easing.easeInCubic);
        float f = animation.update(bl ? 1.0f : 0.0f);
        this.playerListHud.setVisible(bl);
        if (f <= 0.005f) {
            return;
        }
        float f2 = MathHelper.clamp((float)f, (float)0.0f, (float)1.0f);
        float f3 = ServerConfigException.getScaledWindowWidth();
        float f4 = 0.96f + 0.04f * f;
        ServerConfigException.draw();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f2);
        ServerConfigException.getMatrices().push();
        ServerConfigException.getMatrices().translate(f3 / 2.0f, 0.0f, 0.0f);
        ServerConfigException.getMatrices().scale(f4, f4, 1.0f);
        ServerConfigException.getMatrices().translate(-f3 / 2.0f, (f - 1.0f) * 10.0f, 0.0f);
        this.playerListHud.render(ServerConfigException, ServerConfigException.getScaledWindowWidth(), VehicleMoveS2CPacket, class_2662);
        ServerConfigException.draw();
        ServerConfigException.getMatrices().pop();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    @Redirect(method={"clear"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V"))
    private void rockstar$keepChatHistory(ChatHud Entry, boolean bl) {
        if (Beautifully.isChatHistoryEnabled()) {
            return;
        }
        Entry.clear(bl);
    }

    @Inject(method={"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderScoreboardSidebarHook(DrawContext ServerConfigException, ScoreboardObjective class_2662, CallbackInfo callbackInfo) {
        Removals removals;
        if (class_2662.getDisplayName().getString().contains("\u0410\u043d\u0430\u0440\u0445\u0438\u044f") && (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) || ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY))) {
            try {
                ServerDetector.defaultServerIndex = Integer.parseInt(class_2662.getDisplayName().getString().split("-")[1].trim());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (class_2662.getDisplayName().getString().contains("\u0413\u0440\u0438\u0444\u0435\u0440\u0441\u043a\u0438\u0439") && ServerDetector.griefServerDetected) {
            try {
                ServerDetector.griefServerIndex = Integer.parseInt(class_2662.getDisplayName().getString().split("-")[1].trim());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (class_2662.getDisplayName().getString().contains("\u0413\u0420\u0418\u0424") && ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD)) {
            try {
                ServerDetector.classicServerIndex = Integer.parseInt(class_2662.getDisplayName().getString().split("#")[1].trim());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if ((removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class)).isEnabled() && removals.getScoreboard().isSelected()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"renderPortalOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderPortalOverlayHook(DrawContext ServerConfigException, float f, CallbackInfo callbackInfo) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.isEnabled() && removals.getPortal().isSelected()) {
            callbackInfo.cancel();
        }
    }

    @ModifyArgs(method={"renderMiscOverlays"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/InGameHud;renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V", ordinal=0))
    private void onRenderPumpkinOverlay(Args args) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.isEnabled() && removals.getPumpkin().isSelected()) {
            args.set(2, (Object)Float.valueOf(0.0f));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"render"}, at={@At(value="HEAD")})
    public void triggerPreHudRenderEvent(DrawContext ServerConfigException, RenderTickCounter class_97792, CallbackInfo callbackInfo) {
        TextCaptureController.applyCapturePhase(0);
        CustomDrawContext customDrawContext = CustomDrawContext.of(ServerConfigException);
        DrawCallCounter.beginCounting();
        try {
            RenderTargetManager.render();
            RockstarClient.create().getEventBus().post(new PreHudRenderEvent(customDrawContext, class_97792.getTickDelta(false)));
        }
        finally {
            DrawCallCounter.endCounting();
        }
    }

    @Inject(method={"render"}, at={@At(value="RETURN")})
    public void triggerPostHudRenderEvent(DrawContext ServerConfigException, RenderTickCounter class_97792, CallbackInfo callbackInfo) {
        CustomDrawContext customDrawContext = CustomDrawContext.of(ServerConfigException);
        RockstarClient.create().getEventBus().post(new PostHudRenderEvent(customDrawContext, class_97792.getTickDelta(false)));
        ServerConfigException.draw();
        WidgetBatchRenderer.flushCurrentBatch();
        TextCaptureController.applyCapturePhase(1);
    }

    private void rockstar$renderDrawCalls(CustomDrawContext customDrawContext) {
        FontMetrics fontMetrics = Font.MEDIUM.metrics(8.0f);
        String string = "Draw calls: " + DrawCallCounter.getCount();
        float f = 4.0f;
        float f2 = fontMetrics.measureText(string) + f * 2.0f;
        float f3 = fontMetrics.getFontMetricsFloat() + f * 2.0f;
        float f4 = 4.0f;
        float f5 = 4.0f;
        DrawCallCounter.beginCounting();
        try {
            OverlayBatchBuilder overlayBatchBuilder = new OverlayBatchBuilder(fontMetrics.getFontRenderer(), 3.0f);
            overlayBatchBuilder.queueColoredOverlay(customDrawContext.getMatrices().peek().getPositionMatrix(), f4, f5, f2, f3, new ColorRGBA(12.0f, 12.0f, 12.0f, 180.0f));
            overlayBatchBuilder.queueText(customDrawContext.getMatrices().peek().getPositionMatrix(), string, fontMetrics.getFontScale(), f4 + f, f5 + f, 0.0f, ColorRGBA.WHITE.getRGB());
            overlayBatchBuilder.flush();
        }
        finally {
            DrawCallCounter.endCounting();
        }
    }

    @Inject(method={"renderMainHud"}, at={@At(value="HEAD")})
    private void rockstar$shiftVanillaMainHud(DrawContext ServerConfigException, RenderTickCounter class_97792, CallbackInfo callbackInfo) {
        float f = CustomHotbarHud.verticalOffset();
        if (f == 0.0f) {
            return;
        }
        ServerConfigException.getMatrices().push();
        ServerConfigException.getMatrices().translate(0.0f, -f, 0.0f);
        this.rockstar$mainHudShifted = true;
    }

    @Inject(method={"renderMainHud"}, at={@At(value="TAIL")})
    private void triggerHudRenderEvent(DrawContext ServerConfigException, RenderTickCounter class_97792, CallbackInfo callbackInfo) {
        if (this.rockstar$mainHudShifted) {
            this.rockstar$mainHudShifted = false;
            ServerConfigException.getMatrices().pop();
        }
        if (RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }
        CustomDrawContext customDrawContext = CustomDrawContext.of(ServerConfigException);
        ColorPickerScreen.transientNodesInitialized = false;
        RockstarClient.create().getEventBus().post(new HudRenderEvent(customDrawContext, class_97792.getTickDelta(false)));
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiIiIIIii#I ()Lrockstar/ilIlil/IiIiIiIIi; - a plain getter for the
     * HUD manager's own CustomHotbarHud field. HudElementRegistry has neither that field nor the
     * getter yet, so this resolves the element out of the live element list exactly the way
     * CustomHotbarHud.verticalOffset() already does in this tree. Swap it for the registry getter
     * once the registry grows one.
     */
    @Unique
    private CustomHotbarHud rockstar$customHotbarHud() {
        HudElementRegistry hudElementRegistry = RockstarClient.create().getHudElementRegistry();
        if (hudElementRegistry == null) {
            return null;
        }
        for (HudElement hudElement : hudElementRegistry.elements()) {
            if (!(hudElement instanceof CustomHotbarHud)) continue;
            return (CustomHotbarHud)hudElement;
        }
        return null;
    }

    @Inject(method={"renderHotbar"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$hideVanillaHotbar(DrawContext ServerConfigException, RenderTickCounter class_97792, CallbackInfo callbackInfo) {
        if (RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }
        if (RockstarClient.create().getHudElementRegistry() == null) {
            return;
        }
        CustomHotbarHud customHotbarHud = this.rockstar$customHotbarHud();
        if (customHotbarHud != null && customHotbarHud.isShowing() && customHotbarHud.show()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"renderStatusBars"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$hideVanillaStatusBars(DrawContext ServerConfigException, CallbackInfo callbackInfo) {
        if (RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }
        if (RockstarClient.create().getHudElementRegistry() == null) {
            return;
        }
        CustomHotbarHud customHotbarHud = this.rockstar$customHotbarHud();
        if (customHotbarHud != null && customHotbarHud.isShowing() && customHotbarHud.show()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"renderExperienceBar"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$hideVanillaExpBar(DrawContext ServerConfigException, int n, CallbackInfo callbackInfo) {
        if (RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }
        if (RockstarClient.create().getHudElementRegistry() == null) {
            return;
        }
        CustomHotbarHud customHotbarHud = this.rockstar$customHotbarHud();
        if (customHotbarHud != null && customHotbarHud.isShowing() && customHotbarHud.show()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"renderExperienceLevel"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$hideVanillaExpLevel(DrawContext ServerConfigException, RenderTickCounter class_97792, CallbackInfo callbackInfo) {
        if (RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }
        if (RockstarClient.create().getHudElementRegistry() == null) {
            return;
        }
        CustomHotbarHud customHotbarHud = this.rockstar$customHotbarHud();
        if (customHotbarHud != null && customHotbarHud.isShowing() && customHotbarHud.show()) {
            callbackInfo.cancel();
            return;
        }
        float f = CustomHotbarHud.verticalOffset();
        if (f == 0.0f) {
            return;
        }
        ServerConfigException.getMatrices().push();
        ServerConfigException.getMatrices().translate(0.0f, -f, 0.0f);
        this.rockstar$expLevelShifted = true;
    }

    @Inject(method={"renderExperienceLevel"}, at={@At(value="RETURN")})
    private void rockstar$unshiftVanillaExpLevel(DrawContext ServerConfigException, RenderTickCounter class_97792, CallbackInfo callbackInfo) {
        if (!this.rockstar$expLevelShifted) {
            return;
        }
        this.rockstar$expLevelShifted = false;
        ServerConfigException.getMatrices().pop();
    }

    @Inject(method={"renderStatusEffectOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$hideStatusEffects(DrawContext ServerConfigException, RenderTickCounter class_97792, CallbackInfo callbackInfo) {
        if (RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }
        if (RockstarClient.create().getHudElementRegistry() == null) {
            return;
        }
        boolean bl = RockstarClient.create().getHudElementRegistry().elements().stream()
                .filter(hudElement -> hudElement instanceof EffectsHud)
                .anyMatch(hudElement -> hudElement.isShowing() && hudElement.show());
        if (bl) {
            callbackInfo.cancel();
        }
    }

    // ORIGINAL: @Inject(method = "renderMiscOverlays", at = @At("TAIL")) private void
    // rockstar$renderFakeFrozenOverlay(DrawContext, RenderTickCounter, CallbackInfo) reads
    // globals.client.snowball.FakeFrozenTicksAccess#rockstar$getFakeFrozenTicks() off the local
    // player and replays the vanilla powder-snow overlay (field_27960 / hud/frost_overlay) through
    // InGameHudAccessor#rockstar$renderOverlay at
    // Math.min(1, ticks / player.getMinFreezeDamageTicks()). Both the globals package and
    // InGameHudAccessor are absent from this tree, so the member is omitted.
}
