/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  net.minecraft.Identifier
 *  net.minecraft.ResourceManager
 */
package moscow.rockstar.core.resources;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.resource.ResourceManager;

public class ResourceJsonLoader {
    private static final ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
    private static final Gson jsonCodec = new Gson();

    public static Identifier parseResourceId(String string) {
        return RockstarClient.resourceId("core/" + string);
    }

    public static <T> T loadJsonResource(Identifier class_29602, Class<T> clazz) {
        try {
            return (T)jsonCodec.fromJson(ResourceJsonLoader.readResourceText(class_29602), clazz);
        }
        catch (Exception exception) {
            throw new RuntimeException("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u0440\u0435\u0441\u0443\u0440\u0441 " + String.valueOf(class_29602) + ": " + exception.getMessage(), exception);
        }
    }

    public static String readResourceText(Identifier class_29602) {
        return ResourceJsonLoader.readResourceTextWithSeparator(class_29602, "\n");
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static String readResourceTextWithSeparator(Identifier class_29602, String string) {
        try (InputStream inputStream = resourceManager.open(class_29602);){
            String string2;
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));){
                string2 = bufferedReader.lines().collect(Collectors.joining(string));
            }
            return string2;
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
    }
}
