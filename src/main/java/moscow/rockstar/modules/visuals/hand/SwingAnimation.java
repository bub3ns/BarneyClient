/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Arm
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.UseAction
 *  net.minecraft.MatrixStack
 *  org.joml.Quaternionf
 */
package moscow.rockstar.modules.visuals.hand;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.modules.visuals.hand.HandSwingState;
import moscow.rockstar.settings.ActionSetting;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.ui.screens.ColorPresetsScreen;
import net.minecraft.entity.Entity;
import net.minecraft.util.Arm;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import pyrock.events.render.HandRenderEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Swing Animation", category=ModuleCategory.VISUALS, disableLocked=true, description="modules.descriptions.swing_animation")
public class SwingAnimation
extends Module {
    private BooleanSetting onlyAura;
    private ActionSetting openMenu;
    private final EventListener<HandRenderEvent> onHandRenderEvent = handRenderEvent -> {
        if (this.isSwingItem(handRenderEvent.getItemStack()) && handRenderEvent.getArm() == SwingAnimation.minecraftClient.options.getMainArm().getValue()) {
            MatrixStack class_45872 = handRenderEvent.getMatrices();
            float f = handRenderEvent.getSwingProgress();
            float f2 = handRenderEvent.getEquipProgress();
            HandSwingState handSwingState = RockstarClient.create().getHandSwingPresetManager().interpolateSwingState(f);
            if (handRenderEvent.getArm() == Arm.LEFT) {
                handSwingState = this.getSwingAnimation(handSwingState);
            }
            class_45872.translate(handSwingState.getAnchorX(), handSwingState.getAnchorY(), handSwingState.getAnchorZ());
            class_45872.translate(handSwingState.getMoveX(), handSwingState.getMoveY(), handSwingState.getMoveZ());
            class_45872.multiply(new Quaternionf().rotationXYZ((float)Math.toRadians(handSwingState.getRotateX()), (float)Math.toRadians(handSwingState.getRotateY()), (float)Math.toRadians(handSwingState.getRotateZ())));
            class_45872.translate(-handSwingState.getAnchorX(), -handSwingState.getAnchorY(), -handSwingState.getAnchorZ());
            handRenderEvent.cancel();
        }
    };

    public SwingAnimation() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.onlyAura = new BooleanSetting(this, "modules.settings.swing_animation.only_aura");
        this.openMenu = new ActionSetting(this, "modules.settings.swing_animation.open_menu").withAction(() -> minecraftClient.setScreen(new ColorPresetsScreen()));
    }

    private HandSwingState getSwingAnimation(HandSwingState handSwingState) {
        return new HandSwingState(-handSwingState.getAnchorX(), handSwingState.getAnchorY(), handSwingState.getAnchorZ(), -handSwingState.getMoveX(), handSwingState.getMoveY(), handSwingState.getMoveZ(), handSwingState.getRotateX(), -handSwingState.getRotateY(), -handSwingState.getRotateZ());
    }

    public boolean isSwingItem(ItemStack class_17992) {
        Aura aura = RockstarClient.create().getModuleRegistry().getModule(Aura.class);
        Entity class_12972 = RockstarClient.create().getFriendManager().getTargetEntity();
        Item class_17922 = class_17992.getItem();
        if (this.onlyAura.isEnabled() && (!aura.isEnabled() || class_12972 == null)) {
            return false;
        }
        return class_17922 != Items.AIR && class_17922 != Items.FILLED_MAP && class_17922 != Items.CROSSBOW && class_17922 != Items.BOW && class_17922 != Items.TRIDENT && class_17922.getUseAction(class_17992) != UseAction.DRINK && class_17922.getUseAction(class_17992) != UseAction.EAT;
    }
}

