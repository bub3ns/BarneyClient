package moscow.rockstar.render.text;

/** A deferred text replacement captured for the original three-pass pipeline. */
public interface TextCapturePatch {
    int[] getBounds();

    void renderOriginal();

    void renderReplacement();
}
