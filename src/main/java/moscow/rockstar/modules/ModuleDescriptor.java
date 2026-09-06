/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.modules;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.modules.ModuleCategory;

public final class ModuleDescriptor {
    private final String name;
    private final ModuleCategory category;
    private final int key;
    private final boolean disableOnQuit;
    private final boolean enabledAtStartup;
    private final String description;

    public ModuleDescriptor(String string, ModuleCategory moduleCategory, int n, boolean bl, boolean bl2, String string2) {
        this.name = string;
        this.category = moduleCategory;
        this.key = n;
        this.disableOnQuit = bl;
        this.enabledAtStartup = bl2;
        this.description = string2;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "name", "category", "key", "disableOnQuit", "enabledAtStartup", "description");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "name", "category", "key", "disableOnQuit", "enabledAtStartup", "description");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "name", "category", "key", "disableOnQuit", "enabledAtStartup", "description");
    }

    public String getName() {
        return this.name;
    }

    public ModuleCategory getCategory() {
        return this.category;
    }

    public int getKey() {
        return this.key;
    }

    public boolean isDisabledOnQuit() {
        return this.disableOnQuit;
    }

    public boolean isEnabledAtStartup() {
        return this.enabledAtStartup;
    }

    public String getDescription() {
        return this.description;
    }
}

