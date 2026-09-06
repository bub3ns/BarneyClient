/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  lombok.Generated
 */
package moscow.rockstar.render.texture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public class TextureAnimationManifest {
    private final int width;
    private final int height;
    private final int framesPerSecond;
    private final boolean looping;
    private final List<String> frameFiles;

    public static TextureAnimationManifest fromJson(String string) {
        JsonObject jsonObject = JsonParser.parseString((String)string).getAsJsonObject();
        int n = jsonObject.get("width").getAsInt();
        int n2 = jsonObject.get("height").getAsInt();
        int n3 = jsonObject.get("fps").getAsInt();
        boolean bl = jsonObject.get("loop_mode").getAsString().equals("loop");
        ArrayList<String> arrayList = new ArrayList<String>();
        JsonArray jsonArray = jsonObject.getAsJsonArray("frames");
        for (int i = 0; i < jsonArray.size(); ++i) {
            JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
            arrayList.add(jsonObject2.get("file").getAsString());
        }
        return new TextureAnimationManifest(n, n2, n3, bl, arrayList);
    }

    public long getFrameDurationMillis() {
        return 1000L / (long)this.framesPerSecond;
    }

    public int getFrameCount() {
        return this.frameFiles.size();
    }

    @Generated
    public int getWidth() {
        return this.width;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public int getFramesPerSecond() {
        return this.framesPerSecond;
    }

    @Generated
    public boolean isLooping() {
        return this.looping;
    }

    @Generated
    public List<String> getFrameFiles() {
        return this.frameFiles;
    }

    @Generated
    public TextureAnimationManifest(int n, int n2, int n3, boolean bl, List<String> list) {
        this.width = n;
        this.height = n2;
        this.framesPerSecond = n3;
        this.looping = bl;
        this.frameFiles = list;
    }
}

