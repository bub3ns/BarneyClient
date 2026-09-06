package moscow.rockstar.render.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import moscow.rockstar.render.layers.BlockGhostRenderLayer;
import moscow.rockstar.render.layers.SchematicRenderLayer;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;
import pyrock.events.render.Render3DEvent;

/** Maintains the small, camera-facing block set used by the clip effect. */
public final class CameraClipManager {
    private static final double CAMERA_RADIUS = 1.5;
    private static final double CLIP_DISTANCE = 2.4;
    private static final double CLIP_DISTANCE_SLOPE = 1.8;
    private static final double PLAYER_BOX_PADDING = 0.05;
    private static final double EYE_POINT_OFFSET = 0.3;
    private static final long FADE_DURATION_MILLIS = 220L;
    private static final int MAX_CLIPPED_BLOCKS = 256;
    private static final int RETAINED_FRAMES = 3;
    private static final float GHOST_ALPHA = 0.25f;

    private static final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private static final Long2ObjectOpenHashMap<TrackedBlock> trackedBlocks = new Long2ObjectOpenHashMap<>();
    private static volatile LongOpenHashSet hiddenBlocks = new LongOpenHashSet();
    private static volatile int dynamicLight = 0xF000F0;
    private static volatile int blockLight = 15;
    private static volatile int skyLight = 15;
    private static final ClipRenderView renderView = new ClipRenderView();
    private static final AlphaVertexConsumer alphaConsumer = new AlphaVertexConsumer();

    private CameraClipManager() {
    }

    /** Updates the exact block set selected by the original camera-clip algorithm. */
    public static void update(float tickDelta, boolean enabled) {
        if (minecraftClient.player == null || minecraftClient.world == null) {
            clear();
            return;
        }

        boolean firstPerson = minecraftClient.options.getPerspective().isFirstPerson();
        LongOpenHashSet visibleCandidates = enabled && !firstPerson
            ? findVisibleBlocks(tickDelta)
            : new LongOpenHashSet();

        LongIterator candidateIterator = visibleCandidates.iterator();
        while (candidateIterator.hasNext()) {
            trackedBlocks.computeIfAbsent(candidateIterator.nextLong(), ignored -> new TrackedBlock());
        }

        LongOpenHashSet nextHiddenBlocks = new LongOpenHashSet();
        ObjectIterator<Long2ObjectMap.Entry<TrackedBlock>> iterator = trackedBlocks.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<TrackedBlock> entry = iterator.next();
            TrackedBlock trackedBlock = entry.getValue();
            boolean selected = visibleCandidates.contains(entry.getLongKey());

            if (trackedBlock.removing) {
                if (selected) {
                    trackedBlock.removing = false;
                    trackedBlock.retainedFrames = RETAINED_FRAMES;
                } else if (--trackedBlock.retainedFrames > 0) {
                    continue;
                } else {
                    iterator.remove();
                    continue;
                }
            }

            if (trackedBlock.retainedFrames > 0) {
                --trackedBlock.retainedFrames;
            } else if (selected) {
                trackedBlock.fadeAnimation.setReverse(true);
            } else if (trackedBlock.fadeAnimation.update(0.0f) <= 0.001f) {
                trackedBlock.removing = true;
                trackedBlock.retainedFrames = RETAINED_FRAMES;
                continue;
            }
            nextHiddenBlocks.add(entry.getLongKey());
        }

        if (!nextHiddenBlocks.equals(hiddenBlocks)) {
            replaceHiddenBlocks(nextHiddenBlocks);
        }
    }

    /** Compatibility entry point retained for already-remapped callers. */
    public static void setTransparentBlocks(boolean enabled) {
        update(minecraftClient.getRenderTickCounter().getTickDelta(false), enabled);
    }

    public static boolean shouldHideBlock(BlockPos position) {
        return position != null && hiddenBlocks.contains(position.asLong());
    }

    public static int preserveLightLevel(BlockPos position, int lightLevel) {
        return shouldHideBlock(position) ? Math.max(lightLevel, dynamicLight) : lightLevel;
    }

    public static int preserveLightLevel(LightType lightType, int lightLevel) {
        return Math.max(lightLevel, lightType == LightType.SKY ? skyLight : blockLight);
    }

    public static void clear() {
        trackedBlocks.clear();
        replaceHiddenBlocks(new LongOpenHashSet());
    }

    /** Renders the fading ghost blocks using the same block renderer and layer as the original. */
    public static void render(Render3DEvent event) {
        if (trackedBlocks.isEmpty() || minecraftClient.world == null || minecraftClient.player == null) {
            return;
        }

        Vec3d cameraPosition = minecraftClient.gameRenderer.getCamera().getPos();
        VertexConsumerProvider.Immediate vertexConsumers = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
        RenderLayer renderLayer = BlockGhostRenderLayer.isReady()
            ? BlockGhostRenderLayer.getGhostLayer()
            : SchematicRenderLayer.getVisibleLayer();
        VertexConsumer buffer = vertexConsumers.getBuffer(renderLayer);
        MatrixStack matrices = event.getMatrices();
        Random random = Random.create();
        BlockRenderManager blockRenderManager = minecraftClient.getBlockRenderManager();

        renderView.setRenderedPosition(Long.MIN_VALUE);
        alphaConsumer.setDelegate(buffer);
        ObjectIterator<Long2ObjectMap.Entry<TrackedBlock>> iterator = trackedBlocks.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<TrackedBlock> entry = iterator.next();
            BlockPos position = BlockPos.fromLong(entry.getLongKey());
            BlockState state = minecraftClient.world.getBlockState(position);
            if (state.getRenderType() != BlockRenderType.MODEL) {
                continue;
            }

            double dx = position.getX() + 0.5 - cameraPosition.x;
            double dy = position.getY() + 0.5 - cameraPosition.y;
            double dz = position.getZ() + 0.5 - cameraPosition.z;
            float distanceFactor = (float)Math.clamp((Math.sqrt(dx * dx + dy * dy + dz * dz) - CAMERA_RADIUS), 0.0, 1.0);
            float alpha = 1.0f - (1.0f - GHOST_ALPHA * distanceFactor) * entry.getValue().fadeAnimation.getValue();
            if (alpha <= 0.004f) {
                continue;
            }

            alphaConsumer.setAlpha(alpha);
            renderView.setRenderedPosition(entry.getLongKey());
            matrices.push();
            matrices.translate(position.getX() - cameraPosition.x, position.getY() - cameraPosition.y, position.getZ() - cameraPosition.z);
            blockRenderManager.renderBlock(state, position, renderView, matrices, alphaConsumer, true, random);
            matrices.pop();
        }

        alphaConsumer.setDelegate(null);
        renderView.setRenderedPosition(Long.MIN_VALUE);
        vertexConsumers.draw(renderLayer);
    }

    private static LongOpenHashSet findVisibleBlocks(float tickDelta) {
        Vec3d cameraPosition = minecraftClient.gameRenderer.getCamera().getPos();
        Vec3d playerPosition = ProjectionUtils.interpolateEntityPosition(minecraftClient.player, tickDelta);
        Box playerBounds = minecraftClient.player.getBoundingBox()
            .offset(playerPosition.subtract(minecraftClient.player.getPos()))
            .expand(PLAYER_BOX_PADDING);
        BlockPos eyeBlock = BlockPos.ofFloored(playerPosition.add(0.0, minecraftClient.player.getStandingEyeHeight(), 0.0));
        BlockPos cameraBlock = BlockPos.ofFloored(cameraPosition);

        blockLight = Math.max(minecraftClient.world.getLightLevel(LightType.BLOCK, eyeBlock), minecraftClient.world.getLightLevel(LightType.BLOCK, cameraBlock));
        skyLight = Math.max(minecraftClient.world.getLightLevel(LightType.SKY, eyeBlock), minecraftClient.world.getLightLevel(LightType.SKY, cameraBlock));
        dynamicLight = LightmapTextureManager.pack(blockLight, skyLight);

        Vec3d center = playerBounds.getCenter();
        List<Vec3d> points = List.of(
            new Vec3d(center.x, playerBounds.minY + EYE_POINT_OFFSET, center.z),
            center,
            new Vec3d(center.x, playerBounds.maxY, center.z)
        );
        if (points.isEmpty()) {
            return new LongOpenHashSet();
        }

        LongOpenHashSet result = new LongOpenHashSet();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int padding = (int)Math.ceil(CLIP_DISTANCE) + 1;
        int minX = (int)Math.floor(Math.min(cameraPosition.x, playerBounds.minX)) - padding;
        int minY = (int)Math.floor(Math.min(cameraPosition.y, playerBounds.minY)) - padding;
        int minZ = (int)Math.floor(Math.min(cameraPosition.z, playerBounds.minZ)) - padding;
        int maxX = (int)Math.floor(Math.max(cameraPosition.x, playerBounds.maxX)) + padding;
        int maxY = (int)Math.floor(Math.max(cameraPosition.y, playerBounds.maxY)) + padding;
        int maxZ = (int)Math.floor(Math.max(cameraPosition.z, playerBounds.maxZ)) + padding;
        boolean cameraAbovePlayer = cameraPosition.y > playerBounds.minY;

        for (int x = minX; x <= maxX && result.size() < MAX_CLIPPED_BLOCKS; ++x) {
            for (int y = minY; y <= maxY && result.size() < MAX_CLIPPED_BLOCKS; ++y) {
                if (cameraAbovePlayer && y + 1.0 <= playerBounds.minY + 0.02) {
                    continue;
                }
                for (int z = minZ; z <= maxZ && result.size() < MAX_CLIPPED_BLOCKS; ++z) {
                    if (!isWithinClipDistance(x, y, z, cameraPosition, points)) {
                        continue;
                    }
                    mutable.set(x, y, z);
                    if (isModelBlock(minecraftClient.world.getBlockState(mutable))) {
                        result.add(mutable.asLong());
                    }
                }
            }
        }
        return result;
    }

    private static boolean isModelBlock(BlockState state) {
        return state.getRenderType() == BlockRenderType.MODEL;
    }

    private static boolean isWithinClipDistance(int x, int y, int z, Vec3d cameraPosition, List<Vec3d> points) {
        double blockX = x + 0.5;
        double blockY = y + 0.5;
        double blockZ = z + 0.5;
        for (Vec3d point : points) {
            double dx = point.x - cameraPosition.x;
            double dy = point.y - cameraPosition.y;
            double dz = point.z - cameraPosition.z;
            double lengthSquared = dx * dx + dy * dy + dz * dz;
            if (lengthSquared < 0.01) {
                continue;
            }
            double projection = Math.clamp(((blockX - cameraPosition.x) * dx
                + (blockY - cameraPosition.y) * dy
                + (blockZ - cameraPosition.z) * dz) / lengthSquared, 0.0, 1.0);
            double closestX = cameraPosition.x + dx * projection - blockX;
            double closestY = cameraPosition.y + dy * projection - blockY;
            double closestZ = cameraPosition.z + dz * projection - blockZ;
            double radius = CLIP_DISTANCE - CLIP_DISTANCE_SLOPE * projection;
            if (closestX * closestX + closestY * closestY + closestZ * closestZ <= radius * radius) {
                return true;
            }
        }
        return false;
    }

    private static void replaceHiddenBlocks(LongOpenHashSet replacement) {
        LongOpenHashSet previous = hiddenBlocks;
        hiddenBlocks = replacement;
        if (minecraftClient.worldRenderer == null || minecraftClient.world == null) {
            return;
        }

        for (LongOpenHashSet changedSet : List.of(previous, replacement)) {
            LongIterator iterator = changedSet.iterator();
            while (iterator.hasNext()) {
                long packedPosition = iterator.nextLong();
                if (previous.contains(packedPosition) == replacement.contains(packedPosition)) {
                    continue;
                }
                BlockPos position = BlockPos.fromLong(packedPosition);
                BlockState state = minecraftClient.world.getBlockState(position);
                minecraftClient.worldRenderer.updateBlock(minecraftClient.world, position, state, state, 3);
            }
        }
    }

    private static final class TrackedBlock {
        private final Animation fadeAnimation = new Animation(FADE_DURATION_MILLIS, 0.0f, Easing.linear);
        private int retainedFrames = RETAINED_FRAMES;
        private boolean removing;
    }

    private static final class AlphaVertexConsumer implements VertexConsumer {
        private VertexConsumer delegate;
        private float alpha = 1.0f;

        private void setDelegate(VertexConsumer delegate) {
            this.delegate = delegate;
        }

        private void setAlpha(float alpha) {
            this.alpha = alpha;
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(red, green, blue, (int)(alpha * this.alpha));
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            delegate.texture(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            delegate.overlay(u, v);
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            delegate.light(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
            int adjustedColor = ColorHelper.withAlpha((int)(ColorHelper.getAlpha(color) * alpha), color);
            delegate.vertex(x, y, z, adjustedColor, u, v, overlay, light, normalX, normalY, normalZ);
        }
    }

    private static final class ClipRenderView implements BlockRenderView {
        private long renderedPosition = Long.MIN_VALUE;

        private void setRenderedPosition(long renderedPosition) {
            this.renderedPosition = renderedPosition;
        }

        @Override
        public BlockEntity getBlockEntity(BlockPos position) {
            return minecraftClient.world.getBlockEntity(position);
        }

        @Override
        public BlockState getBlockState(BlockPos position) {
            return position.asLong() != renderedPosition && shouldHideBlock(position)
                ? Blocks.AIR.getDefaultState()
                : minecraftClient.world.getBlockState(position);
        }

        @Override
        public FluidState getFluidState(BlockPos position) {
            return getBlockState(position).getFluidState();
        }

        @Override
        public float getBrightness(Direction direction, boolean shaded) {
            return minecraftClient.world.getBrightness(direction, shaded);
        }

        @Override
        public LightingProvider getLightingProvider() {
            return minecraftClient.world.getLightingProvider();
        }

        @Override
        public int getColor(BlockPos position, ColorResolver resolver) {
            return minecraftClient.world.getColor(position, resolver);
        }

        @Override
        public int getHeight() {
            return minecraftClient.world.getHeight();
        }

        @Override
        public int getBottomY() {
            return minecraftClient.world.getBottomY();
        }
    }
}
