/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 *  lombok.Generated
 */
package pyrock.classes;

import java.util.ArrayList;
import java.util.Locale;
import jep.python.PyCallable;
import lombok.Generated;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.esp.targeting.PlayerTargetGroup;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.network.session.BotPacketListener;
import moscow.rockstar.render.esp.EntityTargetRenderer;
import moscow.rockstar.render.esp.OverlayRegistry;

public class PyEspElement
implements ClientAccess {
    private final EntityTargetRenderer element;
    private final ScriptDescriptor creator = ScriptDescriptor.getCurrentScript();

    public PyEspElement(String string, String string2) {
        this.element = new EntityTargetRenderer(this.owner(), string, this.types(string2));
        OverlayRegistry.getInstance().registerOverlayIfAbsent(this.element);
    }

    public PyEspElement onRender(PyCallable pyCallable) {
        this.element.setEntityRenderer(pyCallable == null ? null : (class_12972, render3DEvent) -> this.call(pyCallable, class_12972, render3DEvent));
        return this;
    }

    public PyEspElement onRenderAll(PyCallable pyCallable) {
        this.element.setEntityBatchRenderer(pyCallable == null ? null : (list, render3DEvent) -> this.call(pyCallable, list, render3DEvent));
        return this;
    }

    public PyEspElement onFilter(PyCallable pyCallable) {
        this.element.setTargetFilter(pyCallable == null ? null : class_12972 -> {
            Boolean bl;
            Object object = this.call(pyCallable, class_12972, null);
            return !(object instanceof Boolean) || (bl = (Boolean)object) != false;
        });
        return this;
    }

    public PyEspElement enableFor(String string) {
        TargetGroup targetGroup = PyEspElement.type(string);
        if (targetGroup != null) {
            this.element.enableTargetGroup(targetGroup);
            return this;
        }
        PlayerTargetGroup playerTargetGroup = PyEspElement.subType(string);
        if (playerTargetGroup != null) {
            this.element.enablePlayerGroup(playerTargetGroup);
            return this;
        }
        throw new IllegalArgumentException("\u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u0446\u0435\u043b\u044c: " + string);
    }

    public boolean isEnabled() {
        return OverlayRegistry.isEspEnabled() && this.element.isValid();
    }

    public String getName() {
        return this.element.getName();
    }

    public void remove() {
        OverlayRegistry.getInstance().unregisterOverlay(this.element);
    }

    private Object call(PyCallable pyCallable, Object object, Object object2) {
        if (!moscow.rockstar.scripts.python.PythonRuntime.isAvailable()) {
            return null;
        }
        ScriptDescriptor scriptDescriptor = this.creator != null ? this.creator : ScriptDescriptor.getCurrentScript();
        if (scriptDescriptor != null && !scriptDescriptor.isLoaded()) {
            return null;
        }
        try (AutoCloseable scriptScope = ScriptDescriptor.pushCurrentScript(scriptDescriptor)) {
            return object2 == null ? pyCallable.call(new Object[]{object}) : pyCallable.call(new Object[]{object, object2});
        }
        catch (Exception exception) {
            this.element.setEntityRenderer(null);
            this.element.setEntityBatchRenderer(null);
            this.element.setTargetFilter(null);
            RockstarClient.LOGGER.error("Python error in esp element '" + this.element.getName() + "':", exception);
            return null;
        }
    }

    private TargetGroup[] types(String string) {
        ArrayList<TargetGroup> arrayList = new ArrayList<TargetGroup>();
        if (string == null || string.isBlank()) {
            return new TargetGroup[0];
        }
        for (String string2 : string.split(",")) {
            if (string2.isBlank()) continue;
            TargetGroup targetGroup = PyEspElement.type(string2);
            if (targetGroup == null) {
                throw new IllegalArgumentException("\u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u0446\u0435\u043b\u044c: " + string2.trim());
            }
            arrayList.add(targetGroup);
        }
        return arrayList.toArray(new TargetGroup[0]);
    }

    private static TargetGroup type(String string) {
        String string2 = string.trim().toLowerCase(Locale.ROOT);
        for (TargetGroup targetGroup : TargetGroup.values()) {
            if (!targetGroup.getTargetKey().equals(string2)) continue;
            return targetGroup;
        }
        return null;
    }

    private static PlayerTargetGroup subType(String string) {
        String string2 = string.trim().toLowerCase(Locale.ROOT);
        for (PlayerTargetGroup playerTargetGroup : PlayerTargetGroup.values()) {
            if (!playerTargetGroup.getTargetKey().equals(string2)) continue;
            return playerTargetGroup;
        }
        return null;
    }

    private Object owner() {
        ScriptDescriptor scriptDescriptor = ScriptDescriptor.getCurrentScript();
        return scriptDescriptor != null ? scriptDescriptor : this;
    }

    @Generated
    public EntityTargetRenderer getElement() {
        return this.element;
    }

    @Generated
    public ScriptDescriptor getCreator() {
        return this.creator;
    }
}
