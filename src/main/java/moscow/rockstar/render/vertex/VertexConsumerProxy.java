package moscow.rockstar.render.vertex;

import net.minecraft.client.render.VertexConsumer;

/** Vertex sink used during projection-only hand rendering. */
final class VertexConsumerProxy implements VertexConsumer {
    private final VertexConsumerState state;

    VertexConsumerProxy(VertexConsumerState state) {
        this.state = state;
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        this.state.projectVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this;
    }

    public VertexConsumerState getState() {
        return this.state;
    }
}
