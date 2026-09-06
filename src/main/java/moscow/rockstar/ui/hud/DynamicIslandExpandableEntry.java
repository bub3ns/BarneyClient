package moscow.rockstar.ui.hud;

import moscow.rockstar.settings.MultiBooleanSetting;

/**
 * Base of the dynamic-island statuses that expand into a card when clicked.
 * 1:1 with rockstar/ilIlil/IiIiiIIii (extends IiIiiIiii, adds only the two overrides below).
 *
 * <p>The original's {@code canShow()} override is expressed here as {@code isVisible()} because the
 * remap renamed that hook; see {@link DynamicIslandEntry#canShow()}.</p>
 */
public class DynamicIslandExpandableEntry extends DynamicIslandEntry {
    public DynamicIslandExpandableEntry(MultiBooleanSetting parent, String name) {
        super(parent, name);
    }

    @Override
    public boolean isExpandable() {
        return true;
    }

    /** ORIGINAL: {@code public boolean canShow() { return false; }}. */
    @Override
    public boolean isVisible() {
        return false;
    }
}
