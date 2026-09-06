/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.screens;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import moscow.rockstar.ui.screens.AssistItem;

public final class AssistGroup {
    private final String title;
    private final List<AssistItem> items;

    public AssistGroup(String string, List<AssistItem> list) {
        this.title = string;
        this.items = list;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "title", "items");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "title", "items");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "title", "items");
    }

    public String getTitle() {
        return this.title;
    }

    public List<AssistItem> getItems() {
        return this.items;
    }
}

