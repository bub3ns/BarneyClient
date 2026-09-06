/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.combat.rotation;

import lombok.Generated;

public enum RotationPriority {
    LOW_PRIORITY(-2),
    STANDARD_PRIORITY(0),
    TARGET_PRIORITY(2),
    OVERRIDE_PRIORITY(5),
    ITEM_USE_PRIORITY(4),
    MAXIMUM_PRIORITY(6);
    private final int priorityValue;

    @Generated
    public int getPriorityValue() {
        return this.priorityValue;
    }

    @Generated
    private RotationPriority(int n2) {
        this.priorityValue = n2;
    }
}

