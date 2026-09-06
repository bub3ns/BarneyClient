/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.animation;

import moscow.rockstar.ui.core.UiNode;

public interface Transition {
    public static final Transition NO_OP = (f, uiNode, state) -> {};
    public static final Transition HIDDEN = (f, uiNode, state) -> {
        state.progress = 0.0f;
    };
    public static final Transition PROGRESS_ONLY = (f, uiNode, state) -> {
        state.progress = f;
    };
    public static final Transition SLIDE_UP_SHORT = (f, uiNode, state) -> {
        state.progress = f;
        state.offsetY = (1.0f - f) * 14.0f;
    };
    public static final Transition SLIDE_UP = (f, uiNode, state) -> {
        state.progress = f;
        state.offsetY = (1.0f - f) * 16.0f;
    };
    public static final Transition SLIDE_DOWN = (f, uiNode, state) -> {
        state.progress = f;
        state.offsetY = -(1.0f - f) * 16.0f;
    };
    public static final Transition SLIDE_RIGHT = (f, uiNode, state) -> {
        state.progress = f;
        state.offsetX = (1.0f - f) * 16.0f;
    };
    public static final Transition SLIDE_LEFT = (f, uiNode, state) -> {
        state.progress = f;
        state.offsetX = -(1.0f - f) * 16.0f;
    };
    public static final Transition SCALE = (f, uiNode, state) -> {
        state.progress = f;
        state.scale = 0.92f + 0.08f * f;
    };
    public static final Transition SCALE_UP = (f, uiNode, state) -> {
        state.progress = f;
        state.scale = 0.85f + 0.15f * f;
        state.offsetY = (1.0f - f) * 8.0f;
    };

    public void apply(float var1, UiNode var2, State var3);

    public static Transition slideUp(float f) {
        return (f2, uiNode, state) -> {
            state.progress = f2;
            state.offsetY = (1.0f - f2) * f;
        };
    }

    public static Transition slideRight(float f) {
        return (f2, uiNode, state) -> {
            state.progress = f2;
            state.offsetX = (1.0f - f2) * f;
        };
    }

    public static Transition slideByHeight(float f) {
        return (f2, uiNode, state) -> {
            state.progress = f2;
            state.offsetY = (1.0f - f2) * uiNode.h() * f;
        };
    }

    public static final class State {
        public float progress = 1.0f;
        public float offsetX = 0.0f;
        public float offsetY = 0.0f;
        public float scale = 1.0f;

        public void reset() {
            this.progress = 1.0f;
            this.offsetX = 0.0f;
            this.offsetY = 0.0f;
            this.scale = 1.0f;
        }
    }
}

