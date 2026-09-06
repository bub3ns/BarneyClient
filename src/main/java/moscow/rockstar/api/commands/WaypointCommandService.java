package moscow.rockstar.api.commands;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.world.waypoints.WaypointStore;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import ua.mintantileak.spk.Compile;

/**
 * ORIGINAL: {@code rockstar/ilIlil/IIiiiiII}, slot 19 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 *
 * <p>OMITTED: the original also implements the HUD listener that draws a floating
 * name tag over every stored waypoint.  That listener calls
 * {@code rockstar/ilIlil/IiIIiIIiI#I (CustomDrawContext;String;D;ColorRGBA;F)V}, whose
 * only remapped counterpart is the private
 * {@code modules.visuals.world.WardenHelper#renderWardenLabel} - a four-argument
 * variant with the distance parameter dropped.  There is no reachable renderer to
 * call, so the listener is not reproduced here.</p>
 */
public class WaypointCommandService implements ClientAccess {
    private static final String BASE64_PREFIX = "b64:";
    private static final String DEFAULT_NAME_PREFIX = "Waypoint ";

    public final ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("waypoint")
            .aliases("way", "gps")
            .description("commands.way.description")
            .argument("action", argument -> argument
                .choicesAndValidator("add", "remove", "del", "clear", "list")
                .choices("add", "remove", "clear", "list"))
            .argument("name", argument -> argument.optional().validator(PluginResolver::resolveValue))
            .argument("x", argument -> argument.optional().validator(this::validateNumber))
            .argument("y", argument -> argument.optional().validator(this::validateNumber))
            .argument("z", argument -> argument.optional().validator(this::validateNumber))
            .handler(this::executeWaypointCommand)
            .build();
    }

    private PluginResolver validateNumber(String value) {
        try {
            Integer.parseInt(value);
            return PluginResolver.resolveValue(value);
        } catch (NumberFormatException exception) {
            return PluginResolver.resolveMessage(Localization.translate("commands.way.error_number"));
        }
    }

    @Compile
    private void executeWaypointCommand(DispatchContext dispatchContext) {
        String action = this.stringArgument(dispatchContext, 0);
        String name = this.stringArgument(dispatchContext, 1);
        String x = this.stringArgument(dispatchContext, 2);
        String y = this.stringArgument(dispatchContext, 3);
        String z = this.stringArgument(dispatchContext, 4);
        WaypointStore store = RockstarClient.create().getWaypointStore();
        if (action == null) {
            Notification.error(Text.of((String) Localization.translate("commands.way.help")));
            return;
        }
        switch (action.toLowerCase()) {
            case "add" -> this.addWaypoint(store, name, x, y, z);
            case "remove", "del" -> {
                String joined = this.joinName(name, x, y, z);
                String decoded = this.decodeName(joined);
                if (decoded == null || decoded.isBlank()) {
                    Notification.error(Text.of((String) Localization.translate("commands.way.error_name")));
                    return;
                }
                store.removeWaypoint(decoded);
            }
            case "clear" -> store.clearWaypoints();
            case "list" -> {
                if (store.getWaypoints().isEmpty()) {
                    Notification.info(Text.of((String) Localization.translate("commands.way.list_empty")));
                    return;
                }
                Notification.info(Text.of((String) Localization.translate("commands.way.list_header")));
                store.getWaypoints().forEach(entry ->
                    Notification.sendChat((Text) this.formatEntry(entry.getKey(), entry.getValue())));
            }
            default -> { }
        }
    }

    private String joinName(String name, String x, String y, String z) {
        if (name == null) {
            return null;
        }
        if (name.startsWith(BASE64_PREFIX)) {
            return name;
        }
        if (x == null && y == null && z == null) {
            return name;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        if (x != null) {
            builder.append(" ").append(x);
        }
        if (y != null) {
            builder.append(" ").append(y);
        }
        if (z != null) {
            builder.append(" ").append(z);
        }
        return builder.toString().trim();
    }

    private void addWaypoint(WaypointStore store, String name, String x, String y, String z) {
        Waypoint waypoint = this.resolveWaypoint(store, name, x, y, z);
        if (waypoint == null) {
            return;
        }
        store.addWaypoint(waypoint.name(), waypoint.x(), waypoint.y(), waypoint.z());
    }

    private Waypoint resolveWaypoint(WaypointStore store, String name, String x, String y, String z) {
        int playerX = minecraftClient.player.getBlockX();
        int playerY = minecraftClient.player.getBlockY();
        int playerZ = minecraftClient.player.getBlockZ();
        NamedCoordinates named = this.splitNameAndCoordinates(store, name, x, y, z);
        Coordinates coordinates = this.parseCoordinates(named.coordinates(), playerX, playerY, playerZ);
        if (coordinates == null) {
            return null;
        }
        return new Waypoint(named.name(), coordinates.x(), coordinates.y(), coordinates.z());
    }

    private NamedCoordinates splitNameAndCoordinates(WaypointStore store, String name, String x, String y, String z) {
        if (this.allBlank(name, x, y, z)) {
            return new NamedCoordinates(this.nextDefaultName(store), List.of());
        }
        if (this.bothNumeric(name, x)) {
            return new NamedCoordinates(this.nextDefaultName(store), this.collect(name, x, y, z));
        }
        return new NamedCoordinates(this.nameOrDefault(store, name), this.collect(x, y, z));
    }

    private boolean allBlank(String name, String x, String y, String z) {
        return name == null && x == null && y == null && z == null;
    }

    private boolean bothNumeric(String first, String second) {
        return this.isInteger(first) && this.isInteger(second);
    }

    private String nameOrDefault(WaypointStore store, String name) {
        if (name == null || name.isBlank()) {
            return this.nextDefaultName(store);
        }
        return name;
    }

    private List<String> collect(String first, String second, String third, String fourth) {
        ArrayList<String> values = new ArrayList<String>(4);
        this.addIfPresent(values, first);
        this.addIfPresent(values, second);
        this.addIfPresent(values, third);
        this.addIfPresent(values, fourth);
        return values;
    }

    private List<String> collect(String first, String second, String third) {
        ArrayList<String> values = new ArrayList<String>(3);
        this.addIfPresent(values, first);
        this.addIfPresent(values, second);
        this.addIfPresent(values, third);
        return values;
    }

    private void addIfPresent(List<String> values, String value) {
        if (value != null) {
            values.add(value);
        }
    }

    private Coordinates parseCoordinates(List<String> values, int playerX, int playerY, int playerZ) {
        try {
            return switch (values.size()) {
                case 0 -> new Coordinates(playerX, playerY, playerZ);
                case 2 -> new Coordinates(Integer.parseInt(values.get(0)), playerY, Integer.parseInt(values.get(1)));
                case 3 -> new Coordinates(
                    Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)), Integer.parseInt(values.get(2)));
                default -> {
                    Notification.error(Text.of((String) Localization.translate("commands.way.help")));
                    yield null;
                }
            };
        } catch (NumberFormatException exception) {
            Notification.error(Text.of((String) Localization.translate("commands.way.error_numbers")));
            return null;
        }
    }

    private boolean isInteger(String value) {
        if (value == null) {
            return false;
        }
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private String nextDefaultName(WaypointStore store) {
        int index = 1;
        while (store.containsWaypoint(DEFAULT_NAME_PREFIX + index)) {
            ++index;
        }
        return DEFAULT_NAME_PREFIX + index;
    }

    private String encodeName(String name) {
        if (name == null) {
            return "";
        }
        return BASE64_PREFIX + Base64.getUrlEncoder().withoutPadding()
            .encodeToString(name.getBytes(StandardCharsets.UTF_8));
    }

    private String decodeName(String name) {
        if (name == null) {
            return null;
        }
        if (name.startsWith(BASE64_PREFIX)) {
            try {
                byte[] decoded = Base64.getUrlDecoder().decode(name.substring(BASE64_PREFIX.length()));
                return new String(decoded, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        return name;
    }

    private String stringArgument(DispatchContext dispatchContext, int index) {
        if (dispatchContext == null || dispatchContext.getArguments() == null) {
            return null;
        }
        if (dispatchContext.getArguments().size() <= index) {
            return null;
        }
        Object value = dispatchContext.getArguments().get(index);
        return value instanceof String ? (String) value : null;
    }

    private MutableText formatEntry(String name, Vec3d position) {
        int x = (int) Math.round(position.x);
        int y = (int) Math.round(position.y);
        int z = (int) Math.round(position.z);
        String removeCommand = ".waypoint remove " + this.encodeName(name);
        MutableText nameText = Text.literal((String) name).setStyle(Style.EMPTY
            .withColor(TextColor.fromFormatting((Formatting) Formatting.AQUA))
            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, removeCommand))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                Text.of((String) Localization.translate("commands.way.list_hover")))));
        MutableText coordinatesText = Text.literal((String) Localization.translateFormatted(
            "commands.way.list_coordinates", x, y, z))
            .setStyle(Style.EMPTY.withColor(TextColor.fromFormatting((Formatting) Formatting.GRAY)));
        return Text.literal("- ")
            .setStyle(Style.EMPTY.withColor(TextColor.fromFormatting((Formatting) Formatting.GRAY)))
            .append((Text) nameText)
            .append(" ")
            .append((Text) coordinatesText);
    }

    /** ORIGINAL: the nested {@code record I(String name, int x, int y, int z)}. */
    private record Waypoint(String name, int x, int y, int z) {
    }

    /** ORIGINAL: the nested {@code record II(String name, List<String> cords)}. */
    private record NamedCoordinates(String name, List<String> coordinates) {
    }

    /** ORIGINAL: the nested {@code record i(int x, int y, int z)}. */
    private record Coordinates(int x, int y, int z) {
    }
}
