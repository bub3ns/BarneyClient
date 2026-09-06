/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 */
package pyrock.classes;

import java.util.ArrayList;
import java.util.List;
import jep.python.PyCallable;
import moscow.rockstar.api.data.ClientConfig;
import moscow.rockstar.api.data.ClientConfigManager;
import moscow.rockstar.api.data.ConfigEntry;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.hud.DynamicIslandEntry;
import moscow.rockstar.ui.hud.DynamicIslandManager;
import moscow.rockstar.modules.visuals.hud.DynamicIslandHud;
import pyrock.classes.PyIslandStatus;

public class PyDynamicIsland {
    public PyIslandStatus add(String string, double d, double d2, double d3, boolean bl, PyCallable pyCallable, PyCallable pyCallable2, PyCallable pyCallable3, PyCallable pyCallable4) {
        DynamicIslandManager manager = PyDynamicIsland.island();
        if (manager == null) {
            return null;
        }
        String string2 = PyDynamicIsland.normalizeName(string);
        ScriptDescriptor scriptDescriptor = ScriptDescriptor.getCurrentScript();
        PyDynamicIsland.removeOwnedDuplicate(scriptDescriptor, string2);
        MultiBooleanSetting statusSetting = manager.getStatusSetting();
        PyIslandStatus pyIslandStatus = new PyIslandStatus(scriptDescriptor, statusSetting, string2, Math.max(1.0f, (float)d), Math.max(1.0f, (float)d2), Math.max(0.0f, (float)d3)).expandable(bl);
        if (pyCallable != null) {
            pyIslandStatus.render(pyCallable);
        }
        if (pyCallable2 != null) {
            pyIslandStatus.visibleWhen(pyCallable2);
        }
        if (pyCallable3 != null) {
            pyIslandStatus.measure(pyCallable3);
        }
        if (pyCallable4 != null) {
            pyIslandStatus.onClick(pyCallable4);
        }
        ScriptDescriptor.registerMultiBooleanOption(statusSetting, pyIslandStatus);
        PyDynamicIsland.applySavedState();
        manager.add(pyIslandStatus);
        return pyIslandStatus;
    }

    public PyIslandStatus add(String string, double d, double d2, double d3) {
        return this.add(string, d, d2, d3, false, null, null, null, null);
    }

    public PyIslandStatus add(String string, double d, double d2) {
        return this.add(string, d, d2, 7.0);
    }

    public PyIslandStatus add(String string) {
        return this.add(string, 48.0, 15.0, 7.0);
    }

    public PyIslandStatus find(String string) {
        DynamicIslandManager manager = PyDynamicIsland.island();
        if (manager == null) {
            return null;
        }
        String string2 = PyDynamicIsland.normalizeName(string);
        for (DynamicIslandEntry entry : manager.getEntries()) {
            PyIslandStatus pyIslandStatus;
            if (!(entry instanceof PyIslandStatus) || !(pyIslandStatus = (PyIslandStatus)entry).getName().equalsIgnoreCase(string2)) continue;
            return pyIslandStatus;
        }
        return null;
    }

    public List<PyIslandStatus> all() {
        DynamicIslandManager manager = PyDynamicIsland.island();
        ArrayList<PyIslandStatus> arrayList = new ArrayList<PyIslandStatus>();
        if (manager == null) {
            return arrayList;
        }
        for (DynamicIslandEntry entry : manager.getEntries()) {
            if (!(entry instanceof PyIslandStatus)) continue;
            PyIslandStatus pyIslandStatus = (PyIslandStatus)entry;
            arrayList.add(pyIslandStatus);
        }
        return arrayList;
    }

    public List<PyIslandStatus> mine() {
        ScriptDescriptor scriptDescriptor = ScriptDescriptor.getCurrentScript();
        ArrayList<PyIslandStatus> arrayList = new ArrayList<PyIslandStatus>();
        for (PyIslandStatus pyIslandStatus : this.all()) {
            if (!pyIslandStatus.ownedBy(scriptDescriptor)) continue;
            arrayList.add(pyIslandStatus);
        }
        return arrayList;
    }

    public boolean remove(PyIslandStatus pyIslandStatus) {
        return pyIslandStatus != null && pyIslandStatus.remove();
    }

    public boolean remove(String string) {
        PyIslandStatus pyIslandStatus = this.find(string);
        return pyIslandStatus != null && pyIslandStatus.remove();
    }

    private static DynamicIslandManager island() {
        return RockstarClient.create().getDynamicIslandManager();
    }

    /**
     * ORIGINAL: iterates the island's own entry list and does {@code dispose(); island.i(status)} -
     * the *silent* removal ({@code i(IiIiiIiii)Z}), which does not rewrite the client document.
     * The remap called {@code PyIslandStatus#remove()}, i.e. the saving overload.
     */
    private static void removeOwnedDuplicate(ScriptDescriptor scriptDescriptor, String string) {
        if (scriptDescriptor == null) {
            return;
        }
        DynamicIslandHud hud = DynamicIslandManager.island();
        if (hud == null) {
            return;
        }
        for (DynamicIslandEntry entry : new ArrayList<>(hud.entries())) {
            PyIslandStatus pyIslandStatus;
            if (!(entry instanceof PyIslandStatus) || !(pyIslandStatus = (PyIslandStatus)entry).ownedBy(scriptDescriptor) || !pyIslandStatus.getName().equalsIgnoreCase(string)) continue;
            pyIslandStatus.dispose();
            hud.removeSilently(pyIslandStatus);
        }
    }

    /**
     * ORIGINAL: {@code Ii.I().I().I("client")} then {@code IiIIiIiI.I((IiIiIIiII) island)} -
     * the island itself is a HUD element, so registering a script status re-applies the saved
     * island geometry from the client document.
     */
    private static void applySavedState() {
        DynamicIslandHud hud = DynamicIslandManager.island();
        if (hud == null) {
            return;
        }
        ConfigEntry configEntry = ClientConfigManager.getInstance().get("client");
        if (configEntry instanceof ClientConfig) {
            ClientConfig clientConfig = (ClientConfig)configEntry;
            clientConfig.applySavedState(hud);
        }
    }

    private static String normalizeName(String string) {
        if (string == null || string.isBlank()) {
            return "Script Status";
        }
        return string.trim();
    }
}
