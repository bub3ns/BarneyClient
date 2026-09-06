package moscow.rockstar.modules.visuals.hud;

import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.colors.GradientColors;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.factory.UiNodeFactory;
import moscow.rockstar.ui.hud.DynamicIslandEntry;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.notifications.NotificationRequest;
import moscow.rockstar.ui.notifications.TimedNotification;
import moscow.rockstar.ui.text.Font;
import net.minecraft.util.math.MathHelper;
import pyrock.utility.render.ColorRGBA;

/**
 * The "alerts" island status - a countdown ring plus the newest island notification message.
 * 1:1 with rockstar/ilIlil/IiiIIIIiI, entry [1] of
 * rockstar/ilIlil/IiIiiiIIi#I(Lrockstar/ilIlil/IIiiiiiii;)V.
 *
 * <p>Confirmed mappings used here (read off the bytecode, not the CFR output):
 * <ul>
 *   <li>{@code iiiIIiI} maps to {@link NotificationRequest} (a {@link TimedNotification} of 1000ms
 *       with a no-op render, carrying a type and a message).</li>
 *   <li>{@code iiIiiiI} maps to {@link TimedNotification}. Its three animations are pinned by the
 *       original constructor easings: field {@code I} = 300ms/easeOutBack = fadeAnimation,
 *       {@code II} = 300ms/easeOutBackSoft = slideAnimation, {@code i} =
 *       300ms/easeOutOvershootSoft = contentAnimation. This class only ever touches {@code i()},
 *       i.e. {@link TimedNotification#getContentAnimation()}.</li>
 *   <li>{@code Ii.I().I().I()} maps to
 *       {@code RockstarClient.create().getUiComponentProcessor().getNotifications()}.</li>
 *   <li>{@code iiiIIII} maps to {@code NotificationType} (the enum the remapped
 *       {@link NotificationRequest} actually stores); {@code iiiIIII.i()} is its accent colour.
 *       The switch map in {@code IiiIIIIiI$1} pins SUCCESS to green, ERROR to red, anything else
 *       to the type's own accent.</li>
 * </ul>
 */
public class IslandAlertStatus extends DynamicIslandEntry {
    /** ORIGINAL: the static ColorRGBA field {@code I}. */
    private static final ColorRGBA SUCCESS_COLOR = new ColorRGBA(74.0f, 222.0f, 128.0f);
    /** ORIGINAL: the static ColorRGBA field {@code i}. */
    private static final ColorRGBA ERROR_COLOR = new ColorRGBA(239.0f, 68.0f, 68.0f);
    /** ORIGINAL: the static float field {@code I} = 6.0f. */
    private static final float RING_SIZE = 6.0f;
    /** ORIGINAL: the static float field {@code i} = 1.2f (the ring stroke). */
    private static final float RING_THICKNESS = 1.2f;

    /** ORIGINAL: the package-private field {@code I} of type {@code iiiIIiI}. */
    NotificationRequest current;
    private UiNode contentNode;

    public IslandAlertStatus(MultiBooleanSetting statuses) {
        super(statuses, "alerts");
    }

    /** ORIGINAL: {@code prepare(IiIiiIIII)V}. */
    @Override
    public void prepare(DynamicIslandHud island) {
        this.current = this.newestAlert();
    }

    /** ORIGINAL: {@code content(IiIiiIIII)Lrockstar/ilIlil/iiI;}. */
    @Override
    public UiNode content(DynamicIslandHud island) {
        if (this.contentNode == null) {
            Component row = UiNodeFactory.createRowComponent(15.0f, Insets.of(0.0f, 5.0f, 0.0f, 4.5f), 4.0f)
                    .alignment(Alignment.CENTER)
                    .renderHook((drawContext, component) -> {
                        if (this.current == null) {
                            return;
                        }
                        float progress = this.animation.getValue()
                                * this.current.getContentAnimation().getValue();
                        if (progress <= 0.0f) {
                            return;
                        }
                        ColorRGBA accent = this.accentColor();
                        ColorRGBA solid = accent.withAlpha(71.4f * progress);
                        ColorRGBA clear = accent.withAlpha(0.0f);
                        float radius = island.getRadiusAnim().getValue();
                        drawContext.drawSquircle(component.x(), component.y(), component.w() * 0.69f,
                                component.h(), 2.0f, WidgetState.left(radius, radius),
                                new GradientColors(solid, solid, clear, clear));
                    });
            row.add(new RingNode());
            row.add(new MessageNode());
            this.contentNode = row;
        }
        return this.contentNode;
    }

    /**
     * ORIGINAL: {@code canShow()Z}
     * <pre>
     *   List l = alerts();
     *   if (l.isEmpty()) return false;
     *   iiiIIiI last = l.getLast();
     *   return !last.I().I(last.I());     // !lifetimeTimer.hasElapsed(durationMillis)
     * </pre>
     */
    @Override
    public boolean isVisible() {
        List<NotificationRequest> alerts = this.alerts();
        if (alerts.isEmpty()) {
            return false;
        }
        NotificationRequest last = alerts.getLast();
        return !last.getLifetimeTimer().hasElapsed(last.getDurationMillis());
    }

    /** ORIGINAL: the private {@code I()Lrockstar/ilIlil/iiiIIiI;}. */
    private NotificationRequest newestAlert() {
        List<NotificationRequest> alerts = this.alerts();
        return alerts.isEmpty() ? null : alerts.getLast();
    }

    /** ORIGINAL: the package-private {@code I()Ljava/util/List;}. */
    List<NotificationRequest> alerts() {
        return RockstarClient.create().getUiComponentProcessor().getNotifications().stream()
                .filter(notification -> notification instanceof NotificationRequest)
                .map(notification -> (NotificationRequest)notification)
                .toList();
    }

    /** ORIGINAL: the package-private {@code I()Lpyrock/utility/render/ColorRGBA;}. */
    ColorRGBA accentColor() {
        if (this.current == null) {
            return SUCCESS_COLOR;
        }
        return switch (this.current.getNotificationType()) {
            case SUCCESS -> SUCCESS_COLOR;
            case ERROR -> ERROR_COLOR;
            default -> this.current.getNotificationType().getAccentColor();
        };
    }

    /** ORIGINAL: the inner class {@code IiiIIIIiI$I}. */
    final class RingNode extends UiNode {
        RingNode() {
            this.size(RING_SIZE, RING_SIZE);
            this.interactive(false);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            if (IslandAlertStatus.this.current == null) {
                return;
            }
            for (NotificationRequest alert : IslandAlertStatus.this.alerts()) {
                alert.getContentAnimation().setDuration(500L);
                alert.getContentAnimation().setReverse(IslandAlertStatus.this.current == alert);
            }
            float progress = IslandAlertStatus.this.animation.getValue()
                    * IslandAlertStatus.this.current.getContentAnimation().getValue();
            if (progress <= 0.0f) {
                return;
            }
            long duration = IslandAlertStatus.this.current.getDurationMillis();
            float remaining = duration <= 0L
                    ? 0.0f
                    : MathHelper.clamp((float)(1.0f - (float)IslandAlertStatus.this.current
                            .getLifetimeTimer().getElapsedMillis() / (float)duration),
                            (float)0.0f, (float)1.0f);
            float centerX = this.x() + this.w() / 2.0f - RING_SIZE * (1.0f - progress);
            float centerY = this.y() + this.h() / 2.0f;
            float radius = Math.min(this.w(), this.h()) / 2.0f + 0.5f;
            ColorRGBA track = ColorPalette.getPrimaryTextColor().withAlpha(40.8f * progress);
            ColorRGBA fill = IslandAlertStatus.this.accentColor().withAlpha(255.0f * progress);
            drawContext.drawCircleProgress(centerX, centerY, radius, RING_THICKNESS, 1.0f, track);
            drawContext.drawCircleProgress(centerX, centerY, radius, RING_THICKNESS, remaining, fill);
        }
    }

    /** ORIGINAL: the inner class {@code IiiIIIIiI$i}. */
    final class MessageNode extends UiNode {
        MessageNode() {
            this.interactive(false);
        }

        @Override
        protected void measure() {
            String message = IslandAlertStatus.this.current == null
                    ? "" : IslandAlertStatus.this.current.getMessage();
            this.prefW = message.isEmpty() ? 0.0f : Font.MEDIUM.metrics(7.0f).measureText(message);
            this.prefH = Font.MEDIUM.metrics(7.0f).getFontTopOffset();
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            if (IslandAlertStatus.this.current == null) {
                return;
            }
            String message = IslandAlertStatus.this.current.getMessage();
            float entryProgress = IslandAlertStatus.this.animation.getValue();
            float alertProgress = IslandAlertStatus.this.current.getContentAnimation().getValue();
            drawContext.drawText(Font.MEDIUM.metrics(7.0f), message,
                    this.x() + 6.0f * (1.0f - entryProgress * alertProgress), this.y(),
                    ColorPalette.getPrimaryTextColor().withAlpha(255.0f * alertProgress));
        }
    }
}
