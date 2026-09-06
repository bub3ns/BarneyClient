/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.ui.wallpaper;

import java.util.ArrayList;
import java.util.Iterator;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.render.assets.AssetImageLoader;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.util.math.MathHelper;
import pyrock.utility.render.ColorRGBA;

public class WallpaperCarouselController
implements ClientAccess {
    private static final String[] WALLPAPER_ASSETS = new String[]{"image/mainmenu/barney", "image/mainmenu/golabek"};
    private static final String[] WALLPAPER_KEYS = new String[]{"mainmenu.wallpaper.barney", "mainmenu.wallpaper.golabek"};
    private boolean transitionActive;
    private float transitionProgress;
    private int currentWallpaperIndex;
    private float displayedIndex;
    private int targetIndex;
    private float wallpaperAspectRatio = 1.7777778f;
    private boolean dragging;
    private boolean snapping;
    private double dragStartX;
    private double dragStartY;
    private double dragStartIndex;
    private float dragStartDisplayIndex;
    private int viewportWidth = 1;
    private int viewportHeight = 1;
    private long lastFrameTimeMillis = System.currentTimeMillis();
    private int preloadIndex;

    public static int getDefaultWallpaperIndex() {
        return 1;
    }

    public static int getWallpaperCount() {
        return WALLPAPER_ASSETS.length;
    }

    public WallpaperCarouselController(int n) {
        this.currentWallpaperIndex = MathHelper.clamp((int)n, (int)0, (int)(WALLPAPER_ASSETS.length - 1));
        this.displayedIndex = this.currentWallpaperIndex;
        this.targetIndex = this.currentWallpaperIndex;
    }

    public boolean isTransitionActive() {
        return this.transitionActive || this.transitionProgress > 0.001f;
    }

    public void startTransition() {
        this.transitionActive = true;
        this.displayedIndex = this.currentWallpaperIndex;
        this.targetIndex = this.currentWallpaperIndex;
        this.dragging = false;
        this.snapping = false;
    }

    public void stopTransition() {
        this.transitionActive = false;
        this.dragging = false;
        this.snapping = false;
        this.targetIndex = this.currentWallpaperIndex;
    }

    public void renderWallpaperCarousel(RockstarDrawContext drawContext, int n3, int n4, float f, float f2, float f3, float f4) {
        float f5;
        this.viewportWidth = Math.max(1, n3);
        this.viewportHeight = Math.max(1, n4);
        if (this.preloadIndex < WALLPAPER_ASSETS.length) {
            AssetImageLoader.getTexture(WALLPAPER_ASSETS[this.preloadIndex]);
            ++this.preloadIndex;
        }
        if ((f5 = AssetImageLoader.getTextureAspectRatio(WALLPAPER_ASSETS[this.currentWallpaperIndex])) > 0.0f) {
            this.wallpaperAspectRatio = f5;
        }
        long l = System.currentTimeMillis();
        float f6 = Math.min(0.1f, (float)(l - this.lastFrameTimeMillis) / 1000.0f);
        this.lastFrameTimeMillis = l;
        float f7 = 1.0f - (float)Math.pow(9.0E-4f, f6);
        float f8 = this.transitionActive ? 1.0f : 0.0f;
        this.transitionProgress += (f8 - this.transitionProgress) * f7;
        if (Math.abs(f8 - this.transitionProgress) < 8.0E-4f) {
            this.transitionProgress = f8;
        }
        float f9 = this.transitionProgress;
        if (!this.snapping) {
            this.displayedIndex += ((float)this.targetIndex - this.displayedIndex) * f7;
            if (Math.abs((float)this.targetIndex - this.displayedIndex) < 8.0E-4f) {
                this.displayedIndex = this.targetIndex;
            }
        }
        float f10 = this.calculateWallpaperSize(n3, n4) * MathUtils.interpolateDouble(1.05f - 0.05f * f3, 1.0, f9) * (1.0f - 0.045f * f4);
        float f11 = this.calculateWallpaperHeight(n3, n4);
        float f12 = MathUtils.interpolateDouble(f10, f11, f9);
        float f13 = f12 / this.wallpaperAspectRatio;
        float f14 = MathUtils.interpolateDouble(0.0, f11 * 1.16f, f9);
        float f15 = (float)n3 / 2.0f - f * (1.0f - f9);
        float f16 = (float)n4 / 2.0f - f2 * (1.0f - f9) - 4.0f * f9;
        float f17 = 18.0f * f9;
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (int i = 0; i < WALLPAPER_ASSETS.length; ++i) {
            arrayList.add(i);
        }
        arrayList.sort((n, n2) -> Float.compare(Math.abs((float)n2.intValue() - this.displayedIndex), Math.abs((float)n.intValue() - this.displayedIndex)));
        Iterator iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            int n5 = (Integer)iterator.next();
            float f18 = (float)n5 - this.displayedIndex;
            float f19 = Math.abs(f18);
            if (f19 > 1.85f) continue;
            float f20 = Math.min(1.0f, f19);
            float f21 = 1.0f - 0.14f * f20;
            float f22 = f12 * f21;
            float f23 = f13 * f21;
            float f24 = f15 + f18 * f14 - f22 / 2.0f;
            float f25 = f16 - f23 / 2.0f;
            float f26 = Math.max(f9, Math.max(0.0f, 1.0f - f19 * 6.0f));
            float f27 = (1.0f - 0.4f * f20) * f26;
            if (f27 <= 0.003f) continue;
            WidgetState widgetState = WidgetState.uniform(f17);
            if (f9 > 0.01f) {
                drawContext.drawShadow(f24, f25 + 6.0f * f9, f22, f23, 16.0f * f9, widgetState, ColorRGBA.BLACK.withAlpha(150.0f * f9 * f27));
            }
            drawContext.drawRoundedTexture(AssetImageLoader.getTexture(WALLPAPER_ASSETS[n5]), f24, f25, f22, f23, widgetState, ColorRGBA.WHITE.withAlpha(255.0f * f27));
            if (!(f20 > 0.003f)) continue;
            drawContext.drawRoundedRect(f24, f25, f22, f23, widgetState, ColorRGBA.BLACK.withAlpha(95.0f * f20 * f9));
        }
    }

    public void renderWallpaperTitle(RockstarDrawContext drawContext, int n, int n2) {
        float f;
        float f2;
        float f3;
        float f4 = this.transitionProgress;
        if (f4 <= 0.003f) {
            return;
        }
        FontMetrics fontMetrics = Font.SEMIBOLD.metrics(21.0f);
        float f5 = this.calculateTitleY(n, n2, fontMetrics) - 14.0f * (1.0f - f4);
        float f6 = 140.0f;
        for (int i = 0; i < WALLPAPER_ASSETS.length; ++i) {
            f3 = (float)i - this.displayedIndex;
            if (Math.abs(f3) >= 1.0f) continue;
            f2 = 1.0f - Math.abs(f3);
            f2 *= f2;
            f = (float)n / 2.0f + f3 * f6;
            String string = Localization.translate(WALLPAPER_KEYS[i]);
            drawContext.drawCenteredTextWithShadow(fontMetrics, string, f, f5, ColorRGBA.WHITE.withAlpha(255.0f * f4 * f2), ColorRGBA.BLACK.withAlpha(130.0f * f4 * f2), 1.0f, 2.0f, 4.0f);
        }
        float f7 = (float)n2 - 26.0f;
        f3 = 10.0f;
        f2 = 6.0f;
        f = (float)n / 2.0f - (float)(WALLPAPER_ASSETS.length - 1) * f3 / 2.0f;
        for (int i = 0; i < WALLPAPER_ASSETS.length; ++i) {
            float f8 = 1.0f - Math.min(1.0f, Math.abs((float)i - this.displayedIndex));
            float f9 = (0.3f + 0.7f * f8) * f4;
            drawContext.drawRoundedRect(f + (float)i * f3 - f2 / 2.0f, f7 - f2 / 2.0f, f2, f2, WidgetState.uniform(f2 / 2.0f), ColorRGBA.WHITE.withAlpha(255.0f * f9));
        }
    }

    public boolean beginDrag(double d, double d2, int n) {
        if (!this.transitionActive) {
            return false;
        }
        if (n == 1) {
            this.stopTransition();
            return true;
        }
        if (n != 0) {
            return true;
        }
        this.dragging = true;
        this.snapping = false;
        this.dragStartX = d;
        this.dragStartY = d2;
        this.dragStartIndex = d;
        this.dragStartDisplayIndex = this.displayedIndex;
        return true;
    }

    public boolean updateDragSelection(double d, double d2, int n) {
        if (!this.transitionActive || !this.dragging || n != 0) {
            return this.transitionActive;
        }
        if (Math.abs(d - this.dragStartX) > 4.0 || Math.abs(d2 - this.dragStartY) > 4.0) {
            this.snapping = true;
        }
        if (this.snapping) {
            float f = Math.max(1.0f, this.calculateWallpaperHeight(this.viewportWidth, this.viewportHeight) * 1.16f);
            this.displayedIndex = MathHelper.clamp((float)(this.dragStartDisplayIndex - (float)(d - this.dragStartIndex) / f), (float)0.0f, (float)((float)WALLPAPER_ASSETS.length - 1.0f));
            this.targetIndex = MathHelper.clamp((int)Math.round(this.displayedIndex), (int)0, (int)(WALLPAPER_ASSETS.length - 1));
        }
        return true;
    }

    public boolean endDrag(double d, double d2, int n) {
        if (!this.transitionActive || n != 0 || !this.dragging) {
            this.dragging = false;
            return this.transitionActive;
        }
        this.dragging = false;
        if (this.snapping) {
            this.snapping = false;
            this.targetIndex = MathHelper.clamp((int)Math.round(this.displayedIndex), (int)0, (int)(WALLPAPER_ASSETS.length - 1));
            return true;
        }
        int n2 = MathHelper.clamp((int)Math.round(this.displayedIndex), (int)0, (int)(WALLPAPER_ASSETS.length - 1));
        float f = Math.max(1.0f, this.calculateWallpaperHeight(this.viewportWidth, this.viewportHeight) * 1.16f);
        int n3 = MathHelper.clamp((int)Math.round(this.displayedIndex + (float)(d - (double)this.viewportWidth / 2.0) / f), (int)0, (int)(WALLPAPER_ASSETS.length - 1));
        if (n3 == n2 && this.isPointerOverWallpaper(d, d2)) {
            this.currentWallpaperIndex = n2;
            this.stopTransition();
        } else if (n3 != n2) {
            this.targetIndex = n3;
        }
        return true;
    }

    public boolean scrollBy(double d) {
        if (!this.transitionActive) {
            return false;
        }
        this.targetIndex = MathHelper.clamp((int)(this.targetIndex + (d > 0.0 ? -1 : 1)), (int)0, (int)(WALLPAPER_ASSETS.length - 1));
        return true;
    }

    public boolean moveSelection(int n) {
        if (!this.transitionActive) {
            return false;
        }
        if (n == 0) {
            return true;
        }
        int n2 = this.snapping ? Math.round(this.displayedIndex) : this.targetIndex;
        this.targetIndex = MathHelper.clamp((int)(n2 + Integer.signum(n)), (int)0, (int)(WALLPAPER_ASSETS.length - 1));
        this.dragging = false;
        this.snapping = false;
        return true;
    }

    private boolean isPointerOverWallpaper(double d, double d2) {
        float f = this.calculateWallpaperHeight(this.viewportWidth, this.viewportHeight);
        float f2 = f / this.wallpaperAspectRatio;
        float f3 = (float)this.viewportWidth / 2.0f;
        float f4 = (float)this.viewportHeight / 2.0f - 4.0f;
        return d >= (double)(f3 - f / 2.0f) && d <= (double)(f3 + f / 2.0f) && d2 >= (double)(f4 - f2 / 2.0f) && d2 <= (double)(f4 + f2 / 2.0f);
    }

    private float calculateTitleY(int n, int n2, FontMetrics fontMetrics) {
        float f = (float)n2 / 2.0f - 4.0f - this.calculateWallpaperHeight(n, n2) / this.wallpaperAspectRatio / 2.0f;
        float f2 = Math.min(42.0f, (float)n2 * 0.13f);
        float f3 = f - fontMetrics.getFontScale() - MathHelper.clamp((float)((float)n2 * 0.045f), (float)10.0f, (float)28.0f);
        return Math.max(6.0f, Math.min(f2, f3));
    }

    private float calculateWallpaperSize(int n, int n2) {
        float f;
        float f2;
        float f3 = (float)n / Math.max(1.0f, (float)n2);
        if (f3 > this.wallpaperAspectRatio) {
            f2 = n;
            f = (float)n / this.wallpaperAspectRatio;
        } else {
            f = n2;
            f2 = (float)n2 * this.wallpaperAspectRatio;
        }
        float f4 = 28.0f;
        float f5 = Math.max(((float)n + f4 * 2.0f) / f2, ((float)n2 + f4 * 2.0f) / f);
        f5 = Math.max(f5, 1.06f);
        return f2 * f5;
    }

    private float calculateWallpaperHeight(int n, int n2) {
        float f = (float)n * 0.52f;
        float f2 = (float)n2 * 0.62f;
        if (f / this.wallpaperAspectRatio > f2) {
            f = f2 * this.wallpaperAspectRatio;
        }
        return f;
    }

    @Generated
    public boolean isActive() {
        return this.transitionActive;
    }

    @Generated
    public float getTransitionProgress() {
        return this.transitionProgress;
    }

    @Generated
    public int getCurrentWallpaperIndex() {
        return this.currentWallpaperIndex;
    }
}
