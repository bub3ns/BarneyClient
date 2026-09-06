package moscow.rockstar.ui.screens;

import moscow.rockstar.items.assist.AssistItemProvider;

/** Hit-test geometry for one Assist item card and its key-binding control. */
public final class AssistItemSelection {
    private final AssistItemProvider provider;
    private final float cardX;
    private final float cardY;
    private final float cardWidth;
    private final float cardHeight;
    private final float keyBindX;
    private final float keyBindY;
    private final float keyBindWidth;
    private final float keyBindHeight;

    public AssistItemSelection(AssistItemProvider provider,
                               float cardX, float cardY, float cardWidth, float cardHeight,
                               float keyBindX, float keyBindY, float keyBindWidth, float keyBindHeight) {
        this.provider = provider;
        this.cardX = cardX;
        this.cardY = cardY;
        this.cardWidth = cardWidth;
        this.cardHeight = cardHeight;
        this.keyBindX = keyBindX;
        this.keyBindY = keyBindY;
        this.keyBindWidth = keyBindWidth;
        this.keyBindHeight = keyBindHeight;
    }

    public AssistItemProvider getProvider() {
        return this.provider;
    }

    public float getCardX() {
        return this.cardX;
    }

    public float getCardY() {
        return this.cardY;
    }

    public float getCardWidth() {
        return this.cardWidth;
    }

    public float getCardHeight() {
        return this.cardHeight;
    }

    public float getKeyBindX() {
        return this.keyBindX;
    }

    public float getKeyBindY() {
        return this.keyBindY;
    }

    public float getKeyBindWidth() {
        return this.keyBindWidth;
    }

    public float getKeyBindHeight() {
        return this.keyBindHeight;
    }
}
