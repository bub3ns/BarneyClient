package moscow.rockstar.render.text;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.hud.NameProtect;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.capture.CaptureShield;
import moscow.rockstar.render.target.RenderTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Coordinates the original capture-bypass framebuffer and the text passes
 * that feed it.  The lifecycle mirrors the original client: prepare the
 * private target at frame start, record clipped regions while the world/HUD
 * is rendered, and copy those regions to the capture-excluded window before
 * the main window swaps.
 */
public final class TextCaptureController {
    private static final CaptureShield CAPTURE_SHIELD = new CaptureShield();
    private static final RenderTarget CAPTURE_TARGET = new RenderTarget(true);
    private static final ThreadLocal<Boolean> ORIGINAL_TEXT_PASS = ThreadLocal.withInitial(() -> false);
    private static final int MAX_CLIP_REGIONS = 256;
    private static final int NO_CLIP_FRAME_LIMIT = 30;
    private static final int[] CLIP_BOUNDS = new int[MAX_CLIP_REGIONS * 4];
    private static final int[] CLIP_KINDS = new int[MAX_CLIP_REGIONS];
    private static final List<QueuedTextPatch> TEXT_PATCHES = new ArrayList<>();
    private static VertexConsumerProvider.Immediate immediateProvider;

    private static int clipCount;
    private static int currentPhase = 2;
    private static int framesWithoutClip;
    private static boolean captureAvailable;
    private static boolean captureTargetActive;
    private static boolean clippedCapture;
    private static boolean screenRendering;
    private static boolean runnableCapture;
    private static boolean warnedUnavailable;
    private static int viewportMismatchWarnings;

    private TextCaptureController() {
    }

    /** Normal text transformation used by the remapped UI and world queues. */
    public static String transform(String text) {
        if (!isCaptureActive() || text == null || text.isEmpty()) {
            return text;
        }
        NameProtect nameProtect = getNameProtect();
        if (nameProtect == null || !nameProtect.containsCapturedName(text)) {
            return text;
        }
        String replaced = nameProtect.replaceProtectedText(text);
        return replaced.equals(text) ? text : replaced;
    }

    /** True only while the original capture pass is intercepting text. */
    public static boolean isReplacementActive() {
        return isCaptureActive() && !isOriginalTextPass();
    }

    /** Availability is the private-window state used by TextVisitFactory. */
    public static boolean isCaptureAvailable() {
        return captureAvailable;
    }

    /** True while a clipped region is being drawn into the private target. */
    public static boolean isCaptureInProgress() {
        return clippedCapture;
    }

    /** True for the normal replacement pass, excluding recursive text calls. */
    public static boolean isCaptureActive() {
        return captureAvailable && captureTargetActive && !clippedCapture && !runnableCapture;
    }

    public static boolean isOriginalTextPass() {
        return ORIGINAL_TEXT_PASS.get();
    }

    /** Shared immediate provider used by the original deferred text passes. */
    public static VertexConsumerProvider.Immediate getImmediateProvider() {
        if (immediateProvider == null) {
            immediateProvider = VertexConsumerProvider.immediate(new BufferAllocator(1536));
        }
        return immediateProvider;
    }

    /** Queues one replacement together with the matrices active at its call site. */
    public static boolean registerPatch(TextCapturePatch patch) {
        if (patch == null || !isCaptureActive() || TEXT_PATCHES.size() >= MAX_CLIP_REGIONS) {
            return false;
        }
        TEXT_PATCHES.add(new QueuedTextPatch(patch, CaptureRenderState.capture()));
        return true;
    }

    /**
     * During an available capture bypass the vanilla text visitor must keep
     * its original input so NameProtect can record the text for the private
     * layer.  This deliberately differs from the recursion guard above.
     */
    public static boolean shouldKeepOriginalText() {
        return captureAvailable;
    }

    public static void renderOriginalText(Runnable renderer) {
        if (renderer == null) {
            return;
        }
        boolean previous = ORIGINAL_TEXT_PASS.get();
        ORIGINAL_TEXT_PASS.set(true);
        try {
            renderer.run();
        } finally {
            ORIGINAL_TEXT_PASS.set(previous);
        }
    }

    /** Runs code while the capture text interception is disabled. */
    public static void runWithoutCapture(Runnable renderer) {
        if (renderer == null) {
            return;
        }
        boolean previous = runnableCapture;
        runnableCapture = true;
        try {
            renderer.run();
        } finally {
            runnableCapture = previous;
        }
    }

    /**
     * Renders the original text into the private framebuffer for one region.
     * The main framebuffer is restored even when the renderer throws, because
     * this method is called from both HUD and world batching paths.
     */
    public static void renderClipped(int[] bounds, Runnable renderer) {
        if (bounds == null || renderer == null || !isCaptureActive() || clipCount >= MAX_CLIP_REGIONS) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer mainFramebuffer = client.getFramebuffer();
        if (mainFramebuffer == null || CAPTURE_TARGET.fbo < 0) {
            return;
        }

        boolean scissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        int[] previousScissor = new int[4];
        GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, previousScissor);
        clippedCapture = true;
        try {
            WidgetBatchRenderer.flushCurrentBatch();
            copyFromMainFramebuffer(bounds, mainFramebuffer);
            CAPTURE_TARGET.beginWrite(false);
            GlStateManager._enableScissorTest();
            GlStateManager._scissorBox(bounds[0], bounds[1], bounds[2], bounds[3]);
            renderOriginalText(renderer);

            int offset = clipCount * 4;
            CLIP_BOUNDS[offset] = bounds[0];
            CLIP_BOUNDS[offset + 1] = bounds[1];
            CLIP_BOUNDS[offset + 2] = bounds[2];
            CLIP_BOUNDS[offset + 3] = bounds[3];
            CLIP_KINDS[clipCount] = screenRendering ? 2 : 1;
            ++clipCount;
        } catch (Throwable throwable) {
            RockstarClient.LOGGER.error("[CaptureBypass] clipped text pass failed", throwable);
        } finally {
            mainFramebuffer.beginWrite(false);
            if (scissorEnabled) {
                GlStateManager._enableScissorTest();
                GlStateManager._scissorBox(previousScissor[0], previousScissor[1], previousScissor[2], previousScissor[3]);
            } else {
                GlStateManager._disableScissorTest();
            }
            clippedCapture = false;
        }
    }

    private static void copyFromMainFramebuffer(int[] bounds, Framebuffer mainFramebuffer) {
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.fbo);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, CAPTURE_TARGET.fbo);
        GL30.glBlitFramebuffer(
            bounds[0], bounds[1], bounds[0] + bounds[2], bounds[1] + bounds[3],
            bounds[0], bounds[1], bounds[0] + bounds[2], bounds[1] + bounds[3],
            GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT,
            GL11.GL_NEAREST
        );
    }

    public static int[] transformBounds(Matrix4f transform, float minX, float minY, float maxX, float maxY) {
        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer framebuffer = client.getFramebuffer();
        if (framebuffer == null || framebuffer.textureWidth <= 0 || framebuffer.textureHeight <= 0) {
            return null;
        }
        if (transform == null) {
            return new int[]{
                (int)Math.floor(minX),
                (int)Math.floor(minY),
                Math.max(0, (int)Math.ceil(maxX - minX)),
                Math.max(0, (int)Math.ceil(maxY - minY))
            };
        }

        Matrix4f modelView = RenderSystem.getModelViewMatrix();
        Matrix4f projection = RenderSystem.getProjectionMatrix();
        float projectedMinX = Float.POSITIVE_INFINITY;
        float projectedMinY = Float.POSITIVE_INFINITY;
        float projectedMaxX = Float.NEGATIVE_INFINITY;
        float projectedMaxY = Float.NEGATIVE_INFINITY;
        for (int corner = 0; corner < 4; ++corner) {
            Vector4f point = new Vector4f(
                (corner & 1) == 0 ? minX : maxX,
                (corner & 2) == 0 ? minY : maxY,
                0.0f,
                1.0f
            );
            transform.transform(point);
            modelView.transform(point);
            projection.transform(point);
            if (point.w <= 1.0E-5f) {
                return null;
            }
            float x = (point.x / point.w * 0.5f + 0.5f) * framebuffer.textureWidth;
            float y = (point.y / point.w * 0.5f + 0.5f) * framebuffer.textureHeight;
            projectedMinX = Math.min(projectedMinX, x);
            projectedMinY = Math.min(projectedMinY, y);
            projectedMaxX = Math.max(projectedMaxX, x);
            projectedMaxY = Math.max(projectedMaxY, y);
        }

        int left = Math.max(0, (int)Math.floor(projectedMinX));
        int bottom = Math.max(0, (int)Math.floor(projectedMinY));
        int right = Math.min(framebuffer.textureWidth, (int)Math.ceil(projectedMaxX));
        int top = Math.min(framebuffer.textureHeight, (int)Math.ceil(projectedMaxY));
        if (GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)) {
            int[] scissor = new int[4];
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissor);
            left = Math.max(left, scissor[0]);
            bottom = Math.max(bottom, scissor[1]);
            right = Math.min(right, scissor[0] + scissor[2]);
            top = Math.min(top, scissor[1] + scissor[3]);
        }
        if (right <= left || top <= bottom) {
            return null;
        }
        return new int[]{left, bottom, right - left, top - bottom};
    }

    public static int[] transformBounds(Matrix4f transform, float x, float y, float width) {
        return transformBounds(transform, x - 1.0f, y - 1.0f, x + width + 2.0f, y + 10.0f);
    }

    public static int[] union(int[] first, int[] second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        int minX = Math.min(first[0], second[0]);
        int minY = Math.min(first[1], second[1]);
        int maxX = Math.max(first[0] + first[2], second[0] + second[2]);
        int maxY = Math.max(first[1] + first[3], second[1] + second[3]);
        return new int[]{minX, minY, maxX - minX, maxY - minY};
    }

    public static void beginCaptureFrame() {
        clipCount = 0;
        TEXT_PATCHES.clear();
        currentPhase = 2;
        captureTargetActive = false;
        clippedCapture = false;
        screenRendering = false;

        NameProtect nameProtect = getNameProtect();
        if (nameProtect != null) {
            nameProtect.clearCapturedNames();
        }
        updateCaptureAvailability();
        if (!captureAvailable) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer mainFramebuffer = client.getFramebuffer();
        if (mainFramebuffer == null || mainFramebuffer.textureWidth <= 0 || mainFramebuffer.textureHeight <= 0) {
            return;
        }
        try {
            CAPTURE_TARGET.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            CAPTURE_TARGET.beginPass(true);
            mainFramebuffer.beginWrite(false);
            captureTargetActive = true;
        } catch (Throwable throwable) {
            RockstarClient.LOGGER.error("[CaptureBypass] unable to prepare private capture layer", throwable);
            captureTargetActive = false;
        }
    }

    private static void updateCaptureAvailability() {
        NameProtect nameProtect = getNameProtect();
        boolean bypassEnabled = nameProtect != null
            && nameProtect.isEnabled()
            && nameProtect.getCaptureBypassSetting().isEnabled();
        MinecraftClient client = MinecraftClient.getInstance();
        boolean available = bypassEnabled
            && !client.getWindow().isFullscreen()
            && !CAPTURE_SHIELD.isUnavailable()
            && (CAPTURE_SHIELD.isCreated() || CAPTURE_SHIELD.ensureAvailable());
        if (bypassEnabled && !available && !warnedUnavailable) {
            warnedUnavailable = true;
            RockstarClient.LOGGER.warn(
                "[CaptureBypass] private layer unavailable: {}",
                client.getWindow().isFullscreen()
                    ? "game fullscreen"
                    : "system cannot exclude window from capture (requires Windows 10 2004+)"
            );
        }
        if (available) {
            warnedUnavailable = false;
        }
        if (available == captureAvailable) {
            return;
        }
        captureAvailable = available;
        if (!available) {
            CAPTURE_SHIELD.hide();
        }
    }

    public static void applyCapturePhase(int phase) {
        if (TEXT_PATCHES.isEmpty()) {
            return;
        }
        List<QueuedTextPatch> patches = new ArrayList<>(TEXT_PATCHES);
        TEXT_PATCHES.clear();
        currentPhase = phase;
        runPatchPass(patches, PatchPass.PREPARE);
        runPatchPass(patches, PatchPass.ORIGINAL);
        runPatchPass(patches, PatchPass.REPLACEMENT);
    }

    private static void runPatchPass(List<QueuedTextPatch> patches, PatchPass pass) {
        for (QueuedTextPatch patch : patches) {
            try {
                switch (pass) {
                    case PREPARE -> patch.prepare();
                    case ORIGINAL -> patch.renderOriginal();
                    case REPLACEMENT -> patch.renderReplacement();
                }
            } catch (Throwable throwable) {
                RockstarClient.LOGGER.error("[CaptureBypass] deferred text patch failed", throwable);
            }
        }
    }

    static void reportViewportMismatch(int[] expected, int[] actual) {
        if (viewportMismatchWarnings >= 3) {
            return;
        }
        ++viewportMismatchWarnings;
        RockstarClient.LOGGER.warn(
            "[CaptureBypass] output viewport changed: was {}, became {}",
            Arrays.toString(expected),
            Arrays.toString(actual)
        );
    }

    public static void finishCaptureFrame() {
        if (!captureTargetActive) {
            if (!CAPTURE_SHIELD.isUnavailable()) {
                CAPTURE_SHIELD.hide();
            }
            return;
        }
        captureTargetActive = false;
        try {
            filterCapturedRegionsForScreen();
            if (clipCount == 0) {
                if (++framesWithoutClip > NO_CLIP_FRAME_LIMIT) {
                    CAPTURE_SHIELD.hide();
                    GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
                    return;
                }
            } else {
                framesWithoutClip = 0;
                clearAlphaInCapturedRegions();
            }
            Framebuffer mainFramebuffer = MinecraftClient.getInstance().getFramebuffer();
            if (mainFramebuffer != null) {
                CAPTURE_SHIELD.render(
                    CAPTURE_TARGET.getColorAttachment(),
                    CLIP_BOUNDS,
                    clipCount,
                    mainFramebuffer.textureWidth,
                    mainFramebuffer.textureHeight
                );
            }
        } finally {
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        }
    }

    private static void filterCapturedRegionsForScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean hasScreen = client.getOverlay() != null
            || client.currentScreen != null && !(client.currentScreen instanceof ChatScreen);
        if (!hasScreen) {
            return;
        }
        int writeIndex = 0;
        for (int readIndex = 0; readIndex < clipCount; ++readIndex) {
            if (CLIP_KINDS[readIndex] != 2) {
                continue;
            }
            System.arraycopy(CLIP_BOUNDS, readIndex * 4, CLIP_BOUNDS, writeIndex * 4, 4);
            CLIP_KINDS[writeIndex] = CLIP_KINDS[readIndex];
            ++writeIndex;
        }
        clipCount = writeIndex;
    }

    private static void clearAlphaInCapturedRegions() {
        CAPTURE_TARGET.beginWrite(false);
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager._enableScissorTest();
        for (int index = 0; index < clipCount; ++index) {
            int offset = index * 4;
            GlStateManager._scissorBox(
                CLIP_BOUNDS[offset], CLIP_BOUNDS[offset + 1],
                CLIP_BOUNDS[offset + 2], CLIP_BOUNDS[offset + 3]
            );
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT);
        }
        GlStateManager._disableScissorTest();
        RenderSystem.colorMask(true, true, true, true);
        MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
    }

    public static void setScreenRendering(boolean rendering) {
        screenRendering = rendering;
    }

    public static int getRenderPhase() {
        return currentPhase;
    }

    // Compatibility entry points retained for already-remapped callers.
    public static void beginFrame() {
        beginCaptureFrame();
    }

    public static void setRenderPhase(int phase) {
        applyCapturePhase(phase);
    }

    public static void endFrame() {
        finishCaptureFrame();
        currentPhase = 0;
        ORIGINAL_TEXT_PASS.remove();
    }

    public static void shutdown() {
        TEXT_PATCHES.clear();
        captureTargetActive = false;
        captureAvailable = false;
        clippedCapture = false;
        screenRendering = false;
        runnableCapture = false;
        CAPTURE_SHIELD.close();
        try {
            if (CAPTURE_TARGET.fbo >= 0) {
                CAPTURE_TARGET.delete();
            }
        } catch (Throwable throwable) {
            RockstarClient.LOGGER.debug("[CaptureBypass] private framebuffer cleanup failed", throwable);
        }
        ORIGINAL_TEXT_PASS.remove();
    }

    public static void clear() {
        shutdown();
    }

    private enum PatchPass {
        PREPARE,
        ORIGINAL,
        REPLACEMENT
    }

    private static final class QueuedTextPatch {
        private final TextCapturePatch patch;
        private final CaptureRenderState renderState;
        private int[] bounds;

        private QueuedTextPatch(TextCapturePatch patch, CaptureRenderState renderState) {
            this.patch = patch;
            this.renderState = renderState;
        }

        private void prepare() {
            if (!captureTargetActive) {
                return;
            }
            renderState.prepare();
            try {
                int[] patchBounds = patch.getBounds();
                if (patchBounds == null || clipCount >= MAX_CLIP_REGIONS) {
                    return;
                }
                Framebuffer mainFramebuffer = MinecraftClient.getInstance().getFramebuffer();
                if (mainFramebuffer == null) {
                    return;
                }
                copyFromMainFramebuffer(patchBounds, mainFramebuffer);
                bounds = patchBounds;
                int offset = clipCount * 4;
                CLIP_BOUNDS[offset] = patchBounds[0];
                CLIP_BOUNDS[offset + 1] = patchBounds[1];
                CLIP_BOUNDS[offset + 2] = patchBounds[2];
                CLIP_BOUNDS[offset + 3] = patchBounds[3];
                CLIP_KINDS[clipCount] = currentPhase;
                ++clipCount;
            } finally {
                renderState.restore();
            }
        }

        private void renderOriginal() {
            if (bounds == null) {
                return;
            }
            renderState.prepare();
            clippedCapture = true;
            try {
                CAPTURE_TARGET.beginWrite(false);
                GlStateManager._enableScissorTest();
                GlStateManager._scissorBox(bounds[0], bounds[1], bounds[2], bounds[3]);
                patch.renderOriginal();
            } finally {
                MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
                clippedCapture = false;
                renderState.restore();
            }
        }

        private void renderReplacement() {
            if (bounds == null) {
                return;
            }
            renderState.prepare();
            try {
                patch.renderReplacement();
            } finally {
                renderState.restore();
            }
        }
    }

    private static NameProtect getNameProtect() {
        if (RockstarClient.INSTANCE == null || RockstarClient.INSTANCE.getModuleRegistry() == null) {
            return null;
        }
        return RockstarClient.INSTANCE.getModuleRegistry().getModule(NameProtect.class);
    }
}
