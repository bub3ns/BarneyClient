/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.settings;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;

public final class SettingEntry {
    private final int keyCode;
    private final String command;

    public SettingEntry(int n, String string) {
        this.keyCode = n;
        this.command = string;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "keyCode", "command");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "keyCode", "command");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "keyCode", "command");
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public String getCommand() {
        return this.command;
    }
}

