/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.util.scheduling;

import moscow.rockstar.api.commands.MiningCommandService;
import moscow.rockstar.util.ScheduledState;

@FunctionalInterface
public interface ScheduledStateFactory {
    public ScheduledState createScheduledState(MiningCommandService var1);
}

