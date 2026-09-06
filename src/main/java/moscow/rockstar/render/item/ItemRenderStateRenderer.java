package moscow.rockstar.render.item;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.UnaryOperator;
import moscow.rockstar.mixin.accessors.SpriteAtlasTextureAccessor;
import moscow.rockstar.render.layers.SmoothItemRenderLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * Preserves the original custom item-render path used by Rockstar DrawContexts.
 */
public final class ItemRenderStateRenderer {
    private static final Map<BakedQuad, BakedQuad> INSET_QUADS = new WeakHashMap<>();
    private static SpriteAtlasTexture blockAtlas;
    private static boolean smoothingActive;
    /** ORIGINAL: the second {@code static} flag on rockstar/ilIlil/iIiiiIiiI - when set, the
     *  smooth-item layer swap is bypassed entirely and the raw consumers are used. */
    private static boolean smoothingSuppressed;

    private ItemRenderStateRenderer() {
    }

    /**
     * ORIGINAL: {@code rockstar/ilIlil/iIiiiIiiI$I} - an AutoCloseable that suppresses the
     * smooth-item render layer for the duration of a block and restores the previous state.
     */
    public static final class SuppressScope implements AutoCloseable {
        private final boolean previous = smoothingSuppressed;

        private SuppressScope() {
            smoothingSuppressed = true;
        }

        @Override
        public void close() {
            smoothingSuppressed = this.previous;
        }
    }

    /** ORIGINAL: {@code iIiiiIiiI.I()Lrockstar/ilIlil/iIiiiIiiI$I;}. */
    public static SuppressScope suppressSmoothing() {
        return new SuppressScope();
    }

    public static void render(ItemRenderState state, MatrixStack matrices,
                              VertexConsumerProvider consumers, int light, int overlay) {
        render(state, matrices, consumers, light, overlay, UnaryOperator.identity());
    }

    public static void render(ItemRenderState state, MatrixStack matrices,
                              VertexConsumerProvider consumers, int light, int overlay,
                              UnaryOperator<RenderLayer> layerTransform) {
        if (smoothingSuppressed) {
            state.render(matrices, consumers, light, overlay);
            return;
        }
        boolean previous = smoothingActive;
        smoothingActive = true;
        try {
            state.render(
                matrices,
                layer -> consumers.getBuffer(transformLayer(layerTransform.apply(layer))),
                light,
                overlay
            );
        } finally {
            smoothingActive = previous;
        }
    }

    public static BakedQuad insetQuad(BakedQuad quad) {
        if (!smoothingActive || !SmoothItemRenderLayer.isReady()) {
            return quad;
        }
        BakedQuad cached = INSET_QUADS.get(quad);
        if (cached != null) {
            return cached;
        }
        BakedQuad inset = createInsetQuad(quad);
        INSET_QUADS.put(quad, inset);
        return inset;
    }

    private static RenderLayer transformLayer(RenderLayer layer) {
        if (!SmoothItemRenderLayer.isReady()) {
            return layer;
        }
        // ORIGINAL: rockstar/ilIlil/iIiiiIiiI#I(Lnet/minecraft/class_1921;)Lnet/minecraft/class_1921;
        // compares against class_4722.method_29382() and class_4722.method_24074(), i.e.
        // TexturedRenderLayers.getItemEntityTranslucentCull() and .getEntityCutout(). The remap
        // reconstructed the second one as RenderLayer.getEntityTranslucent(...), which is a
        // DIFFERENT layer, so the smooth-item CUTOUT swap never fired at all. Kept in the
        // original's literal form so it cannot silently drift again.
        if (layer == TexturedRenderLayers.getItemEntityTranslucentCull()) {
            updateBlockAtlas();
            return SmoothItemRenderLayer.getTranslucentLayer(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        }
        if (layer == TexturedRenderLayers.getEntityCutout()) {
            updateBlockAtlas();
            return SmoothItemRenderLayer.getCutoutLayer(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        }
        return layer;
    }

    private static BakedQuad createInsetQuad(BakedQuad quad) {
        Sprite sprite = quad.getSprite();
        int[] vertexData = quad.getVertexData().clone();
        int vertexCount = vertexData.length / 8;
        float minU;
        float maxU;
        float minV;
        float maxV;
        if (sprite != null) {
            SpriteContents contents = sprite.getContents();
            float insetU = (sprite.getMaxU() - sprite.getMinU())
                / (2.0f * Math.max(1, contents.getWidth()));
            float insetV = (sprite.getMaxV() - sprite.getMinV())
                / (2.0f * Math.max(1, contents.getHeight()));
            minU = sprite.getMinU() + insetU;
            maxU = sprite.getMaxU() - insetU;
            minV = sprite.getMinV() + insetV;
            maxV = sprite.getMaxV() - insetV;
        } else {
            if (blockAtlas == null) {
                return quad;
            }
            SpriteAtlasTextureAccessor atlas = (SpriteAtlasTextureAccessor) blockAtlas;
            float insetU = 0.5f / Math.max(1, atlas.rockstar$getWidth());
            float insetV = 0.5f / Math.max(1, atlas.rockstar$getHeight());
            minU = Float.MAX_VALUE;
            maxU = -Float.MAX_VALUE;
            minV = Float.MAX_VALUE;
            maxV = -Float.MAX_VALUE;
            for (int vertex = 0; vertex < vertexCount; ++vertex) {
                float u = Float.intBitsToFloat(vertexData[vertex * 8 + 4]);
                float v = Float.intBitsToFloat(vertexData[vertex * 8 + 5]);
                minU = Math.min(minU, u);
                maxU = Math.max(maxU, u);
                minV = Math.min(minV, v);
                maxV = Math.max(maxV, v);
            }
            minU += insetU;
            maxU -= insetU;
            minV += insetV;
            maxV -= insetV;
        }
        if (minU > maxU) {
            minU = maxU = (minU + maxU) * 0.5f;
        }
        if (minV > maxV) {
            minV = maxV = (minV + maxV) * 0.5f;
        }
        for (int vertex = 0; vertex < vertexCount; ++vertex) {
            int uIndex = vertex * 8 + 4;
            int vIndex = vertex * 8 + 5;
            vertexData[uIndex] = Float.floatToRawIntBits(
                MathHelper.clamp(Float.intBitsToFloat(vertexData[uIndex]), minU, maxU)
            );
            vertexData[vIndex] = Float.floatToRawIntBits(
                MathHelper.clamp(Float.intBitsToFloat(vertexData[vIndex]), minV, maxV)
            );
        }
        return new BakedQuad(
            vertexData,
            quad.getTintIndex(),
            quad.getFace(),
            sprite,
            quad.hasShade(),
            quad.getLightEmission()
        );
    }

    private static void updateBlockAtlas() {
        SpriteAtlasTexture atlas = (SpriteAtlasTexture) MinecraftClient.getInstance()
            .getTextureManager()
            .getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        if (atlas != blockAtlas) {
            blockAtlas = atlas;
            INSET_QUADS.clear();
        }
    }
}
