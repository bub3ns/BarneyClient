package moscow.rockstar.ui.widgets.mainmenu;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.CursorManager;
import moscow.rockstar.modules.visuals.hud.Interface;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.Rect;

/** One of the four action icons displayed by the custom title screen. */
public final class MainMenuIcon extends Rect {
    private final String imagePath;
    private final float iconSize;
    private final Runnable action;
    private final ColorRGBA hoverColor = new ColorRGBA(58.0f, 58.0f, 58.0f);
    private final Animation visibilityAnimation = new Animation(400L, 0.0f, Easing.easeOutBack);
    private final Animation hoverAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);

    public MainMenuIcon(String imagePath, float iconSize, Runnable action) {
        this.imagePath = imagePath;
        this.iconSize = iconSize;
        this.action = action;
    }

    public void render(RockstarDrawContext context, float wallpaperFade) {
        boolean hovered = hovered(context.mouseX(), context.mouseY())
            && visibilityAnimation.getValue() == 1.0f;
        if (hovered && wallpaperFade > 0.5f) {
            CursorManager.request(Cursor.HAND);
        }
        hoverAnimation.setReverse(hovered);

        float radius = Math.min(width, height) / 2.0f;
        float hoverAlpha = 255.0f * (0.15f * visibilityAnimation.getValue()
            + 0.15f * hoverAnimation.getValue()) * wallpaperFade;
        if (Interface.isLiquidGlassEnabled()) {
            context.drawLiquidGlass(
                x - 1.0f,
                y - 1.0f,
                width + 2.0f,
                height + 2.0f,
                7.0f,
                0.08f,
                WidgetState.uniform(radius),
                ColorRGBA.WHITE.withAlpha(255.0f * visibilityAnimation.getValue() * wallpaperFade)
            );
            context.drawRoundedRect(x, y, width, height, WidgetState.uniform(radius), hoverColor.withAlpha(hoverAlpha));
        } else if (Interface.isBlurEnabled()) {
            context.drawRoundedRect(x, y, width, height, WidgetState.uniform(radius), hoverColor.withAlpha(
                255.0f * (0.33f * visibilityAnimation.getValue() + 0.2f * hoverAnimation.getValue()) * wallpaperFade
            ));
        }

        context.drawTexture(
            RockstarClient.resourceId(imagePath),
            x + (width - iconSize) / 2.0f,
            y + (height - iconSize) / 2.0f,
            iconSize,
            iconSize,
            ColorRGBA.WHITE.withAlpha(255.0f * visibilityAnimation.getValue() * wallpaperFade)
        );
    }

    public void handleClick(double mouseX, double mouseY, int button) {
        if (hovered(mouseX, mouseY) && button == 0 && visibilityAnimation.getValue() == 1.0f) {
            action.run();
        }
    }

    public Animation getVisibilityAnimation() {
        return visibilityAnimation;
    }
}
