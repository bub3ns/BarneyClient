/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.modules;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleToggleListener;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.localization.Localization;

public interface ModuleContract
extends SettingOwner,
ClientAccess,
WindowMetricsProvider,
ModuleToggleListener {
    public void disable();

    public void enable();

    public void onTick();

    public boolean isDisableLocked();

    public String getName();

    default public String getDescription() {
        String string = "modules.descriptions.%s".formatted(this.getName().toLowerCase().replace(" ", "_"));
        return Localization.translate(string);
    }

    public int getKeyBind();

    public ModuleCategory getCategory();

    public boolean isEnabled();

    public boolean isVisible();

    default public boolean isAvailable() {
        return true;
    }

    public Animation getTimer();

    public void setKeyBind(int var1);

    public void setEnabled(boolean var1, boolean var2);
}

