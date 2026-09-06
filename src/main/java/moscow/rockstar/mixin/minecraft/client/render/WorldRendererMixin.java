/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Profilers
 *  net.minecraft.BlockRenderView
 *  net.minecraft.BlockState
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.GameRenderer
 *  net.minecraft.WorldRenderer
 *  net.minecraft.RenderTickCounter
 *  net.minecraft.ObjectAllocator
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.render;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.visuals.world.Ambience;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.world.navigation.NavigationService;
import moscow.rockstar.render.world.CameraClipManager;
import moscow.rockstar.render.world.DynamicLightGrid;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.BlockRenderView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyrock.events.render.Render3DEvent;

@Mixin(value={WorldRenderer.class})
public abstract class WorldRendererMixin
implements ClientAccess {
    private MatrixStack renderEventStack;

    private MatrixStack rockstar$renderEventStack() {
        if (this.renderEventStack == null) {
            this.renderEventStack = new MatrixStack();
        }
        return this.renderEventStack;
    }

    @Inject(method={"render"}, at={@At(value="RETURN")})
    private void render(ObjectAllocator class_99222, RenderTickCounter class_97792, boolean bl, Camera class_41842, GameRenderer ThirdParty, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo callbackInfo) {
        Profilers.get().swap(RockstarClient.RESOURCE_NAMESPACE + "_renderWorld");
        this.applyWetWorld(class_41842, matrix4f, matrix4f2, class_97792.getTickDelta(false));
        this.applySaturation();
        MatrixStack class_45872 = this.rockstar$renderEventStack();
        class_45872.push();
        class_45872.multiplyPositionMatrix(matrix4f);
        RockstarClient.create().getEventBus().post(new Render3DEvent(class_45872, matrix4f, matrix4f2, class_41842, class_97792.getTickDelta(false)));
        class_45872.pop();
    }

    @Inject(method={"getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I"}, at={@At(value="RETURN")}, cancellable=true)
    private static void applyDynamicLight(BlockRenderView class_19202, BlockState class_26802, BlockPos adminsky, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(CameraClipManager.preserveLightLevel(
            adminsky,
            DynamicLightGrid.apply(adminsky, callbackInfoReturnable.getReturnValueI())));
    }

    private void applyWetWorld(Camera class_41842, Matrix4f matrix4f, Matrix4f matrix4f2, float f) {
        Ambience ambience = RockstarClient.create().getModuleRegistry().getModule(Ambience.class);
        if (ambience == null || !ambience.isNightModeReady()) {
            return;
        }
        if (ShaderRenderer.wetWorldPostProcessor == null || WorldRendererMixin.minecraftClient.world == null || class_41842 == null || !class_41842.isReady()) {
            return;
        }
        ShaderRenderer.wetWorldPostProcessor.renderPostProcess(ambience.buildReflectionState(matrix4f, matrix4f2, class_41842, f));
    }

    private void applySaturation() {
        Ambience ambience = RockstarClient.create().getModuleRegistry().getModule(Ambience.class);
        if (ambience == null || !ambience.isWetWorldReady()) {
            return;
        }
        if (ShaderRenderer.saturationTextureBatch == null) {
            return;
        }
        Vector3f vector3f = ambience.getFogCameraVector();
        ShaderRenderer.saturationTextureBatch.renderSaturationOverlay(ambience.calculateColorIsolationFactor(), vector3f.x, vector3f.y, vector3f.z, ambience.calculateFogIntensity(), ambience.calculateShaderOpacity(), ambience.calculateReflectionStrength(), ambience.calculateColorIsolationBackground());
    }
}
