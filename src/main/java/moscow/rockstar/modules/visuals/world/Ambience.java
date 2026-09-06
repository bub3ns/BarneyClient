/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.StatusEffect
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.StatusEffects
 *  net.minecraft.BlockRenderView
 *  net.minecraft.LightType
 *  net.minecraft.Biome
 *  net.minecraft.Position
 *  net.minecraft.Vec3d
 *  net.minecraft.WorldTimeUpdateS2CPacket
 *  net.minecraft.Identifier
 *  net.minecraft.MathHelper
 *  net.minecraft.ChunkSectionPos
 *  net.minecraft.Camera
 *  net.minecraft.CameraSubmersionType
 *  net.minecraft.RegistryEntry
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 */
package moscow.rockstar.modules.visuals.world;

import java.util.HashSet;
import java.util.Set;
import lombok.Generated;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.render.world.SkyboxShaderPair;
import moscow.rockstar.render.world.DynamicLightGrid;
import moscow.rockstar.render.assets.AssetImageLoader;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.world.VolumetricFogRenderer;
import moscow.rockstar.render.postprocess.WetWorldShader;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.shaders.TimedAccentShader;
import moscow.rockstar.render.world.WorldOverlayRenderer;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.TextLabelSetting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.client.render.Camera;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.registry.entry.RegistryEntry;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Ambience", category=ModuleCategory.VISUALS, disableLocked=true, description="modules.descriptions.ambience")
public class Ambience
extends Module {
    private MultiBooleanSetting changeColor;
    private MultiBooleanSetting.Option sky;
    private MultiBooleanSetting.Option clouds;
    private MultiBooleanSetting.Option stars;
    private BooleanSetting syncSetting;
    private ColorSetting skyColor;
    private ColorSetting cloudColor;
    private ColorSetting starsColor;
    private ModeSetting skybox;
    private ModeSetting.Option defaultSkybox;
    private ModeSetting.Option brightClouds;
    private ModeSetting.Option lake;
    private ModeSetting.Option cloudSpace;
    private ModeSetting.Option clearEvening;
    private ModeSetting.Option underwater;
    private BooleanSetting shader;
    private ModeSetting shaderType;
    private ModeSetting.Option nebula;
    private ModeSetting.Option caustic;
    private ModeSetting.Option galaxy;
    private ModeSetting.Option space;
    private NumberSetting shaderOpacity;
    private BooleanSetting rain;
    private NumberSetting density;
    private NumberSetting opacity;
    private NumberSetting drops;
    private NumberSetting splashes;
    private BooleanSetting fog;
    private ModeSetting colorMode;
    private ModeSetting.Option biome;
    private ModeSetting.Option theme;
    private ModeSetting.Option custom;
    private ColorSetting color;
    private NumberSetting fogDensity;
    private NumberSetting coverage;
    private NumberSetting level;
    private NumberSetting thickness;
    private ModeSetting quality;
    private ModeSetting.Option low;
    private ModeSetting.Option high;
    private BooleanSetting wetWorld;
    private NumberSetting reflection;
    private NumberSetting reflectionAmount;
    private ModeSetting wetWorldQuality;
    private ModeSetting.Option wetWorldLowQuality;
    private ModeSetting.Option medium;
    private ModeSetting.Option wetWorldHighQuality;
    private BooleanSetting colorIsolation;
    private ColorSetting isolationColor;
    private ModeSetting mode;
    private ModeSetting.Option strict;
    private ModeSetting.Option balanced;
    private ModeSetting.Option loose;
    private ModeSetting.Option isolationCustom;
    private NumberSetting sensitivity;
    private NumberSetting background;
    private BooleanSetting customTime;
    private NumberSetting time;
    public BooleanSetting nightMode;
    public BooleanSetting bright;
    private ModeSetting lightingMode;
    private ModeSetting.Option gamma;
    private ModeSetting.Option effect;
    private ModeSetting.Option dynamic;
    private NumberSetting radius;
    private NumberSetting light;
    private BooleanSetting onlyInCave;
    private BooleanSetting sync;
    private ColorSetting nightModeColor;
    private NumberSetting strength;
    private static final float DEFAULT_REFLECTION_ALPHA = 0.35f;
    private static final float DEFAULT_CLOUD_ALPHA = 0.45f;
    private static final float DEFAULT_SKYBOX_HEIGHT = 24.0f;
    private final WetWorldShader.Uniforms reflectionState = new WetWorldShader.Uniforms();
    private final WorldOverlayRenderer rainState = new WorldOverlayRenderer();
    private final VolumetricFogRenderer fogRenderer = new VolumetricFogRenderer();
    private long worldTime;
    private boolean worldTimeUpdated;
    private BlockPos lastFogBlock;
    private float lastFogHeight = -1.0f;
    private int keyCode = -1;
    private boolean overlayVisible;
    private final Animation ambienceAnimation = new Animation(450L, Easing.easeInOutSine);
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = receivePacketEvent -> {
        if (receivePacketEvent.getPacket() instanceof WorldTimeUpdateS2CPacket && this.customTime.isEnabled()) {
            receivePacketEvent.cancel();
        }
    };
    private final EventListener<Render3DEvent> onRender3DEvent = render3DEvent -> {
        if (Ambience.minecraftClient.world == null || Ambience.minecraftClient.player == null) {
            this.worldTimeUpdated = false;
            this.resetReflectionState();
            return;
        }
        this.updateTimeState(this.isAmbientWorldReady());
        Camera class_41842 = render3DEvent.getCamera();
        if (class_41842 == null || !class_41842.isReady()) {
            return;
        }
        if (class_41842.getSubmersionType() != CameraSubmersionType.NONE) {
            return;
        }
        if (this.rain.isEnabled()) {
            this.rainState.renderRainOverlay(class_41842, render3DEvent.getPositionMatrix(), render3DEvent.getProjectionMatrix(), this.density.getValue() / 100.0f, this.opacity.getValue() / 100.0f, this.drops.getValue() / 100.0f, this.splashes.getValue() / 100.0f);
        }
        if (this.fog.isEnabled()) {
            this.fogRenderer.renderFog(class_41842, render3DEvent.getPositionMatrix(), render3DEvent.getProjectionMatrix(), this.getBiomeFogColor(class_41842.getPos()), this.fogDensity.getValue() / 100.0f, this.coverage.getValue() / 100.0f, this.level.getValue(), this.thickness.getValue(), this.high.isSelected());
        }
    };

    public Ambience() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.customTime = new BooleanSetting(this, "modules.settings.ambience.custom_time").enable();
        this.time = new NumberSetting((SettingOwner)this, "modules.settings.ambience.time", () -> !this.customTime.isEnabled()).setStep(500.0f).setMinValue(0.0f).setMaxValue(24000.0f).setValue(17000.0f);
        this.bright = new BooleanSetting((SettingOwner)this, "modules.settings.ambience.bright", () -> this.nightMode.isEnabled()).enable();
        this.lightingMode = new ModeSetting((SettingOwner)this, "modules.settings.ambience.mode", () -> !this.isAmbientLightingReady());
        this.gamma = new ModeSetting.Option(this.lightingMode, "modules.settings.ambience.mode.gamma");
        this.effect = new ModeSetting.Option(this.lightingMode, "modules.settings.ambience.mode.effect");
        this.dynamic = new ModeSetting.Option(this.lightingMode, "modules.settings.ambience.mode.dynamic").select();
        this.radius = new NumberSetting((SettingOwner)this, "modules.settings.ambience.dynamic.radius", () -> !this.isAmbientLightingReady() || !this.dynamic.isSelected()).setMinValue(4.0f).setMaxValue(12.0f).setStep(0.5f).setValue(12.0f);
        this.light = new NumberSetting((SettingOwner)this, "modules.settings.ambience.dynamic.light", () -> !this.isAmbientLightingReady() || !this.dynamic.isSelected()).setMinValue(8.0f).setMaxValue(15.0f).setStep(1.0f).setValue(15.0f);
        this.onlyInCave = new BooleanSetting((SettingOwner)this, "modules.settings.ambience.dynamic.only_in_cave", () -> !this.isAmbientLightingReady() || !this.dynamic.isSelected()).enable();
        new TextLabelSetting(this, "modules.settings.ambience.section.color").asCentered().setFontSizeOffset(2);
        this.changeColor = new MultiBooleanSetting(this, "modules.settings.ambience.change_color");
        this.sky = new MultiBooleanSetting.Option(this.changeColor, "modules.settings.ambience.change_color.sky").select();
        this.clouds = new MultiBooleanSetting.Option(this.changeColor, "modules.settings.ambience.change_color.clouds").select();
        this.stars = new MultiBooleanSetting.Option(this.changeColor, "modules.settings.ambience.change_color.stars").select();
        this.syncSetting = new BooleanSetting(this, "theme.sync").enable();
        this.skyColor = new ColorSetting(this, "modules.settings.ambience.sky_color", () -> this.syncSetting.isEnabled() || !this.sky.isSelected()).setColor(ColorPalette.getAccentColor()).setAlphaEnabled(false);
        this.cloudColor = new ColorSetting(this, "modules.settings.ambience.cloud_color", () -> this.syncSetting.isEnabled() || !this.clouds.isSelected()).setColor(ColorPalette.getAccentColor()).setAlphaEnabled(false);
        this.starsColor = new ColorSetting(this, "modules.settings.ambience.stars_color", () -> this.syncSetting.isEnabled() || !this.stars.isSelected()).setColor(ColorPalette.getAccentColor());
        this.nightMode = new BooleanSetting(this, "modules.settings.ambience.night_mode");
        this.sync = new BooleanSetting((SettingOwner)this, "modules.settings.ambience.night_mode.sync", () -> !this.nightMode.isEnabled()).enable();
        this.nightModeColor = new ColorSetting(this, "modules.settings.ambience.night_mode.color", () -> !this.nightMode.isEnabled() || this.sync.isEnabled()).setColor(new ColorRGBA(80.0f, 120.0f, 220.0f, 255.0f)).setAlphaEnabled(false);
        this.strength = new NumberSetting((SettingOwner)this, "modules.settings.ambience.night_mode.strength", () -> !this.nightMode.isEnabled()).setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f).setValue(70.0f);
        this.colorIsolation = new BooleanSetting(this, "modules.settings.ambience.color_isolation");
        this.isolationColor = new ColorSetting(this, "modules.settings.ambience.color_isolation.color", () -> !this.colorIsolation.isEnabled()).setColor(new ColorRGBA(0.0f, 122.0f, 255.0f, 255.0f)).setAlphaEnabled(false);
        this.mode = new ModeSetting((SettingOwner)this, "modules.settings.ambience.color_isolation.mode", () -> !this.colorIsolation.isEnabled());
        this.strict = new ModeSetting.Option(this.mode, "modules.settings.ambience.color_isolation.mode.strict");
        this.balanced = new ModeSetting.Option(this.mode, "modules.settings.ambience.color_isolation.mode.balanced").select();
        this.loose = new ModeSetting.Option(this.mode, "modules.settings.ambience.color_isolation.mode.loose");
        this.isolationCustom = new ModeSetting.Option(this.mode, "modules.settings.ambience.color_isolation.mode.custom");
        this.sensitivity = new NumberSetting((SettingOwner)this, "modules.settings.ambience.color_isolation.sensitivity", () -> !this.colorIsolation.isEnabled() || !this.isolationCustom.isSelected()).setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f).setValue(50.0f);
        this.background = new NumberSetting((SettingOwner)this, "modules.settings.ambience.color_isolation.background", () -> !this.colorIsolation.isEnabled()).setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f).setValue(0.0f);
        this.skybox = new ModeSetting(this, "modules.settings.ambience.skybox");
        this.defaultSkybox = new ModeSetting.Option(this.skybox, "modules.settings.ambience.skybox.default");
        this.brightClouds = new ModeSetting.Option(this.skybox, "modules.settings.ambience.skybox.bright_clouds").select();
        this.lake = new ModeSetting.Option(this.skybox, "modules.settings.ambience.skybox.lake");
        this.cloudSpace = new ModeSetting.Option(this.skybox, "modules.settings.ambience.skybox.cloud_space");
        this.clearEvening = new ModeSetting.Option(this.skybox, "modules.settings.ambience.skybox.clear_evening");
        this.underwater = new ModeSetting.Option(this.skybox, "modules.settings.ambience.skybox.underwater");
        this.shader = new BooleanSetting(this, "modules.settings.ambience.shader");
        this.shaderType = new ModeSetting((SettingOwner)this, "modules.settings.ambience.shader_type", () -> !this.shader.isEnabled());
        this.nebula = new ModeSetting.Option(this.shaderType, "modules.settings.ambience.shader_type.nebula").select();
        this.caustic = new ModeSetting.Option(this.shaderType, "modules.settings.ambience.shader_type.caustic");
        this.galaxy = new ModeSetting.Option(this.shaderType, "modules.settings.ambience.shader_type.galaxy");
        this.space = new ModeSetting.Option(this.shaderType, "modules.settings.ambience.shader_type.space");
        this.shaderOpacity = new NumberSetting((SettingOwner)this, "modules.settings.ambience.shader_opacity", () -> !this.shader.isEnabled()).setMinValue(0.0f).setMaxValue(100.0f).setStep(5.0f).setValue(70.0f);
        new TextLabelSetting(this, "modules.settings.ambience.section.fog").asCentered().setFontSizeOffset(2);
        this.fog = new BooleanSetting(this, "modules.settings.ambience.fog");
        this.colorMode = new ModeSetting((SettingOwner)this, "modules.settings.ambience.fog.color_mode", () -> !this.fog.isEnabled());
        this.biome = new ModeSetting.Option(this.colorMode, "modules.settings.ambience.fog.color_mode.biome").select();
        this.theme = new ModeSetting.Option(this.colorMode, "modules.settings.ambience.fog.color_mode.theme");
        this.custom = new ModeSetting.Option(this.colorMode, "modules.settings.ambience.fog.color_mode.custom");
        this.color = new ColorSetting(this, "modules.settings.ambience.fog.color", () -> !this.fog.isEnabled() || !this.custom.isSelected()).setColor(new ColorRGBA(205.0f, 210.0f, 220.0f, 255.0f)).setAlphaEnabled(false);
        this.fogDensity = new NumberSetting((SettingOwner)this, "modules.settings.ambience.fog.density", () -> !this.fog.isEnabled()).setMinValue(10.0f).setMaxValue(250.0f).setStep(5.0f).setValue(90.0f).setUnit("%");
        this.coverage = new NumberSetting((SettingOwner)this, "modules.settings.ambience.fog.coverage", () -> !this.fog.isEnabled()).setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f).setValue(55.0f).setUnit("%");
        this.level = new NumberSetting((SettingOwner)this, "modules.settings.ambience.fog.level", () -> !this.fog.isEnabled()).setMinValue(-64.0f).setMaxValue(320.0f).setStep(1.0f).setValue(66.0f);
        this.thickness = new NumberSetting((SettingOwner)this, "modules.settings.ambience.fog.thickness", () -> !this.fog.isEnabled()).setMinValue(2.0f).setMaxValue(48.0f).setStep(1.0f).setValue(6.0f);
        this.quality = new ModeSetting((SettingOwner)this, "modules.settings.ambience.fog.quality", () -> !this.fog.isEnabled());
        this.low = new ModeSetting.Option(this.quality, "modules.settings.ambience.fog.quality.low");
        this.high = new ModeSetting.Option(this.quality, "modules.settings.ambience.fog.quality.high").select();
        new TextLabelSetting(this, "modules.settings.ambience.section.wet_world").asCentered().setFontSizeOffset(2);
        this.wetWorld = new BooleanSetting(this, "modules.settings.ambience.wet_world");
        this.reflection = new NumberSetting((SettingOwner)this, "modules.settings.ambience.wet_world.reflection", () -> !this.wetWorld.isEnabled()).setMinValue(0.0f).setMaxValue(100.0f).setStep(5.0f).setValue(70.0f).setUnit("%");
        this.reflectionAmount = new NumberSetting((SettingOwner)this, "modules.settings.ambience.wet_world.amount", () -> !this.wetWorld.isEnabled()).setMinValue(0.0f).setMaxValue(100.0f).setStep(5.0f).setValue(60.0f).setUnit("%");
        this.wetWorldQuality = new ModeSetting((SettingOwner)this, "modules.settings.ambience.wet_world.quality", () -> !this.wetWorld.isEnabled());
        this.wetWorldLowQuality = new ModeSetting.Option(this.wetWorldQuality, "modules.settings.ambience.wet_world.quality.low");
        this.medium = new ModeSetting.Option(this.wetWorldQuality, "modules.settings.ambience.wet_world.quality.medium").select();
        this.wetWorldHighQuality = new ModeSetting.Option(this.wetWorldQuality, "modules.settings.ambience.wet_world.quality.high");
        new TextLabelSetting(this, "modules.settings.ambience.section.rain").asCentered().setFontSizeOffset(2);
        this.rain = new BooleanSetting(this, "modules.settings.ambience.rain");
        this.density = new NumberSetting((SettingOwner)this, "modules.settings.ambience.rain.density", () -> !this.rain.isEnabled()).setMinValue(10.0f).setMaxValue(200.0f).setStep(5.0f).setValue(100.0f).setUnit("%");
        this.opacity = new NumberSetting((SettingOwner)this, "modules.settings.ambience.rain.opacity", () -> !this.rain.isEnabled()).setMinValue(5.0f).setMaxValue(100.0f).setStep(5.0f).setValue(45.0f).setUnit("%");
        this.drops = new NumberSetting((SettingOwner)this, "modules.settings.ambience.rain.drops", () -> !this.rain.isEnabled()).setMinValue(0.0f).setMaxValue(100.0f).setStep(5.0f).setValue(60.0f).setUnit("%");
        this.splashes = new NumberSetting((SettingOwner)this, "modules.settings.ambience.rain.splashes", () -> !this.rain.isEnabled()).setMinValue(0.0f).setMaxValue(100.0f).setStep(5.0f).setValue(60.0f).setUnit("%");
    }

    public boolean isAmbientLightingReady() {
        return this.bright.isEnabled() && !this.nightMode.isEnabled();
    }

    public boolean isWeatherRenderReady() {
        return this.isEnabled() && this.nightMode.isEnabled();
    }

    public Vector3f getCameraPositionVector() {
        ColorRGBA colorRGBA = this.sync.isEnabled() ? ColorPalette.getAccentColor() : this.nightModeColor.getColor();
        return new Vector3f(colorRGBA.getRed() / 255.0f, colorRGBA.getGreen() / 255.0f, colorRGBA.getBlue() / 255.0f);
    }

    public float getFogDistance() {
        return this.strength.getValue() / 100.0f;
    }

    private ColorRGBA getBiomeFogColor(Vec3d VanillaChestLootTableGenerator) {
        if (this.theme.isSelected()) {
            return ColorPalette.getAccentColor();
        }
        if (this.custom.isSelected()) {
            return this.color.getColor();
        }
        // DELIBERATE DEVIATION FROM THE ORIGINAL — requested by the user, do not "restore" this.
        //
        // The original (rockstar/ilIlil/IIiIIIiIi#I(Lnet/minecraft/class_243;)Lpyrock/utility/render/ColorRGBA;,
        // disassembled) samples ONE block position:
        //     world.getBiome(BlockPos.ofFloored(pos)).value().getFogColor()
        // A single-point lookup takes whichever biome the camera happens to be standing in, so the
        // fog colour snaps hard at every biome border and an outlier biome under the camera tints
        // the whole screen. Vanilla does not do this: BackgroundRenderer.getFogColor blends the
        // biome fog colour over a neighbourhood with CubicSampler, which is what we do here.
        // Everything after the sample — the 0.55 lightening in getSkyLightLevel and the opaque
        // alpha — is unchanged from the original.
        Vec3d sampled = CubicSampler.sampleColor(
            VanillaChestLootTableGenerator.subtract(2.0, 2.0, 2.0).multiply(0.25),
            (x, y, z) -> Vec3d.unpackRgb(Ambience.minecraftClient.world.getBiomeAccess()
                .getBiomeForNoiseGen(x, y, z).value().getFogColor()));
        int n = MathHelper.clamp((int)Math.round(sampled.x * 255.0), 0, 255);
        int n2 = MathHelper.clamp((int)Math.round(sampled.y * 255.0), 0, 255);
        int n3 = MathHelper.clamp((int)Math.round(sampled.z * 255.0), 0, 255);
        return new ColorRGBA(this.getSkyLightLevel(n), this.getSkyLightLevel(n2), this.getSkyLightLevel(n3), 255.0f);
    }

    private int getSkyLightLevel(int n) {
        return MathHelper.clamp((int)Math.round((float)n + (float)(255 - n) * 0.55f), (int)0, (int)255);
    }

    @Override
    public void onTick() {
        if (Ambience.minecraftClient.world == null) {
            this.worldTimeUpdated = false;
            this.resetReflectionState();
            this.resetShaderState();
            return;
        }
        if (this.customTime.isEnabled()) {
            Ambience.minecraftClient.world.getLevelProperties().setTimeOfDay((long)this.time.getValue());
        }
        if (this.effect.isSelected() && this.isAmbientLightingReady()) {
            this.resetRainState();
        } else {
            this.resetShaderState();
        }
        super.onTick();
    }

    @Override
    public void onEnable() {
        if (!EntityUtils.isClientWorldReady() || Ambience.minecraftClient.world == null) {
            return;
        }
        this.worldTime = Ambience.minecraftClient.world.getTime();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.rainState.finishRainRender();
        this.resetShaderState();
        this.resetReflectionState();
        this.worldTimeUpdated = false;
        if (!EntityUtils.isClientWorldReady() || Ambience.minecraftClient.world == null) {
            return;
        }
        Ambience.minecraftClient.world.getLevelProperties().setTimeOfDay(this.worldTime);
        super.onDisable();
    }

    private boolean isAmbientWorldReady() {
        return this.isEnabled() && this.isAmbientLightingReady() && this.dynamic.isSelected() && (!this.onlyInCave.isEnabled() || this.isAmbientTimeReady());
    }

    private void updateTimeState(boolean bl) {
        float f = this.ambienceAnimation.update(bl ? 1.0f : 0.0f);
        int n = MathHelper.clamp((int)Math.round(this.light.getValue() * f), (int)0, (int)15);
        if (n <= 0) {
            this.resetAmbientState();
            return;
        }
        BlockPos adminsky = BlockPos.ofFloored((Position)Ambience.minecraftClient.gameRenderer.getCamera().getPos());
        float f2 = this.radius.getValue();
        if (this.overlayVisible && adminsky.equals(this.lastFogBlock) && Float.compare(f2, this.lastFogHeight) == 0 && n == this.keyCode) {
            return;
        }
        BlockPos adminsky2 = this.lastFogBlock;
        int n2 = MathHelper.ceil((float)this.lastFogHeight);
        DynamicLightGrid.update(Ambience.minecraftClient.world, adminsky, f2, n);
        this.resetFogColumn(adminsky2, n2, adminsky, MathHelper.ceil((float)f2));
        this.lastFogBlock = adminsky;
        this.lastFogHeight = f2;
        this.keyCode = n;
        this.overlayVisible = true;
    }

    private boolean isAmbientTimeReady() {
        if (Ambience.minecraftClient.player == null || Ambience.minecraftClient.world == null) {
            this.worldTimeUpdated = false;
            return false;
        }
        BlockPos adminsky = BlockPos.ofFloored((Position)Ambience.minecraftClient.player.getEyePos());
        int n = Ambience.minecraftClient.world.getLightLevel(LightType.SKY, adminsky);
        if (Ambience.minecraftClient.world.isSkyVisible(adminsky) || n >= 8) {
            this.worldTimeUpdated = false;
        } else if (n <= 4) {
            this.worldTimeUpdated = true;
        }
        return this.worldTimeUpdated;
    }

    private void resetAmbientState() {
        if (!this.overlayVisible) {
            return;
        }
        DynamicLightGrid.clear();
        this.resetFogColumn(this.lastFogBlock, MathHelper.ceil((float)this.lastFogHeight), null, 0);
        this.resetFogState();
    }

    private void resetReflectionState() {
        this.ambienceAnimation.reset();
        if (!this.overlayVisible) {
            DynamicLightGrid.clear();
            return;
        }
        DynamicLightGrid.clear();
        this.resetFogColumn(this.lastFogBlock, MathHelper.ceil((float)this.lastFogHeight), null, 0);
        this.resetFogState();
    }

    private void resetFogState() {
        this.lastFogBlock = null;
        this.lastFogHeight = -1.0f;
        this.keyCode = -1;
        this.overlayVisible = false;
    }

    private void resetFogColumn(BlockPos adminsky, int n, BlockPos adminsky2, int n2) {
        if (Ambience.minecraftClient.worldRenderer == null) {
            return;
        }
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        this.resetFogColumns(hashSet, adminsky, n);
        this.resetFogColumns(hashSet, adminsky2, n2);
        for (BlockPos adminsky3 : hashSet) {
            Ambience.minecraftClient.worldRenderer.scheduleChunkRender(adminsky3.getX(), adminsky3.getY(), adminsky3.getZ());
        }
    }

    private void resetFogColumns(Set<BlockPos> set, BlockPos adminsky, int n) {
        if (adminsky == null || n <= 0) {
            return;
        }
        int n2 = n + 1;
        int n3 = ChunkSectionPos.getSectionCoord((int)(adminsky.getX() - n2));
        int n4 = ChunkSectionPos.getSectionCoord((int)(adminsky.getY() - n2));
        int n5 = ChunkSectionPos.getSectionCoord((int)(adminsky.getZ() - n2));
        int n6 = ChunkSectionPos.getSectionCoord((int)(adminsky.getX() + n2));
        int n7 = ChunkSectionPos.getSectionCoord((int)(adminsky.getY() + n2));
        int n8 = ChunkSectionPos.getSectionCoord((int)(adminsky.getZ() + n2));
        for (int i = n3; i <= n6; ++i) {
            for (int j = n4; j <= n7; ++j) {
                for (int k = n5; k <= n8; ++k) {
                    int n9;
                    int n10;
                    int n11 = MathHelper.clamp((int)adminsky.getX(), (int)(i << 4), (int)((i << 4) + 15));
                    if (!(adminsky.getSquaredDistance(n11, n10 = MathHelper.clamp((int)adminsky.getY(), (int)(j << 4), (int)((j << 4) + 15)), n9 = MathHelper.clamp((int)adminsky.getZ(), (int)(k << 4), (int)((k << 4) + 15))) <= (double)(n2 * n2))) continue;
                    set.add(new BlockPos(i, j, k));
                }
            }
        }
    }

    private void resetRainState() {
        if (Ambience.minecraftClient.player == null) {
            return;
        }
        StatusEffectInstance class_12932 = Ambience.minecraftClient.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (class_12932 == null) {
            Ambience.minecraftClient.player.addStatusEffect(this.getBiomeEffect());
            return;
        }
        if (this.isEffectSelected(class_12932) && class_12932.getDuration() <= 220) {
            Ambience.minecraftClient.player.addStatusEffect(this.getBiomeEffect());
        }
    }

    private void resetShaderState() {
        if (Ambience.minecraftClient.player == null) {
            return;
        }
        StatusEffectInstance class_12932 = Ambience.minecraftClient.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (this.isEffectSelected(class_12932)) {
            Ambience.minecraftClient.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    private StatusEffectInstance getBiomeEffect() {
        return EntityUtils.createStatusEffect((RegistryEntry<StatusEffect>)StatusEffects.NIGHT_VISION, 400, 0);
    }

    private boolean isEffectSelected(StatusEffectInstance class_12932) {
        return class_12932 != null && class_12932.getEffectType() == StatusEffects.NIGHT_VISION && class_12932.getAmplifier() == 0 && EntityUtils.isEffectActive(class_12932);
    }

    public boolean isShaderReady() {
        return !this.skybox.isSelected(this.defaultSkybox) && AssetImageLoader.isAssetLoaded(this.formatWorldTime());
    }

    public boolean isRainReady() {
        return this.shader.isEnabled();
    }

    public boolean isFogReady() {
        return this.isEnabled() && (this.isShaderReady() || this.isRainReady());
    }

    public TimedAccentShader getShaderResource() {
        if (this.shaderType.isSelected(this.caustic)) {
            return ShaderRenderer.skyCausticShader;
        }
        return ShaderRenderer.nebulaSkyShader;
    }

    public SkyboxShaderPair getShaderRenderer() {
        if (this.shaderType.isSelected(this.galaxy)) {
            return ShaderRenderer.galaxySkyRegistry;
        }
        if (this.shaderType.isSelected(this.space)) {
            return ShaderRenderer.spaceSkyRegistry;
        }
        return null;
    }

    public boolean isWetWorldReady() {
        return this.isEnabled() && this.isColorIsolationReady();
    }

    public boolean isNightModeReady() {
        return this.isEnabled() && this.wetWorld.isEnabled();
    }

    public WetWorldShader.Uniforms buildReflectionState(Matrix4f matrix4f, Matrix4f matrix4f2, Camera class_41842, float f) {
        this.reflectionState.viewProjectionMatrix.set((Matrix4fc)matrix4f2).mul((Matrix4fc)matrix4f);
        this.reflectionState.inverseViewProjectionMatrix.set((Matrix4fc)this.reflectionState.viewProjectionMatrix).invert();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        this.reflectionState.cameraX = (float)VanillaChestLootTableGenerator.x;
        this.reflectionState.cameraY = (float)VanillaChestLootTableGenerator.y;
        this.reflectionState.cameraZ = (float)VanillaChestLootTableGenerator.z;
        int n = Ambience.minecraftClient.world.getSkyColor(VanillaChestLootTableGenerator, f);
        this.reflectionState.skyTintRed = (float)(n >> 16 & 0xFF) / 255.0f;
        this.reflectionState.skyTintGreen = (float)(n >> 8 & 0xFF) / 255.0f;
        this.reflectionState.skyTintBlue = (float)(n & 0xFF) / 255.0f;
        boolean hasSkyLight = Ambience.minecraftClient.world.getDimension().hasSkyLight();
        float f2 = Ambience.minecraftClient.world.getSkyAngleRadians(f);
        this.reflectionState.sunDirectionX = -MathHelper.sin((float)f2);
        this.reflectionState.sunDirectionY = MathHelper.cos((float)f2);
        this.reflectionState.sunDirectionZ = 0.0f;
        this.reflectionState.reflectivity = this.reflection.getValue() / 100.0f;
        this.reflectionState.wetness = this.reflectionAmount.getValue() / 100.0f;
        this.reflectionState.rippleStrength = 0.35f;
        this.reflectionState.gloss = hasSkyLight ? 0.45f : 0.0f;
        this.reflectionState.maxDistance = 24.0f;
        this.reflectionState.hitThickness = 0.98f;
        this.reflectionState.upOnly = 1.0f;
        this.reflectionState.time = (float)(System.currentTimeMillis() % 3600000L) / 1000.0f;
        this.reflectionState.stepCount = this.wetWorldLowQuality.isSelected() ? 14.0f : (this.wetWorldHighQuality.isSelected() ? 40.0f : 24.0f);
        return this.reflectionState;
    }

    public boolean isColorIsolationReady() {
        return this.colorIsolation.isEnabled();
    }

    public float calculateColorIsolationFactor() {
        return this.colorIsolation.isEnabled() ? 1.0f : 0.0f;
    }

    public Vector3f getFogCameraVector() {
        ColorRGBA colorRGBA = this.isolationColor.getColor();
        return new Vector3f(colorRGBA.getRed() / 255.0f, colorRGBA.getGreen() / 255.0f, colorRGBA.getBlue() / 255.0f);
    }

    private float calculateRainIntensity() {
        if (this.isolationCustom.isSelected()) {
            return this.sensitivity.getValue() / 100.0f;
        }
        if (this.strict.isSelected()) {
            return 0.15f;
        }
        if (this.loose.isSelected()) {
            return 0.85f;
        }
        return 0.5f;
    }

    public float calculateFogIntensity() {
        return (8.0f + this.calculateRainIntensity() * 47.0f) / 360.0f;
    }

    public float calculateShaderOpacity() {
        return 0.55f - this.calculateRainIntensity() * 0.43f;
    }

    public float calculateReflectionStrength() {
        return 0.45f - this.calculateRainIntensity() * 0.37f;
    }

    public float calculateColorIsolationBackground() {
        return this.background.getValue() / 100.0f;
    }

    public Identifier getSkyboxTexture() {
        return AssetImageLoader.getTexture(this.formatWorldTime());
    }

    private String formatWorldTime() {
        return "sky/" + Math.max(this.skybox.getOptions().indexOf(this.skybox.getSelectedOption()), 1);
    }

    public float calculateNightBrightness() {
        return this.shaderOpacity.getValue() / 100.0f;
    }

    @Generated
    public MultiBooleanSetting getChangeColorSetting() {
        return this.changeColor;
    }

    @Generated
    public MultiBooleanSetting.Option getSkyChangeOption() {
        return this.sky;
    }

    @Generated
    public MultiBooleanSetting.Option getCloudsChangeOption() {
        return this.clouds;
    }

    @Generated
    public MultiBooleanSetting.Option getStarsChangeOption() {
        return this.stars;
    }

    @Generated
    public BooleanSetting getThemeSyncSetting() {
        return this.syncSetting;
    }

    @Generated
    public ColorSetting getSkyColorSetting() {
        return this.skyColor;
    }

    @Generated
    public ColorSetting getCloudColorSetting() {
        return this.cloudColor;
    }

    @Generated
    public ColorSetting getStarsColorSetting() {
        return this.starsColor;
    }

    @Generated
    public ModeSetting getSkyboxSetting() {
        return this.skybox;
    }

    @Generated
    public ModeSetting.Option getDefaultSkyboxOption() {
        return this.defaultSkybox;
    }

    @Generated
    public ModeSetting.Option getBrightCloudsOption() {
        return this.brightClouds;
    }

    @Generated
    public ModeSetting.Option getLakeSkyboxOption() {
        return this.lake;
    }

    @Generated
    public ModeSetting.Option getCloudSpaceSkyboxOption() {
        return this.cloudSpace;
    }

    @Generated
    public ModeSetting.Option getClearEveningSkyboxOption() {
        return this.clearEvening;
    }

    @Generated
    public ModeSetting.Option getUnderwaterSkyboxOption() {
        return this.underwater;
    }

    @Generated
    public BooleanSetting getShaderSetting() {
        return this.shader;
    }

    @Generated
    public ModeSetting getShaderTypeSetting() {
        return this.shaderType;
    }

    @Generated
    public ModeSetting.Option getNebulaOption() {
        return this.nebula;
    }

    @Generated
    public ModeSetting.Option getCausticOption() {
        return this.caustic;
    }

    @Generated
    public ModeSetting.Option getGalaxyOption() {
        return this.galaxy;
    }

    @Generated
    public ModeSetting.Option getSpaceOption() {
        return this.space;
    }

    @Generated
    public NumberSetting getShaderOpacitySetting() {
        return this.shaderOpacity;
    }

    @Generated
    public BooleanSetting getRainSetting() {
        return this.rain;
    }

    @Generated
    public NumberSetting getRainDensitySetting() {
        return this.density;
    }

    @Generated
    public NumberSetting getRainOpacitySetting() {
        return this.opacity;
    }

    @Generated
    public NumberSetting getRainDropsSetting() {
        return this.drops;
    }

    @Generated
    public NumberSetting getRainSplashesSetting() {
        return this.splashes;
    }

    @Generated
    public BooleanSetting getFogSetting() {
        return this.fog;
    }

    @Generated
    public ModeSetting getFogColorModeSetting() {
        return this.colorMode;
    }

    @Generated
    public ModeSetting.Option getBiomeFogOption() {
        return this.biome;
    }

    @Generated
    public ModeSetting.Option getThemeFogOption() {
        return this.theme;
    }

    @Generated
    public ModeSetting.Option getCustomFogOption() {
        return this.custom;
    }

    @Generated
    public ColorSetting getFogColorSetting() {
        return this.color;
    }

    @Generated
    public NumberSetting getFogDensitySetting() {
        return this.fogDensity;
    }

    @Generated
    public NumberSetting getFogCoverageSetting() {
        return this.coverage;
    }

    @Generated
    public NumberSetting getFogLevelSetting() {
        return this.level;
    }

    @Generated
    public NumberSetting getFogThicknessSetting() {
        return this.thickness;
    }

    @Generated
    public ModeSetting getFogQualitySetting() {
        return this.quality;
    }

    @Generated
    public ModeSetting.Option getLowFogQualityOption() {
        return this.low;
    }

    @Generated
    public ModeSetting.Option getHighFogQualityOption() {
        return this.high;
    }

    @Generated
    public BooleanSetting getWetWorldSetting() {
        return this.wetWorld;
    }

    @Generated
    public NumberSetting getReflectionStrengthSetting() {
        return this.reflection;
    }

    @Generated
    public NumberSetting getReflectionAmountSetting() {
        return this.reflectionAmount;
    }

    @Generated
    public ModeSetting getReflectionQualitySetting() {
        return this.wetWorldQuality;
    }

    @Generated
    public ModeSetting.Option getLowReflectionQualityOption() {
        return this.wetWorldLowQuality;
    }

    @Generated
    public ModeSetting.Option getMediumReflectionQualityOption() {
        return this.medium;
    }

    @Generated
    public ModeSetting.Option getHighReflectionQualityOption() {
        return this.wetWorldHighQuality;
    }

    @Generated
    public BooleanSetting getColorIsolationSetting() {
        return this.colorIsolation;
    }

    @Generated
    public ColorSetting getIsolationColorSetting() {
        return this.isolationColor;
    }

    @Generated
    public ModeSetting getIsolationModeSetting() {
        return this.mode;
    }

    @Generated
    public ModeSetting.Option getStrictIsolationOption() {
        return this.strict;
    }

    @Generated
    public ModeSetting.Option getBalancedIsolationOption() {
        return this.balanced;
    }

    @Generated
    public ModeSetting.Option getLooseIsolationOption() {
        return this.loose;
    }

    @Generated
    public ModeSetting.Option getCustomIsolationOption() {
        return this.isolationCustom;
    }

    @Generated
    public BooleanSetting getCustomTimeSetting() {
        return this.customTime;
    }

    @Generated
    public NumberSetting getWorldTimeSetting() {
        return this.time;
    }

    @Generated
    public BooleanSetting getNightModeSetting() {
        return this.nightMode;
    }

    @Generated
    public BooleanSetting getBrightSetting() {
        return this.bright;
    }

    @Generated
    public ModeSetting getTimeModeSetting() {
        return this.lightingMode;
    }

    @Generated
    public ModeSetting.Option getGammaTimeOption() {
        return this.gamma;
    }

    @Generated
    public ModeSetting.Option getEffectTimeOption() {
        return this.effect;
    }

    @Generated
    public ModeSetting.Option getDynamicTimeOption() {
        return this.dynamic;
    }

    @Generated
    public NumberSetting getDynamicRadiusSetting() {
        return this.radius;
    }

    @Generated
    public NumberSetting getDynamicLightSetting() {
        return this.light;
    }

    @Generated
    public BooleanSetting getOnlyInCaveSetting() {
        return this.onlyInCave;
    }

    @Generated
    public BooleanSetting getNightSyncSetting() {
        return this.sync;
    }

    @Generated
    public ColorSetting getNightColorSetting() {
        return this.nightModeColor;
    }

    @Generated
    public WetWorldShader.Uniforms getReflectionState() {
        return this.reflectionState;
    }

    @Generated
    public WorldOverlayRenderer getRainState() {
        return this.rainState;
    }

    public VolumetricFogRenderer getFogRenderer() {
        return this.fogRenderer;
    }

    @Generated
    public long getWorldTime() {
        return this.worldTime;
    }

    @Generated
    public BlockPos getLastFogBlock() {
        return this.lastFogBlock;
    }

    @Generated
    public float getLastFogHeight() {
        return this.lastFogHeight;
    }

    @Generated
    public int getKeyCode() {
        return this.keyCode;
    }

    @Generated
    public boolean isOverlayVisible() {
        return this.overlayVisible;
    }

    @Generated
    public Animation getAmbienceAnimation() {
        return this.ambienceAnimation;
    }

    @Generated
    public EventListener<ReceivePacketEvent> getPacketListener() {
        return this.onReceivePacketEvent;
    }

    @Generated
    public EventListener<Render3DEvent> getRenderListener() {
        return this.onRender3DEvent;
    }














}

