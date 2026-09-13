package moscow.rockstar.modules.visuals.esp.entities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.visuals.esp.targeting.PlayerTargetGroup;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.esp.TargetRenderModule;
import moscow.rockstar.render.geometry.ShapeRenderer;
import moscow.rockstar.render.shaders.DynamicLightShader;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public class BlockOverlay extends TargetRenderModule {
    private final BooleanSetting enabledSetting = this.createSetting("esp.block_overlay");
    private final BooleanSetting themeSync = (BooleanSetting) this.createSetting((targetRenderModule, booleanSetting) ->
        new BooleanSetting((SettingOwner) targetRenderModule, "theme.sync", () -> !booleanSetting.isEnabled()).enable());
    private final ColorSetting color = (ColorSetting) this.createSetting("theme.sync",
        (targetRenderModule, booleanSetting, themeSyncSetting) ->
            new ColorSetting((SettingOwner) targetRenderModule, "esp.block_overlay.color",
                () -> !booleanSetting.isEnabled() || themeSyncSetting.isEnabled())
                .setColor(ColorPalette.getAccentColor()));
    private final BooleanSetting fill = (BooleanSetting) this.createSetting((targetRenderModule, booleanSetting) ->
        new BooleanSetting((SettingOwner) targetRenderModule, "esp.block_overlay.fill", () -> !booleanSetting.isEnabled()).enable());
    private final BooleanSetting outline = (BooleanSetting) this.createSetting((targetRenderModule, booleanSetting) ->
        new BooleanSetting((SettingOwner) targetRenderModule, "esp.block_overlay.outline", () -> !booleanSetting.isEnabled()).enable());
    private final BooleanSetting flat = (BooleanSetting) this.createSetting((targetRenderModule, booleanSetting) ->
        new BooleanSetting((SettingOwner) targetRenderModule, "esp.block_overlay.flat", () -> !booleanSetting.isEnabled()).enable());
    private final NumberSetting speed = (NumberSetting) this.createSetting((targetRenderModule, booleanSetting) ->
        new NumberSetting((SettingOwner) targetRenderModule, "esp.block_overlay.speed", () -> !booleanSetting.isEnabled())
            .setMinValue(10.0f)
            .setMaxValue(50.0f)
            .setStep(5.0f)
            .setValue(20.0f));
    private final BooleanSetting lighting = (BooleanSetting) this.createSetting((targetRenderModule, booleanSetting) ->
        new BooleanSetting((SettingOwner) targetRenderModule, "esp.block_overlay.lighting", () -> !booleanSetting.isEnabled()).enable());
    private final NumberSetting lightRadius = (NumberSetting) this.createSetting("esp.block_overlay.lighting",
        (targetRenderModule, booleanSetting, lightingSetting) ->
            new NumberSetting((SettingOwner) targetRenderModule, "esp.block_overlay.lighting.radius",
                () -> !booleanSetting.isEnabled() || !lightingSetting.isEnabled())
                .setMinValue(2.0f)
                .setMaxValue(7.0f)
                .setStep(0.5f)
                .setValue(4.0f)
                .setUnit(" blocks"));
    private final NumberSetting lightStrength = (NumberSetting) this.createSetting("esp.block_overlay.lighting",
        (targetRenderModule, booleanSetting, lightingSetting) ->
            new NumberSetting((SettingOwner) targetRenderModule, "esp.block_overlay.lighting.strength",
                () -> !booleanSetting.isEnabled() || !lightingSetting.isEnabled())
                .setMinValue(0.0f)
                .setMaxValue(150.0f)
                .setStep(10.0f)
                .setValue(100.0f)
                .setUnit("%"));

    private Box currentBox = null;
    private BlockPos targetedPos = null;
    private float currentAlpha = 0.0f;
    private long lastFrameTime = 0L;

    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> {
        this.currentBox = null;
        this.targetedPos = null;
        this.currentAlpha = 0.0f;
        this.lastFrameTime = 0L;
    };

    private final EventListener<Render3DEvent> renderListener = render3DEvent -> {
        if (!this.isValid2(PlayerTargetGroup.LOCAL_PLAYER)) {
            this.currentBox = null;
            this.targetedPos = null;
            this.currentAlpha = 0.0f;
            this.lastFrameTime = 0L;
            return;
        }
        this.renderSelectedBlockBounds(render3DEvent);
    };

    public BlockOverlay() {
        super("block_overlay", new PlayerTargetGroup[]{PlayerTargetGroup.LOCAL_PLAYER}, new TargetGroup[]{TargetGroup.PLAYERS});
    }

    public ColorRGBA getColor() {
        if (this.themeSync.isEnabled()) {
            ColorRGBA themeColor = ColorPalette.getAccentColor();
            float[] hsb = Color.RGBtoHSB((int) themeColor.getRed(), (int) themeColor.getGreen(), (int) themeColor.getBlue(), null);
            float newSaturation = Math.min(1.0f, hsb[1] * 1.2f);
            return ColorRGBA.fromHSB(hsb[0], newSaturation, hsb[2]).withAlpha(themeColor.getAlpha());
        }
        return this.color.getColor();
    }

    private void renderSelectedBlockBounds(Render3DEvent render3DEvent) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            this.currentBox = null;
            this.targetedPos = null;
            this.currentAlpha = 0.0f;
            this.lastFrameTime = 0L;
            return;
        }

        long now = System.currentTimeMillis();
        if (this.lastFrameTime == 0L) {
            this.lastFrameTime = now;
        }
        float deltaSeconds = (now - this.lastFrameTime) / 1000.0f;
        this.lastFrameTime = now;
        deltaSeconds = MathHelper.clamp(deltaSeconds, 0.0f, 0.1f);

        Box targetBox = null;
        if (client.crosshairTarget instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPosition = blockHit.getBlockPos();
            BlockState blockState = client.world.getBlockState(blockPosition);
            VoxelShape outlineShape = blockState.getOutlineShape(client.world, blockPosition);
            if (!outlineShape.isEmpty()) {
                targetBox = outlineShape.getBoundingBox().offset(blockPosition);
                this.targetedPos = blockPosition;
            }
        }

        float speedValue = this.speed.getValue();
        double factor = 1.0 - Math.exp(-speedValue * deltaSeconds);
        factor = MathHelper.clamp((float) factor, 0.0f, 1.0f);

        float targetAlpha = (targetBox != null) ? 1.0f : 0.0f;
        this.currentAlpha = (float) (this.currentAlpha + (targetAlpha - this.currentAlpha) * factor);
        if (this.currentAlpha < 0.001f) {
            this.currentAlpha = 0.0f;
            this.currentBox = null;
            this.targetedPos = null;
            return;
        }

        if (targetBox != null) {
            if (this.currentBox == null || this.currentAlpha < 0.02f
                    || this.currentBox.getCenter().squaredDistanceTo(targetBox.getCenter()) > 2500.0) {
                this.currentBox = targetBox;
            } else {
                double minX = this.currentBox.minX + (targetBox.minX - this.currentBox.minX) * factor;
                double minY = this.currentBox.minY + (targetBox.minY - this.currentBox.minY) * factor;
                double minZ = this.currentBox.minZ + (targetBox.minZ - this.currentBox.minZ) * factor;
                double maxX = this.currentBox.maxX + (targetBox.maxX - this.currentBox.maxX) * factor;
                double maxY = this.currentBox.maxY + (targetBox.maxY - this.currentBox.maxY) * factor;
                double maxZ = this.currentBox.maxZ + (targetBox.maxZ - this.currentBox.maxZ) * factor;
                this.currentBox = new Box(minX, minY, minZ, maxX, maxY, maxZ);

                if (Math.abs(this.currentBox.minX - targetBox.minX) < 0.001
                        && Math.abs(this.currentBox.minY - targetBox.minY) < 0.001
                        && Math.abs(this.currentBox.minZ - targetBox.minZ) < 0.001
                        && Math.abs(this.currentBox.maxX - targetBox.maxX) < 0.001
                        && Math.abs(this.currentBox.maxY - targetBox.maxY) < 0.001
                        && Math.abs(this.currentBox.maxZ - targetBox.maxZ) < 0.001) {
                    this.currentBox = targetBox;
                }
            }
        }

        if (this.currentBox == null) {
            return;
        }

        Camera camera = render3DEvent.getCamera();
        Vec3d cameraPos = camera.getPos();
        ColorRGBA colorRGBA = this.getColor();

        // Render dynamic light from the block overlay
        if (this.lighting.isEnabled() && this.currentAlpha > 0.001f) {
            float radiusVal = this.lightRadius.getValue();
            float strengthVal = (this.lightStrength.getValue() / 100.0f) * this.currentAlpha;
            if (radiusVal > 0.0f && strengthVal > 0.001f) {
                Vec3d center = this.currentBox.getCenter();

                double lightX = center.x;
                double lightY = center.y;
                double lightZ = center.z;

                // Position the light slightly outside the block along camera-visible axes,
                // ensuring the surface normals face the light source and eliminating diffuse self-shadowing in the center
                double offset = 0.2;
                if (cameraPos.x > this.currentBox.maxX) {
                    lightX = this.currentBox.maxX + offset;
                } else if (cameraPos.x < this.currentBox.minX) {
                    lightX = this.currentBox.minX - offset;
                }

                if (cameraPos.y > this.currentBox.maxY) {
                    lightY = this.currentBox.maxY + offset;
                } else if (cameraPos.y < this.currentBox.minY) {
                    lightY = this.currentBox.minY - offset;
                }

                if (cameraPos.z > this.currentBox.maxZ) {
                    lightZ = this.currentBox.maxZ + offset;
                } else if (cameraPos.z < this.currentBox.minZ) {
                    lightZ = this.currentBox.minZ - offset;
                }

                float lx = (float) (lightX - cameraPos.x);
                float ly = (float) (lightY - cameraPos.y);
                float lz = (float) (lightZ - cameraPos.z);
                float lr = colorRGBA.getRed() / 255.0f;
                float lg = colorRGBA.getGreen() / 255.0f;
                float lb = colorRGBA.getBlue() / 255.0f;

                List<DynamicLightShader.LightSource> lightSources = new ArrayList<>(1);
                lightSources.add(new DynamicLightShader.LightSource(lx, ly, lz, radiusVal, lr, lg, lb, strengthVal));

                Matrix4f invViewProj = new Matrix4f((Matrix4fc) render3DEvent.getProjectionMatrix())
                        .mul((Matrix4fc) render3DEvent.getPositionMatrix())
                        .invert();
                ShaderRenderer.particleLightRenderer.renderParticleLights(invViewProj, 1.5f, lightSources);
            }
        }

        MatrixStack matrices = render3DEvent.getMatrices();
        Box renderBounds = this.currentBox.offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        boolean isFlat = this.flat.isEnabled();
        boolean upVisible = true;
        boolean downVisible = true;
        boolean northVisible = true;
        boolean southVisible = true;
        boolean westVisible = true;
        boolean eastVisible = true;

        if (isFlat) {
            boolean upFacing = cameraPos.y > this.currentBox.maxY;
            boolean downFacing = cameraPos.y < this.currentBox.minY;
            boolean northFacing = cameraPos.z < this.currentBox.minZ;
            boolean southFacing = cameraPos.z > this.currentBox.maxZ;
            boolean westFacing = cameraPos.x < this.currentBox.minX;
            boolean eastFacing = cameraPos.x > this.currentBox.maxX;

            boolean upOccluded = this.isNeighborSolid(client.world, this.targetedPos, Direction.UP);
            boolean downOccluded = this.isNeighborSolid(client.world, this.targetedPos, Direction.DOWN);
            boolean northOccluded = this.isNeighborSolid(client.world, this.targetedPos, Direction.NORTH);
            boolean southOccluded = this.isNeighborSolid(client.world, this.targetedPos, Direction.SOUTH);
            boolean westOccluded = this.isNeighborSolid(client.world, this.targetedPos, Direction.WEST);
            boolean eastOccluded = this.isNeighborSolid(client.world, this.targetedPos, Direction.EAST);

            upVisible = upFacing && !upOccluded;
            downVisible = downFacing && !downOccluded;
            northVisible = northFacing && !northOccluded;
            southVisible = southFacing && !southOccluded;
            westVisible = westFacing && !westOccluded;
            eastVisible = eastFacing && !eastOccluded;
        }

        if (this.fill.isEnabled()) {
            ColorRGBA fillColor = new ColorRGBA(colorRGBA.getRed(), colorRGBA.getGreen(), colorRGBA.getBlue())
                    .withAlpha(40.0f * this.currentAlpha);
            if (isFlat) {
                ShapeRenderer.drawVisibleFaces(matrices, renderBounds, upVisible, downVisible, northVisible, southVisible, westVisible, eastVisible, fillColor);
            } else {
                ShapeRenderer.drawFilledBox(matrices, renderBounds, fillColor);
            }
        }
        if (this.outline.isEnabled()) {
            ColorRGBA outlineColor = new ColorRGBA(colorRGBA.getRed(), colorRGBA.getGreen(), colorRGBA.getBlue())
                    .withAlpha(220.0f * this.currentAlpha);
            if (isFlat) {
                ShapeRenderer.drawVisibleEdges(matrices, renderBounds, upVisible, downVisible, northVisible, southVisible, westVisible, eastVisible, outlineColor);
            } else {
                ShapeRenderer.drawOutlinedBox(matrices, renderBounds, outlineColor);
            }
        }
    }

    private boolean isNeighborSolid(World world, BlockPos pos, Direction dir) {
        if (world == null || pos == null) {
            return false;
        }
        BlockPos neighborPos = pos.offset(dir);
        BlockState neighborState = world.getBlockState(neighborPos);
        return neighborState.isSideSolidFullSquare(world, neighborPos, dir.getOpposite());
    }
}
