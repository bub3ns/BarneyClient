/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Identifier
 */
package moscow.rockstar.render.texture;

import java.util.Optional;
import moscow.rockstar.api.plugins.PluginEntryConsumer;
import moscow.rockstar.api.validation.RequestContext;
import moscow.rockstar.entity.CollisionProbe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;

public interface TextureReloadTask {
    public void registerCollisionProbe(BlockPos var1);

    public void setBlockCollisionMode(BlockPos var1, boolean var2);

    public void registerCollisionProbe(CollisionProbe var1);

    public void registerRotationTask(BlockPos var1);

    public void registerBlockTargetFinder(Identifier var1);

    public void clearNavigationTasks();

    public boolean isNavigationActive();

    public Optional<RequestContext> getActiveScreenState();

    public boolean isCommandAllowed(String var1);

    public void addPluginListener(PluginEntryConsumer var1);

    public void removePluginListener(PluginEntryConsumer var1);
}

