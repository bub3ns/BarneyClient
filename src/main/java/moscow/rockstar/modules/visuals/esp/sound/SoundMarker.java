package moscow.rockstar.modules.visuals.esp.sound;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.util.Timer;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

final class SoundMarker {
    private final SoundInstance soundEvent;
    private final Vec3d position;
    private final Timer lifetimeTimer;

    SoundMarker(SoundInstance soundEvent) {
        this.soundEvent = soundEvent;
        this.position = new Vec3d(soundEvent.getX(), soundEvent.getY(), soundEvent.getZ());
        this.lifetimeTimer = new Timer();
    }

    void renderLabel(CustomDrawContext context, FontMetrics fontMetrics, SoundType soundType) {
        if (ClientAccess.minecraftClient.player == null) {
            return;
        }
        Vec3d labelPosition = this.position.add(0.0, 0.5, 0.0);
        float distance = (float) ClientAccess.minecraftClient.player.getPos().distanceTo(this.position);
        WeightedSoundSet soundSet = this.soundEvent.getSoundSet(ClientAccess.minecraftClient.getSoundManager());
        if (soundSet == null || soundSet.getSubtitle() == null) {
            return;
        }
        MutableText label = soundSet.getSubtitle().copy()
                .append(" (")
                .append(String.format("%.0f", distance) + "m")
                .append(")");
        Vec2f screenPosition = ProjectionUtils.projectToScreen(labelPosition);
        if (screenPosition == null) {
            return;
        }
        float distanceScale = MathHelper.clamp(1.0f - distance / 20.0f, 0.5f, 1.0f);
        float screenFade = this.calculateScreenFade(screenPosition) / 255.0f;
        Item iconItem = this.getIconItem();
        float iconWidth = iconItem != null ? 18.0f : 0.0f;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(screenPosition.x, screenPosition.y, 0.0f);
        matrices.scale(distanceScale, distanceScale, 1.0f);
        float textWidth = fontMetrics.measureTextComponent(label);
        float totalWidth = textWidth + iconWidth;
        float left = -totalWidth / 2.0f;
        switch (soundType) {
            case TRIDENT -> {
                float fontHeight = fontMetrics.getFontTopOffset();
                float verticalPadding = 4.0f;
                float horizontalPadding = 2.0f;
                context.drawRect(left - horizontalPadding, -verticalPadding,
                        totalWidth + horizontalPadding * 2.0f,
                        fontHeight + verticalPadding * 2.0f,
                        ColorPalette.BLACK.mulAlpha(0.5f).mulAlpha(screenFade));
            }
            case TNT -> {
                if (iconItem == null) {
                    break;
                }
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, screenFade);
                context.drawItem(new ItemStack(iconItem), (int) left, -3);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            case FIREWORK -> {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, screenFade);
                context.drawText(fontMetrics, label.getString(), (int) (left + iconWidth), 0.0f,
                        ColorRGBA.WHITE);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
        matrices.pop();
    }

    private Item getIconItem() {
        return switch (this.soundEvent.getId().toString().toLowerCase()) {
            case "minecraft:entity.generic.explode" -> Items.TNT;
            case "minecraft:item.trident.throw", "minecraft:item.trident.return" -> Items.TRIDENT;
            case "minecraft:entity.firework_rocket.launch" -> Items.FIREWORK_ROCKET;
            default -> null;
        };
    }

    public boolean isExpired() {
        return this.lifetimeTimer.hasElapsed(5000L);
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
