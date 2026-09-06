package moscow.rockstar.items.assist;

import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;

/** Server gates used by the original Assist provider table. */
public final class AssistFeatureAvailability {
    private AssistFeatureAvailability() {
    }

    private static boolean majorNetworkVariant() {
        return ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD_VARIANTS)
            || ServerDetector.isServerProfileSupported(ServerProfile.SUPPORTED_NETWORKS)
            || ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD_VARIANTS);
    }

    public static boolean potionLike() {
        return !majorNetworkVariant()
            || ServerDetector.isServerProfileSupported(ServerProfile.SUPPORTED_NETWORKS);
    }

    public static boolean commonConsumable() {
        return ServerDetector.isServerProfileSupported(ServerProfile.COMMON_NETWORKS)
            || (!ServerDetector.isServerProfileSupported(ServerProfile.COMMON_NETWORKS)
                && !ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)
                && !ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY));
    }

    public static boolean holyworldOrUnknown() {
        return !majorNetworkVariant()
            || ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD_VARIANTS);
    }

    public static boolean unrestrictedClient() {
        return !ServerDetector.isServerProfileSupported(ServerProfile.COMMON_NETWORKS)
            && !ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD)
            && !ServerDetector.isServerProfileSupported(ServerProfile.CHERRY_PIZZA)
            && !ServerDetector.isServerProfileSupported(ServerProfile.SATURN)
            && !ServerDetector.isServerProfileSupported(ServerProfile.MINEBLAZE_DEXLAND);
    }
}
