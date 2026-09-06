package moscow.rockstar.modules.other.games;

import moscow.rockstar.settings.ModeSetting;

abstract class RouletteMode extends ModeSetting.Option {
    protected RouletteMode(ModeSetting setting, String translationKey) {
        super(setting, translationKey);
    }

    abstract void applyMode();
}
