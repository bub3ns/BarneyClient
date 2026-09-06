package moscow.rockstar.ui.hud;

import moscow.rockstar.modules.visuals.hud.DynamicIslandHud;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.state.UiScissorStack;
import moscow.rockstar.ui.core.Component;

/**
 * The island's root flow node: it draws the island chrome under its children and clips them to the
 * island rect. 1:1 with rockstar/ilIlil/IiIiiIiII (extends iii = Component).
 *
 * <p>The original class is package-private and calls the island's package-private draw entry point;
 * here the island lives in {@code moscow.rockstar.modules.visuals.hud} with the other HUD elements,
 * so the class and that one call are public. No behavioural difference.</p>
 */
public class DynamicIslandBackground extends Component {
    private final DynamicIslandHud island;

    public DynamicIslandBackground(DynamicIslandHud island) {
        this.island = island;
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
        this.island.drawIslandChrome(drawContext, this, alpha);
    }

    @Override
    protected void drawChildren(RockstarDrawContext drawContext, float alpha) {
        UiScissorStack.push(drawContext.getMatrices(), this.x(), this.y(), this.w(), this.h());
        super.drawChildren(drawContext, alpha);
        UiScissorStack.pop();
    }
}
