package moscow.rockstar.modules.visuals.hud;

import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.util.Timer;

/** One value cell of a one-line HUD row. 1:1 with rockstar/ilIlil/IiiIIIiiI. */
public class HudRowEntry extends MultiBooleanSetting.Option {
    private String value = "?";
    private String copyValue = "";
    private final String suffix;
    private boolean copied;
    private final Timer copyTimer = new Timer();
    private final Animation entryAnimation = new Animation(300L, 0.0f, Easing.easeOutBack);
    private final Animation copiedAnimation = new Animation(500L, 0.0f, Easing.easeOutOvershootSoft);

    public HudRowEntry(MultiBooleanSetting parent, String name, String suffix) {
        super(parent, name);
        this.select();
        this.suffix = " " + suffix;
    }

    public HudRowEntry(MultiBooleanSetting parent, String name) {
        super(parent, name);
        this.select();
        this.suffix = "";
    }

    public void set(String value, String copyValue) {
        if (!this.isSelected()) {
            return;
        }
        this.value = value;
        this.copyValue = copyValue;
    }

    public void set(String value) {
        if (!this.isSelected()) {
            return;
        }
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public String getCopyValue() {
        return this.copyValue;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public boolean isCopied() {
        return this.copied;
    }

    public Timer getCopyTimer() {
        return this.copyTimer;
    }

    public Animation getEntryAnimation() {
        return this.entryAnimation;
    }

    public Animation getCopiedAnimation() {
        return this.copiedAnimation;
    }

    public HudRowEntry setCopied(boolean copied) {
        this.copied = copied;
        return this;
    }
}
