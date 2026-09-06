/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 *  net.minecraft.Text
 */
package pyrock.classes;

import jep.python.PyCallable;
import moscow.rockstar.api.data.ClientConfigManager;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.hud.DynamicIslandEntry;
import moscow.rockstar.ui.hud.DynamicIslandManager;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.modules.visuals.hud.DynamicIslandHud;
import net.minecraft.text.Text;
import pyrock.utility.render.ColorRGBA;

public class PyIslandStatus
extends DynamicIslandEntry {
    private final ScriptDescriptor owner;
    private PyCallable visibleWhen;
    private PyCallable measureCallback;
    private PyCallable renderCallback;
    private PyCallable clickCallback;
    private PyCallable colorCallback;
    private ColorRGBA color = ColorPalette.getPanelColor();
    private boolean expandable;
    private boolean disposed;
    private boolean errored;
    private DynamicIslandHud island;
    private float x;
    private float y;
    private float width;
    private float height;
    private float alpha;

    public PyIslandStatus(ScriptDescriptor scriptDescriptor, MultiBooleanSetting multiBooleanSetting, String string, float f, float f2, float f3) {
        super(multiBooleanSetting, string, false);
        this.owner = scriptDescriptor;
        // ORIGINAL: this.size.I(max(1,w), max(1,h), max(0,r)) - the inherited DynamicIslandSize
        // triple is what the island actually lays out with; private shadow fields were ignored.
        this.size.set(Math.max(1.0f, f), Math.max(1.0f, f2), Math.max(0.0f, f3));
    }

    public PyIslandStatus visibleWhen(PyCallable pyCallable) {
        this.visibleWhen = pyCallable;
        this.errored = false;
        return this;
    }

    public PyIslandStatus showWhen(PyCallable pyCallable) {
        return this.visibleWhen(pyCallable);
    }

    public PyIslandStatus measure(PyCallable pyCallable) {
        this.measureCallback = pyCallable;
        this.errored = false;
        return this;
    }

    public PyIslandStatus renderer(PyCallable pyCallable) {
        this.renderCallback = pyCallable;
        this.errored = false;
        return this;
    }

    public PyIslandStatus render(PyCallable pyCallable) {
        return this.renderer(pyCallable);
    }

    public PyIslandStatus onClick(PyCallable pyCallable) {
        this.clickCallback = pyCallable;
        this.errored = false;
        return this;
    }

    public PyIslandStatus clickCallback(PyCallable pyCallable) {
        return this.onClick(pyCallable);
    }

    public PyIslandStatus colorFn(PyCallable pyCallable) {
        this.colorCallback = pyCallable;
        this.errored = false;
        return this;
    }

    public PyIslandStatus color(ColorRGBA colorRGBA) {
        this.color = colorRGBA == null ? ColorPalette.getPanelColor() : colorRGBA;
        this.colorCallback = null;
        return this;
    }

    public PyIslandStatus color(double d, double d2, double d3) {
        return this.color(d, d2, d3, 255.0);
    }

    public PyIslandStatus color(double d, double d2, double d3, double d4) {
        return this.color(new ColorRGBA((float)d, (float)d2, (float)d3, (float)d4));
    }

    public PyIslandStatus size(double d, double d2) {
        return this.size(d, d2, this.size.radius);
    }

    public PyIslandStatus size(double d, double d2, double d3) {
        this.size.set(Math.max(1.0f, (float)d), Math.max(1.0f, (float)d2), Math.max(0.0f, (float)d3));
        return this;
    }

    public PyIslandStatus width(double d) {
        this.size.width = Math.max(1.0f, (float)d);
        return this;
    }

    public PyIslandStatus height(double d) {
        this.size.height = Math.max(1.0f, (float)d);
        return this;
    }

    public PyIslandStatus radius(double d) {
        this.size.radius = Math.max(0.0f, (float)d);
        return this;
    }

    public PyIslandStatus expandable(boolean bl) {
        this.expandable = bl;
        return this;
    }

    public PyIslandStatus selected(boolean bl) {
        if (bl) {
            this.getParent().selectOption((MultiBooleanSetting.Option)((Object)this));
        } else {
            this.getParent().getSelectedOptions().remove(this);
        }
        // ORIGINAL: Ii.I().I().i("client") - the client document, not the module config.
        ClientConfigManager.getInstance().save("client");
        return this;
    }

    public boolean selected() {
        return this.isSelected();
    }

    public boolean isExpandable() {
        return this.expandable;
    }

    public float radius(DynamicIslandHud dynamicIslandHud) {
        return this.size.radius;
    }

    public void prepare(DynamicIslandHud dynamicIslandHud) {
        this.island = dynamicIslandHud;
        if (!this.alive() || this.measureCallback == null) {
            return;
        }
        try (AutoCloseable autoCloseable = ScriptDescriptor.pushCurrentScript(this.owner);){
            this.measureCallback.call(new Object[]{this});
        }
        catch (Throwable throwable) {
            this.handleError("measure", throwable);
        }
    }

    public void render(RockstarDrawContext drawContext, DynamicIslandHud dynamicIslandHud, float f, float f2, float f3, float f4, float f5) {
        this.island = dynamicIslandHud;
        this.x = f;
        this.y = f2;
        this.width = f3;
        this.height = f4;
        this.alpha = f5;
        if (!this.alive() || this.renderCallback == null) {
            return;
        }
        try (AutoCloseable autoCloseable = ScriptDescriptor.pushCurrentScript(this.owner);){
            this.renderCallback.call(new Object[]{drawContext, this});
        }
        catch (Throwable throwable) {
            this.handleError("render", throwable);
        }
    }

    public void click(float f, float f2, int n) {
        if (!this.alive() || this.clickCallback == null) {
            return;
        }
        try (AutoCloseable autoCloseable = ScriptDescriptor.pushCurrentScript(this.owner);){
            this.clickCallback.call(new Object[]{Float.valueOf(f), Float.valueOf(f2), n, this});
        }
        catch (Throwable throwable) {
            this.handleError("click", throwable);
        }
    }

    public boolean canShow() {
        if (!this.alive()) {
            return false;
        }
        if (this.visibleWhen == null) {
            return true;
        }
        try (AutoCloseable scriptScope = ScriptDescriptor.pushCurrentScript(this.owner)) {
            return PyIslandStatus.truthy(this.visibleWhen.call(new Object[]{this}));
        }
        catch (Throwable throwable) {
            this.handleError("visible", throwable);
            return false;
        }
    }

    public ColorRGBA getColor() {
        if (!this.alive() || this.colorCallback == null) {
            return this.color;
        }
        try (AutoCloseable scriptScope = ScriptDescriptor.pushCurrentScript(this.owner)) {
            Object result = this.colorCallback.call(new Object[]{this});
            return result instanceof ColorRGBA ? (ColorRGBA)result : this.color;
        }
        catch (Throwable throwable) {
            this.handleError("color", throwable);
            return this.color;
        }
    }

    public boolean ownedBy(ScriptDescriptor scriptDescriptor) {
        return this.owner == scriptDescriptor;
    }

    public boolean remove() {
        this.dispose();
        DynamicIslandManager manager = RockstarClient.create().getDynamicIslandManager();
        return manager != null && manager.remove(this);
    }

    public void dispose() {
        this.disposed = true;
        this.visibleWhen = null;
        this.measureCallback = null;
        this.renderCallback = null;
        this.clickCallback = null;
        this.colorCallback = null;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public float renderAlpha() {
        return this.alpha;
    }

    public float dragAlpha() {
        return 0.0f;
    }

    /** ORIGINAL: {@code this.island != null && this.island.II()} - the island captured in prepare/render. */
    public boolean extended() {
        return this.island != null && this.island.isExpanded();
    }

    /** ORIGINAL: {@code this.island == null ? 0.0f : this.island.I().I()}. */
    public float extending() {
        return this.island == null ? 0.0f : this.island.getExpandAnim().getValue();
    }

    private boolean alive() {
        return !this.disposed && !this.errored && (this.owner == null || this.owner.isLoaded());
    }

    private void handleError(String string, Throwable throwable) {
        int n;
        if (this.errored) {
            return;
        }
        this.errored = true;
        this.visibleWhen = null;
        this.measureCallback = null;
        this.renderCallback = null;
        this.clickCallback = null;
        this.colorCallback = null;
        this.getParent().getSelectedOptions().remove(this);
        String string2 = throwable.getMessage();
        if (string2 == null || string2.isBlank()) {
            string2 = throwable.getClass().getSimpleName();
        }
        if ((n = string2.indexOf(58)) >= 0 && n + 1 < string2.length()) {
            string2 = string2.substring(n + 1).trim();
        }
        Notification.error(Text.of((String)("[Python Island Error] " + this.getName() + " (" + string + "): " + string2)));
        RockstarClient.LOGGER.error("Python island status error in '{}' during {}", new Object[]{this.getName(), string, throwable});
    }

    private static boolean truthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            Boolean bl = (Boolean)object;
            return bl;
        }
        if (object instanceof Number) {
            Number number = (Number)object;
            return number.doubleValue() != 0.0;
        }
        if (object instanceof CharSequence) {
            CharSequence charSequence = (CharSequence)object;
            return !charSequence.isEmpty();
        }
        return true;
    }
}
