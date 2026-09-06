/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a {@link ConfigEntry} with the name and file extension of the document
 * it persists.  ORIGINAL: {@code rockstar/ilIlil/IiIIiIIi}.
 */
@Retention(value=RetentionPolicy.RUNTIME)
public @interface ConfigName {
    /** ORIGINAL: {@code I()Ljava/lang/String;} */
    public String value();

    /** ORIGINAL: {@code i()Ljava/lang/String;} */
    public String extension() default "rock";
}
