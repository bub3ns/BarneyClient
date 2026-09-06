package moscow.rockstar.api.commands;

import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.ui.localization.Localization;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import ua.mintantileak.spk.Compile;

public class VerticalClipCommandService implements ClientAccess {
    private Vec3d lastClipPosition;

    @Compile
    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("vclip")
            .aliases("v", "verticalclip")
            .description("commands.vclip.description")
            .argument("distance", argument -> argument.validator(value -> {
                try {
                    return PluginResolver.resolveValue(Double.parseDouble(value));
                } catch (NumberFormatException exception) {
                    return PluginResolver.resolveMessage(Localization.translate("commands.vclip.invalid"));
                }
            }))
            .handler(this::executeVerticalClip)
            .build();
    }

    @Compile
    private void executeVerticalClip(DispatchContext dispatchContext) {
        double distance = (Double) dispatchContext.getArguments().getFirst();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Vec3d position = client.player.getPos();
            lastClipPosition = position;
            client.player.setPosition(position.add(0.0, distance, 0.0));
        }
    }
}
