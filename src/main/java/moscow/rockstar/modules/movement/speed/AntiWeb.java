package moscow.rockstar.modules.movement.speed;

import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import pyrock.events.player.ClientPlayerTickEvent;

@ModuleInfo(name="Anti Web", category=ModuleCategory.MOVEMENT)
public class AntiWeb extends Module {
    private final NumberSetting speedSetting;

    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (minecraftClient.player == null || minecraftClient.world == null) {
            return;
        }
        this.applyNoWebMovement();
    };

    public AntiWeb() {
        this.speedSetting = new NumberSetting(this, "modules.settings.web_utils.no_web_speed")
                .setMinValue(0.1f)
                .setMaxValue(1.0f)
                .setStep(0.01f)
                .setValue(0.57f);
    }

    private void applyNoWebMovement() {
        if (minecraftClient.player == null || minecraftClient.world == null || !this.isPlayerInWeb()) {
            return;
        }
        double verticalVelocity = minecraftClient.options.jumpKey.isPressed() ? 1.3 : (minecraftClient.options.sneakKey.isPressed() ? -1.3 : 0.0);
        float yawRadians = minecraftClient.player.getYaw() * ((float)Math.PI / 180);
        float speed = this.speedSetting.getValue();
        float forward = minecraftClient.player.forwardSpeed * speed;
        float sideways = minecraftClient.player.sidewaysSpeed * speed;
        if (forward != 0.0f || sideways != 0.0f) {
            minecraftClient.player.setVelocity(
                (double)(-MathHelper.sin(yawRadians) * forward + MathHelper.cos(yawRadians) * sideways),
                verticalVelocity,
                (double)(MathHelper.cos(yawRadians) * forward + MathHelper.sin(yawRadians) * sideways)
            );
        } else {
            minecraftClient.player.setVelocity(0.0, verticalVelocity, 0.0);
        }
    }

    private boolean isPlayerInWeb() {
        return this.isEntityInWeb((LivingEntity)minecraftClient.player);
    }

    private boolean isEntityInWeb(LivingEntity livingEntity) {
        Box box = livingEntity.getBoundingBox();
        int minX = MathHelper.floor(box.minX);
        int minY = MathHelper.floor(box.minY);
        int minZ = MathHelper.floor(box.minZ);
        int maxX = MathHelper.ceil(box.maxX);
        int maxY = MathHelper.ceil(box.maxY);
        int maxZ = MathHelper.ceil(box.maxZ);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    if (!minecraftClient.world.getBlockState((BlockPos)mutable.set(x, y, z)).isOf(Blocks.COBWEB)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public NumberSetting getSpeedSetting() {
        return this.speedSetting;
    }
}
