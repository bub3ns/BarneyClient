/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Entity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.MathHelper
 *  net.minecraft.MatrixStack
 *  net.minecraft.PlayerListEntry
 *  net.minecraft.BuiltBuffer
 */
package moscow.rockstar.modules.combat.targeting;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.events.network.ServerTickRateTracker;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.targeting.BacktrackPoint;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.modules.combat.targeting.BacktrackAccess;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.BuiltBuffer;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Back Track", description="modules.descriptions.back_track", category=ModuleCategory.COMBAT)
public class BackTrack
extends Module {
    private BooleanSetting visual;
    private BooleanSetting autoreset;
    private BooleanSetting pingBased;
    private BooleanSetting tpsBased;
    private NumberSetting delay;
    private NumberSetting pingMultiplier;
    private NumberSetting minTPS;
    private static final double MAX_BACKTRACK_SECONDS = 6.0;
    private static final double MAX_RENDER_DISTANCE = 180.0;
    private static final double PING_MULTIPLIER = 0.6;
    private static final double TICK_MULTIPLIER = 0.4;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        if (BackTrack.minecraftClient.world == null || BackTrack.minecraftClient.player == null) {
            return;
        }
        long l = System.currentTimeMillis();
        boolean bl = this.isTpsBelowMinimum();
        for (Entity class_12972 : BackTrack.minecraftClient.world.getEntities()) {
            if (!(class_12972 instanceof BacktrackAccess)) continue;
            BacktrackAccess backtrackAccess = (BacktrackAccess)class_12972;
            List<BacktrackPoint> list = backtrackAccess.rockstar2_0$getBackTracks();
            if (bl) {
                this.recordBacktrackHistory(class_12972, list, l);
                continue;
            }
            if (!this.isEntityValid(class_12972)) {
                list.clear();
                continue;
            }
            this.recordBacktrackHistory(class_12972, list, l);
        }
    };
    private final EventListener<Render3DEvent> onRender3DEvent = render3DEvent -> {
        if (!this.visual.isEnabled()) {
            return;
        }
        if (BackTrack.minecraftClient.world == null || BackTrack.minecraftClient.player == null) {
            return;
        }
        if (this.isTpsBelowMinimum()) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Vec3d VanillaChestLootTableGenerator = BackTrack.minecraftClient.gameRenderer.getCamera().getPos();
        for (PlayerEntity class_16572 : BackTrack.minecraftClient.world.getPlayers()) {
            Vec3d WallPlayerSkullBlock;
            List<BacktrackPoint> list;
            if (class_16572 == BackTrack.minecraftClient.player || RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString()) || !(class_16572 instanceof BacktrackAccess) || (list = ((BacktrackAccess)class_16572).rockstar2_0$getBackTracks()).isEmpty()) continue;
            long l = System.currentTimeMillis();
            this.recordBacktrackHistory((Entity)class_16572, list, l);
            if (list.isEmpty() || (WallPlayerSkullBlock = this.getPosition(class_16572, list)) == null) continue;
            BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            class_45872.push();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderUtils.drawBoxOutline(class_45872, class_2872, class_16572.getBoundingBox().offset(WallPlayerSkullBlock.subtract(class_16572.getPos())).offset(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z), ColorRGBA.WHITE.withAlpha(180.0f));
            BuiltBuffer class_98012 = class_2872.endNullable();
            if (class_98012 != null) {
                BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
            }
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            class_45872.pop();
        }
    };

    public BackTrack() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.visual = new BooleanSetting(this, "modules.settings.backtrack.visual");
        this.autoreset = new BooleanSetting(this, "modules.settings.backtrack.autoreset");
        this.pingBased = new BooleanSetting(this, "Ping Based");
        this.tpsBased = new BooleanSetting(this, "TPS Based");
        this.delay = new NumberSetting(this, "Delay").setUnit("ms").setMinValue(50.0f).setMaxValue(1200.0f).setStep(25.0f).setValue(150.0f);
        this.pingMultiplier = new NumberSetting((SettingOwner)this, "Ping Multiplier", () -> !this.pingBased.isEnabled()).setUnit("x").setMinValue(0.6f).setMaxValue(2.0f).setStep(0.1f).setValue(1.1f);
        this.minTPS = new NumberSetting((SettingOwner)this, "Min TPS", () -> !this.tpsBased.isEnabled()).setUnit("").setMinValue(14.0f).setMaxValue(20.0f).setStep(0.5f).setValue(17.0f);
    }

    private int getClientTick() {
        if (minecraftClient.getNetworkHandler() == null || BackTrack.minecraftClient.player == null) {
            return 0;
        }
        PlayerListEntry ServerSamplerSource = minecraftClient.getNetworkHandler().getPlayerListEntry(BackTrack.minecraftClient.player.getUuid());
        return ServerSamplerSource != null ? ServerSamplerSource.getLatency() : 0;
    }

    private int getPlayerTick(PlayerEntity class_16572) {
        if (minecraftClient.getNetworkHandler() == null) {
            return 0;
        }
        PlayerListEntry ServerSamplerSource = minecraftClient.getNetworkHandler().getPlayerListEntry(class_16572.getUuid());
        return ServerSamplerSource != null ? ServerSamplerSource.getLatency() : 0;
    }

    private float getServerTps() {
        ServerTickRateTracker tracker = RockstarClient.create().getServerTickRateTracker();
        return tracker != null ? tracker.getTicksPerSecond() : 20.0f;
    }

    private boolean isTpsBelowMinimum() {
        return this.tpsBased.isEnabled() && this.getServerTps() < this.minTPS.getValue();
    }

    private long getEntityTimestamp(Entity class_12972) {
        float f;
        long l = (long)this.delay.getValue();
        if (this.pingBased.isEnabled() && class_12972 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            int n = this.getClientTick() + this.getPlayerTick(class_16572);
            long l2 = (long)((float)n * this.pingMultiplier.getValue());
            l = Math.clamp(l2, (long)this.delay.getMinValue(), (long)this.delay.getMaxValue());
        }
        if (this.tpsBased.isEnabled() && (f = this.getServerTps()) > 0.0f) {
            float f2 = MathHelper.clamp((float)(20.0f / f), (float)1.0f, (float)(this.delay.getMaxValue() / Math.max(this.delay.getMinValue(), (float)l)));
            l = (long)((float)l * f2);
        }
        return Math.clamp(l, (long)this.delay.getMinValue(), (long)this.delay.getMaxValue());
    }

    private Vec3d getPosition(PlayerEntity class_16572, List<BacktrackPoint> list) {
        if (list.isEmpty()) {
            return null;
        }
        Vec3d VanillaChestLootTableGenerator = list.getLast().getPosition();
        if (!this.autoreset.isEnabled()) {
            return VanillaChestLootTableGenerator;
        }
        Vec3d WallPlayerSkullBlock = BackTrack.minecraftClient.player.getEyePos();
        Vec3d VanillaEntityLootTableGenerator = class_16572.getPos();
        Box HorizontalFacingBlock = class_16572.getBoundingBox();
        double d = this.calculateBacktrackOffset(WallPlayerSkullBlock, HorizontalFacingBlock);
        for (int i = list.size() - 1; i >= 0; --i) {
            BacktrackPoint backtrackPoint = list.get(i);
            Box InfestedBlock = HorizontalFacingBlock.offset(backtrackPoint.getPosition().subtract(VanillaEntityLootTableGenerator));
            double d2 = this.calculateBacktrackOffset(WallPlayerSkullBlock, InfestedBlock);
            if (!(d2 < d)) continue;
            return backtrackPoint.getPosition();
        }
        return null;
    }

    private double calculateBacktrackOffset(Vec3d VanillaChestLootTableGenerator, Box HorizontalFacingBlock) {
        double d = this.calculateHorizontalOffset(VanillaChestLootTableGenerator, HorizontalFacingBlock);
        double d2 = this.calculateVerticalOffset(VanillaChestLootTableGenerator, HorizontalFacingBlock);
        double d3 = Math.min(d / 6.0, 1.0);
        double d4 = Math.min(d2 / 180.0, 1.0);
        return d3 * 0.6 + d4 * 0.4;
    }

    private double calculateHorizontalOffset(Vec3d VanillaChestLootTableGenerator, Box HorizontalFacingBlock) {
        double d = MathHelper.clamp((double)VanillaChestLootTableGenerator.x, (double)HorizontalFacingBlock.minX, (double)HorizontalFacingBlock.maxX);
        double d2 = MathHelper.clamp((double)VanillaChestLootTableGenerator.y, (double)HorizontalFacingBlock.minY, (double)HorizontalFacingBlock.maxY);
        double d3 = MathHelper.clamp((double)VanillaChestLootTableGenerator.z, (double)HorizontalFacingBlock.minZ, (double)HorizontalFacingBlock.maxZ);
        return VanillaChestLootTableGenerator.distanceTo(new Vec3d(d, d2, d3));
    }

    private double calculateVerticalOffset(Vec3d VanillaChestLootTableGenerator, Box HorizontalFacingBlock) {
        float f = BackTrack.minecraftClient.player.getYaw();
        float f2 = BackTrack.minecraftClient.player.getPitch();
        Vec3d WallPlayerSkullBlock = this.getPosition(f2, f);
        Vec3d VanillaEntityLootTableGenerator = this.getPosition(VanillaChestLootTableGenerator, HorizontalFacingBlock);
        Vec3d PlayerSkullBlock = VanillaEntityLootTableGenerator.subtract(VanillaChestLootTableGenerator).normalize();
        double d = WallPlayerSkullBlock.dotProduct(PlayerSkullBlock);
        d = MathHelper.clamp((double)d, (double)-1.0, (double)1.0);
        return Math.toDegrees(Math.acos(d));
    }

    private Vec3d getPosition(Vec3d VanillaChestLootTableGenerator, Box HorizontalFacingBlock) {
        double d = MathHelper.clamp((double)VanillaChestLootTableGenerator.x, (double)HorizontalFacingBlock.minX, (double)HorizontalFacingBlock.maxX);
        double d2 = MathHelper.clamp((double)VanillaChestLootTableGenerator.y, (double)HorizontalFacingBlock.minY, (double)HorizontalFacingBlock.maxY);
        double d3 = MathHelper.clamp((double)VanillaChestLootTableGenerator.z, (double)HorizontalFacingBlock.minZ, (double)HorizontalFacingBlock.maxZ);
        return new Vec3d(d, d2, d3);
    }

    private Vec3d getPosition(float f, float f2) {
        float f3 = (float)Math.toRadians(f);
        float f4 = (float)Math.toRadians(f2);
        float f5 = MathHelper.cos((float)(-f4 - (float)Math.PI));
        float f6 = MathHelper.sin((float)(-f4 - (float)Math.PI));
        float f7 = MathHelper.cos((float)(-f3));
        float f8 = MathHelper.sin((float)(-f3));
        return new Vec3d((double)(f6 * f7), (double)f8, (double)(f5 * f7));
    }

    public Vec3d getPosition(Entity class_12972) {
        if (!this.isEnabled()) {
            return null;
        }
        if (BackTrack.minecraftClient.player == null) {
            return null;
        }
        if (this.isTpsBelowMinimum()) {
            return null;
        }
        if (!(class_12972 instanceof BacktrackAccess)) {
            return null;
        }
        BacktrackAccess backtrackAccess = (BacktrackAccess)class_12972;
        if (!(class_12972 instanceof PlayerEntity)) {
            return null;
        }
        PlayerEntity class_16572 = (PlayerEntity)class_12972;
        if (!this.isEntityValid(class_12972)) {
            return null;
        }
        List<BacktrackPoint> list = backtrackAccess.rockstar2_0$getBackTracks();
        if (list.isEmpty()) {
            return null;
        }
        long l = System.currentTimeMillis();
        this.recordBacktrackHistory(class_12972, list, l);
        if (list.isEmpty()) {
            return null;
        }
        return this.getPosition(class_16572, list);
    }

    @Override
    public void onDisable() {
        this.clearBacktrackHistory();
    }

    public boolean isEntityValid(Entity class_12972) {
        if (!this.isEnabled()) {
            return false;
        }
        if (BackTrack.minecraftClient.player == null) {
            return false;
        }
        if (!(class_12972 instanceof PlayerEntity)) {
            return false;
        }
        PlayerEntity class_16572 = (PlayerEntity)class_12972;
        if (class_16572 == BackTrack.minecraftClient.player) {
            return false;
        }
        if (RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString())) {
            return false;
        }
        return !this.isTpsBelowMinimum();
    }

    public void recordBacktrackPoint(Entity class_12972, Vec3d VanillaChestLootTableGenerator, long l) {
        if (!this.isEntityValid(class_12972)) {
            return;
        }
        if (!(class_12972 instanceof BacktrackAccess)) {
            return;
        }
        BacktrackAccess backtrackAccess = (BacktrackAccess)class_12972;
        List<BacktrackPoint> list = backtrackAccess.rockstar2_0$getBackTracks();
        this.recordBacktrackHistory(class_12972, list, l);
        list.add(new BacktrackPoint(VanillaChestLootTableGenerator, l));
    }

    public void resetBacktrackHistory(Entity class_12972, Vec3d VanillaChestLootTableGenerator, long l) {
        if (!(class_12972 instanceof BacktrackAccess)) {
            return;
        }
        BacktrackAccess backtrackAccess = (BacktrackAccess)class_12972;
        List<BacktrackPoint> list = backtrackAccess.rockstar2_0$getBackTracks();
        list.clear();
        if (this.isEntityValid(class_12972)) {
            list.add(new BacktrackPoint(VanillaChestLootTableGenerator, l));
        }
    }

    private void recordBacktrackHistory(Entity class_12972, List<BacktrackPoint> list, long l) {
        long l2 = this.getEntityTimestamp(class_12972);
        list.removeIf(backtrackPoint -> l - backtrackPoint.getTime() > l2);
    }

    private void clearBacktrackHistory() {
        if (BackTrack.minecraftClient.world == null) {
            return;
        }
        for (Entity class_12972 : BackTrack.minecraftClient.world.getEntities()) {
            if (!(class_12972 instanceof BacktrackAccess)) continue;
            ((BacktrackAccess)class_12972).rockstar2_0$getBackTracks().clear();
        }
    }
}
