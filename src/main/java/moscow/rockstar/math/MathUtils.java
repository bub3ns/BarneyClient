/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.BlockView
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.Direction
 *  net.minecraft.Position
 *  net.minecraft.Box
 *  net.minecraft.HitResult
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.TrapdoorBlock
 *  net.minecraft.VoxelShape
 *  net.minecraft.BlockState
 *  net.minecraft.BossBarHud
 *  net.minecraft.ClientBossBar
 *  net.minecraft.MathHelper
 *  net.minecraft.ShapeContext
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 */
package moscow.rockstar.math;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Generated;
import moscow.rockstar.combat.WallMode;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.tracking.EntityPositionCache;
import moscow.rockstar.modules.combat.attacks.Aura;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.network.http.UrlEncodedFormBody;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.ShapeContext;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import ua.mintantileak.spk.Compile;

public final class MathUtils
implements ClientAccess {
    public static SecureRandom RANDOM = new SecureRandom();
    private static final int SINE_TABLE_SIZE = 65536;
    private static final double TWO_PI = Math.PI * 2;
    private static final double[] SINE_TABLE = new double[65536];
    private static final DecimalFormatSymbols US_DECIMAL_SYMBOLS;
    private static final DecimalFormat NUMBER_FORMAT;

    @Compile(obfuscation=1)
    public static double lookupSine(double d) {
        int n = (int)(d * 10430.378350470453) & 0xFFFF;
        return SINE_TABLE[n];
    }

    @Compile(obfuscation=1)
    public static double lookupCosine(double d) {
        int n = (int)(d * 10430.378350470453 + 16384.0) & 0xFFFF;
        return SINE_TABLE[n];
    }

    @Compile(obfuscation=1)
    public static float interpolateRandomAverage(float f, float f2) {
        float f3 = RANDOM.nextFloat();
        float f4 = RANDOM.nextFloat();
        return f + (f2 - f) * ((f3 + f4) / 2.0f);
    }

    @Compile(obfuscation=1)
    public static float interpolateBiasedRandom(float f, float f2) {
        double d = RANDOM.nextDouble();
        double d2 = RANDOM.nextDouble();
        double d3 = RANDOM.nextGaussian() * (double)0.02f;
        double d4 = Math.pow(d, 1.0 + RANDOM.nextDouble() * 0.7);
        double d5 = (d2 * 0.8 + 0.1) * (Math.log1p(d * 3.0) * 0.5 + 0.5);
        return (float)((double)f + (double)(f2 - f) * d4 * d5 + d3);
    }

    @Compile(obfuscation=1)
    public static float interpolateMathRandom(float f, float f2) {
        return f + (f2 - f) * (float)Math.random();
    }

    @Compile(obfuscation=1)
    public static float interpolateSecureRandom(float f, float f2) {
        return f + (f2 - f) * RANDOM.nextFloat();
    }

    @Compile(obfuscation=1)
    public static float interpolateRandomStrategy(float f, float f2) {
        switch (RANDOM.nextInt(4)) {
            case 0: {
                return MathUtils.interpolateRandomAverage(f, f2);
            }
            case 1: {
                return MathUtils.interpolateBiasedRandom(f, f2);
            }
            case 2: {
                return MathUtils.interpolateMathRandom(f, f2);
            }
        }
        return MathUtils.interpolateSecureRandom(f, f2);
    }

    @Compile(obfuscation=1)
    public static float sampleGaussianAverage(float f, float f2) {
        float f3 = (float)RANDOM.nextGaussian() * f2 + f;
        float f4 = (float)RANDOM.nextGaussian() * f2 + f;
        return (f3 + f4) / 2.0f;
    }

    @Compile(obfuscation=1)
    public static float sampleBiasedGaussian(float f, float f2) {
        double d = RANDOM.nextGaussian() * (double)f2 + (double)f;
        double d2 = RANDOM.nextGaussian();
        double d3 = RANDOM.nextGaussian() * 0.02;
        double d4 = Math.pow(Math.abs(d2), 1.0 + RANDOM.nextDouble() * 0.7);
        double d5 = (Math.abs(d2) * 0.8 + 0.1) * (Math.log1p(Math.abs(d - (double)f) * 3.0) * 0.5 + 0.5);
        return (float)((double)f + (double)f2 * d4 * d5 + d3);
    }

    @Compile(obfuscation=1)
    public static float sampleGaussianCentered(float f, float f2) {
        return (float)RANDOM.nextGaussian() * f2 + f;
    }

    @Compile(obfuscation=1)
    public static float sampleGaussianCenteredAlternate(float f, float f2) {
        return (float)RANDOM.nextGaussian() * f2 + f;
    }

    @Compile(obfuscation=1)
    public static float sampleGaussianStrategy(float f, float f2) {
        switch (RANDOM.nextInt(4)) {
            case 0: {
                return MathUtils.sampleGaussianAverage(f, f2);
            }
            case 1: {
                return MathUtils.sampleBiasedGaussian(f, f2);
            }
            case 2: {
                return MathUtils.sampleGaussianCentered(f, f2);
            }
        }
        return MathUtils.sampleGaussianCenteredAlternate(f, f2);
    }

    @Compile(obfuscation=1)
    public static float interpolateRandomDouble(double d, double d2) {
        return (float)(d + (d2 - d) * Math.random());
    }

    @Compile(obfuscation=1)
    public static double interpolateCubicBezier(double d, double d2, double d3, double d4, double d5) {
        return Math.pow(1.0 - d, 3.0) * d2 + 3.0 * d * Math.pow(1.0 - d, 2.0) * d3 + 3.0 * Math.pow(d, 2.0) * (1.0 - d) * d4 + Math.pow(d, 3.0) * d5;
    }

    @Compile(obfuscation=1)
    public static boolean hasClearLineOfSight(Vec3d VanillaChestLootTableGenerator) {
        return MathUtils.minecraftClient.world.raycast(new RaycastContext(MathUtils.minecraftClient.player.getEyePos(), VanillaChestLootTableGenerator, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)MathUtils.minecraftClient.player)).getType() == HitResult.Type.MISS;
    }

    @Compile(obfuscation=1)
    public static boolean hasClearPathToPosition(Vec3d VanillaChestLootTableGenerator) {
        Object object3;
        Vec3d WallPlayerSkullBlock = MathUtils.minecraftClient.player.getEyePos();
        Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator.subtract(WallPlayerSkullBlock);
        double d = VanillaEntityLootTableGenerator.length();
        VanillaEntityLootTableGenerator = VanillaEntityLootTableGenerator.normalize();
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        int n = 0;
        double d2 = 0.25;
        for (double d3 = 0.0; d3 <= d; d3 += d2) {
            object3 = WallPlayerSkullBlock.add(VanillaEntityLootTableGenerator.multiply(d3));
            BlockPos blockPosition = BlockPos.ofFloored((Position)object3);
            if (hashSet.contains(blockPosition)) continue;
            hashSet.add(blockPosition);
            BlockState blockState = MathUtils.minecraftClient.world.getBlockState(blockPosition);
            if (blockState.isAir()) continue;
            VoxelShape collisionShape = blockState.getCollisionShape((BlockView)MathUtils.minecraftClient.world, blockPosition);
            if (blockState.isOf(Blocks.GLASS) || blockState.isOf(Blocks.GLASS_PANE) || blockState.getBlock() instanceof TrapdoorBlock || collisionShape.isEmpty()) continue;
            ++n;
        }
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        BossBarHud SimpleRoomFactory = MathUtils.minecraftClient.inGameHud.getBossBarHud();
        if (SimpleRoomFactory != null) {
            try {
                Field bossBarsField = BossBarHud.class.getDeclaredField("bossBars");
                bossBarsField.setAccessible(true);
                Map<UUID, ClientBossBar> bossBars = (Map<UUID, ClientBossBar>)bossBarsField.get(SimpleRoomFactory);
                for (ClientBossBar bossBar : bossBars.values()) {
                    List<net.minecraft.text.Text> siblings = bossBar.getName().getSiblings();
                    siblings.stream().allMatch(sibling -> {
                        if (sibling.getString().contains("\ub8f3\ua223\ua203\ub8f2\ua223\ua205")) {
                            atomicBoolean.set(true);
                        }
                        return true;
                    });
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return n <= (atomicBoolean.get() ? 3 : (MathUtils.minecraftClient.player.getInventory().selectedSlot == 0 ? 2 : 1));
    }

    @Compile(obfuscation=1)
    public static int levenshteinDistance(String string, String string2) {
        int n;
        int n2 = string.length();
        int n3 = string2.length();
        int[] nArray = new int[n3 + 1];
        for (n = 0; n <= n3; ++n) {
            nArray[n] = n;
        }
        for (n = 1; n <= n2; ++n) {
            int n4 = nArray[0];
            nArray[0] = n;
            for (int i = 1; i <= n3; ++i) {
                int n5 = nArray[i];
                int n6 = string.charAt(n - 1) == string2.charAt(i - 1) ? 0 : 1;
                nArray[i] = Math.min(Math.min(nArray[i] + 1, nArray[i - 1] + 1), n4 + n6);
                n4 = n5;
            }
        }
        return nArray[n3];
    }

    @Compile(obfuscation=1)
    public static float interpolateDouble(double d, double d2, double d3) {
        return (float)(d + (d2 - d) * d3);
    }

    @Compile(obfuscation=1)
    public static HitResult raycastFromCamera(double d, float f, float f2, Entity class_12972) {
        Vec3d VanillaChestLootTableGenerator = MathUtils.minecraftClient.player.getCameraPosVec(1.0f);
        Vec3d WallPlayerSkullBlock = MathUtils.directionFromYawPitch(f2, f);
        Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock.x * d, WallPlayerSkullBlock.y * d, WallPlayerSkullBlock.z * d);
        return MathUtils.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, VanillaEntityLootTableGenerator, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, class_12972));
    }

    @Compile(obfuscation=1)
    private static boolean isHitboxVisible(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, Entity class_12972, Entity class_12973) {
        Box Vec3i = class_12972.getBoundingBox().offset(-class_12972.getX(), -class_12972.getY(), -class_12972.getZ()).offset(EntityPositionCache.getTrackedPosition(class_12972));
        Box HorizontalFacingBlock = class_12972.getBoundingBox();
        if (Vec3i.raycast(VanillaChestLootTableGenerator, WallPlayerSkullBlock).isPresent() && RockstarClient.create().getModuleRegistry().getModule(Aura.class).getResolverOption().isSelected()) {
            return true;
        }
        return HorizontalFacingBlock.raycast(VanillaChestLootTableGenerator, WallPlayerSkullBlock).isPresent();
    }

    @Compile(obfuscation=1)
    public static BlockHitResult raycastThroughWallMode(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, Entity class_12972, WallMode wallMode) {
        if (MathUtils.minecraftClient.world == null || wallMode.usesDirectRaycast()) {
            return null;
        }
        Vec3d VanillaEntityLootTableGenerator = WallPlayerSkullBlock.subtract(VanillaChestLootTableGenerator);
        double d = VanillaEntityLootTableGenerator.lengthSquared();
        if (d < 1.0E-8) {
            return null;
        }
        BlockHitResult class_39652 = MathUtils.raycastBlockForWallMode(VanillaChestLootTableGenerator, class_12972, wallMode);
        if (class_39652 != null) {
            return class_39652;
        }
        Vec3d PlayerSkullBlock = VanillaEntityLootTableGenerator.normalize().multiply(0.01);
        Vec3d RedstoneBlock = VanillaChestLootTableGenerator;
        for (int i = 0; i < 40; ++i) {
            BlockHitResult class_39653 = MathUtils.minecraftClient.world.raycast(new RaycastContext(RedstoneBlock, WallPlayerSkullBlock, wallMode.getRaycastShape(), RaycastContext.FluidHandling.NONE, class_12972));
            if (class_39653 == null || class_39653.getType() != HitResult.Type.BLOCK) {
                return null;
            }
            BlockHitResult class_39654 = class_39653;
            BlockPos adminsky = class_39654.getBlockPos();
            if (!wallMode.shouldIgnoreBlock((BlockView)MathUtils.minecraftClient.world, adminsky, MathUtils.minecraftClient.world.getBlockState(adminsky))) {
                return class_39654;
            }
            RedstoneBlock = class_39654.getPos().add(PlayerSkullBlock);
            if (!(RedstoneBlock.squaredDistanceTo(VanillaChestLootTableGenerator) >= d)) continue;
            return null;
        }
        return null;
    }

    private static BlockHitResult raycastBlockForWallMode(Vec3d VanillaChestLootTableGenerator, Entity class_12972, WallMode wallMode) {
        if (wallMode.getRaycastShape() != RaycastContext.ShapeType.OUTLINE) {
            return null;
        }
        BlockPos adminsky = BlockPos.ofFloored((Position)VanillaChestLootTableGenerator);
        BlockState class_26802 = MathUtils.minecraftClient.world.getBlockState(adminsky);
        if (class_26802.isAir() || wallMode.shouldIgnoreBlock((BlockView)MathUtils.minecraftClient.world, adminsky, class_26802)) {
            return null;
        }
        VoxelShape class_2652 = class_26802.getOutlineShape((BlockView)MathUtils.minecraftClient.world, adminsky, ShapeContext.of((Entity)class_12972));
        if (class_2652.isEmpty()) {
            return null;
        }
        return new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky, true);
    }

    @Compile(obfuscation=1)
    public static boolean isVisibleThroughWallMode(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, Entity class_12972, WallMode wallMode) {
        return MathUtils.raycastThroughWallMode(VanillaChestLootTableGenerator, WallPlayerSkullBlock, class_12972, wallMode) == null;
    }

    @Compile(obfuscation=1)
    public static boolean isRotationPathClear(double d, float f, float f2, Entity class_12972, Entity class_12973, WallMode wallMode) {
        if (class_12973 == null || class_12972 == null || MathUtils.minecraftClient.world == null) {
            return false;
        }
        float f3 = minecraftClient.getRenderTickCounter().getTickDelta(false);
        Vec3d VanillaChestLootTableGenerator = class_12972.getCameraPosVec(f3);
        Vec3d WallPlayerSkullBlock = MathUtils.directionFromYawPitch(f2, f);
        Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock.multiply(d));
        if (!wallMode.usesDirectRaycast()) {
            double d2;
            double d3;
            Box Vec3i = class_12973.getBoundingBox().offset(-class_12973.getX(), -class_12973.getY(), -class_12973.getZ()).offset(EntityPositionCache.getTrackedPosition(class_12973));
            Box HorizontalFacingBlock = class_12973.getBoundingBox();
            if (HorizontalFacingBlock.contains(MathUtils.minecraftClient.player.getEyePos())) {
                return true;
            }
            if (Vec3i.contains(MathUtils.minecraftClient.player.getEyePos()) && RockstarClient.create().getModuleRegistry().getModule(Aura.class).getResolverOption().isSelected()) {
                return true;
            }
            BlockHitResult class_39652 = MathUtils.raycastThroughWallMode(VanillaChestLootTableGenerator, VanillaEntityLootTableGenerator, class_12972, wallMode);
            if (class_39652 != null && (d3 = class_39652.getPos().distanceTo(VanillaChestLootTableGenerator)) < (d2 = class_12973.getEyePos().distanceTo(VanillaChestLootTableGenerator))) {
                return false;
            }
        } else {
            double d4;
            double d5;
            BlockHitResult class_39653 = MathUtils.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, VanillaEntityLootTableGenerator, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, class_12972));
            if (class_39653 == null || class_39653.getType() != HitResult.Type.BLOCK || (d5 = class_39653.getPos().distanceTo(VanillaChestLootTableGenerator)) < (d4 = class_12973.getEyePos().distanceTo(VanillaChestLootTableGenerator))) {
                // empty if block
            }
        }
        return MathUtils.isHitboxVisible(VanillaChestLootTableGenerator, VanillaEntityLootTableGenerator, class_12973, (Entity)MathUtils.minecraftClient.player);
    }

    @Compile(obfuscation=1)
    public static Vec3d directionFromYawPitch(float f, float f2) {
        float f3 = -f2 * ((float)Math.PI / 180) - (float)Math.PI;
        float f4 = -f * ((float)Math.PI / 180);
        float f5 = MathHelper.cos((float)f3);
        float f6 = MathHelper.sin((float)f3);
        float f7 = -MathHelper.cos((float)f4);
        float f8 = MathHelper.sin((float)f4);
        return new Vec3d((double)(f6 * f7), (double)f8, (double)(f5 * f7));
    }

    @Compile(obfuscation=1)
    public static float wrapAngleDifference(float f, float f2) {
        float f3 = (f - f2) % 360.0f;
        if (f3 < -180.0f) {
            f3 += 360.0f;
        } else if (f3 > 180.0f) {
            f3 -= 360.0f;
        }
        return f3;
    }

    @Compile(obfuscation=1)
    public static String parseNumericExpression(String string) {
        if ((string = string.replaceAll("\\s+", "")).isEmpty()) {
            return "";
        }
        try {
            double value = Double.parseDouble(string);
            return String.valueOf(value);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            illegalArgumentException.printStackTrace();
            return string;
        }
    }

    @Compile(obfuscation=1)
    public static float sampleSineInterpolation(float f) {
        int n = (int)Math.floor(f) & 0xFF;
        float f2 = f - (float)Math.floor(f);
        float f3 = MathUtils.smoothstep(f2);
        float f4 = MathUtils.hashToSignedUnit(n);
        float f5 = MathUtils.hashToSignedUnit(n + 1);
        return MathUtils.interpolate(f4, f5, f3);
    }

    @Compile(obfuscation=1)
    public static int randomIntegerInclusive(int n, int n2) {
        if (n > n2) {
            throw new IllegalArgumentException("min must be less than or equal to max");
        }
        return n + RANDOM.nextInt(n2 - n + 1);
    }

    @Compile(obfuscation=1)
    private static float smoothstep(float f) {
        return f * f * f * (f * (f * 6.0f - 15.0f) + 10.0f);
    }

    @Compile(obfuscation=1)
    public static float interpolate(float f, float f2, float f3) {
        return f + (f2 - f) * f3;
    }

    @Compile(obfuscation=1)
    private static float hashToSignedUnit(int n) {
        n = n ^ 0x3D ^ n >> 16;
        n += n << 3;
        n ^= n >> 4;
        n *= 668265261;
        n ^= n >> 15;
        return (float)(n & Integer.MAX_VALUE) / 2.1474836E9f * 2.0f - 1.0f;
    }

    @Compile(obfuscation=1)
    public static String formatCurrencyAmount(long l) {
        return NUMBER_FORMAT.format(l) + "$";
    }

    @Generated
    private MathUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        for (int i = 0; i < 65536; ++i) {
            MathUtils.SINE_TABLE[i] = Math.sin((double)i * (Math.PI * 2) / 65536.0);
        }
        US_DECIMAL_SYMBOLS = new DecimalFormatSymbols(Locale.US);
        NUMBER_FORMAT = new DecimalFormat("#,###", US_DECIMAL_SYMBOLS);
    }
}
