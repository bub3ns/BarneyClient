package moscow.rockstar.modules.visuals.esp.entities;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import moscow.rockstar.render.text.FontRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

/**
 * Port of the two text helpers the ESP nametag overlay uses out of
 * {@code rockstar/ilIlil/iIIIIiIiI}:
 *
 * <ul>
 *   <li>{@code iIIIIiIiI.I (Lnet/minecraft/class_2561;Lrockstar/ilIlil/IIiiii;)Lnet/minecraft/class_5250;}
 *       - {@link #sanitize(Text, FontRenderer)}</li>
 *   <li>{@code iIIIIiIiI.i (Ljava/lang/String;)Ljava/lang/String;} - {@link #smallCaps(String)}</li>
 * </ul>
 *
 * <p>Both are shared utilities in the original client; they live beside the
 * nametag overlay here because the nametag port is their only consumer in the
 * remapped tree.</p>
 */
public final class NametagTextUtils {
    private static final String SMALL_CAPS_SOURCE = "\u1d00\u0299\u1d04\u1d05\u1d07\u0493\ua730\u0262\u029c\u026a\u1d0a\u1d0b\u029f\u1d0d\u0274\u1d0f\u1d18\ua7af\u01eb\u0280\ua731\u0455\u1d1b\u1d1c\u1d20\u1d21\u1d61\u028f\u1d22";
    private static final String SMALL_CAPS_TARGET = "ABCDEFFGHIJKLMNOPQQRSSTUVWXYZ";

    private NametagTextUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Flattens a styled component into something the client font can actually
     * draw: private-use rank glyphs become their plain-text label (coloured with
     * the label gradient), legacy section codes are stripped, runs of spaces are
     * collapsed, empty {@code []} pairs are dropped and - when the supplied font
     * has glyphs at all - characters the font cannot display are removed.
     */
    public static MutableText sanitize(Text class_25612, FontRenderer font) {
        if (class_25612 == null) {
            return Text.empty();
        }
        StringBuilder characters = new StringBuilder();
        List<Style> styles = new ArrayList<Style>();
        BitSet glyphMask = new BitSet();
        class_25612.visit((Style style, String value) -> {
            value.codePoints().forEach(codePoint -> {
                String label = RankGlyphRegistry.getGlyphLabel(codePoint);
                if (label == null) {
                    characters.appendCodePoint(codePoint);
                    while (styles.size() < characters.length()) {
                        styles.add(style);
                    }
                    return;
                }
                boolean colored = RankGlyphRegistry.isColoredGlyph(codePoint);
                glyphMask.set(characters.length(), characters.length() + label.length());
                for (int i = 0; i < label.length(); ++i) {
                    characters.append(label.charAt(i));
                    styles.add(colored
                        ? style.withColor(RankGlyphRegistry.getGlyphColor(codePoint, i, label.length(), 1.0f, 0) & 0xFFFFFF)
                        : style);
                }
            });
            return Optional.<Void>empty();
        }, Style.EMPTY);
        int wordStart = 0;
        for (int i = 0; i <= characters.length(); ++i) {
            if (i < characters.length() && !Character.isWhitespace(characters.charAt(i))) continue;
            int glyphAt = glyphMask.nextSetBit(wordStart);
            if (glyphAt >= 0 && glyphAt < i) {
                for (int j = wordStart; j < i; ++j) {
                    characters.setCharAt(j, Character.toUpperCase(characters.charAt(j)));
                }
            }
            wordStart = i + 1;
        }
        StringBuilder filtered = new StringBuilder();
        List<Style> filteredStyles = new ArrayList<Style>();
        boolean fontHasGlyphs = font != null && font.canDisplay('a');
        for (int i = 0; i < characters.length(); ++i) {
            char c = characters.charAt(i);
            char previous = filtered.isEmpty() ? (char)0 : filtered.charAt(filtered.length() - 1);
            if (c == 167) {
                ++i;
                continue;
            }
            if (c == 32 && (previous == 0 || previous == 32) || c != 32 && fontHasGlyphs && !font.canDisplay(c)) continue;
            if (c == 93 && previous == 91) {
                filtered.deleteCharAt(filtered.length() - 1);
                filteredStyles.removeLast();
                continue;
            }
            filtered.append(c);
            filteredStyles.add(styles.get(i));
        }
        while (!filtered.isEmpty() && filtered.charAt(filtered.length() - 1) == ' ') {
            filtered.deleteCharAt(filtered.length() - 1);
            filteredStyles.removeLast();
        }
        MutableText class_52502 = Text.empty();
        int runStart = 0;
        for (int i = 1; i <= filtered.length(); ++i) {
            if (i < filtered.length() && filteredStyles.get(i).equals(filteredStyles.get(runStart))) continue;
            class_52502.append((Text)Text.literal((String)filtered.substring(runStart, i)).setStyle(filteredStyles.get(runStart)));
            runStart = i;
        }
        return class_52502;
    }

    /** Rewrites the server's small-capital letters back to plain upper case. */
    public static String smallCaps(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(string.length());
        int wordStart = 0;
        boolean converted = false;
        for (int i = 0; i <= string.length(); ++i) {
            if (i == string.length() || Character.isWhitespace(string.charAt(i))) {
                if (converted) {
                    for (int j = wordStart; j < stringBuilder.length(); ++j) {
                        stringBuilder.setCharAt(j, Character.toUpperCase(stringBuilder.charAt(j)));
                    }
                }
                if (i < string.length()) {
                    stringBuilder.append(string.charAt(i));
                }
                wordStart = stringBuilder.length();
                converted = false;
                continue;
            }
            char c = string.charAt(i);
            int index = SMALL_CAPS_SOURCE.indexOf(c);
            if (index < 0) {
                stringBuilder.append(c);
                continue;
            }
            stringBuilder.append(SMALL_CAPS_TARGET.charAt(index));
            converted = true;
        }
        return stringBuilder.toString();
    }
}
