/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  moscow.rockstar.modules.visuals.effects.EffectEntry
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ItemConvertible
 */
package moscow.rockstar.modules.visuals.effects.ui;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.modules.visuals.effects.EffectEntry;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.particles.AmbientParticleRenderer;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.text.Font;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemConvertible;

public class EffectPanel
implements EffectEntry {
    private final String title;
    private final ItemStack icon;
    private final List<EffectEntry> effectEntries = new ArrayList<EffectEntry>();
    private boolean expanded;
    private float y;
    private float x;
    private float width;
    private float scrollOffset;

    public EffectPanel(String string, ItemStack class_17992) {
        this.title = string;
        this.icon = class_17992;
    }

    public EffectPanel(String string) {
        this(string, new ItemStack((ItemConvertible)Items.ENCHANTED_BOOK));
    }

    public void addEntry(EffectEntry effectEntry) {
        this.effectEntries.add(effectEntry);
    }

    public float getHeight() {
        if (!this.expanded) {
            return 20.0f;
        }
        float f = 0.0f;
        for (EffectEntry effectEntry : this.effectEntries) {
            f += effectEntry.getValue();
        }
        return 20.0f + Math.min(f, 100.0f);
    }

    public void render(RockstarDrawContext drawContext, float f, float f2, float f3) {
        this.y = f2;
        this.x = f;
        this.width = f3;
        this.renderHeader(drawContext, f, f2, f3);
        if (this.expanded && !this.effectEntries.isEmpty()) {
            float f4 = f2 + 23.0f;
            float f5 = 100.0f;
            float f6 = this.getEntriesHeight();
            float maxScrollOffset = Math.max(0.0f, f6 - f5);
            this.scrollOffset = Math.max(0.0f, Math.min(maxScrollOffset, this.scrollOffset));
            float f7 = Math.min(f6, f5);
            moscow.rockstar.render.state.UiScissorStack.push((float)f, (float)f4, (float)f3, (float)f7);
            float f8 = f4 - this.scrollOffset;
            for (EffectEntry effectEntry : this.effectEntries) {
                effectEntry.handle(drawContext, f, f8, f3);
                f8 += effectEntry.getValue();
            }
            moscow.rockstar.render.state.UiScissorStack.pop();
        }
    }

    private void renderHeader(RockstarDrawContext drawContext, float f, float f2, float f3) {
        drawContext.drawRoundedRect(f, f2, f3, 20.0f, WidgetState.uniform(4.0f), ColorPalette.getPanelBackgroundColor());
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(0.0f, 0.0f, 200.0f);
        drawContext.drawBatchItem(this.icon, f + 4.0f, f2 + 4.0f, 0.75f);
        drawContext.getMatrices().pop();
        drawContext.drawText(Font.MEDIUM.metrics(7.0f), this.title, f + 22.0f, f2 + 8.0f, ColorPalette.getPrimaryTextColor());
    }

    private float getEntriesHeight() {
        float f = 0.0f;
        for (EffectEntry effectEntry : this.effectEntries) {
            f += effectEntry.getValue();
        }
        return f;
    }

    public void setScrollOffset(double d) {
        if (this.expanded) {
            float maxScrollOffset = Math.max(0.0f, this.getEntriesHeight() - 100.0f);
            this.scrollOffset = Math.max(0.0f, Math.min(maxScrollOffset, (float)d));
        }
    }

    public boolean handleClick(AmbientParticleRenderer ambientParticleRenderer, double d, double d2, int n) {
        if (d2 < (double)(this.y + 20.0f)) {
            this.expanded = !this.expanded;
            return true;
        }
        if (this.expanded) {
            float f = this.y + 20.0f - this.scrollOffset;
            for (EffectEntry effectEntry : this.effectEntries) {
                if (d2 >= (double)f && d2 < (double)(f + effectEntry.getValue())) {
                    return effectEntry.isValid(ambientParticleRenderer, d, d2, n);
                }
                f += effectEntry.getValue();
            }
        }
        return true;
    }

    @Generated
    public String getTitle() {
        return this.title;
    }

    @Generated
    public ItemStack getIcon() {
        return this.icon;
    }

    @Generated
    public List<EffectEntry> getEffectEntries() {
        return this.effectEntries;
    }

    @Generated
    public boolean isExpanded() {
        return this.expanded;
    }

    @Generated
    public float getY() {
        return this.y;
    }

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getWidth() {
        return this.width;
    }

    @Generated
    public float getScrollOffset() {
        return this.scrollOffset;
    }
}
