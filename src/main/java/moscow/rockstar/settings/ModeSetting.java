/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonPrimitive
 *  lombok.Generated
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.texture.TextureAnimationAtlas;
import moscow.rockstar.render.texture.TextureRegion;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import moscow.rockstar.ui.text.TextComponent;
import org.jetbrains.annotations.NotNull;
import pyrock.utility.render.ColorRGBA;

public class ModeSetting
extends AbstractSetting {
    final List<Option> options = new ArrayList<Option>();
    private Option selectedOption;

    public ModeSetting(@NotNull SettingOwner settingOwner, String string, String string2, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public ModeSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public ModeSetting(@NotNull SettingOwner settingOwner, String string, String string2) {
        super(settingOwner, string);
    }

    public ModeSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public void addOption(Option option) {
        this.options.add(option);
        if (this.selectedOption == null) {
            this.selectedOption = option;
        }
    }

    public void setOptions(String ... stringArray) {
        String string = this.selectedOption == null ? null : this.selectedOption.getName();
        this.options.clear();
        this.selectedOption = null;
        if (stringArray == null) {
            return;
        }
        for (String string2 : stringArray) {
            new Option(this, string2);
        }
        if (string != null) {
            for (Option option : this.options) {
                if (!option.getName().equalsIgnoreCase(string)) continue;
                this.selectedOption = option;
                return;
            }
        }
        if (!this.options.isEmpty()) {
            this.selectedOption = this.options.getFirst();
        }
    }

    public boolean isSelectedByName(String string) {
        return this.selectedOption != null && this.selectedOption.getName().equalsIgnoreCase(string);
    }

    public boolean isSelected(Option option) {
        return this.selectedOption == option;
    }

    public void select(Option option) {
        if (this.selectedOption == option) {
            return;
        }
        this.notifyChange();
        this.selectedOption = option;
    }

    public int getSelectedIndex() {
        return this.options.indexOf(this.selectedOption);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(this.selectedOption == null ? "" : this.selectedOption.getName());
    }

    public Option getRandomSelectedOption() {
        List<Option> list = this.options.stream().filter(Option::isSelected).toList();
        if (!list.isEmpty()) {
            Random random = new Random();
            return list.get(random.nextInt(list.size()));
        }
        return null;
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isString()) {
            return;
        }
        String string = jsonElement.getAsString();
        for (Option option : this.options) {
            if (!option.getName().equalsIgnoreCase(string)) continue;
            this.selectedOption = option;
            break;
        }
    }

    @Override
    public boolean isValidJson(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isString()) {
            return false;
        }
        String string = jsonElement.getAsString();
        return this.options.stream().anyMatch(option -> option.getName().equalsIgnoreCase(string));
    }

    @Override
    public Component buildComponent() {
        Component component = new Component().add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> Localization.translate(this.key)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).fadeOut().fill()).gap(6.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER).padding(Insets.of(6.0f, 0.0f, 0.0f, 0.0f)).fillWidth();
        Component component2 = new Component(){
            private final IdentityHashMap<Option, UiNode> optionAnimations = new IdentityHashMap();
            private List<Option> options;
            {
                for (Option option : ModeSetting.this.options) {
                    TextComponent textComponent = option.buildComponent();
                    this.optionAnimations.put(option, textComponent);
                    this.add(textComponent);
                }
                this.options = new ArrayList<Option>(ModeSetting.this.options);
            }

            @Override
            protected void onTick(float f, float f2, float f3) {
                if (!ModeSetting.sameIdentity(this.options, ModeSetting.this.options)) {
                    ArrayList<UiNode> arrayList = new ArrayList<UiNode>(ModeSetting.this.options.size());
                    for (Option option2 : ModeSetting.this.options) {
                        arrayList.add(this.optionAnimations.computeIfAbsent(option2, Option::buildComponent));
                    }
                    this.optionAnimations.keySet().removeIf(option -> !ModeSetting.this.options.contains(option));
                    this.updateChildren(arrayList);
                    this.options = new ArrayList<Option>(ModeSetting.this.options);
                }
                super.onTick(f, f2, f3);
            }
        }.layout(Layout.ROW).gap(2.0f).fillWidth().padding(Insets.of(0.0f, 0.0f, 5.0f, 0.0f)).wrapContent();
        return new Component().layout(Layout.COLUMN).gap(5.0f).add(component).add(component2);
    }

    static boolean sameIdentity(List<?> list, List<?> list2) {
        if (list.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i) == list2.get(i)) continue;
            return false;
        }
        return true;
    }

    @Generated
    public List<Option> getOptions() {
        return this.options;
    }

    @Generated
    public Option getSelectedOption() {
        return this.selectedOption;
    }

    public static class Option {
        private final ModeSetting parent;
        private final String name;
        private final String description;
        private final Animation hoverAnimation = new Animation(300L, Easing.easeInOutCubicBezier);
        private final Animation activeAnimation = new Animation(300L, Easing.easeInOutCubicBezier);
        private final BooleanSupplier hideCondition;
        private TextureAnimationAtlas.Animation enableAnimation;
        private TextureAnimationAtlas.Animation disableAnimation;
        private TextureAnimationAtlas.Animation currentAnimation;
        private long currentAnimationStartedAtMillis;
        private boolean lastSelected;

        public Option(ModeSetting modeSetting, String string) {
            this(modeSetting, string, "", () -> false);
        }

        public Option(ModeSetting modeSetting, String string, String string2) {
            this(modeSetting, string, string2, () -> false);
        }

        public Option(ModeSetting modeSetting, String string, String string2, BooleanSupplier booleanSupplier) {
            this.parent = modeSetting;
            this.name = string;
            this.description = string2;
            this.hideCondition = booleanSupplier;
            modeSetting.addOption(this);
        }

        public boolean isHidden() {
            return this.hideCondition != null && this.hideCondition.getAsBoolean();
        }

        public Option select() {
            this.parent.select(this);
            return this;
        }

        public boolean isSelected() {
            return this.parent.getSelectedOption() == this;
        }

        public TextComponent buildComponent() {
            return new TextComponent().bind("selected", this::isSelected, Motion.resolveMotionMotionFromLongAndEasing(220L, Easing.easeInOutCubicBezier)).text(Font.MEDIUM.metrics(7.0f), () -> Localization.translate(this.name), textComponent -> ColorPalette.blendWithContrastBackground(Option.background(textComponent)).mulAlpha(0.75f + 0.25f * textComponent.sig("selected"))).textAlign(Alignment.CENTER).background(Option::background).radius(2.5f).padding(Insets.uniform(3.0f)).cursor(Cursor.HAND).onClick(this::select);
        }

        private static ColorRGBA background(TextComponent textComponent) {
            return ColorPalette.MUTED_PANEL_COLOR.mix(ColorPalette.ACCENT_COLOR, 0.2f * textComponent.hover()).mix(ColorPalette.ACCENT_COLOR.mix(ColorPalette.MUTED_PANEL_COLOR, 0.2f * textComponent.hover()), textComponent.sig("selected"));
        }

        public String toString() {
            return this.name;
        }

        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            if (object == null || object.getClass() != this.getClass()) {
                return false;
            }
            Option option = (Option)object;
            return Objects.equals(this.parent, option.parent) && Objects.equals(this.name, option.name) && Objects.equals(this.description, option.description);
        }

        public int hashCode() {
            return Objects.hash(this.parent, this.name, this.description);
        }

        @Generated
        public void setEnableAnimation(TextureAnimationAtlas.Animation animation) {
            this.enableAnimation = animation;
        }

        @Generated
        public void setDisableAnimation(TextureAnimationAtlas.Animation animation) {
            this.disableAnimation = animation;
        }

        @Generated
        public void setCurrentAnimation(TextureAnimationAtlas.Animation animation) {
            if (this.currentAnimation != animation) {
                this.currentAnimation = animation;
                this.currentAnimationStartedAtMillis = System.currentTimeMillis();
            }
        }

        @Generated
        public void setLastSelected(boolean bl) {
            this.lastSelected = bl;
        }

        @Generated
        public ModeSetting getParent() {
            return this.parent;
        }

        @Generated
        public String getName() {
            return this.name;
        }

        @Generated
        public String getDescription() {
            return this.description;
        }

        @Generated
        public Animation getHoverAnimation() {
            return this.hoverAnimation;
        }

        @Generated
        public Animation getActiveAnimation() {
            return this.activeAnimation;
        }

        @Generated
        public BooleanSupplier getHideCondition() {
            return this.hideCondition;
        }

        @Generated
        public TextureAnimationAtlas.Animation getEnableAnimation() {
            return this.enableAnimation;
        }

        @Generated
        public TextureAnimationAtlas.Animation getDisableAnimation() {
            return this.disableAnimation;
        }

        @Generated
        public TextureAnimationAtlas.Animation getCurrentAnimation() {
            return this.currentAnimation;
        }

        public TextureRegion getCurrentAnimationFrame() {
            if (this.currentAnimation == null) {
                return null;
            }
            long elapsedMillis = Math.max(0L, System.currentTimeMillis() - this.currentAnimationStartedAtMillis);
            return this.currentAnimation.getFrameAt(elapsedMillis);
        }

        public boolean isAnimationPlaying() {
            if (this.currentAnimation == null || this.currentAnimation.manifest == null
                    || this.currentAnimation.regions == null || this.currentAnimation.regions.isEmpty()) {
                return false;
            }
            if (this.currentAnimation.manifest.isLooping()) {
                return true;
            }
            long frameDurationMillis = Math.max(1L, this.currentAnimation.manifest.getFrameDurationMillis());
            long elapsedMillis = Math.max(0L, System.currentTimeMillis() - this.currentAnimationStartedAtMillis);
            return elapsedMillis / frameDurationMillis < (long)this.currentAnimation.regions.size();
        }

        /** Park the current animation on its last frame (original: seek(0) + stop on the disable sprite). */
        public void parkCurrentAnimation() {
            this.currentAnimationStartedAtMillis = 0L;
        }

        @Generated
        public boolean wasSelected() {
            return this.lastSelected;
        }
    }
}
