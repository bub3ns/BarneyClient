/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Vec3d
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumer
 *  net.minecraft.EntityModel
 *  net.minecraft.ModelPart
 *  net.minecraft.RotationAxis
 *  net.minecraft.EntityRenderer
 *  net.minecraft.EntityRenderDispatcher
 *  net.minecraft.LivingEntityRenderer
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package moscow.rockstar.render.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.mixin.accessors.ModelPartAccessor;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.render.util.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import pyrock.utility.render.ColorRGBA;

public final class ModelPartBoundsRenderer {
    public static List<ModelPartBounds> collectModelPartBounds(LivingEntity class_13092) {
        ArrayList<ModelPartBounds> arrayList = new ArrayList<ModelPartBounds>();
        MinecraftClient client = MinecraftClient.getInstance();
        EntityRenderDispatcher TrialSpawnerDetectionParticle = client.getEntityRenderDispatcher();
        EntityRenderer BreezeAnimations = TrialSpawnerDetectionParticle.getRenderer((Entity)class_13092);
        if (!(BreezeAnimations instanceof LivingEntityRenderer)) {
            return arrayList;
        }
        LivingEntityRenderer Builder = (LivingEntityRenderer)BreezeAnimations;
        EntityModel Impl = Builder.getModel();
        MatrixStack class_45872 = new MatrixStack();
        float f = client.getRenderTickCounter().getTickDelta(true);
        Vec3d VanillaChestLootTableGenerator = class_13092.getLerpedPos(f);
        class_45872.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-class_13092.bodyYaw + 180.0f));
        Impl.getParts().forEach(GameTest -> ModelPartBoundsRenderer.collectPartBounds(GameTest, class_45872, arrayList, VanillaChestLootTableGenerator, "root"));
        return arrayList;
    }

    private static void collectPartBounds(ModelPart GameTest, MatrixStack class_45872, List<ModelPartBounds> list, Vec3d VanillaChestLootTableGenerator, String string2) {
        class_45872.push();
        GameTest.rotate(class_45872);
        GameTest.forEachCuboid(class_45872, (class_46652, string, n, PackageInfo6282) -> {
            Matrix4f matrix4f = class_46652.getPositionMatrix();
            Vec3d[] class_243Array = new Vec3d[8];
            Vector3f[] vector3fArray = new Vector3f[]{new Vector3f(PackageInfo6282.minX, PackageInfo6282.minY, PackageInfo6282.minZ), new Vector3f(PackageInfo6282.maxX, PackageInfo6282.minY, PackageInfo6282.minZ), new Vector3f(PackageInfo6282.minX, PackageInfo6282.maxY, PackageInfo6282.minZ), new Vector3f(PackageInfo6282.maxX, PackageInfo6282.maxY, PackageInfo6282.minZ), new Vector3f(PackageInfo6282.minX, PackageInfo6282.minY, PackageInfo6282.maxZ), new Vector3f(PackageInfo6282.maxX, PackageInfo6282.minY, PackageInfo6282.maxZ), new Vector3f(PackageInfo6282.minX, PackageInfo6282.maxY, PackageInfo6282.maxZ), new Vector3f(PackageInfo6282.maxX, PackageInfo6282.maxY, PackageInfo6282.maxZ)};
            for (int i = 0; i < 8; ++i) {
                Vector3f vector3f = new Vector3f((Vector3fc)vector3fArray[i]);
                matrix4f.transformPosition(vector3f);
                double d = 0.0625;
                class_243Array[i] = new Vec3d(VanillaChestLootTableGenerator.x + (double)vector3f.x * d, VanillaChestLootTableGenerator.y + (double)vector3f.y * d, VanillaChestLootTableGenerator.z + (double)vector3f.z * d);
            }
            list.add(new ModelPartBounds(class_243Array, string));
        });
        ModelPartAccessor modelPartAccessor = (ModelPartAccessor)(Object)GameTest;
        for (Map.Entry<String, ModelPart> entry : modelPartAccessor.rockstar$getChildren().entrySet()) {
            ModelPartBoundsRenderer.collectPartBounds(entry.getValue(), class_45872, list, VanillaChestLootTableGenerator, string2 + "." + entry.getKey());
        }
        class_45872.pop();
    }

    public static void renderBoundsEdges(ModelPartBounds modelPartBounds, VertexConsumer class_45882, MatrixStack class_45872, ColorRGBA colorRGBA) {
        ModelPartBoundsRenderer.renderEdge(class_45872, class_45882, modelPartBounds.corners[0], modelPartBounds.corners[1], colorRGBA);
        ModelPartBoundsRenderer.renderEdge(class_45872, class_45882, modelPartBounds.corners[1], modelPartBounds.corners[3], colorRGBA);
        ModelPartBoundsRenderer.renderEdge(class_45872, class_45882, modelPartBounds.corners[3], modelPartBounds.corners[2], colorRGBA);
        ModelPartBoundsRenderer.renderEdge(class_45872, class_45882, modelPartBounds.corners[2], modelPartBounds.corners[0], colorRGBA);
        ModelPartBoundsRenderer.renderEdge(class_45872, class_45882, modelPartBounds.corners[4], modelPartBounds.corners[5], colorRGBA);
        ModelPartBoundsRenderer.renderEdge(class_45872, class_45882, modelPartBounds.corners[5], modelPartBounds.corners[7], colorRGBA);
        ModelPartBoundsRenderer.renderEdge(class_45872, class_45882, modelPartBounds.corners[7], modelPartBounds.corners[6], colorRGBA);
        ModelPartBoundsRenderer.renderEdge(class_45872, class_45882, modelPartBounds.corners[6], modelPartBounds.corners[4], colorRGBA);
        ModelPartBoundsRenderer.renderEdge(class_45872, class_45882, modelPartBounds.corners[0], modelPartBounds.corners[4], colorRGBA);
        ModelPartBoundsRenderer.renderEdge(class_45872, class_45882, modelPartBounds.corners[1], modelPartBounds.corners[5], colorRGBA);
        ModelPartBoundsRenderer.renderEdge(class_45872, class_45882, modelPartBounds.corners[2], modelPartBounds.corners[6], colorRGBA);
        ModelPartBoundsRenderer.renderEdge(class_45872, class_45882, modelPartBounds.corners[3], modelPartBounds.corners[7], colorRGBA);
    }

    private static void renderEdge(MatrixStack class_45872, VertexConsumer class_45882, Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, ColorRGBA colorRGBA) {
        RenderUtils.drawLineSegment(class_45872, class_45882, VanillaChestLootTableGenerator, WallPlayerSkullBlock, colorRGBA);
    }

    @Generated
    private ModelPartBoundsRenderer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static class ModelPartBounds {
        public Vec3d[] corners;
        public String partName;

        public ModelPartBounds(Vec3d[] class_243Array, String string) {
            this.corners = class_243Array;
            this.partName = string;
        }

        public Vec3d getCenter() {
            double d = 0.0;
            double d2 = 0.0;
            double d3 = 0.0;
            for (Vec3d VanillaChestLootTableGenerator : this.corners) {
                d += VanillaChestLootTableGenerator.x;
                d2 += VanillaChestLootTableGenerator.y;
                d3 += VanillaChestLootTableGenerator.z;
            }
            return new Vec3d(d / 8.0, d2 / 8.0, d3 / 8.0);
        }

        public boolean contains(Vec3d VanillaChestLootTableGenerator) {
            double d = Double.MAX_VALUE;
            double d2 = Double.MAX_VALUE;
            double d3 = Double.MAX_VALUE;
            double d4 = -1.7976931348623157E308;
            double d5 = -1.7976931348623157E308;
            double d6 = -1.7976931348623157E308;
            for (Vec3d WallPlayerSkullBlock : this.corners) {
                d = Math.min(d, WallPlayerSkullBlock.x);
                d2 = Math.min(d2, WallPlayerSkullBlock.y);
                d3 = Math.min(d3, WallPlayerSkullBlock.z);
                d4 = Math.max(d4, WallPlayerSkullBlock.x);
                d5 = Math.max(d5, WallPlayerSkullBlock.y);
                d6 = Math.max(d6, WallPlayerSkullBlock.z);
            }
            return VanillaChestLootTableGenerator.x >= d && VanillaChestLootTableGenerator.x <= d4 && VanillaChestLootTableGenerator.y >= d2 && VanillaChestLootTableGenerator.y <= d5 && VanillaChestLootTableGenerator.z >= d3 && VanillaChestLootTableGenerator.z <= d6;
        }
    }
}
