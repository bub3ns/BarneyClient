package moscow.rockstar.ui.hud;

import moscow.rockstar.modules.visuals.hud.DynamicIslandHud;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.UiNode;
import pyrock.utility.render.ColorRGBA;

/**
 * Base of every dynamic-island status surface. 1:1 with rockstar/ilIlil/IiIiiIiii
 * (abstract, extends IIiiiiiii$I = MultiBooleanSetting.Option, fields size / animation / element).
 *
 * <p>Two deliberate deviations from the original, both forced by classes this group does not own:
 * <ul>
 *   <li>The original declares {@code public abstract boolean canShow()}. The remap renamed that hook
 *       to {@code isVisible()} and made it concrete; {@code SettingGroupHeader},
 *       {@code MiningStatusHeader} and {@code ServerWaypointTracker} all override
 *       {@code isVisible()} with {@code @Override}. {@code canShow()} is therefore kept as the name
 *       the island calls and delegates to {@code isVisible()}, and the class stays concrete.</li>
 *   <li>The original's 3-arg constructor flag selects the translation-key prefix, it is NOT an
 *       "always enabled" flag; the previous stub called {@code setAlwaysEnabled(flag)}, which is why
 *       {@code PyIslandStatus}'s {@code super(setting, name, false)} produced the wrong key.</li>
 * </ul>
 */
public class DynamicIslandEntry extends MultiBooleanSetting.Option {
    protected DynamicIslandSize size = new DynamicIslandSize(48.0f, 15.0f, 7.0f);
    protected final Animation animation = new Animation(500L, Easing.easeOutOvershootSoft);
    private UiNode element;

    public DynamicIslandEntry(MultiBooleanSetting parent, String name) {
        this(parent, name, true);
    }

    /**
     * @param builtIn ORIGINAL {@code IiIiiIiii(IIiiiiiii, String, boolean)}: when true the key is
     *                prefixed with {@code "hud.dynamic_island.statuses."}, otherwise the raw name is
     *                sanitised. Scripts pass false.
     */
    public DynamicIslandEntry(MultiBooleanSetting parent, String name, boolean builtIn) {
        super(parent, builtIn ? "hud.dynamic_island.statuses." + name : DynamicIslandEntry.sanitizeName(name));
        this.select();
    }

    /** ORIGINAL: {@code public final iiI element(IiIiiIIII)} - lazily creates the entry's flow node. */
    public final UiNode element(DynamicIslandHud island) {
        if (this.element == null) {
            this.element = new DynamicIslandEntryNode(this, island);
        }
        return this.element;
    }

    public boolean isExpandable() {
        return false;
    }

    public boolean drawsOwnBackground() {
        return false;
    }

    /** ORIGINAL: {@code prepare(Lrockstar/ilIlil/IiIiiIIII;)V} */
    public void prepare(DynamicIslandHud island) {
    }

    /** ORIGINAL: {@code content(Lrockstar/ilIlil/IiIiiIIII;)Lrockstar/ilIlil/iiI;} */
    public UiNode content(DynamicIslandHud island) {
        return null;
    }

    /** ORIGINAL: {@code radius(Lrockstar/ilIlil/IiIiiIIII;)F} */
    public float radius(DynamicIslandHud island) {
        return 7.0f;
    }

    /** ORIGINAL: {@code render(Lrockstar/ilIlil/III;Lrockstar/ilIlil/IiIiiIIII;FFFFF)V} */
    public void render(RockstarDrawContext drawContext, DynamicIslandHud island,
                       float x, float y, float width, float height, float alpha) {
    }

    public void click(float mouseX, float mouseY, int button) {
    }

    /** ORIGINAL: {@code public abstract boolean canShow()}. See the class javadoc. */
    public boolean canShow() {
        return this.isVisible();
    }

    public boolean isVisible() {
        return true;
    }

    public ColorRGBA getColor() {
        return ColorPalette.getPanelColor();
    }

    private static String sanitizeName(String name) {
        return name == null || name.isBlank() ? "Script Status" : name.trim();
    }

    public DynamicIslandSize getSize() {
        return this.size;
    }

    public Animation getAnimation() {
        return this.animation;
    }

    public UiNode getElement() {
        return this.element;
    }
}
