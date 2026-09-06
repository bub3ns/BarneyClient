package moscow.rockstar.modules.visuals.esp.entities;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.text.Style;
import pyrock.utility.render.ColorRGBA;

/**
 * Port of {@code rockstar/ilIlil/IIiiIi}.
 *
 * <p>Server rank glyphs live in the Unicode private-use area and are supplied by
 * the server resource pack, so a client-side font cannot draw them.  This
 * registry maps every known glyph code point to the plain-text label the client
 * substitutes for it, plus the gradient colour that label is drawn in.</p>
 *
 * <p>In the original client this is a shared utility.  It is kept next to the
 * nametag overlay here because the nametag port is currently its only consumer
 * in the remapped tree.</p>
 */
public class RankGlyphRegistry {
    private static final Map<Integer, String> GLYPH_LABELS = new HashMap<Integer, String>();
    private static final Map<Integer, Integer> GLYPH_COLORS = new HashMap<Integer, Integer>();
    private static final Map<String, String> GROUP_LABELS = new HashMap<String, String>();
    private static final Map<String, Integer> GROUP_COLORS = new HashMap<String, Integer>();
    private static final int[] COLORED_GLYPHS = new int[]{42240, 42244, 42248, 42258, 42262, 42272, 42276, 42280, 42336, 42290, 42294, 42308, 42326, 42312, 42304, 42322, 42249, 42259, 42263, 42273, 42277, 42281, 42291, 42295, 42241, 42245};

    public static String getGlyphLabel(int n) {
        return GLYPH_LABELS.get(n);
    }

    public static boolean isColoredGlyph(int n) {
        for (int n2 : COLORED_GLYPHS) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }

    public static String getGroupLabel(String string) {
        return GROUP_LABELS.getOrDefault(string, string);
    }

    public static void registerGroupLabel(String string, String string2) {
        GROUP_LABELS.put(string, string2);
    }

    public static boolean isGroupPlaceholder(int n, Style class_25832) {
        if (n == 97 && class_25832 != null && class_25832.getFont() != null) {
            String string = class_25832.getFont().toString();
            return GROUP_LABELS.containsKey(string);
        }
        return false;
    }

    public static String getGroupLabelForStyle(Style class_25832) {
        if (class_25832 != null && class_25832.getFont() != null) {
            String string = class_25832.getFont().toString();
            return GROUP_LABELS.get(string);
        }
        return null;
    }

    public static int getGroupColor(String string) {
        return GROUP_COLORS.getOrDefault(string, -1);
    }

    public static int getGroupColor(String string, float f) {
        int n = RankGlyphRegistry.getGroupColor(string);
        if (n == -1) {
            return -1;
        }
        return ColorRGBA.applyOpacity((int)n, (float)f).getRGB();
    }

    public static int getGlyphColor(int n, int n2, int n3, float f, int n4) {
        boolean bl = false;
        int[] nArray = COLORED_GLYPHS;
        int n5 = nArray.length;
        for (int i = 0; i < n5; ++i) {
            int n6 = nArray[i];
            if (n6 != n) continue;
            bl = true;
            break;
        }
        Integer color = bl ? GLYPH_COLORS.get(n) : null;
        if (color == null) {
            return ColorRGBA.applyOpacity((int)n4, (float)f).getRGB();
        }
        float f2 = n3 > 1 ? (float)n2 / (float)(n3 - 1) : 0.0f;
        ColorRGBA colorRGBA = ColorRGBA.fromInt((int)color.intValue()).mix(ColorRGBA.darken((int)color.intValue(), (float)0.8f), f2);
        return ColorRGBA.applyOpacity((int)colorRGBA.getRGB(), (float)f).getRGB();
    }

    static {
        GLYPH_LABELS.put(9889, "");
        GLYPH_LABELS.put(9733, "");
        GLYPH_LABELS.put(42240, "PLAYER");
        GLYPH_LABELS.put(42244, "HERO");
        GLYPH_LABELS.put(42248, "TITAN");
        GLYPH_LABELS.put(42258, "AVENGER");
        GLYPH_LABELS.put(42262, "OVERLORD");
        GLYPH_LABELS.put(42272, "MAGISTER");
        GLYPH_LABELS.put(42276, "IMPERATOR");
        GLYPH_LABELS.put(42280, "DRAGON");
        GLYPH_LABELS.put(42336, "D.HELPER");
        GLYPH_LABELS.put(42290, "BULL");
        GLYPH_LABELS.put(42294, "TIGER");
        GLYPH_LABELS.put(42308, "DRACULA");
        GLYPH_LABELS.put(42326, "BUNNY");
        GLYPH_LABELS.put(42312, "COBRA");
        GLYPH_LABELS.put(42304, "HYDRA");
        GLYPH_LABELS.put(42322, "RABBIT");
        GLYPH_LABELS.put(42249, "HELPER");
        GLYPH_LABELS.put(42259, "ML.MODER");
        GLYPH_LABELS.put(42263, "MODER");
        GLYPH_LABELS.put(42273, "MODER+");
        GLYPH_LABELS.put(42277, "ST.MODER");
        GLYPH_LABELS.put(42281, "GL.MODER");
        GLYPH_LABELS.put(42291, "ML.ADMIN");
        GLYPH_LABELS.put(42295, "ADMIN");
        GLYPH_LABELS.put(42241, "MEDIA");
        GLYPH_LABELS.put(42245, "YT");
        GLYPH_LABELS.put(1171, "F");
        GLYPH_LABELS.put(1109, "S");
        GLYPH_LABELS.put(42927, "Q");
        GLYPH_LABELS.put(7424, "A");
        GLYPH_LABELS.put(665, "B");
        GLYPH_LABELS.put(7428, "C");
        GLYPH_LABELS.put(7429, "D");
        GLYPH_LABELS.put(7431, "E");
        GLYPH_LABELS.put(42800, "F");
        GLYPH_LABELS.put(610, "G");
        GLYPH_LABELS.put(668, "H");
        GLYPH_LABELS.put(618, "I");
        GLYPH_LABELS.put(7434, "J");
        GLYPH_LABELS.put(7435, "K");
        GLYPH_LABELS.put(671, "L");
        GLYPH_LABELS.put(7437, "M");
        GLYPH_LABELS.put(628, "N");
        GLYPH_LABELS.put(7439, "O");
        GLYPH_LABELS.put(7448, "P");
        GLYPH_LABELS.put(491, "Q");
        GLYPH_LABELS.put(640, "R");
        GLYPH_LABELS.put(7451, "T");
        GLYPH_LABELS.put(7452, "U");
        GLYPH_LABELS.put(42801, "S");
        GLYPH_LABELS.put(7456, "V");
        GLYPH_LABELS.put(7457, "W");
        GLYPH_LABELS.put(7521, "X");
        GLYPH_LABELS.put(655, "Y");
        GLYPH_LABELS.put(7458, "Z");
        GROUP_LABELS.put("custom:groups/hydra", "\u0413\u0438\u0434\u0440\u0430");
        GROUP_LABELS.put("custom:groups/cerberus", "\u0426\u0435\u0440\u0431\u0435\u0440");
        GROUP_LABELS.put("custom:groups/triton", "\u0422\u0440\u0438\u0442\u043e\u043d");
        GROUP_LABELS.put("custom:groups/phoenix", "\u0424\u0435\u043d\u0438\u043a\u0441");
        GROUP_LABELS.put("custom:groups/pandar", "\u041f\u0430\u043d\u0434\u0430\u0440");
        GROUP_LABELS.put("custom:groups/heat", "\u0416\u0430\u0440\u0430");
        GROUP_LABELS.put("custom:groups/cold", "\u0425\u043e\u043b\u043e\u0434");
        GROUP_LABELS.put("custom:groups/kronos", "\u041a\u0440\u043e\u043d\u043e\u0441");
        GROUP_LABELS.put("custom:groups/summer", "\u041b\u0435\u0442\u043e");
        GROUP_LABELS.put("custom:groups/winter", "\u0417\u0438\u043c\u0430");
        GROUP_LABELS.put("custom:groups/phobos", "\u0424\u043e\u0431\u043e\u0441");
        GROUP_LABELS.put("custom:groups/ares", "\u0410\u0440\u0435\u0441");
        GROUP_LABELS.put("custom:groups/aristocrat", "\u0410\u0440\u0438\u0441\u0442\u043e\u043a\u0440\u0430\u0442");
        GROUP_LABELS.put("custom:groups/youtuber", "\u042e\u0442\u0443\u0431\u0435\u0440");
        GROUP_LABELS.put("custom:groups/helper", "\u0425\u0435\u043b\u043f\u0435\u0440");
        GROUP_LABELS.put("custom:groups/shelper", "\u0421\u0442 \u0425\u0435\u043b\u043f\u0435\u0440");
        GROUP_LABELS.put("custom:groups/moder", "\u041c\u043e\u0434\u0435\u0440");
        GROUP_LABELS.put("custom:groups/smoder", "\u0421\u0442 \u041c\u043e\u0434\u0435\u0440");
        GROUP_LABELS.put("custom:groups/admin", "\u0410\u0434\u043c\u0438\u043d");
        GROUP_LABELS.put("custom:groups/default", "\u0418\u0433\u0440\u043e\u043a");
        GROUP_COLORS.put("custom:groups/aristocrat", new ColorRGBA(100.0f, 149.0f, 237.0f).getRGB());
        GROUP_COLORS.put("custom:groups/ares", new ColorRGBA(255.0f, 215.0f, 0.0f).getRGB());
        GROUP_COLORS.put("custom:groups/phobos", new ColorRGBA(255.0f, 165.0f, 0.0f).getRGB());
        GROUP_COLORS.put("custom:groups/kronos", new ColorRGBA(139.0f, 0.0f, 139.0f).getRGB());
        GROUP_COLORS.put("custom:groups/pandar", new ColorRGBA(255.0f, 0.0f, 0.0f).getRGB());
        GROUP_COLORS.put("custom:groups/phoenix", new ColorRGBA(187.0f, 0.0f, 0.0f).getRGB());
        GROUP_COLORS.put("custom:groups/triton", new ColorRGBA(173.0f, 216.0f, 230.0f).getRGB());
        GROUP_COLORS.put("custom:groups/cerberus", new ColorRGBA(0.0f, 255.0f, 0.0f).getRGB());
        GROUP_COLORS.put("custom:groups/hydra", new ColorRGBA(144.0f, 238.0f, 144.0f).getRGB());
        GROUP_COLORS.put("custom:groups/admin", new ColorRGBA(255.0f, 0.0f, 0.0f).getRGB());
        GROUP_COLORS.put("custom:groups/helper", new ColorRGBA(184.0f, 134.0f, 11.0f).getRGB());
        GROUP_COLORS.put("custom:groups/shelper", new ColorRGBA(255.0f, 215.0f, 0.0f).getRGB());
        GROUP_COLORS.put("custom:groups/moder", new ColorRGBA(0.0f, 0.0f, 255.0f).getRGB());
        GROUP_COLORS.put("custom:groups/smoder", new ColorRGBA(65.0f, 105.0f, 225.0f).getRGB());
        GROUP_COLORS.put("custom:groups/heat", new ColorRGBA(255.0f, 69.0f, 0.0f).getRGB());
        GROUP_COLORS.put("custom:groups/cold", new ColorRGBA(135.0f, 206.0f, 250.0f).getRGB());
        GROUP_COLORS.put("custom:groups/summer", new ColorRGBA(255.0f, 215.0f, 0.0f).getRGB());
        GROUP_COLORS.put("custom:groups/winter", new ColorRGBA(240.0f, 248.0f, 255.0f).getRGB());
        GROUP_COLORS.put("custom:groups/youtuber", new ColorRGBA(255.0f, 0.0f, 0.0f).getRGB());
        GROUP_COLORS.put("custom:groups/default", new ColorRGBA(255.0f, 255.0f, 255.0f).getRGB());
        GLYPH_COLORS.put(42240, new ColorRGBA(120.0f, 120.0f, 120.0f).getRGB());
        GLYPH_COLORS.put(42244, new ColorRGBA(100.0f, 113.0f, 251.0f).getRGB());
        GLYPH_COLORS.put(42248, new ColorRGBA(214.0f, 200.0f, 42.0f).getRGB());
        GLYPH_COLORS.put(42258, new ColorRGBA(101.0f, 189.0f, 56.0f).getRGB());
        GLYPH_COLORS.put(42262, new ColorRGBA(64.0f, 151.0f, 214.0f).getRGB());
        GLYPH_COLORS.put(42272, new ColorRGBA(202.0f, 130.0f, 60.0f).getRGB());
        GLYPH_COLORS.put(42276, new ColorRGBA(202.0f, 60.0f, 60.0f).getRGB());
        GLYPH_COLORS.put(42280, new ColorRGBA(245.0f, 51.0f, 238.0f).getRGB());
        GLYPH_COLORS.put(42336, new ColorRGBA(214.0f, 200.0f, 42.0f).getRGB());
        GLYPH_COLORS.put(42290, new ColorRGBA(121.0f, 81.0f, 202.0f).getRGB());
        GLYPH_COLORS.put(42294, new ColorRGBA(202.0f, 130.0f, 60.0f).getRGB());
        GLYPH_COLORS.put(42308, new ColorRGBA(202.0f, 60.0f, 60.0f).getRGB());
        GLYPH_COLORS.put(42326, new ColorRGBA(68.0f, 65.0f, 66.0f).getRGB());
        GLYPH_COLORS.put(42312, new ColorRGBA(127.0f, 214.0f, 86.0f).getRGB());
        GLYPH_COLORS.put(42304, new ColorRGBA(92.0f, 120.0f, 7.0f).getRGB());
        GLYPH_COLORS.put(42322, new ColorRGBA(120.0f, 120.0f, 120.0f).getRGB());
        GLYPH_COLORS.put(42249, new ColorRGBA(214.0f, 200.0f, 42.0f).getRGB());
        GLYPH_COLORS.put(42259, new ColorRGBA(100.0f, 113.0f, 251.0f).getRGB());
        GLYPH_COLORS.put(42263, new ColorRGBA(100.0f, 113.0f, 251.0f).getRGB());
        GLYPH_COLORS.put(42273, new ColorRGBA(121.0f, 81.0f, 202.0f).getRGB());
        GLYPH_COLORS.put(42277, new ColorRGBA(100.0f, 113.0f, 251.0f).getRGB());
        GLYPH_COLORS.put(42281, new ColorRGBA(121.0f, 81.0f, 202.0f).getRGB());
        GLYPH_COLORS.put(42291, new ColorRGBA(64.0f, 151.0f, 214.0f).getRGB());
        GLYPH_COLORS.put(42295, new ColorRGBA(202.0f, 60.0f, 60.0f).getRGB());
        GLYPH_COLORS.put(42241, new ColorRGBA(121.0f, 81.0f, 202.0f).getRGB());
        GLYPH_COLORS.put(42245, new ColorRGBA(255.0f, 255.0f, 255.0f).getRGB());
    }
}
