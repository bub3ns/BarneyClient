package moscow.rockstar.ui.screens;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.items.assist.AssistItemProvider;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.ActionSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingComponent;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.settings.SettingWidget;

/** Owns the editable settings and key-capture state for one Assist item. */
public final class AssistItemSettingsState {
    private final AssistItemProvider provider;
    private final ActionSetting deleteAction;
    private final List<SettingComponent> settingComponents = new ArrayList<>();
    private final SettingWidget componentHost = new SettingWidget() {
        @Override
        protected void renderContent(RockstarDrawContext drawContext) {
        }
    };
    private boolean capturingKeyBinding;

    public AssistItemSettingsState(AssistItemProvider provider, Runnable removalAction) {
        this.provider = provider;
        for (Setting setting : provider.getSettings()) {
            SettingComponent component = UiUtils.createSettingWidget(setting, this.componentHost);
            if (component == null) {
                continue;
            }
            this.settingComponents.add(component);
        }
        this.deleteAction = new ActionSetting(new SettingOwner() {
            private final List<Setting> settings = new ArrayList<Setting>();

            @Override
            public List<Setting> getSettings() {
                return this.settings;
            }
        }, "macro.delete").withAction(removalAction);
        SettingComponent deleteComponent = UiUtils.createSettingWidget(this.deleteAction, this.componentHost);
        if (deleteComponent != null) {
            this.settingComponents.add(deleteComponent);
        }
    }

    public AssistItemProvider getProvider() {
        return this.provider;
    }

    public ActionSetting getDeleteAction() {
        return this.deleteAction;
    }

    public SettingWidget getComponentHost() {
        return this.componentHost;
    }

    public List<SettingComponent> getSettingComponents() {
        return this.settingComponents;
    }

    /** Returns the measured height of the settings rows in the detail panel. */
    public float getContentHeight() {
        float height = 0.0f;
        for (SettingComponent component : this.settingComponents) {
            height += component.getHeight();
        }
        return height;
    }

    public boolean isCapturingKeyBinding() {
        return this.capturingKeyBinding;
    }

    public void beginKeyBindingCapture() {
        this.capturingKeyBinding = true;
    }

    public void cancelKeyBindingCapture() {
        this.capturingKeyBinding = false;
    }

    public void assignKeyCode(int keyCode) {
        this.provider.setKeyCode(keyCode);
    }

    public void clearKeyBinding() {
        this.provider.setKeyCode(-1);
    }
}
