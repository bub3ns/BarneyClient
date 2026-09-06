package moscow.rockstar.modules.visuals.tnt;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

final class TntMarker {
    private final Vec3d position;
    private final String countdownLabel;

    TntMarker(TntEntity tntEntity) {
        float tickDelta = ClientAccess.minecraftClient.getRenderTickCounter().getTickDelta(true);
        this.position = tntEntity.getLerpedPos(tickDelta).add(0.0, 0.5, 0.0);
        this.countdownLabel = Localization.translateFormatted(
                "modules.tnt_timer.format", tntEntity.getFuse() / 20.0f);
    }

    void renderLabel(CustomDrawContext context, FontMetrics fontMetrics, TntType tntType) {
        Vec2f screenPosition = ProjectionUtils.projectToScreen(this.position);
        if (screenPosition == null) {
            return;
        }
        float distance = (float) ClientAccess.minecraftClient.player.getPos().distanceTo(this.position);
        float scale = MathHelper.clamp(1.0f - distance / 20.0f, 0.5f, 1.0f);
        float fade = this.calculateScreenFade(screenPosition) / 255.0f;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(screenPosition.x, screenPosition.y, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        float totalWidth = fontMetrics.measureText(this.countdownLabel) + 18.0f;
        float left = -totalWidth / 2.0f;
        float fontHeight = fontMetrics.getFontTopOffset();
        float verticalPadding = 4.0f;
        float horizontalPadding = 2.0f;
        switch (tntType) {
            case PRIMED -> context.drawRect(left - horizontalPadding, -verticalPadding,
                    totalWidth + horizontalPadding * 2.0f + 4.0f,
                    fontHeight + verticalPadding * 2.0f,
                    ColorPalette.BLACK.mulAlpha(0.5f).mulAlpha(fade));
            case MINECART -> {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fade);
                context.drawItem(new ItemStack(Items.TNT), (int) left, -3);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            case BLOCK -> {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fade);
                context.drawText(fontMetrics, this.countdownLabel,
                        (int) (left + 20.0f), 0.0f, ColorRGBA.WHITE);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
        matrices.pop();
    }

    private float calculateScreenFade(Vec2f screenPosition) {
        if (screenPosition == null || ClientAccess.minecraftClient.getWindow() == null) {
            return 255.0f;
        }
        int width = ClientAccess.minecraftClient.getWindow().getScaledWidth();
        int height = ClientAccess.minecraftClient.getWindow().getScaledHeight();
        float x = screenPosition.x - width / 2.0f;
        float y = screenPosition.y - height / 2.0f;
        float diagonal = (float) Math.sqrt(width * width + height * height) / 12.0f;
        return 90.0f + 165.0f * Math.min((float) Math.sqrt(x * x + y * y) / diagonal, 1.0f);
    }
}
