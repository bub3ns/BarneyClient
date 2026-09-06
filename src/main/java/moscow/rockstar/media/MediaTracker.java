/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.NativeImage
 *  net.minecraft.NativeImageBackedTexture
 *  net.minecraft.AbstractTexture
 *  net.minecraft.Identifier
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package moscow.rockstar.media;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.media.lyrics.LyricsProviderClient;
import moscow.rockstar.media.lyrics.LyricsTimeline;
import moscow.rockstar.media.music.TrackBpmClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pyrock.utility.render.ColorRGBA;

public class MediaTracker
implements ClientAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"rockstar-lyrics");
    private final Thread mediaPollThread;
    private final AtomicReference<IMediaSession> currentMediaSession = new AtomicReference();
    private volatile ColorRGBA artworkColor = ColorRGBA.WHITE;
    private static final AtomicInteger ARTWORK_TEXTURE_SEQUENCE = new AtomicInteger();
    private volatile Identifier artworkTexture;
    private String artworkTrackKey = "";
    private int lastArtworkHash;
    private int pendingArtworkHash;
    private static final long MEDIA_SESSION_RETRY_MILLIS = 1500L;
    private volatile long lastSessionUpdateMillis;
    private static final int MAX_CACHED_TRACKS = 128;
    private static final Executor lyricsExecutor = MediaTracker.createNamedDaemonExecutor("rockstar-track-lyrics");
    private static final Executor metadataExecutor = MediaTracker.createNamedDaemonExecutor("rockstar-track-metadata");
    private final Map<String, LyricsTimeline> lyricsCache = new ConcurrentHashMap<String, LyricsTimeline>();
    private final Map<String, Float> bpmCache = new ConcurrentHashMap<String, Float>();
    private static final long LYRICS_RETRY_MILLIS = 15000L;
    private static final int MAX_LYRICS_ATTEMPTS = 3;
    private volatile long nextLyricsRetryMillis;
    private volatile int lyricsAttemptCount;
    private volatile boolean lyricsRequestInFlight;
    private final Object mediaStateLock = new Object();
    private volatile LyricsTimeline lyrics = LyricsTimeline.empty();
    private volatile String trackKey = "";
    private volatile float bpm;
    private volatile long playbackPositionSeconds = -1L;
    private volatile long playbackPositionUpdatedMillis;
    private volatile boolean playing;

    private static Executor createNamedDaemonExecutor(String string) {
        return Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, string);
            thread.setDaemon(true);
            thread.setPriority(1);
            return thread;
        });
    }

    public MediaTracker() {
        this.mediaPollThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    long pollStartMillis = System.currentTimeMillis();
                    this.pollMediaSession();
                    long pollIntervalMillis = this.currentMediaSession.get() == null ? 1000L : 100L;
                    long elapsedMillis = System.currentTimeMillis() - pollStartMillis;
                    Thread.sleep(Math.max(0L, pollIntervalMillis - elapsedMillis));
                }
                catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "rockstar-media-poll");
        this.mediaPollThread.setDaemon(true);
        this.mediaPollThread.setPriority(1);
        this.mediaPollThread.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void pollMediaSession() {
        block13: {
            try {
                List<IMediaSession> list = MediaPlayerInfo.INSTANCE.getMediaSessions();
                IMediaSession iMediaSession2 = list.stream().filter(iMediaSession -> !iMediaSession.getMedia().getArtist().isEmpty() && !iMediaSession.getMedia().getTitle().isEmpty()).findFirst().orElse(null);
                if (iMediaSession2 == null && System.currentTimeMillis() - this.lastSessionUpdateMillis < 1500L) {
                    return;
                }
                if (iMediaSession2 != null) {
                    this.lastSessionUpdateMillis = System.currentTimeMillis();
                }
                this.currentMediaSession.set(iMediaSession2);
                if (iMediaSession2 != null) {
                    boolean bl;
                    MediaInfo mediaInfo = iMediaSession2.getMedia();
                    String string = mediaInfo.getArtist();
                    String string2 = mediaInfo.getTitle();
                    String string3 = string + " - " + string2;
                    this.playing = mediaInfo.isPlaying();
                    long l = mediaInfo.getPosition();
                    if (l != this.playbackPositionSeconds) {
                        this.playbackPositionSeconds = l;
                        this.playbackPositionUpdatedMillis = System.currentTimeMillis();
                    }
                    Object object = this.mediaStateLock;
                    synchronized (object) {
                        boolean bl2 = bl = !string3.equals(this.trackKey);
                        if (bl) {
                            this.trackKey = string3;
                            this.lyrics = LyricsTimeline.empty();
                            this.bpm = 0.0f;
                        }
                    }
                    if (!bl && this.lyrics.isEmpty() && !this.lyricsRequestInFlight && this.lyricsAttemptCount < 3 && System.currentTimeMillis() >= this.nextLyricsRetryMillis) {
                        this.requestLyrics(string3, string, string2, mediaInfo.getDuration());
                    }
                    if (bl) {
                        this.lyricsAttemptCount = 0;
                        this.nextLyricsRetryMillis = 0L;
                        if (!this.lyricsRequestInFlight) {
                            this.requestLyrics(string3, string, string2, mediaInfo.getDuration());
                        }
                        metadataExecutor.execute(() -> {
                            if (!string3.equals(this.trackKey)) {
                                return;
                            }
                            Float f = this.bpmCache.get(string3);
                            if (f == null) {
                                f = Float.valueOf(TrackBpmClient.findTrackBpm(string, string2, mediaInfo.getDuration()));
                                if (this.bpmCache.size() > 64) {
                                    this.bpmCache.clear();
                                }
                                this.bpmCache.put(string3, f);
                            }
                            if (f.floatValue() > 0.0f) {
                                Object mediaStateLock = this.mediaStateLock;
                                synchronized (mediaStateLock) {
                                    if (string3.equals(this.trackKey)) {
                                        this.bpm = f.floatValue();
                                    }
                                }
                            }
                        });
                    }
                    this.updateArtwork(string3, mediaInfo.getArtworkPng(), iMediaSession2.getOwner() != null && iMediaSession2.getOwner().toLowerCase(Locale.ROOT).contains("spotify"));
                    break block13;
                }
                this.playing = false;
                this.updateArtwork("", null, false);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private void requestLyrics(String string, String string2, String string3, long l) {
        int n = ++this.lyricsAttemptCount;
        LOGGER.info("[lyrics] \u0441\u0442\u0430\u0432\u043b\u044e \u0432 \u043e\u0447\u0435\u0440\u0435\u0434\u044c \u00ab{}\u00bb, \u043f\u043e\u043f\u044b\u0442\u043a\u0430 {}/{}", new Object[]{string, n, 3});
        this.nextLyricsRetryMillis = System.currentTimeMillis() + 15000L;
        this.lyricsRequestInFlight = true;
        lyricsExecutor.execute(() -> {
            try {
                LyricsTimeline lyricsTimeline;
                if (!string.equals(this.trackKey)) {
                    LOGGER.info("[lyrics] \u00ab{}\u00bb \u043e\u0442\u043c\u0435\u043d\u0451\u043d: \u0438\u0433\u0440\u0430\u0435\u0442 \u0443\u0436\u0435 \u00ab{}\u00bb", (Object)string, (Object)this.trackKey);
                    return;
                }
                LyricsTimeline lyricsTimeline2 = this.lyricsCache.get(string);
                if (lyricsTimeline2 != null) {
                    LOGGER.info("[lyrics] \u00ab{}\u00bb \u0443\u0436\u0435 \u0432 \u043a\u044d\u0448\u0435: {} \u0441\u0442\u0440\u043e\u043a", (Object)string, (Object)lyricsTimeline2.getLines().size());
                }
                LyricsTimeline lyricsTimeline3 = lyricsTimeline = lyricsTimeline2 != null ? lyricsTimeline2 : LyricsProviderClient.fetchLyricsWithDeadline(string2, string3, l);
                if (lyricsTimeline == null) {
                    lyricsTimeline = LyricsTimeline.empty();
                }
                if (lyricsTimeline.isEmpty()) {
                    if (n >= 3) {
                        this.cacheLyrics(string, lyricsTimeline);
                    }
                    return;
                }
                this.cacheLyrics(string, lyricsTimeline);
                Object object = this.mediaStateLock;
                synchronized (object) {
                    if (string.equals(this.trackKey)) {
                        this.lyrics = lyricsTimeline;
                    }
                }
            }
            finally {
                this.lyricsRequestInFlight = false;
            }
        });
    }

    private void cacheLyrics(String string, LyricsTimeline lyricsTimeline) {
        if (this.lyricsCache.size() > 64) {
            this.lyricsCache.clear();
        }
        this.lyricsCache.put(string, lyricsTimeline);
    }

    public Identifier getArtworkTexture() {
        return this.artworkTexture;
    }

    private void updateArtwork(String string, byte[] byArray, boolean bl) {
        int n;
        int n2 = n = byArray == null || byArray.length == 0 ? 0 : Arrays.hashCode(byArray);
        if (n == 0 && string.equals(this.artworkTrackKey)) {
            return;
        }
        if (n == this.lastArtworkHash) {
            this.artworkTrackKey = string;
            return;
        }
        if (n != this.pendingArtworkHash) {
            this.pendingArtworkHash = n;
            return;
        }
        this.artworkTrackKey = string;
        this.lastArtworkHash = n;
        if (n == 0) {
            this.publishArtwork(null, ColorRGBA.WHITE);
            return;
        }
        try {
            NativeImage LootTableData = NativeImage.read((byte[])byArray);
            if (bl) {
                LootTableData = this.cropArtworkForSpotify(LootTableData);
            }
            ColorRGBA colorRGBA = this.sampleArtworkColor(LootTableData, 16);
            this.publishArtwork(this.scaleArtwork(LootTableData, 128), colorRGBA);
        }
        catch (IOException | RuntimeException exception) {
            this.publishArtwork(null, ColorRGBA.WHITE);
        }
    }

    private NativeImage scaleArtwork(NativeImage LootTableData, int n) {
        int n2;
        int n3 = LootTableData.getWidth();
        if (Math.max(n3, n2 = LootTableData.getHeight()) <= n) {
            return LootTableData;
        }
        float f = (float)n / (float)Math.max(n3, n2);
        NativeImage BlockFamilyRecipeFactory = new NativeImage(LootTableData.getFormat(), Math.max(1, Math.round((float)n3 * f)), Math.max(1, Math.round((float)n2 * f)), false);
        LootTableData.resizeSubRectTo(0, 0, n3, n2, BlockFamilyRecipeFactory);
        LootTableData.close();
        return BlockFamilyRecipeFactory;
    }

    private NativeImage cropArtworkForSpotify(NativeImage LootTableData) {
        int n = LootTableData.getWidth();
        int n2 = LootTableData.getHeight();
        int n3 = (int)((double)n * 0.11);
        int n4 = n - n3 * 2;
        int n5 = n2 - (int)((double)n2 * 0.22);
        if (n4 <= 0 || n5 <= 0) {
            return LootTableData;
        }
        NativeImage BlockFamilyRecipeFactory = new NativeImage(LootTableData.getFormat(), n4, n5, false);
        for (int i = 0; i < n5; ++i) {
            for (int j = 0; j < n4; ++j) {
                BlockFamilyRecipeFactory.setColorArgb(j, i, LootTableData.getColorArgb(j + n3, i));
            }
        }
        LootTableData.close();
        return BlockFamilyRecipeFactory;
    }

    private void publishArtwork(NativeImage LootTableData, ColorRGBA colorRGBA) {
        Identifier class_29602 = LootTableData == null ? null : RockstarClient.resourceId("temp/artwork_" + ARTWORK_TEXTURE_SEQUENCE.incrementAndGet());
        minecraftClient.execute(() -> {
            Identifier class_29603 = this.artworkTexture;
            if (LootTableData != null) {
                minecraftClient.getTextureManager().registerTexture(class_29602, (AbstractTexture)new NativeImageBackedTexture(LootTableData));
            }
            this.artworkTexture = class_29602;
            this.artworkColor = colorRGBA;
            if (class_29603 != null) {
                minecraftClient.getTextureManager().destroyTexture(class_29603);
            }
        });
    }

    public ColorRGBA sampleArtworkColor(NativeImage LootTableData, int n) {
        int n2 = LootTableData.getWidth();
        int n3 = LootTableData.getHeight();
        long l = 0L;
        long l2 = 0L;
        long l3 = 0L;
        long l4 = 0L;
        int n4 = 0;
        for (int i = 0; i < n3; i += n) {
            for (int j = 0; j < n2; j += n) {
                int n5 = LootTableData.getColorArgb(j, i);
                int n6 = n5 >> 24 & 0xFF;
                if (n6 == 0) continue;
                l += (long)n6;
                l2 += (long)(n5 >> 16 & 0xFF);
                l3 += (long)(n5 >> 8 & 0xFF);
                l4 += (long)(n5 & 0xFF);
                ++n4;
            }
        }
        if (n4 == 0) {
            return ColorRGBA.WHITE;
        }
        float f = 50.0f;
        return new ColorRGBA((float)l2 / (float)n4 + f, (float)l3 / (float)n4 + f, (float)l4 / (float)n4 + f);
    }

    public double getPlaybackPositionSeconds() {
        if (this.playbackPositionSeconds < 0L) {
            return 0.0;
        }
        if (!this.playing) {
            return this.playbackPositionSeconds;
        }
        long l = System.currentTimeMillis() - this.playbackPositionUpdatedMillis;
        return (double)this.playbackPositionSeconds + (double)Math.min(l, 1000L) / 1000.0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public MediaSnapshot getMediaSnapshot() {
        IMediaSession iMediaSession = this.currentMediaSession.get();
        if (iMediaSession == null) {
            return null;
        }
        try {
            MediaInfo mediaInfo = iMediaSession.getMedia();
            if (mediaInfo == null) {
                return null;
            }
            String string = Objects.requireNonNullElse(mediaInfo.getTitle(), "");
            String string2 = Objects.requireNonNullElse(mediaInfo.getArtist(), "");
            String string3 = string2 + " - " + string;
            String string4 = Objects.requireNonNullElse(iMediaSession.getOwner(), "");
            long l = Math.max(0L, mediaInfo.getDuration());
            Object object = this.mediaStateLock;
            synchronized (object) {
                boolean bl = string3.equals(this.trackKey);
                double d = bl ? this.getPlaybackPositionSeconds() : (double)mediaInfo.getPosition();
                d = Math.max(0.0, l > 0L ? Math.min(d, (double)l) : d);
                return new MediaSnapshot(string, string2, string4, mediaInfo.isPlaying(), d, l, bl ? this.bpm : 0.0f, this.artworkColor, this.artworkTexture, bl ? this.lyrics : LyricsTimeline.empty());
            }
        }
        catch (Exception exception) {
            return null;
        }
    }

    public boolean hasActiveMediaSession() {
        return this.currentMediaSession.get() != null;
    }

    public IMediaSession getMediaSession() {
        return this.currentMediaSession.get();
    }

    public boolean equals(Object object) {
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        MediaTracker mediaTracker = (MediaTracker)object;
        return Objects.equals(this.mediaPollThread, mediaTracker.mediaPollThread) && Objects.equals(this.getMediaSession(), mediaTracker.getMediaSession()) && Objects.equals(this.artworkColor, mediaTracker.artworkColor) && Objects.equals(this.lyrics, mediaTracker.lyrics) && Objects.equals(this.trackKey, mediaTracker.trackKey);
    }

    public int hashCode() {
        return Objects.hash(this.mediaPollThread, this.getMediaSession(), this.artworkColor, this.lyrics, this.trackKey);
    }

    @Generated
    public Thread getMediaPollThread() {
        return this.mediaPollThread;
    }

    @Generated
    public ColorRGBA getArtworkColor() {
        return this.artworkColor;
    }

    @Generated
    public Object getMediaStateLock() {
        return this.mediaStateLock;
    }

    @Generated
    public LyricsTimeline getLyrics() {
        return this.lyrics;
    }

    @Generated
    public String getTrackKey() {
        return this.trackKey;
    }

    @Generated
    public float getBpm() {
        return this.bpm;
    }

    public static final class MediaSnapshot {
        private final String title;
        private final String artist;
        private final String owner;
        private final boolean playing;
        private final double positionSeconds;
        private final long durationSeconds;
        private final float bpm;
        private final ColorRGBA artworkColor;
        private final Identifier artworkTexture;
        private final LyricsTimeline lyrics;

        public MediaSnapshot(String string, String string2, String string3, boolean bl, double d, long l, float f, ColorRGBA colorRGBA, Identifier class_29602, LyricsTimeline lyricsTimeline) {
            this.title = string;
            this.artist = string2;
            this.owner = string3;
            this.playing = bl;
            this.positionSeconds = d;
            this.durationSeconds = l;
            this.bpm = f;
            this.artworkColor = colorRGBA;
            this.artworkTexture = class_29602;
            this.lyrics = lyricsTimeline;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "title", "artist", "owner", "playing", "positionSeconds", "durationSeconds", "bpm", "artworkColor", "artworkTexture", "lyrics");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "title", "artist", "owner", "playing", "positionSeconds", "durationSeconds", "bpm", "artworkColor", "artworkTexture", "lyrics");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "title", "artist", "owner", "playing", "positionSeconds", "durationSeconds", "bpm", "artworkColor", "artworkTexture", "lyrics");
        }

        public String getTitle() {
            return this.title;
        }

        public String getArtist() {
            return this.artist;
        }

        public String getOwner() {
            return this.owner;
        }

        public boolean isPlaying() {
            return this.playing;
        }

        public double getPositionSeconds() {
            return this.positionSeconds;
        }

        public long getDurationSeconds() {
            return this.durationSeconds;
        }

        public float getBpm() {
            return this.bpm;
        }

        public ColorRGBA getArtworkColor() {
            return this.artworkColor;
        }

        public Identifier getArtworkTexture() {
            return this.artworkTexture;
        }

        public LyricsTimeline getLyrics() {
            return this.lyrics;
        }
    }
}
