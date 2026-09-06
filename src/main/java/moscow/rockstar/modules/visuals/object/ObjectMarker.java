package moscow.rockstar.modules.visuals.object;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.shaders.ShaderRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import pyrock.events.render.Render3DEvent;

final class ObjectMarker {
    double x;
    double y;
    double z;
    float velocityX;
    float velocityY;
    float velocityZ;
    long spawnTimeMillis;
    float innerRadius = 1.0f;
    float outerRadius = 2.5f;
    float gravityStep;
    long lifetimeMillis;
    boolean affectedByGravity;
    float velocityDamping;
    static final float COLLISION_BOUNCE_FACTOR = 0.4f;

    ObjectMarker(ObjectInfo owner, Vec3d position, float offsetX, float offsetY, float offsetZ,
                 float velocityX, float velocityY, float velocityZ, boolean affectedByGravity) {
        this.x = position.x + offsetX;
        this.y = position.y + offsetY;
        this.z = position.z + offsetZ;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.spawnTimeMillis = System.currentTimeMillis();
        this.affectedByGravity = affectedByGravity;
        this.gravityStep = 2.0E-4f + ObjectInfo.random.nextFloat() * 4.0E-4f;
        this.lifetimeMillis = 2000L + ObjectInfo.random.nextInt(2000);
        this.velocityDamping = affectedByGravity ? 0.9999f : 0.999f;
    }

    void updatePhysics() {
        if (this.affectedByGravity) {
            this.velocityY -= this.gravityStep;
        }
        this.velocityX *= this.velocityDamping;
        this.velocityY *= this.velocityDamping;
        this.velocityZ *= this.velocityDamping;
        double nextX = this.x + this.velocityX;
        double nextY = this.y + this.velocityY;
        double nextZ = this.z + this.velocityZ;
        if (this.affectedByGravity) {
            BlockPos below = BlockPos.ofFloored(this.x, nextY - 0.5, this.z);
            if (!ClientAccess.minecraftClient.world.getBlockState(below).getCollisionShape(
                    ClientAccess.minecraftClient.world, below).isEmpty()) {
                this.velocityY = -this.velocityY * 0.4f;
                nextY = this.y;
            }
            BlockPos xBlock = BlockPos.ofFloored(nextX, this.y, this.z);
            if (!ClientAccess.minecraftClient.world.getBlockState(xBlock).getCollisionShape(
                    ClientAccess.minecraftClient.world, xBlock).isEmpty()) {
                this.velocityX = -this.velocityX * 0.4f;
                nextX = this.x;
            }
            BlockPos zBlock = BlockPos.ofFloored(this.x, this.y, nextZ);
            if (!ClientAccess.minecraftClient.world.getBlockState(zBlock).getCollisionShape(
                    ClientAccess.minecraftClient.world, zBlock).isEmpty()) {
                this.velocityZ = -this.velocityZ * 0.4f;
                nextZ = this.z;
            }
        }
        this.x = nextX;
        this.y = nextY;
        this.z = nextZ;
    }

    float getAgeProgress() {
        return MathHelper.clamp((float) (System.currentTimeMillis() - this.spawnTimeMillis)
                / (float) this.lifetimeMillis, 0.0f, 1.0f);
    }

    float getFadeAlpha() {
        return 1.0f - this.getAgeProgress();
    }

    boolean isExpired() {
        return System.currentTimeMillis() - this.spawnTimeMillis > this.lifetimeMillis;
    }

    void render(Render3DEvent event, BufferBuilder buffer) {
        float innerSize = 1.0f;
        float outerSize = 2.5f;
        MatrixStack matrices = event.getMatrices();
        Camera camera = ClientAccess.minecraftClient.gameRenderer.getCamera();
        float fade = this.getFadeAlpha();
        matrices.push();
        ItemRenderUtils.translateToWorldPosition(matrices, new Vec3d(this.x, this.y, this.z));
        matrices.multiply(camera.getRotation());
        ShaderRenderer.appendTexturedQuadVertices(matrices, buffer,
                -innerSize / 2.0f, -innerSize / 2.0f, 0.0,
                innerSize, innerSize, ColorPalette.getAccentColor().mulAlpha(0.9f * fade));
        ShaderRenderer.appendTexturedQuadVertices(matrices, buffer,
                -outerSize / 2.0f, -outerSize / 2.0f, 0.0,
                outerSize, outerSize, ColorPalette.getAccentColor().mulAlpha(0.1f * fade));
        matrices.pop();
    }
}
