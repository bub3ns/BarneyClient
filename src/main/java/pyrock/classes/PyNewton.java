/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Block
 *  net.minecraft.Vec3d
 *  org.jetbrains.annotations.Nullable
 */
package pyrock.classes;

import java.util.List;
import moscow.rockstar.api.commands.NavigationCommandService;
import moscow.rockstar.api.validation.ScreenStateService;
import moscow.rockstar.combat.rotation.RotationEngine;
import moscow.rockstar.core.ClientFeatureFlags;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.entity.CollisionProbe;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.world.BlockCollisionProbe;
import moscow.rockstar.world.BlockInteractionState;
import moscow.rockstar.world.BlockOffset;
import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.BlockRegion;
import moscow.rockstar.world.VerticalOffset;
import moscow.rockstar.world.mining.BlockTargetFinder;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.PathExecutor;
import moscow.rockstar.world.navigation.PathNavigator;
import moscow.rockstar.world.BlockNameResolver;
import moscow.rockstar.world.selection.BlockSelectionState;
import net.minecraft.block.Block;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class PyNewton {
    public boolean ready() {
        return ClientServiceRegistry.isInitialized();
    }

    public boolean active() {
        return this.ready() && ClientServiceRegistry.getInstance().getEventListenerSlot().hasPendingScreenState();
    }

    public boolean goTo(int n, int n2, int n3) {
        return this.start(new PathNavigator(new BlockCollisionProbe(new BlockPos(n, n2, n3))));
    }

    public boolean goToNear(int n, int n2, int n3, int n4) {
        return this.start(new PathNavigator(new BlockRegion(new BlockPos(n, n2, n3), Math.max(0, n4))));
    }

    public boolean goToXZ(int n, int n2) {
        return this.start(new PathNavigator(new BlockOffset(n, n2)));
    }

    public boolean goToY(int n) {
        return this.start(new PathNavigator(new VerticalOffset(n)));
    }

    public boolean flyTo(int n, int n2, int n3, boolean bl) {
        return this.start(new RotationEngine(n, n2, n3, bl));
    }

    public boolean mine(String string) {
        Block class_22482 = BlockNameResolver.resolve(string);
        if (class_22482 == null) {
            return false;
        }
        return this.start(new BlockTargetFinder(class_22482));
    }

    public boolean excavate(int n, int n2, int n3, int n4, int n5, int n6, @Nullable String string) {
        Block class_22482 = null;
        if (string != null && !string.isEmpty() && (class_22482 = BlockNameResolver.resolve(string)) == null) {
            return false;
        }
        return this.start(new ExcavationController(PyNewton.min(n, n2, n3, n4, n5, n6), PyNewton.max(n, n2, n3, n4, n5, n6), class_22482));
    }

    public boolean fill(int n, int n2, int n3, int n4, int n5, int n6, String string) {
        Block class_22482 = BlockNameResolver.resolve(string);
        if (class_22482 == null) {
            return false;
        }
        return this.start(new BlockInteractionState(PyNewton.min(n, n2, n3, n4, n5, n6), PyNewton.max(n, n2, n3, n4, n5, n6), class_22482));
    }

    public void cancel() {
        if (!this.ready()) {
            return;
        }
        ClientServiceRegistry.getInstance().getEventListenerSlot().cancelPendingScreenState();
        ClientServiceRegistry.getInstance().getRotationState().disableMovementOverride();
    }

    public boolean pause() {
        ScreenStateService screenStateService = this.current();
        if (screenStateService == null) {
            return false;
        }
        screenStateService.pauseNavigation();
        return true;
    }

    public boolean resume() {
        ScreenStateService screenStateService = this.current();
        if (screenStateService == null) {
            return false;
        }
        screenStateService.resumeNavigation();
        return true;
    }

    public boolean paused() {
        ScreenStateService screenStateService = this.current();
        return screenStateService != null && screenStateService.isPaused();
    }

    @Nullable
    public String process() {
        ScreenStateService screenStateService = this.current();
        return screenStateService == null ? null : screenStateService.getCommandName();
    }

    @Nullable
    public String status() {
        ScreenStateService screenStateService = this.current();
        return screenStateService == null ? null : screenStateService.getStatusMessage();
    }

    public boolean command(String string) {
        if (!this.ready() || string == null || string.isBlank()) {
            return false;
        }
        try {
            NavigationCommandService navigationCommandService = ClientServiceRegistry.getInstance().getNavigationCommandService();
            return navigationCommandService.executeCommand(navigationCommandService.getCommandPrefix() + "newton " + string);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public int pathSteps() {
        PathExecutor pathExecutor = this.executor();
        return pathExecutor == null ? 0 : pathExecutor.getPath().getMovements().size();
    }

    public int pathStep() {
        PathExecutor pathExecutor = this.executor();
        return pathExecutor == null ? 0 : pathExecutor.getCurrentStep();
    }

    public int @Nullable [] nextNode() {
        PathExecutor pathExecutor = this.executor();
        if (pathExecutor == null) {
            return null;
        }
        List<ExcavationController> list = pathExecutor.getPath().getMovements();
        int n = pathExecutor.getCurrentStep();
        if (n >= list.size()) {
            return null;
        }
        BlockPositionOffset blockPositionOffset = BlockPositionOffset.fromBlockPosition(list.get(n).getEndPosition());
        return new int[]{blockPositionOffset.getX(), blockPositionOffset.getY(), blockPositionOffset.getZ()};
    }

    public double @Nullable [] goalPos() {
        double[] dArray;
        CollisionProbe collisionProbe;
        PathExecutor pathExecutor = this.executor();
        CollisionProbe collisionProbe2 = collisionProbe = pathExecutor == null ? null : pathExecutor.getCollisionProbe();
        if (collisionProbe == null) {
            return null;
        }
        Vec3d VanillaChestLootTableGenerator = collisionProbe.toBlockCenter();
        if (VanillaChestLootTableGenerator == null) {
            dArray = null;
        } else {
            double[] dArray2 = new double[3];
            dArray2[0] = VanillaChestLootTableGenerator.x;
            dArray2[1] = VanillaChestLootTableGenerator.y;
            dArray = dArray2;
            dArray2[2] = VanillaChestLootTableGenerator.z;
        }
        return dArray;
    }

    public void select(int n, int n2, int n3) {
        BlockSelectionState.getInstance().select(new BlockPos(n, n2, n3));
    }

    public void selectClear() {
        BlockSelectionState.getInstance().clearSelection();
    }

    public int @Nullable [] selection() {
        BlockSelectionState selectionState = BlockSelectionState.getInstance();
        if (!selectionState.hasSelection()) {
            return null;
        }
        BlockPos startPosition = selectionState.getStartPosition();
        BlockPos endPosition = selectionState.getEndPosition();
        return new int[]{startPosition.getX(), startPosition.getY(), startPosition.getZ(), endPosition.getX(), endPosition.getY(), endPosition.getZ()};
    }

    public boolean excavateSelection(@Nullable String string) {
        BlockSelectionState selectionState = BlockSelectionState.getInstance();
        if (!selectionState.hasSelection()) {
            return false;
        }
        BlockPos startPosition = selectionState.getStartPosition();
        BlockPos endPosition = selectionState.getEndPosition();
        return this.excavate(startPosition.getX(), startPosition.getY(), startPosition.getZ(), endPosition.getX(), endPosition.getY(), endPosition.getZ(), string);
    }

    public boolean fillSelection(String string) {
        BlockSelectionState selectionState = BlockSelectionState.getInstance();
        if (!selectionState.hasSelection()) {
            return false;
        }
        BlockPos startPosition = selectionState.getStartPosition();
        BlockPos endPosition = selectionState.getEndPosition();
        return this.fill(startPosition.getX(), startPosition.getY(), startPosition.getZ(), endPosition.getX(), endPosition.getY(), endPosition.getZ(), string);
    }

    public boolean safewalk() {
        return ClientFeatureFlags.safewalkEnabled;
    }

    public void setSafewalk(boolean bl) {
        ClientFeatureFlags.safewalkEnabled = bl;
    }

    public boolean logging() {
        return ClientFeatureFlags.loggingEnabled;
    }

    public void setLogging(boolean bl) {
        ClientFeatureFlags.loggingEnabled = bl;
    }

    private boolean start(ScreenStateService screenStateService) {
        if (!this.ready()) {
            return false;
        }
        try {
            ClientServiceRegistry.getInstance().getEventListenerSlot().scheduleScreenState(screenStateService);
            return true;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    @Nullable
    private ScreenStateService current() {
        return this.ready() ? (ScreenStateService)ClientServiceRegistry.getInstance().getEventListenerSlot().getPendingScreenState().orElse(null) : null;
    }

    @Nullable
    private PathExecutor executor() {
        return this.ready() ? ClientServiceRegistry.getInstance().getPathExecutor() : null;
    }

    private static BlockPos min(int n, int n2, int n3, int n4, int n5, int n6) {
        return new BlockPos(Math.min(n, n4), Math.min(n2, n5), Math.min(n3, n6));
    }

    private static BlockPos max(int n, int n2, int n3, int n4, int n5, int n6) {
        return new BlockPos(Math.max(n, n4), Math.max(n2, n5), Math.max(n3, n6));
    }
}
