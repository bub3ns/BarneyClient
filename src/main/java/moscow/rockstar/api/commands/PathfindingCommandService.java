package moscow.rockstar.api.commands;
import moscow.rockstar.ui.localization.Localization;

import java.util.Optional;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.api.validation.ScreenStateService;
import moscow.rockstar.combat.rotation.AimAdjustment;
import moscow.rockstar.core.ClientFeatureFlags;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.ui.notifications.NotificationBridge;

/**
 * ORIGINAL: {@code rockstar/ilIlil/iiIiiiiIi}, slot 27 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 *
 * <p>The original child order is goto, mine, sel, cleararea, fill, stop, pause, resume, status,
 * neuro.  {@code fill} ({@code rockstar/ilIlil/iiIiiiIiI}) is NOT installed: it schedules
 * {@code new iiiiIIIIi(min, max, block)}, a ~30-method block-placement screen state with its own
 * inventory/rotation helpers, and that class has no remapped counterpart - the only
 * {@code ScreenStateService} implementations in the tree are RotationEngine,
 * BlockInteractionState, BlockTargetFinder, ExcavationController and PathNavigator.  The usage
 * text below still advertises it, exactly as the original does.</p>
 */
public final class PathfindingCommandService {
    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("newton")
            .aliases("nt")
            .description("commands.newton.description")
            .children(
                new NavigationCommandService().getCommandRegistration(),
                new MiningCommandService().getCommandRegistration(),
                new SelectionCommandService().getCommandRegistration(),
                new ClearAreaCommandService().getCommandRegistration(),
                createStopCommand(),
                createPauseCommand(),
                createResumeCommand(),
                createStatusCommand(),
                createNeuroCommand())
            .handler(this::showUsage)
            .build();
    }

    private ServiceRegistry createStopCommand() {
        return CommandBuilder.command("stop")
            .aliases("cancel", "стоп")
            .description("commands.newton.stop")
            .handler(dispatchContext -> {
                ClientServiceRegistry services = ClientServiceRegistry.getInstance();
                boolean stopped = services.getEventListenerSlot().hasPendingScreenState();
                services.getEventListenerSlot().cancelPendingScreenState();
                services.getRotationState().disableMovementOverride();
                NotificationBridge.showMessage(Localization.translate(stopped ? "commands.newton.stopped" : "commands.newton.no_process"));
            })
            .build();
    }

    private ServiceRegistry createPauseCommand() {
        return CommandBuilder.command("pause")
            .aliases("пауза")
            .description("commands.newton.pause")
            .handler(dispatchContext -> getActiveScreenState().ifPresentOrElse(
                state -> {
                    state.pauseNavigation();
                    NotificationBridge.showMessage(Localization.translate("commands.newton.paused"));
                },
                () -> NotificationBridge.showPersistentMessage(Localization.translate("commands.newton.no_process"))))
            .build();
    }

    private ServiceRegistry createResumeCommand() {
        return CommandBuilder.command("resume")
            .aliases("продолжить")
            .description("commands.newton.resume")
            .handler(dispatchContext -> getActiveScreenState().ifPresentOrElse(
                state -> {
                    state.resumeNavigation();
                    NotificationBridge.showMessage(Localization.translate("commands.newton.resumed"));
                },
                () -> NotificationBridge.showPersistentMessage(Localization.translate("commands.newton.no_process"))))
            .build();
    }

    private ServiceRegistry createStatusCommand() {
        return CommandBuilder.command("status")
            .aliases("info", "статус")
            .description("commands.newton.status")
            .handler(dispatchContext -> getActiveScreenState().ifPresentOrElse(
                state -> NotificationBridge.showMessage(state.getCommandName() + ": " + state.getStatusMessage()),
                () -> NotificationBridge.showPersistentMessage(Localization.translate("commands.newton.no_process"))))
            .build();
    }

    private ServiceRegistry createNeuroCommand() {
        return CommandBuilder.command("neuro")
            .aliases("нейро")
            .description("commands.newton.neuro")
            .handler(dispatchContext -> {
                if (ClientFeatureFlags.neuroRotationEnabled) {
                    ClientFeatureFlags.neuroRotationEnabled = false;
                    NotificationBridge.showMessage(Localization.translate("commands.newton.neuro_off"));
                    return;
                }
                if (!AimAdjustment.isModelAvailable()) {
                    NotificationBridge.showPersistentMessage(Localization.translate("commands.newton.neuro_no_model"));
                    return;
                }
                ClientFeatureFlags.neuroRotationEnabled = true;
                NotificationBridge.showMessage(Localization.translateFormatted("commands.newton.neuro_on", AimAdjustment.getActiveModelName()));
            })
            .build();
    }

    private static Optional<ScreenStateService> getActiveScreenState() {
        return ClientServiceRegistry.getInstance().getEventListenerSlot().getPendingScreenState();
    }

    private void showUsage(DispatchContext dispatchContext) {
        NotificationBridge.showMessage(Localization.translate("commands.newton.usage.header"));
        NotificationBridge.showMessage(" " + Localization.translate("commands.newton.usage.goto"));
        NotificationBridge.showMessage(" " + Localization.translate("commands.newton.usage.mine"));
        NotificationBridge.showMessage(" " + Localization.translate("commands.newton.usage.sel"));
        NotificationBridge.showMessage(" " + Localization.translate("commands.newton.usage.cleararea"));
        NotificationBridge.showMessage(" " + Localization.translate("commands.newton.usage.fill"));
        NotificationBridge.showMessage(" " + Localization.translate("commands.newton.usage.control"));
        NotificationBridge.showMessage(" " + Localization.translate("commands.newton.usage.neuro"));
    }
}
