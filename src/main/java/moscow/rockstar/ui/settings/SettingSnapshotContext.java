package moscow.rockstar.ui.settings;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import moscow.rockstar.settings.Setting;
import net.minecraft.client.gui.screen.Screen;

public final class SettingSnapshotContext implements AutoCloseable {
    static final SettingSnapshotContext GLOBAL_CONTEXT = new SettingSnapshotContext(true);
    private final IdentityHashMap<Setting, JsonElement> originalValuesBySetting = new IdentityHashMap<>();
    private Screen screen;
    private final boolean tracking;
    private boolean closed;

    SettingSnapshotContext() {
        this(false);
        SettingSnapshotCache.activeContext = this;
    }

    private SettingSnapshotContext(boolean tracking) {
        this.tracking = tracking;
    }

    void captureOriginalValue(Screen screen, Setting setting, JsonElement value) {
        if (this.tracking || this.closed || this.screen != null && this.screen != screen) {
            return;
        }
        this.screen = screen;
        this.originalValuesBySetting.putIfAbsent(setting, value);
    }

    public void restoreEnabledFlag() {
        if (this.tracking || this.closed) {
            return;
        }
        this.closed = true;
        SettingSnapshotCache.activeContext = null;
        if (this.screen == null || this.originalValuesBySetting.isEmpty()) {
            return;
        }
        ArrayList<SettingValueSnapshot> snapshots = new ArrayList<>(this.originalValuesBySetting.size());
        for (Map.Entry<Setting, JsonElement> entry : this.originalValuesBySetting.entrySet()) {
            snapshots.add(new SettingValueSnapshot(entry.getKey(), entry.getValue()));
        }
        SettingSnapshotCache.addSnapshot(SettingSnapshotCache.pendingSnapshots, new SettingScreenSnapshot(
                this.screen, snapshots, System.currentTimeMillis()));
        SettingSnapshotCache.appliedSnapshots.clear();
    }

    @Override
    public void close() {
        this.restoreEnabledFlag();
    }
}
