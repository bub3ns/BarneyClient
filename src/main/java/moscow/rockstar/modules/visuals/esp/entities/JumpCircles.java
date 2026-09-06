/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  moscow.rockstar.modules.visuals.esp.entities.JumpCircles$JumpCircle
 *  moscow.rockstar.modules.visuals.esp.entities.JumpCircles$PlayerJumpState
 *  moscow.rockstar.render.esp.TargetRenderModule
 *  net.minecraft.PlayerEntity
 *  net.minecraft.BlockView
 *  net.minecraft.Blocks
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.VoxelShape
 *  net.minecraft.BlockState
 *  net.minecraft.MathHelper
 *  net.minecraft.Camera
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package moscow.rockstar.modules.visuals.esp.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.visuals.esp.entities.JumpCircles;
import moscow.rockstar.modules.visuals.esp.targeting.PlayerTargetGroup;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.esp.TargetRenderModule;
import moscow.rockstar.render.postprocess.JumpCirclePostProcessor;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.BlockView;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import pyrock.events.game.GameTickEvent;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public class JumpCircles
extends TargetRenderModule {
    private final BooleanSetting enabledSetting = this.createSetting("esp.jump_circles");
    private final BooleanSetting themeSyncSetting = (BooleanSetting)this.createSetting((targetRenderModule, booleanSetting) -> new BooleanSetting((SettingOwner)targetRenderModule, "theme.sync", () -> !booleanSetting.isEnabled()).enable());
    private final ColorSetting circleColorSetting = (ColorSetting)this.createSetting("theme.sync", (targetRenderModule, booleanSetting, booleanSetting2) -> new ColorSetting((SettingOwner)targetRenderModule, "esp.jump_circles.color", () -> !booleanSetting.isEnabled() || booleanSetting2.isEnabled()).setColor(ColorPalette.getAccentColor()));
    private final NumberSetting radiusSetting = (NumberSetting)this.createSetting((targetRenderModule, booleanSetting) -> new NumberSetting((SettingOwner)targetRenderModule, "esp.jump_circles.radius", () -> !booleanSetting.isEnabled()).setMinValue(0.5f).setMaxValue(2.5f).setStep(0.1f).setValue(1.5f));
    private final NumberSetting widthSetting = (NumberSetting)this.createSetting((targetRenderModule, booleanSetting) -> new NumberSetting((SettingOwner)targetRenderModule, "esp.jump_circles.width", () -> !booleanSetting.isEnabled()).setMinValue(0.05f).setMaxValue(1.5f).setStep(0.05f).setValue(1.0f));
    private final NumberSetting strengthSetting = (NumberSetting)this.createSetting((targetRenderModule, booleanSetting) -> new NumberSetting((SettingOwner)targetRenderModule, "esp.jump_circles.strength", () -> !booleanSetting.isEnabled()).setMinValue(0.1f).setMaxValue(2.0f).setStep(0.1f).setValue(2.0f));
    private final NumberSetting expandDurationSetting = (NumberSetting)this.createSetting((targetRenderModule, booleanSetting) -> new NumberSetting((SettingOwner)targetRenderModule, "esp.jump_circles.expand", () -> !booleanSetting.isEnabled()).setMinValue(100.0f).setMaxValue(800.0f).setStep(25.0f).setValue(800.0f));
    private final NumberSetting fadeDurationSetting = (NumberSetting)this.createSetting((targetRenderModule, booleanSetting) -> new NumberSetting((SettingOwner)targetRenderModule, "esp.jump_circles.fade", () -> !booleanSetting.isEnabled()).setMinValue(200.0f).setMaxValue(1500.0f).setStep(25.0f).setValue(1500.0f));
    private final CopyOnWriteArrayList<JumpCircle> jumpCircles = new CopyOnWriteArrayList();
    private final Map<UUID, PlayerJumpState> playerStates = new HashMap<UUID, PlayerJumpState>();
    private final JumpCirclePostProcessor postProcessor = new JumpCirclePostProcessor();
    private final EventListener<GameTickEvent> gameTickListener = gameTickEvent -> {
        if (ClientAccess.minecraftClient.world == null || ClientAccess.minecraftClient.player == null) {
            return;
        }
        if (!this.isValid()) {
            return;
        }
        HashSet<UUID> hashSet = new HashSet<UUID>();
        for (PlayerEntity class_16572 : ClientAccess.minecraftClient.world.getPlayers()) {
            PlayerTargetGroup playerTargetGroup;
            boolean bl;
            UUID uUID = class_16572.getUuid();
            hashSet.add(uUID);
            boolean bl2 = class_16572.isOnGround();
            double d = class_16572.getY();
            PlayerJumpState playerState = this.playerStates.get(uUID);
            if (playerState == null) {
                this.playerStates.put(uUID, new PlayerJumpState(bl2, class_16572.getX(), d, class_16572.getZ(), d));
                continue;
            }
            if (bl2) {
                playerState.lastGroundX = class_16572.getX();
                playerState.lastGroundY = d;
                playerState.lastGroundZ = class_16572.getZ();
            }
            boolean bl3 = bl = d - playerState.lastY > 0.02 || class_16572.getVelocity().y > 0.0;
            if (playerState.wasOnGround && !bl2 && bl && this.isValid2(playerTargetGroup = JumpCircles.getPlayerGroup(class_16572))) {
                this.addJumpCircle(playerTargetGroup, playerState.lastGroundX, this.findGroundHeight(playerState.lastGroundX, playerState.lastGroundY, playerState.lastGroundZ), playerState.lastGroundZ);
            }
            playerState.wasOnGround = bl2;
            playerState.lastY = d;
        }
        this.playerStates.keySet().retainAll(hashSet);
    };
    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> {
        this.jumpCircles.clear();
        this.playerStates.clear();
    };
    private final EventListener<Render3DEvent> render3DEventListener = render3DEvent -> {
        if (this.jumpCircles.isEmpty()) {
            return;
        }
        if (!this.isValid()) {
            this.removeExpiredCircles();
            return;
        }
        List<JumpCirclePostProcessor.JumpCircleInstance> list = this.buildRenderInstances(render3DEvent.getCamera());
        if (list.isEmpty()) {
            return;
        }
        Matrix4f matrix4f = new Matrix4f((Matrix4fc)render3DEvent.getProjectionMatrix()).mul((Matrix4fc)render3DEvent.getPositionMatrix()).invert();
        this.postProcessor.renderJumpCircles(matrix4f, list);
    };

    public JumpCircles() {
        super("jump_circles", new TargetGroup[]{TargetGroup.PLAYERS});
        this.enablePlayerGroup(PlayerTargetGroup.LOCAL_PLAYER);
        this.postProcessor.refreshRenderOutput();
    }

    private void addJumpCircle(PlayerTargetGroup playerTargetGroup, double d, double d2, double d3) {
        BooleanSetting booleanSetting = this.getSettingForScope("theme.sync", playerTargetGroup);
        ColorSetting colorSetting = this.getSettingForScope("esp.jump_circles.color", playerTargetGroup);
        NumberSetting numberSetting = this.getSettingForScope("esp.jump_circles.radius", playerTargetGroup);
        NumberSetting numberSetting2 = this.getSettingForScope("esp.jump_circles.width", playerTargetGroup);
        NumberSetting numberSetting3 = this.getSettingForScope("esp.jump_circles.strength", playerTargetGroup);
        NumberSetting numberSetting4 = this.getSettingForScope("esp.jump_circles.expand", playerTargetGroup);
        NumberSetting numberSetting5 = this.getSettingForScope("esp.jump_circles.fade", playerTargetGroup);
        if (numberSetting == null || numberSetting2 == null || numberSetting3 == null || numberSetting4 == null || numberSetting5 == null) {
            return;
        }
        ColorRGBA color = booleanSetting != null && booleanSetting.isEnabled()
            ? ColorPalette.getAccentColor()
            : colorSetting != null ? colorSetting.getColor() : null;
        if (color == null) {
            color = ColorPalette.getAccentColor();
        }
        int n = color.getRGB();
        float f = (float)(n >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(n & 0xFF) / 255.0f;
        float f4 = (float)(n >>> 24 & 0xFF) / 255.0f;
        this.jumpCircles.add(new JumpCircle(d, d2, d3, numberSetting.getValue(), numberSetting2.getValue(), (long)numberSetting4.getValue(), (long)numberSetting5.getValue(), numberSetting3.getValue(), f, f2, f3, f4));
    }

    private double findGroundHeight(double d, double d2, double d3) {
        if (ClientAccess.minecraftClient.world == null) {
            return d2 + 0.01;
        }
        BlockPos adminsky = BlockPos.ofFloored((double)d, (double)d2, (double)d3);
        double d4 = d - (double)adminsky.getX();
        double d5 = d3 - (double)adminsky.getZ();
        double d6 = Double.NEGATIVE_INFINITY;
        for (int i = adminsky.getY() + 1; i >= adminsky.getY() - 2; --i) {
            BlockPos adminsky2 = new BlockPos(adminsky.getX(), i, adminsky.getZ());
            BlockState class_26802 = ClientAccess.minecraftClient.world.getBlockState(adminsky2);
            VoxelShape class_2652 = class_26802.getOutlineShape(ClientAccess.minecraftClient.world, adminsky2);
            for (Box HorizontalFacingBlock : class_2652.getBoundingBoxes()) {
                boolean bl;
                if (d4 < HorizontalFacingBlock.minX || d4 > HorizontalFacingBlock.maxX || d5 < HorizontalFacingBlock.minZ || d5 > HorizontalFacingBlock.maxZ) continue;
                double d7 = (double)adminsky2.getY() + HorizontalFacingBlock.maxY;
                boolean bl2 = Math.abs(d7 - d2) <= 0.01;
                boolean bl3 = bl = class_26802.isOf(Blocks.SNOW) && d7 >= d2 - 0.01 && d7 <= d2 + 0.13;
                if (!bl2 && !bl || !(d7 > d6)) continue;
                d6 = d7;
            }
        }
        return (d6 == Double.NEGATIVE_INFINITY ? d2 : d6) + 0.01;
    }

    private void removeExpiredCircles() {
        long l = System.currentTimeMillis();
        this.jumpCircles.removeIf(circle -> circle.isExpired(l));
    }

    private List<JumpCirclePostProcessor.JumpCircleInstance> buildRenderInstances(Camera class_41842) {
        ArrayList<JumpCirclePostProcessor.JumpCircleInstance> arrayList = new ArrayList<JumpCirclePostProcessor.JumpCircleInstance>();
        long l = System.currentTimeMillis();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        this.jumpCircles.removeIf(circle -> circle.isExpired(l));
        for (JumpCircle circle : this.jumpCircles) {
            float f;
            float f2;
            float f3;
            float f4;
            if (arrayList.size() >= 12) break;
            long l2 = l - circle.createdAt;
            long l3 = circle.expandDurationMillis + circle.fadeDurationMillis;
            if (l3 <= 0L) continue;
            if (l2 < circle.expandDurationMillis) {
                f4 = (float)l2 / (float)circle.expandDurationMillis;
                f3 = 1.0f - (1.0f - f4) * (1.0f - f4) * (1.0f - f4);
                f2 = circle.radius * f3;
                f = f3;
            } else {
                f4 = MathHelper.clamp((float)((float)(l2 - circle.expandDurationMillis) / (float)circle.fadeDurationMillis), (float)0.0f, (float)1.0f);
                f2 = circle.radius * (1.0f + f4 * 0.18f);
                f3 = 1.0f - f4;
                f = f3 * f3;
            }
            if (f2 <= 0.001f || f <= 0.001f) continue;
            f4 = MathHelper.clamp((float)((float)l2 / (float)l3), (float)0.0f, (float)1.0f);
            f3 = circle.width * (1.0f - 0.5f * f4);
            float f5 = circle.alpha * f;
            float f6 = circle.strength * f;
            arrayList.add(new JumpCirclePostProcessor.JumpCircleInstance((float)(circle.x - VanillaChestLootTableGenerator.x), (float)(circle.y - VanillaChestLootTableGenerator.y), (float)(circle.z - VanillaChestLootTableGenerator.z), f2, f3, circle.red, circle.green, circle.blue, f5, f6));
        }
        return arrayList;
    }

    private static PlayerTargetGroup getPlayerGroup(PlayerEntity class_16572) {
        if (class_16572 == ClientAccess.minecraftClient.player) {
            return PlayerTargetGroup.LOCAL_PLAYER;
        }
        if (RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString())) {
            return PlayerTargetGroup.FRIENDS;
        }
        if (JumpCircles.isRockstarUser(class_16572)) {
            return PlayerTargetGroup.ROCKSTAR_USERS;
        }
        return PlayerTargetGroup.OTHERS;
    }

    private static boolean isRockstarUser(PlayerEntity player) {
        return RockstarClient.create().getFriendManager().isFriend(player.getName().getString());
    }

    @Generated
    public BooleanSetting getEnabledSetting() {
        return this.enabledSetting;
    }

    @Generated
    public BooleanSetting getThemeSyncSetting() {
        return this.themeSyncSetting;
    }

    @Generated
    public ColorSetting getCircleColorSetting() {
        return this.circleColorSetting;
    }

    @Generated
    public NumberSetting getRadiusSetting() {
        return this.radiusSetting;
    }

    @Generated
    public NumberSetting getWidthSetting() {
        return this.widthSetting;
    }

    @Generated
    public NumberSetting getStrengthSetting() {
        return this.strengthSetting;
    }

    @Generated
    public NumberSetting getExpandDurationSetting() {
        return this.expandDurationSetting;
    }

    @Generated
    public NumberSetting getFadeDurationSetting() {
        return this.fadeDurationSetting;
    }

    @Generated
    public CopyOnWriteArrayList<JumpCircle> getJumpCircles() {
        return this.jumpCircles;
    }

    @Generated
    public Map<UUID, PlayerJumpState> getPlayerStates() {
        return this.playerStates;
    }

    @Generated
    public JumpCirclePostProcessor getPostProcessor() {
        return this.postProcessor;
    }

    @Generated
    public EventListener<GameTickEvent> getGameTickListener() {
        return this.gameTickListener;
    }

    @Generated
    public EventListener<WorldChangeEvent> getWorldChangeListener() {
        return this.worldChangeListener;
    }

    @Generated
    public EventListener<Render3DEvent> getRender3DEventListener() {
        return this.render3DEventListener;
    }

    public static final class JumpCircle {
        private final double x;
        private final double y;
        private final double z;
        private final float radius;
        private final float width;
        private final long expandDurationMillis;
        private final long fadeDurationMillis;
        private final float strength;
        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;
        private final long createdAt;

        JumpCircle(double x, double y, double z, float radius, float width, long expandDurationMillis, long fadeDurationMillis, float strength, float red, float green, float blue, float alpha) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
            this.width = width;
            this.expandDurationMillis = expandDurationMillis;
            this.fadeDurationMillis = fadeDurationMillis;
            this.strength = strength;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired(long now) {
            return now - this.createdAt > this.expandDurationMillis + this.fadeDurationMillis;
        }
    }

    public static final class PlayerJumpState {
        private boolean wasOnGround;
        private double lastGroundX;
        private double lastGroundY;
        private double lastGroundZ;
        private double lastY;

        PlayerJumpState(boolean wasOnGround, double lastGroundX, double lastGroundY, double lastGroundZ, double lastY) {
            this.wasOnGround = wasOnGround;
            this.lastGroundX = lastGroundX;
            this.lastGroundY = lastGroundY;
            this.lastGroundZ = lastGroundZ;
            this.lastY = lastY;
        }
    }
}
