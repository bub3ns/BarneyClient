/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Arm
 *  net.minecraft.ItemStack
 *  net.minecraft.MatrixStack
 */
package pyrock.events.render;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import net.minecraft.util.Arm;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.events.EventCancellable;

@ScreenController(description="hand_render")
public class HandRenderEvent
extends EventCancellable {
    private final Arm arm;
    private final float swingProgress;
    private final ItemStack itemStack;
    private final float equipProgress;
    private final MatrixStack matrices;

    @Generated
    public Arm getArm() {
        return this.arm;
    }

    @Generated
    public float getSwingProgress() {
        return this.swingProgress;
    }

    @Generated
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Generated
    public float getEquipProgress() {
        return this.equipProgress;
    }

    @Generated
    public MatrixStack getMatrices() {
        return this.matrices;
    }

    @Generated
    public HandRenderEvent(Arm class_13062, float f, ItemStack class_17992, float f2, MatrixStack class_45872) {
        this.arm = class_13062;
        this.swingProgress = f;
        this.itemStack = class_17992;
        this.equipProgress = f2;
        this.matrices = class_45872;
    }
}

