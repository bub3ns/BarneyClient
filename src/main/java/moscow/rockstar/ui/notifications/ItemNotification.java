/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  org.lwjgl.opengl.GL11
 */
package moscow.rockstar.ui.notifications;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.ui.notifications.NotificationRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

public class ItemNotification
extends NotificationRenderer {
    private final ItemStack itemStack;

    public ItemNotification(String string, ItemStack class_17992) {
        super(string, null, null);
        this.itemStack = class_17992.copy();
    }

    public ItemNotification(String string, Item class_17922) {
        super(string, null, null);
        this.itemStack = class_17922.getDefaultStack();
    }

    public ItemNotification withHighlightedText(String string) {
        this.highlightedText = string;
        if (this.highlightColor == null) {
            Integer n = this.itemStack.getRarity().getFormatting().getColorValue();
            this.highlightColor = n != null ? ColorRGBA.fromInt(n) : new ColorRGBA(255.0f, 85.0f, 85.0f);
        }
        return this;
    }

    public ItemNotification withHighlightColor(ColorRGBA colorRGBA) {
        this.highlightColor = colorRGBA;
        return this;
    }

    @Override
    protected void renderContent(CustomDrawContext customDrawContext, float f, float f2, float f3) {
        float[] fArray = (float[])RenderSystem.getShaderColor().clone();
        boolean bl = GL11.glIsEnabled((int)3042);
        WidgetBatchRenderer.flushCurrentBatch();
        customDrawContext.draw();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f3);
        customDrawContext.drawItem(this.itemStack, f, f2, 0.625f);
        customDrawContext.draw();
        RenderSystem.setShaderColor((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
        if (!bl) {
            RenderSystem.disableBlend();
        }
    }
}

