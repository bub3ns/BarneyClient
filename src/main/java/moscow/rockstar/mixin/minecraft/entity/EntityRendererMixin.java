/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.EntityRenderState
 *  net.minecraft.Entity
 *  net.minecraft.ArmorStandEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Text
 *  net.minecraft.EntityRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.entity;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.esp.entities.Nametags;
import moscow.rockstar.modules.visuals.world.WardenHelper;
import moscow.rockstar.render.esp.OverlayRegistry;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.client.render.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EntityRenderer.class})
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method={"getDisplayName"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderLabel(T t, CallbackInfoReturnable<Text> callbackInfoReturnable) {
        WardenHelper wardenHelper;
        ArmorStandEntity class_15312;
        if (t instanceof ArmorStandEntity) {
            class_15312 = (ArmorStandEntity)t;
            wardenHelper = RockstarClient.create().getModuleRegistry().getModule(WardenHelper.class);
            if (wardenHelper != null && wardenHelper.isWardenEntity(class_15312)) {
                callbackInfoReturnable.setReturnValue(null);
                return;
            }
        }
        if (!(t instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity class_16572 = (PlayerEntity)t;
        Nametags nametags = OverlayRegistry.getInstance().findOverlayByType(Nametags.class);
        if (nametags == null) {
            return;
        }
        if (!nametags.handlesEntity((Entity)class_16572)) {
            return;
        }
        callbackInfoReturnable.setReturnValue(null);
    }
}
