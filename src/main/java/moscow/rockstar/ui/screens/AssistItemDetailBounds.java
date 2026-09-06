package moscow.rockstar.ui.screens;

/** Screen-space bounds of an Assist detail panel and its content area. */
public final class AssistItemDetailBounds {
    private final float panelX;
    private final float panelY;
    private final float panelWidth;
    private final float panelHeight;
    private final float contentX;
    private final float contentY;
    private final float contentWidth;
    private final float contentHeight;

    public AssistItemDetailBounds(float panelX, float panelY, float panelWidth, float panelHeight,
                                  float contentX, float contentY, float contentWidth, float contentHeight) {
        this.panelX = panelX;
        this.panelY = panelY;
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.contentX = contentX;
        this.contentY = contentY;
        this.contentWidth = contentWidth;
        this.contentHeight = contentHeight;
    }

    public float getPanelX() {
        return this.panelX;
    }

    public float getPanelY() {
        return this.panelY;
    }

    public float getPanelWidth() {
        return this.panelWidth;
    }

    public float getPanelHeight() {
        return this.panelHeight;
    }

    public float getContentX() {
        return this.contentX;
    }

    public float getContentY() {
        return this.contentY;
    }

    public float getContentWidth() {
        return this.contentWidth;
    }

    public float getContentHeight() {
        return this.contentHeight;
    }
}
