package moscow.rockstar.modules.visuals.effects.particles;

import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.util.Timer;
import net.minecraft.util.math.Vec3d;

public class ParticleInstance {
    public Vec3d currentPosition;
    public Vec3d previousPosition;
    public Vec3d nextPosition;
    public Vec3d nextMovement;
    public Vec3d primaryVelocity;
    public Vec3d secondaryVelocity;
    public final long LIFETIME_MILLIS;
    public float animationProgress;
    public boolean persistent;
    public boolean opening;
    public boolean closing;
    public final Timer lifetimeTimer = new Timer();
    public final Animation animation = new Animation(300L, Easing.easeInOutCubicBezier);
    public final Animation openingAnimation = new Animation(200L, 1.0f, Easing.easeInOutCubicBezier);
    public final Animation closingAnimation = new Animation(220L, Easing.easeInOutCubicBezier);

    public ParticleInstance(Vec3d nextPosition, Vec3d nextMovement, Vec3d primaryVelocity,
                            Vec3d secondaryVelocity, long lifetimeMillis, float animationProgress) {
        this.nextPosition = nextPosition;
        this.nextMovement = nextMovement;
        this.primaryVelocity = primaryVelocity.multiply(0.04f);
        this.secondaryVelocity = secondaryVelocity.multiply(0.04f);
        this.LIFETIME_MILLIS = lifetimeMillis;
        this.animationProgress = animationProgress;
        this.previousPosition = nextMovement;
        this.currentPosition = nextPosition;
        this.animation.setDuration(1000L);
    }

    public boolean isAlive() {
        return this.persistent || this.lifetimeTimer.hasElapsed(this.LIFETIME_MILLIS);
    }

    public void updatePosition() {
        this.currentPosition = this.nextPosition;
        this.previousPosition = this.nextMovement;
        this.nextPosition = this.nextPosition.add(this.primaryVelocity);
        this.nextMovement = this.nextMovement.add(this.secondaryVelocity);
        this.primaryVelocity = this.primaryVelocity.multiply(0.98);
        this.secondaryVelocity = this.secondaryVelocity.multiply(0.98);
    }
}
