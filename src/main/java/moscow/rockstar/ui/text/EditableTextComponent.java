package moscow.rockstar.ui.text;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.AnimatedValue;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.PointerAction;
import pyrock.utility.render.ColorRGBA;

/**
 * A layout-aware text editor node (original rockstar/ilIlil/IIiII).
 *
 * The node owns a {@link TextInputField} and only positions/paints it: the field
 * itself owns focus, caret and selection. The node has no intrinsic measure pass -
 * it starts at a fixed 120x15 and is resized by the caller.
 */
public class EditableTextComponent extends UiNode {
    private final FontMetrics font;
    private final TextInputField editor;
    private final Consumer<String> changeListener;
    private String lastText;
    private Supplier<String> placeholder = () -> "";
    private Function<EditableTextComponent, ColorRGBA> background;
    private Function<EditableTextComponent, ColorRGBA> border;
    private float borderWidth = 0.0f;
    private Function<EditableTextComponent, ColorRGBA> textColor = editableTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR;
    private float radius = 4.0f;
    private float padding = 0.0f;
    private final AnimatedValue focusAnimation =
        new AnimatedValue(0.0f, Motion.resolveMotionMotionFromLongAndEasing(200L, Easing.easeOutCubic));

    public EditableTextComponent(FontMetrics fontMetrics, String string, Consumer<String> consumer) {
        this.font = fontMetrics;
        this.changeListener = consumer;
        this.editor = new TextInputField(fontMetrics);
        this.editor.setText(string == null ? "" : string);
        this.lastText = this.editor.getText();
        this.size(120.0f, 15.0f);
    }

    /** Original I()Ljava/lang/String; */
    public String getText() {
        return this.editor.getText();
    }

    /** Original I(Ljava/lang/String;) */
    public EditableTextComponent setText(String string) {
        String value = string == null ? "" : string;
        if (value.equals(this.editor.getText())) {
            return this;
        }
        this.editor.setText(value);
        this.lastText = value;
        return this;
    }

    /** Original I(Ljava/util/function/Supplier;) */
    public EditableTextComponent placeholder(Supplier<String> supplier) {
        if (supplier != null) {
            this.placeholder = supplier;
        }
        return this;
    }

    /** Original i(Ljava/lang/String;) */
    public EditableTextComponent placeholder(String string) {
        this.placeholder = () -> string;
        return this;
    }

    /** Original I(Z) */
    public EditableTextComponent numericOnly(boolean bl) {
        this.editor.setNumericOnly(bl);
        return this;
    }

    /** Original I(I) / i(I) - the obfuscated class declares both, with identical bodies. */
    public EditableTextComponent maxLength(int n) {
        this.editor.setMaxLength(n);
        return this;
    }

    /** Original I() - switches the underlying field into password mode. */
    public EditableTextComponent password() {
        this.editor.passwordField();
        return this;
    }

    /** Original I(Lpyrock/utility/render/ColorRGBA;) */
    public EditableTextComponent background(ColorRGBA colorRGBA) {
        this.background = editableTextComponent -> colorRGBA;
        return this;
    }

    /** Original I(Ljava/util/function/Function;) */
    public EditableTextComponent background(Function<EditableTextComponent, ColorRGBA> function) {
        this.background = function;
        return this;
    }

    /** Original I(FLpyrock/utility/render/ColorRGBA;) - the border width/colour pair. */
    public EditableTextComponent border(float f, ColorRGBA colorRGBA) {
        this.borderWidth = f;
        this.border = editableTextComponent -> colorRGBA;
        return this;
    }

    /** Original I(FLjava/util/function/Function;) - the border width/provider pair. */
    public EditableTextComponent border(float f, Function<EditableTextComponent, ColorRGBA> function) {
        this.borderWidth = f;
        this.border = function;
        return this;
    }

    /** Original I(F) */
    public EditableTextComponent radius(float f) {
        this.radius = f;
        return this;
    }

    /** Original i(Lpyrock/utility/render/ColorRGBA;) */
    public EditableTextComponent textColor(ColorRGBA colorRGBA) {
        this.textColor = editableTextComponent -> colorRGBA;
        return this;
    }

    /** Original i(Ljava/util/function/Function;) */
    public EditableTextComponent textColor(Function<EditableTextComponent, ColorRGBA> function) {
        this.textColor = function;
        return this;
    }

    /** Original i(F) - horizontal inset applied to the editor rect only. */
    public EditableTextComponent padding(float f) {
        this.padding = f;
        return this;
    }

    /** Original I()Z */
    public boolean isFocused() {
        return this.editor.isFocused();
    }

    /** Original I()F - the 0..1 focus animation progress. */
    public float focusProgress() {
        return this.focusAnimation.getCurrent();
    }

    @Override
    public EditableTextComponent width(float f) {
        super.width(f);
        return this;
    }

    @Override
    public EditableTextComponent height(float f) {
        super.height(f);
        return this;
    }

    @Override
    public EditableTextComponent size(float f, float f2) {
        super.size(f, f2);
        return this;
    }

    @Override
    public EditableTextComponent fillWidth() {
        super.fillWidth();
        return this;
    }

    @Override
    public EditableTextComponent fillHeight() {
        super.fillHeight();
        return this;
    }

    /** Original I()V - the editor rect is inset horizontally only. */
    private void updateEditorBounds() {
        this.editor.setBounds(this.x() + this.padding, this.y(), this.w() - this.padding * 2.0f, this.h());
    }

    @Override
    public boolean mouseClicked(float f, float f2, PointerAction pointerAction) {
        if (!this.inFlow()) {
            return false;
        }
        this.updateEditorBounds();
        this.editor.mouseClicked(f, f2, pointerAction);
        return this.contains(f, f2);
    }

    @Override
    public void mouseReleased(float f, float f2, PointerAction pointerAction) {
        this.editor.mouseReleased(f, f2, pointerAction);
        super.mouseReleased(f, f2, pointerAction);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (!this.editor.isFocused()) {
            return false;
        }
        this.editor.keyPressed(n, n2, n3);
        return true;
    }

    @Override
    public boolean charTyped(char c, int n) {
        if (!this.editor.isFocused()) {
            return false;
        }
        return this.editor.charTyped(c, n);
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        this.focusAnimation.setTarget(this.editor.isFocused() ? 1.0f : 0.0f);
        this.focusAnimation.update(f);
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        ColorRGBA colorRGBA;
        if (this.background != null && (colorRGBA = this.background.apply(this)) != null && colorRGBA.getAlpha() > 0.0f) {
            drawContext.drawRoundedRect(this.x(), this.y(), this.w(), this.h(), WidgetState.uniform(this.radius), colorRGBA);
        }
        if (this.border != null && this.borderWidth > 0.0f && (colorRGBA = this.border.apply(this)) != null && colorRGBA.getAlpha() > 0.0f) {
            drawContext.drawRoundedBorder(this.x(), this.y(), this.w(), this.h(), this.borderWidth,
                WidgetState.uniform(this.radius), colorRGBA);
        }
        this.updateEditorBounds();
        this.editor.setOpacity(1.0f);
        this.editor.setPlaceholder(this.placeholder.get());
        this.editor.setTextColor(this.textColor.apply(this));
        this.editor.render(drawContext);
        String current = this.editor.getText();
        if (!current.equals(this.lastText)) {
            this.lastText = current;
            this.changeListener.accept(current);
        }
    }
}
