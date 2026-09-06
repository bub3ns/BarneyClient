/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.core;

import lombok.Generated;
import moscow.rockstar.api.commands.NavigationCommandService;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.RotationState;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.dispatch.EventBus;
import moscow.rockstar.events.dispatch.EventListenerSlot;
import moscow.rockstar.events.render.CameraRenderListener;
import moscow.rockstar.render.navigation.NavigationOverlayRenderer;
import moscow.rockstar.render.texture.TextureReloadProvider;
import moscow.rockstar.render.world.BlockSelectionRenderer;
import moscow.rockstar.world.navigation.NavigationService;
import moscow.rockstar.world.navigation.PathExecutor;
import org.jetbrains.annotations.Nullable;

public final class ClientServiceRegistry {
    private static ClientServiceRegistry INSTANCE;
    private final RotationState rotationState;
    private final EventListenerSlot eventListenerSlot;
    private final NavigationOverlayRenderer renderStateNode;
    private final BlockSelectionRenderer blockSelectionRenderer;
    private final CameraRenderListener cameraRenderListener;
    private final NavigationService navigationService;
    @Nullable
    private PathExecutor pathExecutor;

    public static ClientServiceRegistry getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("NewtonCore not initialized yet");
        }
        return INSTANCE;
    }

    public static boolean isInitialized() {
        return INSTANCE != null;
    }

    public void setPathExecutor(@Nullable PathExecutor pathExecutor) {
        this.pathExecutor = pathExecutor;
    }

    public ClientServiceRegistry() {
        INSTANCE = this;
        this.rotationState = RotationState.createRotationState(this);
        this.eventListenerSlot = EventListenerSlot.createRegisteredSlot(this);
        this.renderStateNode = NavigationOverlayRenderer.create(this);
        this.blockSelectionRenderer = BlockSelectionRenderer.register(this);
        this.cameraRenderListener = CameraRenderListener.createRegistered(this);
        this.navigationService = new NavigationService();
        TextureReloadProvider.install(this.navigationService);
    }

    public EventBus getEventBus() {
        return RockstarClient.create().getEventBus();
    }

    public RotationManager getRotationManager() {
        return RockstarClient.create().getRotationManager();
    }

    public NavigationCommandService getNavigationCommandService() {
        return RockstarClient.create().getNavigationCommandService();
    }

    @Generated
    public RotationState getRotationState() {
        return this.rotationState;
    }

    @Generated
    public EventListenerSlot getEventListenerSlot() {
        return this.eventListenerSlot;
    }

    @Generated
    public NavigationOverlayRenderer getRenderStateNode() {
        return this.renderStateNode;
    }

    @Generated
    public BlockSelectionRenderer getBlockSelectionRenderer() {
        return this.blockSelectionRenderer;
    }

    @Generated
    public CameraRenderListener getCameraRenderListener() {
        return this.cameraRenderListener;
    }

    @Generated
    public NavigationService getNavigationService() {
        return this.navigationService;
    }

    @Nullable
    @Generated
    public PathExecutor getPathExecutor() {
        return this.pathExecutor;
    }
}

