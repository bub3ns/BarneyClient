/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ClientPlayerEntity
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.world.navigation;

import lombok.Generated;
import moscow.rockstar.entity.CollisionProbe;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.CollisionPath;
import moscow.rockstar.world.mining.ExcavationController;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class PathExecutor {
    private final CollisionPath path;
    @Nullable
    private final CollisionProbe collisionProbe;
    private int currentStep = 0;
    private static final int MAX_STATIONARY_TICKS = 60;
    private static final double WAYPOINT_REACH_DISTANCE = 0.9;
    private double snapshotX;
    private double snapshotY;
    private double snapshotZ;
    private int snapshotStep = -1;
    private int stationaryTicks;
    private boolean snapshotValid;

    public PathExecutor(CollisionPath collisionPath, @Nullable CollisionProbe collisionProbe) {
        this.path = collisionPath;
        this.collisionProbe = collisionProbe;
    }

    public MovementAction getMovementAction() {
        if (this.path.getMovements().isEmpty()) {
            return MovementAction.ADVANCE;
        }
        if (this.currentStep >= this.path.getMovements().size()) {
            return MovementAction.ADVANCE;
        }
        this.skipReachedSteps();
        if (this.currentStep >= this.path.getMovements().size()) {
            return MovementAction.ADVANCE;
        }
        ClientPlayerEntity class_7462 = MinecraftClient.getInstance().player;
        if (class_7462 != null && this.isPlayerStationary(class_7462)) {
            this.path.getMovements().get(this.currentStep).stopNavigation();
            this.clearPlayerSnapshot();
            return MovementAction.ARRIVED;
        }
        this.prepareLookahead();
        int n = 16;
        while (n-- > 0) {
            ExcavationController excavationController = this.path.getMovements().get(this.currentStep);
            ExcavationController.NavigationResult navigationResult = excavationController.tickNavigation()
                ? ExcavationController.NavigationResult.REACHED_STEP
                : ExcavationController.NavigationResult.IN_PROGRESS;
            switch (navigationResult) {
                case IN_PROGRESS: {
                    return MovementAction.WAITING;
                }
                case REACHED_STEP: {
                    ++this.currentStep;
                    if (this.currentStep >= this.path.getMovements().size()) {
                        return MovementAction.ADVANCE;
                    }
                    this.prepareLookahead();
                    break;
                }
                case UNAVAILABLE: {
                    excavationController.stopNavigation();
                    return MovementAction.ARRIVED;
                }
            }
        }
        return MovementAction.WAITING;
    }

    private void prepareLookahead() {
        int n = Math.min(this.currentStep + 6, this.path.getMovements().size());
        // Individual movement controllers perform their own preparation when ticked.
    }

    private void skipReachedSteps() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity class_7462 = client.player;
        if (class_7462 == null) {
            return;
        }
        double d = class_7462.getX();
        double d2 = class_7462.getY();
        double d3 = class_7462.getZ();
        int n = 8;
        while (n-- > 0 && this.currentStep + 1 < this.path.getMovements().size()) {
            boolean bl;
            ExcavationController excavationController = this.path.getMovements().get(this.currentStep);
            ExcavationController excavationController2 = this.path.getMovements().get(this.currentStep + 1);
            double d4 = PathExecutor.distanceSquaredToWaypoint(d, d2, d3,
                BlockPositionOffset.fromBlockPosition(excavationController.getEndPosition()));
            double d5 = PathExecutor.distanceSquaredToWaypoint(d, d2, d3,
                BlockPositionOffset.fromBlockPosition(excavationController2.getStartPosition()));
            double d6 = PathExecutor.distanceSquaredToWaypoint(d, d2, d3,
                BlockPositionOffset.fromBlockPosition(excavationController2.getEndPosition()));
            boolean bl2 = d5 < 0.36;
            boolean bl3 = d6 < 0.36;
            boolean bl4 = bl = d6 + 0.25 < d4;
            if (!bl2 && !bl3 && !bl) break;
            excavationController.stopNavigation();
            ++this.currentStep;
        }
    }

    private boolean isPlayerStationary(ClientPlayerEntity class_7462) {
        double d;
        double d2;
        if (!this.snapshotValid || this.currentStep != this.snapshotStep) {
            this.capturePlayerPosition(class_7462);
            return false;
        }
        double d3 = class_7462.getX() - this.snapshotX;
        if (d3 * d3 + (d2 = class_7462.getY() - this.snapshotY) * d2 + (d = class_7462.getZ() - this.snapshotZ) * d > 0.81) {
            this.capturePlayerPosition(class_7462);
            return false;
        }
        ExcavationController excavationController = this.getCurrentMovement();
        int n = Math.max(60, excavationController != null ? excavationController.getTargetBlockCount() : 0);
        return ++this.stationaryTicks >= n;
    }

    private void capturePlayerPosition(ClientPlayerEntity class_7462) {
        this.snapshotX = class_7462.getX();
        this.snapshotY = class_7462.getY();
        this.snapshotZ = class_7462.getZ();
        this.snapshotStep = this.currentStep;
        this.stationaryTicks = 0;
        this.snapshotValid = true;
    }

    private void clearPlayerSnapshot() {
        this.snapshotValid = false;
        this.snapshotStep = -1;
        this.stationaryTicks = 0;
    }

    private static double distanceSquaredToWaypoint(double d, double d2, double d3, BlockPositionOffset blockPositionOffset) {
        double d4 = d - ((double)blockPositionOffset.getX() + 0.5);
        double d5 = d2 - (double)blockPositionOffset.getY();
        double d6 = d3 - ((double)blockPositionOffset.getZ() + 0.5);
        return d4 * d4 + d5 * d5 + d6 * d6;
    }

    public ExcavationController getCurrentMovement() {
        if (this.currentStep >= this.path.getMovements().size()) {
            return null;
        }
        return this.path.getMovements().get(this.currentStep);
    }

    public void jumpToStep(int n) {
        ExcavationController excavationController = this.getCurrentMovement();
        if (excavationController != null) {
            excavationController.stopNavigation();
        }
        this.currentStep = Math.max(0, Math.min(n, this.path.getMovements().size()));
        this.clearPlayerSnapshot();
    }

    @Generated
    public CollisionPath getPath() {
        return this.path;
    }

    @Nullable
    @Generated
    public CollisionProbe getCollisionProbe() {
        return this.collisionProbe;
    }

    @Generated
    public int getCurrentStep() {
        return this.currentStep;
    }

    public static enum MovementAction {
        WAITING,
        ADVANCE,
        ARRIVED;
}
}

