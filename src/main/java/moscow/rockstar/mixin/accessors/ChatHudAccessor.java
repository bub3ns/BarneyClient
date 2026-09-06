/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatHudLine
 *  net.minecraft.ChatHudLine$Visible
 *  net.minecraft.ChatHud
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package moscow.rockstar.mixin.accessors;

import java.util.List;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ChatHud.class})
public interface ChatHudAccessor {
    @Accessor(value="visibleMessages")
    public List<ChatHudLine.Visible> getVisibleMessages();

    @Accessor(value="messages")
    public List<ChatHudLine> getMessages();

    @Invoker(value="getMessageLineIndex")
    public int invokeGetMessageLineIndex(double var1, double var3);

    @Invoker(value="toChatLineX")
    public double invokeToChatLineX(double var1);

    @Invoker(value="toChatLineY")
    public double invokeToChatLineY(double var1);
}

