package moscow.rockstar.render.texture;

import java.awt.image.BufferedImage;
import net.minecraft.client.texture.NativeImage;

/** Converts Java2D images into the native image format used by Minecraft textures. */
public final class ImageTextureConverter {
    private ImageTextureConverter() {
    }

    public static NativeImage fromBufferedImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        NativeImage nativeImage = new NativeImage(width, height, true);
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        for (int index = 0; index < pixels.length; index++) {
            nativeImage.setColorArgb(index % width, index / width, pixels[index]);
        }
        return nativeImage;
    }
}
