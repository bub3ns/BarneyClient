/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.NativeImageBackedTexture
 *  net.minecraft.AbstractTexture
 *  net.minecraft.Identifier
 */
package moscow.rockstar.ui.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.render.text.icon.EmojiAtlas;
import moscow.rockstar.render.texture.ImageTextureConverter;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.render.text.FontRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

public final class EmojiTextNode
extends UiNode {
    private static final Pattern GRAPHEME_PATTERN = Pattern.compile("\\X");
    private static final Map<String, GlyphTexture> GLYPH_CACHE = new HashMap<String, GlyphTexture>();
    private static final GlyphTexture peaceHandGlyph = new GlyphTexture(RockstarClient.resourceId("textures/emoji/peace.png"));
    private final FontMetrics fontMetrics;
    private final Supplier<String> textSupplier;
    private final Supplier<ColorRGBA> colorSupplier;
    private final UiNode.SignalValueProvider offsetProvider;

    public EmojiTextNode(FontMetrics fontMetrics, Supplier<String> supplier, Supplier<ColorRGBA> supplier2) {
        this(fontMetrics, supplier, supplier2, () -> 0.0f);
    }

    public EmojiTextNode(FontMetrics fontMetrics, Supplier<String> supplier, Supplier<ColorRGBA> supplier2, UiNode.SignalValueProvider signalValueProvider) {
        this.fontMetrics = fontMetrics;
        this.textSupplier = supplier;
        this.colorSupplier = supplier2;
        this.offsetProvider = signalValueProvider;
        this.interactive(false);
    }

    @Override
    protected void measure() {
        String string = this.getText();
        if (!this.explicitW) {
            this.prefW = EmojiTextNode.measureTextWidth(this.fontMetrics, string);
        }
        if (!this.explicitH) {
            this.prefH = Math.max(this.fontMetrics.getFontTopOffset(), EmojiTextNode.getEmojiGlyphSize(this.fontMetrics));
        }
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        String string = this.getText();
        if (string.isEmpty()) {
            return;
        }
        EmojiTextNode.drawTextWithEmoji(drawContext, this.fontMetrics, string, this.x() + this.offsetProvider.getValue(), this.y(), this.h(), this.colorSupplier == null ? ColorRGBA.WHITE : this.colorSupplier.get());
    }

    public static float measureTextWidth(FontMetrics fontMetrics, String string) {
        if (string == null || string.isEmpty()) {
            return 0.0f;
        }
        Matcher matcher = GRAPHEME_PATTERN.matcher(string);
        float f = 0.0f;
        while (matcher.find()) {
            String string2 = matcher.group();
            f += EmojiTextNode.isEmojiCluster(string2) ? EmojiTextNode.getEmojiGlyphSize(fontMetrics) + FontRenderer.getLetterSpacing(fontMetrics.getFontScale()) : fontMetrics.measureText(string2);
        }
        return f;
    }

    public static boolean containsEmoji(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        Matcher matcher = GRAPHEME_PATTERN.matcher(string);
        while (matcher.find()) {
            if (!EmojiTextNode.isEmojiCluster(matcher.group())) continue;
            return true;
        }
        return false;
    }

    public static void drawTextWithEmoji(CustomDrawContext customDrawContext, FontMetrics fontMetrics, String string, float f, float f2, float f3, ColorRGBA colorRGBA) {
        if (string == null || string.isEmpty() || colorRGBA == null) {
            return;
        }
        Matcher matcher = GRAPHEME_PATTERN.matcher(string);
        float f4 = f;
        while (matcher.find()) {
            String string2 = matcher.group();
            if (EmojiTextNode.isEmojiCluster(string2)) {
                float f5 = EmojiTextNode.getEmojiGlyphSize(fontMetrics);
                EmojiTextNode.getOrCreateGlyphTexture(string2).draw(customDrawContext, f4, f2 + (f3 - f5) / 2.0f, f5, colorRGBA.getAlpha());
                f4 += f5 + FontRenderer.getLetterSpacing(fontMetrics.getFontScale());
                continue;
            }
            customDrawContext.drawText(fontMetrics, string2, f4, f2 + (f3 - fontMetrics.getFontTopOffset()) / 2.0f, colorRGBA);
            f4 += fontMetrics.measureText(string2);
        }
    }

    private static float getEmojiGlyphSize(FontMetrics fontMetrics) {
        return fontMetrics.getFontScale() * 1.4f;
    }

    private String getText() {
        String string = this.textSupplier == null ? "" : this.textSupplier.get();
        return string == null ? "" : string;
    }

    private static boolean isEmojiCluster(String string) {
        return string.codePoints().anyMatch(n -> n == 169 || n == 174 || n == 8252 || n == 8265 || n == 8482 || n == 8505 || n == 12336 || n == 12349 || n == 12951 || n == 12953 || n >= 8960 && n <= 9215 || n >= 9728 && n <= 10175 || n >= 11008 && n <= 11263 || n >= 126976 && n <= 129791);
    }

    private static GlyphTexture getOrCreateGlyphTexture(String string) {
        if (string.equals("\u270c") || string.equals("\u270c\ufe0f")) {
            return peaceHandGlyph;
        }
        GlyphTexture glyphTexture = GLYPH_CACHE.get(string);
        if (glyphTexture != null) {
            return glyphTexture;
        }
        Integer atlasIndex = EmojiAtlas.getIndex(EmojiTextNode.buildCodePointKey(string));
        if (atlasIndex != null) {
            glyphTexture = GlyphTexture.ofAtlasIndex(atlasIndex);
            GLYPH_CACHE.put(string, glyphTexture);
            return glyphTexture;
        }
        BufferedImage bufferedImage = new BufferedImage(96, 96, 2);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        Font font = EmojiTextNode.findEmojiFont(string);
        graphics2D.setFont(font);
        graphics2D.setPaint(EmojiTextNode.createEmojiGradient(string));
        java.awt.FontMetrics fontMetrics = graphics2D.getFontMetrics(font);
        int n2 = fontMetrics.stringWidth(string);
        graphics2D.drawString(string, (float)(96 - n2) / 2.0f, (float)(96 - fontMetrics.getHeight()) / 2.0f + (float)fontMetrics.getAscent());
        graphics2D.dispose();
        Identifier class_29602 = Identifier.of((String)RockstarClient.RESOURCE_NAMESPACE, (String)("dynamic_island/emoji/" + String.valueOf(UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8)))));
        MinecraftClient.getInstance().getTextureManager().registerTexture(class_29602, (AbstractTexture)new NativeImageBackedTexture(ImageTextureConverter.fromBufferedImage(bufferedImage)));
        glyphTexture = new GlyphTexture(class_29602);
        GLYPH_CACHE.put(string, glyphTexture);
        return glyphTexture;
    }

    private static String buildCodePointKey(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        string.codePoints().filter(n -> n != 65038 && n != 65039).forEach(n -> {
            if (!stringBuilder.isEmpty()) {
                stringBuilder.append('-');
            }
            stringBuilder.append(Integer.toHexString(n));
        });
        return stringBuilder.toString();
    }

    private static Font findEmojiFont(String string) {
        for (String string2 : new String[]{"Apple Color Emoji", "Segoe UI Emoji", "Noto Color Emoji"}) {
            Font font = new Font(string2, 0, 76);
            if (!font.canDisplay(string.codePointAt(0))) continue;
            return font;
        }
        return new Font("Dialog", 0, 76);
    }

    private static GradientPaint createEmojiGradient(String string) {
        boolean bl = string.codePoints().anyMatch(n -> n == 10084 || n >= 128147 && n <= 128159);
        return bl ? new GradientPaint(12.0f, 12.0f, new Color(255, 115, 125), 84.0f, 84.0f, new Color(221, 38, 63)) : new GradientPaint(12.0f, 12.0f, new Color(255, 235, 112), 84.0f, 84.0f, new Color(255, 157, 30));
    }

    static final class GlyphTexture {
        private final Identifier textureId;
        private final int atlasIndex;

        GlyphTexture(Identifier class_29602) {
            this(class_29602, -1);
        }

        private GlyphTexture(Identifier class_29602, int n) {
            this.textureId = class_29602;
            this.atlasIndex = n;
        }

        static GlyphTexture ofAtlasIndex(int n) {
            return new GlyphTexture(null, n);
        }

        void draw(CustomDrawContext customDrawContext, float f, float f2, float f3, float f4) {
            if (this.atlasIndex >= 0) {
                EmojiAtlas.draw(customDrawContext, this.atlasIndex, f, f2, f3, f4);
            } else {
                customDrawContext.drawTexture(this.textureId, f, f2, f3, f3, ColorRGBA.WHITE.withAlpha(f4));
            }
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "textureId", "atlasIndex");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "textureId", "atlasIndex");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "textureId", "atlasIndex");
        }

        public Identifier getTextureId() {
            return this.textureId;
        }

        public int getAtlasIndex() {
            return this.atlasIndex;
        }

    }
}
