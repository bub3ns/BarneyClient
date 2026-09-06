/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  lombok.Generated
 */
package moscow.rockstar.modules;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventHandlerRecord;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.visuals.audio.Sounds;
import moscow.rockstar.modules.visuals.audio.SoundEffectPlayer;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.localization.Language;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationType;
import pyrock.classes.PyModule;
import pyrock.events.client.ModuleToggledEvent;
import ua.mintantileak.profile.Profile;
import ua.mintantileak.profile.Role;
import net.minecraft.client.MinecraftClient;

public abstract class Module
implements ModuleContract {
    private int keyBind;
    private ModuleCategory category;
    private boolean enabled;
    private boolean visible;
    private String name;
    private boolean disableLocked;
    private boolean alwaysEnabled;
    private boolean adminOnly;
    private List<Setting> settings = new ArrayList<Setting>();
    private int savedKeyBind;
    private final Map<String, JsonElement> savedSettings = new HashMap<String, JsonElement>();
    private final Animation toggleAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);

    public Module() {
        ModuleInfo moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);
        this.disableLocked = moduleInfo.disableLocked();
        this.alwaysEnabled = moduleInfo.alwaysEnabled();
        this.name = moduleInfo.name();
        this.category = moduleInfo.category();
        this.savedKeyBind = this.keyBind = moduleInfo.keyBind();
        this.adminOnly = moduleInfo.adminOnly();
    }

    public Module(String string, ModuleCategory moduleCategory, int n) {
        this.name = string;
        this.category = moduleCategory;
        this.keyBind = n;
        this.savedKeyBind = n;
    }

    @Override
    public final void toggle() {
        this.setEnabled(!this.enabled, false);
    }

    @Override
    public boolean isAvailable() {
        return !this.adminOnly || Module.isAdminOrOwner();
    }

    public static boolean isAdminOrOwner() {
        Role role = Profile.getRole();
        return role == Role.ADMIN || role == Role.OWNER;
    }

    public static boolean isLoggedIn() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.getSession() != null && !client.getSession().getUsername().isBlank();
    }

    @Override
    public final void setKeyBind(int n) {
        if (this.keyBind == n) {
            return;
        }
        this.keyBind = n;
        RockstarClient.create().getEventBus().post(new EventHandlerRecord(this));
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onTick() {
    }

    @Override
    public final void disable() {
        this.setEnabled(false, false);
    }

    @Override
    public final void enable() {
        this.setEnabled(true, false);
    }

    @Override
    public final void setEnabled(boolean bl, boolean bl2) {
        if (!bl && this.alwaysEnabled) {
            return;
        }
        if (this.enabled == bl) {
            return;
        }
        this.enabled = bl;
        RockstarClient.create().getEventBus().post(new ModuleToggledEvent(new PyModule(this), this.enabled));
        if (!(this instanceof Menu) && RockstarClient.create().getModuleRegistry().getModule(Sounds.class).isEnabled() && !bl2) {
            SoundEffectPlayer.playToggle(RockstarClient.create().getModuleRegistry().getModule(Sounds.class).getVolume(), this.enabled ? 1.1f : 1.0f);
        }
        if (this.enabled) {
            RockstarClient.create().getEventBus().registerListeners(this);
            if (!(bl2 || this instanceof Menu)) {
                RockstarClient.create().getUiComponentProcessor().enqueueNotificationRequest(NotificationType.SUCCESS, this.name.replace(" ", "") + " " + Localization.translate("enabled") /* + (Localization.getLanguage() == Language.RU_RU ? moscow.rockstar.util.RussianWordEnding.notificationSuffix(this.name) : "") */);
            }
            this.onEnable();
        } else {
            RockstarClient.create().getEventBus().unregisterListeners(this);
            if (!(bl2 || this instanceof Menu)) {
                RockstarClient.create().getUiComponentProcessor().enqueueNotificationRequest(NotificationType.ERROR, this.name.replace(" ", "") + " " + Localization.translate("disabled") /* + (Localization.getLanguage() == Language.RU_RU ? moscow.rockstar.util.RussianWordEnding.notificationSuffix(this.name) : "") */);
            }
            this.onDisable();
        }
    }

    public String getSettingKey(String string) {
        return "modules.settings." + this.getName().toLowerCase().replace(" ", "_") + "." + string;
    }

    public final void saveSettings() {
        this.savedKeyBind = this.keyBind;
        this.savedSettings.clear();
        for (Setting setting : this.settings) {
            this.savedSettings.put(setting.getName(), setting.serialize());
        }
    }

    public final void loadSettings() {
        this.setKeyBind(this.savedKeyBind);
        for (Setting setting : this.settings) {
            JsonElement jsonElement = this.savedSettings.get(setting.getName());
            if (jsonElement == null) continue;
            setting.deserialize(jsonElement);
        }
    }

    @Override
    @Generated
    public int getKeyBind() {
        return this.keyBind;
    }

    @Override
    @Generated
    public ModuleCategory getCategory() {
        return this.category;
    }

    @Override
    @Generated
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    @Generated
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    @Generated
    public String getName() {
        return this.name;
    }

    @Override
    @Generated
    public boolean isDisableLocked() {
        return this.disableLocked;
    }

    @Generated
    public boolean isAlwaysEnabled() {
        return this.alwaysEnabled;
    }

    @Generated
    public boolean isAdminOnly() {
        return this.adminOnly;
    }

    @Override
    @Generated
    public List<Setting> getSettings() {
        return this.settings;
    }

    @Generated
    public int getSavedKeyBind() {
        return this.savedKeyBind;
    }

    @Generated
    public Map<String, JsonElement> getSavedSettings() {
        return this.savedSettings;
    }

    @Override
    @Generated
    public Animation getTimer() {
        return this.toggleAnimation;
    }

    @Generated
    public void setCategory(ModuleCategory moduleCategory) {
        this.category = moduleCategory;
    }

    @Generated
    public void setEnabled(boolean bl) {
        this.enabled = bl;
    }

    @Generated
    public void setVisible(boolean bl) {
        this.visible = bl;
    }

    @Generated
    public void setName(String string) {
        this.name = string;
    }

    @Generated
    public void setDisableLocked(boolean bl) {
        this.disableLocked = bl;
    }

    @Generated
    public void setAlwaysEnabled(boolean bl) {
        this.alwaysEnabled = bl;
    }

    @Generated
    public void setSettings(List<Setting> list) {
        this.settings = list;
    }

    @Generated
    public void setSavedKeyBind(int n) {
        this.savedKeyBind = n;
    }
}
