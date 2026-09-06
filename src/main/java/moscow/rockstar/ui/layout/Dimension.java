/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  lombok.Generated
 */
package moscow.rockstar.ui.layout;

import javax.annotation.Nonnull;
import lombok.Generated;
import moscow.rockstar.util.scheduling.ScheduledStateFactory;

public class Dimension {
    private int width;
    private int height;
    @Nonnull
    private ScheduledStateFactory serviceFactory;

    public Dimension() {
        this(0, 0);
    }

    public Dimension(int n, int n2) {
        this(n, n2, ignored -> moscow.rockstar.util.ScheduledState.UNSCHEDULED);
    }

    public Dimension(int n, int n2, @Nonnull ScheduledStateFactory scheduledStateFactory) {
        this.width = n;
        this.height = n2;
        this.serviceFactory = scheduledStateFactory;
    }

    @Generated
    public int getWidth() {
        return this.width;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Nonnull
    @Generated
    public ScheduledStateFactory getServiceFactory() {
        return this.serviceFactory;
    }

    @Generated
    public Dimension setWidth(int n) {
        this.width = n;
        return this;
    }

    @Generated
    public Dimension setHeight(int n) {
        this.height = n;
        return this;
    }

    @Generated
    public Dimension setServiceFactory(@Nonnull ScheduledStateFactory scheduledStateFactory) {
        if (scheduledStateFactory == null) {
            throw new NullPointerException("retryHandler is marked non-null but is null");
        }
        this.serviceFactory = scheduledStateFactory;
        return this;
    }
}
