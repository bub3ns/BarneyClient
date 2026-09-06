package moscow.rockstar.api.commands;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.texture.ImageTextureConverter;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import pyrock.events.render.PreHudRenderEvent;

/**
 * ORIGINAL: {@code rockstar/ilIlil/IIiIiIII}, slot 1 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 *
 * <p>The HTTP fetch and the JSON scrape are inlined from
 * {@code rockstar/ilIlil/iIIIIiiII#I(String)} and {@code #i(String)}; the remap kept only that
 * class's third method, as {@link ImageTextureConverter#fromBufferedImage}.  The dropped
 * {@code boolean} parameter of the original converter is unused there - it always builds the
 * native image with {@code useStb = true} - so the call is equivalent.</p>
 */
public class CatCommandService implements ClientAccess {
    private static final String CAT_API_URL = "https://api.thecatapi.com/v1/images/search";
    // NOTE: the backslashes must be doubled. Since Java 15 a bare \s inside a STRING literal is
    // a legal escape meaning a literal space, so "\s*" compiles silently but yields the regex
    // ` *` (space-star) instead of the whitespace class the original ldc carries.
    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");

    private final Animation fadeAnimation = new Animation(1000L, Easing.easeInOutCubicPolynomial);
    private boolean fadingOut = false;
    private Identifier textureId = null;
    private boolean loading = false;
    private final EventListener<PreHudRenderEvent> preHudRenderListener = preHudRenderEvent -> {
        if (this.textureId == null) {
            return;
        }
        if ((double) this.fadeAnimation.getValue() == 1.0 && !this.fadingOut) {
            this.fadingOut = true;
        }
        this.fadeAnimation.update(this.fadingOut ? 0.0f : 1.0f);
        if (this.fadeAnimation.getValue() == 0.0f && this.fadingOut) {
            return;
        }
        float size = 200.0f;
        float x = ((float) minecraftClient.getWindow().getScaledWidth() - size) / 2.0f;
        float y = ((float) minecraftClient.getWindow().getScaledHeight() - size) / 2.0f;
        preHudRenderEvent.getContext().drawTexture(this.textureId, x, y, size, size,
            ColorPalette.WHITE.withAlpha(255.0f * this.fadeAnimation.getValue()));
    };

    public CatCommandService() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("cat")
            .aliases("kitty")
            .description("commands.cat.description")
            .handler(dispatchContext -> this.requestCat())
            .build();
    }

    private void requestCat() {
        if (this.loading) {
            return;
        }
        this.loading = true;
        CompletableFuture.supplyAsync(() -> {
            try {
                String body = CatCommandService.fetchUrl(CAT_API_URL);
                String imageUrl = CatCommandService.extractImageUrl(body);
                if (imageUrl == null) {
                    return null;
                }
                BufferedImage image = ImageIO.read(URI.create(imageUrl).toURL());
                if (image == null) {
                    return null;
                }
                return ImageTextureConverter.fromBufferedImage(image);
            } catch (IOException exception) {
                return null;
            }
        }).thenAccept(nativeImage -> minecraftClient.execute(() -> {
            if (nativeImage != null) {
                if (this.textureId != null) {
                    minecraftClient.getTextureManager().destroyTexture(this.textureId);
                }
                Identifier identifier = RockstarClient.resourceId("temp/cat/" + String.valueOf(UUID.randomUUID()));
                minecraftClient.getTextureManager().registerTexture(identifier,
                    (AbstractTexture) new NativeImageBackedTexture(nativeImage));
                this.textureId = identifier;
                this.fadeAnimation.update(1.0f);
                this.fadingOut = false;
            }
            this.loading = false;
        }));
    }

    /** ORIGINAL: {@code rockstar/ilIlil/iIIIIiiII#I (Ljava/lang/String;)Ljava/lang/String;}. */
    private static String fetchUrl(String address) throws IOException {
        URL url = new URL(address);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        try (InputStream stream = connection.getInputStream()) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** ORIGINAL: {@code rockstar/ilIlil/iIIIIiiII#i (Ljava/lang/String;)Ljava/lang/String;}. */
    private static String extractImageUrl(String json) {
        Matcher matcher = IMAGE_URL_PATTERN.matcher(json);
        if (matcher.find()) {
            // The JSON body escapes forward slashes as \/ ; in Java source that literal
            // backslash must itself be escaped. "\/" alone is not a legal Java escape.
            return matcher.group(1).replace("\\/", "/");
        }
        return null;
    }
}
