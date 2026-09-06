/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.DrawContext
 *  net.minecraft.BossBarHud
 *  net.minecraft.ClientBossBar
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.overlay.Removals;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BossBarHud.class})
public class BossBarHudMixin
implements ClientAccess {
    @Shadow
    @Final
    private Map<UUID, ClientBossBar> bossBars;
    @Unique
    private static final Pattern PVP_TIME_PATTERN = Pattern.compile("(\\d+)\\s*(?:[\u0441c][\u0435e][\u043ak]\\.?|[\u0441c][\u0435e][\u043ak][\u0443y]?[\u043dnh][\u0434d]?)(?=$|\\s|\\p{Punct})", 322);
    @Unique
    private static final Pattern ROCKSTAR_PVP_TIME_LOOSE = Pattern.compile("(\\d{1,4})[^\\d]{0,4}?[\u0441c][\u0435e][\u043ak]", 66);
    @Unique
    private static final Pattern ROCKSTAR_NUMBER = Pattern.compile("\\d{1,4}");
    @Unique
    private static final String ROCKSTAR_CYRILLIC = "\u0430\u0441\u0435\u043e\u0440\u0445\u0443\u043a\u043d\u0432\u0442\u043c";
    @Unique
    private static final String ROCKSTAR_LATIN = "aceopxykhbtm";
    private static final String FILTERED_TEXT = "\ub445\ua223\ua203\ub444\ua223\ua205";

    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void onRenderHead(DrawContext ServerConfigException, CallbackInfo callbackInfo) {
        int n = 0;
        for (ClientBossBar bossBar : this.bossBars.values()) {
            int n2;
            String string;
            if (bossBar.getName() == null
                || !BossBarHudMixin.rockstar$isPvpBar(string = BossBarHudMixin.rockstar$plain(bossBar.getName().getString()))
                || (n2 = BossBarHudMixin.rockstar$seconds(string)) <= n) continue;
            n = n2;
        }
        ServerDetector.setEnabled(n > 0);
        ServerDetector.setRequestedServerNumber(n);
    }

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void render(CallbackInfo callbackInfo) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals != null && removals.isEnabled() && removals.getBossBar().isSelected()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"render"}, at={@At(value="RETURN")})
    private void onRenderReturn(DrawContext ServerConfigException, CallbackInfo callbackInfo) {
        // Kept as a separate injection point so the original HUD render stack is untouched.
    }

    @Unique
    private static String rockstar$plain(String string) {
        StringBuilder stringBuilder = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c >= '\ue000' && c <= '\uf8ff' || Character.getType(c) == 16) continue;
            stringBuilder.append(Character.isSpaceChar(c) ? (char)' ' : (char)c);
        }
        return stringBuilder.toString();
    }

    @Unique
    private static boolean rockstar$isPvpBar(String string) {
        String string2 = string.toLowerCase(Locale.ROOT);
        if (string2.contains("\u0431\u043e\u0439") || string2.contains("\u0431\u043e\u044e") || string2.contains("\u043f\u0432\u043f")) {
            return true;
        }
        StringBuilder stringBuilder = new StringBuilder(string2.length());
        for (int i = 0; i < string2.length(); ++i) {
            char c = string2.charAt(i);
            int n = ROCKSTAR_CYRILLIC.indexOf(c);
            stringBuilder.append(n < 0 ? c : ROCKSTAR_LATIN.charAt(n));
        }
        return stringBuilder.indexOf("pvp") >= 0;
    }

    @Unique
    private static int rockstar$seconds(String string) {
        Matcher matcher = PVP_TIME_PATTERN.matcher(string);
        if (matcher.find()) {
            return BossBarHudMixin.rockstar$parse(matcher.group(1));
        }
        Matcher matcher2 = ROCKSTAR_PVP_TIME_LOOSE.matcher(string);
        if (matcher2.find()) {
            return BossBarHudMixin.rockstar$parse(matcher2.group(1));
        }
        Matcher matcher3 = ROCKSTAR_NUMBER.matcher(string);
        if (!matcher3.find()) {
            return -1;
        }
        String string2 = matcher3.group();
        return matcher3.find() ? -1 : BossBarHudMixin.rockstar$parse(string2);
    }

    @Unique
    private static int rockstar$parse(String string) {
        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException numberFormatException) {
            return -1;
        }
    }
}
