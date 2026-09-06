/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.text.FontMetrics;
import pyrock.utility.render.ColorRGBA;

public class MultilineTextComponent
extends UiNode {
    private final FontMetrics fontMetrics;
    private final Supplier<String> textSupplier;
    private Function<MultilineTextComponent, ColorRGBA> colorProvider = multilineTextComponent -> ColorRGBA.WHITE;
    private boolean centered;
    private float lineSpacing = 3.0f;
    private final List<String> lines = new ArrayList<String>();
    private String lastText;
    private float lastMeasuredWidth = -1.0f;

    public MultilineTextComponent(FontMetrics fontMetrics, String string) {
        this(fontMetrics, () -> string);
    }

    public MultilineTextComponent(FontMetrics fontMetrics, Supplier<String> supplier) {
        this.fontMetrics = fontMetrics;
        this.textSupplier = supplier;
    }

    public MultilineTextComponent setColor(ColorRGBA colorRGBA) {
        this.colorProvider = multilineTextComponent -> colorRGBA;
        return this;
    }

    public MultilineTextComponent setColorProvider(Function<MultilineTextComponent, ColorRGBA> function) {
        this.colorProvider = function;
        return this;
    }

    public MultilineTextComponent setCentered(boolean bl) {
        this.centered = bl;
        return this;
    }

    public MultilineTextComponent getMultilineTextComponentMultilineTextComponent() {
        this.centered = true;
        return this;
    }

    public MultilineTextComponent setLineSpacing(float f) {
        this.lineSpacing = f;
        return this;
    }

    public MultilineTextComponent setWidth(float f) {
        super.width(f);
        return this;
    }

    public MultilineTextComponent setHeight(float f) {
        super.height(f);
        return this;
    }

    public MultilineTextComponent fillWidthNode() {
        super.fillWidth();
        return this;
    }

    public MultilineTextComponent fillHeightNode() {
        super.fillHeight();
        return this;
    }

    @Override
    protected void measure() {
        this.rebuildLines();
        if (!this.explicitH) {
            int n = Math.max(1, this.lines.size());
            this.prefH = (float)n * this.fontMetrics.getFontTopOffset() + (float)(n - 1) * this.lineSpacing;
        }
    }

    private void rebuildLines() {
        String string = this.textSupplier.get();
        float f = this.w();
        boolean textUnchanged = string == null ? this.lastText == null : string.equals(this.lastText);
        if (textUnchanged && Math.abs(f - this.lastMeasuredWidth) < 0.5f) {
            return;
        }
        this.lastText = string;
        this.lastMeasuredWidth = f;
        this.lines.clear();
        if (string == null || string.isEmpty()) {
            return;
        }
        if (f < 8.0f) {
            this.lines.add(string);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String string2 : string.split(" ")) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append(string2);
                continue;
            }
            if (this.fontMetrics.measureText(String.valueOf(stringBuilder) + " " + string2) > f) {
                this.lines.add(stringBuilder.toString());
                stringBuilder = new StringBuilder(string2);
                continue;
            }
            stringBuilder.append(' ').append(string2);
        }
        if (stringBuilder.length() > 0) {
            this.lines.add(stringBuilder.toString());
        }
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        if (this.lines.isEmpty()) {
            return;
        }
        ColorRGBA colorRGBA = this.colorProvider.apply(this);
        if (colorRGBA == null || colorRGBA.getAlpha() <= 0.0f) {
            return;
        }
        float f2 = this.x();
        float f3 = this.y();
        float f4 = this.w();
        float f5 = f3;
        for (String string : this.lines) {
            if (this.centered) {
                drawContext.drawCenteredText(this.fontMetrics, string, f2 + f4 / 2.0f, f5, colorRGBA);
            } else {
                drawContext.drawText(this.fontMetrics, string, f2, f5, colorRGBA);
            }
            f5 += this.fontMetrics.getFontTopOffset() + this.lineSpacing;
        }
    }




}

