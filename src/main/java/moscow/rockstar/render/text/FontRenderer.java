package moscow.rockstar.render.text;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.text.glyph.Glyph;
import moscow.rockstar.render.text.glyph.GlyphAtlas;
import moscow.rockstar.render.text.glyph.GlyphOutline;
import moscow.rockstar.render.text.glyph.GlyphSpatialIndex;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

/** Loads a vector font, builds Slug glyph data, and draws/measures text. */
public final class FontRenderer {
    private static final float LETTER_SPACING = 0.025f;
    private static final float FONT_RESOLUTION = 2048.0f;
    private static final int WIDTH_CACHE_LIMIT = 4096;
    private static final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, false, true);
    private static volatile FontRenderer fallbackFont;

    private final String name;
    private final Identifier resource;
    private final Map<Integer, Glyph> glyphs = new ConcurrentHashMap<>();
    private final Map<WidthKey, Float> widthCache = new ConcurrentHashMap<>();
    private final Map<Integer, FontMetrics> metricsCache = new ConcurrentHashMap<>();

    private Font awtFont;
    private float baseline = 0.7f;
    private float ascent = 0.75f;
    private float descent = -0.25f;
    private float lineHeight = 1.0f;

    private FontRenderer(String name, Identifier resource) {
        this.name = name;
        this.resource = resource;
    }

    public static float getLetterSpacing(float scale) {
        return LETTER_SPACING * scale;
    }

    public static FontRenderer getFallbackFont() {
        FontRenderer existing = fallbackFont;
        if (existing != null) {
            return existing;
        }
        synchronized (FontRenderer.class) {
            if (fallbackFont == null) {
                FontRenderer created = new FontRenderer("fallback", null);
                created.awtFont = findSystemFont();
                created.updateMetrics();
                fallbackFont = created;
            }
            return fallbackFont;
        }
    }

    private static Font findSystemFont() {
        for (String family : new String[]{"Segoe UI", "Helvetica Neue", "DejaVu Sans", "Arial", "SansSerif"}) {
            Font candidate = new Font(family, Font.PLAIN, 1).deriveFont(FONT_RESOLUTION);
            if (candidate.canDisplay('\u1D00') || family.equals("SansSerif")) {
                return candidate;
            }
        }
        return new Font("SansSerif", Font.PLAIN, 1).deriveFont(FONT_RESOLUTION);
    }

    public static FontRenderer fromResource(String name, String resourceName) {
        return new FontRenderer(name, RockstarClient.resourceId("fonts/" + resourceName + ".otf"));
    }

    public static FontRenderer empty(String name) {
        return new FontRenderer(name, null);
    }

    public static FontRenderer fromAwtFont(String name, Font font) {
        FontRenderer renderer = new FontRenderer(name, null);
        renderer.awtFont = normalizeFont(font);
        renderer.updateMetrics();
        return renderer;
    }

    public FontRenderer withAwtFont(Font font) {
        this.setAwtFont(font);
        return this;
    }

    /** Clears glyphs registered from a resource-backed vector collection. */
    public void clearGlyphs() {
        this.glyphs.clear();
        this.widthCache.clear();
        this.metricsCache.clear();
    }

    public synchronized void reload() {
        this.glyphs.clear();
        this.widthCache.clear();
        this.metricsCache.clear();
        if (this.resource == null) {
            return;
        }
        try (InputStream input = MinecraftClient.getInstance().getResourceManager().open(this.resource)) {
            this.awtFont = Font.createFont(Font.TRUETYPE_FONT, input).deriveFont(FONT_RESOLUTION);
        } catch (Exception exception) {
            RockstarClient.LOGGER.error("Unable to load font {}", this.resource, exception);
            this.awtFont = getFallbackFont().awtFont;
        }
        this.updateMetrics();
    }

    public synchronized void setAwtFont(Font font) {
        this.glyphs.clear();
        this.widthCache.clear();
        this.metricsCache.clear();
        this.awtFont = normalizeFont(font);
        this.updateMetrics();
    }

    private static Font normalizeFont(Font font) {
        return font.getSize2D() == FONT_RESOLUTION ? font : font.deriveFont(FONT_RESOLUTION);
    }

    private void ensureFontLoaded() {
        if (this.awtFont == null && this.resource != null) {
            this.reload();
        }
    }

    private void updateMetrics() {
        if (this.awtFont == null) {
            return;
        }
        LineMetrics lineMetrics = this.awtFont.getLineMetrics("Hg", RENDER_CONTEXT);
        this.ascent = lineMetrics.getAscent() / FONT_RESOLUTION;
        this.descent = -lineMetrics.getDescent() / FONT_RESOLUTION;
        this.lineHeight = lineMetrics.getHeight() / FONT_RESOLUTION;
        Rectangle2D bounds = this.awtFont.createGlyphVector(RENDER_CONTEXT, "H").getOutline().getBounds2D();
        this.baseline = bounds.getHeight() > 0.0 ? (float)(-bounds.getMinY() / FONT_RESOLUTION) : Math.max(0.1f, this.ascent * 0.72f);
    }

    public Glyph getGlyph(int codePoint) {
        Glyph cached = this.glyphs.get(codePoint);
        if (cached != null) {
            return cached;
        }
        this.ensureFontLoaded();
        if (this.awtFont == null || !this.awtFont.canDisplay(codePoint)) {
            return null;
        }
        GlyphOutline outline = GlyphOutline.fromGlyph(this.awtFont, RENDER_CONTEXT, codePoint);
        int atlasIndex = outline.isEmpty() ? -1 : GlyphAtlas.getInstance().register(GlyphSpatialIndex.fromGlyphOutline(outline));
        Glyph created = new Glyph(codePoint, atlasIndex, outline.advanceWidth, outline.minX, outline.minY, outline.maxX, outline.maxY);
        Glyph previous = this.glyphs.putIfAbsent(codePoint, created);
        return previous != null ? previous : created;
    }

    /** Registers a glyph at an explicit code point, as used by the SVG icon font. */
    public Glyph registerGlyph(int codePoint, GlyphOutline outline) {
        if (outline == null) {
            return null;
        }
        int atlasIndex = outline.isEmpty() ? -1
            : GlyphAtlas.getInstance().register(GlyphSpatialIndex.fromGlyphOutline(outline));
        Glyph glyph = new Glyph(codePoint, atlasIndex, outline.advanceWidth,
            outline.minX, outline.minY, outline.maxX, outline.maxY);
        this.glyphs.put(codePoint, glyph);
        return glyph;
    }

    public Glyph getGlyphOrFallback(int codePoint) {
        Glyph glyph = this.getGlyph(codePoint);
        if (glyph != null) {
            return glyph;
        }
        FontRenderer fallback = getFallbackFont();
        return fallback == this ? null : fallback.getGlyph(codePoint);
    }

    public boolean canDisplay(char character) {
        return this.getGlyphOrFallback(character) != null;
    }

    public boolean canDisplay(int codePoint) {
        return this.getGlyphOrFallback(codePoint) != null;
    }

    public boolean drawGlyph(Matrix4f matrix, VertexConsumer vertices, int codePoint, float x, float topY, float scale, int color) {
        Glyph glyph = this.getGlyphOrFallback(codePoint);
        if (glyph == null) {
            return false;
        }
        glyph.appendUiQuad(matrix, vertices, x, topY, scale, color);
        return true;
    }

    public void warmUp(String text) {
        for (int index = 0; index < text.length(); index++) {
            this.getGlyphOrFallback(text.charAt(index));
        }
    }

    public float getBaseline() {
        this.ensureFontLoaded();
        return this.baseline;
    }

    public float getAscent() {
        this.ensureFontLoaded();
        return this.ascent;
    }

    public float getDescent() {
        this.ensureFontLoaded();
        return this.descent;
    }

    public float getLineHeight() {
        this.ensureFontLoaded();
        return this.lineHeight;
    }

    public float getBaselineOffset(float scale) {
        return this.getBaseline() * scale;
    }

    public float getLineHeight(float scale) {
        return this.getLineHeight() * scale;
    }

    public float offsetBaseline(float y, float scale) {
        return y + this.getBaselineOffset(scale);
    }

    public float measure(String text, float scale) {
        if (text == null || text.isEmpty()) {
            return 0.0f;
        }
        WidthKey key = new WidthKey(text, scale);
        Float cached = this.widthCache.get(key);
        if (cached != null) {
            return cached;
        }
        float width = layout(text, scale, 0.0f, null);
        if (this.widthCache.size() >= WIDTH_CACHE_LIMIT) {
            this.widthCache.clear();
        }
        this.widthCache.put(key, width);
        return width;
    }

    public float measure(Text text, float scale) {
        return text == null ? 0.0f : this.measure(text.getString(), scale);
    }

    public float measureCharacter(char character, float scale) {
        Glyph glyph = this.getGlyphOrFallback(character);
        return glyph == null ? 0.0f : glyph.getAdvanceWidth() * scale + getLetterSpacing(scale);
    }

    public FontMetrics metrics(float scale) {
        return this.metricsCache.computeIfAbsent(Float.floatToIntBits(scale), ignored -> new FontMetrics(this, scale));
    }

    public void draw(Matrix4f matrix, VertexConsumer vertices, String text, float scale, float x, float y, float z, int color) {
        this.draw(matrix, vertices, text, scale, x, y, z, color, 1.0f);
    }

    public void draw(Matrix4f matrix, VertexConsumer vertices, String text, float scale, float x, float y, float z, int color, float edgePadding) {
        float baselineY = this.offsetBaseline(y, scale);
        layout(text, scale, x, (glyph, glyphX) -> glyph.appendQuad(matrix, vertices, scale, glyphX, baselineY, z, color, edgePadding));
    }

    public void drawWithCallback(String text, float scale, float x, float y, float z, int color, GlyphConsumer consumer) {
        float baselineY = this.offsetBaseline(y, scale);
        layout(text, scale, x, (glyph, glyphX) -> consumer.accept(glyph, glyphX, baselineY, z, color));
    }

    /** Emits glyph bounds to the streaming UI renderer without changing the glyph data. */
    public void drawToSink(String text, float scale, float x, float y, float z, int color, GlyphRenderSink sink) {
        if (sink == null) {
            return;
        }
        float baselineY = this.offsetBaseline(y, scale);
        layout(text, scale, x, (glyph, glyphX) -> sink.renderGlyph(glyph, scale, glyphX, baselineY, z, color));
    }

    /** Visits each glyph with its source character and laid-out position. */
    public void visitGlyphs(String text, float scale, float x, float y, FontRenderer fallback,
                            GlyphVisitConsumer consumer) {
        float baselineY = this.offsetBaseline(y, scale);
        float spacing = getLetterSpacing(scale);
        float cursor = x;
        boolean formattingCode = false;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (formattingCode) {
                formattingCode = false;
                continue;
            }
            if (character == '\u00a7') {
                formattingCode = true;
                continue;
            }
            Glyph glyph = this.getGlyph(character);
            FontRenderer source = this;
            if (glyph == null) {
                source = getFallbackFont();
                glyph = source.getGlyph(character);
            }
            consumer.accept(source, glyph, character, cursor, baselineY);
            if (glyph == null) {
                continue;
            }
            cursor += glyph.getAdvanceWidth() * scale + spacing;
        }
    }

    private float layout(String text, float scale, float startX, GlyphPlacementConsumer consumer) {
        float spacing = getLetterSpacing(scale);
        float cursor = startX;
        boolean formattingCode = false;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (formattingCode) {
                formattingCode = false;
                continue;
            }
            if (character == '\u00a7') {
                formattingCode = true;
                continue;
            }
            Glyph glyph = this.getGlyphOrFallback(character);
            if (glyph == null) {
                continue;
            }
            if (consumer != null) {
                consumer.accept(glyph, cursor);
            }
            cursor += glyph.getAdvanceWidth() * scale + spacing;
        }
        return cursor;
    }

    public void clearCaches() {
        this.widthCache.clear();
    }

    public String getName() {
        return this.name;
    }

    @FunctionalInterface
    public interface GlyphPlacementConsumer {
        void accept(Glyph glyph, float x);
    }

    @FunctionalInterface
    public interface GlyphConsumer {
        void accept(Glyph glyph, float x, float baselineY, float z, int color);
    }

    @FunctionalInterface
    public interface GlyphRenderSink {
        void renderGlyph(Glyph glyph, float scale, float x, float baselineY, float z, int color);
    }

    @FunctionalInterface
    public interface GlyphVisitConsumer {
        void accept(FontRenderer font, Glyph glyph, char character, float x, float y);
    }

    private record WidthKey(String text, float scale) {
    }
}
