package moscow.rockstar.render.hand;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.visuals.hand.HandSwingState;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.EasingSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingGroup;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

/** Owns the editable hand-swing state and the built-in animation presets. */
public final class HandSwingPresetManager {
    private final List<HandSwingPreset> presets = new ArrayList<>();
    private String selectedPresetName = "autosave";
    private final SettingGroup settings = new SettingGroup();
    private final HandSwingSettings initialSwing = new HandSwingSettings();
    private final HandSwingSettings finalSwing = new HandSwingSettings();
    private final EasingSetting easingSetting = new EasingSetting(this.settings, "animation")
            .setStartControlPoint(0.5f, 1.0f)
            .setEndControlPoint(0.5f, 0.0f);
    private final BooleanSetting backSwingSetting = new BooleanSetting(this.settings, "swing.back").enable();
    private final NumberSetting swingSpeedSetting = new NumberSetting(this.settings, "swing.wing_speed")
            .setStep(0.5f)
            .setMinValue(1.0f)
            .setMaxValue(5.0f)
            .setValue(2.0f);

    public HandSwingPresetManager() {
        registerDefaultPresets();
        if (!presets.isEmpty()) {
            selectPreset(presets.getFirst());
        }
    }

    private void registerDefaultPresets() {
        presets.add(new HandSwingPreset(
                "swings.block_hit",
                new Vec2f(0.5f, 1.0f),
                new Vec2f(0.5f, 0.0f),
                true,
                2.0f,
                new HandSwingState(0.0f, -0.05f, -0.7f, 1.0500001f, -0.7f, -1.1f, -120.0f, -135.0f, -60.0f),
                new HandSwingState(0.0f, -0.05f, -0.7f, 1.0500001f, -0.7f, -1.1f, -120.0f, -180.0f, -60.0f)));
        presets.add(new HandSwingPreset(
                "swings.bonk",
                new Vec2f(0.40131578f, 0.53543305f),
                new Vec2f(0.0f, -0.24409449f),
                true,
                2.0f,
                new HandSwingState(0.0f, -0.4f, -0.65000004f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                new HandSwingState(0.0f, -0.4f, -0.65000004f, 0.0f, 0.0f, 0.0f, -45.0f, 0.0f, 0.0f)));
        presets.add(new HandSwingPreset(
                "swings.rotate_360",
                new Vec2f(0.43421054f, 0.61417323f),
                new Vec2f(0.04605263f, -0.26771653f),
                false,
                2.0f,
                new HandSwingState(0.0f, -0.4f, -0.65000004f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                new HandSwingState(0.0f, -0.4f, -0.65000004f, 0.0f, 0.0f, 0.0f, -360.0f, 0.0f, 0.0f)));
        presets.add(new HandSwingPreset(
                "swings.from_me",
                new Vec2f(0.42105263f, 0.87401575f),
                new Vec2f(0.3881579f, -0.4566929f),
                true,
                2.0f,
                new HandSwingState(0.0f, 0.0f, -1.1f, 0.2f, 0.0f, -0.1f, -135.0f, 45.0f, 60.0f),
                new HandSwingState(0.0f, 0.0f, -1.1f, 0.2f, 0.0f, -0.3f, -180.0f, 45.0f, 60.0f)));
    }

    public HandSwingState interpolateSwingState(float progress) {
        progress = easingSetting.getEasing().ease(progress, 0.0f, 1.0f, 1.0f);
        if (backSwingSetting.isEnabled()) {
            progress = MathHelper.sin(MathHelper.sqrt(progress) * (float) Math.PI);
        }
        return new HandSwingState(
                interpolate(initialSwing.getAnchorXSetting(), finalSwing.getAnchorXSetting(), progress),
                interpolate(initialSwing.getAnchorYSetting(), finalSwing.getAnchorYSetting(), progress),
                interpolate(initialSwing.getAnchorZSetting(), finalSwing.getAnchorZSetting(), progress),
                interpolate(initialSwing.getMoveXSetting(), finalSwing.getMoveXSetting(), progress),
                interpolate(initialSwing.getMoveYSetting(), finalSwing.getMoveYSetting(), progress),
                interpolate(initialSwing.getMoveZSetting(), finalSwing.getMoveZSetting(), progress),
                interpolate(initialSwing.getRotateXSetting(), finalSwing.getRotateXSetting(), progress),
                interpolate(initialSwing.getRotateYSetting(), finalSwing.getRotateYSetting(), progress),
                interpolate(initialSwing.getRotateZSetting(), finalSwing.getRotateZSetting(), progress));
    }

    private static float interpolate(NumberSetting from, NumberSetting to, float progress) {
        return MathUtils.interpolateDouble(from.getValue(), to.getValue(), progress);
    }

    public void selectPreset(HandSwingPreset preset) {
        if (preset == null) {
            return;
        }
        easingSetting.setStartControlPoint(preset.getStartControlPoint())
                .setEndControlPoint(preset.getEndControlPoint());
        backSwingSetting.setActiveExtra(preset.isBackSwing());
        swingSpeedSetting.updateValue(preset.getSwingSpeed());
        applySwingState(initialSwing, preset.getStartState());
        applySwingState(finalSwing, preset.getEndState());
        selectedPresetName = preset.getName();
        SwingPresetFileManager fileManager = RockstarClient.create().getSwingPresetFileManager();
        if (fileManager != null) {
            fileManager.setActivePreset(null);
        }
    }

    private static void applySwingState(HandSwingSettings settings, HandSwingState state) {
        settings.getAnchorXSetting().updateValue(state.getAnchorX());
        settings.getAnchorYSetting().updateValue(state.getAnchorY());
        settings.getAnchorZSetting().updateValue(state.getAnchorZ());
        settings.getMoveXSetting().updateValue(state.getMoveX());
        settings.getMoveYSetting().updateValue(state.getMoveY());
        settings.getMoveZSetting().updateValue(state.getMoveZ());
        settings.getRotateXSetting().updateValue(state.getRotateX());
        settings.getRotateYSetting().updateValue(state.getRotateY());
        settings.getRotateZSetting().updateValue(state.getRotateZ());
    }

    public String getSelectedPresetName() {
        SwingPresetFileManager fileManager = RockstarClient.create().getSwingPresetFileManager();
        SwingPresetFile active = fileManager == null ? null : fileManager.getActivePreset();
        return active == null ? selectedPresetName : active.getName();
    }

    public List<HandSwingPreset> getPresets() {
        return presets;
    }

    public SettingGroup getSettings() {
        return settings;
    }

    public HandSwingSettings getInitialSwing() {
        return initialSwing;
    }

    public HandSwingSettings getFinalSwing() {
        return finalSwing;
    }

    public EasingSetting getEasingSetting() {
        return easingSetting;
    }

    public BooleanSetting getBackSwingSetting() {
        return backSwingSetting;
    }

    public NumberSetting getSwingSpeedSetting() {
        return swingSpeedSetting;
    }

    public void setSelectedPresetName(String name) {
        selectedPresetName = name;
    }
}
