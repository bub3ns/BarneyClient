package moscow.rockstar.modules.visuals.accessories;

import moscow.rockstar.events.EventListener;
import pyrock.events.render.Render3DEvent;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.visuals.accessories.renderers.HeadCrystalRenderer;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.shaders.DynamicLightShader;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import org.joml.Matrix4f;
import pyrock.utility.render.ColorRGBA;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Accessories", category = ModuleCategory.VISUALS, description = "modules.descriptions.accessories")
public class Accessories extends Module {
    private ModeSetting head;
    private ModeSetting.Option headNone;
    private ModeSetting.Option headCrystal;

    private ModeSetting back;
    private ModeSetting.Option backNone;

    private ModeSetting legs;
    private ModeSetting.Option legsNone;

    private ModeSetting boots;
    private ModeSetting.Option bootsNone;

    private NumberSetting amount;
    private NumberSetting radius;
    private NumberSetting height;
    private NumberSetting minScale;
    private NumberSetting maxScale;
    private NumberSetting minLifetime;
    private NumberSetting maxLifetime;
    private NumberSetting orbitSpeed;
    private NumberSetting rotationSpeed;
    private BooleanSetting bloom;
    private NumberSetting bloomSize;
    private BooleanSetting lighting;
    private NumberSetting lightRadius;
    private NumberSetting lightStrength;

    private BooleanSetting sync;
    private ColorSetting color;

    private final HeadCrystalRenderer headCrystalRenderer = new HeadCrystalRenderer();

    private final EventListener<Render3DEvent> onRender3DEvent = render3DEvent -> {
        if (!this.isEnabled() || !this.headCrystal.isSelected() || !this.lighting.isEnabled()) {
            return;
        }
        List<HeadCrystalRenderer.LightPoint> lights = this.headCrystalRenderer.getCurrentLights();
        if (lights.isEmpty()) {
            return;
        }
        float r = this.lightRadius.getValue();
        float strength = this.lightStrength.getValue() / 100.0f;
        ColorRGBA col = this.getColor();
        float red = col.getRed() / 255.0f;
        float green = col.getGreen() / 255.0f;
        float blue = col.getBlue() / 255.0f;

        List<DynamicLightShader.LightSource> lightSources = new ArrayList<>();
        int maxLights = Math.min(lights.size(), 48);
        for (int i = 0; i < maxLights; i++) {
            HeadCrystalRenderer.LightPoint lp = lights.get(i);
            float intensity = strength * lp.fade;
            if (intensity > 0.01f) {
                lightSources.add(new DynamicLightShader.LightSource(
                    lp.x, lp.y, lp.z,
                    r,
                    red, green, blue,
                    intensity
                ));
            }
        }

        if (lightSources.isEmpty()) {
            return;
        }

        Matrix4f matrix4f = new Matrix4f(render3DEvent.getProjectionMatrix()).mul(render3DEvent.getPositionMatrix());
        Matrix4f inverseViewProjection = new Matrix4f(matrix4f).invert();
        ShaderRenderer.particleLightRenderer.renderParticleLights(inverseViewProjection, 1.0f, lightSources);
    };

    public Accessories() {
        this.initializeSettings();
    }

    private void initializeSettings() {
        this.head = new ModeSetting(this, "modules.settings.accessories.head");
        this.headNone = new ModeSetting.Option(this.head, "modules.settings.accessories.none").select();
        this.headCrystal = new ModeSetting.Option(this.head, "modules.settings.accessories.head.crystal");

        this.back = new ModeSetting(this, "modules.settings.accessories.back");
        this.backNone = new ModeSetting.Option(this.back, "modules.settings.accessories.none").select();

        this.legs = new ModeSetting(this, "modules.settings.accessories.legs");
        this.legsNone = new ModeSetting.Option(this.legs, "modules.settings.accessories.none").select();

        this.boots = new ModeSetting(this, "modules.settings.accessories.boots");
        this.bootsNone = new ModeSetting.Option(this.boots, "modules.settings.accessories.none").select();

        this.amount = new NumberSetting((SettingOwner) this, "modules.settings.accessories.amount", () -> !this.headCrystal.isSelected())
                .setMinValue(5.0f).setMaxValue(20.0f).setStep(1.0f).setValue(15.0f);

        this.radius = new NumberSetting((SettingOwner) this, "modules.settings.accessories.radius", () -> !this.headCrystal.isSelected())
                .setMinValue(0.20f).setMaxValue(0.60f).setStep(0.01f).setValue(0.45f);

        this.height = new NumberSetting((SettingOwner) this, "modules.settings.accessories.height", () -> !this.headCrystal.isSelected())
                .setMinValue(0.30f).setMaxValue(0.80f).setStep(0.05f).setValue(0.60f);

        this.minScale = new NumberSetting((SettingOwner) this, "modules.settings.accessories.min_scale", () -> !this.headCrystal.isSelected())
                .setMinValue(0.01f).setMaxValue(0.05f).setStep(0.005f).setValue(0.04f);

        this.maxScale = new NumberSetting((SettingOwner) this, "modules.settings.accessories.max_scale", () -> !this.headCrystal.isSelected())
                .setMinValue(0.05f).setMaxValue(0.20f).setStep(0.005f).setValue(0.16f);

        this.minLifetime = new NumberSetting((SettingOwner) this, "modules.settings.accessories.min_lifetime", () -> !this.headCrystal.isSelected())
                .setMinValue(0.5f).setMaxValue(1.5f).setStep(0.1f).setValue(1.0f);

        this.maxLifetime = new NumberSetting((SettingOwner) this, "modules.settings.accessories.max_lifetime", () -> !this.headCrystal.isSelected())
                .setMinValue(1.5f).setMaxValue(6.0f).setStep(0.1f).setValue(4.0f);

        this.orbitSpeed = new NumberSetting((SettingOwner) this, "modules.settings.accessories.orbit_speed", () -> !this.headCrystal.isSelected())
                .setMinValue(-2.0f).setMaxValue(2.0f).setStep(0.1f).setValue(1.3f);

        this.rotationSpeed = new NumberSetting((SettingOwner) this, "modules.settings.accessories.rotation_speed", () -> !this.headCrystal.isSelected())
                .setMinValue(0.3f).setMaxValue(1.5f).setStep(0.05f).setValue(1.0f);

        this.bloom = new BooleanSetting((SettingOwner) this, "modules.settings.accessories.bloom", () -> !this.headCrystal.isSelected())
                .enable();

        this.bloomSize = new NumberSetting((SettingOwner) this, "modules.settings.accessories.bloom_size", () -> !this.headCrystal.isSelected() || !this.bloom.isEnabled())
                .setMinValue(0.10f).setMaxValue(1.50f).setStep(0.05f).setValue(0.70f);

        this.lighting = new BooleanSetting((SettingOwner) this, "modules.settings.accessories.lighting", () -> !this.headCrystal.isSelected())
                .enable();

        this.lightRadius = new NumberSetting((SettingOwner) this, "modules.settings.accessories.light_radius", () -> !this.headCrystal.isSelected() || !this.lighting.isEnabled())
                .setMinValue(0.5f).setMaxValue(2.0f).setStep(0.1f).setValue(1.5f);

        this.lightStrength = new NumberSetting((SettingOwner) this, "modules.settings.accessories.light_strength", () -> !this.headCrystal.isSelected() || !this.lighting.isEnabled())
                .setMinValue(5.0f).setMaxValue(30.0f).setStep(1.0f).setValue(20.0f);

        this.sync = new BooleanSetting(this, "theme.sync").enable();
        this.color = new ColorSetting(this, "modules.settings.accessories.color", this.sync::isEnabled)
                .setColor(new ColorRGBA(255.0f, 51.0f, 181.0f));
    }

    public HeadCrystalRenderer getHeadCrystalRenderer() {
        return this.headCrystalRenderer;
    }

    public ColorRGBA getColor() {
        return this.sync.isEnabled() ? ColorPalette.getAccentColor() : this.color.getColor();
    }

    public ModeSetting getHead() {
        return this.head;
    }

    public ModeSetting.Option getHeadNone() {
        return this.headNone;
    }

    public ModeSetting.Option getHeadCrystal() {
        return this.headCrystal;
    }

    public ModeSetting getBack() {
        return this.back;
    }

    public ModeSetting.Option getBackNone() {
        return this.backNone;
    }

    public ModeSetting getLegs() {
        return this.legs;
    }

    public ModeSetting.Option getLegsNone() {
        return this.legsNone;
    }

    public ModeSetting getBoots() {
        return this.boots;
    }

    public ModeSetting.Option getBootsNone() {
        return this.bootsNone;
    }

    public NumberSetting getAmount() {
        return this.amount;
    }

    public NumberSetting getRadius() {
        return this.radius;
    }

    public NumberSetting getHeight() {
        return this.height;
    }

    public NumberSetting getMinScale() {
        return this.minScale;
    }

    public NumberSetting getMaxScale() {
        return this.maxScale;
    }

    public NumberSetting getMinLifetime() {
        return this.minLifetime;
    }

    public NumberSetting getMaxLifetime() {
        return this.maxLifetime;
    }

    public NumberSetting getOrbitSpeed() {
        return this.orbitSpeed;
    }

    public NumberSetting getRotationSpeed() {
        return this.rotationSpeed;
    }

    public BooleanSetting getBloom() {
        return this.bloom;
    }

    public NumberSetting getBloomSize() {
        return this.bloomSize;
    }

    public BooleanSetting getLighting() {
        return this.lighting;
    }

    public NumberSetting getLightRadius() {
        return this.lightRadius;
    }

    public NumberSetting getLightStrength() {
        return this.lightStrength;
    }
}
