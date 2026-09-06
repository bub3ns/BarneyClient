package moscow.rockstar.ui.text;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.platform.WindowsImeApi;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.state.UiScissorStack;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.CursorManager;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.util.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.Rect;

/**
 * Editable single-line text control used by the client screens and settings.
 *
 * <p>This is the source-level counterpart of the original grapheme-aware
 * editor.  Its public API deliberately describes the editor state instead of
 * exposing the obfuscated owner names that appeared in the decompiled tree.</p>
 */
public final class TextInputField extends Rect {
    private final FontMetrics fontMetrics;
    private String text = "";
    private String placeholder = "";
    private Supplier<String> placeholderSupplier;
    private Function<TextInputField, ColorRGBA> colorProvider = ignored -> ColorRGBA.WHITE;
    private ColorRGBA color = ColorRGBA.WHITE;
    private int maxLength;
    private int caret;
    private int selectionAnchor;
    private boolean focused;
    private boolean numericOnly;
    private boolean password;
    private float opacity = 1.0f;
    private float scrollOffset;
    private final Animation focusAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
    private final Animation caretAnimation = new Animation(300L, 0.0f, Easing.easeOutBack);
    private final Timer caretBlinkTimer = new Timer();
    private final Timer scrollTimer = new Timer();
    private static TextInputField activeField;

    /** {@code \X} - the grapheme cluster the original editor advances by. */
    private static final Pattern GRAPHEME_PATTERN = Pattern.compile("\\X");
    /** The original masks every grapheme with a single asterisk. */
    private static final String MASK_GRAPHEME = "*";

    public TextInputField(FontMetrics fontMetrics) {
        this.fontMetrics = fontMetrics;
    }

    public TextInputField passwordField() {
        this.password = true;
        return this;
    }

    public TextInputField setText(String value) {
        String normalized = value == null ? "" : value;
        if (this.numericOnly) {
            normalized = sanitizeNumber(normalized);
        }
        if (this.maxLength > 0 && normalized.length() > this.maxLength) {
            normalized = normalized.substring(0, this.maxLength);
        }
        this.text = normalized;
        this.caret = normalized.length();
        this.selectionAnchor = this.caret;
        this.scrollOffset = 0.0f;
        this.caretBlinkTimer.reset();
        return this;
    }

    public TextInputField insertText(String value) {
        if (value == null || value.isEmpty()) {
            return this;
        }
        if (this.hasSelection()) {
            deleteSelection();
        }
        String insertion = this.numericOnly ? sanitizeNumber(value) : value;
        if (insertion.isEmpty()) {
            return this;
        }
        int allowed = this.maxLength > 0 ? this.maxLength - this.text.length() : insertion.length();
        if (allowed <= 0) {
            return this;
        }
        if (insertion.length() > allowed) {
            insertion = insertion.substring(0, allowed);
        }
        this.text = this.text.substring(0, this.caret) + insertion + this.text.substring(this.caret);
        this.caret += insertion.length();
        this.selectionAnchor = this.caret;
        this.caretBlinkTimer.reset();
        return this;
    }

    public TextInputField setPlaceholder(String value) {
        this.placeholder = value == null ? "" : value;
        this.placeholderSupplier = null;
        return this;
    }

    public TextInputField placeholder(Supplier<String> supplier) {
        this.placeholderSupplier = supplier;
        return this;
    }

    public TextInputField setTextColor(ColorRGBA value) {
        this.color = value == null ? ColorRGBA.WHITE : value;
        this.colorProvider = ignored -> this.color;
        return this;
    }

    public TextInputField textColor(Function<TextInputField, ColorRGBA> provider) {
        this.colorProvider = provider == null ? ignored -> ColorRGBA.WHITE : provider;
        return this;
    }

    public TextInputField setOpacity(float value) {
        this.opacity = Math.max(0.0f, Math.min(1.0f, value));
        return this;
    }

    public TextInputField setNumericOnly(boolean value) {
        this.numericOnly = value;
        if (value) {
            this.setText(this.text);
        }
        return this;
    }

    public TextInputField setMaxLength(int value) {
        this.maxLength = Math.max(0, value);
        if (this.maxLength > 0 && this.text.length() > this.maxLength) {
            this.setText(this.text.substring(0, this.maxLength));
        }
        return this;
    }

    public TextInputField setFocused(boolean value) {
        if (value && activeField != null && activeField != this) {
            activeField.setFocused(false);
        }
        this.focused = value;
        if (value) {
            activeField = this;
        }
        if (!value) {
            this.selectionAnchor = this.caret;
        }
        return this;
    }

    public static TextInputField getActiveField() {
        return activeField;
    }

    public boolean isFocused() {
        return this.focused;
    }

    public String getText() {
        return this.text;
    }

    public void clear() {
        this.text = "";
        this.caret = 0;
        this.selectionAnchor = 0;
    }

    public void setBounds(float x, float y, float width, float height) {
        this.set(x, y, width, height);
    }

    /**
     * Faithful port of the original editor's draw pass.
     *
     * <p>Every horizontal coordinate is inset by {@code pad}, the same value the
     * original uses to vertically centre the text in the field.  Dropping that
     * inset is what pushed the text hard against the leading icon of every
     * search control.</p>
     */
    public void render(RockstarDrawContext drawContext) {
        // ORIGINAL: the first instruction of rockstar/ilIlil/IiIiIIIII#i(Lrockstar/ilIlil/III;)V -
        // while this field holds focus, drain any pending Windows IME composition that the
        // ALT+chat shortcut armed. This is the ONLY caller of the helper in the original
        // (confirmed with recaf_search_references on iIIIIiiIi.I()V); both it and the helper class
        // were dropped by the remap.
        if (this.focused) {
            WindowsImeApi.cancelComposition();
        }
        float advance = 0.0f;
        float pad = this.height / 2.0f - this.fontMetrics.getFontTopOffset() / 2.0f;
        float caretWidth = this.fontMetrics.getFontTopOffset() / 8.0f;

        this.focusAnimation.setReverse(this.focused);

        List<String> graphemes = splitGraphemes(this.text);
        int graphemeCount = graphemes.size();
        int caretIndex = graphemeIndex(graphemes, this.caret);
        int anchorIndex = graphemeIndex(graphemes, this.selectionAnchor);
        float[] prefix = new float[graphemeCount + 1];
        float firstGraphemeWidth = 0.0f;
        StringBuilder shownBuilder = new StringBuilder();
        for (int i = 0; i < graphemeCount; ++i) {
            String piece = this.password ? MASK_GRAPHEME : graphemes.get(i);
            shownBuilder.append(piece);
            float pieceWidth = EmojiTextNode.measureTextWidth(this.fontMetrics, piece);
            prefix[i + 1] = prefix[i] + pieceWidth;
            if (i == 0) {
                firstGraphemeWidth = pieceWidth;
            }
        }
        String shown = shownBuilder.toString();

        if (this.contains(drawContext.mouseX(), drawContext.mouseY()) && this.opacity > 0.0f) {
            CursorManager.request(Cursor.IBEAM);
        }

        float focus = this.focusAnimation.getValue();
        float selectionStart = prefix[Math.min(caretIndex, anchorIndex)];
        float selectionEnd = prefix[Math.max(caretIndex, anchorIndex)];

        ColorRGBA drawColor = this.colorProvider.apply(this);
        if (drawColor == null) {
            drawColor = this.color;
        }

        UiScissorStack.push(drawContext.getMatrices(), this.x, this.y, this.width, this.height);
        float previousAlpha = RenderSystem.getShaderColor()[3];
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, previousAlpha * this.opacity);

        drawContext.drawRect(this.x + this.scrollOffset + pad + selectionStart, this.y + pad - 1.0f,
            selectionEnd - selectionStart, this.fontMetrics.getFontTopOffset() + 2.0f,
            ColorPalette.ACCENT_COLOR.withAlpha(114.75f * focus));

        if (graphemeCount == 0) {
            String placeholderText = this.placeholderSupplier == null
                ? this.placeholder
                : this.placeholderSupplier.get();
            if (placeholderText != null && !placeholderText.isEmpty()) {
                drawContext.drawText(this.fontMetrics, placeholderText, this.x + advance + pad,
                    this.y + pad - 2.0f * focus, drawColor.mulAlpha(0.75f * (1.0f - focus)));
            }
        }

        if (!shown.isEmpty()) {
            EmojiTextNode.drawTextWithEmoji(drawContext, this.fontMetrics, shown,
                this.x + advance + pad + this.scrollOffset, this.y + pad,
                this.fontMetrics.getFontTopOffset(), drawColor);
        }

        float caretTarget = prefix[caretIndex] + (caretIndex == graphemeCount ? 1.0f : 0.0f);
        if (this.scrollTimer.hasElapsed(10L)) {
            if (graphemeCount != 0 && caretTarget + pad + this.scrollOffset > this.width - 5.0f) {
                this.scrollOffset -= firstGraphemeWidth;
                this.scrollTimer.reset();
            } else if (graphemeCount != 0 && caretTarget + pad + this.scrollOffset < 5.0f) {
                this.scrollOffset += firstGraphemeWidth;
                this.scrollTimer.reset();
            }
            if (EmojiTextNode.measureTextWidth(this.fontMetrics, this.text) < this.width - 10.0f) {
                this.scrollOffset = 0.0f;
            }
        }

        this.caretAnimation.setEasing(Easing.easeOutBackSoft);
        this.caretAnimation.update(caretTarget);
        float caretValue = this.caretAnimation.getValue();
        ItemRenderUtils.translateAndRotate(drawContext.getMatrices(),
            this.x + pad + this.scrollOffset + caretValue + caretWidth / 2.0f, this.y + pad - 1.0f,
            Math.clamp(caretTarget - caretValue, -20.0f, 20.0f));
        drawContext.drawRect(this.x + pad + caretValue + this.scrollOffset, this.y + pad - 1.0f,
            caretWidth, this.fontMetrics.getFontTopOffset() + 2.0f,
            drawColor.mulAlpha((float)((double)(0.78431374f * focus)
                * (!this.caretBlinkTimer.hasElapsed(300L)
                    ? 3.0
                    : MathUtils.lookupSine((double)System.currentTimeMillis() / 200.0) + 2.0) / 3.0)));
        ItemRenderUtils.popMatrix(drawContext.getMatrices());

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, previousAlpha);
        UiScissorStack.pop();
    }

    private static List<String> splitGraphemes(String value) {
        List<String> graphemes = new ArrayList<String>();
        if (value == null || value.isEmpty()) {
            return graphemes;
        }
        Matcher matcher = GRAPHEME_PATTERN.matcher(value);
        while (matcher.find()) {
            graphemes.add(matcher.group());
        }
        return graphemes;
    }

    /** Converts a character offset into {@code text} to the original's grapheme index. */
    private static int graphemeIndex(List<String> graphemes, int characterOffset) {
        int consumed = 0;
        for (int i = 0; i < graphemes.size(); ++i) {
            if (consumed >= characterOffset) {
                return i;
            }
            consumed += graphemes.get(i).length();
        }
        return graphemes.size();
    }

    public void mouseClicked(double mouseX, double mouseY, PointerAction action) {
        if (action != PointerAction.LEFT_CLICK) {
            return;
        }
        if (!this.contains(mouseX, mouseY)) {
            this.setFocused(false);
            return;
        }
        this.setFocused(true);
        List<String> graphemes = splitGraphemes(this.text);
        int index = graphemes.size();
        float walked = 0.0f;
        for (int i = 0; i < graphemes.size(); ++i) {
            float pieceWidth = EmojiTextNode.measureTextWidth(this.fontMetrics,
                this.password ? MASK_GRAPHEME : graphemes.get(i));
            if (mouseX < (double)(this.x + this.scrollOffset + walked + pieceWidth + pieceWidth / 2.0f)) {
                index = i;
                break;
            }
            walked += pieceWidth;
        }
        int offset = 0;
        for (int i = 0; i < index; ++i) {
            offset += graphemes.get(i).length();
        }
        this.caret = offset;
        this.selectionAnchor = offset;
    }

    public void mouseReleased(double mouseX, double mouseY, PointerAction action) {
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.focused) {
            return;
        }
        // ORIGINAL: rockstar/ilIlil/IiIiIIIII#I(III)V dispatches select-all/copy/cut/paste through
        // the vanilla Screen helpers (method_25439 / 25438 / 25436 / 25437) before falling through
        // to the plain keys, and widens the arrow and delete steps to a whole word while Ctrl is
        // held (method_25441). The remap had none of it - Ctrl+A/C/X/V did nothing.
        if (Screen.isSelectAll(keyCode)) {
            this.selectionAnchor = 0;
            this.caret = this.text.length();
            this.caretBlinkTimer.reset();
            return;
        }
        if (Screen.isCopy(keyCode)) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.selectedTextOrAll());
            return;
        }
        if (Screen.isCut(keyCode)) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.selectedTextOrAll());
            if (this.hasSelection()) {
                this.deleteSelection();
            } else {
                this.clear();
            }
            return;
        }
        if (Screen.isPaste(keyCode)) {
            this.insertText(MinecraftClient.getInstance().keyboard.getClipboard());
            return;
        }
        boolean byWord = Screen.hasControlDown();
        switch (keyCode) {
            case 259 -> backspace(byWord ? Math.max(1, wordSkipDistance(false)) : 1);
            case 261 -> deleteForward(byWord ? Math.max(1, wordSkipDistance(true)) : 1);
            case 263 -> moveCaret(-(byWord ? Math.max(1, wordSkipDistance(false)) : 1), (modifiers & 1) != 0);
            case 262 -> moveCaret(byWord ? Math.max(1, wordSkipDistance(true)) : 1, (modifiers & 1) != 0);
            case 268 -> moveCaretTo(0, (modifiers & 1) != 0);
            case 269 -> moveCaretTo(this.text.length(), (modifiers & 1) != 0);
            case 257, 258 -> setFocused(false);
            default -> {
            }
        }
    }

    public boolean charTyped(char character, int modifiers) {
        if (!this.focused || Character.isISOControl(character)) {
            return false;
        }
        if (this.numericOnly) {
            if (!Character.isDigit(character) && character != '.' && character != ',' && character != '-') {
                return false;
            }
            if (character == '-' && this.caret != 0) {
                return false;
            }
            if ((character == '.' || character == ',') && (this.text.contains(".") || this.text.contains(","))) {
                return false;
            }
            if (character == ',') {
                character = '.';
            }
        }
        this.insertText(String.valueOf(character));
        return true;
    }

    public boolean contains(double mouseX, double mouseY) {
        return this.x <= mouseX && mouseX < this.x + this.width && this.y <= mouseY && mouseY < this.y + this.height;
    }

    public FontMetrics getFontMetrics() {
        return this.fontMetrics;
    }

    private boolean hasSelection() {
        return this.caret != this.selectionAnchor;
    }

    private void deleteSelection() {
        int start = Math.min(this.caret, this.selectionAnchor);
        int end = Math.max(this.caret, this.selectionAnchor);
        this.text = this.text.substring(0, start) + this.text.substring(end);
        this.caret = start;
        this.selectionAnchor = start;
        this.caretBlinkTimer.reset();
    }

    private void backspace(int count) {
        if (hasSelection()) {
            deleteSelection();
        } else {
            for (int i = 0; i < count && this.caret > 0; ++i) {
                this.text = this.text.substring(0, this.caret - 1) + this.text.substring(this.caret);
                --this.caret;
            }
            this.selectionAnchor = this.caret;
        }
        this.caretBlinkTimer.reset();
    }

    private void deleteForward(int count) {
        if (hasSelection()) {
            deleteSelection();
        } else {
            for (int i = 0; i < count && this.caret < this.text.length(); ++i) {
                this.text = this.text.substring(0, this.caret) + this.text.substring(this.caret + 1);
            }
        }
        this.selectionAnchor = this.caret;
        this.caretBlinkTimer.reset();
    }

    /** The selection, or the whole line when there is none - the original's copy/cut source. */
    private String selectedTextOrAll() {
        if (!this.hasSelection()) {
            return this.text;
        }
        return this.text.substring(Math.min(this.caret, this.selectionAnchor),
                                   Math.max(this.caret, this.selectionAnchor));
    }

    /** Distance from the caret to the next word boundary - the original's {@code I(Z)I}. */
    private int wordSkipDistance(boolean forward) {
        int position = this.caret;
        if (forward) {
            int length = this.text.length();
            while (position < length && this.text.charAt(position) == ' ') {
                ++position;
            }
            while (position < length && this.text.charAt(position) != ' ') {
                ++position;
            }
            return position - this.caret;
        }
        while (position > 0 && this.text.charAt(position - 1) == ' ') {
            --position;
        }
        while (position > 0 && this.text.charAt(position - 1) != ' ') {
            --position;
        }
        return this.caret - position;
    }

    private void moveCaret(int amount, boolean extendSelection) {
        this.moveCaretTo(Math.max(0, Math.min(this.text.length(), this.caret + amount)), extendSelection);
    }

    private void moveCaretTo(int position, boolean extendSelection) {
        this.caret = Math.max(0, Math.min(this.text.length(), position));
        if (!extendSelection) {
            this.selectionAnchor = this.caret;
        }
        this.caretBlinkTimer.reset();
    }

    private static String sanitizeNumber(String value) {
        StringBuilder result = new StringBuilder();
        boolean decimal = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c)) {
                result.append(c);
            } else if ((c == '.' || c == ',') && !decimal) {
                result.append('.');
                decimal = true;
            } else if (c == '-' && result.isEmpty()) {
                result.append(c);
            }
        }
        return result.toString();
    }
}
