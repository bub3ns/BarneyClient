package moscow.rockstar.modules.visuals.hud;

import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingGroupHeader;
import pyrock.utility.render.ColorRGBA;

/**
 * The "PvP mode" island status. 1:1 with rockstar/ilIlil/IiiIIIIii, entry [4] of
 * rockstar/ilIlil/IiIiiiIIi#I(Lrockstar/ilIlil/IIiiiiiii;)V.
 *
 * <p>ORIGINAL, in full:
 * <pre>
 *   IiiIIIIii(IIiiiiiii s)       { super(s, "pvp"); }
 *   prepare(IiIiiIIII island)    { this.I("s", iIIIiiiII.I,
 *                                          IiIiIIII.I("hud.pvp_mode"),
 *                                          new ColorRGBA(185, 28, 28));
 *                                  super.prepare(island); }
 *   canShow()                    { return iIIIiiiII.I &amp;&amp; IiiiiiiII.i(); }
 * </pre>
 * {@code iIIIiiiII} is {@link ServerDetector} (its two lombok setters {@code I(Z)V} / {@code I(I)V}
 * pin {@code I:Z} to {@code enabled} and {@code I:I} to {@code requestedServerNumber});
 * {@code IiiiiiiII.i()} is {@link EntityUtils#isClientWorldReady()} (player != null &amp;&amp; world != null);
 * {@code IiIiiiIiI} is {@link SettingGroupHeader} and its 4-arg
 * {@code I(String,int,String,ColorRGBA)} forwards to {@code I("", s, n, label, color)} - i.e.
 * {@link SettingGroupHeader#setHeaderInfo(String, int, String, ColorRGBA)}.
 */
public class IslandPvpStatus extends SettingGroupHeader {
    public IslandPvpStatus(MultiBooleanSetting statuses) {
        super(statuses, "pvp");
    }

    @Override
    public void prepare(DynamicIslandHud island) {
        this.setHeaderInfo("s", ServerDetector.requestedServerNumber,
                Localization.translate("hud.pvp_mode"), new ColorRGBA(185.0f, 28.0f, 28.0f));
        super.prepare(island);
    }

    /** ORIGINAL: {@code canShow()}; the remap expresses the hook as {@code isVisible()}. */
    @Override
    public boolean isVisible() {
        return ServerDetector.enabled && EntityUtils.isClientWorldReady();
    }
}
