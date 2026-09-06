package moscow.rockstar.render.text.icon;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.text.FontRenderer;
import moscow.rockstar.render.text.glyph.GlyphOutline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/** Loads the original vector icon font from {@code assets/rockstar/icons/font}. */
public final class SvgIconRegistry {
    private static final int FIRST_CODE_POINT = 0xE000;
    private static final String ICON_DIRECTORY = "icons/font";
    private static final FontRenderer ICON_FONT = FontRenderer.empty("icons");
    private static volatile Map<String, Integer> iconCodePoints = Collections.emptyMap();

    private SvgIconRegistry() {
    }

    public static FontRenderer getFont() {
        return ICON_FONT;
    }

    public static boolean isLoaded() {
        return !iconCodePoints.isEmpty();
    }

    public static Integer getCodePoint(String name) {
        return name == null ? null : iconCodePoints.get(name);
    }

    /** Original IIiiII#I(Ljava/lang/String;)Z - ICONS.containsKey(name). */
    public static boolean hasIcon(String name) {
        return iconCodePoints.containsKey(name);
    }

    /** Original IIiiII#I()Ljava/util/Set; - the icon-name set (ICONS.keySet()). */
    public static Set<String> getIconNames() {
        return iconCodePoints.keySet();
    }

    /** Rebuilds the icon font in the same sorted resource order as the original client. */
    public static synchronized void reload() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            ResourceManager resourceManager = client.getResourceManager();
            Map<Identifier, ?> resources = resourceManager.findResources(
                ICON_DIRECTORY, identifier -> identifier.getPath().endsWith(".svg"));
            TreeMap<String, Identifier> sortedResources = new TreeMap<>();
            resources.forEach((identifier, resource) -> {
                if (!identifier.getNamespace().equals(RockstarClient.RESOURCE_NAMESPACE)) {
                    return;
                }
                String path = identifier.getPath();
                sortedResources.put(path.substring(ICON_DIRECTORY.length() + 1, path.length() - 4), identifier);
            });

            Map<String, Integer> loaded = new HashMap<>();
            int codePoint = FIRST_CODE_POINT;
            for (Map.Entry<String, Identifier> entry : sortedResources.entrySet()) {
                try (InputStream input = resourceManager.open(entry.getValue())) {
                    String svg = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                    GlyphOutline outline = SvgPathParser.parse(svg);
                    if (outline == null) {
                        continue;
                    }
                    ICON_FONT.registerGlyph(codePoint, outline);
                    loaded.put(entry.getKey(), codePoint++);
                } catch (Exception exception) {
                    RockstarClient.LOGGER.warn("Unable to parse icon {}: {}", entry.getValue(), exception.toString());
                }
            }
            iconCodePoints = Map.copyOf(loaded);
            RockstarClient.LOGGER.info("Loaded {} vector icons", loaded.size());
        } catch (Throwable throwable) {
            RockstarClient.LOGGER.error("Unable to load vector icons: {}", throwable.toString());
        }
    }
}
