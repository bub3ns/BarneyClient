/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.input;

import lombok.Generated;

public enum PointerAction {
    LEFT_CLICK(0),
    RIGHT_CLICK(1),
    MIDDLE_CLICK(2),
    BUTTON_4(3),
    BUTTON_5(4),
    BUTTON_6(5),
    BUTTON_7(6),
    BUTTON_8(7);
    private final int buttonCode;

    public static PointerAction fromButtonCode(int n) {
        for (PointerAction pointerAction : PointerAction.values()) {
            if (pointerAction.getButtonCode() != n) continue;
            return pointerAction;
        }
        return LEFT_CLICK;
    }

    @Generated
    private PointerAction(int n2) {
        this.buttonCode = n2;
    }

    @Generated
    public int getButtonCode() {
        return this.buttonCode;
    }
}

