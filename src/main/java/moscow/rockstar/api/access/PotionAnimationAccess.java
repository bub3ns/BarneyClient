package moscow.rockstar.api.access;

import moscow.rockstar.ui.animation.Animation;

/** Access to potion-overlay animation state added by the HUD mixin. */
public interface PotionAnimationAccess {
    Animation rockstar$getAnimPotion();

    Animation rockstar$getTimeAnimation();
}
