/*
 * Decompiled with CFR 0.152.
 */
package ua.mintantileak.spk;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.CLASS)
public @interface Compile {
    public int obfuscation() default 0;
}

