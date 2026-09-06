package moscow.rockstar.render.text.icon;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.core.resources.ResourceJsonLoader;
import net.minecraft.util.Identifier;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

/** The shipped 64x64 emoji sprite sheet. */
public final class EmojiAtlas {
    private static final Identifier ATLAS = RockstarClient.resourceId("textures/emoji/emoji_atlas.png");
    private static Map<String, Integer> index;

    private EmojiAtlas() {
    }

    public static Integer getIndex(String key) {
        if (index == null) {
            JsonObject json = ResourceJsonLoader.loadJsonResource(RockstarClient.resourceId("emoji/emoji_atlas.json"), JsonObject.class);
            index = new HashMap<>(json.size());
            for (String name : json.keySet()) {
                index.put(name, json.get(name).getAsInt());
            }
        }
        return index.get(key);
    }

    public static void draw(CustomDrawContext context, int atlasIndex, float x, float y, float size, float alpha) {
        float tile = 0.015625f;
        float u = (float)(atlasIndex % 64) * tile;
        float v = (float)(atlasIndex / 64) * tile;
        context.drawTexture(ATLAS, x, y, size, size, u, u + tile, v, v + tile, ColorRGBA.WHITE.withAlpha(alpha));
    }
}
