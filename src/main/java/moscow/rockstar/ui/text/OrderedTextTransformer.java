package moscow.rockstar.ui.text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import moscow.rockstar.modules.visuals.hud.NameProtect;
import net.minecraft.text.Style;
import net.minecraft.text.OrderedText;

/** Converts and selectively rewrites Minecraft's styled text stream. */
public final class OrderedTextTransformer {
    private OrderedTextTransformer() {
    }

    public static CharSequence toPlainText(OrderedText orderedText) {
        StringBuilder plainText = new StringBuilder();
        orderedText.accept((index, style, codePoint) -> {
            plainText.appendCodePoint(codePoint);
            return true;
        });
        return plainText;
    }

    public static OrderedText replaceProtectedText(OrderedText orderedText, NameProtect nameProtect) {
        List<Style> styles = new ArrayList<>();
        List<StringBuilder> segments = new ArrayList<>();
        orderedText.accept((index, style, codePoint) -> {
            if (segments.isEmpty() || !styles.get(styles.size() - 1).equals(style)) {
                styles.add(style);
                segments.add(new StringBuilder());
            }
            segments.get(segments.size() - 1).appendCodePoint(codePoint);
            return true;
        });
        if (segments.isEmpty()) {
            return null;
        }

        UnaryOperator<String> replace = nameProtect::replaceProtectedText;
        List<OrderedText> rewrittenSegments = new ArrayList<>(segments.size());
        StringBuilder combinedText = new StringBuilder();
        boolean changed = false;
        for (int index = 0; index < segments.size(); index++) {
            String original = segments.get(index).toString();
            String rewritten = replace.apply(original);
            changed |= !rewritten.equals(original);
            combinedText.append(rewritten);
            rewrittenSegments.add(OrderedText.styledForwardsVisitedString(rewritten, styles.get(index)));
        }
        if (!changed) {
            return null;
        }

        String rewrittenCombined = replace.apply(combinedText.toString());
        if (!rewrittenCombined.equals(combinedText.toString())) {
            return OrderedText.styledForwardsVisitedString(rewrittenCombined, styles.get(0));
        }
        return OrderedText.concat(rewrittenSegments);
    }
}
