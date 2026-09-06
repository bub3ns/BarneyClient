/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.registry;

import java.io.IOException;

@FunctionalInterface
public interface ServiceSingleton<T> {
    public T get() throws IOException;
}

