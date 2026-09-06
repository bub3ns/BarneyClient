/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Position
 *  net.minecraft.Vec3d
 *  net.minecraft.Text
 */
package moscow.rockstar.combat.rotation;

import lombok.Generated;
import moscow.rockstar.api.commands.CommandBuilder;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.combat.rotation.RotationEngine;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

public class RotationEventProcessor {
    private static RotationEventProcessor INSTANCE;
    private RotationEngine activeAutopilot;
    private boolean autopilotActive;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (!this.autopilotActive || this.activeAutopilot == null) {
            return;
        }
        if (this.activeAutopilot.isTargetReached()) {
            this.autopilotActive = false;
            this.activeAutopilot = null;
            Notification.info(Text.of((String)Localization.translate("commands.autopilot.stopped")));
            return;
        }
        if (ClientServiceRegistry.getInstance().getEventListenerSlot().getPendingScreenState().orElse(null) != this.activeAutopilot) {
            this.autopilotActive = false;
            this.activeAutopilot = null;
        }
    };

    private void registerCommand() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public RotationEventProcessor() {
        INSTANCE = this;
        this.registerCommand();
    }

    public void startAutopilot(Vec3d VanillaChestLootTableGenerator) {
        if (VanillaChestLootTableGenerator == null || !ClientServiceRegistry.isInitialized()) {
            return;
        }
        this.stopAutopilot();
        BlockPos adminsky = BlockPos.ofFloored((Position)VanillaChestLootTableGenerator);
        this.activeAutopilot = new RotationEngine(adminsky);
        this.autopilotActive = true;
        ClientServiceRegistry.getInstance().getEventListenerSlot().scheduleScreenState(this.activeAutopilot);
        Notification.info(Text.of((String)Localization.translateFormatted("commands.autopilot.start", VanillaChestLootTableGenerator.getX(), VanillaChestLootTableGenerator.getY(), VanillaChestLootTableGenerator.getZ())));
    }

    public ServiceRegistry buildAutopilotCommand() {
        return CommandBuilder.command("autopilot")
            .aliases("ap", "pilot", "\u0430\u0432\u0442\u043e\u043f\u0438\u043b\u043e\u0442", "\u043f\u0438\u043b\u043e\u0442")
            .description("commands.autopilot.description")
            .argument("x", argument -> argument.optional().validator(RotationEventProcessor::parseCoordinate))
            .argument("y", argument -> argument.optional().validator(RotationEventProcessor::parseCoordinate))
            .argument("z", argument -> argument.optional().validator(RotationEventProcessor::parseCoordinate))
            .handler(this::handleAutopilotCommand)
            .build();
    }

    private static PluginResolver parseCoordinate(String string) {
        try {
            Double.parseDouble(string);
            return PluginResolver.resolveValue(string);
        }
        catch (NumberFormatException numberFormatException) {
            return PluginResolver.resolveMessage(Localization.translate("commands.autopilot.invalid"));
        }
    }

    @Compile
    private void handleAutopilotCommand(DispatchContext dispatchContext) {
        String string = (String)dispatchContext.getArguments().get(0);
        String string2 = (String)dispatchContext.getArguments().get(1);
        String string3 = (String)dispatchContext.getArguments().get(2);
        if (string == null || string2 == null || string3 == null) {
            if (this.autopilotActive) {
                this.stopAutopilot();
                Notification.info(Text.of((String)Localization.translate("commands.autopilot.stopping")));
            } else {
                Notification.error(Text.of((String)Localization.translate("commands.autopilot.not_active")));
            }
            return;
        }
        try {
            this.startAutopilot(new Vec3d(Double.parseDouble(string), Double.parseDouble(string2), Double.parseDouble(string3)));
        }
        catch (NumberFormatException numberFormatException) {
            Notification.error(Text.of((String)Localization.translate("commands.autopilot.invalid")));
        }
    }

    private void stopAutopilot() {
        RotationEngine rotationEngine = this.activeAutopilot;
        this.activeAutopilot = null;
        this.autopilotActive = false;
        if (rotationEngine != null && ClientServiceRegistry.isInitialized() && ClientServiceRegistry.getInstance().getEventListenerSlot().getPendingScreenState().orElse(null) == rotationEngine) {
            ClientServiceRegistry.getInstance().getEventListenerSlot().cancelPendingScreenState();
        }
    }

    @Generated
    public static RotationEventProcessor getInstance() {
        return INSTANCE;
    }
}
