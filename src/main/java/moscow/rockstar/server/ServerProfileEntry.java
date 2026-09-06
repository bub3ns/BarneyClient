/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.server;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.server.PartySize;

public final class ServerProfileEntry {
    private final int number;
    private final PartySize category;
    private final int categorySlot;
    private final int serverSlot;

    public ServerProfileEntry(int n, PartySize partySize, int n2, int n3) {
        this.number = n;
        this.category = partySize;
        this.categorySlot = n2;
        this.serverSlot = n3;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "number", "category", "categorySlot", "serverSlot");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "number", "category", "categorySlot", "serverSlot");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "number", "category", "categorySlot", "serverSlot");
    }

    public int getNumber() {
        return this.number;
    }

    public PartySize getCategory() {
        return this.category;
    }

    public int getCategorySlot() {
        return this.categorySlot;
    }

    public int getServerSlot() {
        return this.serverSlot;
    }
}

