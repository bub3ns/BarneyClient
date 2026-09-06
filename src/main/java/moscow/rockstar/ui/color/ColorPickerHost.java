/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.DrawContext
 *  net.minecraft.Screen
 *  net.minecraft.Vector2f
 */
package moscow.rockstar.ui.color;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.screens.MinecraftScreenBase;
import moscow.rockstar.ui.settings.SettingSnapshotCache;
import moscow.rockstar.ui.text.TextInputField;
import moscow.rockstar.ui.text.ValueFormatter;
import moscow.rockstar.ui.widgets.settings.KeyBindingControl;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.Vector2f;

public abstract class ColorPickerHost
extends MinecraftScreenBase {
    protected final List<UiNode> roots = new ArrayList<UiNode>();
    protected final List<UiNode> overlays = new ArrayList<UiNode>();
    private long lastTime = 0L;
    private static long renderClock = 0L;
    protected float contentAlpha = 1.0f;

    public static long renderClock() {
        return renderClock;
    }

    protected <T extends UiNode> T add(T t) {
        this.roots.add(t);
        return t;
    }

    protected void clearRoots() {
        this.roots.clear();
    }

    protected void clearOverlays() {
        this.overlays.forEach(UiNode::discard);
        this.overlays.clear();
    }

    protected void initializeScreen() {
        super.init();
        this.clearOverlays();
    }

    /**
     * Minecraft calls this lifecycle method when a screen is installed. The
     * original bytecode's method_25426 was retained under the descriptive
     * helper name above during remapping, so bridge the real entry point to it
     * instead of leaving subclass state uninitialized.
     */
    @Override
    protected void init() {
        this.initializeScreen();
    }

    public <T extends UiNode> T openWindow(T t) {
        this.overlays.add(t);
        return t;
    }

    @Override
    public void render(RockstarDrawContext drawContext) {
        long l;
        renderClock = l = System.currentTimeMillis();
        float f = this.lastTime == 0L ? 16.0f : Math.min(64.0f, (float)(l - this.lastTime));
        this.lastTime = l;
        float f2 = drawContext.mouseX();
        float f3 = drawContext.mouseY();
        WidgetBatchRenderer widgetBatchRenderer = this.lowDrawBatching() ? WidgetBatchRenderer.beginGeometryBatch() : WidgetBatchRenderer.beginBatch();
        try {
            for (UiNode uiNode2 : this.roots) {
                uiNode2.prepareLayout(this.width, this.height);
                uiNode2.tick(f, f2, f3);
            }
            for (UiNode uiNode2 : this.roots) {
                uiNode2.draw(drawContext, this.contentAlpha);
            }
            this.overlays.removeIf(uiNode -> !uiNode.alive());
            if (!this.overlays.isEmpty() && !WidgetBatchRenderer.isGeometryBatchActive()) {
                WidgetBatchRenderer.flushCurrentBatch();
            }
            for (UiNode uiNode2 : this.overlays) {
                uiNode2.prepareRoot();
                uiNode2.tick(f, f2, f3);
            }
            for (UiNode uiNode2 : this.overlays) {
                uiNode2.draw(drawContext, this.contentAlpha);
            }
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            this.afterRender(drawContext);
        }
        finally {
            if (widgetBatchRenderer != null) {
                widgetBatchRenderer.restoreEnabledFlag();
            }
        }
    }

    protected boolean lowDrawBatching() {
        return false;
    }

    protected void afterRender(RockstarDrawContext drawContext) {
    }

    public void removed() {
        ValueFormatter.commitActiveEditor(null);
        Vector2f class_56112 = UiUtils.mousePosition();
        for (UiNode uiNode : this.overlays) {
            uiNode.mouseReleased(class_56112.getX(), class_56112.getY(), PointerAction.LEFT_CLICK);
        }
        for (UiNode uiNode : this.roots) {
            uiNode.mouseReleased(class_56112.getX(), class_56112.getY(), PointerAction.LEFT_CLICK);
        }
        KeyBindingControl.cancelActiveControl();
        this.overlays.forEach(UiNode::close);
        super.removed();
    }

    @Override
    public void onMouseClicked(double d, double d2, PointerAction pointerAction) {
        int n;
        TextInputField activeField = TextInputField.getActiveField();
        boolean bl = false;
        for (n = this.overlays.size() - 1; n >= 0; --n) {
            if (!this.overlays.get(n).mouseClicked((float)d, (float)d2, pointerAction)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            for (n = this.roots.size() - 1; n >= 0 && !this.roots.get(n).mouseClicked((float)d, (float)d2, pointerAction); --n) {
            }
        }
        if (activeField != null && TextInputField.getActiveField() == activeField && activeField.isFocused() && !activeField.contains(d, d2)) {
            activeField.setFocused(false);
        }
    }

    @Override
    public void onMouseReleased(double d, double d2, PointerAction pointerAction) {
        for (UiNode uiNode : this.overlays) {
            uiNode.mouseReleased((float)d, (float)d2, pointerAction);
        }
        for (UiNode uiNode : this.roots) {
            uiNode.mouseReleased((float)d, (float)d2, pointerAction);
        }
    }

    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        int n;
        for (n = this.overlays.size() - 1; n >= 0; --n) {
            if (!this.overlays.get(n).mouseScrolled((float)d, (float)d2, (float)d3, (float)d4)) continue;
            return true;
        }
        for (n = this.roots.size() - 1; n >= 0; --n) {
            if (!this.roots.get(n).mouseScrolled((float)d, (float)d2, (float)d3, (float)d4)) continue;
            return true;
        }
        return super.mouseScrolled(d, d2, d3, d4);
    }

    public boolean keyPressed(int n, int n2, int n3) {
        int n4;
        if (!KeyBindingControl.isControlActive()) {
            if (Screen.hasControlDown() && n == 90 && SettingSnapshotCache.isCollectionCacheReady()) {
                return true;
            }
            if (Screen.hasControlDown() && n == 89 && SettingSnapshotCache.isCollectionProcessorCacheTargetReady()) {
                return true;
            }
        }
        for (n4 = this.overlays.size() - 1; n4 >= 0; --n4) {
            if (!this.overlays.get(n4).keyPressed(n, n2, n3)) continue;
            return true;
        }
        for (n4 = this.roots.size() - 1; n4 >= 0; --n4) {
            if (!this.roots.get(n4).keyPressed(n, n2, n3)) continue;
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    public boolean keyReleased(int n, int n2, int n3) {
        int n4;
        for (n4 = this.overlays.size() - 1; n4 >= 0; --n4) {
            if (!this.overlays.get(n4).keyReleased(n, n2, n3)) continue;
            return true;
        }
        for (n4 = this.roots.size() - 1; n4 >= 0; --n4) {
            if (!this.roots.get(n4).keyReleased(n, n2, n3)) continue;
            return true;
        }
        return super.keyReleased(n, n2, n3);
    }

    public boolean charTyped(char c, int n) {
        int n2;
        for (n2 = this.overlays.size() - 1; n2 >= 0; --n2) {
            if (!this.overlays.get(n2).charTyped(c, n)) continue;
            return true;
        }
        for (n2 = this.roots.size() - 1; n2 >= 0; --n2) {
            if (!this.roots.get(n2).charTyped(c, n)) continue;
            return true;
        }
        return super.charTyped(c, n);
    }

    public boolean shouldPause() {
        return false;
    }

    public void renderBackground(DrawContext ServerConfigException, int n, int n2, float f) {
    }
}
