/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.notifications;

import lombok.Generated;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.util.Timer;
import pyrock.utility.render.CustomDrawContext;

public abstract class TimedNotification {
    private final Timer lifetimeTimer = new Timer();
    public final Animation fadeAnimation;
    public final Animation contentAnimation;
    public final Animation slideAnimation;
    public final long durationMillis;

    public TimedNotification(long l) {
        this.durationMillis = l;
        this.fadeAnimation = new Animation(300L, Easing.easeOutBack);
        this.slideAnimation = new Animation(300L, Easing.easeOutBackSoft);
        this.contentAnimation = new Animation(300L, Easing.easeOutOvershootSoft);
    }

    public abstract void render(CustomDrawContext var1, float var2);

    public float getHeight() {
        return 30.0f;
    }

    public final void updateVisibility() {
        this.fadeAnimation.update(this.lifetimeTimer.hasElapsed(this.durationMillis) ? 0.0f : 1.0f);
    }

    public final boolean isFinished() {
        return this.fadeAnimation.getValue() == 0.0f && this.lifetimeTimer.hasElapsed(this.durationMillis);
    }

    @Generated
    public Timer getLifetimeTimer() {
        return this.lifetimeTimer;
    }

    @Generated
    public Animation getFadeAnimation() {
        return this.fadeAnimation;
    }

    @Generated
    public Animation getContentAnimation() {
        return this.contentAnimation;
    }

    @Generated
    public Animation getSlideAnimation() {
        return this.slideAnimation;
    }

    @Generated
    public long getDurationMillis() {
        return this.durationMillis;
    }
}

