package moscow.rockstar.render.vertex;

import net.minecraft.client.render.VertexConsumer;

/** Applies a uniform alpha multiplier while forwarding vertex data. */
final class VertexConsumerWrapper implements VertexConsumer {
    VertexConsumer delegate;
    float alphaMultiplier = 1.0f;

    VertexConsumerWrapper() {
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        return this.delegate.vertex(x, y, z);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this.delegate.color(red, green, blue, (int) (alpha * this.alphaMultiplier));
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return this.delegate.texture(u, v);
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this.delegate.overlay(u, v);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return this.delegate.light(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this.delegate.normal(x, y, z);
    }
}
