package moscow.rockstar.modules.visuals.effects.kill;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.shaders.ShaderRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

final class KillEffect {
    double x;
    double y;
    double z;
    float velocityX;
    float velocityY;
    float velocityZ;
    long spawnTimeMillis;
    long lastUpdateMillis;
    float upwardDrift;
    long lifetimeMillis;
    boolean affectedByGravity;
    float velocityDamping;
    ColorRGBA color;
    static final float COLLISION_BOUNCE_FACTOR = 0.5f;

    KillEffect(KillEffects owner, Vec3d position, float offsetX, float offsetY, float offsetZ,
               float velocityX, float velocityY, float velocityZ, ColorRGBA color,
               boolean affectedByGravity) {
        this.x = position.x + offsetX;
        this.y = position.y + offsetY;
        this.z = position.z + offsetZ;
        this.color = color;
        this.lastUpdateMillis = this.spawnTimeMillis = System.currentTimeMillis();
        this.affectedByGravity = affectedByGravity;
        this.upwardDrift = 0.005f + KillEffects.random.nextFloat() * 0.005f;
        this.lifetimeMillis = 1500L + KillEffects.random.nextInt(1500);
        this.velocityDamping = affectedByGravity ? 0.999f : 0.995f;
        float spread = affectedByGravity ? 0.04f : 0.03f;
        this.velocityX = (KillEffects.random.nextFloat() - 0.5f) * spread;
        this.velocityY = affectedByGravity
                ? 0.025f + KillEffects.random.nextFloat() * 0.035f
                : (KillEffects.random.nextFloat() - 0.5f) * 0.03f;
        this.velocityZ = (KillEffects.random.nextFloat() - 0.5f) * spread;
    }

    void updatePhysics() {
        float bounceFactor = 0.5f;
        long now = System.currentTimeMillis();
        float tickScale = (float) (now - this.lastUpdateMillis) / 16.67f;
        this.lastUpdateMillis = now;
        if (tickScale > 5.0f) {
            tickScale = 5.0f;
        }
        if (this.affectedByGravity) {
            this.velocityY -= this.upwardDrift * tickScale;
        }
        float damping = (float) Math.pow(this.velocityDamping, tickScale);
        this.velocityX *= damping;
        this.velocityY *= damping;
        this.velocityZ *= damping;
        double nextX = this.x + this.velocityX * tickScale;
        double nextY = this.y + this.velocityY * tickScale;
        double nextZ = this.z + this.velocityZ * tickScale;
        if (this.affectedByGravity && ClientAccess.minecraftClient.world != null) {
            BlockPos below = BlockPos.ofFloored(this.x, nextY - 0.05f, this.z);
            if (!ClientAccess.minecraftClient.world.getBlockState(below).getCollisionShape(
                    ClientAccess.minecraftClient.world, below).isEmpty()) {
                this.velocityY = -this.velocityY * bounceFactor;
                nextY = this.y;
            }
            BlockPos xBlock = BlockPos.ofFloored(nextX, this.y, this.z);
            if (!ClientAccess.minecraftClient.world.getBlockState(xBlock).getCollisionShape(
                    ClientAccess.minecraftClient.world, xBlock).isEmpty()) {
                this.velocityX = -this.velocityX * bounceFactor;
                nextX = this.x;
            }
            BlockPos zBlock = BlockPos.ofFloored(this.x, this.y, nextZ);
            if (!ClientAccess.minecraftClient.world.getBlockState(zBlock).getCollisionShape(
                    ClientAccess.minecraftClient.world, zBlock).isEmpty()) {
                this.velocityZ = -this.velocityZ * bounceFactor;
                nextZ = this.z;
            }
        }
        if (Math.abs(this.velocityY) <= 1.0E-4f) {
            this.velocityX = 0.0f;
            this.velocityZ = 0.0f;
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
        float innerSize = 0.1f;
        float outerSize = 0.5f;
        MatrixStack matrices = event.getMatrices();
        Camera camera = ClientAccess.minecraftClient.gameRenderer.getCamera();
        float fade = this.getFadeAlpha();
        matrices.push();
        ItemRenderUtils.translateToWorldPosition(matrices, new Vec3d(this.x, this.y, this.z));
        matrices.multiply(camera.getRotation());
        ShaderRenderer.appendTexturedQuadVertices(matrices, buffer,
                -innerSize / 2.0f, -innerSize / 2.0f, 0.0,
                innerSize, innerSize, this.color.mulAlpha(0.9f * fade));
        ShaderRenderer.appendTexturedQuadVertices(matrices, buffer,
                -outerSize / 2.0f, -outerSize / 2.0f, 0.0,
                outerSize, outerSize, this.color.mulAlpha(0.15f * fade));
        matrices.pop();
    }
}
