package moscow.rockstar.ui.hud;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

/** Collects and draws the HUD drag alignment guides. 1:1 with rockstar/ilIlil/IiIiIIIIi. */
public class HudSnapGuideOverlay {
    private final List<HudSnapGuide> guides = new ArrayList<HudSnapGuide>();

    public void draw(CustomDrawContext drawContext) {
        for (HudSnapGuide guide : this.guides) {
            if (!guide.isHighlighted()) {
                continue;
            }
            float x = guide.getAxis() == HudSnapGuide.Axis.VERTICAL ? guide.getPosition() - 0.5f : 0.0f;
            float y = guide.getAxis() == HudSnapGuide.Axis.HORIZONTAL ? guide.getPosition() - 0.5f : 0.0f;
            float width = guide.getAxis() == HudSnapGuide.Axis.VERTICAL ? 1.0f : WindowMetricsProvider.INSTANCE.width();
            float height = guide.getAxis() == HudSnapGuide.Axis.HORIZONTAL ? 1.0f : WindowMetricsProvider.INSTANCE.height();
            drawContext.drawRect(x, y, width, height, ColorRGBA.WHITE.mulAlpha(0.3f));
        }
    }

    public void rebuild() {
        this.guides.clear();
        this.guides.add(new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, 0.0f));
        this.guides.add(new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, WindowMetricsProvider.INSTANCE.width() * 0.5f));
        this.guides.add(new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, WindowMetricsProvider.INSTANCE.width()));
        this.guides.add(new HudSnapGuide(HudSnapGuide.Axis.HORIZONTAL, 0.0f));
        this.guides.add(new HudSnapGuide(HudSnapGuide.Axis.HORIZONTAL, WindowMetricsProvider.INSTANCE.height() * 0.5f));
        this.guides.add(new HudSnapGuide(HudSnapGuide.Axis.HORIZONTAL, WindowMetricsProvider.INSTANCE.height()));
        for (HudElement element : RockstarClient.create().getHudElementRegistry().elements()) {
            // ORIGINAL: || element instanceof IiIiiIIII || element instanceof IiIiIiIIi
            //           (the "hud.dynamic_island" and "hud.custom_hotbar" elements)
            if (!element.isShowing() || element.isDragging()
                    || "hud.dynamic_island".equals(element.getName())
                    || "hud.custom_hotbar".equals(element.getName())) {
                continue;
            }
            this.guides.add(new HudSnapGuide(HudSnapGuide.Axis.HORIZONTAL, element.getY()));
            this.guides.add(new HudSnapGuide(HudSnapGuide.Axis.HORIZONTAL, element.getY() + element.getHeight() * 0.5f));
            this.guides.add(new HudSnapGuide(HudSnapGuide.Axis.HORIZONTAL, element.getY() + element.getHeight()));
            this.guides.add(new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, element.getX()));
            this.guides.add(new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, element.getX() + element.getWidth() * 0.5f));
            this.guides.add(new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, element.getX() + element.getWidth()));
        }
    }

    public List<HudSnapGuide> guides() {
        return this.guides;
    }
}
