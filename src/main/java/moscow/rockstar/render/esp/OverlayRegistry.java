/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  lombok.Generated
 */
package moscow.rockstar.render.esp;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.visuals.esp.entities.ESP;
import moscow.rockstar.modules.visuals.esp.entities.EntityArrowRenderer;
import moscow.rockstar.modules.visuals.esp.entities.EntityBoxRenderer;
import moscow.rockstar.modules.visuals.esp.entities.Fill;
import moscow.rockstar.modules.visuals.esp.entities.Flame;
import moscow.rockstar.modules.visuals.esp.entities.Glow;
import moscow.rockstar.modules.visuals.esp.entities.JumpCircles;
import moscow.rockstar.modules.visuals.esp.entities.Nametags;
import moscow.rockstar.modules.visuals.esp.entities.TaksaESP;
import moscow.rockstar.render.esp.FriendMarkerRenderer;
import pyrock.events.window.KeyPressEvent;
import pyrock.events.window.MouseEvent;

public class OverlayRegistry {
    private static final OverlayRegistry INSTANCE = new OverlayRegistry();
    private final List<TargetRenderModule> registeredOverlays = new ArrayList<TargetRenderModule>();
    private final Map<String, JsonObject> pendingOverlayConfigs = new ConcurrentHashMap<String, JsonObject>();
    private boolean listenersRegistered;
    private final EventListener<KeyPressEvent> keyboardListener = keyPressEvent -> {
        if (keyPressEvent.getAction() == 1) {
            this.handleInputCode(keyPressEvent.getKey());
        }
    };
    private final EventListener<MouseEvent> mouseListener = mouseEvent -> {
        if (mouseEvent.getAction() == 1) {
            this.handleInputCode(mouseEvent.getButton());
        }
    };

    private OverlayRegistry() {
    }

    public static boolean isEspEnabled() {
        ESP eSP = RockstarClient.create().getModuleRegistry().getModule(ESP.class);
        return eSP != null && eSP.isEnabled();
    }

    public void registerDefaultOverlays() {
        if (!this.listenersRegistered) {
            RockstarClient.create().getEventBus().registerListeners(this);
            this.listenersRegistered = true;
        }
        this.registerOverlay(new Glow());
        this.registerOverlay(new Flame());
        this.registerOverlay(new Fill());
        this.registerOverlay(new Nametags());
        this.registerOverlay(new EntityArrowRenderer());
        this.registerOverlay(new FriendMarkerRenderer());
        this.registerOverlay(new TaksaESP());
        this.registerOverlay(new EntityBoxRenderer());
        this.registerOverlay(new JumpCircles());
    }

    private void handleInputCode(int n) {
        if (n == -1) {
            return;
        }
        if (MinecraftClient.getInstance().currentScreen != null) {
            return;
        }
        if (!OverlayRegistry.isEspEnabled()) {
            return;
        }
        for (TargetRenderModule overlay : this.registeredOverlays) {
            overlay.handleInput(n);
        }
    }

    private void registerOverlay(TargetRenderModule overlay) {
        this.registeredOverlays.add(overlay);
        overlay.registerListeners();
        this.applyPendingOverlayConfig(overlay);
    }

    public void registerOverlayIfAbsent(TargetRenderModule overlay) {
        if (overlay == null || this.registeredOverlays.contains(overlay)) {
            return;
        }
        this.registerOverlay(overlay);
    }

    public void unregisterOverlay(TargetRenderModule overlay) {
        if (overlay == null || !this.registeredOverlays.remove(overlay)) {
            return;
        }
        try {
            this.pendingOverlayConfigs.put(overlay.getName(), overlay.serializeConfiguration());
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.warn("Config: \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u044d\u043b\u0435\u043c\u0435\u043d\u0442 ESP {}", (Object)overlay.getName(), (Object)exception);
        }
        RockstarClient.create().getEventBus().unregisterListeners(overlay);
    }

    public void unregisterOverlayByOwner(Object object) {
        for (TargetRenderModule overlay : new ArrayList<TargetRenderModule>(this.registeredOverlays)) {
            if (!(overlay instanceof EntityTargetRenderer dynamicOverlay) || dynamicOverlay.getOwner() != object) continue;
            this.unregisterOverlay(dynamicOverlay);
        }
    }

    private void applyPendingOverlayConfig(TargetRenderModule overlay) {
        JsonObject jsonObject = this.pendingOverlayConfigs.remove(overlay.getName());
        if (jsonObject != null) {
            overlay.applyConfiguration(jsonObject);
        }
    }

    public void applyPendingConfigByOwner(Object object) {
        for (TargetRenderModule overlay : new ArrayList<TargetRenderModule>(this.registeredOverlays)) {
            if (!(overlay instanceof EntityTargetRenderer dynamicOverlay) || dynamicOverlay.getOwner() != object) continue;
            this.applyPendingOverlayConfig(dynamicOverlay);
        }
    }

    public <T extends TargetRenderModule> T findOverlayByType(Class<T> clazz) {
        for (TargetRenderModule overlay : this.registeredOverlays) {
            if (!clazz.isInstance(overlay)) continue;
            return clazz.cast(overlay);
        }
        return null;
    }

    public TargetRenderModule findOverlayByName(String string) {
        for (TargetRenderModule overlay : this.registeredOverlays) {
            if (!overlay.getName().equals(string)) continue;
            return overlay;
        }
        return null;
    }

    public JsonArray serializeOverlayConfig() {
        JsonArray jsonArray = new JsonArray();
        HashSet<String> hashSet = new HashSet<String>();
        for (TargetRenderModule overlay : this.registeredOverlays) {
            hashSet.add(overlay.getName());
            jsonArray.add(overlay.serializeConfiguration());
        }
        for (Map.Entry entry : this.pendingOverlayConfigs.entrySet()) {
            if (hashSet.contains(entry.getKey())) continue;
            jsonArray.add((JsonElement)((JsonObject)entry.getValue()).deepCopy());
        }
        return jsonArray;
    }

    public void loadOverlayConfig(JsonArray jsonArray) {
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (!jsonObject.has("name")) continue;
            String string = jsonObject.get("name").getAsString();
            TargetRenderModule overlay = this.findOverlayByName(string);
            if (overlay != null) {
                overlay.applyConfiguration(jsonObject);
                continue;
            }
            this.pendingOverlayConfigs.put(string, jsonObject.deepCopy());
        }
    }

    @Generated
    public static OverlayRegistry getInstance() {
        return INSTANCE;
    }

    @Generated
    public List<TargetRenderModule> getRegisteredOverlays() {
        return this.registeredOverlays;
    }
}
