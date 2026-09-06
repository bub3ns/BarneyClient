package moscow.rockstar.ui.widgets.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.AnimatedColor;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.CursorManager;
import moscow.rockstar.ui.localization.Language;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingPanel;
import moscow.rockstar.ui.text.DateTimeText;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.util.Timer;
import moscow.rockstar.math.MathUtils;
import pyrock.utility.render.ColorRGBA;

/** The compact status and navigation bar displayed above the main menu. */
public final class MainMenuBar implements ClientAccess {
    public static final float BAR_HEIGHT = 22.0f;
    private static final float LOGO_SIZE = 12.0f;
    private static final float TEXT_SIZE = 8.0f;
    private static final float LANGUAGE_ICON_SIZE = 12.0f;
    private static final float AVATAR_SIZE = 13.0f;
    private static final float LANGUAGE_MENU_WIDTH = 106.0f;
    private static final float LANGUAGE_ROW_HEIGHT = 18.0f;
    private static final float LANGUAGE_MENU_Y = 26.0f;
    private static final ColorRGBA DARK_TEXT = new ColorRGBA(28.0f, 28.0f, 30.0f);
    private static final ColorRGBA LIGHT_TEXT = new ColorRGBA(255.0f, 255.0f, 255.0f);

    private final Runnable wallpaperTransitionAction;
    private final List<MenuBarItem> items = new ArrayList<>();
    private final Animation languageMenuAnimation = new Animation(300L, 0.0f, Easing.linear);
    private final Timer backgroundSampleTimer = new Timer();
    private final AnimatedColor leftTextColor = new AnimatedColor(300L, LIGHT_TEXT, Easing.linear);
    private final AnimatedColor rightTextColor = new AnimatedColor(300L, LIGHT_TEXT, Easing.linear);
    private boolean languageMenuOpen;
    private float languageIconX;
    private float languageMenuX;
    private float languageMenuAnchorY;
    private float barOpacity;
    private boolean leftTextNeedsDarkColor;
    private boolean rightTextNeedsDarkColor;

    public MainMenuBar(Runnable wallpaperTransitionAction) {
        this.wallpaperTransitionAction = wallpaperTransitionAction;
        items.add(new MenuBarItem(
            "mainmenu.bar.wallpaper",
            () -> {
                languageMenuOpen = false;
                wallpaperTransitionAction.run();
            },
            () -> false
        ));
    }

    public void render(RockstarDrawContext context, int width, int height, float fade) {
        render(context, width, height, fade, 1.0f);
    }

    public void render(RockstarDrawContext context, int width, int height, float fade, float scale) {
        updateTextColors(width);
        renderAccountInfo(context, width, fade, scale);

        if (fade < 0.5f) {
            languageMenuOpen = false;
        }
        barOpacity = fade * scale;
        languageMenuAnimation.setReverse(languageMenuOpen);
        renderLanguageMenu(context);

        if (barOpacity <= 0.003f) {
            return;
        }

        FontMetrics itemFont = Font.REGULAR.metrics(TEXT_SIZE);
        float verticalOffset = (1.0f - scale) * -5.0f;
        float logoY = 11.0f - LOGO_SIZE / 2.0f + verticalOffset;
        context.drawIcon("logo", 8.0f, logoY, LOGO_SIZE, leftTextColor.getColor().withAlpha(255.0f * barOpacity));

        float nextItemX = 8.0f + LOGO_SIZE + 12.0f;
        for (MenuBarItem item : items) {
            item.textWidth = itemFont.measureText(Localization.translate(item.labelKey));
            item.textX = nextItemX;
            item.x = nextItemX - 6.0f;
            item.y = 0.0f;
            item.width = item.textWidth + 12.0f;
            item.height = BAR_HEIGHT;
            nextItemX += item.width;
        }

        int mouseX = context.mouseX();
        int mouseY = context.mouseY();
        for (MenuBarItem item : items) {
            String label = Localization.translate(item.labelKey);
            boolean hovered = barOpacity > 0.5f
                && mouseX >= item.x
                && mouseX <= item.x + item.width
                && mouseY >= item.y
                && mouseY <= item.y + item.height;
            boolean alwaysVisible = item.availability.getAsBoolean();
            item.hoverAnimation.setReverse(hovered || alwaysVisible);
            if (hovered) {
                CursorManager.request(Cursor.HAND);
            }
            float visibility = MathUtils.interpolateDouble(0.83f, 1.0f, item.hoverAnimation.getValue());
            float textY = item.y + item.height / 2.0f - itemFont.getFontTopOffset() / 2.0f + 0.5f + verticalOffset;
            context.drawText(itemFont, label, item.textX, textY, leftTextColor.getColor().withAlpha(
                255.0f * visibility * barOpacity
            ));
        }
    }

    private void updateTextColors(int width) {
        if (backgroundSampleTimer.hasElapsed(250L)) {
            double scale = minecraftScaleFactor();
            leftTextNeedsDarkColor = isBrightPixel(40.0f, 11.0f, scale);
            rightTextNeedsDarkColor = isBrightPixel(width - 70.0f, 11.0f, scale);
            backgroundSampleTimer.reset();
        }
        leftTextColor.setTargetColor(leftTextNeedsDarkColor ? DARK_TEXT : LIGHT_TEXT);
        rightTextColor.setTargetColor(rightTextNeedsDarkColor ? DARK_TEXT : LIGHT_TEXT);
    }

    private void renderAccountInfo(RockstarDrawContext context, int width, float fade, float scale) {
        if (fade <= 0.003f) {
            return;
        }

        FontMetrics font = Font.REGULAR.metrics(TEXT_SIZE);
        float textHeight = font.getFontTopOffset();
        float baseline = 11.0f - textHeight / 2.0f + 0.5f;
        float dateTimeWidth = font.measureText(DateTimeText.currentDate() + "  " + DateTimeText.currentTime());
        String username = minecraftClient.getSession() == null ? "" : minecraftClient.getSession().getUsername();
        float usernameWidth = font.measureText(username);

        float rightEdge = width - 10.0f;
        float dateTimeX = rightEdge - dateTimeWidth;
        rightEdge = dateTimeX - (6.0f + 4.0f);
        float languageIconX = rightEdge - LANGUAGE_ICON_SIZE;
        rightEdge = languageIconX - 6.0f;
        float avatarX = rightEdge - AVATAR_SIZE;
        rightEdge = avatarX - 6.0f;
        float usernameX = rightEdge - usernameWidth;
        float textSlide = (dateTimeWidth + 6.0f + 4.0f) * (1.0f - scale);
        float visibleTextAlpha = 255.0f * fade * scale;

        context.drawText(font, DateTimeText.currentDate() + "  " + DateTimeText.currentTime(), dateTimeX + textSlide, baseline,
            rightTextColor.getColor().withAlpha(visibleTextAlpha));
        this.languageIconX = languageIconX;
        context.drawIcon("language", languageIconX, 11.0f - LANGUAGE_ICON_SIZE / 2.0f, LANGUAGE_ICON_SIZE,
            rightTextColor.getColor().withAlpha(255.0f * fade));
        context.drawRoundedTexture(
            SettingPanel.getAvatarTexture(),
            avatarX + textSlide,
            11.0f - AVATAR_SIZE / 2.0f,
            AVATAR_SIZE,
            AVATAR_SIZE,
            WidgetState.uniform(AVATAR_SIZE / 2.0f),
            ColorPalette.WHITE.withAlpha(visibleTextAlpha)
        );
        context.drawText(font, username, usernameX + textSlide, baseline, rightTextColor.getColor().withAlpha(visibleTextAlpha));

        languageMenuX = Math.max(4.0f, languageIconX + LANGUAGE_ICON_SIZE - LANGUAGE_MENU_WIDTH);
        languageMenuAnchorY = LANGUAGE_MENU_Y;
    }

    private void renderLanguageMenu(RockstarDrawContext context) {
        float progress = languageMenuAnimation.getValue();
        if (progress <= 0.003f) {
            return;
        }

        Language[] languages = Language.values();
        float menuHeight = languages.length * LANGUAGE_ROW_HEIGHT + 10.0f;
        float cornerRadius = ColorPalette.getThemeColorSettings().getCornerRadius();
        float rowRadius = Math.max(2.0f, cornerRadius - 3.0f);
        context.drawShadow(languageMenuX, languageMenuAnchorY, LANGUAGE_MENU_WIDTH, menuHeight, 12.0f,
            WidgetState.uniform(cornerRadius), ColorRGBA.BLACK.withAlpha(120.0f));
        context.drawClientRect(languageMenuX, languageMenuAnchorY, LANGUAGE_MENU_WIDTH, menuHeight, progress, 0.0f, 7.0f);

        FontMetrics languageFont = Font.REGULAR.metrics(TEXT_SIZE);
        int mouseX = context.mouseX();
        int mouseY = context.mouseY();
        Language selectedLanguage = Localization.getLanguage();
        for (int index = 0; index < languages.length; index++) {
            float rowY = languageMenuAnchorY + 5.0f + index * LANGUAGE_ROW_HEIGHT;
            boolean hovered = mouseX >= languageMenuX + 5.0f
                && mouseX <= languageMenuX + LANGUAGE_MENU_WIDTH - 5.0f
                && mouseY >= rowY
                && mouseY <= rowY + LANGUAGE_ROW_HEIGHT;
            boolean selected = selectedLanguage == languages[index];
            ColorRGBA textColor = ColorPalette.getPrimaryTextColor();
            if (hovered) {
                CursorManager.request(Cursor.HAND);
                context.drawSquircle(languageMenuX + 5.0f, rowY, LANGUAGE_MENU_WIDTH - 10.0f, LANGUAGE_ROW_HEIGHT,
                    7.0f, WidgetState.uniform(rowRadius), textColor.withAlpha(13.0f));
            }
            context.drawText(languageFont, languageName(languages[index]), languageMenuX + 12.0f,
                rowY + LANGUAGE_ROW_HEIGHT / 2.0f - languageFont.getFontTopOffset() / 2.0f,
                textColor.withAlpha(selected ? 255.0f : 150.0f));
            if (selected) {
                float markerSize = 4.0f;
                context.drawRoundedRect(languageMenuX + LANGUAGE_MENU_WIDTH - 5.0f - 7.0f - markerSize,
                    rowY + LANGUAGE_ROW_HEIGHT / 2.0f - markerSize / 2.0f, markerSize, markerSize,
                    WidgetState.uniform(markerSize / 2.0f), ColorRGBA.WHITE);
            }
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public boolean handleClick(double mouseX, double mouseY, int button) {
        if (barOpacity > 0.5f && isLanguageControlHovered(mouseX, mouseY)) {
            if (button == 0) {
                languageMenuOpen = !languageMenuOpen;
            }
            return true;
        }

        if (languageMenuOpen) {
            Language[] languages = Language.values();
            float menuHeight = languages.length * LANGUAGE_ROW_HEIGHT + 10.0f;
            if (mouseX >= languageMenuX && mouseX <= languageMenuX + LANGUAGE_MENU_WIDTH
                && mouseY >= languageMenuAnchorY && mouseY <= languageMenuAnchorY + menuHeight) {
                int index = (int) ((mouseY - languageMenuAnchorY - 5.0f) / LANGUAGE_ROW_HEIGHT);
                if (button == 0 && index >= 0 && index < languages.length) {
                    Localization.setLanguage(languages[index]);
                }
            }
            languageMenuOpen = false;
            return true;
        }

        if (barOpacity <= 0.5f) {
            return false;
        }
        for (MenuBarItem item : items) {
            if (mouseX < item.x || mouseX > item.x + item.width || mouseY < item.y || mouseY > item.y + item.height) {
                continue;
            }
            if (button == 0) {
                item.action.run();
            }
            return true;
        }
        return false;
    }

    public boolean handleKeyPressed(int keyCode) {
        if (languageMenuOpen && keyCode == 256) {
            languageMenuOpen = false;
            return true;
        }
        return false;
    }

    private boolean isLanguageControlHovered(double mouseX, double mouseY) {
        return mouseX >= languageIconX - 2.0f
            && mouseX <= languageIconX + LANGUAGE_ICON_SIZE + 2.0f
            && mouseY >= 11.0f - LANGUAGE_ICON_SIZE / 2.0f - 2.0f
            && mouseY <= 11.0f + LANGUAGE_ICON_SIZE / 2.0f + 2.0f;
    }

    private static String languageName(Language language) {
        return switch (language) {
            case EN_US -> "English";
            // case RU_RU -> "Русский";
            // case UK_UA -> "Українська";
            // case PL_PL -> "Polski";
        };
    }

    private static double minecraftScaleFactor() {
        return minecraftClient.getWindow().getScaleFactor();
    }

    private static boolean isBrightPixel(float x, float y, double scale) {
        ColorRGBA pixel = ColorRGBA.fromPixel(x * (float) scale,
            minecraftClient.getWindow().getFramebufferHeight() - y * (float) scale);
        return (pixel.getRed() + pixel.getGreen() + pixel.getBlue()) / 3.0f > 120.0f;
    }

    private static final class MenuBarItem {
        private final String labelKey;
        private final Runnable action;
        private final BooleanSupplier availability;
        private final Animation hoverAnimation = new Animation(220L, 0.0f, Easing.easeInOutCubicBezier);
        private float x;
        private float y;
        private float width;
        private float height;
        private float textX;
        private float textWidth;

        private MenuBarItem(String labelKey, Runnable action, BooleanSupplier availability) {
            this.labelKey = labelKey;
            this.action = action;
            this.availability = availability;
        }
    }
}
