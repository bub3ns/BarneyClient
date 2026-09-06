/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Block
 *  net.minecraft.Identifier
 *  net.minecraft.Registries
 */
package moscow.rockstar.world.navigation;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import moscow.rockstar.api.commands.NavigationCommandService;
import moscow.rockstar.api.plugins.PluginEntry;
import moscow.rockstar.api.plugins.PluginEntryConsumer;
import moscow.rockstar.api.validation.RequestContext;
import moscow.rockstar.api.validation.ScreenStateService;
import moscow.rockstar.combat.rotation.RotationEngine;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.entity.CollisionProbe;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.render.texture.TextureReloadTask;
import moscow.rockstar.world.BlockCollisionProbe;
import moscow.rockstar.world.mining.BlockTargetFinder;
import moscow.rockstar.world.navigation.PathNavigator;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public final class NavigationService
implements TextureReloadTask {
    private final List<PluginEntryConsumer> pluginListeners = new CopyOnWriteArrayList<PluginEntryConsumer>();

    @Override
    public void registerCollisionProbe(BlockPos adminsky) {
        this.registerCollisionProbe(new BlockCollisionProbe(adminsky));
    }

    @Override
    public void setBlockCollisionMode(BlockPos adminsky, boolean bl) {
        if (bl) {
            this.registerRotationTask(adminsky);
        } else {
            this.registerCollisionProbe(adminsky);
        }
    }

    @Override
    public void registerCollisionProbe(CollisionProbe collisionProbe) {
        ClientServiceRegistry.getInstance().getEventListenerSlot().scheduleScreenState(new PathNavigator(collisionProbe));
    }

    @Override
    public void registerRotationTask(BlockPos adminsky) {
        ClientServiceRegistry.getInstance().getEventListenerSlot().scheduleScreenState(new RotationEngine(adminsky));
    }

    @Override
    public void registerBlockTargetFinder(Identifier class_29602) {
        Block class_22482 = (Block)Registries.BLOCK.get(class_29602);
        ClientServiceRegistry.getInstance().getEventListenerSlot().scheduleScreenState(new BlockTargetFinder(class_22482));
    }

    @Override
    public void clearNavigationTasks() {
        ClientServiceRegistry.getInstance().getEventListenerSlot().cancelPendingScreenState();
    }

    @Override
    public boolean isNavigationActive() {
        return ClientServiceRegistry.getInstance().getEventListenerSlot().hasPendingScreenState();
    }

    @Override
    public Optional<RequestContext> getActiveScreenState() {
        return ClientServiceRegistry.getInstance().getEventListenerSlot().getPendingScreenState().map(this::createScreenStateRequest);
    }

    @Override
    public boolean isCommandAllowed(String string) {
        NavigationCommandService navigationCommandService = ClientServiceRegistry.getInstance().getNavigationCommandService();
        return navigationCommandService.isCommandRegistered(string);
    }

    @Override
    public void addPluginListener(PluginEntryConsumer pluginEntryConsumer) {
        this.pluginListeners.add(pluginEntryConsumer);
    }

    @Override
    public void removePluginListener(PluginEntryConsumer pluginEntryConsumer) {
        this.pluginListeners.remove(pluginEntryConsumer);
    }

    public void publishPluginEntry(PluginEntry pluginEntry) {
        for (PluginEntryConsumer pluginEntryConsumer : this.pluginListeners) {
            try {
                pluginEntryConsumer.accept(pluginEntry);
            }
            catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private RequestContext createScreenStateRequest(final ScreenStateService screenStateService) {
        return new RequestContext(){

            @Override
            public String getCommandName() {
                return screenStateService.getCommandName();
            }

            @Override
            public String getStatusMessage() {
                return screenStateService.getStatusMessage();
            }

            @Override
            public boolean isPaused() {
                return screenStateService.isPaused();
            }

            @Override
            public void pause() {
                screenStateService.pauseNavigation();
            }

            @Override
            public void resume() {
                screenStateService.resumeNavigation();
            }

            @Override
            public void stop() {
                screenStateService.stopNavigation();
            }
        };
    }
}
