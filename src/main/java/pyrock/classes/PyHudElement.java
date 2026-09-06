/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 *  lombok.Generated
 *  net.minecraft.Text
 */
package pyrock.classes;

import java.util.Objects;
import jep.python.PyCallable;
import lombok.Generated;
import moscow.rockstar.api.data.ClientConfig;
import moscow.rockstar.api.data.ClientConfigManager;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.text.Text;
import pyrock.ui.Node;
import pyrock.ui.Ui;

public class PyHudElement
extends HudElement {
    private final ScriptDescriptor owner;
    private PyCallable renderer;
    private PyCallable layoutBuilder;
    private PyCallable signatureFn;
    private Object lastSig;
    private PyCallable visibleWhen;
    private boolean disposed;
    private boolean errored;

    public PyHudElement(ScriptDescriptor scriptDescriptor, String string, String string2, float f, float f2, float f3, float f4) {
        super(string, string2);
        this.owner = scriptDescriptor;
        this.width = Math.max(1.0f, f);
        this.height = Math.max(1.0f, f2);
        this.pos(f3, f4);
    }

    public PyHudElement renderer(PyCallable pyCallable) {
        this.renderer = pyCallable;
        this.errored = false;
        this.rebuild();
        return this;
    }

    public PyHudElement render(PyCallable pyCallable) {
        return this.renderer(pyCallable);
    }

    public PyHudElement layout(PyCallable pyCallable) {
        this.layoutBuilder = pyCallable;
        this.renderer = null;
        this.errored = false;
        this.rebuild();
        return this;
    }

    public PyHudElement signature(PyCallable pyCallable) {
        this.signatureFn = pyCallable;
        this.lastSig = null;
        return this;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Component build() {
        if (this.layoutBuilder == null) {
            return null;
        }
        try (AutoCloseable autoCloseable = ScriptDescriptor.pushCurrentScript(this.owner);){
            Ui ui = new Ui();
            this.layoutBuilder.call(new Object[]{ui});
            Node node = ui.firstRoot();
            if (node == null) return null;
            UiNode uiNode = node.element();
            if (!(uiNode instanceof Component)) return null;
            Component component = (Component)uiNode;
            return component;
        }
        catch (Throwable throwable) {
            this.handleError("layout", throwable);
        }
        return null;
    }

    public PyHudElement visibleWhen(PyCallable pyCallable) {
        this.visibleWhen = pyCallable;
        this.errored = false;
        return this;
    }

    public PyHudElement size(double d, double d2) {
        this.width = Math.max(1.0f, (float)d);
        this.height = Math.max(1.0f, (float)d2);
        return this;
    }

    public PyHudElement width(double d) {
        this.width = Math.max(1.0f, (float)d);
        return this;
    }

    public PyHudElement height(double d) {
        this.height = Math.max(1.0f, (float)d);
        return this;
    }

    public PyHudElement position(double d, double d2) {
        this.pos((float)d, (float)d2);
        return this;
    }

    public PyHudElement shown(boolean bl) {
        this.setShowing(bl);
        return this;
    }

    public float renderAlpha() {
        return this.animation.getValue() * this.visible.getValue();
    }

    public float dragAlpha() {
        return this.dragAnim.getValue();
    }

    public boolean ownedBy(ScriptDescriptor scriptDescriptor) {
        return this.owner == scriptDescriptor;
    }

    /**
     * ORIGINAL: {@code dispose(); IiIIiIiI.I(getName()); Ii.I().I().III().remove(this);} and, when
     * the element really was removed, {@code Ii.I().I().i("client")}. The remap dropped the cached
     * HUD-state invalidation and saved the *module* config instead of the client document.
     */
    public boolean remove() {
        this.dispose();
        ClientConfig.invalidateHudElement(this.getName());
        boolean bl = moscow.rockstar.core.RockstarClient.create().getHudElementRegistry().remove(this);
        if (bl) {
            ClientConfigManager.getInstance().save("client");
        }
        return bl;
    }

    public void dispose() {
        this.disposed = true;
        this.renderer = null;
        this.layoutBuilder = null;
        this.signatureFn = null;
        this.visibleWhen = null;
        this.setShowing(false);
    }

    public boolean show() {
        if (!this.alive()) {
            return false;
        }
        if (this.visibleWhen == null) {
            return true;
        }
        try (AutoCloseable ignored = ScriptDescriptor.pushCurrentScript(this.owner)) {
            return PyHudElement.truthy(this.visibleWhen.call(new Object[0]));
        } catch (Throwable throwable) {
            this.handleError("visible", throwable);
            return false;
        }
    }

    public void renderComponent(RockstarDrawContext drawContext) {
        if (!this.alive()) {
            return;
        }
        if (this.layoutBuilder != null) {
            if (this.signatureFn != null) {
                try (AutoCloseable autoCloseable = ScriptDescriptor.pushCurrentScript(this.owner);){
                    Object object = this.signatureFn.call(new Object[0]);
                    if (!Objects.equals(object, this.lastSig)) {
                        this.lastSig = object;
                        this.rebuild();
                    }
                }
                catch (Throwable throwable) {
                    this.handleError("signature", throwable);
                }
            }
            super.renderComponent(drawContext);
            return;
        }
        if (this.renderer == null) {
            this.renderFallback(drawContext);
            return;
        }
        try (AutoCloseable autoCloseable = ScriptDescriptor.pushCurrentScript(this.owner);){
            this.renderer.call(new Object[]{drawContext, this});
        }
        catch (Throwable throwable) {
            this.handleError("render", throwable);
        }
    }

    private boolean alive() {
        return !this.disposed && !this.errored && (this.owner == null || this.owner.isLoaded());
    }

    private void renderFallback(RockstarDrawContext drawContext) {
        drawContext.drawClientRect(this.x, this.y, this.width, this.height, this.renderAlpha(), this.dragAlpha(), 3.0f);
        FontMetrics fontMetrics = Font.MEDIUM.metrics(7.0f);
        drawContext.drawText(fontMetrics, this.getName(), this.x + 6.0f, this.y + this.height / 2.0f - fontMetrics.getFontMetricsFloat() / 2.0f, ColorPalette.getPrimaryTextColor());
    }

    private void handleError(String string, Throwable throwable) {
        int n;
        if (this.errored) {
            return;
        }
        this.errored = true;
        this.renderer = null;
        this.visibleWhen = null;
        this.setShowing(false);
        String string2 = throwable.getMessage();
        if (string2 == null || string2.isBlank()) {
            string2 = throwable.getClass().getSimpleName();
        }
        if ((n = string2.indexOf(58)) >= 0 && n + 1 < string2.length()) {
            string2 = string2.substring(n + 1).trim();
        }
        Notification.error(Text.of((String)("[Python HUD Error] " + this.getName() + " (" + string + "): " + string2)));
        RockstarClient.LOGGER.error("Python HUD error in '{}' during {}", new Object[]{this.getName(), string, throwable});
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

    @Generated
    public ScriptDescriptor getOwner() {
        return this.owner;
    }

    @Generated
    public PyCallable getRenderer() {
        return this.renderer;
    }

    @Generated
    public PyCallable getLayoutBuilder() {
        return this.layoutBuilder;
    }

    @Generated
    public PyCallable getSignatureFn() {
        return this.signatureFn;
    }

    @Generated
    public Object getLastSig() {
        return this.lastSig;
    }

    @Generated
    public PyCallable getVisibleWhen() {
        return this.visibleWhen;
    }

    @Generated
    public boolean isDisposed() {
        return this.disposed;
    }

    @Generated
    public boolean isErrored() {
        return this.errored;
    }
}
