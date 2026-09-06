/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.modules;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import moscow.rockstar.modules.ModuleCategory;

@Retention(value=RetentionPolicy.RUNTIME)
public @interface ModuleInfo {
    public String name();

    public ModuleCategory category();

    public int keyBind() default -1;

    public boolean hidden() default false;

    public boolean disableLocked() default false;

    public boolean alwaysEnabled() default false;

    public String description() default "modules.descriptions.no_description";

    public boolean adminOnly() default false;

}
