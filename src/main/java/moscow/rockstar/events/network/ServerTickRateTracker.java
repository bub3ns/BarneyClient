package moscow.rockstar.events.network;

import java.util.Arrays;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;
import pyrock.events.network.ReceivePacketEvent;

/** Measures the server tick rate from the cadence of world-time updates. */
public final class ServerTickRateTracker {
    private final float[] samples = new float[20];
    private int sampleIndex;
    private long lastUpdateNanos = -1L;

    private final EventListener<ReceivePacketEvent> packetListener = event -> {
        if (!(event.getPacket() instanceof WorldTimeUpdateS2CPacket)) {
            return;
        }
        long now = System.nanoTime();
        if (this.lastUpdateNanos != -1L) {
            float elapsedSeconds = (float)(now - this.lastUpdateNanos) / 1.0E9f;
            this.samples[this.sampleIndex % this.samples.length] = MathHelper.clamp(
                20.0f / elapsedSeconds, 0.0f, 20.0f
            );
            ++this.sampleIndex;
        }
        this.lastUpdateNanos = now;
    };

    public ServerTickRateTracker() {
        Arrays.fill(this.samples, 0.0f);
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public float getTicksPerSecond() {
        float total = 0.0f;
        float count = 0.0f;
        for (float sample : this.samples) {
            if (!(sample > 0.0f)) {
                continue;
            }
            total += sample;
            count += 1.0f;
        }
        return MathHelper.clamp(total / count, 0.0f, 20.0f);
    }
}
