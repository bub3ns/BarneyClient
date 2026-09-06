/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.api.scripts;

import lombok.Generated;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;

public class ScriptModule
extends Module {
    private String descriptionText = "";

    public ScriptModule(String string, ModuleCategory moduleCategory, int n) {
        super(string, moduleCategory, n);
    }

    @Generated
    public void setDescription(String string) {
        this.descriptionText = string;
    }

    @Override
    @Generated
    public String getDescription() {
        return this.descriptionText;
    }
}

