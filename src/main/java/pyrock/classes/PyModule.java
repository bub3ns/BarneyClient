/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.classes;

import java.util.List;
import lombok.Generated;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.api.scripts.ScriptModule;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.ModuleRegistry;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.settings.Setting;
import pyrock.classes.PySetting;

public class PyModule {
    private final ModuleContract module;
    private final String name;
    private final String category;

    public PyModule(String string, String string2) {
        this.name = string.trim();
        this.category = string2.trim();
        ModuleCategory moduleCategory = ModuleCategory.fromName(this.category);
        if (moduleCategory == null) {
            moduleCategory = ModuleCategory.OTHER;
        }
        ScriptModule scriptModule = new ScriptModule(this.name, moduleCategory, -1);
        RockstarClient.create().getModuleRegistry().getModules().add(scriptModule);
        ModuleRegistry.incrementRegistryCount();
        ScriptDescriptor.registerModule(scriptModule);
        this.module = scriptModule;
    }

    public PyModule(ModuleContract moduleContract) {
        this.module = moduleContract;
        this.name = moduleContract.getName();
        this.category = moduleContract.getCategory().name();
    }

    public PySetting[] settings() {
        List<Setting> list = this.module.getSettings();
        PySetting[] pySettingArray = new PySetting[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            pySettingArray[i] = new PySetting(list.get(i));
        }
        return pySettingArray;
    }

    public PySetting[] settingsApi() {
        return this.settings();
    }

    public boolean isEnabled() {
        return this.module.isEnabled();
    }

    public PyModule setEnabled(boolean bl) {
        this.module.setEnabled(bl, false);
        return this;
    }

    public PyModule toggle() {
        this.module.toggle();
        return this;
    }

    public int getKey() {
        return this.module.getKeyBind();
    }

    public String getKeyName() {
        return moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(this.module.getKeyBind());
    }

    public PyModule setKey(int n) {
        this.module.setKeyBind(n);
        return this;
    }

    public String getDesc() {
        return this.module.getDescription();
    }

    public PyModule setDesc(String string) {
        ModuleContract moduleContract = this.module;
        if (moduleContract instanceof ScriptModule) {
            ScriptModule scriptModule = (ScriptModule)moduleContract;
            scriptModule.setDescription(string);
        }
        return this;
    }

    @Generated
    public ModuleContract getModule() {
        return this.module;
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getCategory() {
        return this.category;
    }
}
