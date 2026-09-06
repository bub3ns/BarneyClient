/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.DrawContext
 *  net.minecraft.MultiplayerServerListWidget$ServerEntry
 *  net.minecraft.MultiplayerServerListWidget$Entry
 *  net.minecraft.ServerInfo
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen.multiplayer;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={MultiplayerServerListWidget.ServerEntry.class})
public abstract class MultiplayerServerListWidgetServerEntryMixin
extends MultiplayerServerListWidget.Entry {
    protected MultiplayerServerListWidgetServerEntryMixin() {
    }
}
