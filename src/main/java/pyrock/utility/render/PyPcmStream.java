/*
 * Decompiled with CFR 0.152.
 */
package pyrock.utility.render;

import java.util.concurrent.ArrayBlockingQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public final class PyPcmStream
implements AutoCloseable {
    private final ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue(4);
    private final SourceDataLine line;
    private final Thread worker;
    private volatile boolean running = true;

    public PyPcmStream(float f, int n) {
        try {
            AudioFormat audioFormat = new AudioFormat(f, 16, n, true, false);
            this.line = (SourceDataLine)AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, audioFormat));
            int n2 = Math.max(8192, (int)f * n / 2);
            n2 -= n2 % audioFormat.getFrameSize();
            this.line.open(audioFormat, n2);
            this.line.start();
        }
        catch (Exception exception) {
            throw new IllegalStateException("failed to open PCM output", exception);
        }
        this.worker = new Thread(this::run, "Barney-Script-PCM");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    public void submit(Object object) {
        if (!this.running) {
            return;
        }
        if (!(object instanceof byte[])) {
            throw new IllegalArgumentException("PCM samples must be bytes");
        }
        byte[] byArray = (byte[])object;
        byte[] byArray2 = (byte[])byArray.clone();
        if (!this.queue.offer(byArray2)) {
            this.queue.poll();
            this.queue.offer(byArray2);
        }
    }

    private void run() {
        try {
            while (this.running) {
                byte[] byArray = this.queue.take();
                this.line.write(byArray, 0, byArray.length);
            }
        }
        catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
        finally {
            this.line.stop();
            this.line.flush();
            this.line.close();
        }
    }

    public void restoreEnabledFlag() {
        if (!this.running) {
            return;
        }
        this.running = false;
        this.worker.interrupt();
    }

    @Override
    public void close() {
        this.restoreEnabledFlag();
    }
}
