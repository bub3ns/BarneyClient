package moscow.rockstar.ui.hud;

import java.util.List;
import moscow.rockstar.modules.visuals.hud.DynamicIslandHud;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;

/**
 * The flow node one dynamic-island entry is mounted into. 1:1 with rockstar/ilIlil/IiIiiiIII
 * (final, extends iii = Component).
 *
 * <p>An entry either supplies its own child node through {@code content(island)} - in which case
 * this node just lays it out - or draws immediately through {@code render(...)} while this node
 * reports the entry's declared size.</p>
 */
final class DynamicIslandEntryNode extends Component {
    private final DynamicIslandEntry entry;
    private final DynamicIslandHud island;
    private UiNode contentNode;

    DynamicIslandEntryNode(DynamicIslandEntry entry, DynamicIslandHud island) {
        this.entry = entry;
        this.island = island;
        this.uniformLayout().fill().snapPosition().snapSize().interactive(false)
                .motion(Motion.resolveMotionMotionFromLongAndEasing(500L, Easing.easeOutOvershootSoft))
                .lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(360L, Easing.easeOutOvershootSoft))
                .enter(Transition.PROGRESS_ONLY)
                .exit(Transition.PROGRESS_ONLY);
    }

    @Override
    protected void measure() {
        this.syncContent();
        if (this.contentNode == null) {
            this.prefW = this.entry.getSize().width;
            this.prefH = this.entry.getSize().height;
            return;
        }
        super.measure();
        this.entry.getSize().set(this.desiredW(), this.desiredH());
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
        if (this.contentNode == null) {
            this.entry.render(drawContext, this.island, this.x(), this.y(), this.w(), this.h(), alpha);
        }
    }

    /** ORIGINAL: the private {@code II()V} helper. */
    private void syncContent() {
        UiNode node = this.entry.content(this.island);
        if (node == this.contentNode) {
            return;
        }
        this.contentNode = node;
        if (this.contentNode == null) {
            this.clear();
        } else {
            this.updateChildren(List.of(this.contentNode));
        }
    }
}
