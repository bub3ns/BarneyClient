package moscow.rockstar.ui.animation;

import moscow.rockstar.util.Timer;
import pyrock.utility.render.Rect;

/**
 * Inertia / squash-stretch rectangle. 1:1 with rockstar/ilIlil/IiIIiIIii.
 *
 * <p>Field identity was resolved by disassembling
 * {@code IiIIiIIii#<init> ()V} and {@code IiIIiIIii#I (Lpyrock/utility/render/Rect;Z)V}
 * (CFR collapses the reused obfuscated identifiers).</p>
 */
public class InertiaRect
extends Rect {
    private Rect target = Rect.EMPTY;
    private Rect previous = Rect.EMPTY;
    private final Timer timer = new Timer();
    private float velocityX = 0.0f;
    private float velocityY = 0.0f;
    private float previousVelocityX = 0.0f;
    private float previousVelocityY = 0.0f;
    private float dragDistance = 0.0f;
    private float overshootX = 0.0f;
    private float overshootY = 0.0f;
    private final float bounceFactor = 0.8f;
    private final float overshootDecay = 8.0f;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float targetScaleX = 1.0f;
    private float targetScaleY = 1.0f;
    private boolean bounceX = false;
    private boolean bounceY = false;

    public void update(float x, float y, float width, float height) {
        this.update(new Rect(x, y, width, height), false);
    }

    public void update(float x, float y, float width, float height, boolean dragging) {
        this.update(new Rect(x, y, width, height), dragging);
    }

    public void update(Rect rect) {
        this.update(rect, false);
    }

    public void update(Rect rect, boolean dragging) {
        float offsetX;
        float offsetY;
        float scaledWidth;
        float scaledHeight;
        float stretch = 0.3f;
        float damping = 0.1f;
        float scaleLerp = 15.0f;
        float settle = 0.3f;
        float dt = Math.min((float)this.timer.getElapsedMillis() / 1000.0f, 0.033f);
        this.timer.reset();
        if (dt < 0.001f) {
            return;
        }
        this.target = rect;
        this.previousVelocityX = this.velocityX;
        this.previousVelocityY = this.velocityY;
        float deltaX = rect.getX() - this.previous.getX();
        float deltaY = rect.getY() - this.previous.getY();
        float distance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        this.dragDistance = dragging ? (this.dragDistance += distance) : 0.0f;
        this.velocityX = deltaX / dt;
        this.velocityY = deltaY / dt;
        this.velocityX = (float)((double)this.velocityX * Math.pow(damping, dt * 60.0f));
        this.velocityY = (float)((double)this.velocityY * Math.pow(damping, dt * 60.0f));
        float absVelocityX = Math.abs(this.velocityX);
        float absVelocityY = Math.abs(this.velocityY);
        float absPreviousVelocityX = Math.abs(this.previousVelocityX);
        float absPreviousVelocityY = Math.abs(this.previousVelocityY);
        boolean settled = !dragging || this.dragDistance > 5.0f;
        if (!dragging && settled) {
            float sign;
            float impulseX = Math.abs(this.velocityX - this.previousVelocityX);
            float impulseY = Math.abs(this.velocityY - this.previousVelocityY);
            if (absPreviousVelocityX > 100.0f && impulseX > 80.0f) {
                sign = Math.signum(this.previousVelocityX);
                this.overshootX = sign * absPreviousVelocityX * 0.8f * 0.01f;
            }
            if (absPreviousVelocityY > 100.0f && impulseY > 80.0f) {
                sign = Math.signum(this.previousVelocityY);
                this.overshootY = sign * absPreviousVelocityY * 0.8f * 0.01f;
            }
        }
        this.overshootX += (0.0f - this.overshootX) * dt * 8.0f;
        this.overshootY += (0.0f - this.overshootY) * dt * 8.0f;
        if (Math.abs(this.overshootX) < 0.1f) {
            this.overshootX = 0.0f;
        }
        if (Math.abs(this.overshootY) < 0.1f) {
            this.overshootY = 0.0f;
        }
        if (absPreviousVelocityX > 50.0f && absVelocityX < 20.0f && settled) {
            this.bounceX = true;
        }
        if (absPreviousVelocityY > 50.0f && absVelocityY < 20.0f && settled) {
            this.bounceY = true;
        }
        if ((absVelocityX > 5.0f || absVelocityY > 5.0f) && settled) {
            this.bounceX = false;
            this.bounceY = false;
            if (absVelocityX > absVelocityY) {
                this.targetScaleX = 1.0f + absVelocityX * stretch * 8.0E-4f;
                this.targetScaleY = Math.max(0.6f, 1.0f - absVelocityX * stretch * 0.002f);
            } else {
                this.targetScaleX = Math.max(0.6f, 1.0f - absVelocityY * stretch * 0.002f);
                this.targetScaleY = 1.0f + absVelocityY * stretch * 8.0E-4f;
            }
        } else if ((this.bounceX || this.bounceY) && settled) {
            if (this.bounceX) {
                this.targetScaleX = 1.0f - settle * 0.3f;
                this.targetScaleY = 1.0f + settle * 0.5f;
                if (Math.abs(this.scaleX - this.targetScaleX) < 0.05f && Math.abs(this.scaleY - this.targetScaleY) < 0.05f) {
                    this.bounceX = false;
                }
            }
            if (this.bounceY) {
                this.targetScaleX = 1.0f + settle * 0.5f;
                this.targetScaleY = 1.0f - settle * 0.3f;
                if (Math.abs(this.scaleX - this.targetScaleX) < 0.05f && Math.abs(this.scaleY - this.targetScaleY) < 0.05f) {
                    this.bounceY = false;
                }
            }
        } else {
            this.targetScaleX = 1.0f;
            this.targetScaleY = 1.0f;
        }
        this.scaleX += (this.targetScaleX - this.scaleX) * dt * scaleLerp;
        this.scaleY += (this.targetScaleY - this.scaleY) * dt * scaleLerp;
        scaledWidth = rect.getWidth() * this.scaleX;
        scaledHeight = rect.getHeight() * this.scaleY;
        if (absVelocityX > absVelocityY && absVelocityX > 5.0f && settled) {
            offsetX = this.velocityX > 0.0f ? rect.getWidth() - scaledWidth : 0.0f;
            offsetY = (rect.getHeight() - scaledHeight) * 0.5f;
        } else if (absVelocityY > 5.0f && settled) {
            offsetX = (rect.getWidth() - scaledWidth) * 0.5f;
            offsetY = this.velocityY > 0.0f ? rect.getHeight() - scaledHeight : 0.0f;
        } else {
            offsetX = (rect.getWidth() - scaledWidth) * 0.5f;
            offsetY = (rect.getHeight() - scaledHeight) * 0.5f;
        }
        this.set(rect.getX() + offsetX + this.overshootX, rect.getY() + offsetY + this.overshootY, scaledWidth, scaledHeight);
        this.previous = new Rect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public void reset() {
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.targetScaleX = 1.0f;
        this.targetScaleY = 1.0f;
        this.velocityX = 0.0f;
        this.velocityY = 0.0f;
        this.previousVelocityX = 0.0f;
        this.previousVelocityY = 0.0f;
        this.bounceX = false;
        this.bounceY = false;
        this.dragDistance = 0.0f;
        this.overshootX = 0.0f;
        this.overshootY = 0.0f;
        this.timer.reset();
    }

    public Rect getTarget() {
        return this.target;
    }

    public float getBounceFactor() {
        return this.bounceFactor;
    }

    public float getOvershootDecay() {
        return this.overshootDecay;
    }
}
