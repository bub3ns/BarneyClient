/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 *  net.minecraft.ScoreboardObjective
 *  net.minecraft.Scoreboard
 *  net.minecraft.TextRenderer
 *  net.minecraft.DrawContext
 *  net.minecraft.MathHelper
 *  net.minecraft.PlayerListHud
 *  net.minecraft.PlayerListEntry
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.visuals.camera.Beautifully;
import moscow.rockstar.modules.visuals.hud.NameProtect;
import moscow.rockstar.server.ServerDetector;
import net.minecraft.text.Text;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerListHud.class})
public class PlayerListHudMixin {
    @Unique
    private static int entryCount;

    @Inject(method={"collectPlayerEntries"}, at={@At(value="RETURN")}, cancellable=true)
    private void filterStreamerTabEntries(CallbackInfoReturnable<List<PlayerListEntry>> callbackInfoReturnable) {
        entryCount = ((List)callbackInfoReturnable.getReturnValue()).size();
        NameProtect nameProtect = RockstarClient.create().getModuleRegistry().getModule(NameProtect.class);
        if (nameProtect == null || !nameProtect.isEnabled() || !nameProtect.getStreamerModeSetting().isEnabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getSession() == null) {
            return;
        }
        ArrayList<PlayerListEntry> arrayList = new ArrayList<PlayerListEntry>();
        for (PlayerListEntry ServerSamplerSource : callbackInfoReturnable.getReturnValue()) {
            String string = ServerSamplerSource.getProfile().getName();
            if (!string.equalsIgnoreCase(client.getSession().getUsername()) && this.shouldHideStreamerTabEntry(ServerSamplerSource)) continue;
            arrayList.add(ServerSamplerSource);
        }
        callbackInfoReturnable.setReturnValue(arrayList);
        entryCount = arrayList.size();
    }

    @ModifyConstant(method={"render"}, constant={@Constant(intValue=20)})
    private int rockstar$rowsPerColumn(int n) {
        int n2 = Beautifully.getTabColumnCount();
        if (n2 <= 0 || entryCount <= 0) {
            return n;
        }
        return Math.max(1, MathHelper.ceil((float)((float)entryCount / (float)n2)));
    }

    @Inject(method={"getPlayerName"}, at={@At(value="RETURN")}, cancellable=true)
    private void cleanStreamerTabName(PlayerListEntry ServerSamplerSource, CallbackInfoReturnable<Text> callbackInfoReturnable) {
        NameProtect nameProtect = RockstarClient.create().getModuleRegistry().getModule(NameProtect.class);
        if (nameProtect == null || !nameProtect.isEnabled() || !nameProtect.getStreamerModeSetting().isEnabled()) {
            return;
        }
        callbackInfoReturnable.setReturnValue(Text.literal(nameProtect.replacePlayerOrServerName(ServerSamplerSource.getProfile().getName())));
    }

    @Unique
    private boolean shouldHideStreamerTabEntry(PlayerListEntry ServerSamplerSource) {
        String string = ServerSamplerSource.getProfile().getName();
        int n = Objects.hash(string.toLowerCase(Locale.ROOT), ServerSamplerSource.getProfile().getId(), ServerDetector.getConnectedServerAddress());
        return Math.floorMod(n, 4) == 0;
    }
}
