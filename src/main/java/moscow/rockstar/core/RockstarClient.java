/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
 *  net.fabricmc.fabric.api.resource.ResourceManagerHelper
 *  net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
 *  net.minecraft.Entity
 *  net.minecraft.Identifier
 *  net.minecraft.ResourceType
 *  net.minecraft.ResourceManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package moscow.rockstar.core;

import lombok.Generated;
import moscow.rockstar.api.commands.ClientCommandDispatcher;
import moscow.rockstar.api.commands.NavigationCommandService;
import moscow.rockstar.api.data.SettingDataStore;
import moscow.rockstar.api.scripts.ScriptRegistry;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.flows.LocalRedirectLoginFlow;
import moscow.rockstar.auth.requests.MicrosoftDeviceCodeRequest;
import moscow.rockstar.auth.requests.MicrosoftRefreshTokenRequest;
import moscow.rockstar.auth.tokens.PlayFabToken;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.ClientFeatureFlags;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.core.FriendManager;
import moscow.rockstar.core.resources.ResourceJsonLoader;
import moscow.rockstar.events.dispatch.EventBus;
import moscow.rockstar.events.input.ChatKeyBindingHandler;
import moscow.rockstar.events.network.ServerTickRateTracker;
import moscow.rockstar.events.player.PlayerInputBridge;
import moscow.rockstar.events.player.PlayerTickListener;
import moscow.rockstar.events.render.HudRenderListener;
import moscow.rockstar.math.DoubleLookupTable;
import moscow.rockstar.media.MediaTracker;
import moscow.rockstar.modules.ModuleRegistry;
import moscow.rockstar.modules.visuals.esp.entities.TaksaParticleState;
import moscow.rockstar.modules.visuals.waypoints.ChatWaypointListener;
import moscow.rockstar.render.assets.AssetImageLoader;
import moscow.rockstar.render.esp.OverlayRegistry;
import moscow.rockstar.render.hand.HandSwingPresetManager;
import moscow.rockstar.render.hand.SwingPresetFileManager;
import moscow.rockstar.render.postprocess.ShaderPostProcessor;
import moscow.rockstar.render.shaders.ShaderProgramBase;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.text.icon.SvgIconRegistry;
import moscow.rockstar.render.texture.TextureReloadProvider;
import moscow.rockstar.server.staff.StaffListManager;
import moscow.rockstar.social.FriendListManager;
import moscow.rockstar.ui.core.UiComponentProcessor;
import moscow.rockstar.ui.hud.DynamicIslandManager;
import moscow.rockstar.ui.hud.HudElementRegistry;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.screens.AssistScreen;
import moscow.rockstar.ui.screens.MinecraftScreenBase;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.theme.ColorTheme;
import moscow.rockstar.world.waypoints.WaypointStore;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.mintantileak.spk.Compile;

public enum RockstarClient
implements ClientAccess {
    INSTANCE;
    public static final String CLIENT_NAME = "Barney";
    public static final String VERSION = "2.1";
    public static final String RESOURCE_NAMESPACE = "rockstar";
    public static final Logger LOGGER = LoggerFactory.getLogger(CLIENT_NAME);
    public static Entity CURRENT_ENTITY;
    private EventBus eventBus;
    private ColorTheme colorTheme = ColorTheme.DARK;
    private ModuleRegistry moduleRegistry;
    private NavigationCommandService navigationCommandService;
    private FriendListManager friendListManager;
    private RotationManager rotationManager;
    private ClientServiceRegistry clientServiceRegistry;
    private FriendManager friendManager;
    private MediaTracker mediaTracker;
    private MicrosoftClientConfiguration microsoftClientConfiguration;
    private UiComponentProcessor uiComponentProcessor;
    private DynamicIslandManager dynamicIslandManager;
    private HudElementRegistry hudElementRegistry;
    private MicrosoftRefreshTokenRequest refreshTokenRequest;
    private SettingDataStore settingDataStore;
    private HandSwingPresetManager handSwingPresetManager;
    private ServerTickRateTracker serverTickRateTracker;
    private ShaderPostProcessor shaderPostProcessor;
    private ChatKeyBindingHandler chatKeyBindingHandler;
    private ChatWaypointListener chatWaypointListener;
    private AssistScreen assistScreen;
    private TaksaParticleState taksaParticleState;
    private WaypointStore waypointStore;
    private SwingPresetFileManager swingPresetFileManager;
    private StaffListManager staffListManager;
    private ScriptRegistry scriptRegistry;
    private MicrosoftDeviceCodeRequest deviceCodeRequest;
    private LocalRedirectLoginFlow localRedirectLoginFlow;
    private MinecraftScreenBase minecraftScreen;
    /** Set when the safe Panic action has disabled client features. */
    boolean panicMode;

    @Compile(obfuscation=4)
    public void initializeClient() {
        LOGGER.info("Initializing {}...", (Object)CLIENT_NAME);
        this.mediaTracker = new MediaTracker();
        this.waypointStore = new WaypointStore();
        this.scriptRegistry = new ScriptRegistry();
        this.eventBus = new EventBus();
        this.serverTickRateTracker = new ServerTickRateTracker();
        this.friendListManager = new FriendListManager();
        this.staffListManager = new StaffListManager();
        this.rotationManager = new RotationManager(new PlayerInputBridge());
        this.clientServiceRegistry = new ClientServiceRegistry();
        this.friendManager = new FriendManager();
        this.microsoftClientConfiguration = new MicrosoftClientConfiguration("0000000048183522", "service::user.auth.xboxlive.com::MBI_SSL");
        this.moduleRegistry = new ModuleRegistry(new PlayerTickListener(), new HudRenderListener());
        this.shaderPostProcessor = new ShaderPostProcessor();
        this.uiComponentProcessor = new UiComponentProcessor();
        this.dynamicIslandManager = new DynamicIslandManager();
        this.hudElementRegistry = new HudElementRegistry();
        this.moduleRegistry.registerModules();
        this.hudElementRegistry.registerDefaultElements();
        this.moduleRegistry.enableLockedModules();
        OverlayRegistry.getInstance().registerDefaultOverlays();
        this.navigationCommandService = new NavigationCommandService();
        // The original constructs the command dispatcher here and immediately installs its
        // built-in command set. Without this call the dispatcher stays empty, so no client
        // command runs and the chat suggestor has nothing to offer.
        ClientCommandDispatcher.registerDefaults();
        this.settingDataStore = new SettingDataStore();
        this.handSwingPresetManager = new HandSwingPresetManager();
        this.swingPresetFileManager = new SwingPresetFileManager();
        this.swingPresetFileManager.loadAutosave();
        AssetImageLoader.loadAssets();
        ResourceManagerHelper.get((ResourceType)ResourceType.CLIENT_RESOURCES).registerReloadListener((IdentifiableResourceReloadListener)new SimpleSynchronousResourceReloadListener(){

            public Identifier getFabricId() {
                return RockstarClient.resourceId("after_shader_load");
            }

            public void reload(ResourceManager class_33002) {
                if (RockstarClient.this.panicMode) {
                    return;
                }
                Font.reloadAllFonts();
                SvgIconRegistry.reload();
                ShaderProgramBase.reloadShaders();
            }
        });
        ShaderRenderer.initializeShaders();
        ShaderProgramBase.reloadShaderPrograms(true);
        Localization.loadLanguageFile();
        this.chatWaypointListener = new ChatWaypointListener();
        this.chatKeyBindingHandler = new ChatKeyBindingHandler();
        this.assistScreen = new AssistScreen();
        this.taksaParticleState = new TaksaParticleState();
        // Read back Rockstar/client.rock and staff.txt. This must run last: the client
        // document restores HUD element geometry, colour-picker presets, waypoints,
        // macros and per-module state, so every registry it writes into has to exist
        // first. Without this call the config stack is write-only and nothing the user
        // changes survives a restart.
        moscow.rockstar.api.data.ClientConfigManager.getInstance().loadAll();
        LOGGER.info("{} initialized", (Object)CLIENT_NAME);
    }

    public void shutdownClient() {
        LOGGER.info("Shutting down...");
        if (!this.isPanicMode()) {
            if (this.swingPresetFileManager != null) {
                this.swingPresetFileManager.saveActivePreset();
            }
            // Flush client.rock and staff.txt on the way out, so state that is only
            // marked dirty in memory (theme, waypoints, macros, per-module settings)
            // is not lost when the game closes without a triggering UI action.
            try {
                moscow.rockstar.api.data.ClientConfigManager.getInstance().saveAll();
            }
            catch (Exception exception) {
                LOGGER.error("Failed to save client configuration on shutdown", (Throwable)exception);
            }
        }
        this.setPanicMode(false);
    }

    public static RockstarClient create() {
        return INSTANCE;
    }

    public static Identifier resourceId(String string) {
        return Identifier.of((String)RESOURCE_NAMESPACE, (String)string);
    }

    @Generated
    public EventBus getEventBus() {
        return this.eventBus;
    }

    @Generated
    public ColorTheme getColorTheme() {
        return this.colorTheme;
    }

    @Generated
    public void setColorTheme(ColorTheme colorTheme) {
        this.colorTheme = colorTheme == null ? ColorTheme.DARK : colorTheme;
    }

    @Generated
    public ModuleRegistry getModuleRegistry() {
        return this.moduleRegistry;
    }

    @Generated
    public NavigationCommandService getNavigationCommandService() {
        return this.navigationCommandService;
    }

    @Generated
    public FriendListManager getFriendListManager() {
        return this.friendListManager;
    }

    @Generated
    public RotationManager getRotationManager() {
        return this.rotationManager;
    }

    @Generated
    public ClientServiceRegistry getClientServiceRegistry() {
        return this.clientServiceRegistry;
    }

    @Generated
    public FriendManager getFriendManager() {
        return this.friendManager;
    }

    @Generated
    public MediaTracker getMediaTracker() {
        return this.mediaTracker;
    }

    @Generated
    public MicrosoftClientConfiguration getMicrosoftClientConfiguration() {
        return this.microsoftClientConfiguration;
    }

    @Generated
    public UiComponentProcessor getUiComponentProcessor() {
        return this.uiComponentProcessor;
    }

    @Generated
    public DynamicIslandManager getDynamicIslandManager() {
        return this.dynamicIslandManager;
    }

    @Generated
    public HudElementRegistry getHudElementRegistry() {
        return this.hudElementRegistry;
    }

    @Generated
    public MicrosoftRefreshTokenRequest getRefreshTokenRequest() {
        return this.refreshTokenRequest;
    }

    @Generated
    public SettingDataStore getSettingDataStore() {
        return this.settingDataStore;
    }

    @Generated
    public HandSwingPresetManager getHandSwingPresetManager() {
        return this.handSwingPresetManager;
    }

    @Generated
    public ServerTickRateTracker getServerTickRateTracker() {
        return this.serverTickRateTracker;
    }

    @Generated
    public ShaderPostProcessor getShaderPostProcessor() {
        return this.shaderPostProcessor;
    }

    @Generated
    public ChatKeyBindingHandler getChatKeyBindingHandler() {
        return this.chatKeyBindingHandler;
    }

    @Generated
    public ChatWaypointListener getChatWaypointListener() {
        return this.chatWaypointListener;
    }

    @Generated
    public AssistScreen getAssistScreen() {
        return this.assistScreen;
    }

    @Generated
    public TaksaParticleState getTaksaParticleState() {
        return this.taksaParticleState;
    }

    @Generated
    public WaypointStore getWaypointStore() {
        return this.waypointStore;
    }

    @Generated
    public SwingPresetFileManager getSwingPresetFileManager() {
        return this.swingPresetFileManager;
    }

    @Generated
    public StaffListManager getStaffListManager() {
        return this.staffListManager;
    }

    @Generated
    public ScriptRegistry getScriptRegistry() {
        return this.scriptRegistry;
    }

    @Generated
    public MicrosoftDeviceCodeRequest getDeviceCodeRequest() {
        return this.deviceCodeRequest;
    }

    @Generated
    public LocalRedirectLoginFlow getLocalRedirectLoginFlow() {
        return this.localRedirectLoginFlow;
    }

    @Generated
    public MinecraftScreenBase getMinecraftScreen() {
        return this.minecraftScreen;
    }

    @Generated
    public boolean isPanicMode() {
        return this.panicMode;
    }

    @Generated
    public void setMinecraftScreen(MinecraftScreenBase minecraftScreenBase) {
        this.minecraftScreen = minecraftScreenBase;
    }

    @Generated
    public void setPanicMode(boolean bl) {
        this.panicMode = bl;
    }
}
