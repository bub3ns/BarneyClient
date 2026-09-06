/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.DiffuseLighting
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.ui.layout;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import moscow.rockstar.mixin.accessors.DrawContextAccessor;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.ui.animation.AnimatedValue;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;

public class ItemGrid<T>
extends UiNode {
    private final Supplier<List<T>> itemsSupplier;
    private final Function<T, ItemStack> itemStackProvider;
    private final Predicate<T> itemStatePredicate;
    private final Consumer<T> itemClickHandler;
    private float cellSize = 20.0f;
    private float cellPadding = 2.0f;
    private float itemScale = 0.9f;
    private float cornerRadius = 4.0f;
    private float topInset = 2.0f;
    private float bottomInset = -1.0f;
    private Consumer<T> hoverHandler;
    private int zOffset;
    private Function<ItemGrid<T>, ColorRGBA> backgroundColorProvider = itemGrid -> ColorPalette.MUTED_PANEL_COLOR;
    private Function<ItemGrid<T>, ColorRGBA> selectionColorProvider = itemGrid -> ColorPalette.ACCENT_COLOR.mulAlpha(0.7f);
    private Function<ItemGrid<T>, ColorRGBA> hoverColorProvider = itemGrid -> ColorPalette.ACCENT_COLOR;
    private final AnimatedValue scrollAnimation = new AnimatedValue(0.0f, Motion.resolveMotionMotionFromLongAndEasing(260L, Easing.easeOutQuart));
    private float targetScrollOffset = 0.0f;
    private float contentHeight = 0.0f;
    private final Map<T, float[]> itemAnimationStates = new IdentityHashMap<T, float[]>();

    public ItemGrid(Supplier<List<T>> supplier, Function<T, ItemStack> function, Predicate<T> predicate, Consumer<T> consumer) {
        this.itemsSupplier = supplier;
        this.itemStackProvider = function;
        this.itemStatePredicate = predicate;
        this.itemClickHandler = consumer;
        this.cursor(Cursor.HAND);
    }

    public ItemGrid<T> onItemHover(Consumer<T> consumer) {
        this.hoverHandler = consumer;
        return this;
    }

    public ItemGrid<T> setZOffset(int n) {
        this.zOffset = n;
        return this;
    }

    public ItemGrid<T> setCellSize(float f) {
        this.cellSize = f;
        return this;
    }

    public ItemGrid<T> setCellPadding(float f) {
        this.cellPadding = f;
        return this;
    }

    public ItemGrid<T> setItemScale(float f) {
        this.itemScale = f;
        return this;
    }

    public ItemGrid<T> setCornerRadius(float f) {
        this.cornerRadius = f;
        return this;
    }

    public ItemGrid<T> setTopInset(float f) {
        this.topInset = f;
        return this;
    }

    public ItemGrid<T> setBottomInset(float f) {
        this.bottomInset = f;
        return this;
    }

    public ItemGrid<T> setBackgroundColor(ColorRGBA colorRGBA) {
        this.backgroundColorProvider = itemGrid -> colorRGBA;
        return this;
    }

    public ItemGrid<T> setBackgroundColorProvider(Function<ItemGrid<T>, ColorRGBA> function) {
        this.backgroundColorProvider = function;
        return this;
    }

    public ItemGrid<T> setSelectionColor(ColorRGBA colorRGBA) {
        this.selectionColorProvider = itemGrid -> colorRGBA;
        return this;
    }

    public ItemGrid<T> setHoverColor(ColorRGBA colorRGBA) {
        this.hoverColorProvider = itemGrid -> colorRGBA;
        return this;
    }

    public ItemGrid<T> setWidth(float f) {
        super.width(f);
        return this;
    }

    public ItemGrid<T> setHeight(float f) {
        super.height(f);
        return this;
    }

    @Override
    public ItemGrid<T> fillWidth() {
        super.fillWidth();
        return this;
    }

    @Override
    public ItemGrid<T> fillHeight() {
        super.fillHeight();
        return this;
    }

    private float getInnerCellSize() {
        return this.cellSize - this.cellPadding * 2.0f;
    }

    private int getColumnCount(float f) {
        return Math.max(1, (int)Math.floor((f - this.topInset * 2.0f) / this.cellSize));
    }

    private int getItemIndexAt(float f, float f2) {
        float f3 = this.x();
        float f4 = this.y();
        float f5 = this.w();
        float f6 = this.h();
        if (f < f3 || f > f3 + f5 || f2 < f4 || f2 > f4 + f6) {
            return -1;
        }
        List<T> list = this.itemsSupplier.get();
        int n = this.getColumnCount(f5);
        float f7 = (float)n * this.cellSize;
        float f8 = f3 + (f5 - f7) / 2.0f;
        float f9 = f - f8;
        float f10 = f2 - f4 - this.topInset + this.scrollAnimation.getCurrent();
        if (f9 < 0.0f || f10 < 0.0f) {
            return -1;
        }
        int n2 = (int)(f9 / this.cellSize);
        int n3 = (int)(f10 / this.cellSize);
        if (n2 < 0 || n2 >= n) {
            return -1;
        }
        int n4 = n3 * n + n2;
        return n4 >= 0 && n4 < list.size() ? n4 : -1;
    }

    @Override
    protected void measure() {
        float f;
        float f2 = this.w();
        float f3 = f = this.bottomInset > 0.0f ? this.bottomInset : f2;
        if (f2 < 4.0f) {
            if (!this.explicitH) {
                this.prefH = this.bottomInset > 0.0f ? this.bottomInset : 110.0f;
            }
            return;
        }
        int n = this.getColumnCount(f2);
        int n2 = this.itemsSupplier.get().size();
        int n3 = Math.max(1, (int)Math.ceil((float)n2 / (float)n));
        this.contentHeight = (float)n3 * this.cellSize + this.topInset * 2.0f;
        if (!this.explicitH) {
            this.prefH = Math.min(this.contentHeight, f);
        }
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        float f4 = Math.max(0.0f, this.contentHeight - this.h());
        this.targetScrollOffset = Math.max(0.0f, Math.min(this.targetScrollOffset, f4));
        this.scrollAnimation.setTarget(this.targetScrollOffset);
        this.scrollAnimation.update(f);
        int n = this.inFlow() && this.contains(f2, f3) ? this.getItemIndexAt(f2, f3) : -1;
        List<T> list = this.itemsSupplier.get();
        if (this.hoverHandler != null) {
            T hoveredItem = n >= 0 && n < list.size() ? list.get(n) : null;
            this.hoverHandler.accept(hoveredItem);
        }
        float f5 = Math.min(1.0f, f / 1000.0f * 12.0f);
        for (int i = 0; i < list.size(); ++i) {
            T t = list.get(i);
            float[] fArray = this.getItemAnimation(t);
            float f6 = this.itemStatePredicate.test(t) ? 1.0f : 0.0f;
            float f7 = i == n ? 1.0f : 0.0f;
            fArray[0] = fArray[0] + (f6 - fArray[0]) * f5;
            fArray[1] = fArray[1] + (f7 - fArray[1]) * f5;
        }
    }

    private float[] getItemAnimation(T t) {
        float[] fArray = this.itemAnimationStates.get(t);
        if (fArray == null) {
            fArray = new float[]{this.itemStatePredicate.test(t) ? 1.0f : 0.0f, 0.0f};
            this.itemAnimationStates.put(t, fArray);
        }
        return fArray;
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        float f2;
        float f3;
        int n;
        List<T> list = this.itemsSupplier.get();
        int n2 = list.size();
        float f4 = this.x();
        float f5 = this.y();
        float f6 = this.w();
        float f7 = this.h();
        int n3 = this.getColumnCount(f6);
        float f8 = (float)n3 * this.cellSize;
        float f9 = f4 + (f6 - f8) / 2.0f;
        float f10 = this.scrollAnimation.getCurrent();
        float f11 = this.getInnerCellSize();
        WidgetState widgetState = WidgetState.uniform(this.cornerRadius);
        MatrixStack class_45872 = drawContext.getMatrices();
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)class_45872, (float)f4, (float)f5, (float)f6, (float)f7);
        ColorRGBA colorRGBA = this.backgroundColorProvider.apply(this);
        ColorRGBA colorRGBA2 = this.selectionColorProvider.apply(this);
        for (n = 0; n < n2; ++n) {
            f3 = f9 + (float)(n % n3) * this.cellSize;
            f2 = f5 + this.topInset + (float)(n / n3) * this.cellSize - f10;
            if (f2 + this.cellSize < f5 || f2 > f5 + f7) continue;
            float[] fArray = this.getItemAnimation(list.get(n));
            ColorRGBA colorRGBA3 = colorRGBA.mix(colorRGBA2, fArray[0]).mulAlpha(1.0f + 0.22f * fArray[1]);
            drawContext.drawRoundedRect(f3 + this.cellPadding, f2 + this.cellPadding, f11, f11, widgetState, colorRGBA3);
        }
        if (this.zOffset != 0) {
            class_45872.push();
            class_45872.translate(0.0f, 0.0f, (float)this.zOffset);
        }
        for (n = 0; n < n2; ++n) {
            f3 = f9 + (float)(n % n3) * this.cellSize;
            f2 = f5 + this.topInset + (float)(n / n3) * this.cellSize - f10;
            if (f2 + this.cellSize < f5 || f2 > f5 + f7) continue;
            float f12 = f3 + this.cellPadding + f11 / 2.0f;
            float f13 = f2 + this.cellPadding + f11 / 2.0f;
            ItemRenderUtils.translateAndScale(class_45872, f12, f13, this.itemScale * f);
            drawContext.drawBatchItem(this.itemStackProvider.apply(list.get(n)), f12 - 8.0f, f13 - 8.0f);
            ItemRenderUtils.popMatrix(class_45872);
            DiffuseLighting.disableGuiDepthLighting();
        }
        if (this.zOffset != 0) {
            class_45872.pop();
        }
        ((DrawContextAccessor)((Object)drawContext)).getVertexConsumers().draw();
        ColorRGBA colorRGBA4 = this.hoverColorProvider.apply(this);
        for (int i = 0; i < n2; ++i) {
            float[] fArray = this.getItemAnimation(list.get(i));
            if (fArray[1] <= 0.01f) continue;
            float f14 = f9 + (float)(i % n3) * this.cellSize;
            float f15 = f5 + this.topInset + (float)(i / n3) * this.cellSize - f10;
            if (f15 + this.cellSize < f5 || f15 > f5 + f7) continue;
            drawContext.drawRoundedBorder(f14 + this.cellPadding, f15 + this.cellPadding, f11, f11, 1.2f, widgetState, colorRGBA4.mulAlpha(0.6f * fArray[1]));
        }
        moscow.rockstar.render.state.UiScissorStack.pop();
    }

    @Override
    public boolean mouseClicked(float f, float f2, PointerAction pointerAction) {
        if (!this.inFlow() || !this.contains(f, f2)) {
            return false;
        }
        if (pointerAction != PointerAction.LEFT_CLICK) {
            return false;
        }
        int n = this.getItemIndexAt(f, f2);
        if (n < 0) {
            return false;
        }
        this.itemClickHandler.accept(this.itemsSupplier.get().get(n));
        return true;
    }

    @Override
    public boolean mouseScrolled(float f, float f2, float f3, float f4) {
        if (!this.inFlow() || !this.contains(f, f2)) {
            return false;
        }
        float f5 = Math.max(0.0f, this.contentHeight - this.h());
        if (f5 <= 0.5f) {
            return false;
        }
        this.targetScrollOffset = Math.max(0.0f, Math.min(this.targetScrollOffset - f4 * 22.0f, f5));
        return true;
    }




}

