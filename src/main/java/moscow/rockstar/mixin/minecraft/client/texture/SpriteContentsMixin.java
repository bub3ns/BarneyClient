/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.NativeImage
 *  net.minecraft.NativeImage$Format
 *  net.minecraft.Identifier
 *  net.minecraft.ResourceMetadata
 *  net.minecraft.SpriteContents
 *  net.minecraft.SpriteDimensions
 *  net.minecraft.ColorHelper
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.texture;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SpriteContents.class})
public class SpriteContentsMixin {
    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void rockstar$bleedAlpha(Identifier class_29602, SpriteDimensions class_77712, NativeImage LootTableData, ResourceMetadata class_73682, CallbackInfo callbackInfo) {
        int n;
        int n2;
        int n3;
        if (LootTableData.getFormat() != NativeImage.Format.RGBA) {
            return;
        }
        int n4 = LootTableData.getWidth();
        int n5 = LootTableData.getHeight();
        if (n4 <= 0 || n5 <= 0) {
            return;
        }
        int[] nArray = new int[n4 * n5];
        boolean bl = false;
        for (n3 = 0; n3 < n5; ++n3) {
            for (n2 = 0; n2 < n4; ++n2) {
                nArray[n3 * n4 + n2] = n = LootTableData.getColorArgb(n2, n3);
                bl |= n >>> 24 == 0;
            }
        }
        if (!bl) {
            return;
        }
        for (n3 = 0; n3 < n5; ++n3) {
            for (n2 = 0; n2 < n4; ++n2) {
                if (nArray[n3 * n4 + n2] >>> 24 != 0) continue;
                n = 0;
                int n6 = 0;
                int n7 = 0;
                int n8 = 0;
                for (int i = -1; i <= 1; ++i) {
                    int n9 = n3 + i;
                    if (n9 < 0 || n9 >= n5) continue;
                    for (int j = -1; j <= 1; ++j) {
                        int n10;
                        int n11 = n2 + j;
                        if (n11 < 0 || n11 >= n4 || j == 0 && i == 0 || (n10 = nArray[n9 * n4 + n11]) >>> 24 == 0) continue;
                        int n12 = j == 0 || i == 0 ? 2 : 1;
                        n += ColorHelper.getRed((int)n10) * n12;
                        n6 += ColorHelper.getGreen((int)n10) * n12;
                        n7 += ColorHelper.getBlue((int)n10) * n12;
                        n8 += n12;
                    }
                }
                if (n8 == 0) continue;
                LootTableData.setColorArgb(n2, n3, ColorHelper.getArgb((int)0, (int)(n / n8), (int)(n6 / n8), (int)(n7 / n8)));
            }
        }
    }
}

