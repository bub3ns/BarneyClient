/*
 * Decompiled with CFR 0.152.
 */
package pyrock;

import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;

public class GlobalVars {
    public void install(String string) {
    }

    public FontMetrics font(String string, float f) {
        return Font.byName(string).metrics(f);
    }
}
