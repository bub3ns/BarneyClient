/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.api.validation;

import org.jetbrains.annotations.Nullable;

public interface ScreenStateService {
    public String getCommandName();

    public String getStatusMessage();

    public boolean tickNavigation();

    public void stopNavigation();

    public void pauseNavigation();

    public void resumeNavigation();

    public boolean isPaused();

    default public boolean isTargetReached() {
        return false;
    }

    @Nullable
    default public String getErrorMessage() {
        return null;
    }
}

