/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Box
 *  net.minecraft.Vec2f
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.modules.visuals.prediction;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import moscow.rockstar.combat.rotation.AimTrajectorySampler;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

@ModuleInfo(name="PositionPredict", category=ModuleCategory.VISUALS)
public class PositionPredict
extends Module {
    private NumberSetting ticks;
    private NumberSetting directions;
    private NumberSetting branchInterval;
    private NumberSetting hitchance;
    private BooleanSetting jumpBranches;
    private BooleanSetting predictSelf;
    private BooleanSetting nearestFallback;
    private BooleanSetting renderCloud;
    private BooleanSetting renderBoxes;
    private final AimTrajectorySampler.MotionHistory predictionRenderState = new AimTrajectorySampler.MotionHistory();
    private AimTrajectorySampler.TrajectoryResult predictionLineState;
    private final EventListener<Render3DEvent> onRender3DEventListener = render3DEvent -> {
        float f;
        AimTrajectorySampler.TrajectoryResult trajectoryResult = this.predictionLineState;
        if (trajectoryResult == null) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        class_45872.push();
        ItemRenderUtils.beginOverlayRendering(true);
        ItemRenderUtils.translateToCamera(class_45872);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        double d = 1.0E-6;
        for (AimTrajectorySampler.TrajectoryCandidate trajectoryCandidate : trajectoryResult.getCandidates()) {
            d = Math.max(d, trajectoryCandidate.getProbability());
        }
        if (this.renderCloud.isEnabled()) {
            for (AimTrajectorySampler.TrajectoryCandidate trajectoryCandidate : trajectoryResult.getCandidates()) {
                f = (float)(25.0 + 165.0 * (trajectoryCandidate.getProbability() / d));
                ColorRGBA colorRGBA = ColorPalette.getAccentColor().withAlpha(f);
                List<Vec3d> list = trajectoryCandidate.getTrajectoryPoints();
                for (int i = 0; i < list.size() - 1; ++i) {
                    RenderUtils.drawLineSegment(class_45872, class_2872, list.get(i), list.get(i + 1), colorRGBA);
                }
            }
        }
        if (this.renderBoxes.isEnabled()) {
            for (AimTrajectorySampler.TrajectoryCandidate trajectoryCandidate : trajectoryResult.getCandidates()) {
                if (trajectoryCandidate.isMostLikely()) {
                    this.renderPredictionBox(class_45872, class_2872, trajectoryCandidate.getEndBounds(), ColorPalette.WHITE.withAlpha(200.0f));
                    continue;
                }
                f = (float)(20.0 + 120.0 * (trajectoryCandidate.getProbability() / d));
                this.renderPredictionBox(class_45872, class_2872, trajectoryCandidate.getEndBounds(), ColorPalette.getAccentColor().withAlpha(f));
            }
        }
        RenderUtils.drawLineSegment(class_45872, class_2872, trajectoryResult.getEyePosition(), trajectoryResult.getAimPoint(), ColorPalette.GREEN.withAlpha(190.0f));
        this.renderPredictionPoint(class_45872, class_2872, trajectoryResult.getMostLikelyPoint(), ColorPalette.WHITE.withAlpha(220.0f));
        this.renderPredictionPoint(class_45872, class_2872, trajectoryResult.getAimPoint(), ColorPalette.GREEN.withAlpha(255.0f));
        ItemRenderUtils.flushVertexConsumer(class_2872);
        ItemRenderUtils.endOverlayRendering();
        class_45872.pop();
    };
    private final EventListener<PreHudRenderEvent> onPreHudRenderEventListener = preHudRenderEvent -> {
        AimTrajectorySampler.TrajectoryResult trajectoryResult = this.predictionLineState;
        if (trajectoryResult == null) {
            return;
        }
        Vec2f VanillaAdventureTabAdvancementGenerator = ProjectionUtils.projectToScreen(trajectoryResult.getAimPoint());
        if (VanillaAdventureTabAdvancementGenerator == null) {
            return;
        }
        CustomDrawContext customDrawContext = preHudRenderEvent.getContext();
        FontMetrics fontMetrics = Font.MEDIUM.metrics(11.0f);
        String string = String.format("%.0f%% \u00b7 %dt \u00b7 %d", trajectoryResult.getCoveragePercent(), trajectoryResult.getPredictionTicks(), trajectoryResult.getVariantCount());
        customDrawContext.drawCenteredText(fontMetrics, string, VanillaAdventureTabAdvancementGenerator.x, VanillaAdventureTabAdvancementGenerator.y - fontMetrics.getFontTopOffset() - 2.0f, ColorPalette.GREEN);
    };

    public PositionPredict() {
        this.ticks = new NumberSetting(this, "ticks").setMinValue(1.0f).setMaxValue(40.0f).setStep(1.0f).setValue(10.0f).setUnit(" t");
        this.directions = new NumberSetting(this, "directions").setMinValue(4.0f).setMaxValue(16.0f).setStep(1.0f).setValue(8.0f);
        this.branchInterval = new NumberSetting(this, "branch_interval").setMinValue(1.0f).setMaxValue(20.0f).setStep(1.0f).setValue(4.0f).setUnit(" t");
        this.hitchance = new NumberSetting(this, "hitchance").setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f).setValue(100.0f).setUnit("%");
        this.jumpBranches = new BooleanSetting(this, "jump_branches").enable();
        this.predictSelf = new BooleanSetting(this, "predict_self").enable();
        this.nearestFallback = new BooleanSetting(this, "nearest_fallback").enable();
        this.renderCloud = new BooleanSetting(this, "render_cloud").enable();
        this.renderBoxes = new BooleanSetting(this, "render_boxes").enable();
    }

    @Override
    public void onDisable() {
        this.predictionLineState = null;
        this.predictionRenderState.reset();
    }

    @Override
    public void onTick() {
        Vec3d VanillaChestLootTableGenerator;
        this.predictionLineState = null;
        if (PositionPredict.minecraftClient.player == null || PositionPredict.minecraftClient.world == null) {
            return;
        }
        LivingEntity class_13092 = this.getTargetEntity();
        if (class_13092 == null) {
            this.predictionRenderState.reset();
            return;
        }
        this.predictionRenderState.recordPosition((Entity)class_13092);
        int n = (int)this.ticks.getValue();
        Vec3d WallPlayerSkullBlock = VanillaChestLootTableGenerator = this.predictSelf.isEnabled() ? AimTrajectorySampler.predictPlayerPosition(n) : PositionPredict.minecraftClient.player.getEyePos();
        if (VanillaChestLootTableGenerator == null) {
            VanillaChestLootTableGenerator = PositionPredict.minecraftClient.player.getEyePos();
        }
        AimTrajectorySampler.TrajectorySettings trajectorySettings = new AimTrajectorySampler.TrajectorySettings(n, (int)this.directions.getValue(), (int)this.branchInterval.getValue(), this.jumpBranches.isEnabled(), 64.0, 96);
        this.predictionLineState = AimTrajectorySampler.predictAimTrajectory((Entity)class_13092, this.predictionRenderState.getAverageDelta(), this.predictionRenderState.getStationaryTicks(), VanillaChestLootTableGenerator, (double)this.hitchance.getValue() / 100.0, trajectorySettings);
    }

    private LivingEntity getTargetEntity() {
        LivingEntity class_13092 = RockstarClient.create().getFriendManager().getTargetLivingEntity();
        if (class_13092 != null && class_13092.isAlive() && class_13092 != PositionPredict.minecraftClient.player) {
            return class_13092;
        }
        if (!this.nearestFallback.isEnabled()) {
            return null;
        }
        PlayerEntity class_16572 = null;
        double d = Double.MAX_VALUE;
        for (PlayerEntity class_16573 : PositionPredict.minecraftClient.world.getPlayers()) {
            double d2;
            if (class_16573 == PositionPredict.minecraftClient.player || !class_16573.isAlive() || !((d2 = class_16573.squaredDistanceTo((Entity)PositionPredict.minecraftClient.player)) < d)) continue;
            d = d2;
            class_16572 = class_16573;
        }
        return class_16572;
    }

    private void renderPredictionBox(MatrixStack class_45872, BufferBuilder class_2872, Box HorizontalFacingBlock, ColorRGBA colorRGBA) {
        double d = HorizontalFacingBlock.minX;
        double d2 = HorizontalFacingBlock.minY;
        double d3 = HorizontalFacingBlock.minZ;
        double d4 = HorizontalFacingBlock.maxX;
        double d5 = HorizontalFacingBlock.maxY;
        double d6 = HorizontalFacingBlock.maxZ;
        this.updatePredictionGeometry(class_45872, class_2872, d, d2, d3, d4, d2, d3, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, d4, d2, d3, d4, d2, d6, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, d4, d2, d6, d, d2, d6, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, d, d2, d6, d, d2, d3, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, d, d5, d3, d4, d5, d3, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, d4, d5, d3, d4, d5, d6, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, d4, d5, d6, d, d5, d6, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, d, d5, d6, d, d5, d3, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, d, d2, d3, d, d5, d3, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, d4, d2, d3, d4, d5, d3, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, d4, d2, d6, d4, d5, d6, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, d, d2, d6, d, d5, d6, colorRGBA);
    }

    private void renderPredictionPoint(MatrixStack class_45872, BufferBuilder class_2872, Vec3d VanillaChestLootTableGenerator, ColorRGBA colorRGBA) {
        double d = 0.18;
        this.updatePredictionGeometry(class_45872, class_2872, VanillaChestLootTableGenerator.x - d, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z, VanillaChestLootTableGenerator.x + d, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y - d, VanillaChestLootTableGenerator.z, VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y + d, VanillaChestLootTableGenerator.z, colorRGBA);
        this.updatePredictionGeometry(class_45872, class_2872, VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z - d, VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z + d, colorRGBA);
    }

    private void updatePredictionGeometry(MatrixStack class_45872, BufferBuilder class_2872, double d, double d2, double d3, double d4, double d5, double d6, ColorRGBA colorRGBA) {
        RenderUtils.drawLineSegment(class_45872, class_2872, new Vec3d(d, d2, d3), new Vec3d(d4, d5, d6), colorRGBA);
    }
}
