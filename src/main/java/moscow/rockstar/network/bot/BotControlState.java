/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.network.bot;

import lombok.Generated;

public class BotControlState {
    private double rotationToleranceDegrees;
    private double movementStepScale;
    private double baseMovementSpeed;
    private double movementAcceleration;
    private double motionDampingFactor;
    private double yawTurnRateDegrees;
    private double pitchTurnRateDegrees = 6.0;
    private double contactReachDistance = 3.0;
    private long attackIntervalMillis;
    private double approachStepDistance = 0.45;
    private double sprintSpeedMultiplier = 1.3;
    private double sneakSpeedMultiplier = 0.35;
    private double verticalMovementStep = 0.42;
    private double movementEpsilon = 1.0E-5;
    private long actionRepeatIntervalMillis = 120L;
    private double targetReachDistance = 4.5;
    private double blockRayStep = 0.1;
    private double entityHitboxRadius = 0.65;
    private double entityVerticalOffset = 1.0;
    private long entitySnapshotMaxAgeTicks = 400L;
    private double targetSearchDistance = 4.0;
    private int visibleSlotLimit = 12;
    private int hotbarSlotLimit = 6;
    private int inventorySlotLimit = 10;
    private double interactionReachDistance = 8.0;
    private long initialRetryDelayMillis = 180L;
    private long botPollIntervalMillis = 120L;
    private long rebreakDelayMillis = 160L;
    private double aimToleranceDegrees = 8.0;
    private int maxSearchAttempts = 150000;
    private int navigationAttemptLimit = 100;
    private int rebreakCycleLimit = 200;
    private int breakAttemptLimit = 20;
    private boolean controlInputValid = true;
    private long breakCompletionTimeoutMillis = 50L;
    private double initialPositionDistance = 6.0;
    private long auctionClickDelayMillis = 120L;
    private long auctionNavigationDelayMillis = 150L;
    private String auxiliaryCommand = "/an";
    private double verticalSmoothingStep = 0.08;
    private double verticalSmoothingFactor = 0.98;
    private double verticalSmoothingLimit = 3.92;
    private double targetHeightTolerance = 0.08;
    private double blockSearchDistance = 4.0;
    private double cameraVerticalThreshold = 0.6;
    private double eyeHeightOffset = 1.62;
    private boolean targetHeightKnown = true;
    private boolean positionStateReady = true;
    private int movementUpdateLimit = 50;
    private int movementPacketIntervalTicks = 20;
    private double positionChangeThreshold = 9.0E-4;
    private double rotationChangeThreshold = 1.0E-4;
    private double blockHardnessMultiplier = 30.0;
    private double minimumBreakRate = 1.0;
    private long swingIntervalMillis = 150L;
    private int slotSelectionLimit = 1;
    private long sessionTimeoutMillis = 3000L;
    private int botCreationIntervalSeconds = 8;
    private String serverName = "Rickstone";
    private String backupServerName = "Rick123";
    private boolean environmentReady = false;
    private boolean screenReady = false;
    private String idleCommand = "/spanw";
    private long actionCooldownMillis = 180000L;
    private long movementToggleIntervalMillis = 180000L;
    private double idleVerticalOffset = 0.24;
    private int botSlotIndex = 100;

    public BotControlState() {
    }

    public BotControlState(double d, double d2, boolean bl, boolean bl2) {
        this.rotationToleranceDegrees = d;
        this.movementStepScale = d2;
        this.targetHeightKnown = bl;
        this.positionStateReady = bl2;
    }

    public void copyFrom(BotControlState botControlState) {
        this.rotationToleranceDegrees = botControlState.rotationToleranceDegrees;
        this.movementStepScale = botControlState.movementStepScale;
        this.baseMovementSpeed = botControlState.baseMovementSpeed;
        this.movementAcceleration = botControlState.movementAcceleration;
        this.motionDampingFactor = botControlState.motionDampingFactor;
        this.yawTurnRateDegrees = botControlState.yawTurnRateDegrees;
        this.pitchTurnRateDegrees = botControlState.pitchTurnRateDegrees;
        this.contactReachDistance = botControlState.contactReachDistance;
        this.attackIntervalMillis = botControlState.attackIntervalMillis;
        this.approachStepDistance = botControlState.approachStepDistance;
        this.sprintSpeedMultiplier = botControlState.sprintSpeedMultiplier;
        this.sneakSpeedMultiplier = botControlState.sneakSpeedMultiplier;
        this.verticalMovementStep = botControlState.verticalMovementStep;
        this.movementEpsilon = botControlState.movementEpsilon;
        this.actionRepeatIntervalMillis = botControlState.actionRepeatIntervalMillis;
        this.targetReachDistance = botControlState.targetReachDistance;
        this.blockRayStep = botControlState.blockRayStep;
        this.entityHitboxRadius = botControlState.entityHitboxRadius;
        this.entityVerticalOffset = botControlState.entityVerticalOffset;
        this.entitySnapshotMaxAgeTicks = botControlState.entitySnapshotMaxAgeTicks;
        this.targetSearchDistance = botControlState.targetSearchDistance;
        this.visibleSlotLimit = botControlState.visibleSlotLimit;
        this.hotbarSlotLimit = botControlState.hotbarSlotLimit;
        this.inventorySlotLimit = botControlState.inventorySlotLimit;
        this.interactionReachDistance = botControlState.interactionReachDistance;
        this.initialRetryDelayMillis = botControlState.initialRetryDelayMillis;
        this.botPollIntervalMillis = botControlState.botPollIntervalMillis;
        this.rebreakDelayMillis = botControlState.rebreakDelayMillis;
        this.aimToleranceDegrees = botControlState.aimToleranceDegrees;
        this.maxSearchAttempts = botControlState.maxSearchAttempts;
        this.navigationAttemptLimit = botControlState.navigationAttemptLimit;
        this.rebreakCycleLimit = botControlState.rebreakCycleLimit;
        this.breakAttemptLimit = botControlState.breakAttemptLimit;
        this.controlInputValid = botControlState.controlInputValid;
        this.breakCompletionTimeoutMillis = botControlState.breakCompletionTimeoutMillis;
        this.initialPositionDistance = botControlState.initialPositionDistance;
        this.auctionClickDelayMillis = botControlState.auctionClickDelayMillis;
        this.auctionNavigationDelayMillis = botControlState.auctionNavigationDelayMillis;
        this.auxiliaryCommand = botControlState.auxiliaryCommand;
        this.verticalSmoothingStep = botControlState.verticalSmoothingStep;
        this.verticalSmoothingFactor = botControlState.verticalSmoothingFactor;
        this.verticalSmoothingLimit = botControlState.verticalSmoothingLimit;
        this.targetHeightTolerance = botControlState.targetHeightTolerance;
        this.blockSearchDistance = botControlState.blockSearchDistance;
        this.cameraVerticalThreshold = botControlState.cameraVerticalThreshold;
        this.eyeHeightOffset = botControlState.eyeHeightOffset;
        this.targetHeightKnown = botControlState.targetHeightKnown;
        this.positionStateReady = botControlState.positionStateReady;
        this.movementUpdateLimit = botControlState.movementUpdateLimit;
        this.movementPacketIntervalTicks = botControlState.movementPacketIntervalTicks;
        this.positionChangeThreshold = botControlState.positionChangeThreshold;
        this.rotationChangeThreshold = botControlState.rotationChangeThreshold;
        this.blockHardnessMultiplier = botControlState.blockHardnessMultiplier;
        this.minimumBreakRate = botControlState.minimumBreakRate;
        this.swingIntervalMillis = botControlState.swingIntervalMillis;
        this.slotSelectionLimit = botControlState.slotSelectionLimit;
        this.sessionTimeoutMillis = botControlState.sessionTimeoutMillis;
        this.botCreationIntervalSeconds = botControlState.botCreationIntervalSeconds;
        this.serverName = botControlState.serverName;
        this.backupServerName = botControlState.backupServerName;
        this.environmentReady = botControlState.environmentReady;
        this.screenReady = botControlState.screenReady;
        this.idleCommand = botControlState.idleCommand;
        this.actionCooldownMillis = botControlState.actionCooldownMillis;
        this.movementToggleIntervalMillis = botControlState.movementToggleIntervalMillis;
        this.idleVerticalOffset = botControlState.idleVerticalOffset;
        this.botSlotIndex = botControlState.botSlotIndex;
    }

    @Generated
    public double getRotationToleranceDegrees() {
        return this.rotationToleranceDegrees;
    }

    @Generated
    public double getMovementStepScale() {
        return this.movementStepScale;
    }

    @Generated
    public double getBaseMovementSpeed() {
        return this.baseMovementSpeed;
    }

    @Generated
    public double getMovementAcceleration() {
        return this.movementAcceleration;
    }

    @Generated
    public double getMotionDampingFactor() {
        return this.motionDampingFactor;
    }

    @Generated
    public double getYawTurnRateDegrees() {
        return this.yawTurnRateDegrees;
    }

    @Generated
    public double getPitchTurnRateDegrees() {
        return this.pitchTurnRateDegrees;
    }

    @Generated
    public double getContactReachDistance() {
        return this.contactReachDistance;
    }

    @Generated
    public long getAttackIntervalMillis() {
        return this.attackIntervalMillis;
    }

    @Generated
    public double getApproachStepDistance() {
        return this.approachStepDistance;
    }

    @Generated
    public double getSprintSpeedMultiplier() {
        return this.sprintSpeedMultiplier;
    }

    @Generated
    public double getSneakSpeedMultiplier() {
        return this.sneakSpeedMultiplier;
    }

    @Generated
    public double getVerticalMovementStep() {
        return this.verticalMovementStep;
    }

    @Generated
    public double getMovementEpsilon() {
        return this.movementEpsilon;
    }

    @Generated
    public long getActionRepeatIntervalMillis() {
        return this.actionRepeatIntervalMillis;
    }

    @Generated
    public double getTargetReachDistance() {
        return this.targetReachDistance;
    }

    @Generated
    public double getBlockRayStep() {
        return this.blockRayStep;
    }

    @Generated
    public double getEntityHitboxRadius() {
        return this.entityHitboxRadius;
    }

    @Generated
    public double getEntityVerticalOffset() {
        return this.entityVerticalOffset;
    }

    @Generated
    public long getEntitySnapshotMaxAgeTicks() {
        return this.entitySnapshotMaxAgeTicks;
    }

    @Generated
    public double getTargetSearchDistance() {
        return this.targetSearchDistance;
    }

    @Generated
    public int getVisibleSlotLimit() {
        return this.visibleSlotLimit;
    }

    @Generated
    public int getHotbarSlotLimit() {
        return this.hotbarSlotLimit;
    }

    @Generated
    public int getInventorySlotLimit() {
        return this.inventorySlotLimit;
    }

    @Generated
    public double getInteractionReachDistance() {
        return this.interactionReachDistance;
    }

    @Generated
    public long getInitialRetryDelayMillis() {
        return this.initialRetryDelayMillis;
    }

    @Generated
    public long getBotPollIntervalMillis() {
        return this.botPollIntervalMillis;
    }

    @Generated
    public long getRebreakDelayMillis() {
        return this.rebreakDelayMillis;
    }

    @Generated
    public double getAimToleranceDegrees() {
        return this.aimToleranceDegrees;
    }

    @Generated
    public int getMaxSearchAttempts() {
        return this.maxSearchAttempts;
    }

    @Generated
    public int getNavigationAttemptLimit() {
        return this.navigationAttemptLimit;
    }

    @Generated
    public int getRebreakCycleLimit() {
        return this.rebreakCycleLimit;
    }

    @Generated
    public int getBreakAttemptLimit() {
        return this.breakAttemptLimit;
    }

    @Generated
    public boolean isControlInputValid() {
        return this.controlInputValid;
    }

    @Generated
    public long getBreakCompletionTimeoutMillis() {
        return this.breakCompletionTimeoutMillis;
    }

    @Generated
    public double getInitialPositionDistance() {
        return this.initialPositionDistance;
    }

    @Generated
    public long getAuctionClickDelayMillis() {
        return this.auctionClickDelayMillis;
    }

    @Generated
    public long getAuctionNavigationDelayMillis() {
        return this.auctionNavigationDelayMillis;
    }

    @Generated
    public String getAuxiliaryCommand() {
        return this.auxiliaryCommand;
    }

    @Generated
    public double getVerticalSmoothingStep() {
        return this.verticalSmoothingStep;
    }

    @Generated
    public double getVerticalSmoothingFactor() {
        return this.verticalSmoothingFactor;
    }

    @Generated
    public double getVerticalSmoothingLimit() {
        return this.verticalSmoothingLimit;
    }

    @Generated
    public double getTargetHeightTolerance() {
        return this.targetHeightTolerance;
    }

    @Generated
    public double getBlockSearchDistance() {
        return this.blockSearchDistance;
    }

    @Generated
    public double getCameraVerticalThreshold() {
        return this.cameraVerticalThreshold;
    }

    @Generated
    public double getEyeHeightOffset() {
        return this.eyeHeightOffset;
    }

    @Generated
    public boolean isTargetHeightKnown() {
        return this.targetHeightKnown;
    }

    @Generated
    public boolean isPositionStateReady() {
        return this.positionStateReady;
    }

    @Generated
    public int getMovementUpdateLimit() {
        return this.movementUpdateLimit;
    }

    @Generated
    public int getMovementPacketIntervalTicks() {
        return this.movementPacketIntervalTicks;
    }

    @Generated
    public double getPositionChangeThreshold() {
        return this.positionChangeThreshold;
    }

    @Generated
    public double getRotationChangeThreshold() {
        return this.rotationChangeThreshold;
    }

    @Generated
    public double getBlockHardnessMultiplier() {
        return this.blockHardnessMultiplier;
    }

    @Generated
    public double getMinimumBreakRate() {
        return this.minimumBreakRate;
    }

    @Generated
    public long getSwingIntervalMillis() {
        return this.swingIntervalMillis;
    }

    @Generated
    public int getSlotSelectionLimit() {
        return this.slotSelectionLimit;
    }

    @Generated
    public long getSessionTimeoutMillis() {
        return this.sessionTimeoutMillis;
    }

    @Generated
    public int getBotCreationIntervalSeconds() {
        return this.botCreationIntervalSeconds;
    }

    @Generated
    public String getServerName() {
        return this.serverName;
    }

    @Generated
    public String getBackupServerName() {
        return this.backupServerName;
    }

    @Generated
    public boolean isEnvironmentReady() {
        return this.environmentReady;
    }

    @Generated
    public boolean isScreenReady() {
        return this.screenReady;
    }

    @Generated
    public String getIdleCommand() {
        return this.idleCommand;
    }

    @Generated
    public long getActionCooldownMillis() {
        return this.actionCooldownMillis;
    }

    @Generated
    public long getMovementToggleIntervalMillis() {
        return this.movementToggleIntervalMillis;
    }

    @Generated
    public double getIdleVerticalOffset() {
        return this.idleVerticalOffset;
    }

    @Generated
    public int getBotSlotIndex() {
        return this.botSlotIndex;
    }

    @Generated
    public void setRotationToleranceDegrees(double d) {
        this.rotationToleranceDegrees = d;
    }

    @Generated
    public void setMovementStepScale(double d) {
        this.movementStepScale = d;
    }

    @Generated
    public void setBaseMovementSpeed(double d) {
        this.baseMovementSpeed = d;
    }

    @Generated
    public void setMovementAcceleration(double d) {
        this.movementAcceleration = d;
    }

    @Generated
    public void setMotionDampingFactor(double d) {
        this.motionDampingFactor = d;
    }

    @Generated
    public void setYawTurnRateDegrees(double d) {
        this.yawTurnRateDegrees = d;
    }

    @Generated
    public void setPitchTurnRateDegrees(double d) {
        this.pitchTurnRateDegrees = d;
    }

    @Generated
    public void setContactReachDistance(double d) {
        this.contactReachDistance = d;
    }

    @Generated
    public void setAttackIntervalMillis(long l) {
        this.attackIntervalMillis = l;
    }

    @Generated
    public void setApproachStepDistance(double d) {
        this.approachStepDistance = d;
    }

    @Generated
    public void setSprintSpeedMultiplier(double d) {
        this.sprintSpeedMultiplier = d;
    }

    @Generated
    public void setSneakSpeedMultiplier(double d) {
        this.sneakSpeedMultiplier = d;
    }

    @Generated
    public void setVerticalMovementStep(double d) {
        this.verticalMovementStep = d;
    }

    @Generated
    public void setMovementEpsilon(double d) {
        this.movementEpsilon = d;
    }

    @Generated
    public void setActionRepeatIntervalMillis(long l) {
        this.actionRepeatIntervalMillis = l;
    }

    @Generated
    public void setTargetReachDistance(double d) {
        this.targetReachDistance = d;
    }

    @Generated
    public void setBlockRayStep(double d) {
        this.blockRayStep = d;
    }

    @Generated
    public void setEntityHitboxRadius(double d) {
        this.entityHitboxRadius = d;
    }

    @Generated
    public void setEntityVerticalOffset(double d) {
        this.entityVerticalOffset = d;
    }

    @Generated
    public void setEntitySnapshotMaxAgeTicks(long l) {
        this.entitySnapshotMaxAgeTicks = l;
    }

    @Generated
    public void setTargetSearchDistance(double d) {
        this.targetSearchDistance = d;
    }

    @Generated
    public void setVisibleSlotLimit(int n) {
        this.visibleSlotLimit = n;
    }

    @Generated
    public void setHotbarSlotLimit(int n) {
        this.hotbarSlotLimit = n;
    }

    @Generated
    public void setInventorySlotLimit(int n) {
        this.inventorySlotLimit = n;
    }

    @Generated
    public void setInteractionReachDistance(double d) {
        this.interactionReachDistance = d;
    }

    @Generated
    public void setInitialRetryDelayMillis(long l) {
        this.initialRetryDelayMillis = l;
    }

    @Generated
    public void setBotPollIntervalMillis(long l) {
        this.botPollIntervalMillis = l;
    }

    @Generated
    public void setRebreakDelayMillis(long l) {
        this.rebreakDelayMillis = l;
    }

    @Generated
    public void setAimToleranceDegrees(double d) {
        this.aimToleranceDegrees = d;
    }

    @Generated
    public void setMaxSearchAttempts(int n) {
        this.maxSearchAttempts = n;
    }

    @Generated
    public void setNavigationAttemptLimit(int n) {
        this.navigationAttemptLimit = n;
    }

    @Generated
    public void setRebreakCycleLimit(int n) {
        this.rebreakCycleLimit = n;
    }

    @Generated
    public void setBreakAttemptLimit(int n) {
        this.breakAttemptLimit = n;
    }

    @Generated
    public void setControlInputValid(boolean bl) {
        this.controlInputValid = bl;
    }

    @Generated
    public void setBreakCompletionTimeoutMillis(long l) {
        this.breakCompletionTimeoutMillis = l;
    }

    @Generated
    public void setInitialPositionDistance(double d) {
        this.initialPositionDistance = d;
    }

    @Generated
    public void setAuctionClickDelayMillis(long l) {
        this.auctionClickDelayMillis = l;
    }

    @Generated
    public void setAuctionNavigationDelayMillis(long l) {
        this.auctionNavigationDelayMillis = l;
    }

    @Generated
    public void setAuxiliaryCommand(String string) {
        this.auxiliaryCommand = string;
    }

    @Generated
    public void setVerticalSmoothingStep(double d) {
        this.verticalSmoothingStep = d;
    }

    @Generated
    public void setVerticalSmoothingFactor(double d) {
        this.verticalSmoothingFactor = d;
    }

    @Generated
    public void setVerticalSmoothingLimit(double d) {
        this.verticalSmoothingLimit = d;
    }

    @Generated
    public void setTargetHeightTolerance(double d) {
        this.targetHeightTolerance = d;
    }

    @Generated
    public void setBlockSearchDistance(double d) {
        this.blockSearchDistance = d;
    }

    @Generated
    public void setCameraVerticalThreshold(double d) {
        this.cameraVerticalThreshold = d;
    }

    @Generated
    public void setEyeHeightOffset(double d) {
        this.eyeHeightOffset = d;
    }

    @Generated
    public void setTargetHeightKnown(boolean bl) {
        this.targetHeightKnown = bl;
    }

    @Generated
    public void setPositionStateReady(boolean bl) {
        this.positionStateReady = bl;
    }

    @Generated
    public void setMovementUpdateLimit(int n) {
        this.movementUpdateLimit = n;
    }

    @Generated
    public void setMovementPacketIntervalTicks(int n) {
        this.movementPacketIntervalTicks = n;
    }

    @Generated
    public void setPositionChangeThreshold(double d) {
        this.positionChangeThreshold = d;
    }

    @Generated
    public void setRotationChangeThreshold(double d) {
        this.rotationChangeThreshold = d;
    }

    @Generated
    public void setBlockHardnessMultiplier(double d) {
        this.blockHardnessMultiplier = d;
    }

    @Generated
    public void setMinimumBreakRate(double d) {
        this.minimumBreakRate = d;
    }

    @Generated
    public void setSwingIntervalMillis(long l) {
        this.swingIntervalMillis = l;
    }

    @Generated
    public void setSlotSelectionLimit(int n) {
        this.slotSelectionLimit = n;
    }

    @Generated
    public void setSessionTimeoutMillis(long l) {
        this.sessionTimeoutMillis = l;
    }

    @Generated
    public void setBotCreationIntervalSeconds(int n) {
        this.botCreationIntervalSeconds = n;
    }

    @Generated
    public void setServerName(String string) {
        this.serverName = string;
    }

    @Generated
    public void setBackupServerName(String string) {
        this.backupServerName = string;
    }

    @Generated
    public void setEnvironmentReady(boolean bl) {
        this.environmentReady = bl;
    }

    @Generated
    public void setScreenReady(boolean bl) {
        this.screenReady = bl;
    }

    @Generated
    public void setIdleCommand(String string) {
        this.idleCommand = string;
    }

    @Generated
    public void setActionCooldownMillis(long l) {
        this.actionCooldownMillis = l;
    }

    @Generated
    public void setMovementToggleIntervalMillis(long l) {
        this.movementToggleIntervalMillis = l;
    }

    @Generated
    public void setIdleVerticalOffset(double d) {
        this.idleVerticalOffset = d;
    }

    @Generated
    public void setBotSlotIndex(int n) {
        this.botSlotIndex = n;
    }
}
