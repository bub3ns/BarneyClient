/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 */
package pyrock.classes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jep.python.PyCallable;
import moscow.rockstar.api.data.ClientConfig;
import moscow.rockstar.api.data.ClientConfigManager;
import moscow.rockstar.api.data.ConfigEntry;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.localization.Localization;
import pyrock.classes.PyHudElement;

public class PyHud {
    public PyHudElement add(String string, String string2, double d, double d2, double d3, double d4, boolean bl, PyCallable pyCallable, PyCallable pyCallable2) {
        String string3 = PyHud.normalizeName(string);
        ScriptDescriptor scriptDescriptor = ScriptDescriptor.getCurrentScript();
        PyHud.removeOwnedDuplicate(scriptDescriptor, string3);
        PyHudElement pyHudElement = new PyHudElement(scriptDescriptor, string3, PyHud.normalizeIcon(string2), Math.max(1.0f, (float)d), Math.max(1.0f, (float)d2), (float)d3, (float)d4);
        pyHudElement.setShowing(bl);
        if (pyCallable != null) {
            pyHudElement.renderer(pyCallable);
        }
        if (pyCallable2 != null) {
            pyHudElement.visibleWhen(pyCallable2);
        }
        RockstarClient.create().getHudElementRegistry().add(pyHudElement);
        ScriptDescriptor.registerHudElement(pyHudElement);
        PyHud.applySavedState(pyHudElement);
        return pyHudElement;
    }

    public PyHudElement find(String string) {
        String string2 = PyHud.normalizeName(string);
        for (HudElement ambientParticleRenderer : RockstarClient.create().getHudElementRegistry().elements()) {
            PyHudElement pyHudElement;
            if (!(ambientParticleRenderer instanceof PyHudElement) || !(pyHudElement = (PyHudElement)ambientParticleRenderer).getName().equalsIgnoreCase(string2)) continue;
            return pyHudElement;
        }
        return null;
    }

    public List<PyHudElement> all() {
        ArrayList<PyHudElement> arrayList = new ArrayList<PyHudElement>();
        for (HudElement ambientParticleRenderer : RockstarClient.create().getHudElementRegistry().elements()) {
            if (!(ambientParticleRenderer instanceof PyHudElement)) continue;
            PyHudElement pyHudElement = (PyHudElement)ambientParticleRenderer;
            arrayList.add(pyHudElement);
        }
        return arrayList;
    }

    public List<PyHudElement> mine() {
        ScriptDescriptor scriptDescriptor = ScriptDescriptor.getCurrentScript();
        ArrayList<PyHudElement> arrayList = new ArrayList<PyHudElement>();
        for (PyHudElement pyHudElement : this.all()) {
            if (!pyHudElement.ownedBy(scriptDescriptor)) continue;
            arrayList.add(pyHudElement);
        }
        return arrayList;
    }

    public List<Map<String, Object>> elements() {
        List<HudElement> list = RockstarClient.create().getHudElementRegistry().elements();
        ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>(list.size());
        for (HudElement ambientParticleRenderer : list) {
            arrayList.add(PyHud.describe(ambientParticleRenderer));
        }
        return arrayList;
    }

    public Map<String, Object> element(String string) {
        if (string == null || string.isBlank()) {
            return null;
        }
        for (HudElement ambientParticleRenderer : RockstarClient.create().getHudElementRegistry().elements()) {
            if (!PyHud.matches(ambientParticleRenderer, string.trim())) continue;
            return PyHud.describe(ambientParticleRenderer);
        }
        return null;
    }

    private static boolean matches(HudElement ambientParticleRenderer, String string) {
        String string2 = ambientParticleRenderer.getName();
        if (string2 == null) {
            return false;
        }
        if (string2.equalsIgnoreCase(string) || Localization.translate(string2).equalsIgnoreCase(string)) {
            return true;
        }
        int n = string2.lastIndexOf(46);
        return n >= 0 && n < string2.length() - 1 && string2.substring(n + 1).equalsIgnoreCase(string);
    }

    private static Map<String, Object> describe(HudElement ambientParticleRenderer) {
        float f = ambientParticleRenderer.getAnimation().getValue();
        float f2 = ambientParticleRenderer.getVisible().getValue();
        float f3 = ambientParticleRenderer.getSelecting().getValue();
        float f4 = f * f2;
        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
        linkedHashMap.put("name", ambientParticleRenderer.getName());
        linkedHashMap.put("title", Localization.translate(ambientParticleRenderer.getName()));
        linkedHashMap.put("icon", ambientParticleRenderer.getIcon());
        linkedHashMap.put("x", Float.valueOf(ambientParticleRenderer.getX()));
        linkedHashMap.put("y", Float.valueOf(ambientParticleRenderer.getY()));
        linkedHashMap.put("width", Float.valueOf(ambientParticleRenderer.getWidth()));
        linkedHashMap.put("height", Float.valueOf(ambientParticleRenderer.getHeight()));
        linkedHashMap.put("right", Float.valueOf(ambientParticleRenderer.getX() + ambientParticleRenderer.getWidth()));
        linkedHashMap.put("bottom", Float.valueOf(ambientParticleRenderer.getY() + ambientParticleRenderer.getHeight()));
        linkedHashMap.put("center_x", Float.valueOf(ambientParticleRenderer.getX() + ambientParticleRenderer.getWidth() / 2.0f));
        linkedHashMap.put("center_y", Float.valueOf(ambientParticleRenderer.getY() + ambientParticleRenderer.getHeight() / 2.0f));
        linkedHashMap.put("alpha", Float.valueOf(f4));
        linkedHashMap.put("appear", Float.valueOf(f));
        linkedHashMap.put("visible", Float.valueOf(f2));
        linkedHashMap.put("select", Float.valueOf(f3));
        linkedHashMap.put("drag", Float.valueOf(ambientParticleRenderer.getDragAnim().getValue()));
        linkedHashMap.put("scale", Float.valueOf(0.5f + f4 * 0.5f - 0.05f * f3));
        linkedHashMap.put("showing", ambientParticleRenderer.isShowing());
        linkedHashMap.put("dragging", ambientParticleRenderer.isDragging());
        linkedHashMap.put("script", ambientParticleRenderer instanceof PyHudElement);
        return linkedHashMap;
    }

    public boolean remove(PyHudElement pyHudElement) {
        return pyHudElement != null && pyHudElement.remove();
    }

    public boolean remove(String string) {
        PyHudElement pyHudElement = this.find(string);
        return pyHudElement != null && pyHudElement.remove();
    }

    private static void removeOwnedDuplicate(ScriptDescriptor scriptDescriptor, String string) {
        if (scriptDescriptor == null) {
            return;
        }
        RockstarClient.create().getHudElementRegistry().removeIf(ambientParticleRenderer -> {
            PyHudElement pyHudElement;
            if (ambientParticleRenderer instanceof PyHudElement && (pyHudElement = (PyHudElement)ambientParticleRenderer).ownedBy(scriptDescriptor) && pyHudElement.getName().equalsIgnoreCase(string)) {
                pyHudElement.dispose();
                return true;
            }
            return false;
        });
    }

    /**
     * ORIGINAL: {@code Ii.I().I().I("client")} -> the client config document, then
     * {@code IiIIiIiI.I(Lrockstar/ilIlil/IiIiIIiII;)Z} = {@link ClientConfig#applySavedState}.
     * Restores the saved position / size / visibility of a script HUD element as soon as it is
     * registered, so a reloaded script does not snap its element back to the default corner.
     */
    private static void applySavedState(PyHudElement pyHudElement) {
        ConfigEntry configEntry = ClientConfigManager.getInstance().get("client");
        if (configEntry instanceof ClientConfig) {
            ClientConfig clientConfig = (ClientConfig)configEntry;
            clientConfig.applySavedState(pyHudElement);
        }
    }

    private static String normalizeName(String string) {
        if (string == null || string.isBlank()) {
            return "Script HUD";
        }
        return string.trim();
    }

    private static String normalizeIcon(String string) {
        if (string == null || string.isBlank()) {
            return "hud/player";
        }
        return string.trim();
    }
}
