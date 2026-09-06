/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ResourceReload
 *  net.minecraft.SplashOverlay
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceReload;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={SplashOverlay.class})
public interface SplashOverlayAccessor {
    @Accessor(value="reload")
    public ResourceReload getReload();

    @Accessor(value="client")
    public MinecraftClient getClient();

    @Accessor(value="reloadCompleteTime")
    public long getReloadCompleteTime();

    @Accessor(value="reloadCompleteTime")
    public void setReloadCompleteTime(long var1);
}

