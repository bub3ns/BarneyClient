package moscow.rockstar.modules.visuals.hud;

import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.factory.UiNodeFactory;
import moscow.rockstar.ui.hud.DynamicIslandEntry;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.text.EmojiTextNode;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.render.colors.GradientColors;
import pyrock.utility.render.ColorRGBA;

/**
 * The island's default status - the logo plus the island title.
 * 1:1 with rockstar/ilIlil/IiIiiiiIi, entry [8] of
 * rockstar/ilIlil/IiIiiiIIi#I(Lrockstar/ilIlil/IIiiiiiii;)V, registered {@code .alwaysEnabled()}.
 *
 * <p>This is the entry that keeps the island on screen at all times: its {@code canShow()} is a
 * literal {@code return true} in the original, and the registration marks it always-enabled, so it
 * can never be deselected. Every other built-in status is conditional.
 *
 * <p>ORIGINAL members, all reproduced:
 * <pre>
 *   &lt;init&gt;(IIiiiiiii)                     -&gt; super(statuses, "default")
 *   content(IiIiiIIII)Lrockstar/ilIlil/iiI; -&gt; the cached row node
 *   canShow()Z                              -&gt; true            (expressed as isVisible(), see
 *                                              DynamicIslandEntry's class javadoc)
 *   I()F / i()F                             -&gt; the two lambda offsets (+10 / -10 * (1 - anim))
 *   I(IiIiiIIII,III,iii)V                   -&gt; the paint lambda
 * </pre>
 */
public class IslandDefaultStatus extends DynamicIslandEntry {
    private UiNode contentNode;

    public IslandDefaultStatus(MultiBooleanSetting statuses) {
        super(statuses, "default");
    }

    /**
     * ORIGINAL: {@code content(Lrockstar/ilIlil/IiIiiIIII;)Lrockstar/ilIlil/iiI;}
     * <pre>
     *   IiIiiIiIi.I(15F, IIII.I(0F, 5F, 0F, 4F), 4F).I(IIi.i).I(paint)
     *   row.I(IiIiiIiIi.I("logo", 8F, IiiiiIIIi::i, this::i))
     *   row.I(new IiIiiIIiI(IIiIiI.i.I(7F), island::I, IiiiiIIIi::iI, this::I))
     * </pre>
     */
    @Override
    public UiNode content(DynamicIslandHud island) {
        if (this.contentNode == null) {
            Component row = UiNodeFactory.createRowComponent(15.0f, Insets.of(0.0f, 5.0f, 0.0f, 4.0f), 4.0f)
                    .alignment(Alignment.CENTER)
                    .renderHook((drawContext, component) -> {
                        ColorRGBA accent = ColorPalette.getAccentColor();
                        ColorRGBA solid = accent.withAlpha(71.4f);
                        ColorRGBA clear = accent.withAlpha(0.0f);
                        float radius = island.getRadiusAnim().getValue();
                        drawContext.drawSquircle(component.x(), component.y(), component.w() * 0.69f,
                                component.h(), 2.0f, WidgetState.left(radius, radius),
                                new GradientColors(solid, solid, clear, clear));
                    });
            row.add(UiNodeFactory.createIconNode("logo", 8.0f, ColorPalette::getAccentColor,
                    this::iconOffset));
            row.add(new EmojiTextNode(Font.MEDIUM.metrics(7.0f), island::islandTitle,
                    ColorPalette::getPrimaryTextColor, this::titleOffset));
            this.contentNode = row;
        }
        return this.contentNode;
    }

    /** ORIGINAL: the private synthetic {@code i()F} bound to the "logo" icon node. */
    private float iconOffset() {
        return -10.0f * (1.0f - this.animation.getValue());
    }

    /** ORIGINAL: the private synthetic {@code I()F} bound to the title text node. */
    private float titleOffset() {
        return 10.0f * (1.0f - this.animation.getValue());
    }

    /** ORIGINAL: {@code public boolean canShow() { return true; }}. */
    @Override
    public boolean isVisible() {
        return true;
    }
}
