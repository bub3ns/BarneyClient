package moscow.rockstar.settings;

import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.settings.SettingWidget;

/** Bridges a setting's modern component tree into the legacy setting host. */
public final class SettingComponentAdapter extends SettingComponent<Setting> {
    private final Component content;

    public SettingComponentAdapter(Setting setting, SettingWidget host) {
        super(setting, host);
        this.content = setting.buildComponent();
    }

    @Override
    public void tick() {
        if (this.content != null) {
            this.content.prepareRoot();
        }
        super.tick();
    }

    @Override
    public void render(RockstarDrawContext drawContext) {
        if (this.content == null) {
            return;
        }
        this.content.prepareLayout(this.width, this.height);
        this.content.setSlot(this.x, this.y, this.width, this.height);
        this.content.tick(16.0f, drawContext.mouseX(), drawContext.mouseY());
        this.content.draw(drawContext, 1.0f);
    }

    @Override
    protected void renderContent(RockstarDrawContext drawContext) {
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, PointerAction action) {
        if (this.content != null) {
            this.content.mouseClicked((float)mouseX, (float)mouseY, action);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, PointerAction action) {
        if (this.content != null) {
            this.content.mouseReleased((float)mouseX, (float)mouseY, action);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.content != null) {
            this.content.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        return this.content != null && this.content.charTyped(character, modifiers);
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.content != null) {
            this.content.mouseScrolled((float)mouseX, (float)mouseY, (float)horizontalAmount, (float)verticalAmount);
        }
    }

    @Override
    public float getHeight() {
        if (this.content == null) {
            return 18.0f;
        }
        this.content.prepareRoot();
        return Math.max(18.0f, this.content.desiredH());
    }
}
