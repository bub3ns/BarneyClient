/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value=RetentionPolicy.RUNTIME)
public @interface PluginMetadata {
    public String name();

    public String description() default "rock";
}

