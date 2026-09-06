/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3d
 *  net.minecraft.ClientWorld
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.world.Ambience;
import moscow.rockstar.render.colors.ColorPalette;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientWorld.class})
public class ClientWorldMixin {
    @Inject(method={"getSkyColor"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetSkyColor(Vec3d VanillaChestLootTableGenerator, float f, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        Ambience ambience = RockstarClient.create().getModuleRegistry().getModule(Ambience.class);
        if (ambience.isEnabled() && ambience.getSkyChangeOption().isSelected()) {
            callbackInfoReturnable.setReturnValue(ambience.getThemeSyncSetting().isEnabled()
                ? ColorPalette.getAccentColor().getRGB()
                : ambience.getSkyColorSetting().getColor().getRGB());
        }
    }

    @Inject(method={"getCloudsColor"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetCloudsColor(float f, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        Ambience ambience = RockstarClient.create().getModuleRegistry().getModule(Ambience.class);
        if (ambience.isEnabled() && ambience.getCloudsChangeOption().isSelected()) {
            callbackInfoReturnable.setReturnValue(ambience.getThemeSyncSetting().isEnabled()
                ? ColorPalette.getAccentColor().getRGB()
                : ambience.getCloudColorSetting().getColor().getRGB());
        }
    }
}
