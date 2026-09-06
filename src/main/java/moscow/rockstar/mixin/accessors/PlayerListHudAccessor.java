/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.Text
 *  net.minecraft.PlayerListHud
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.accessors;

import javax.annotation.Nullable;
import net.minecraft.text.Text;
import net.minecraft.client.gui.hud.PlayerListHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={PlayerListHud.class})
public interface PlayerListHudAccessor {
    @Accessor(value="header")
    @Nullable
    public Text getHeader();
}

