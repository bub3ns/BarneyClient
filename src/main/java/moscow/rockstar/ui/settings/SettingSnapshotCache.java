/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  net.minecraft.Screen
 */
package moscow.rockstar.ui.settings;

import com.google.gson.JsonElement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.settings.SettingScreenSnapshot;
import moscow.rockstar.ui.settings.SettingSnapshotContext;
import moscow.rockstar.ui.settings.SettingValueSnapshot;
import net.minecraft.client.gui.screen.Screen;

public final class SettingSnapshotCache {
    static final Deque<SettingScreenSnapshot> pendingSnapshots = new ArrayDeque<SettingScreenSnapshot>();
    static final Deque<SettingScreenSnapshot> appliedSnapshots = new ArrayDeque<SettingScreenSnapshot>();
    static SettingSnapshotContext activeContext;
    private static boolean cacheOpen;

    private SettingSnapshotCache() {
    }

    public static void applyCollectionProcessorCacheToSetting(Setting setting) {
        JsonElement jsonElement;
        if (cacheOpen || setting == null) {
            return;
        }
        Screen class_4372 = SettingSnapshotCache.getclass437();
        if (class_4372 == null) {
            return;
        }
        try {
            jsonElement = setting.serialize().deepCopy();
        }
        catch (RuntimeException runtimeException) {
            return;
        }
        if (activeContext != null) {
            activeContext.captureOriginalValue(class_4372, setting, jsonElement);
            return;
        }
        long l = System.currentTimeMillis();
        SettingScreenSnapshot settingScreenSnapshot = pendingSnapshots.peekFirst();
        if (settingScreenSnapshot != null && settingScreenSnapshot.getScreen() == class_4372 && settingScreenSnapshot.getSettingValues().size() == 1 && settingScreenSnapshot.getSettingValues().get(0).getSetting() == setting && l - settingScreenSnapshot.getCapturedAtMillis() < 350L) {
            return;
        }
        SettingSnapshotCache.addSnapshot(pendingSnapshots, new SettingScreenSnapshot(class_4372, List.of(new SettingValueSnapshot(setting, jsonElement)), l));
        appliedSnapshots.clear();
    }

    public static SettingSnapshotContext getInner2() {
        return activeContext == null ? new SettingSnapshotContext() : SettingSnapshotContext.GLOBAL_CONTEXT;
    }

    public static boolean isCollectionCacheReady() {
        return SettingSnapshotCache.isDequeAndDequeAndCollectionValid(pendingSnapshots, appliedSnapshots, null);
    }

    public static boolean isCollectionProcessorCacheTargetReady() {
        return SettingSnapshotCache.isDequeAndDequeAndCollectionValid(appliedSnapshots, pendingSnapshots, null);
    }

    public static boolean isSettingOwnerValid(SettingOwner settingOwner) {
        return SettingSnapshotCache.isDequeAndDequeAndCollectionValid(pendingSnapshots, appliedSnapshots, settingOwner == null ? null : List.of(settingOwner));
    }

    public static boolean acceptsSettingOwnerForCollectionProcessorCacheTargetReady(SettingOwner settingOwner) {
        return SettingSnapshotCache.isDequeAndDequeAndCollectionValid(appliedSnapshots, pendingSnapshots, settingOwner == null ? null : List.of(settingOwner));
    }

    public static boolean isCollectionValid(Collection<? extends SettingOwner> collection) {
        return SettingSnapshotCache.isDequeAndDequeAndCollectionValid(pendingSnapshots, appliedSnapshots, collection);
    }

    public static boolean acceptsCollectionForCollectionProcessorCacheTargetReady(Collection<? extends SettingOwner> collection) {
        return SettingSnapshotCache.isDequeAndDequeAndCollectionValid(appliedSnapshots, pendingSnapshots, collection);
    }

    public static boolean acceptsSettingOwnerForCollectionProcessorCacheStateReady(SettingOwner settingOwner) {
        return SettingSnapshotCache.isDequeAndCollectionValid(pendingSnapshots, settingOwner == null ? null : List.of(settingOwner));
    }

    public static boolean acceptsSettingOwnerForCollectionProcessorCacheEnvironmentReady(SettingOwner settingOwner) {
        return SettingSnapshotCache.isDequeAndCollectionValid(appliedSnapshots, settingOwner == null ? null : List.of(settingOwner));
    }

    public static boolean acceptsCollectionForCollectionProcessorCacheStateReady(Collection<? extends SettingOwner> collection) {
        return SettingSnapshotCache.isDequeAndCollectionValid(pendingSnapshots, collection);
    }

    public static boolean acceptsCollectionForCollectionProcessorCacheEnvironmentReady(Collection<? extends SettingOwner> collection) {
        return SettingSnapshotCache.isDequeAndCollectionValid(appliedSnapshots, collection);
    }

    public static long calculateLongFromSettingOwner(SettingOwner settingOwner) {
        return SettingSnapshotCache.calculateLongFromDequeAndCollection(pendingSnapshots, settingOwner == null ? null : List.of(settingOwner));
    }

    public static long getOwnerSnapshotVersion(SettingOwner settingOwner) {
        return SettingSnapshotCache.calculateLongFromDequeAndCollection(appliedSnapshots, settingOwner == null ? null : List.of(settingOwner));
    }

    private static boolean isDequeAndCollectionValid(Deque<SettingScreenSnapshot> deque, Collection<? extends SettingOwner> collection) {
        Screen class_4372 = SettingSnapshotCache.getclass437();
        if (class_4372 == null) {
            return false;
        }
        for (SettingScreenSnapshot settingScreenSnapshot : deque) {
            if (settingScreenSnapshot.getScreen() != class_4372 || !SettingSnapshotCache.isInnerAndCollectionValid(settingScreenSnapshot, collection)) continue;
            return true;
        }
        return false;
    }

    private static long calculateLongFromDequeAndCollection(Deque<SettingScreenSnapshot> deque, Collection<? extends SettingOwner> collection) {
        Screen class_4372 = SettingSnapshotCache.getclass437();
        if (class_4372 == null) {
            return Long.MIN_VALUE;
        }
        for (SettingScreenSnapshot settingScreenSnapshot : deque) {
            if (settingScreenSnapshot.getScreen() != class_4372 || !SettingSnapshotCache.isInnerAndCollectionValid(settingScreenSnapshot, collection)) continue;
            return settingScreenSnapshot.getCapturedAtMillis();
        }
        return Long.MIN_VALUE;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean isDequeAndDequeAndCollectionValid(Deque<SettingScreenSnapshot> deque, Deque<SettingScreenSnapshot> deque2, Collection<? extends SettingOwner> collection) {
        Screen class_4372 = SettingSnapshotCache.getclass437();
        if (class_4372 == null) {
            return false;
        }
        Iterator<SettingScreenSnapshot> iterator = deque.iterator();
        while (iterator.hasNext()) {
            SettingScreenSnapshot settingScreenSnapshot = iterator.next();
            if (settingScreenSnapshot.getScreen() != class_4372 || !SettingSnapshotCache.isInnerAndCollectionValid(settingScreenSnapshot, collection)) continue;
            iterator.remove();
            ArrayList<SettingValueSnapshot> arrayList = new ArrayList<SettingValueSnapshot>(settingScreenSnapshot.getSettingValues().size());
            for (SettingValueSnapshot settingValueSnapshot : settingScreenSnapshot.getSettingValues()) {
                try {
                    arrayList.add(new SettingValueSnapshot(settingValueSnapshot.getSetting(), settingValueSnapshot.getSetting().serialize().deepCopy()));
                }
                catch (RuntimeException runtimeException) {}
            }
            boolean bl = arrayList.size() != settingScreenSnapshot.getSettingValues().size();
            for (int i = 0; !bl && i < arrayList.size(); ++i) {
                bl = !((SettingValueSnapshot)arrayList.get(i)).getJsonElement().equals(settingScreenSnapshot.getSettingValues().get(i).getJsonElement());
            }
            if (!bl) continue;
            cacheOpen = true;
            try {
                for (SettingValueSnapshot settingValueSnapshot : settingScreenSnapshot.getSettingValues()) {
                    settingValueSnapshot.getSetting().deserialize(settingValueSnapshot.getJsonElement().deepCopy());
                }
            }
            finally {
                cacheOpen = false;
            }
            if (!arrayList.isEmpty()) {
                SettingSnapshotCache.addSnapshot(deque2, new SettingScreenSnapshot(class_4372, arrayList, settingScreenSnapshot.getCapturedAtMillis()));
            }
            return true;
        }
        return false;
    }

    private static boolean isInnerAndCollectionValid(SettingScreenSnapshot settingScreenSnapshot, Collection<? extends SettingOwner> collection) {
        if (collection == null) {
            return true;
        }
        if (collection.isEmpty()) {
            return false;
        }
        for (SettingValueSnapshot settingValueSnapshot : settingScreenSnapshot.getSettingValues()) {
            AbstractSetting abstractSetting;
            Setting setting = settingValueSnapshot.getSetting();
            if (setting instanceof AbstractSetting && collection.contains((abstractSetting = (AbstractSetting)setting).getOwner())) continue;
            return false;
        }
        return true;
    }

    static void addSnapshot(Deque<SettingScreenSnapshot> deque, SettingScreenSnapshot settingScreenSnapshot) {
        deque.addFirst(settingScreenSnapshot);
        while (deque.size() > 100) {
            deque.removeLast();
        }
    }

    private static Screen getclass437() {
        return MinecraftClient.getInstance().currentScreen;
    }
}
