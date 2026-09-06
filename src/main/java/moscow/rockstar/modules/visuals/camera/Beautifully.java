/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.modules.visuals.camera;

import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.mixin.accessors.CameraAccessor;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import pyrock.events.render.CameraUpdateEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Beautifully", category=ModuleCategory.VISUALS, disableLocked=true)
public class Beautifully
extends Module {
    public static final long CAMERA_TRANSITION_TIMEOUT_MILLIS = 200L;
    private MultiBooleanSetting animationSettings;
    private MultiBooleanSetting.Option smoothF5Option;
    private MultiBooleanSetting.Option chatAnimationOption;
    private MultiBooleanSetting.Option tabAnimationOption;
    private MultiBooleanSetting.Option inventoryAnimationOption;
    private MultiBooleanSetting.Option chatHistoryOption;
    private MultiBooleanSetting.Option customTabColumnsOption;
    private NumberSetting tabColumnsSetting;
    private final Animation thirdPersonAnimation = new Animation(300L, Easing.easeInOutSine);
    private final Animation inverseViewAnimation = new Animation(300L, Easing.easeInOutSine);
    private final EventListener<CameraUpdateEvent> cameraUpdateListener = cameraUpdateEvent -> {
        if (!this.smoothF5Option.isSelected()) {
            this.thirdPersonAnimation.setValue(cameraUpdateEvent.isThirdPerson() ? 1.0f : 0.0f);
            this.inverseViewAnimation.setValue(cameraUpdateEvent.isThirdPerson() && cameraUpdateEvent.isInverseView() ? 1.0f : 0.0f);
            return;
        }
        boolean bl = cameraUpdateEvent.isThirdPerson();
        boolean bl2 = cameraUpdateEvent.isInverseView();
        float f = cameraUpdateEvent.getTickDelta();
        this.thirdPersonAnimation.setEasing(bl ? Easing.cubicBezier(0.31, 0.87, 0.41, 1.3) : Easing.cubicBezier(0.17, 0.85, 0.29, 0.99));
        this.thirdPersonAnimation.setDuration(400L);
        this.inverseViewAnimation.setEasing(Easing.cubicBezier(0.31, 0.87, 0.43, 0.94));
        Entity class_12972 = cameraUpdateEvent.getFocusedEntity();
        float f2 = this.thirdPersonAnimation.update(bl ? 1.0f : 0.0f);
        float f3 = this.inverseViewAnimation.update(bl && bl2 ? 1.0f : 0.0f);
        if (this.thirdPersonAnimation.isAtTarget() && this.inverseViewAnimation.isAtTarget()) {
            return;
        }
        CameraAccessor cameraAccessor = (CameraAccessor)cameraUpdateEvent.getCamera();
        double d = MathHelper.lerp((double)f, (double)class_12972.prevX, (double)class_12972.getX());
        double d2 = MathHelper.lerp((double)f, (double)class_12972.prevY, (double)class_12972.getY()) + (double)MathHelper.lerp((float)f, (float)cameraAccessor.getLastCameraY(), (float)cameraAccessor.getCameraY());
        double d3 = MathHelper.lerp((double)f, (double)class_12972.prevZ, (double)class_12972.getZ());
        cameraAccessor.invokeSetPos(new Vec3d(d, d2, d3));
        cameraAccessor.setThirdPerson(bl || f2 >= 0.1f);
        if (f2 > 0.001f) {
            float f4;
            cameraAccessor.invokeSetRotation(class_12972.getYaw(f), class_12972.getPitch(f));
            if (bl2 || f3 > 0.001f) {
                f4 = bl2 ? 180.0f * f3 : -180.0f * f3;
                cameraAccessor.invokeSetRotation(cameraUpdateEvent.getCamera().getYaw() + f4, MathUtils.interpolateDouble(cameraUpdateEvent.getCamera().getPitch(), -cameraUpdateEvent.getCamera().getPitch(), this.inverseViewAnimation.getValue()));
            }
            f4 = class_12972 instanceof LivingEntity ? ((LivingEntity)class_12972).getScale() : 1.0f;
            cameraAccessor.invokeMoveBy(-cameraAccessor.invokeClipToSpace(4.0f * f4 * f2), 0.0f, 0.0f);
        }
    };

    public Beautifully() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.animationSettings = new MultiBooleanSetting(this, "modules.settings.beautifully.select");
        this.smoothF5Option = new MultiBooleanSetting.Option(this.animationSettings, "modules.settings.beautifully.smooth_f5").select();
        this.chatAnimationOption = new MultiBooleanSetting.Option(this.animationSettings, "modules.settings.beautifully.chat_animation").select();
        this.tabAnimationOption = new MultiBooleanSetting.Option(this.animationSettings, "modules.settings.beautifully.tab_animation").select();
        this.inventoryAnimationOption = new MultiBooleanSetting.Option(this.animationSettings, "modules.settings.beautifully.inventory_animation").select();
        this.chatHistoryOption = new MultiBooleanSetting.Option(this.animationSettings, "modules.settings.beautifully.chat_history", "modules.settings.beautifully.chat_history.desc");
        this.customTabColumnsOption = new MultiBooleanSetting.Option(this.animationSettings, "modules.settings.beautifully.custom_tab_columns", "modules.settings.beautifully.custom_tab_columns.desc");
        this.tabColumnsSetting = new NumberSetting((SettingOwner)this, "modules.settings.beautifully.tab_columns", () -> !this.customTabColumnsOption.isSelected()).setMinValue(1.0f).setMaxValue(5.0f).setStep(1.0f).setValue(2.0f);
    }

    private static Beautifully getEnabledInstance() {
        if (RockstarClient.create() == null || RockstarClient.create().getModuleRegistry() == null) {
            return null;
        }
        Beautifully beautifully = RockstarClient.create().getModuleRegistry().getModule(Beautifully.class);
        return beautifully != null && beautifully.isEnabled() ? beautifully : null;
    }

    public static boolean isChatAnimationEnabled() {
        Beautifully beautifully = Beautifully.getEnabledInstance();
        return beautifully != null && beautifully.chatAnimationOption.isSelected();
    }

    public static boolean isTabAnimationEnabled() {
        Beautifully beautifully = Beautifully.getEnabledInstance();
        return beautifully != null && beautifully.tabAnimationOption.isSelected();
    }

    public static boolean isInventoryAnimationEnabled() {
        Beautifully beautifully = Beautifully.getEnabledInstance();
        return beautifully != null && beautifully.inventoryAnimationOption.isSelected();
    }

    public static boolean isChatHistoryEnabled() {
        Beautifully beautifully = Beautifully.getEnabledInstance();
        return beautifully != null && beautifully.chatHistoryOption.isSelected();
    }

    public static int getTabColumnCount() {
        Beautifully beautifully = Beautifully.getEnabledInstance();
        if (beautifully == null || !beautifully.customTabColumnsOption.isSelected()) {
            return 0;
        }
        return (int)beautifully.tabColumnsSetting.getValue();
    }

    @Generated
    public MultiBooleanSetting getAnimationSettings() {
        return this.animationSettings;
    }

    @Generated
    public MultiBooleanSetting.Option getSmoothF5Option() {
        return this.smoothF5Option;
    }

    @Generated
    public MultiBooleanSetting.Option getChatAnimationOption() {
        return this.chatAnimationOption;
    }

    @Generated
    public MultiBooleanSetting.Option getTabAnimationOption() {
        return this.tabAnimationOption;
    }

    @Generated
    public MultiBooleanSetting.Option getInventoryAnimationOption() {
        return this.inventoryAnimationOption;
    }

    @Generated
    public MultiBooleanSetting.Option getChatHistoryOption() {
        return this.chatHistoryOption;
    }

    @Generated
    public MultiBooleanSetting.Option getCustomTabColumnsOption() {
        return this.customTabColumnsOption;
    }

    @Generated
    public NumberSetting getTabColumnsSetting() {
        return this.tabColumnsSetting;
    }

    @Generated
    public Animation getThirdPersonAnimation() {
        return this.thirdPersonAnimation;
    }

    @Generated
    public Animation getInverseViewAnimation() {
        return this.inverseViewAnimation;
    }

    @Generated
    public EventListener<CameraUpdateEvent> getCameraUpdateListener() {
        return this.cameraUpdateListener;
    }
}

