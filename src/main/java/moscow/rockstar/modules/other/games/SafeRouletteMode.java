package moscow.rockstar.modules.other.games;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.settings.ModeSetting;

class SafeRouletteMode extends RouletteMode {
    SafeRouletteMode(RussianRoulette owner, ModeSetting setting, String translationKey) {
        super(setting, translationKey);
    }

    @Override
    void applyMode() {
        ClientAccess.minecraftClient.scheduleStop();
    }
}
