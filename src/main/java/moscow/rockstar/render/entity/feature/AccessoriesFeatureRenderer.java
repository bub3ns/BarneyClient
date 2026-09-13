package moscow.rockstar.render.entity.feature;

import moscow.rockstar.api.access.EntityAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.accessories.Accessories;
import moscow.rockstar.modules.visuals.accessories.renderers.HeadCrystalRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class AccessoriesFeatureRenderer
extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    public AccessoriesFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    @Override
    public void render(
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        PlayerEntityRenderState state,
        float limbAngle,
        float limbDistance
    ) {
        if (state.invisible) {
            return;
        }

        Accessories accessories = RockstarClient.create().getModuleRegistry().getModule(Accessories.class);
        if (accessories == null || !accessories.isEnabled()) {
            return;
        }

        Entity entity = ((EntityAccess) state).rockstar$getEntity();
        if (entity == null || entity != MinecraftClient.getInstance().player) {
            return;
        }

        // Head Category
        if (accessories.getHead().isSelected(accessories.getHeadCrystal())) {
            matrices.push();
            PlayerEntityModel model = this.getContextModel();
            model.getRootPart().rotate(matrices);
            model.getHead().rotate(matrices);
            accessories.getHeadCrystalRenderer().render(matrices, vertexConsumers, accessories.getColor(), accessories);
            matrices.pop();
        }

        // Back Category -> NONE
        // Legs Category -> NONE
        // Boots Category -> NONE
    }
}
