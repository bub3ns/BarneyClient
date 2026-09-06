/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 */
package pyrock.ui;

import jep.python.PyCallable;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.color.ColorPickerHost;
import moscow.rockstar.ui.input.PointerAction;
import pyrock.ui.Node;
import pyrock.ui.Ui;
import ua.mintantileak.spk.Compile;

public class PyScreen
extends ColorPickerHost {
    private final PyCallable builder;
    private PyCallable immediate;
    private PyCallable clickCb;
    private PyCallable releaseCb;
    private PyCallable keyCb;
    private PyCallable moveCb;
    private PyCallable onClose;

    public PyScreen(PyCallable pyCallable) {
        this.builder = pyCallable;
    }

    public PyScreen() {
        this.builder = null;
    }

    public PyScreen immediate(PyCallable pyCallable) {
        this.immediate = pyCallable;
        return this;
    }

    public PyScreen onClick(PyCallable pyCallable) {
        this.clickCb = pyCallable;
        return this;
    }

    public PyScreen onRelease(PyCallable pyCallable) {
        this.releaseCb = pyCallable;
        return this;
    }

    public PyScreen onKey(PyCallable pyCallable) {
        this.keyCb = pyCallable;
        return this;
    }

    public PyScreen onMouseMove(PyCallable pyCallable) {
        this.moveCb = pyCallable;
        return this;
    }

    public PyScreen onClose(PyCallable pyCallable) {
        this.onClose = pyCallable;
        return this;
    }

    public void open() {
        ClientAccess.minecraftClient.setScreen(this);
    }

    public void closeScreen() {
        if (ClientAccess.minecraftClient.currentScreen == this) {
            ClientAccess.minecraftClient.setScreen(null);
        }
    }

    public void addRoot(Node node) {
        if (node != null) {
            this.add(node.element());
        }
    }

    @Override
    @Compile(obfuscation=4)
    public void initializeScreen() {
        super.initializeScreen();
        this.clearRoots();
        if (this.builder == null) {
            return;
        }
        try {
            this.builder.call(new Object[]{new Ui(this)});
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("[PyScreen] build error", (Throwable)exception);
        }
    }

    @Override
    public void afterRender(RockstarDrawContext drawContext) {
        if (this.immediate == null) {
            return;
        }
        try {
            this.immediate.call(new Object[]{drawContext, Float.valueOf(this.width), Float.valueOf(this.height)});
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("[PyScreen] immediate render error", (Throwable)exception);
        }
    }

    @Override
    public void onMouseClicked(double d, double d2, PointerAction pointerAction) {
        super.onMouseClicked(d, d2, pointerAction);
        if (this.clickCb != null) {
            try {
                this.clickCb.call(new Object[]{Float.valueOf((float)d), Float.valueOf((float)d2), pointerAction.name().toLowerCase()});
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.error("[PyScreen] click error", (Throwable)exception);
            }
        }
    }

    @Override
    public void onMouseReleased(double d, double d2, PointerAction pointerAction) {
        super.onMouseReleased(d, d2, pointerAction);
        if (this.releaseCb != null) {
            try {
                this.releaseCb.call(new Object[]{Float.valueOf((float)d), Float.valueOf((float)d2), pointerAction.name().toLowerCase()});
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.error("[PyScreen] release error", (Throwable)exception);
            }
        }
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (this.keyCb == null) {
            return super.keyPressed(n, n2, n3);
        }
        try {
            this.keyCb.call(new Object[]{n, n2, n3, true});
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("[PyScreen] key press error", (Throwable)exception);
        }
        return true;
    }

    @Override
    public boolean keyReleased(int n, int n2, int n3) {
        if (this.keyCb == null) {
            return super.keyReleased(n, n2, n3);
        }
        try {
            this.keyCb.call(new Object[]{n, n2, n3, false});
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("[PyScreen] key release error", (Throwable)exception);
        }
        return true;
    }

    public void mouseMoved(double d, double d2) {
        super.mouseMoved(d, d2);
        if (this.moveCb == null) {
            return;
        }
        try {
            this.moveCb.call(new Object[]{Float.valueOf((float)d), Float.valueOf((float)d2)});
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("[PyScreen] mouse move error", (Throwable)exception);
        }
    }

    public void close() {
        if (this.onClose != null) {
            try {
                this.onClose.call(new Object[0]);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        super.close();
    }
}

