package moscow.rockstar.ui.hud;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.modules.visuals.hud.DynamicIslandHud;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingOwner;

/**
 * Facade over the live {@link DynamicIslandHud}.
 *
 * <p>There is no counterpart to this class in the original client: the island element (obf
 * rockstar/ilIlil/IiIiiIIII) owns the {@code "hud.dynamic_island.statuses"} setting and the entry
 * list itself, and pyrock reaches it through {@code Ii.I().I().I()} (the HUD registry's public
 * island field). The remap invented this manager, hung the setting off it, and rewrote
 * {@code pyrock/classes/PyDynamicIsland} and {@code PyIslandStatus} - which are supposed to be
 * byte-identical - to call it. {@code RockstarClient} also creates it, before the HUD registry.</p>
 *
 * <p>Rather than keep a second, parallel status list, this class now forwards everything to the
 * island element, so the setting is owned by the {@code HudElement} exactly as in the original and
 * scripts and the island see one list. The element attaches itself in its constructor.</p>
 */
public final class DynamicIslandManager implements SettingOwner {
    private static volatile DynamicIslandHud island;

    private final List<Setting> settings = new ArrayList<Setting>();
    /**
     * Only used while no island element exists (i.e. the HUD registry never constructed one). It
     * keeps script status registration from throwing; those entries are simply never drawn, which
     * is what happens today.
     */
    private MultiBooleanSetting detachedStatuses;

    /** Called by {@link DynamicIslandHud}'s constructor. */
    public static void attach(DynamicIslandHud hud) {
        island = hud;
    }

    /** The live island element, or null when the HUD registry has not created one. */
    public static DynamicIslandHud island() {
        return island;
    }

    public MultiBooleanSetting getStatusSetting() {
        DynamicIslandHud hud = island;
        if (hud != null) {
            return hud.getStatuses();
        }
        if (this.detachedStatuses == null) {
            this.detachedStatuses = new MultiBooleanSetting(this, "hud.dynamic_island.statuses").preserveOrder();
        }
        return this.detachedStatuses;
    }

    /** ORIGINAL: IiIiiIIII#I()Ljava/util/List; - every entry registered on the status setting. */
    public List<DynamicIslandEntry> getEntries() {
        DynamicIslandHud hud = island;
        if (hud != null) {
            return hud.entries();
        }
        ArrayList<DynamicIslandEntry> list = new ArrayList<DynamicIslandEntry>();
        for (MultiBooleanSetting.Option option : this.getStatusSetting().getOptions()) {
            if (!(option instanceof DynamicIslandEntry)) {
                continue;
            }
            list.add((DynamicIslandEntry)option);
        }
        return list;
    }

    /**
     * ORIGINAL: {@code <T extends IiIiiIiii> T IiIiiIIII.I(T)} is the identity - an entry registers
     * itself with the status setting from {@code MultiBooleanSetting.Option}'s constructor, so
     * there is nothing left to do here.
     */
    public void add(DynamicIslandEntry entry) {
    }

    /** ORIGINAL: IiIiiIIII#I(IiIiiIiii)Z. */
    public boolean remove(DynamicIslandEntry entry) {
        DynamicIslandHud hud = island;
        if (hud != null) {
            return hud.remove(entry);
        }
        if (entry == null) {
            return false;
        }
        this.getStatusSetting().getSelectedOptions().remove(entry);
        return this.getStatusSetting().getOptions().remove(entry);
    }

    public boolean isExpanded() {
        DynamicIslandHud hud = island;
        return hud != null && hud.isExpanded();
    }

    public void setExpanded(boolean expanded) {
        DynamicIslandHud hud = island;
        if (hud != null) {
            hud.setExpanded(expanded);
        }
    }

    /** ORIGINAL: the island's expansion animation value, which PyIslandStatus#extending() returns. */
    public float getExpansionProgress() {
        DynamicIslandHud hud = island;
        return hud == null ? 0.0f : hud.getExpandAnim().getValue();
    }

    /**
     * The expansion progress is derived from the island's animation, so there is nothing to set.
     * Kept because the previous stub exposed it.
     */
    public void setExpansionProgress(float expansionProgress) {
    }

    @Override
    public List<Setting> getSettings() {
        return this.settings;
    }
}
