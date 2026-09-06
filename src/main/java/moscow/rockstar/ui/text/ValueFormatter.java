/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.text;

import java.util.function.Function;
import java.util.function.Supplier;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextInputField;
import pyrock.utility.render.ColorRGBA;

public class ValueFormatter
extends UiNode {
    private final FontMetrics fontMetrics;
    private final ValueSupplier valueSupplier;
    private final ValueConsumer valueConsumer;
    private final float minimumValue;
    private final float maximumValue;
    private Supplier<String> displayValueSupplier;
    private Supplier<String> suffixSupplier = () -> "";
    private Function<ValueFormatter, ColorRGBA> textColorProvider = valueFormatter -> ColorRGBA.WHITE;
    private Function<ValueFormatter, ColorRGBA> suffixColorProvider;
    private TextInputField editor;
    private boolean editing;
    private static ValueFormatter activeEditor;
    private static final float EDITOR_WIDTH_PADDING = 40.0f;
    private static final float EDITOR_X_OFFSET = 3.0f;

    public ValueFormatter(FontMetrics fontMetrics, ValueSupplier valueSupplier, ValueConsumer valueConsumer, float f, float f2) {
        this.fontMetrics = fontMetrics;
        this.valueSupplier = valueSupplier;
        this.valueConsumer = valueConsumer;
        this.minimumValue = f;
        this.maximumValue = f2;
        this.displayValueSupplier = () -> moscow.rockstar.util.NumberFormatting.formatDecimal(valueSupplier.getValue());
        this.height(fontMetrics.getFontTopOffset());
        this.cursor(Cursor.HAND);
        this.snapPosition();
        this.snapSize();
    }

    public ValueFormatter setDisplayValue(Supplier<String> supplier) {
        if (supplier != null) {
            this.displayValueSupplier = supplier;
        }
        return this;
    }

    public ValueFormatter setSuffix(Supplier<String> supplier) {
        if (supplier != null) {
            this.suffixSupplier = supplier;
        }
        return this;
    }

    public ValueFormatter setTextColor(ColorRGBA colorRGBA) {
        this.textColorProvider = valueFormatter -> colorRGBA;
        return this;
    }

    public ValueFormatter setTextColorProvider(Supplier<ColorRGBA> supplier) {
        this.textColorProvider = valueFormatter -> (ColorRGBA)supplier.get();
        return this;
    }

    public ValueFormatter setTextColorProvider(Function<ValueFormatter, ColorRGBA> function) {
        this.textColorProvider = function;
        return this;
    }

    public ValueFormatter setSuffixColor(ColorRGBA colorRGBA) {
        this.suffixColorProvider = valueFormatter -> colorRGBA;
        return this;
    }

    public ValueFormatter setSuffixColorProvider(Function<ValueFormatter, ColorRGBA> function) {
        this.suffixColorProvider = function;
        return this;
    }

    public ValueFormatter setWidth(float f) {
        super.width(f);
        return this;
    }

    public ValueFormatter fillAvailableWidth() {
        super.fillWidth();
        return this;
    }

    public boolean isEditing() {
        return this.editing;
    }

    private TextInputField getEditor() {
        if (this.editor == null) {
            this.editor = new TextInputField(this.fontMetrics);
            this.editor.setNumericOnly(true);
        }
        return this.editor;
    }

    public static void commitActiveEditor(UiNode uiNode) {
        ValueFormatter valueFormatter = activeEditor;
        if (valueFormatter != null && valueFormatter != uiNode && valueFormatter.editing) {
            valueFormatter.commitEditing();
        }
    }

    private void beginEditing() {
        ValueFormatter.commitActiveEditor(this);
        String string = moscow.rockstar.util.NumberFormatting.formatDecimal(this.valueSupplier.getValue());
        this.getEditor().setText(string);
        this.getEditor().setPlaceholder(string);
        this.getEditor().setFocused(true);
        this.editing = true;
        activeEditor = this;
    }

    private void commitEditing() {
        if (this.editor != null) {
            try {
                String string = this.editor.getText().replace(',', '.');
                if (!(string.isEmpty() || string.equals("-") || string.equals("."))) {
                    float f = Float.parseFloat(string);
                    f = Math.max(this.minimumValue, Math.min(this.maximumValue, f));
                    this.valueConsumer.acceptValue(f);
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            this.editor.setFocused(false);
        }
        this.editing = false;
        if (activeEditor == this) {
            activeEditor = null;
        }
    }

    private void cancelEditing() {
        if (this.editor != null) {
            this.editor.setFocused(false);
        }
        this.editing = false;
        if (activeEditor == this) {
            activeEditor = null;
        }
    }

    @Override
    public boolean mouseClicked(float f, float f2, PointerAction pointerAction) {
        if (this.contains(f, f2)) {
            if (pointerAction == PointerAction.LEFT_CLICK) {
                if (!this.editing) {
                    this.beginEditing();
                } else {
                    this.getEditor().mouseClicked(f, f2, pointerAction);
                }
            }
            return true;
        }
        if (this.editing) {
            this.commitEditing();
        }
        return false;
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (!this.editing) {
            return false;
        }
        if (n == 257 || n == 335) {
            this.commitEditing();
            return true;
        }
        if (n == 256) {
            this.cancelEditing();
            return true;
        }
        this.getEditor().keyPressed(n, n2, n3);
        return true;
    }

    @Override
    public boolean charTyped(char c, int n) {
        if (!this.editing) {
            return false;
        }
        return this.getEditor().charTyped(c, n);
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        if (this.editing && this.editor != null && !this.editor.isFocused()) {
            this.commitEditing();
        }
    }

    private String getDisplayedText() {
        String string = this.editing && this.editor != null ? this.editor.getText() : this.displayValueSupplier.get();
        return string == null ? "" : string;
    }

    @Override
    protected void measure() {
        if (!this.explicitW) {
            String string = this.suffixSupplier.get();
            this.prefW = Math.max(6.0f, this.fontMetrics.measureText(this.getDisplayedText()) + this.fontMetrics.measureText(string == null ? "" : string));
        }
        if (!this.explicitH) {
            this.prefH = this.fontMetrics.getFontTopOffset();
        }
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        Object object;
        float f2;
        float f3 = this.y() + this.h() / 2.0f - this.fontMetrics.getFontTopOffset() / 2.0f;
        ColorRGBA colorRGBA = this.textColorProvider.apply(this);
        String string = this.suffixSupplier.get();
        if (this.editing && this.editor != null) {
            f2 = this.fontMetrics.measureText(this.editor.getText());
            this.editor.setBounds(this.x() - 3.0f, f3 - 3.0f, f2 + 40.0f, this.fontMetrics.getFontTopOffset() + 6.0f);
            this.editor.setTextColor(colorRGBA);
            this.editor.render(drawContext);
        } else {
            object = this.getDisplayedText();
            f2 = this.fontMetrics.measureText((String)object);
            if (!((String)object).isEmpty()) {
                drawContext.drawText(this.fontMetrics, (String)object, this.x(), f3, colorRGBA);
            }
        }
        if (string != null && !string.isEmpty()) {
            object = this.suffixColorProvider != null ? this.suffixColorProvider.apply(this) : colorRGBA;
            drawContext.drawText(this.fontMetrics, string, this.x() + f2, f3, (ColorRGBA)object);
        }
    }



    public static interface ValueSupplier {
        public float getValue();
    }

    public static interface ValueConsumer {
        public void acceptValue(float var1);
    }
}

