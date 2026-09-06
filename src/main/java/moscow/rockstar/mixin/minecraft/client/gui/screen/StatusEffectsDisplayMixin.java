/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.StatusEffectsDisplay
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import java.util.Collection;
import moscow.rockstar.entity.utility.EntityUtils;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={StatusEffectsDisplay.class})
public class StatusEffectsDisplayMixin {
    @ModifyVariable(method={"drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;II)V"}, at=@At(value="STORE"), ordinal=0)
    private Collection<StatusEffectInstance> rockstar$filterStatusEffects(Collection<StatusEffectInstance> collection) {
        return this.filterCollection(collection);
    }

    @Unique
    private Collection<StatusEffectInstance> filterCollection(Collection<StatusEffectInstance> collection) {
        return collection.stream().filter(class_12932 -> !EntityUtils.isEffectActive(class_12932)).toList();
    }
}

