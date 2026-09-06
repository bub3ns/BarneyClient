/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.DrawContext
 *  net.minecraft.Screen
 */
package moscow.rockstar.ui.screens;

import java.util.Collection;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.hand.HandSwingPreset;
import moscow.rockstar.render.hand.HandSwingPresetManager;
import moscow.rockstar.render.hand.SwingPresetFile;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.ui.color.ColorPickerCallback;
import moscow.rockstar.ui.color.ColorPickerScreen;
import moscow.rockstar.ui.hand.SwingEditorWidget;
import moscow.rockstar.ui.hand.SwingPresetListWidget;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.screens.MinecraftScreenBase;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.TextInputField;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class ColorPresetsScreen
extends MinecraftScreenBase
implements ClientAccess,
WindowMetricsProvider {
    private final ColorPickerScreen presetPanel = new ColorPickerScreen(100.0f, 100.0f).addTextRow("presets").useLiquidGlass();
    private final ColorPickerScreen sharedSettingsPanel = new ColorPickerScreen(100.0f, 100.0f).addTextRow("shared").useLiquidGlass();
    private final SwingEditorWidget swingEditor = new SwingEditorWidget();

    public ColorPresetsScreen() {
        HandSwingPresetManager manager = RockstarClient.create().getHandSwingPresetManager();
        RockstarClient.create().getSwingPresetFileManager().reload();
        this.presetPanel.addSettingWidget(new SwingPresetListWidget());
        this.populateSettings(manager.getSettings().getSettings(), this.sharedSettingsPanel);
        HandSwingPresetManager selectionManager = RockstarClient.create().getHandSwingPresetManager();
        String selectedName = selectionManager.getSelectedPresetName();
        for (HandSwingPreset preset : RockstarClient.create().getHandSwingPresetManager().getPresets()) {
            if (!preset.getName().equals(selectedName)) {
                continue;
            }
            selectionManager.selectPreset(preset);
        }
    }

    @Override
    public void render(RockstarDrawContext drawContext) {
        float f = 210.0f;
        float f2 = 230.0f;
        float f3 = 360.0f + f;
        float f4 = WindowMetricsProvider.INSTANCE.width() / 2.0f - f3 / 2.0f;
        float f5 = INSTANCE.height() / 2.0f;
        float f6 = f5 - f2 / 2.0f;
        this.presetPanel.setWidth(170.0f);
        this.sharedSettingsPanel.setWidth(170.0f);
        this.presetPanel.setX(f4);
        this.sharedSettingsPanel.setX(f4 + 180.0f);
        this.presetPanel.setY(f6);
        this.sharedSettingsPanel.setY(f6);
        this.presetPanel.render(drawContext);
        this.sharedSettingsPanel.render(drawContext);
        this.swingEditor.setBounds(f4 + 360.0f, f6, f, f2);
        this.swingEditor.render(drawContext);
    }

    @Override
    public void onMouseClicked(double d, double d2, PointerAction pointerAction) {
        this.forEachPanel(panel -> panel.mouseClicked(d, d2, pointerAction));
        this.swingEditor.mouseClicked(d, d2, pointerAction);
    }

    @Override
    public void onMouseReleased(double d, double d2, PointerAction pointerAction) {
        this.forEachPanel(panel -> panel.mouseReleased(d, d2, pointerAction));
        this.swingEditor.mouseReleased(d, d2, pointerAction);
    }

    @Override
    public void onMouseDragged(double d, double d2, PointerAction pointerAction, double d3, double d4) {
        this.swingEditor.handlePointerDrag(d, d2, pointerAction, d3, d4);
    }

    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        this.forEachPanel(panel -> panel.mouseScrolled(d, d2, d3, d4));
        this.swingEditor.mouseScrolled(d, d2, d3, d4);
        return super.mouseScrolled(d, d2, d3, d4);
    }

    public boolean keyPressed(int n, int n2, int n3) {
        if (Screen.hasControlDown() && (n == 90 || n == 89)) {
            boolean bl;
            boolean bl2 = bl = n == 89;
            if (bl ? this.presetPanel.isColorPickerScreenTargetReady() : this.presetPanel.isColorPickerReady()) {
                return true;
            }
            if (bl ? this.sharedSettingsPanel.isColorPickerScreenTargetReady() : this.sharedSettingsPanel.isColorPickerReady()) {
                return true;
            }
            this.swingEditor.keyPressed(n, n2, n3);
            return true;
        }
        this.forEachPanel(panel -> panel.keyPressed(n, n2, n3));
        this.swingEditor.keyPressed(n, n2, n3);
        return super.keyPressed(n, n2, n3);
    }

    public boolean charTyped(char c, int n) {
        this.forEachPanel(panel -> panel.charTyped(c, n));
        return super.charTyped(c, n);
    }

    private void populateSettings(Collection<Setting> collection, ColorPickerScreen panel) {
        for (Setting setting : collection) {
            panel.addSetting(setting);
        }
    }

    private void forEachPanel(ColorPickerCallback callback) {
        callback.call(this.presetPanel);
        callback.call(this.sharedSettingsPanel);
    }

    public boolean shouldPause() {
        return false;
    }

    public void renderBackground(DrawContext ServerConfigException, int n, int n2, float f) {
    }

    public void close() {
        SwingPresetFile activePreset = RockstarClient.create().getSwingPresetFileManager().getActivePreset();
        if (activePreset != null) {
            activePreset.save();
        }
        TextInputField activeField = TextInputField.getActiveField();
        if (activeField != null) {
            activeField.setFocused(false);
        }
        super.close();
        Menu.updateMenuState();
    }
}
