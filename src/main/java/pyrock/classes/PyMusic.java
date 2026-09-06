/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Identifier
 *  org.jetbrains.annotations.Nullable
 */
package pyrock.classes;

import dev.redstones.mediaplayerinfo.IMediaSession;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.media.MediaTracker;
import moscow.rockstar.media.lyrics.LyricsTimeline;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import pyrock.utility.render.ColorRGBA;

public class PyMusic {
    public boolean active() {
        return PyMusic.snapshot() != null;
    }

    public boolean playing() {
        MediaTracker.MediaSnapshot mediaSnapshot = PyMusic.snapshot();
        return mediaSnapshot != null && mediaSnapshot.isPlaying();
    }

    public double position() {
        MediaTracker.MediaSnapshot mediaSnapshot = PyMusic.snapshot();
        return mediaSnapshot == null ? 0.0 : mediaSnapshot.getPositionSeconds();
    }

    public long positionMs() {
        return Math.round(this.position() * 1000.0);
    }

    public long duration() {
        MediaTracker.MediaSnapshot mediaSnapshot = PyMusic.snapshot();
        return mediaSnapshot == null ? 0L : mediaSnapshot.getDurationSeconds();
    }

    public long durationMs() {
        return this.duration() * 1000L;
    }

    public double progress() {
        MediaTracker.MediaSnapshot mediaSnapshot = PyMusic.snapshot();
        if (mediaSnapshot == null || mediaSnapshot.getDurationSeconds() <= 0L) {
            return 0.0;
        }
        return Math.clamp(mediaSnapshot.getPositionSeconds() / (double)mediaSnapshot.getDurationSeconds(), 0.0, 1.0);
    }

    public float bpm() {
        MediaTracker.MediaSnapshot mediaSnapshot = PyMusic.snapshot();
        return mediaSnapshot == null ? 0.0f : mediaSnapshot.getBpm();
    }

    public boolean lyricsSynced() {
        MediaTracker.MediaSnapshot mediaSnapshot = PyMusic.snapshot();
        return mediaSnapshot != null && mediaSnapshot.getLyrics().hasTiming();
    }

    @Nullable
    public ColorRGBA color() {
        MediaTracker.MediaSnapshot mediaSnapshot = PyMusic.snapshot();
        return mediaSnapshot == null ? null : mediaSnapshot.getArtworkColor();
    }

    @Nullable
    public Identifier artwork() {
        MediaTracker.MediaSnapshot mediaSnapshot = PyMusic.snapshot();
        return mediaSnapshot == null ? null : mediaSnapshot.getArtworkTexture();
    }

    @Nullable
    public Map<String, Object> current() {
        MediaTracker.MediaSnapshot mediaSnapshot = PyMusic.snapshot();
        return mediaSnapshot == null ? null : PyMusic.track(mediaSnapshot);
    }

    public List<Map<String, Object>> lyrics() {
        MediaTracker.MediaSnapshot mediaSnapshot = PyMusic.snapshot();
        if (mediaSnapshot == null) {
            return List.of();
        }
        ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
        for (LyricsTimeline.LyricLine lyricLine : mediaSnapshot.getLyrics().getLines()) {
            arrayList.add(PyMusic.line(lyricLine));
        }
        return arrayList;
    }

    @Nullable
    public Map<String, Object> lyricAt(double d) {
        long l;
        MediaTracker.MediaSnapshot mediaSnapshot = PyMusic.snapshot();
        if (mediaSnapshot == null || !mediaSnapshot.getLyrics().hasTiming()) {
            return null;
        }
        LyricsTimeline lyricsTimeline = mediaSnapshot.getLyrics();
        int n = lyricsTimeline.findLineAtOrBefore(l = Math.max(0L, Math.round(d)));
        if (n < 0) {
            return null;
        }
        Map<String, Object> map = PyMusic.line(lyricsTimeline.getLines().get(n));
        long l2 = mediaSnapshot.getDurationSeconds() * 1000L;
        map.put("index", n);
        map.put("progress", Float.valueOf(lyricsTimeline.getLineRevealProgress(n, l, l2)));
        map.put("singing_progress", Float.valueOf(lyricsTimeline.getCharacterRevealProgress(n, l, l2)));
        map.put("active", n == lyricsTimeline.findLineForPlayback(l, l2, 1000L));
        return map;
    }

    @Nullable
    public Map<String, Object> currentLyric() {
        return this.lyricAt(this.positionMs());
    }

    public boolean play() {
        return PyMusic.control(IMediaSession::play);
    }

    public boolean pause() {
        return PyMusic.control(IMediaSession::pause);
    }

    public boolean toggle() {
        return PyMusic.control(IMediaSession::playPause);
    }

    public boolean next() {
        return PyMusic.control(IMediaSession::next);
    }

    public boolean previous() {
        return PyMusic.control(IMediaSession::previous);
    }

    public boolean stop() {
        return PyMusic.control(IMediaSession::stop);
    }

    private static Map<String, Object> track(MediaTracker.MediaSnapshot mediaSnapshot) {
        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
        linkedHashMap.put("title", mediaSnapshot.getTitle());
        linkedHashMap.put("artist", mediaSnapshot.getArtist());
        linkedHashMap.put("owner", mediaSnapshot.getOwner());
        linkedHashMap.put("playing", mediaSnapshot.isPlaying());
        linkedHashMap.put("position", mediaSnapshot.getPositionSeconds());
        linkedHashMap.put("position_ms", Math.round(mediaSnapshot.getPositionSeconds() * 1000.0));
        linkedHashMap.put("duration", mediaSnapshot.getDurationSeconds());
        linkedHashMap.put("duration_ms", mediaSnapshot.getDurationSeconds() * 1000L);
        linkedHashMap.put("progress", mediaSnapshot.getDurationSeconds() <= 0L ? 0.0 : Math.clamp(mediaSnapshot.getPositionSeconds() / (double)mediaSnapshot.getDurationSeconds(), 0.0, 1.0));
        linkedHashMap.put("bpm", Float.valueOf(mediaSnapshot.getBpm()));
        linkedHashMap.put("color", mediaSnapshot.getArtworkColor());
        linkedHashMap.put("artwork", mediaSnapshot.getArtworkTexture());
        linkedHashMap.put("lyrics_synced", mediaSnapshot.getLyrics().hasTiming());
        linkedHashMap.put("has_lyrics", !mediaSnapshot.getLyrics().isEmpty());
        return linkedHashMap;
    }

    private static Map<String, Object> line(LyricsTimeline.LyricLine lyricLine) {
        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
        ArrayList arrayList = new ArrayList();
        for (LyricsTimeline.LyricCue lyricCue : lyricLine.getCues()) {
            LinkedHashMap<String, Number> linkedHashMap2 = new LinkedHashMap<String, Number>();
            linkedHashMap2.put("time_ms", lyricCue.getTimestampMillis());
            linkedHashMap2.put("char_index", lyricCue.getCharacterIndex());
            arrayList.add(linkedHashMap2);
        }
        linkedHashMap.put("time_ms", lyricLine.getTimestampMillis());
        linkedHashMap.put("text", lyricLine.getText());
        linkedHashMap.put("cues", arrayList);
        return linkedHashMap;
    }

    private static boolean control(Consumer<IMediaSession> consumer) {
        IMediaSession iMediaSession;
        MediaTracker mediaTracker = PyMusic.tracker();
        IMediaSession iMediaSession2 = iMediaSession = mediaTracker == null ? null : mediaTracker.getMediaSession();
        if (iMediaSession == null) {
            return false;
        }
        try {
            consumer.accept(iMediaSession);
            return true;
        }
        catch (Exception exception) {
            return false;
        }
    }

    @Nullable
    private static MediaTracker tracker() {
        return RockstarClient.create().getMediaTracker();
    }

    @Nullable
    private static MediaTracker.MediaSnapshot snapshot() {
        MediaTracker mediaTracker = PyMusic.tracker();
        return mediaTracker == null ? null : mediaTracker.getMediaSnapshot();
    }
}

