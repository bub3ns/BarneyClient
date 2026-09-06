/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Window
 *  net.minecraft.ResourcePack
 *  net.minecraft.InputSupplier
 *  net.minecraft.Icons
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package moscow.rockstar.mixin.minecraft.client.util;

import java.io.InputStream;
import java.util.List;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.InputSupplier;
import net.minecraft.client.util.Icons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={Window.class})
public class WindowMixin {
    @Redirect(method={"setIcon"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/util/Icons;getIcons(Lnet/minecraft/resource/ResourcePack;)Ljava/util/List;"))
    public List<InputSupplier<InputStream>> setCustomIcon(Icons class_85182, ResourcePack class_32622) {
        if (RockstarClient.create().isPanicMode()) {
            try {
                return class_85182.getIcons(class_32622);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        InputStream inputStream = RockstarClient.class.getResourceAsStream("/assets/%s/icons/window/icon16x16.png".formatted(RockstarClient.RESOURCE_NAMESPACE));
        InputStream inputStream2 = RockstarClient.class.getResourceAsStream("/assets/%s/icons/window/icon32x32.png".formatted(RockstarClient.RESOURCE_NAMESPACE));
        return List.of(() -> inputStream, () -> inputStream2);
    }
}
