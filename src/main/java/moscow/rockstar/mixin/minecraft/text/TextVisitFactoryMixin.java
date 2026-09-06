/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.TextVisitFactory
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package moscow.rockstar.mixin.minecraft.text;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.hud.NameProtect;
import moscow.rockstar.render.text.TextCaptureController;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={TextVisitFactory.class})
public class TextVisitFactoryMixin
implements ClientAccess {
    @ModifyArg(method={"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"}, at=@At(value="INVOKE", target="Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", ordinal=0), index=0)
    private static String patchName(String string) {
        NameProtect nameProtect = RockstarClient.create().getModuleRegistry().getModule(NameProtect.class);
        if (nameProtect.isEnabled() && TextVisitFactoryMixin.minecraftClient.world != null && TextVisitFactoryMixin.minecraftClient.player != null) {
            if (TextCaptureController.isCaptureAvailable()) {
                return string;
            }
            return nameProtect.replaceProtectedText(string);
        }
        return string;
    }
}
