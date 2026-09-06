package moscow.rockstar.ui.screens;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.colors.GradientColors;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.target.RenderTargetManager;
import moscow.rockstar.ui.animation.AnimatedColor;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.settings.SettingPanel;
import moscow.rockstar.ui.text.DateTimeText;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.widgets.mainmenu.MainMenuBar;
import moscow.rockstar.ui.widgets.mainmenu.MainMenuIcon;
import moscow.rockstar.ui.wallpaper.WallpaperCarouselController;
import moscow.rockstar.util.Timer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import pyrock.utility.render.ColorRGBA;

/** The original Rockstar title screen, including its wallpaper carousel and menu controls. */
public final class MainMenuScreen extends MinecraftScreenBase implements ClientAccess {
    private static final List<MainMenuIcon> MENU_ICONS = new ArrayList<>();
    private static final long CLICK_HOLD_THRESHOLD_MILLIS = 1500L;
    private static WallpaperCarouselController wallpaperController;
    private static MainMenuBar mainMenuBar;
    private static boolean menuIconsInitialized;

    private final Animation titleTransitionAnimation = new Animation(800L, 0.0f, Easing.easeInOutCubicBezier);
    private final ColorRGBA titleBrightColor = new ColorRGBA(171.0f, 254.0f, 255.0f);
    private final ColorRGBA titleDimColor = new ColorRGBA(203.0f, 254.0f, 255.0f);
    private final long titleTransitionDurationMillis = titleTransitionAnimation.getDuration();
    private final Timer transitionClickTimer = new Timer();
    private final Timer backgroundSampleTimer = new Timer();
    private final AnimatedColor titleColor = new AnimatedColor(300L, ColorRGBA.WHITE, Easing.linear);
    private boolean transitionShowing;
    private boolean dragging;
    private long dragStartedAtMillis;
    private double dragStartX;
    private double dragStartY;
    private float smoothedMouseX;
    private float smoothedMouseY;
    private long lastFrameTimeMillis = System.currentTimeMillis();
    private float dragTransitionProgress;
    private boolean titleUsesDarkText;

    public MainMenuScreen() {
    }

    @Override
    protected void init() {
        ensureMenuIcons();
        getMainMenuBar();
        Font.warmUpFontCaches();
        super.init();
    }

    private static void ensureMenuIcons() {
        if (menuIconsInitialized) {
            return;
        }
        MENU_ICONS.clear();
        MENU_ICONS.add(new MainMenuIcon("image/mainmenu/icons/single.png", 12.0f,
            () -> minecraftClient.setScreen(new SelectWorldScreen((Screen) minecraftClient.currentScreen))));
        MENU_ICONS.add(new MainMenuIcon("image/mainmenu/icons/multi.png", 12.0f,
            () -> minecraftClient.setScreen(new MultiplayerScreen((Screen) minecraftClient.currentScreen))));
        MENU_ICONS.add(new MainMenuIcon("image/mainmenu/icons/settings.png", 12.0f,
            () -> minecraftClient.setScreen(new OptionsScreen((Screen) minecraftClient.currentScreen, minecraftClient.options))));
        MENU_ICONS.add(new MainMenuIcon("image/mainmenu/icons/quit.png", 14.0f,
            minecraftClient::scheduleStop));
        menuIconsInitialized = true;
    }

    private static WallpaperCarouselController getWallpaperController() {
        if (wallpaperController == null) {
            wallpaperController = new WallpaperCarouselController(WallpaperCarouselController.getDefaultWallpaperIndex());
        }
        return wallpaperController;
    }

    private static MainMenuBar getMainMenuBar() {
        if (mainMenuBar == null) {
            mainMenuBar = new MainMenuBar(() -> getWallpaperController().startTransition());
        }
        return mainMenuBar;
    }

    @Override
    public void render(RockstarDrawContext context) {
        FontMetrics titleFont = Font.ROUND_BOLD.metrics(65.0f);
        FontMetrics dateFont = Font.REGULAR.metrics(16.0f);
        FontMetrics helperFont = Font.REGULAR.metrics(10.0f);
        WallpaperCarouselController wallpapers = getWallpaperController();
        MainMenuBar menuBar = getMainMenuBar();

        float wallpaperFade = 1.0f - wallpapers.getTransitionProgress();
        float initialTitleTransition = titleTransitionAnimation.getValue();
        float titleAlpha = 205.0f * (1.0f - initialTitleTransition) * wallpaperFade;
        float titleBaseY = 80.0f;
        float titleY = MathUtils.interpolateDouble(titleBaseY, -120.0, initialTitleTransition);
        titleTransitionAnimation.setReverse(transitionShowing);

        context.drawRoundedRect(0.0f, 0.0f, width, height, WidgetState.NONE, ColorRGBA.BLACK);

        long now = System.currentTimeMillis();
        float frameDelta = Math.min(0.1f, (now - lastFrameTimeMillis) / 1000.0f);
        lastFrameTimeMillis = now;
        updateTransitionProgress(now, frameDelta, wallpapers);

        float centerX = width / 2.0f;
        float centerY = height / 2.0f;
        float mouseInfluence = (10.0f + 6.0f * titleTransitionAnimation.getValue()) * wallpaperFade;
        float targetMouseX = clamp((context.mouseX() - centerX) / Math.max(1.0f, centerX), -1.0f, 1.0f) * mouseInfluence;
        float targetMouseY = clamp((context.mouseY() - centerY) / Math.max(1.0f, centerY), -1.0f, 1.0f) * mouseInfluence;
        float mouseSmoothing = 1.0f - (float) Math.pow(0.0025f, frameDelta);
        smoothedMouseX += (targetMouseX - smoothedMouseX) * mouseSmoothing;
        smoothedMouseY += (targetMouseY - smoothedMouseY) * mouseSmoothing;

        float titleTransitionValue = titleTransitionAnimation.getValue();
        wallpapers.renderWallpaperCarousel(context, width, height, smoothedMouseX, smoothedMouseY,
            titleTransitionValue, dragTransitionProgress);

        boolean inWorld = minecraftClient.world != null;
        float blurOpacity = (inWorld ? 1.0f : 1.0f - titleTransitionValue) * wallpaperFade;
        if (blurOpacity > 0.01f) {
            RenderTargetManager.render(inWorld ? 0.5f : 1.4f);
            context.drawBlurredRect(0.0f, 0.0f, width, height, 15.0f, WidgetState.NONE,
                ColorRGBA.WHITE.withAlpha(255.0f * blurOpacity));
        }

        float bottomGradientHeight = Math.min(130.0f, height * 0.38f);
        context.drawRoundedRect(0.0f, height - bottomGradientHeight, width, bottomGradientHeight, WidgetState.NONE,
            // GradientColors' two-colour form is a LEFT->RIGHT fade (see IiIii -> IiIIi(a,a,b,b)).
            // This vignette is vertical, so name all four corners explicitly.
            new GradientColors(
                new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f),
                new ColorRGBA(0.0f, 0.0f, 0.0f, 90.0f * wallpaperFade),
                new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f),
                new ColorRGBA(0.0f, 0.0f, 0.0f, 90.0f * wallpaperFade)
            ));

        if (backgroundSampleTimer.hasElapsed(250L)) {
            double scale = minecraftClient.getWindow().getScaleFactor();
            float sampleY = titleBaseY + titleFont.getFontTopOffset() * 0.5f;
            ColorRGBA sampledPixel = ColorRGBA.fromPixel(width / 2.0f * (float) scale,
                minecraftClient.getWindow().getFramebufferHeight() - sampleY * (float) scale);
            titleUsesDarkText = (sampledPixel.getRed() + sampledPixel.getGreen() + sampledPixel.getBlue()) / 3.0f > 120.0f;
            backgroundSampleTimer.reset();
        }
        titleColor.setTargetColor(titleUsesDarkText ? titleDimColor : ColorRGBA.WHITE);
        ColorRGBA visibleTitleColor = titleColor.getColor().withAlpha(titleAlpha);

        String date = DateTimeText.currentDate();
        context.drawCenteredText(dateFont, date, width / 2.0f, titleY - 23.0f, visibleTitleColor);

        String time = DateTimeText.currentTime();
        String[] timeParts = time.split(":", 2);
        String hours = timeParts.length > 0 ? timeParts[0] : time;
        String minutes = timeParts.length > 1 ? timeParts[1] : "";
        float hoursWidth = titleFont.measureText(hours);
        float colonWidth = titleFont.measureText(":");
        float minutesWidth = titleFont.measureText(minutes);
        float timeWidth = hoursWidth + colonWidth + minutesWidth;
        float timeX = width / 2.0f - timeWidth / 2.0f;
        float pulseScale = 1.0f + 0.008f * (float) Math.sin(now * 0.00185f);
        ItemRenderUtils.translateAndScale(context.getMatrices(), width / 2.0f,
            titleY + titleFont.getFontTopOffset() / 2.0f, pulseScale);
        float colonY = titleY - titleFont.getFontTopOffset() * 0.1f;
        context.drawText(titleFont, hours, timeX, titleY, visibleTitleColor);
        context.drawText(titleFont, ":", timeX + hoursWidth, colonY, visibleTitleColor);
        context.drawText(titleFont, minutes, timeX + hoursWidth + colonWidth, titleY, visibleTitleColor);
        ItemRenderUtils.popMatrix(context.getMatrices());

        float profileOpacity = (1.0f - titleTransitionValue) * wallpaperFade;
        float profileOffset = 6.0f * titleTransitionValue;
        context.drawCenteredText(helperFont, moscow.rockstar.ui.localization.Localization.translate("mainmenu.next"),
            width / 2.0f, height - 70.0f + profileOffset, ColorRGBA.WHITE.withAlpha(180.0f * profileOpacity));
        if (height > 400) {
            float avatarSize = 26.0f;
            float avatarX = width / 2.0f - avatarSize / 2.0f;
            float avatarY = height - 54.0f + profileOffset;
            context.drawRoundedTexture(SettingPanel.getAvatarTexture(), avatarX, avatarY, avatarSize, avatarSize,
                WidgetState.uniform(avatarSize / 2.0f), ColorRGBA.WHITE.withAlpha(255.0f * profileOpacity));
        }
        String username = minecraftClient.getSession() == null ? "" : minecraftClient.getSession().getUsername();
        context.drawCenteredText(Font.REGULAR.metrics(11.0f), username, width / 2.0f,
            height - 22.0f + profileOffset, ColorRGBA.WHITE.withAlpha(255.0f * profileOpacity));

        float iconOffset = 0.0f;
        for (int index = 0; index < MENU_ICONS.size(); index++) {
            MainMenuIcon icon = MENU_ICONS.get(index);
            boolean hideIcon = MENU_ICONS.size() - index > (1.0f - titleTransitionValue) * MENU_ICONS.size() + 0.5f;
            icon.getVisibilityAnimation().setReverse(hideIcon);
            float iconX = width / 2.0f - 69.0f + iconOffset;
            float iconY = (height > 500 ? height / 2.0f + 20.0f : height / 1.25f)
                - 5.0f - 10.0f * icon.getVisibilityAnimation().getValue();
            icon.set(iconX, iconY, 30.0f, 30.0f);
            iconOffset += icon.getWidth() + 6.0f;
            icon.render(context, wallpaperFade);
        }

        wallpapers.renderWallpaperTitle(context, width, height);
        menuBar.render(context, width, height, wallpaperFade, titleTransitionValue);
    }

    private void updateTransitionProgress(long now, float frameDelta, WallpaperCarouselController wallpapers) {
        if (dragging && !wallpapers.isTransitionActive()
            && now - dragStartedAtMillis >= CLICK_HOLD_THRESHOLD_MILLIS) {
            dragging = false;
            transitionShowing = false;
            wallpapers.stopTransition();
        }

        float targetProgress = dragging && !wallpapers.isTransitionActive()
            ? clamp((now - dragStartedAtMillis) / (float) CLICK_HOLD_THRESHOLD_MILLIS, 0.0f, 1.0f)
            : 0.0f;
        dragTransitionProgress += (targetProgress - dragTransitionProgress)
            * (1.0f - (float) Math.pow(0.0006f, frameDelta));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, PointerAction action) {
        WallpaperCarouselController wallpapers = getWallpaperController();
        if (wallpapers.isTransitionActive()) {
            wallpapers.beginDrag(mouseX, mouseY, action.getButtonCode());
            return;
        }
        if (getMainMenuBar().handleClick(mouseX, mouseY, action.getButtonCode())) {
            return;
        }
        for (MainMenuIcon icon : MENU_ICONS) {
            if (!icon.hovered(mouseX, mouseY) || icon.getVisibilityAnimation().getValue() != 1.0f) {
                continue;
            }
            icon.handleClick(mouseX, mouseY, action.getButtonCode());
            return;
        }
        if (action == PointerAction.LEFT_CLICK) {
            dragging = true;
            dragStartedAtMillis = System.currentTimeMillis();
            dragStartX = mouseX;
            dragStartY = mouseY;
        }
        super.onMouseClicked(mouseX, mouseY, action);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, PointerAction action) {
        WallpaperCarouselController wallpapers = getWallpaperController();
        if (wallpapers.isTransitionActive()) {
            wallpapers.endDrag(mouseX, mouseY, action.getButtonCode());
            return;
        }
        if (dragging && action == PointerAction.LEFT_CLICK) {
            dragging = false;
            if (System.currentTimeMillis() - dragStartedAtMillis < CLICK_HOLD_THRESHOLD_MILLIS
                && transitionClickTimer.hasElapsed(titleTransitionDurationMillis)) {
                transitionShowing = !transitionShowing;
                transitionClickTimer.reset();
            }
        }
    }

    @Override
    public void onMouseDragged(double mouseX, double mouseY, PointerAction action, double deltaX, double deltaY) {
        WallpaperCarouselController wallpapers = getWallpaperController();
        if (wallpapers.isTransitionActive()) {
            wallpapers.updateDragSelection(mouseX, mouseY, action.getButtonCode());
            return;
        }
        if (dragging && (Math.abs(mouseX - dragStartX) > 6.0 || Math.abs(mouseY - dragStartY) > 6.0)) {
            dragging = false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        WallpaperCarouselController wallpapers = getWallpaperController();
        if (wallpapers.isTransitionActive()) {
            wallpapers.scrollBy(verticalAmount);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        WallpaperCarouselController wallpapers = getWallpaperController();
        if (wallpapers.isTransitionActive()) {
            if (keyCode == 256) {
                wallpapers.stopTransition();
            } else if (keyCode == 263) {
                wallpapers.moveSelection(-1);
            } else if (keyCode == 262) {
                wallpapers.moveSelection(1);
            }
            return true;
        }
        if (getMainMenuBar().handleKeyPressed(keyCode)) {
            return true;
        }
        if (keyCode == 82) {
            minecraftClient.setScreen(new MultiplayerScreen(this));
        } else if (keyCode == 84) {
            minecraftClient.setScreen(new SelectWorldScreen(this));
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
