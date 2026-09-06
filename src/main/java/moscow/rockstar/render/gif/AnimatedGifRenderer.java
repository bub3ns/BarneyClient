package moscow.rockstar.render.gif;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.BoundedRenderer;
import moscow.rockstar.render.core.RockstarDrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

/** Renders and advances an animated GIF as a Minecraft dynamic texture. */
public final class AnimatedGifRenderer extends BoundedRenderer implements ClientAccess {
    private final GifDecoder decoder;
    private final int frameCount;
    private final Map<Integer, Integer> frameDelays = new HashMap<>();
    private final NativeImage image;
    private final NativeImageBackedTexture texture;
    private final Identifier textureId;
    private int frameIndex;
    private long lastFrameTimeMillis;
    private float opacity = 1.0f;
    private boolean cleanedUp;

    public AnimatedGifRenderer(Identifier resourceId, float x, float y, float width, float height) {
        super(x, y, width, height);
        decoder = new GifDecoder();

        try {
            Resource resource = minecraftClient.getResourceManager().getResourceOrThrow(resourceId);
            try (InputStream input = resource.getInputStream()) {
            int status = decoder.read(input);
            if (status != GifDecoder.STATUS_OK) {
                throw new IllegalStateException("Unable to decode GIF " + resourceId + " (status " + status + ")");
            }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read GIF " + resourceId, exception);
        }

        frameCount = decoder.getFrameCount();
        if (frameCount == 0) {
            throw new IllegalStateException("GIF has no frames: " + resourceId);
        }
        for (int index = 0; index < frameCount; index++) {
            frameDelays.put(index, decoder.getDelay(index));
        }

        BufferedImage firstFrame = decoder.getFrame(0);
        if (firstFrame == null || firstFrame.getWidth() <= 0 || firstFrame.getHeight() <= 0) {
            throw new IllegalStateException("GIF has an invalid first frame: " + resourceId);
        }
        image = new NativeImage(firstFrame.getWidth(), firstFrame.getHeight(), false);
        texture = new NativeImageBackedTexture(image);
        textureId = RockstarClient.resourceId("gif_texture_" + resourceId.getPath().hashCode());
        minecraftClient.getTextureManager().registerTexture(textureId, texture);
        uploadFrame(0);
    }

    @Override
    public void update(RockstarDrawContext context) {
        if (frameCount <= 1 || cleanedUp) {
            return;
        }

        long now = System.currentTimeMillis();
        int delay = Math.max(1, frameDelays.getOrDefault(frameIndex, 100));
        if (now - lastFrameTimeMillis >= delay) {
            lastFrameTimeMillis = now;
            frameIndex = (frameIndex + 1) % frameCount;
            uploadFrame(frameIndex);
        }
    }

    @Override
    protected void draw(RockstarDrawContext context) {
        context.drawTexture(textureId, x, y, width, height, ColorPalette.WHITE.mulAlpha(opacity));
    }

    private void uploadFrame(int index) {
        BufferedImage frame = decoder.getFrame(index);
        if (frame == null) {
            throw new IllegalStateException("GIF frame is unavailable: " + index);
        }
        int frameWidth = Math.min(frame.getWidth(), image.getWidth());
        int frameHeight = Math.min(frame.getHeight(), image.getHeight());
        for (int row = 0; row < frameHeight; row++) {
            for (int column = 0; column < frameWidth; column++) {
                image.setColorArgb(column, row, frame.getRGB(column, row));
            }
        }
        texture.upload();
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.max(0.0f, Math.min(1.0f, opacity));
    }

    public float getOpacity() {
        return opacity;
    }

    public void cleanup() {
        if (cleanedUp) {
            return;
        }
        cleanedUp = true;
        minecraftClient.getTextureManager().destroyTexture(textureId);
        texture.close();
    }

    @Override
    public void dispose() {
        cleanup();
    }
}
