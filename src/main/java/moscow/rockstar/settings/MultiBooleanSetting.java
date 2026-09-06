/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonPrimitive
 *  lombok.Generated
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.network.http.client.ReactorNettyClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.texture.TextureAnimationAtlas;
import moscow.rockstar.render.texture.TextureRegion;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.RollingNumberComponent;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.util.Timer;
import org.jetbrains.annotations.NotNull;
import pyrock.utility.render.ColorRGBA;

public class MultiBooleanSetting
extends AbstractSetting {
    final List<Option> options = new ArrayList<Option>();
    private List<Option> selectedOptions = new ArrayList<Option>();
    private boolean preserveOrder;
    private int maxSelections;

    public MultiBooleanSetting(@NotNull SettingOwner settingOwner, String string, String string2, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public MultiBooleanSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public MultiBooleanSetting(@NotNull SettingOwner settingOwner, String string, String string2) {
        super(settingOwner, string);
    }

    public MultiBooleanSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public MultiBooleanSetting preserveOrder() {
        this.preserveOrder = true;
        return this;
    }

    public MultiBooleanSetting setMaxSelections(int n) {
        this.maxSelections = Math.max(0, n);
        return this;
    }

    public void addOption(Option option) {
        this.options.add(option);
    }

    public void selectOption(Option option) {
        this.setSelected(option, true);
    }

    void setSelected(Option option, boolean bl) {
        if (option == null || this.selectedOptions.contains(option) == bl) {
            return;
        }
        this.notifyChange();
        if (bl) {
            this.selectedOptions.add(option);
        } else {
            this.selectedOptions.remove(option);
        }
    }

    void enforceSelectionLimit(Option option) {
        if (option == null || option.alwaysEnabled) {
            return;
        }
        if (this.selectedOptions.contains(option)) {
            if (this.selectedOptions.size() > this.maxSelections) {
                this.setSelected(option, false);
            }
        } else {
            this.setSelected(option, true);
        }
    }

    @Override
    public JsonElement serialize() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (Option object : this.selectedOptions) {
            jsonArray.add((JsonElement)new JsonPrimitive(object.getName()));
        }
        jsonObject.add("selected", (JsonElement)jsonArray);
        if (this.preserveOrder) {
            JsonArray jsonArray2 = new JsonArray();
            for (Option option : this.options) {
                jsonArray2.add((JsonElement)new JsonPrimitive(option.getName()));
            }
            jsonObject.add("order", (JsonElement)jsonArray2);
        }
        return jsonObject;
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
        if (jsonElement == null) {
            return;
        }
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (this.preserveOrder && jsonObject.has("order") && !this.validateSelectionArray(jsonObject.get("order"))) {
                return;
            }
            if (jsonObject.has("selected") && !this.validateSelectionArray(jsonObject.get("selected"))) {
                return;
            }
        } else if (!this.validateSelectionArray(jsonElement)) {
            return;
        }
        this.selectedOptions.clear();
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (this.preserveOrder && jsonObject.has("order")) {
                JsonArray order = jsonObject.getAsJsonArray("order");
                List<Option> reorderedOptions = new ArrayList<>();
                for (JsonElement entry : order) {
                    String name = entry.getAsString();
                    this.options.stream()
                        .filter(option -> option.getName().equalsIgnoreCase(name))
                        .findFirst()
                        .ifPresent(reorderedOptions::add);
                }
                for (Option option : this.options) {
                    if (!reorderedOptions.contains(option)) {
                        reorderedOptions.add(option);
                    }
                }
                this.options.clear();
                this.options.addAll(reorderedOptions);
            }
            if (jsonObject.has("selected")) {
                JsonArray selected = jsonObject.getAsJsonArray("selected");
                for (JsonElement entry : selected) {
                    String name = entry.getAsString();
                    this.options.stream()
                        .filter(option -> option.getName().equalsIgnoreCase(name))
                        .findFirst()
                        .ifPresent(this.selectedOptions::add);
                }
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray selected = jsonElement.getAsJsonArray();
            for (JsonElement entry : selected) {
                String name = entry.getAsString();
                this.options.stream()
                    .filter(option -> MultiBooleanSetting.matchesOptionName(name, option))
                    .findFirst()
                    .ifPresent(this.selectedOptions::add);
            }
        }
        for (Option option3 : this.options) {
            if (!option3.isAlwaysEnabled() || this.selectedOptions.contains(option3)) continue;
            this.selectedOptions.add(option3);
        }
        if (this.selectedOptions.size() < this.maxSelections) {
            this.options.stream().filter(option -> !this.selectedOptions.contains(option)).limit(this.maxSelections - this.selectedOptions.size()).forEach(this.selectedOptions::add);
        }
    }

    private boolean validateSelectionArray(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonArray()) {
            return false;
        }
        for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
            if (jsonElement2.isJsonPrimitive() && jsonElement2.getAsJsonPrimitive().isString()) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isValidJson(JsonElement jsonElement) {
        if (jsonElement == null) {
            return false;
        }
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (!jsonObject.has("selected") || !this.validateOrderArray(jsonObject.get("selected"))) {
                return false;
            }
            return !this.preserveOrder || jsonObject.has("order") && this.validateOrderArray(jsonObject.get("order"));
        }
        return this.validateOrderArray(jsonElement);
    }

    private boolean validateOrderArray(JsonElement jsonElement) {
        if (!this.validateSelectionArray(jsonElement)) {
            return false;
        }
        for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
            if (!this.options.stream().noneMatch(option -> option.getName().equalsIgnoreCase(jsonElement2.getAsString()))) continue;
            return false;
        }
        return true;
    }

    @Override
    public Component buildComponent() {
        Component selectedCount = new Component()
            .layout(Layout.ROW)
            .alignment(Alignment.CENTER)
            .add(new RollingNumberComponent(Font.REGULAR.metrics(7.0f),
                () -> Math.toIntExact(this.selectedOptions.stream().filter(option -> !option.isHidden()).count()))
                .setColorProvider(rollingNumberComponent -> ColorPalette.ACCENT_COLOR))
            .add(new TextComponent().text(Font.REGULAR.metrics(7.0f),
                () -> " " + Localization.translate("setting_of") + " " + this.options.stream().filter(option -> !option.isHidden()).count(),
                textComponent -> ColorPalette.ACCENT_COLOR));
        Component component = new Component()
            .add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> Localization.translate(this.key))
                .setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover()))
                .fadeOut()
                .fill())
            .add(selectedCount)
            .gap(6.0f)
            .layout(Layout.ROW)
            .overflowMode(JustifyContent.SPACE_BETWEEN)
            .alignment(Alignment.CENTER)
            .padding(Insets.of(6.0f, 0.0f, 0.0f, 0.0f))
            .fillWidth();
        UiNode uiNode = this.preserveOrder ? this.getSelectionComponent() : new Component(){
            private final IdentityHashMap<Option, UiNode> optionAnimations = new IdentityHashMap();
            private List<Option> options;
            {
                for (Option option : MultiBooleanSetting.this.options) {
                    TextComponent textComponent = option.buildComponent();
                    this.optionAnimations.put(option, textComponent);
                    this.add(textComponent);
                }
                this.options = new ArrayList<Option>(MultiBooleanSetting.this.options);
            }

            @Override
            protected void onTick(float f, float f2, float f3) {
                if (!MultiBooleanSetting.sameIdentity(this.options, MultiBooleanSetting.this.options)) {
                    ArrayList<UiNode> arrayList = new ArrayList<UiNode>(MultiBooleanSetting.this.options.size());
                    for (Option option2 : MultiBooleanSetting.this.options) {
                        arrayList.add(this.optionAnimations.computeIfAbsent(option2, Option::buildComponent));
                    }
                    this.optionAnimations.keySet().removeIf(option -> !MultiBooleanSetting.this.options.contains(option));
                    this.updateChildren(arrayList);
                    this.options = new ArrayList<Option>(MultiBooleanSetting.this.options);
                }
                super.onTick(f, f2, f3);
            }
        }.layout(Layout.ROW).gap(2.0f).fillWidth().padding(Insets.of(0.0f, 0.0f, 5.0f, 0.0f)).wrapContent();
        return new Component().layout(Layout.COLUMN).gap(5.0f).add(component).add(uiNode);
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

    int getMaxSelectionsInternal() {
        return (int)this.options.stream().filter(option -> !option.isHidden()).count();
    }

    private TextComponent getSelectionComponent() {
        final Timer timer = new Timer();
        TextComponent textComponent = new TextComponent(){
            private Option draggedOption;
            private float mouseX;
            private float mouseY;

            @Override
            public float desiredH() {
                return 8 + 12 * MultiBooleanSetting.this.getMaxSelectionsInternal();
            }

            @Override
            protected void onTick(float f, float f2, float f3) {
                this.mouseX = f2;
                this.mouseY = f3;
            }

            @Override
            protected void drawSelf(RockstarDrawContext drawContext, float f) {
                float f2 = this.x();
                float f3 = this.y();
                float f4 = this.w();
                drawContext.drawRoundedRect(f2, f3, f4, this.h(), WidgetState.uniform(6.0f), ColorPalette.getPanelColor().withAlpha(76.5f * f));
                float f5 = 0.0f;
                for (Option option : MultiBooleanSetting.this.options) {
                    if (option.isHidden()) continue;
                    float f6 = this.draggedOption == option ? Math.clamp(this.mouseY - 2.0f, f3 + 1.0f, f3 + 3.0f + (float)(12 * MultiBooleanSetting.this.getMaxSelectionsInternal())) : f3 + 7.0f + f5;
                    boolean bl = this.inFlow() && this.mouseX >= f2 - 1.0f && this.mouseX <= f2 + f4 + 1.0f && this.mouseY >= f6 - 4.0f && this.mouseY <= f6 + 8.0f;
                    option.getPositionAnimation().setEasing(Easing.easeOutBackSoft);
                    option.getPositionAnimation().update(f6 - f3);
                    option.setLayoutY(f6);
                    if (bl && this.draggedOption != option && !option.isAlwaysEnabled()) {
                        moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
                    }
                    if (bl && this.mouseX <= f2 + 17.0f || option == this.draggedOption) {
                        moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.VERTICAL_RESIZE);
                    }
                    option.getHoverAnimation().setReverse(bl);
                    option.getActiveAnimation().setEasing(Easing.easeOutBack);
                    option.getActiveAnimation().setReverse(option.isSelected());
                    float f7 = option.getActiveAnimation().getValue();
                    float f8 = f3 + option.getPositionAnimation().getValue();
                    drawContext.drawIcon("hud/drag", f2 + 7.0f, f8, 6.0f, ColorPalette.getPrimaryTextColor().mulAlpha(f));
                    drawContext.drawFadeoutText(Font.REGULAR.metrics(7.0f), Localization.translate(option.getName()), f2 + 18.0f, f8 + 0.5f, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * f * (0.75f + 0.25f * option.getHoverAnimation().getValue() + 0.25f * f7)), 0.8f, 1.0f, f4 - 30.0f - f7 * 9.0f);
                    if (f7 > 0.01f) {
                        float f9 = 0.5f + 0.5f * f7;
                        float f10 = 6.0f * f9;
                        drawContext.drawIcon("check", f2 + f4 - 8.0f - f7 * 2.0f - f10 / 2.0f, f8 + 3.0f - f10 / 2.0f, f10, ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(f * Math.min(1.0f, f7)));
                    }
                    f5 += 12.0f;
                }
                if (this.draggedOption != null && timer.hasElapsed(100L)) {
                    MultiBooleanSetting.this.notifyChange();
                    MultiBooleanSetting.this.options.sort(Comparator.comparingDouble(Option::getLayoutY));
                    timer.reset();
                }
            }

            @Override
            public boolean mouseClicked(float f, float f2, PointerAction pointerAction) {
                if (pointerAction != PointerAction.LEFT_CLICK || !this.inFlow() || !this.contains(f, f2)) {
                    return false;
                }
                float f3 = this.x();
                float f4 = this.y();
                float f5 = this.w();
                float f6 = 0.0f;
                for (Option option : MultiBooleanSetting.this.options) {
                    boolean bl;
                    if (option.isHidden()) continue;
                    boolean bl2 = bl = f >= f3 - 1.0f && f <= f3 + f5 + 1.0f && f2 >= f4 + 3.0f + f6 && f2 <= f4 + 15.0f + f6;
                    if (bl && f <= f3 + 17.0f) {
                        this.draggedOption = option;
                    } else if (bl) {
                        option.toggle();
                    }
                    f6 += 12.0f;
                }
                return true;
            }

            @Override
            public void mouseReleased(float f, float f2, PointerAction pointerAction) {
                this.draggedOption = null;
                super.mouseReleased(f, f2, pointerAction);
            }
        };
        return textComponent.fillWidth();
    }

    @Generated
    public List<Option> getOptions() {
        return this.options;
    }

    @Generated
    public List<Option> getSelectedOptions() {
        return this.selectedOptions;
    }

    @Generated
    public boolean isPreserveOrder() {
        return this.preserveOrder;
    }

    @Generated
    public int getMaxSelections() {
        return this.maxSelections;
    }

    private static /* synthetic */ boolean matchesOptionName(String string, Option option) {
        return option.getName().equalsIgnoreCase(string);
    }

    public static class Option {
        private final MultiBooleanSetting parent;
        private final String name;
        private final String description;
        private final Animation hoverAnimation = new Animation(300L, Easing.easeInOutCubicBezier);
        private final Animation activeAnimation = new Animation(300L, Easing.easeInOutCubicBezier);
        private final Animation positionAnimation = new Animation(300L, Easing.easeOutBack);
        private float layoutY;
        boolean alwaysEnabled;
        private final BooleanSupplier hideCondition;
        private TextureAnimationAtlas.Animation enableAnimation;
        private TextureAnimationAtlas.Animation disableAnimation;
        private TextureAnimationAtlas.Animation currentAnimation;
        private long currentAnimationStartedAtMillis;
        private boolean lastSelected;

        public Option(MultiBooleanSetting multiBooleanSetting, String string) {
            this(multiBooleanSetting, string, "", () -> false);
        }

        public Option(MultiBooleanSetting multiBooleanSetting, String string, BooleanSupplier booleanSupplier) {
            this(multiBooleanSetting, string, "", booleanSupplier);
        }

        public Option(MultiBooleanSetting multiBooleanSetting, String string, String string2) {
            this(multiBooleanSetting, string, string2, () -> false);
        }

        public Option(MultiBooleanSetting multiBooleanSetting, String string, String string2, BooleanSupplier booleanSupplier) {
            this.parent = multiBooleanSetting;
            this.name = string;
            this.description = string2;
            this.hideCondition = booleanSupplier;
            multiBooleanSetting.addOption(this);
        }

        public boolean isHidden() {
            return this.hideCondition != null && this.hideCondition.getAsBoolean();
        }

        public Option select() {
            this.parent.selectOption(this);
            return this;
        }

        public Option deselect() {
            if (!this.alwaysEnabled) {
                this.parent.setSelected(this, false);
            }
            return this;
        }

        public Option alwaysEnabled() {
            this.alwaysEnabled = true;
            this.parent.selectOption(this);
            return this;
        }

        public Option toggle() {
            this.parent.enforceSelectionLimit(this);
            return this;
        }

        public boolean isSelected() {
            return this.parent.getSelectedOptions().contains(this);
        }

        public TextComponent buildComponent() {
            return new TextComponent().bind("selected", this::isSelected, Motion.resolveMotionMotionFromLongAndEasing(220L, Easing.easeInOutCubicBezier)).text(Font.MEDIUM.metrics(7.0f), () -> Localization.translate(this.name), textComponent -> ColorPalette.blendWithContrastBackground(Option.background(textComponent)).mulAlpha(0.75f + 0.25f * textComponent.sig("selected"))).textAlign(Alignment.CENTER).background(Option::background).radius(2.5f).padding(Insets.uniform(3.0f)).cursor(Cursor.HAND).onClick(this::toggle);
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
        public MultiBooleanSetting getParent() {
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
        public Animation getPositionAnimation() {
            return this.positionAnimation;
        }

        @Generated
        public float getLayoutY() {
            return this.layoutY;
        }

        @Generated
        public boolean isAlwaysEnabled() {
            return this.alwaysEnabled;
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

        @Generated
        public boolean wasSelected() {
            return this.lastSelected;
        }

        @Generated
        public void setLayoutY(float f) {
            this.layoutY = f;
        }

        @Generated
        public void setAlwaysEnabled(boolean bl) {
            this.alwaysEnabled = bl;
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
        public void setLastSelected(boolean bl) {
            this.lastSelected = bl;
        }
    }
}
