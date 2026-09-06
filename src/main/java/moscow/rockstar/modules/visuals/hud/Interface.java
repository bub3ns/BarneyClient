/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  lombok.Generated
 */
package moscow.rockstar.modules.visuals.hud;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Interface", category=ModuleCategory.VISUALS, disableLocked=true, alwaysEnabled=true)
public class Interface
extends Module {
    private final Map<HudElement, MultiBooleanSetting.Option> elementOptionsByRenderer = new IdentityHashMap<HudElement, MultiBooleanSetting.Option>();
    private MultiBooleanSetting elementVisibilitySetting;

    public Interface() {
        this.initializeElementSettings();
    }

    @Compile(obfuscation=4)
    private void initializeElementSettings() {
        this.elementVisibilitySetting = new MultiBooleanSetting(this, "modules.settings.interface.elements"){

            @Override
            public JsonElement serialize() {
                return new JsonObject();
            }

            @Override
            public void deserialize(JsonElement jsonElement) {
            }

            @Override
            public boolean isValidJson(JsonElement jsonElement) {
                return jsonElement != null && jsonElement.isJsonObject();
            }

            @Override
            public Component buildComponent() {
                Interface.this.synchronizeElementOptions();
                Component component = super.buildComponent().fillWidth();
                return new Component(){

                    @Override
                    protected void onTick(float f, float f2, float f3) {
                        Interface.this.synchronizeElementOptions();
                        super.onTick(f, f2, f3);
                    }
                }.layout(Layout.COLUMN).add(component);
            }
        };
        this.synchronizeElementOptions();
    }

    void synchronizeElementOptions() {
        List<MultiBooleanSetting.Option> list;
        if (RockstarClient.create().getHudElementRegistry() == null || this.elementVisibilitySetting == null) {
            return;
        }
        List<HudElement> list2 = RockstarClient.create().getHudElementRegistry().elements();
        List<MultiBooleanSetting.Option> list3 = this.elementVisibilitySetting.getOptions();
        if (!this.areElementOptionsSynchronized(list3, list2)) {
            list = new ArrayList<MultiBooleanSetting.Option>(list2.size());
            for (HudElement object : list2) {
                list.add(this.elementOptionsByRenderer.computeIfAbsent(object, hudElement -> new ElementOption(this.elementVisibilitySetting, hudElement)));
            }
            this.elementOptionsByRenderer.keySet().removeIf(ambientParticleRenderer -> !list2.contains(ambientParticleRenderer));
            list3.clear();
            list3.addAll(list);
        }
        list = this.elementVisibilitySetting.getSelectedOptions();
        list.removeIf(option -> !list3.contains(option));
        for (MultiBooleanSetting.Option option2 : list3) {
            boolean bl = option2.isSelected();
            if (bl == list.contains(option2)) continue;
            if (bl) {
                list.add(option2);
                continue;
            }
            list.remove(option2);
        }
    }

    private boolean areElementOptionsSynchronized(List<MultiBooleanSetting.Option> list, List<HudElement> list2) {
        if (list.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list2.size(); ++i) {
            if (this.elementOptionsByRenderer.get(list2.get(i)) == list.get(i)) continue;
            return false;
        }
        return true;
    }

    public static boolean isLiquidGlassEnvironmentReady() {
        return false;
    }

    public static float getLiquidGlassAlpha() {
        return 0.0f;
    }

    public static float getBlurAlpha() {
        return 1.0f;
    }

    public static float getDistortionStrength() {
        return 0.79f;
    }

    public static float getDistortionRadius() {
        return 12.0f;
    }

    public static float getAberrationStrength() {
        return 2.0f;
    }

    public static float getSaturation() {
        return 1.0f;
    }

    public static boolean isLiquidGlassEnabled() {
        return false;
    }

    public static boolean isBlurEnabled() {
        return true;
    }

    @Generated
    public Map<HudElement, MultiBooleanSetting.Option> getElementOptionsByRenderer() {
        return this.elementOptionsByRenderer;
    }

    @Generated
    public MultiBooleanSetting getElementVisibilitySetting() {
        return this.elementVisibilitySetting;
    }

    static final class ElementOption
    extends MultiBooleanSetting.Option {
        private final HudElement renderedElement;

        ElementOption(MultiBooleanSetting multiBooleanSetting, HudElement ambientParticleRenderer) {
            super(multiBooleanSetting, ambientParticleRenderer.getName());
            this.renderedElement = ambientParticleRenderer;
        }

        @Override
        public boolean isSelected() {
            return this.renderedElement.isShowing();
        }

        @Override
        public MultiBooleanSetting.Option toggle() {
            boolean bl;
            boolean bl2 = bl = !this.renderedElement.isShowing();
            if (bl && this.renderedElement.getX() == 0.0f && this.renderedElement.getY() == 0.0f) {
                this.renderedElement.pos(WindowMetricsProvider.INSTANCE.width() / 2.0f, WindowMetricsProvider.INSTANCE.height() / 2.0f);
            }
            this.renderedElement.setShowing(bl);
            if (bl) {
                this.select();
            } else {
                this.deselect();
            }
            moscow.rockstar.modules.config.ModuleConfigurationStore.saveConfiguration();
            return this;
        }
    }
}
