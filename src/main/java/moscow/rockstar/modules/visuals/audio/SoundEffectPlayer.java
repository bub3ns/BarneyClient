package moscow.rockstar.modules.visuals.audio;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/** Plays the client-owned sound events declared in assets/rockstar/sounds.json. */
public final class SoundEffectPlayer {
    private static final String NAMESPACE = "rockstar";

    private SoundEffectPlayer() {
    }

    public static void playMenuOpen(float volume) {
        play("menu", volume, 1.0f);
    }

    public static void playClickGuiOpen(float volume) {
        play("clickgui_open", volume, 1.0f);
    }

    public static void playToggle(float volume, float pitch) {
        play("toggle", volume, pitch);
    }

    public static void playPurchase(float volume) {
        play("applepay", volume, 1.0f);
    }

    public static void playNotification(float volume) {
        play("notification", volume, 1.0f);
    }

    public static void playTotem(float volume) {
        play("totem.totem-totem", volume, 1.0f);
    }

    public static void playKill(float volume) {
        play("kill.easy", volume, 1.0f);
    }

    public static void playDeath(float volume) {
        play("death.blin", volume, 1.0f);
    }

    private static void play(String eventName, float volume, float pitch) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        client.getSoundManager().play(PositionedSoundInstance.master(
                SoundEvent.of(Identifier.of(NAMESPACE, eventName)), volume, pitch));
    }
}
