/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.StatusEffect
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.AxeItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.SwordItem
 *  net.minecraft.TridentItem
 *  net.minecraft.World
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.Position
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.ScoreboardObjective
 *  net.minecraft.ScoreboardCriterion$RenderType
 *  net.minecraft.RegistryEntry
 *  net.minecraft.ScoreboardDisplaySlot
 *  net.minecraft.ReadableScoreboardScore
 *  net.minecraft.ScoreHolder
 *  net.minecraft.NumberFormat
 *  net.minecraft.StyledNumberFormat
 *  net.minecraft.MaceItem
 */
package moscow.rockstar.entity.utility;

import java.util.Locale;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.mixin.accessors.EntityMovementMultiplierAccessor;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.item.MaceItem;

public final class EntityUtils
implements ClientAccess {
    private static float movementFactor = 1.0f;

    public static void resetMovementFactor() {
        movementFactor = 1.0f;
    }

    public static Block getPlayerBlock() {
        return EntityUtils.getBlockAtOffset(0.0, 0.0, 0.0);
    }

    public static Block getBlockAtOffset(double d, double d2, double d3) {
        return !EntityUtils.isClientWorldReady() ? Blocks.AIR : EntityUtils.minecraftClient.world.getBlockState(BlockPos.ofFloored((Position)EntityUtils.minecraftClient.player.getPos().add(d, d2, d3))).getBlock();
    }

    public static boolean isClearAtHeight(double d) {
        return EntityUtils.getBlockAtOffset(0.3, d, 0.3) != Blocks.AIR || EntityUtils.getBlockAtOffset(-0.3, d, 0.3) != Blocks.AIR || EntityUtils.getBlockAtOffset(0.3, d, -0.3) != Blocks.AIR || EntityUtils.getBlockAtOffset(-0.3, d, -0.3) != Blocks.AIR;
    }

    public static boolean isOverlappingPlayer(LivingEntity class_13092) {
        return EntityUtils.isEntityOverlapping(class_13092, 0.0f);
    }

    public static boolean isEntityOverlapping(LivingEntity class_13092, float f) {
        Box Vec3i = EntityUtils.minecraftClient.player.getBoundingBox().expand((double)f, 0.0, (double)f);
        return class_13092.getBoundingBox().intersects(Vec3i);
    }

    public static boolean isOffsetOverlapping(LivingEntity class_13092, Vec3d VanillaChestLootTableGenerator, float f) {
        return EntityUtils.isBoundsOverlapping(class_13092.getBoundingBox().offset(-class_13092.getX(), -class_13092.getY(), -class_13092.getZ()).offset(VanillaChestLootTableGenerator), f);
    }

    public static boolean isBoundsOverlapping(Box Vec3i, float f) {
        Box HorizontalFacingBlock = EntityUtils.minecraftClient.player.getBoundingBox().expand((double)f, 0.0, (double)f);
        return Vec3i.intersects(HorizontalFacingBlock);
    }

    public static StatusEffectInstance createStatusEffect(RegistryEntry<StatusEffect> class_68802, int n, int n2) {
        return new StatusEffectInstance(class_68802, n, n2, false, false, false);
    }

    public static boolean isEffectActive(StatusEffectInstance class_12932) {
        return class_12932 != null && !class_12932.shouldShowParticles() && !class_12932.shouldShowIcon();
    }

    public static void applyMovement(double d, boolean bl) {
        double d2 = EntityUtils.minecraftClient.player.input.movementForward;
        double d3 = EntityUtils.minecraftClient.player.input.movementSideways;
        float f = EntityUtils.minecraftClient.player.getYaw();
        if (!(d2 != 0.0 || d3 != 0.0 || bl && (EntityUtils.minecraftClient.options.jumpKey.isPressed() || EntityUtils.minecraftClient.options.sneakKey.isPressed()))) {
            EntityUtils.minecraftClient.player.setVelocity(0.0, EntityUtils.minecraftClient.player.getVelocity().y, 0.0);
            return;
        }
        if (d2 != 0.0) {
            if (d3 > 0.0) {
                f += (float)(d2 > 0.0 ? -45 : 45);
            } else if (d3 < 0.0) {
                f += (float)(d2 > 0.0 ? 45 : -45);
            }
            d3 = 0.0;
            d2 = d2 > 0.0 ? 1.0 : -1.0;
        }
        double d4 = Math.sin(Math.toRadians((double)f + 90.0));
        double d5 = Math.cos(Math.toRadians((double)f + 90.0));
        double d6 = d2 * d * d5 + d3 * d * d4;
        double d7 = d2 * d * d4 - d3 * d * d5;
        double d8 = 0.0;
        if (EntityUtils.minecraftClient.options.jumpKey.isPressed()) {
            d8 += d;
        }
        if (EntityUtils.minecraftClient.options.sneakKey.isPressed()) {
            d8 -= d;
        }
        EntityUtils.minecraftClient.player.setVelocity(d6, bl ? d8 / 2.0 : EntityUtils.minecraftClient.player.getVelocity().y, d7);
    }

    public static boolean isMovingTowardPlayer(LivingEntity class_13092) {
        double d;
        if (EntityUtils.minecraftClient.player == null) {
            return false;
        }
        double d2 = Math.sqrt(class_13092.getVelocity().x * class_13092.getVelocity().x + class_13092.getVelocity().z * class_13092.getVelocity().z);
        if (d2 < 0.1) {
            return false;
        }
        double d3 = class_13092.getX() - EntityUtils.minecraftClient.player.getX();
        double d4 = Math.sqrt(d3 * d3 + (d = class_13092.getZ() - EntityUtils.minecraftClient.player.getZ()) * d);
        if (d4 < 0.1) {
            return false;
        }
        double d5 = d3 / d4;
        double d6 = class_13092.getVelocity().x;
        double d7 = d / d4;
        double d8 = class_13092.getVelocity().z;
        double d9 = d5 * d6 + d7 * d8;
        return d9 > 0.15;
    }

    public static boolean hasMovementInput() {
        if (EntityUtils.minecraftClient.player == null || EntityUtils.minecraftClient.world == null || EntityUtils.minecraftClient.player.input == null) {
            return false;
        }
        return (double)EntityUtils.minecraftClient.player.forwardSpeed != 0.0 || (double)EntityUtils.minecraftClient.player.input.movementSideways != 0.0;
    }

    public static Block getBlockBelowEntity(Entity class_12972) {
        if (class_12972 == null) {
            return null;
        }
        BlockPos adminsky = class_12972.getBlockPos().down();
        return EntityUtils.getBlockFromWorld(adminsky, class_12972.getWorld());
    }

    public static Block getBlockAtEntityHead(Entity class_12972) {
        if (class_12972 == null) {
            return null;
        }
        BlockPos adminsky = class_12972.getBlockPos().add(0, Math.round(class_12972.getHeight()), 0).up();
        return EntityUtils.getBlockFromWorld(adminsky, class_12972.getWorld());
    }

    public static Block getPlayerFootBlock() {
        if (EntityUtils.minecraftClient.player == null || EntityUtils.minecraftClient.world == null) {
            return null;
        }
        BlockPos adminsky = EntityUtils.minecraftClient.player.getBlockPos().down().up();
        return EntityUtils.getBlockFromWorld(adminsky, (World)EntityUtils.minecraftClient.world);
    }

    public static Block getBlockAbovePlayer() {
        if (EntityUtils.minecraftClient.player == null || EntityUtils.minecraftClient.world == null) {
            return null;
        }
        BlockPos adminsky = EntityUtils.minecraftClient.player.getBlockPos().up();
        return EntityUtils.getBlockFromWorld(adminsky, (World)EntityUtils.minecraftClient.world);
    }

    public static Block getBlockAtEntityPosition(Entity class_12972) {
        if (class_12972 == null) {
            return null;
        }
        BlockPos adminsky = class_12972.getBlockPos();
        return EntityUtils.getBlockFromWorld(adminsky, class_12972.getWorld());
    }

    public static double getHorizontalSpeed() {
        return Math.hypot(EntityUtils.minecraftClient.player.getVelocity().x, EntityUtils.minecraftClient.player.getVelocity().z);
    }

    public static Block getBlockAtPlayerPosition() {
        if (EntityUtils.minecraftClient.player == null || EntityUtils.minecraftClient.world == null) {
            return null;
        }
        BlockPos adminsky = EntityUtils.minecraftClient.player.getBlockPos();
        return EntityUtils.getBlockFromWorld(adminsky, (World)EntityUtils.minecraftClient.world);
    }

    public static Block getBlockFromWorld(BlockPos adminsky, World class_19372) {
        return class_19372.getBlockState(adminsky).getBlock();
    }

    public static double getDirectionRadians(float f, double d, double d2) {
        if (d < 0.0) {
            f += 180.0f;
        }
        float f2 = 1.0f;
        if (d < 0.0) {
            f2 = -0.5f;
        } else if (d > 0.0) {
            f2 = 0.5f;
        }
        if (d2 > 0.0) {
            f -= 90.0f * f2;
        }
        if (d2 < 0.0) {
            f += 90.0f * f2;
        }
        return Math.toRadians(f);
    }

    public static boolean isClientWorldReady() {
        return EntityUtils.minecraftClient.player != null && EntityUtils.minecraftClient.world != null;
    }

    public static float getPlayerHealth(PlayerEntity class_16572) {
        if (class_16572 == null) {
            return 0.0f;
        }
        ScoreboardObjective class_2662 = class_16572.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
        if (class_2662 != null) {
            ReadableScoreboardScore class_90132 = class_16572.getScoreboard().getScore((ScoreHolder)class_16572, class_2662);
            String string = ReadableScoreboardScore.getFormattedScore((ReadableScoreboardScore)class_90132, (NumberFormat)class_2662.getNumberFormatOr((NumberFormat)StyledNumberFormat.EMPTY)).getString();
            Float f = EntityUtils.parseHealthText(string);
            if (EntityUtils.isHealthScoreboardText(class_2662, string)) {
                return f != null ? f.floatValue() : (class_90132 != null ? (float)class_90132.getScore() : class_16572.getHealth());
            }
        }
        return class_16572.getHealth();
    }

    private static Float parseHealthText(String string) {
        int n = -1;
        int n2 = -1;
        boolean bl = false;
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (Character.isDigit(c)) {
                if (n == -1) {
                    n = i;
                }
                n2 = i + 1;
                continue;
            }
            if (!(c != '.' && c != ',' || n == -1 || bl)) {
                bl = true;
                n2 = i + 1;
                continue;
            }
            if (n != -1) break;
        }
        if (n == -1) {
            return null;
        }
        try {
            return Float.valueOf(Float.parseFloat(string.substring(n, n2).replace(',', '.')));
        }
        catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    private static boolean isHealthScoreboardText(ScoreboardObjective class_2662, String string) {
        if (ServerDetector.isServerProfileSupported(ServerProfile.MODERN_NETWORKS)) {
            return true;
        }
        if (ServerDetector.isFuntimeServer()) {
            return true;
        }
        if (class_2662.getRenderType() == ScoreboardCriterion.RenderType.HEARTS) {
            return true;
        }
        String string2 = (class_2662.getName() + " " + class_2662.getDisplayName().getString() + " " + string).toLowerCase(Locale.ROOT);
        if (string2.contains("hp") || string2.contains("\u0445\u043f") || string2.contains("\u0437\u0434\u043e\u0440\u043e\u0432") || string2.contains("\u2764") || string2.contains("\u2665")) {
            return true;
        }
        return string2.contains("funtime") || string2.contains("fun time") || string2.contains("\u0444\u0430\u043d\u0442\u0430\u0439\u043c");
    }

    public static boolean isHoldingMiningTool() {
        if (EntityUtils.minecraftClient.player == null) {
            return false;
        }
        ItemStack class_17992 = EntityUtils.minecraftClient.player.getMainHandStack();
        Item class_17922 = class_17992.getItem();
        if (class_17992.isEmpty()) {
            return false;
        }
        return class_17922 instanceof SwordItem || class_17922 instanceof AxeItem || class_17922 instanceof TridentItem || class_17922 instanceof MaceItem;
    }

    public static boolean isPlayerOnKnownMovementSurface() {
        return EntityUtils.hasKnownMovementMultiplier((Entity)EntityUtils.minecraftClient.player);
    }

    public static boolean hasKnownMovementMultiplier(Entity class_12972) {
        if (!(class_12972 instanceof EntityMovementMultiplierAccessor)) {
            return false;
        }
        EntityMovementMultiplierAccessor entityMovementMultiplierAccessor = (EntityMovementMultiplierAccessor)class_12972;
        Vec3d VanillaChestLootTableGenerator = entityMovementMultiplierAccessor.getMovementMultiplier();
        if (VanillaChestLootTableGenerator == null) {
            return false;
        }
        return EntityUtils.isKnownMovementMultiplier(VanillaChestLootTableGenerator);
    }

    private static boolean isKnownMovementMultiplier(Vec3d VanillaChestLootTableGenerator) {
        return EntityUtils.matchesMovementMultiplier(VanillaChestLootTableGenerator, 0.25, 0.05, 0.25) || EntityUtils.matchesMovementMultiplier(VanillaChestLootTableGenerator, 0.5, 0.25, 0.5);
    }

    private static boolean matchesMovementMultiplier(Vec3d VanillaChestLootTableGenerator, double d, double d2, double d3) {
        double d4 = 1.0E-6;
        return Math.abs(VanillaChestLootTableGenerator.x - d) < d4 && Math.abs(VanillaChestLootTableGenerator.y - d2) < d4 && Math.abs(VanillaChestLootTableGenerator.z - d3) < d4;
    }

    @Generated
    private EntityUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static void setMovementFactor(float f) {
        movementFactor = f;
    }

    @Generated
    public static float getMovementFactor() {
        return movementFactor;
    }
}

