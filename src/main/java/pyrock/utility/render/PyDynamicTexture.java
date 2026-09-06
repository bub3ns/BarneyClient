/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.NativeImage
 *  net.minecraft.NativeImage$Format
 *  net.minecraft.NativeImageBackedTexture
 *  net.minecraft.AbstractTexture
 *  net.minecraft.Identifier
 *  org.lwjgl.system.MemoryUtil
 */
package pyrock.utility.render;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.mixin.accessors.NativeImageAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryUtil;

public final class PyDynamicTexture
implements AutoCloseable {
    private static final AtomicInteger IDS = new AtomicInteger();
    private final int width;
    private final int height;
    private final NativeImage image;
    private final NativeImageBackedTexture texture;
    private final Identifier identifier;
    private final ByteBuffer pixels;
    private boolean closed;

    public PyDynamicTexture(String string, int n, int n2) {
        if (n <= 0 || n2 <= 0 || (long)n * (long)n2 > 0x1000000L) {
            throw new IllegalArgumentException("invalid dynamic texture size: " + n + "x" + n2);
        }
        this.width = n;
        this.height = n2;
        this.image = new NativeImage(NativeImage.Format.RGBA, n, n2, false);
        this.texture = new NativeImageBackedTexture(this.image);
        this.texture.setFilter(false, false);
        this.identifier = RockstarClient.resourceId("scripts/dynamic/" + PyDynamicTexture.sanitize(string) + "_" + IDS.incrementAndGet());
        this.pixels = MemoryUtil.memByteBuffer(((NativeImageAccessor)(Object)this.image).getPointer(), n * n2 * 4);
        MinecraftClient.getInstance().getTextureManager().registerTexture(this.identifier, (AbstractTexture)this.texture);
    }

    public Identifier identifier() {
        return this.identifier;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public void update(Object object) {
        if (this.closed) {
            throw new IllegalStateException("dynamic texture is closed");
        }
        if (!(object instanceof byte[])) {
            throw new IllegalArgumentException("dynamic texture pixels must be bytes");
        }
        byte[] byArray = (byte[])object;
        if (byArray.length != this.pixels.capacity()) {
            throw new IllegalArgumentException("expected " + this.pixels.capacity() + " RGBA bytes, got " + byArray.length);
        }
        this.pixels.clear();
        this.pixels.put(byArray);
        this.pixels.clear();
        this.texture.upload();
    }

    public void restoreEnabledFlag() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        MinecraftClient.getInstance().getTextureManager().destroyTexture(this.identifier);
    }

    @Override
    public void close() {
        this.restoreEnabledFlag();
    }

    private static String sanitize(String string) {
        String string2 = string == null ? "texture" : string.toLowerCase().replaceAll("[^a-z0-9/._-]", "_").replaceAll("_+", "_").replaceAll("^_+|_+$", "");
        return string2.isBlank() ? "texture" : string2;
    }
}
