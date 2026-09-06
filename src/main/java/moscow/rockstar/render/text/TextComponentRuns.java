package moscow.rockstar.render.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

/** Converts a styled Minecraft text component into the runs used by the batched renderers. */
public final class TextComponentRuns {
    private TextComponentRuns() {
    }

    public static List<TextComponentRun> flatten(Text text, int defaultColor) {
        if (text == null) {
            return List.of();
        }
        List<TextComponentRun> runs = new ArrayList<>();
        text.visit((Style style, String value) -> {
            if (value != null && !value.isEmpty()) {
                TextColor textColor = style.getColor();
                runs.add(new TextComponentRun(value, textColor == null ? defaultColor : (textColor.getRgb() | 0xFF000000)));
            }
            return Optional.<Void>empty();
        }, Style.EMPTY);
        return List.copyOf(runs);
    }
}
