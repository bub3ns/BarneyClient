/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 */
package moscow.rockstar.modules.combat.aura.attack;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.combat.aura.rotation.AuraRotationMode;
import moscow.rockstar.render.esp.EntityOverlayGeometry;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

public class AuraAttackController
extends AuraRotationMode {
    private final Timer attackCooldown = new Timer();
    private final AuraAttackController.RotationProfileController profileController = new AuraAttackController.RotationProfileController(this);
    private long attackDelayMillis;
    private final EventListener<ClientPlayerTickEvent> playerTickListener = clientPlayerTickEvent -> {
        if (RockstarClient.create().getFriendManager().getTargetLivingEntity() == null && !this.attackCooldown.hasElapsed(this.attackDelayMillis) && this.aura().getSortingSetting().getSelectedOption() instanceof AuraAttackController) {
            Rotation rotation = RockstarClient.create().getRotationManager().getPlayerRotation();
            rotation = rotation.offset(MathUtils.interpolateRandomStrategy(-8.0f, 5.0f), MathUtils.interpolateRandomStrategy(-5.0f, 8.0f));
            float f = MathUtils.interpolateRandomStrategy(65.0f, 95.0f);
            float f2 = MathUtils.interpolateRandomStrategy(30.0f, 45.0f);
            RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, f, f2, 90.0f, RotationPriority.TARGET_PRIORITY);
        }
    };

    public AuraAttackController(ModeSetting modeSetting) {
        super(modeSetting, "SpookyTime");
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    @Override
    @Compile(obfuscation=1)
    public void rotate(RotationManager rotationManager, float f, boolean bl, boolean bl2, RotationCorrectionMode rotationCorrectionMode, LivingEntity class_13092) {
        if (class_13092 == null || AuraAttackController.minecraftClient.player == null) {
            return;
        }
        Rotation rotation = this.profileController.calculateAttackRotation(class_13092, rotationManager.getEffectiveRotation());
        rotationManager.requestRotation(rotation, rotationCorrectionMode, 180.0f, 180.0f, 90.0f, RotationPriority.TARGET_PRIORITY);
        this.attackCooldown.reset();
        this.attackDelayMillis = (long)MathUtils.interpolateRandomStrategy(700.0f, 1000.0f);
    }

    @Override
    public void onAttack() {
        LivingEntity class_13092 = RockstarClient.create().getFriendManager().getTargetLivingEntity();
        if (class_13092 != null) {
            this.profileController.recordAttack(class_13092);
        }
    }

    @Override
    public void onTargetLost() {
        this.profileController.clearCurrentTarget();
    }

    @Override
    public boolean canAttack() {
        LivingEntity class_13092 = RockstarClient.create().getFriendManager().getTargetLivingEntity();
        if (class_13092 == null || AuraAttackController.minecraftClient.player == null) {
            return false;
        }
        Rotation rotation = RockstarClient.create().getRotationManager().getEffectiveRotation();
        return this.profileController.isTargetAttackAllowed(class_13092, this.aura().getAttackDistanceSetting().getValue(), 58.0f, rotation);
    }

    static final class AttackTimingHistory {
        private long lastTargetTimestamp;
        private boolean humanizationBurstActive;
        private int variationIndex;
        private long humanizationBurstUntil;
        float recentAttackCount = 10.0f;
        final List<Long> attackTimestamps = new ArrayList<Long>();

        AttackTimingHistory() {
        }
    }

    static final class AttackPatternState {
        float yawJitter;
        float pitchJitter;
        final float[] healthThresholds = new float[20];
        int aimPointIndex;
        long targetAcquiredAt;
        private String aimPointMode = "health";
        boolean humanizationActive;
        int burstAttackCount;
        long humanizationBurstUntil;
        long lastAttackAt;
        LivingEntity currentTarget;
        private final Map<String, Float> profileValueCache = new ConcurrentHashMap<String, Float>();

        AttackPatternState() {
        }
    }

    static final class HumanizationSettings {
        int minHumanizationDelayMillis;
        int maxHumanizationDelayMillis;
        double humanizationChance;
        double movementPenaltyChance;
        boolean lowHealthHumanization;

        HumanizationSettings() {
        }
    }

    static final class AttackPatternSettings {
        int maxBurstAttacks;
        int burstDelayMinMillis;
        int burstDelayMaxMillis;
        double minimumRotationSpeed;
        double maximumRotationSpeed;
        double aimAccuracy;
        int minimumAttackDelayMillis;
        int maximumAttackDelayMillis;
        String preferredAimPoint;
        int targetSwitchCooldownMillis;

        AttackPatternSettings() {
        }
    }

    static final class RotationSettings {
        double yawJitterDegrees;
        double pitchJitterDegrees;
        double rotationResponse;
        double verticalCorrection;
        double movementPrediction;

        RotationSettings() {
        }
    }

    static final class RotationProfile {
        String profileName;
        double profileVersion;
        AttackPatternSettings attackPattern;
        HumanizationSettings humanization;
        RotationSettings rotationSettings;

        RotationProfile() {
        }
    }

    static final class RotationProfileController {
        private static final String PROFILE_RESOURCE_PATH = "/assets/rockstar/ml/rotation/profile.json";
        private static final Gson PROFILE_JSON_PARSER = new GsonBuilder().create();
        private final AttackPatternState attackPatternState = new AttackPatternState();
        private final AttackTimingHistory attackTimingHistory = new AttackTimingHistory();
        private RotationProfile rotationProfile = this.loadRotationProfile();
        private final AuraAttackController ownerController;

        RotationProfileController(AuraAttackController ownerController) {
            this.ownerController = ownerController;
        }

        Rotation calculateAttackRotation(LivingEntity target, Rotation currentRotation) {
            this.ensureProfileLoaded();
            Vec3d aimPoint = this.selectTargetAimPoint(target);
            Vec3d playerEye = AuraAttackController.minecraftClient.player.getEyePos();
            Vec3d relative = aimPoint.subtract(playerEye);
            Vec3d predicted = this.predictTargetPosition(relative, target);
            float targetYaw = (float)Math.toDegrees(Math.atan2(predicted.z, predicted.x)) - 90.0f;
            float targetPitch = (float)(-Math.toDegrees(Math.atan2(predicted.y, Math.hypot(predicted.z, predicted.x))));
            float yawDelta = MathHelper.wrapDegrees(targetYaw - currentRotation.getYaw());
            float pitchDelta = MathHelper.wrapDegrees(targetPitch - currentRotation.getPitch());
            float jitterMultiplier = this.getRotationJitterMultiplier();
            float yawJitter = (float)(this.rotationProfile.rotationSettings.yawJitterDegrees * jitterMultiplier);
            float pitchJitter = (float)(this.rotationProfile.rotationSettings.pitchJitterDegrees * jitterMultiplier);
            float adjustedYaw = MathHelper.clamp(yawDelta, -yawJitter, yawJitter);
            float adjustedPitch = MathHelper.clamp(pitchDelta, -pitchJitter, pitchJitter);
            float response = (float)this.rotationProfile.rotationSettings.rotationResponse;
            adjustedYaw *= response;
            adjustedPitch *= response;
            float verticalCorrection = (float)(this.rotationProfile.rotationSettings.verticalCorrection * this.getVerticalCorrectionMultiplier());
            this.attackPatternState.yawJitter = adjustedYaw += this.randomizeFloat(-verticalCorrection, verticalCorrection);
            this.attackPatternState.pitchJitter = adjustedPitch += this.randomizeFloat(-verticalCorrection, verticalCorrection);
            return new Rotation(currentRotation.getYaw() + adjustedYaw,
                MathHelper.clamp(currentRotation.getPitch() + adjustedPitch, -90.0f, 90.0f));
        }

        boolean isTargetAttackAllowed(LivingEntity target, float maxDistance, float maxAngle, Rotation rotation) {
            this.ensureProfileLoaded();
            long now = System.currentTimeMillis();
            if (this.attackPatternState.currentTarget != target) {
                if (now - this.attackPatternState.targetAcquiredAt < this.rotationProfile.attackPattern.targetSwitchCooldownMillis) {
                    return false;
                }
                this.attackPatternState.currentTarget = target;
                this.attackPatternState.targetAcquiredAt = now;
            }
            if (AuraAttackController.minecraftClient.player.distanceTo(target) > maxDistance) {
                return false;
            }
            Vec3d direction = rotation.toDirectionVector().normalize();
            Vec3d targetVector = target.getBoundingBox().getCenter().subtract(AuraAttackController.minecraftClient.player.getEyePos());
            if (targetVector.lengthSquared() > 1.0E-6 && direction.dotProduct(targetVector.normalize()) < Math.cos(Math.toRadians(maxAngle))) {
                return false;
            }
            return this.passesAttackHumanization(target, now);
        }

        private boolean passesAttackHumanization(LivingEntity target, long now) {
            float targetHealth = target.getHealth();
            float playerHealth = AuraAttackController.minecraftClient.player.getHealth();
            double distance = AuraAttackController.minecraftClient.player.distanceTo(target);
            double score = 0.5;
            if (targetHealth < 6.0f) score += 0.3;
            if (targetHealth < 3.0f) score += 0.4;
            if (playerHealth < 8.0f) score += 0.2;
            if (playerHealth < 4.0f) score += 0.3;
            if (distance < 2.0) score += 0.2;
            if (distance > 4.0) score -= 0.2;
            long sinceAttack = now - this.attackPatternState.lastAttackAt;
            if (sinceAttack < 150L) score -= 0.3;
            if (this.attackPatternState.humanizationActive) {
                if (this.attackPatternState.burstAttackCount >= this.rotationProfile.attackPattern.maxBurstAttacks || now > this.attackPatternState.humanizationBurstUntil) {
                    this.attackPatternState.humanizationActive = false;
                    score -= 0.4;
                } else {
                    score += 0.3;
                }
            }
            if (ThreadLocalRandom.current().nextDouble() < this.rotationProfile.humanization.movementPenaltyChance) score -= 0.5;
            if (this.rotationProfile.humanization.lowHealthHumanization && targetHealth <= 0.5f) score -= 0.8;
            return score > 0.6;
        }

        private long getAttackDelayMillis() {
            this.ensureProfileLoaded();
            float multiplier = this.getAttackSpeedMultiplier();
            float minimum = (float)this.rotationProfile.attackPattern.minimumAttackDelayMillis * multiplier;
            float maximum = (float)this.rotationProfile.attackPattern.maximumAttackDelayMillis * multiplier;
            long delay = (long)ThreadLocalRandom.current().nextDouble(minimum, maximum);
            return Math.max(20L, delay + ThreadLocalRandom.current().nextInt(-8, 8));
        }

        private float getAttackSpeedMultiplier() {
            float multiplier = 1.0f;
            if (AuraAttackController.minecraftClient.player.getHealth() < 6.0f) multiplier *= 0.85f;
            if (System.currentTimeMillis() - this.attackPatternState.lastAttackAt > 15000L) multiplier *= 1.15f;
            return MathHelper.clamp(multiplier, 0.7f, 1.4f);
        }

        private Vec3d selectTargetAimPoint(LivingEntity target) {
            Box bounds = EntityOverlayGeometry.getTargetBoundingBox(target, this.ownerController.aura().getResolverOption().isSelected());
            double heightFactor = switch (this.rotationProfile.attackPattern.preferredAimPoint == null ? "" : this.rotationProfile.attackPattern.preferredAimPoint) {
                case "head" -> 0.85;
                case "chest" -> 0.65;
                case "health" -> target.getHealth() < 8.0f ? 0.85 : 0.55;
                default -> 0.65;
            };
            heightFactor = MathHelper.clamp(heightFactor + ThreadLocalRandom.current().nextDouble(-0.07, 0.07), 0.05, 0.95);
            return new Vec3d(
                bounds.minX + (bounds.maxX - bounds.minX) * (0.3 + ThreadLocalRandom.current().nextDouble(0.4)),
                bounds.minY + (bounds.maxY - bounds.minY) * heightFactor,
                bounds.minZ + (bounds.maxZ - bounds.minZ) * (0.3 + ThreadLocalRandom.current().nextDouble(0.4)));
        }

        private Vec3d predictTargetPosition(Vec3d relativePosition, LivingEntity target) {
            Vec3d velocity = target.getVelocity();
            double factor = this.rotationProfile.rotationSettings.movementPrediction * 0.1;
            return new Vec3d(relativePosition.x + velocity.x * factor, relativePosition.y, relativePosition.z + velocity.z * factor);
        }

        private float getRotationJitterMultiplier() {
            float multiplier = 1.0f;
            if (AuraAttackController.minecraftClient.player.getHealth() < 6.0f) multiplier *= 1.3f;
            if (this.attackPatternState.humanizationActive) multiplier *= 1.2f;
            return MathHelper.clamp(multiplier, 0.6f, 1.5f);
        }

        private float getVerticalCorrectionMultiplier() {
            float multiplier = 1.0f;
            if (AuraAttackController.minecraftClient.player.fallDistance > 0.0f) multiplier *= 2.5f;
            if (this.attackPatternState.humanizationActive) multiplier *= 1.3f;
            return MathHelper.clamp(multiplier, 0.5f, 3.0f);
        }

        void recordAttack(LivingEntity target) {
            this.ensureProfileLoaded();
            long now = System.currentTimeMillis();
            this.attackPatternState.lastAttackAt = now;
            if (!this.attackPatternState.humanizationActive) {
                this.attackPatternState.humanizationActive = true;
                this.attackPatternState.burstAttackCount = 0;
                int delay = ThreadLocalRandom.current().nextInt(this.rotationProfile.attackPattern.burstDelayMinMillis, this.rotationProfile.attackPattern.burstDelayMaxMillis);
                this.attackPatternState.humanizationBurstUntil = now + delay;
            }
            ++this.attackPatternState.burstAttackCount;
            this.attackTimingHistory.attackTimestamps.add(now);
            this.attackTimingHistory.attackTimestamps.removeIf(timestamp -> now - timestamp > 1000L);
            this.attackTimingHistory.recentAttackCount = this.attackTimingHistory.attackTimestamps.size();
            this.adjustAimPointForHealth(target);
        }

        private void adjustAimPointForHealth(LivingEntity target) {
            if (target instanceof PlayerEntity && target.getHealth() > 12.0f && this.attackPatternState.healthThresholds[this.attackPatternState.aimPointIndex] < 5.0f) {
                this.rotationProfile.attackPattern.preferredAimPoint = "head";
            }
        }

        private int getHumanizationDelayMillis() {
            this.ensureProfileLoaded();
            return ThreadLocalRandom.current().nextInt(this.rotationProfile.humanization.minHumanizationDelayMillis, this.rotationProfile.humanization.maxHumanizationDelayMillis);
        }

        private boolean shouldHumanizeAttack() {
            this.ensureProfileLoaded();
            return ThreadLocalRandom.current().nextDouble() < this.rotationProfile.humanization.humanizationChance;
        }

        void clearCurrentTarget() {
            this.attackPatternState.currentTarget = null;
        }

        private void ensureProfileLoaded() {
            if (this.rotationProfile == null || this.rotationProfile.attackPattern == null || this.rotationProfile.humanization == null || this.rotationProfile.rotationSettings == null) {
                this.rotationProfile = this.createDefaultRotationProfile();
            }
        }

        private RotationProfile loadRotationProfile() {
            try (InputStream stream = AuraAttackController.class.getResourceAsStream(PROFILE_RESOURCE_PATH)) {
                if (stream == null) return this.createDefaultRotationProfile();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    RotationProfile profile = PROFILE_JSON_PARSER.fromJson((Reader)reader, RotationProfile.class);
                    return profile == null ? this.createDefaultRotationProfile() : profile;
                }
            } catch (Exception exception) {
                return this.createDefaultRotationProfile();
            }
        }

        private RotationProfile createDefaultRotationProfile() {
            RotationProfile profile = new RotationProfile();
            profile.profileName = "AggressivePlayer";
            profile.profileVersion = 1.0;
            profile.attackPattern = new AttackPatternSettings();
            profile.attackPattern.maxBurstAttacks = 5;
            profile.attackPattern.burstDelayMinMillis = 120;
            profile.attackPattern.burstDelayMaxMillis = 280;
            profile.attackPattern.minimumRotationSpeed = 12.0;
            profile.attackPattern.maximumRotationSpeed = 18.5;
            profile.attackPattern.aimAccuracy = 0.85;
            profile.attackPattern.minimumAttackDelayMillis = 32;
            profile.attackPattern.maximumAttackDelayMillis = 67;
            profile.attackPattern.preferredAimPoint = "head";
            profile.attackPattern.targetSwitchCooldownMillis = 180;
            profile.humanization = new HumanizationSettings();
            profile.humanization.minHumanizationDelayMillis = 55;
            profile.humanization.maxHumanizationDelayMillis = 130;
            profile.humanization.humanizationChance = 0.21;
            profile.humanization.movementPenaltyChance = 0.07;
            profile.humanization.lowHealthHumanization = false;
            profile.rotationSettings = new RotationSettings();
            profile.rotationSettings.yawJitterDegrees = 110.0;
            profile.rotationSettings.pitchJitterDegrees = 58.0;
            profile.rotationSettings.rotationResponse = 0.68;
            profile.rotationSettings.verticalCorrection = 0.35;
            profile.rotationSettings.movementPrediction = 0.52;
            return profile;
        }

        private float randomizeFloat(float minimum, float maximum) {
            return (float)ThreadLocalRandom.current().nextDouble(minimum, maximum);
        }
    }
}
