package moscow.rockstar.ui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.api.data.ClientConfigManager;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.InertiaRect;
import moscow.rockstar.ui.color.ColorPickerScreen;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.CursorManager;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.localization.Localization;
import net.minecraft.client.gui.screen.Screen;

/** Base state and geometry for an on-screen HUD element. 1:1 with rockstar/ilIlil/IiIiIIiII. */
public abstract class HudElement
implements SettingOwner,
ClientAccess {
    private static final float SNAP_DIST = 2.0f;

    protected float x;
    protected float y;
    protected float width;
    protected float height;

    protected final Animation animation = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
    protected final Animation visible = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
    protected final Animation selecting = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
    protected final Animation dragAnim = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
    private final Animation blurAnim = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
    private final Animation loadingAnim = new Animation(700L, 0.0f, Easing.smoothStep);

    protected boolean showing;
    protected boolean select;
    private float lastWidth;
    private boolean widthTracked;
    private List<Setting> settings = new ArrayList<Setting>();
    protected final InertiaRect inertion = new InertiaRect();
    protected Component flow;
    private long lastFrame;
    private boolean dragging;
    private float dragX;
    private float dragY;
    private float startDragX;
    private float startDragY;
    protected final String name;
    protected final String icon;
    private Anchor anchorX;
    private Anchor anchorY;

    public HudElement(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    public void render(RockstarDrawContext drawContext) {
        this.render(drawContext, 1.0f);
    }

    public void render(RockstarDrawContext drawContext, float extraAlpha) {
        if (drawContext == null) {
            return;
        }
        this.update(drawContext);
        float base = this.animation.getValue() * this.visible.getValue();
        float alpha = base * extraAlpha;
        if (alpha == 0.0f) {
            return;
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, Math.min(1.0f, alpha));
        float scale = 0.5f + base * 0.5f - 0.05f * this.selecting.getValue();
        ItemRenderUtils.translateAndScale(drawContext.getMatrices(),
                this.x + this.width / 2.0f, this.y + this.height / 2.0f, scale);
        this.renderComponent(drawContext);
        ItemRenderUtils.popMatrix(drawContext.getMatrices());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected Component build() {
        return null;
    }

    public void rebuild() {
        this.flow = null;
        this.lastFrame = 0L;
        this.widthTracked = false;
    }

    public void renderComponent(RockstarDrawContext drawContext) {
        if (this.flow == null) {
            this.flow = this.build();
        }
        if (this.flow == null) {
            return;
        }
        long now = System.currentTimeMillis();
        float delta = this.lastFrame == 0L ? 16.0f : Math.min(64.0f, (float)(now - this.lastFrame));
        this.lastFrame = now;
        this.flow.prepareRoot();
        this.flow.snapAt(this.x, this.y);
        this.flow.tick(delta, drawContext.mouseX(), drawContext.mouseY());
        this.width = this.flow.w();
        this.height = this.flow.h();
        if (this.syncWidth()) {
            this.flow.snapAt(this.x, this.y);
            this.flow.tick(0.0f, drawContext.mouseX(), drawContext.mouseY());
        }
        this.flow.draw(drawContext, Math.min(1.0f, this.animation.getValue() * this.visible.getValue()));
    }

    private boolean syncWidth() {
        if (!this.widthTracked) {
            if (this.width <= 0.0f) {
                return false;
            }
            this.lastWidth = this.width;
            this.widthTracked = true;
            return false;
        }
        float delta = this.width - this.lastWidth;
        this.lastWidth = this.width;
        if (delta == 0.0f || !this.anchorsRightEdge()) {
            return false;
        }
        boolean leftHalf = this.x + this.width / 2.0f < WindowMetricsProvider.INSTANCE.width() / 2.0f;
        this.pushNeighbours(delta, leftHalf);
        if (leftHalf || this.dragging) {
            return false;
        }
        this.x -= delta;
        return true;
    }

    protected boolean anchorsRightEdge() {
        return true;
    }

    private void pushNeighbours(float delta, boolean leftHalf) {
        List<HudElement> list = RockstarClient.create().getHudElementRegistry().elements();
        for (HudElement other : list) {
            if (other == this || !other.isShowing()) {
                continue;
            }
            float overlap = Math.min(this.y + this.height, other.y + other.height) - Math.max(this.y, other.y);
            if (overlap <= 0.0f) {
                continue;
            }
            float gap = leftHalf ? other.x - (this.x + this.width) : this.x - (other.x + other.width);
            if (gap < -5.0f || gap > 25.0f) {
                continue;
            }
            other.x = other.x + (leftHalf ? delta : -delta);
            other.x = Math.max(0.0f, Math.min(other.x, WindowMetricsProvider.INSTANCE.width() - other.width));
        }
    }

    public void update(RockstarDrawContext drawContext) {
        this.syncWidth();
        this.dragAnim.setReverse(this.dragging);
        this.animation.setEasing(this.showing ? Easing.easeOutBack : Easing.easeInBack);
        this.animation.setReverse(this.showing);
        this.visible.setEasing(this.show() ? Easing.easeOutBack : Easing.easeInBack);
        this.visible.setReverse(this.show());
        this.selecting.setReverse(this.select);
        this.blurAnim.setReverse(this.animation.getValue() >= 0.6f);
        if (this.dragging) {
            float maxX = Math.max(0.0f, WindowMetricsProvider.INSTANCE.width() - this.width);
            float maxY = Math.max(0.0f, WindowMetricsProvider.INSTANCE.height() - this.height);
            this.x = Math.clamp((float)drawContext.mouseX() - this.dragX, 0.0f, maxX);
            this.y = Math.clamp((float)drawContext.mouseY() - this.dragY, 0.0f, maxY);
            // ORIGINAL: if (!(this instanceof IiIiiIIII /* the "hud.dynamic_island" element */) && !class_437.method_25441())
            if (!"hud.dynamic_island".equals(this.name) && !Screen.hasControlDown()) {
                this.x = Math.clamp(this.x + this.snapDelta(HudSnapGuide.Axis.VERTICAL, this.x, this.width), 0.0f, maxX);
                this.y = Math.clamp(this.y + this.snapDelta(HudSnapGuide.Axis.HORIZONTAL, this.y, this.height), 0.0f, maxY);
            }
        }
        if (this.isHovered(drawContext) && this.animation.getValue() >= 1.0f) {
            CursorManager.request(Cursor.HAND);
        }
    }

    public void onMouseClicked(double mouseX, double mouseY, PointerAction action) {
        if (this.isHovered(mouseX, mouseY) && this.showing) {
            if (action == PointerAction.LEFT_CLICK) {
                this.beginDrag(mouseX, mouseY);
            } else if (action == PointerAction.RIGHT_CLICK) {
                // ORIGINAL rockstar/ilIlil/IiIiIIiII#onMouseClicked, RIGHT_CLICK branch — disassembled.
                // The popup builder is obf rockstar/ilIlil/IiIIiiIii; in this tree it is ported as
                // moscow.rockstar.ui.color.ColorPickerScreen (a misleading name: that class is the
                // context/right-click menu, not a colour picker).
                this.select = true;
                this.loadingAnim.setValue(0.0f);
                ColorPickerScreen menu = new ColorPickerScreen((float)mouseX, (float)mouseY, 110.0f, 6.0f)
                        .addTextRow(Localization.translate(this.settings.isEmpty() ? "actions" : "settings"))
                        .addSeparator();
                for (Setting setting : this.settings) {
                    menu.addSetting(setting);
                }
                menu.addActionOption(Localization.translate("remove"), "trash", popup -> {
                            this.setShowing(false);
                            popup.setOpen(false);
                            ClientConfigManager.getInstance().save("client");
                        })
                        .setCloseCallback(() -> {
                            this.select = false;
                            ClientConfigManager.getInstance().save("client");
                        });
                RockstarClient.create().getHudElementRegistry().popups().add(menu);
            }
        }
    }

    protected void beginDrag(double mouseX, double mouseY) {
        this.dragging = true;
        this.dragX = (float)(mouseX - (double)this.x);
        this.dragY = (float)(mouseY - (double)this.y);
        this.startDragX = this.x;
        this.startDragY = this.y;
    }

    public void onMouseReleased(double mouseX, double mouseY, PointerAction action) {
        if (this.dragging && action == PointerAction.LEFT_CLICK) {
            this.dragging = false;
            this.anchorY = null;
            this.anchorX = null;
            if (this.x != this.startDragX || this.y != this.startDragY) {
                RockstarClient.create().getHudElementRegistry().moveHistory()
                        .record(this, this.startDragX, this.startDragY, this.x, this.y);
            }
            ClientConfigManager.getInstance().save("client");
        }
    }

    private float snapDelta(HudSnapGuide.Axis axis, float pos, float size) {
        float best = 0.0f;
        float bestDistance = 3.0f;
        float[] anchors = HudElement.anchors(pos, size);
        for (HudSnapGuide guide : RockstarClient.create().getHudElementRegistry().snapGuides().guides()) {
            if (guide.getAxis() != axis) {
                continue;
            }
            for (float anchor : anchors) {
                float delta = guide.getPosition() - anchor;
                float distance = Math.abs(delta);
                if (distance > SNAP_DIST || distance >= bestDistance) {
                    continue;
                }
                best = delta;
                bestDistance = distance;
            }
        }
        if (bestDistance > SNAP_DIST) {
            return 0.0f;
        }
        for (HudSnapGuide guide : RockstarClient.create().getHudElementRegistry().snapGuides().guides()) {
            if (guide.getAxis() != axis) {
                continue;
            }
            for (float anchor : anchors) {
                if (!(Math.abs(guide.getPosition() - anchor - best) <= 0.001f)) {
                    continue;
                }
                guide.setHighlighted(true);
                return best;
            }
        }
        return best;
    }

    private static float[] anchors(float pos, float size) {
        return new float[]{pos, pos + size * 0.5f, pos + size};
    }

    /** Visibility predicate used by both built-in and script HUD elements. */
    public boolean show() {
        return true;
    }

    public boolean isHovered(float mouseX, float mouseY) {
        return UiUtils.contains(this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return UiUtils.contains(this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    public boolean isHovered(RockstarDrawContext drawContext) {
        return this.isHovered(drawContext.mouseX(), drawContext.mouseY());
    }

    public void pos(float x, float y) {
        this.x = x;
        this.y = y;
        this.anchorY = null;
        this.anchorX = null;
    }

    public void reanchor(float oldWidth, float oldHeight, float newWidth, float newHeight) {
        if (this.dragging) {
            return;
        }
        if (this.anchorX == null) {
            this.anchorX = HudElement.zoneOf(this.x, this.width, oldWidth);
        }
        if (this.anchorY == null) {
            this.anchorY = HudElement.zoneOf(this.y, this.height, oldHeight);
        }
        this.x = HudElement.reanchorAxis(this.anchorX, this.x, this.width, oldWidth, newWidth);
        this.y = HudElement.reanchorAxis(this.anchorY, this.y, this.height, oldHeight, newHeight);
    }

    private static Anchor zoneOf(float pos, float size, float total) {
        float center = pos + size / 2.0f;
        float third = total / 3.0f;
        if (center <= third) {
            return Anchor.START;
        }
        if (center >= total - third) {
            return Anchor.END;
        }
        return Anchor.CENTER;
    }

    private static float reanchorAxis(Anchor anchor, float pos, float size, float oldTotal, float newTotal) {
        float result = switch (anchor) {
            case START -> pos;
            case END -> newTotal - size - (oldTotal - (pos + size));
            case CENTER -> newTotal / 2.0f + (pos + size / 2.0f - oldTotal / 2.0f) - size / 2.0f;
        };
        return Math.clamp(result, 0.0f, Math.max(0.0f, newTotal - size));
    }

    public void setShowing(boolean showing) {
        boolean wasShowing = this.showing;
        this.showing = showing;
        if (showing && !wasShowing) {
            this.rebuild();
        }
        if (wasShowing != showing) {
            HudElementRegistry registry = RockstarClient.create().getHudElementRegistry();
            if (registry != null) {
                registry.notifyShowingChanged(this);
            }
        }
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public Animation getAnimation() {
        return this.animation;
    }

    public Animation getVisible() {
        return this.visible;
    }

    public Animation getSelecting() {
        return this.selecting;
    }

    public Animation getDragAnim() {
        return this.dragAnim;
    }

    public Animation getBlurAnim() {
        return this.blurAnim;
    }

    public Animation getLoadingAnim() {
        return this.loadingAnim;
    }

    public boolean isShowing() {
        return this.showing;
    }

    public boolean isSelect() {
        return this.select;
    }

    public float getLastWidth() {
        return this.lastWidth;
    }

    public boolean isWidthTracked() {
        return this.widthTracked;
    }

    @Override
    public List<Setting> getSettings() {
        return this.settings;
    }

    public InertiaRect getInertion() {
        return this.inertion;
    }

    public Component getFlow() {
        return this.flow;
    }

    public long getLastFrame() {
        return this.lastFrame;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public float getDragX() {
        return this.dragX;
    }

    public float getDragY() {
        return this.dragY;
    }

    public float getStartDragX() {
        return this.startDragX;
    }

    public float getStartDragY() {
        return this.startDragY;
    }

    public String getName() {
        return this.name;
    }

    public String getIcon() {
        return this.icon;
    }

    public Anchor getAnchorX() {
        return this.anchorX;
    }

    public Anchor getAnchorY() {
        return this.anchorY;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public void setLastWidth(float lastWidth) {
        this.lastWidth = lastWidth;
    }

    public void setWidthTracked(boolean widthTracked) {
        this.widthTracked = widthTracked;
    }

    public void setSettings(List<Setting> settings) {
        this.settings = settings;
    }

    public void setFlow(Component flow) {
        this.flow = flow;
    }

    public void setLastFrame(long lastFrame) {
        this.lastFrame = lastFrame;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public void setDragX(float dragX) {
        this.dragX = dragX;
    }

    public void setDragY(float dragY) {
        this.dragY = dragY;
    }

    public void setStartDragX(float startDragX) {
        this.startDragX = startDragX;
    }

    public void setStartDragY(float startDragY) {
        this.startDragY = startDragY;
    }

    public void setAnchorX(Anchor anchorX) {
        this.anchorX = anchorX;
    }

    public void setAnchorY(Anchor anchorY) {
        this.anchorY = anchorY;
    }

    /** rockstar/ilIlil/IiIiIIiII$I — constants I, i, II in declaration order. */
    public static enum Anchor {
        START,
        CENTER,
        END;
    }
}
