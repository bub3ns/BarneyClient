/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.layout;

public enum Layout {
    COLUMN,
    COLUMN_REVERSE,
    ROW,
    ROW_REVERSE;

    public boolean isVertical() {
        return this == COLUMN || this == COLUMN_REVERSE;
    }

    public boolean isHorizontal() {
        return this == ROW || this == ROW_REVERSE;
    }

    public boolean isReversed() {
        return this == COLUMN_REVERSE || this == ROW_REVERSE;
    }
}

