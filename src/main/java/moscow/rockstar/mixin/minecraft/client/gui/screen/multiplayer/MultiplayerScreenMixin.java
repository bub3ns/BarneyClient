/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 *  net.minecraft.ButtonWidget
 *  net.minecraft.MultiplayerServerListWidget
 *  net.minecraft.MultiplayerServerListWidget$ServerEntry
 *  net.minecraft.MultiplayerServerListWidget$Entry
 *  net.minecraft.Screen
 *  net.minecraft.MultiplayerScreen
 *  net.minecraft.ServerList
 *  net.minecraft.ServerInfo
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen.multiplayer;

import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={MultiplayerScreen.class})
public abstract class MultiplayerScreenMixin
extends Screen {
    protected MultiplayerScreenMixin(Text class_25612) {
        super(class_25612);
    }
}
