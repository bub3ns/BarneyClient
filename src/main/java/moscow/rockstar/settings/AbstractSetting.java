/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.settings;

import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.settings.SettingSnapshotCache;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSetting
implements Setting {
    protected final String key;
    private final SettingOwner owner;
    @NotNull
    private final BooleanSupplier visibilityCondition;

    public AbstractSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        this.owner = settingOwner;
        this.key = string;
        this.visibilityCondition = booleanSupplier;
        this.registerWithOwner(settingOwner);
    }

    public AbstractSetting(@NotNull SettingOwner settingOwner, String string) {
        this(settingOwner, string, () -> false);
    }

    @Override
    public final void registerWithOwner(SettingOwner settingOwner) {
        settingOwner.getSettings().add(this);
    }

    public final void notifyChange() {
        SettingSnapshotCache.applyCollectionProcessorCacheToSetting(this);
    }

    @Override
    public final String getDescriptionKey() {
        return this.getName() + ".description";
    }

    @Override
    @Generated
    public String getName() {
        return this.key;
    }

    @Generated
    public SettingOwner getOwner() {
        return this.owner;
    }

    @Override
    @NotNull
    @Generated
    public BooleanSupplier getVisibilityCondition() {
        return this.visibilityCondition;
    }
}

