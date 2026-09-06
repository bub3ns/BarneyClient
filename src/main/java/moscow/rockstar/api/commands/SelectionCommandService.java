package moscow.rockstar.api.commands;
import moscow.rockstar.ui.localization.Localization;

import java.util.List;
import java.util.Set;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.ui.notifications.NotificationBridge;
import moscow.rockstar.world.selection.BlockSelectionState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * ORIGINAL: {@code rockstar/ilIlil/iiIiiiiiI}, the third child of {@code newton}
 * ({@code rockstar/ilIlil/iiIiiiiIi#I()V}).
 */
public final class SelectionCommandService {
    static final Set<String> CLEAR_ALIASES = Set.of(
        "clear", "reset", "\u0441\u0431\u0440\u043e\u0441", "\u043e\u0447\u0438\u0441\u0442\u0438\u0442\u044c");
    static final Object CLEAR_SENTINEL = new Object();
    private static final double RAYCAST_DISTANCE = 256.0;

    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("sel")
            .description("commands.sel.description")
            .aliases("pos", "\u0432\u044b\u0434\u0435\u043b\u0438\u0442\u044c")
            .argument("args", argument -> argument.vararg().optional().validator(new CommandSuggestionProvider() {
                @Override
                public PluginResolver validate(String value) {
                    if (CLEAR_ALIASES.contains(value.toLowerCase())) {
                        return PluginResolver.resolveValue(CLEAR_SENTINEL);
                    }
                    try {
                        return PluginResolver.resolveValue(Integer.parseInt(value));
                    } catch (NumberFormatException exception) {
                        return PluginResolver.resolveMessage(
                            Localization.translateFormatted("commands.sel.not_a_number", value));
                    }
                }

                @Override
                public List<String> suggestions(String prefix) {
                    return "clear".startsWith(prefix.toLowerCase()) ? List.of("clear") : List.of();
                }
            }))
            .handler(this::executeSelectCommand)
            .build();
    }

    private void executeSelectCommand(DispatchContext dispatchContext) {
        BlockPos position;
        List<?> values = (List<?>) dispatchContext.getArguments().get(0);
        BlockSelectionState selection = BlockSelectionState.getInstance();
        if (values != null && values.stream().anyMatch(value -> value == CLEAR_SENTINEL)) {
            selection.clearSelection();
            NotificationBridge.showMessage(
                Localization.translate("commands.sel.cleared"));
            return;
        }
        if (values != null && !values.isEmpty()) {
            List<Integer> numbers = values.stream().filter(Integer.class::isInstance).map(Integer.class::cast).toList();
            if (numbers.size() != 3) {
                NotificationBridge.showPersistentMessage(
                    Localization.translate("commands.sel.usage"));
                return;
            }
            position = new BlockPos(numbers.get(0), numbers.get(1), numbers.get(2));
        } else {
            position = raycastTargetBlock();
            if (position == null) {
                NotificationBridge.showPersistentMessage(
                    Localization.translate("commands.sel.no_block"));
                return;
            }
        }
        selection.select(position);
        int corner = selection.hasSelection() ? 2 : 1;
        if (selection.hasSelection()) {
            BlockPos minimum = minimumPosition(selection);
            BlockPos maximum = maximumPosition(selection);
            long volume = (long) (maximum.getX() - minimum.getX() + 1)
                * (long) (maximum.getY() - minimum.getY() + 1)
                * (long) (maximum.getZ() - minimum.getZ() + 1);
            NotificationBridge.showMessage(Localization.translateFormatted("commands.sel.corner_done", corner, format(position), volume));
        } else {
            NotificationBridge.showMessage(Localization.translateFormatted("commands.sel.corner_first", corner, format(position)));
        }
    }

    /**
     * ORIGINAL: {@code iiIiiIIii#II()} - the per-axis minimum of the two selected corners.
     * The remapped {@link BlockSelectionState} exposes only the raw endpoints.
     */
    static BlockPos minimumPosition(BlockSelectionState selection) {
        BlockPos start = selection.getStartPosition();
        BlockPos end = selection.getEndPosition();
        return new BlockPos(
            Math.min(start.getX(), end.getX()),
            Math.min(start.getY(), end.getY()),
            Math.min(start.getZ(), end.getZ()));
    }

    /** ORIGINAL: {@code iiIiiIIii#Ii()} - the per-axis maximum of the two selected corners. */
    static BlockPos maximumPosition(BlockSelectionState selection) {
        BlockPos start = selection.getStartPosition();
        BlockPos end = selection.getEndPosition();
        return new BlockPos(
            Math.max(start.getX(), end.getX()),
            Math.max(start.getY(), end.getY()),
            Math.max(start.getZ(), end.getZ()));
    }

    static String format(BlockPos position) {
        return position.getX() + ", " + position.getY() + ", " + position.getZ();
    }

    private static BlockPos raycastTargetBlock() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return null;
        }
        Vec3d origin = client.player.getEyePos();
        Vec3d direction = client.player.getRotationVec(1.0f);
        Vec3d target = origin.add(direction.multiply(RAYCAST_DISTANCE));
        RaycastContext context = new RaycastContext(origin, target,
            RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity) client.player);
        BlockHitResult hit = client.world.raycast(context);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return hit.getBlockPos();
    }
}
