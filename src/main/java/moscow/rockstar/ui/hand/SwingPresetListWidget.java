package moscow.rockstar.ui.hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.hand.HandSwingPreset;
import moscow.rockstar.render.hand.HandSwingPresetManager;
import moscow.rockstar.render.hand.HandSwingSettings;
import moscow.rockstar.render.hand.SwingPresetFile;
import moscow.rockstar.render.hand.SwingPresetFileManager;
import moscow.rockstar.render.state.UiScissorStack;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.CursorManager;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.state.ScrollOffset;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.TextInputField;

/**
 * The preset list that sits at the top of the "presets" popup on the swing-animation
 * screen. Faithful port of the original {@code rockstar/ilIlil/iiIIIiiI}.
 *
 * <p>It renders two scrolled lists inside one clipped viewport - first the built-in
 * {@link HandSwingPreset}s owned by the {@link HandSwingPresetManager}, then every
 * user-saved {@link SwingPresetFile} except {@code autosave} - followed by a name-entry
 * row with a "plus" submit button.</p>
 *
 * <p>The per-row hover/active {@link Animation}s live on the row objects themselves, as
 * in the original: {@code iiIIIiii} and {@code iiIIiIII} each own two
 * {@code new IiiiIiIii(300L, IiiiIiiII.III)} fields, exposed here as
 * {@code getHoverAnimation()} / {@code getActiveAnimation()}.</p>
 */
public class SwingPresetListWidget
extends SettingWidget {
    /** Original field {@code I:IiiiIiIii} - drives the "plus" submit button. */
    private final Animation submitAnimation = new Animation(300L, Easing.easeOutBack);
    /** Original field {@code I:iIIiIIIii}. */
    private final ScrollOffset scroll = new ScrollOffset();
    /** Original field {@code i:IiiiIiIii} - smooths the widget height. */
    private final Animation heightAnimation = new Animation(300L, Easing.easeOutBackSoft);
    /** Original field {@code I:IiIiIIIII}. */
    private final TextInputField nameField;

    public SwingPresetListWidget() {
        this.nameField = new TextInputField(Font.REGULAR.metrics(8.0f));
        this.nameField.setPlaceholder(Localization.translate("type_name"));
    }

    @Override
    protected void renderContent(RockstarDrawContext drawContext) {
        HandSwingPresetManager manager = RockstarClient.create().getHandSwingPresetManager();
        SwingPresetFileManager fileManager = RockstarClient.create().getSwingPresetFileManager();
        List<SwingPresetFile> files = fileManager.getPresets();
        float listX = this.x + 8.0f;
        float listY = this.y - 1.0f;
        float listWidth = this.width - 16.0f;
        this.scroll.update();
        drawContext.drawRoundedRect(listX - 1.0f, listY + 7.0f, listWidth + 2.0f, 8.0f + this.height - 46.0f, WidgetState.uniform(6.0f), ColorPalette.getPanelColor().withAlpha(76.5f));
        UiScissorStack.push(drawContext.getMatrices(), listX - 1.0f, listY + 7.5f, listWidth + 2.0f, 7.0f + this.height - 46.0f);
        float rowOffset = 0.0f;
        for (HandSwingPreset preset : RockstarClient.create().getHandSwingPresetManager().getPresets()) {
            float rowY = (float)((double)(listY + 14.0f + rowOffset) - (double)this.scroll.getOffset());
            boolean hovered = UiUtils.contains(listX - 1.0f, listY + 7.5f, listWidth + 2.0f, 7.0f + this.height - 46.0f, drawContext) && UiUtils.contains(listX - 1.0f, rowY - 4.0f, listWidth + 2.0f, 12.0, drawContext.mouseX(), drawContext.mouseY());
            Animation rowHover = preset.getHoverAnimation();
            Animation rowActive = preset.getActiveAnimation();
            rowHover.setReverse(hovered);
            rowActive.setReverse(Objects.equals(preset.getName(), manager.getSelectedPresetName()));
            drawContext.drawFadeoutText(Font.REGULAR.metrics(7.0f), Localization.translate(preset.getName()), listX + 7.0f, rowY + 0.5f, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * rowHover.getValue() + 0.25f * rowActive.getValue())), 0.8f, 1.0f, listWidth - 12.0f - rowActive.getValue() * 10.0f);
            if (hovered) {
                CursorManager.request(Cursor.HAND);
            }
            if (rowActive.getValue() >= 0.0f) {
                drawContext.drawIcon("check", listX + listWidth - 11.0f - rowActive.getValue() * 2.0f, rowY, 6.0f, ColorPalette.getPrimaryTextColor().withAlpha(rowActive.getValue() * 255.0f));
            }
            rowOffset += 12.0f;
        }
        for (SwingPresetFile file : files) {
            if (file.getName().equals("autosave")) {
                continue;
            }
            float rowY = (float)((double)(listY + 14.0f + rowOffset) - (double)this.scroll.getOffset());
            boolean hovered = UiUtils.contains(listX - 1.0f, listY + 7.5f, listWidth + 2.0f, 7.0f + this.height - 46.0f, drawContext) && UiUtils.contains(listX - 1.0f, rowY - 4.0f, listWidth + 2.0f, 12.0, drawContext.mouseX(), drawContext.mouseY());
            Animation rowHover = file.getHoverAnimation();
            Animation rowActive = file.getActiveAnimation();
            rowHover.setReverse(hovered);
            rowActive.setReverse(Objects.equals(file.getName(), manager.getSelectedPresetName()));
            drawContext.drawFadeoutText(Font.REGULAR.metrics(7.0f), file.getName(), listX + 7.0f + 10.0f * rowHover.getValue(), rowY + 0.5f, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * rowHover.getValue() + 0.25f * rowActive.getValue())), 0.8f, 1.0f, listWidth - 12.0f - rowActive.getValue() * 10.0f - 10.0f * rowHover.getValue());
            if (hovered) {
                CursorManager.request(Cursor.HAND);
            }
            if (rowHover.getValue() >= 0.0f) {
                drawContext.drawIcon("trash", listX + 7.0f * rowHover.getValue(), rowY, 6.0f, ColorPalette.getPrimaryTextColor().withAlpha(rowHover.getValue() * 255.0f));
            }
            if (rowActive.getValue() >= 0.0f) {
                drawContext.drawIcon("check", listX + listWidth - 11.0f - rowActive.getValue() * 2.0f, rowY, 6.0f, ColorPalette.getPrimaryTextColor().withAlpha(rowActive.getValue() * 255.0f));
            }
            rowOffset += 12.0f;
        }
        UiScissorStack.pop();
        drawContext.drawRoundedRect(listX - 1.0f, listY + this.height - 25.0f, listWidth + 2.0f, 20.0f, WidgetState.uniform(6.0f), ColorPalette.getPanelColor().mulAlpha(0.3f));
        drawContext.drawIcon("plus", listX + listWidth - 2.0f * this.submitAnimation.getValue() - 10.0f, listY + this.height - 25.0f + 6.0f, 8.0f, ColorPalette.getPrimaryTextColor().mulAlpha(this.submitAnimation.getValue()));
        this.nameField.setBounds(listX - 1.0f, listY + this.height - 25.0f, listWidth + 2.0f - 12.0f, 20.0f);
        this.nameField.setOpacity(1.0f);
        this.nameField.render(drawContext);
        this.submitAnimation.setReverse(!this.nameField.getText().isBlank());
        if (UiUtils.contains(listX + listWidth - 2.0f - 10.0f, listY + this.height - 25.0f + 6.0f, 8.0, 8.0, drawContext) && this.submitAnimation.getValue() > 0.0f) {
            CursorManager.request(Cursor.HAND);
        }
        this.scroll.setLimit(-rowOffset + this.height - 20.0f - 25.0f);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, PointerAction action) {
        this.nameField.mouseClicked(mouseX, mouseY, action);
        HandSwingPresetManager manager = RockstarClient.create().getHandSwingPresetManager();
        SwingPresetFileManager fileManager = RockstarClient.create().getSwingPresetFileManager();
        List<SwingPresetFile> files = fileManager.getPresets();
        float listX = this.x + 8.0f;
        float listY = this.y - 1.0f;
        float listWidth = this.width - 16.0f;
        float rowOffset = 0.0f;
        for (HandSwingPreset preset : RockstarClient.create().getHandSwingPresetManager().getPresets()) {
            float rowY = (float)((double)(listY + 14.0f + rowOffset) - (double)this.scroll.getOffset());
            boolean hovered = UiUtils.contains(listX - 1.0f, listY + 7.5f, listWidth + 2.0f, 7.0f + this.height - 46.0f, mouseX, mouseY) && UiUtils.contains(listX - 1.0f, rowY - 4.0f, listWidth + 2.0f, 12.0, mouseX, mouseY);
            if (hovered && action == PointerAction.LEFT_CLICK) {
                manager.selectPreset(preset);
            }
            rowOffset += 12.0f;
        }
        for (SwingPresetFile file : new ArrayList<SwingPresetFile>(files)) {
            if (file.getName().equals("autosave")) {
                continue;
            }
            float rowY = (float)((double)(listY + 14.0f + rowOffset) - (double)this.scroll.getOffset());
            boolean hovered = UiUtils.contains(listX - 1.0f, listY + 7.5f, listWidth + 2.0f, 7.0f + this.height - 46.0f, mouseX, mouseY) && UiUtils.contains(listX - 1.0f, rowY - 4.0f, listWidth + 2.0f, 12.0, mouseX, mouseY);
            if (hovered && UiUtils.contains(listX + 7.0f, rowY, 6.0, 6.0, mouseX, mouseY) && action == PointerAction.LEFT_CLICK) {
                file.delete();
            } else if (hovered && action == PointerAction.LEFT_CLICK) {
                if (fileManager.getActivePreset() != null) {
                    fileManager.getActivePreset().save();
                }
                manager.setSelectedPresetName(file.getName());
                file.load();
            }
            rowOffset += 12.0f;
        }
        if (UiUtils.contains(listX + listWidth - 2.0f - 10.0f, listY + this.height - 25.0f + 6.0f, 8.0, 8.0, mouseX, mouseY) && !this.nameField.getText().isBlank()) {
            this.createPreset();
        }
    }

    /**
     * Original {@code iiIIIiiI#i()V}: resets the shared swing settings to the defaults the
     * {@link HandSwingPresetManager} constructor uses, then creates and loads a preset file
     * named after the text field.
     */
    private void createPreset() {
        SwingPresetFileManager fileManager = RockstarClient.create().getSwingPresetFileManager();
        HandSwingPresetManager manager = RockstarClient.create().getHandSwingPresetManager();
        manager.getEasingSetting().setStartControlPoint(0.5f, 1.0f).setEndControlPoint(0.5f, 0.0f);
        manager.getBackSwingSetting().setActiveExtra(true);
        manager.getSwingSpeedSetting().updateValue(2.0f);
        for (Setting setting : RockstarClient.create().getHandSwingPresetManager().getInitialSwing().getSettings()) {
            if (!(setting instanceof HandSwingSettings.MirroredNumberSetting)) continue;
            HandSwingSettings.MirroredNumberSetting mirrored = (HandSwingSettings.MirroredNumberSetting)setting;
            mirrored.updateValue(0.0f);
        }
        for (Setting setting : RockstarClient.create().getHandSwingPresetManager().getFinalSwing().getSettings()) {
            if (!(setting instanceof HandSwingSettings.MirroredNumberSetting)) continue;
            HandSwingSettings.MirroredNumberSetting mirrored = (HandSwingSettings.MirroredNumberSetting)setting;
            mirrored.updateValue(0.0f);
        }
        manager.setSelectedPresetName(this.nameField.getText());
        fileManager.createPreset(this.nameField.getText());
        SwingPresetFile created = fileManager.find(this.nameField.getText());
        if (created != null) {
            created.load();
        }
        this.nameField.clear();
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, PointerAction action) {
        this.nameField.mouseReleased(mouseX, mouseY, action);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && !this.nameField.getText().isBlank()) {
            this.createPreset();
            return;
        }
        this.nameField.keyPressed(keyCode, scanCode, modifiers);
        if (this.contains(UiUtils.mousePosition().getX(), UiUtils.mousePosition().getY())) {
            this.scroll.handleKey(keyCode);
        }
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        this.nameField.charTyped(character, modifiers);
        return super.charTyped(character, modifiers);
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scroll.scrollBy(verticalAmount);
    }

    @Override
    public float getHeight() {
        SwingPresetFileManager fileManager = RockstarClient.create().getSwingPresetFileManager();
        List<SwingPresetFile> files = fileManager.getPresets();
        this.height = this.heightAnimation.update(Math.min(files.size() * 12 + RockstarClient.create().getHandSwingPresetManager().getPresets().size() * 12 - 12, 182) + 46);
        return this.height;
    }
}
