package moscow.rockstar.ui.screens;

/** Result returned when the searchable Assist list hits an item. */
public final class AssistSearchResult {
    private final AssistItem item;

    public AssistSearchResult(AssistItem item) {
        this.item = item;
    }

    public AssistItem getItem() {
        return this.item;
    }
}
