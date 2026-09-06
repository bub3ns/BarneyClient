/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 *  net.minecraft.DrawContext
 *  net.minecraft.Screen
 */
package moscow.rockstar.ui.screens;

import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.diagnostics.DrawCallCounter;
import moscow.rockstar.ui.input.PointerAction;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public abstract class MinecraftScreenBase
extends Screen {
    protected MinecraftScreenBase() {
        super((Text)Text.empty());
    }

    public abstract void render(RockstarDrawContext context);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void render(DrawContext originalContext, int n, int n2, float f) {
        super.render(originalContext, n, n2, f);
        RockstarDrawContext drawContext = RockstarDrawContext.create(originalContext, n, n2, f);
        DrawCallCounter.beginCounting();
        try {
            this.render(drawContext);
        }
        finally {
            DrawCallCounter.endCounting();
        }
    }

    public final boolean mouseClicked(double d, double d2, int n) {
        PointerAction pointerAction = PointerAction.fromButtonCode(n);
        this.onMouseClicked(d, d2, pointerAction);
        return super.mouseClicked(d, d2, n);
    }

    public final boolean mouseReleased(double d, double d2, int n) {
        PointerAction pointerAction = PointerAction.fromButtonCode(n);
        this.onMouseReleased(d, d2, pointerAction);
        return super.mouseReleased(d, d2, n);
    }

    public final boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        PointerAction pointerAction = PointerAction.fromButtonCode(n);
        this.onMouseDragged(d, d2, pointerAction, d3, d4);
        return super.mouseDragged(d, d2, n, d3, d4);
    }

    public void onMouseClicked(double d, double d2, PointerAction pointerAction) {
    }

    public void onMouseReleased(double d, double d2, PointerAction pointerAction) {
    }

    public void onMouseDragged(double d, double d2, PointerAction pointerAction, double d3, double d4) {
    }
}
