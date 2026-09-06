/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.PlayerEntity
 */
package moscow.rockstar.entity.targeting;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public final class TargetActionQueue {
    private static PlayerEntity currentPlayerTarget;
    private static Runnable pendingAction;
    private static Runnable deferredAction;
    private static boolean runImmediately;

    private TargetActionQueue() {
    }

    public static void runDeferredAction() {
        if (deferredAction == null) {
            return;
        }
        Runnable runnable = deferredAction;
        deferredAction = null;
        runnable.run();
    }

    public static void setCurrentTarget(PlayerEntity class_16572) {
        TargetActionQueue.queueTargetAction(class_16572, null);
    }

    public static void queueTargetAction(PlayerEntity class_16572, Runnable runnable) {
        TargetActionQueue.queueTargetAction(class_16572, runnable, false);
    }

    public static void queueTargetAction(PlayerEntity class_16572, Runnable runnable, boolean bl) {
        currentPlayerTarget = class_16572;
        pendingAction = runnable;
        runImmediately = bl;
    }

    public static boolean isCurrentTarget(Entity class_12972) {
        return currentPlayerTarget != null && class_12972 == currentPlayerTarget;
    }

    public static void completeTargetAction(Entity class_12972) {
        if (!TargetActionQueue.isCurrentTarget(class_12972)) {
            return;
        }
        Runnable runnable = pendingAction;
        boolean bl = runImmediately;
        TargetActionQueue.clearTarget(class_12972);
        if (runnable != null) {
            if (bl) {
                runnable.run();
            } else {
                deferredAction = runnable;
            }
        }
    }

    public static boolean hasDeferredAction() {
        return deferredAction != null;
    }

    public static void clearTarget(Entity class_12972) {
        if (currentPlayerTarget != null && class_12972 == currentPlayerTarget) {
            currentPlayerTarget = null;
            pendingAction = null;
            runImmediately = false;
        }
    }

    public static boolean isImmediateTarget(Entity class_12972) {
        return TargetActionQueue.isCurrentTarget(class_12972) && runImmediately;
    }
}

