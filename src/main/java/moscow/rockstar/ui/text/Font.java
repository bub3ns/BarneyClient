/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.text;

import lombok.Generated;
import moscow.rockstar.render.text.FontRenderer;

public final class Font {
    public static final FontRenderer BOLD = FontRenderer.fromResource("bold", "bold");
    public static final FontRenderer MEDIUM = FontRenderer.fromResource("medium", "medium");
    public static final FontRenderer REGULAR = FontRenderer.fromResource("regular", "regular");
    public static final FontRenderer SEMIBOLD = FontRenderer.fromResource("semibold", "semi_bold");
    public static final FontRenderer LIGHT = FontRenderer.fromResource("light", "light");
    public static final FontRenderer ROUND_BOLD = FontRenderer.fromResource("roundbold", "round");
    public static final FontRenderer NOTO = FontRenderer.getFallbackFont();
    private static final String WARMUP_CHARACTERS = " !?.,:;-\u2013\u2014()[]{}<>/\\|+*=%#@&\"'`~^_$0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\u0430\u0431\u0432\u0433\u0434\u0435\u0451\u0436\u0437\u0438\u0439\u043a\u043b\u043c\u043d\u043e\u043f\u0440\u0441\u0442\u0443\u0444\u0445\u0446\u0447\u0448\u0449\u044a\u044b\u044c\u044d\u044e\u044f\u0410\u0411\u0412\u0413\u0414\u0415\u0401\u0416\u0417\u0418\u0419\u041a\u041b\u041c\u041d\u041e\u041f\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427\u0428\u0429\u042a\u042b\u042c\u042d\u042e\u042f";

    public static void warmUpFontCaches() {
        Thread thread = new Thread(() -> {
            for (FontRenderer fontRenderer : new FontRenderer[]{REGULAR, MEDIUM, SEMIBOLD, BOLD}) {
                fontRenderer.warmUp(WARMUP_CHARACTERS);
            }
        }, "rockstar-font-warmup");
        thread.setDaemon(true);
        thread.setPriority(1);
        thread.start();
    }

    public static void reloadAllFonts() {
        BOLD.reload();
        MEDIUM.reload();
        REGULAR.reload();
        SEMIBOLD.reload();
        LIGHT.reload();
        ROUND_BOLD.reload();
        Font.warmUpFontCaches();
    }

    public static FontRenderer byName(String string) {
        return switch (string.toLowerCase()) {
            case "noto" -> NOTO;
            case "bold" -> BOLD;
            case "medium" -> MEDIUM;
            case "light" -> LIGHT;
            case "semibold" -> SEMIBOLD;
            case "roundbold" -> ROUND_BOLD;
            default -> REGULAR;
        };
    }

    @Generated
    private Font() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
