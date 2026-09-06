/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.combat.rotation;

import lombok.Generated;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.events.Event;
import moscow.rockstar.events.EventListener;
import net.minecraft.client.MinecraftClient;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.player.InputEvent;

public final class RotationState {
    boolean forwardPressed;
    boolean backwardPressed;
    boolean strafeRightPressed;
    boolean strafeLeftPressed;
    boolean jumpPressed;
    boolean sneakPressed;
    boolean sprintPressed;
    boolean movementOverrideEnabled;
    private boolean sprintKeyForced;
    private final EventListener<InputEvent> inputListener = new EventListener<InputEvent>(){

        public void onEvent(InputEvent inputEvent) {
            if (!RotationState.this.movementOverrideEnabled) {
                return;
            }
            float f = (RotationState.this.forwardPressed ? 1.0f : 0.0f) + (RotationState.this.backwardPressed ? -1.0f : 0.0f);
            float f2 = (RotationState.this.strafeRightPressed ? 1.0f : 0.0f) + (RotationState.this.strafeLeftPressed ? -1.0f : 0.0f);
            inputEvent.setForward(f);
            inputEvent.setStrafe(f2);
            inputEvent.setJump(RotationState.this.jumpPressed);
            inputEvent.setSneak(RotationState.this.sneakPressed);
            inputEvent.setSprint(RotationState.this.sprintPressed);
        }

        @Override
        public int getPriority() {
            return 100;
        }

    };
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (this.movementOverrideEnabled && this.sprintPressed && this.forwardPressed) {
            this.pressSprintKey();
        } else if (this.sprintKeyForced) {
            this.releaseSprintKey();
        }
    };

    public void setForwardPressed(boolean bl) {
        this.forwardPressed = bl;
    }

    public void setBackwardPressed(boolean bl) {
        this.backwardPressed = bl;
    }

    public void setStrafeRightPressed(boolean bl) {
        this.strafeRightPressed = bl;
    }

    public void setStrafeLeftPressed(boolean bl) {
        this.strafeLeftPressed = bl;
    }

    public void setJumpPressed(boolean bl) {
        this.jumpPressed = bl;
    }

    public void setSneakPressed(boolean bl) {
        this.sneakPressed = bl;
    }

    public void setSprintPressed(boolean bl) {
        this.sprintPressed = bl;
    }

    public void enableMovementOverride() {
        this.movementOverrideEnabled = true;
    }

    public void disableMovementOverride() {
        this.movementOverrideEnabled = false;
        this.sprintPressed = false;
        this.sneakPressed = false;
        this.jumpPressed = false;
        this.strafeLeftPressed = false;
        this.strafeRightPressed = false;
        this.backwardPressed = false;
        this.forwardPressed = false;
        this.releaseSprintKey();
    }

    private void pressSprintKey() {
        if (this.sprintKeyForced) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null && client.options.sprintKey != null) {
            client.options.sprintKey.setPressed(true);
            this.sprintKeyForced = true;
        }
    }

    private void releaseSprintKey() {
        if (!this.sprintKeyForced) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null && client.options.sprintKey != null) {
            client.options.sprintKey.setPressed(false);
        }
        this.sprintKeyForced = false;
    }

    public static RotationState createRotationState(ClientServiceRegistry clientServiceRegistry) {
        RotationState rotationState = new RotationState();
        clientServiceRegistry.getEventBus().registerListeners(rotationState);
        return rotationState;
    }

    @Generated
    public boolean isForwardPressed() {
        return this.forwardPressed;
    }

    @Generated
    public boolean isBackwardPressed() {
        return this.backwardPressed;
    }

    @Generated
    public boolean isStrafeRightPressed() {
        return this.strafeRightPressed;
    }

    @Generated
    public boolean isStrafeLeftPressed() {
        return this.strafeLeftPressed;
    }

    @Generated
    public boolean isJumpPressed() {
        return this.jumpPressed;
    }

    @Generated
    public boolean isSneakPressed() {
        return this.sneakPressed;
    }

    @Generated
    public boolean isSprintPressed() {
        return this.sprintPressed;
    }

    @Generated
    public boolean isMovementOverrideEnabled() {
        return this.movementOverrideEnabled;
    }
}

