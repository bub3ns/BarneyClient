/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.settings;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingOwner;

public class SettingGroup
implements SettingOwner {
    protected final List<Setting> settings = new ArrayList<Setting>();

    @Override
    @Generated
    public List<Setting> getSettings() {
        return this.settings;
    }
}

