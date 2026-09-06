/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  org.joml.Matrix4f
 */
package pyrock.events.render;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

@ScreenController(description="render_3d")
public class Render3DEvent
extends Event {
    private final MatrixStack matrices;
    private final Matrix4f positionMatrix;
    private final Matrix4f projectionMatrix;
    private final Camera camera;
    private final float tickDelta;

    @Generated
    public MatrixStack getMatrices() {
        return this.matrices;
    }

    @Generated
    public Matrix4f getPositionMatrix() {
        return this.positionMatrix;
    }

    @Generated
    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    @Generated
    public Camera getCamera() {
        return this.camera;
    }

    @Generated
    public float getTickDelta() {
        return this.tickDelta;
    }

    @Generated
    public Render3DEvent(MatrixStack class_45872, Matrix4f matrix4f, Matrix4f matrix4f2, Camera class_41842, float f) {
        this.matrices = class_45872;
        this.positionMatrix = matrix4f;
        this.projectionMatrix = matrix4f2;
        this.camera = class_41842;
        this.tickDelta = f;
    }
}

