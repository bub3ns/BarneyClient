/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.media.lyrics;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;

public class LyricsTimeline {
    private static final Pattern TIMED_LINE_PATTERN = Pattern.compile("\\[(\\d{1,3}):(\\d{2})[.,](\\d{2,3})]");
    private static final Pattern CUE_MARKER_PATTERN = Pattern.compile("<(\\d{1,3}):(\\d{2})[.,](\\d{2,3})>");
    private final List<LyricLine> lines;
    private final boolean hasTiming;

    private LyricsTimeline(List<LyricLine> list, boolean bl) {
        this.lines = Collections.unmodifiableList(list);
        this.hasTiming = bl;
    }

    public static LyricsTimeline parseTimedLyrics(String string) {
        if (string == null || string.isBlank()) {
            return LyricsTimeline.empty();
        }
        ArrayList<LyricLine> arrayList = new ArrayList<LyricLine>();
        for (String string2 : string.split("\n")) {
            String string3 = string2.trim();
            Matcher matcher = TIMED_LINE_PATTERN.matcher(string3);
            ArrayList<Long> arrayList2 = new ArrayList<Long>();
            int n = 0;
            while (matcher.find() && matcher.start() == n) {
                arrayList2.add(LyricsTimeline.parseTimestamp(matcher));
                n = matcher.end();
            }
            if (arrayList2.isEmpty()) continue;
            String string4 = string3.substring(n).strip();
            Matcher matcher2 = CUE_MARKER_PATTERN.matcher(string4);
            ArrayList<LyricCue> arrayList3 = new ArrayList<LyricCue>();
            StringBuilder stringBuilder = new StringBuilder();
            int n2 = 0;
            while (matcher2.find()) {
                stringBuilder.append(string4, n2, matcher2.start());
                arrayList3.add(new LyricCue(LyricsTimeline.parseTimestamp(matcher2), Character.codePointCount(stringBuilder, 0, stringBuilder.length())));
                n2 = matcher2.end();
            }
            stringBuilder.append(string4, n2, string4.length());
            String string5 = stringBuilder.toString().strip();
            if (string5.isEmpty()) continue;
            Iterator iterator = arrayList2.iterator();
            while (iterator.hasNext()) {
                long l = (Long)iterator.next();
                arrayList.add(new LyricLine(l, string5, arrayList3));
            }
        }
        Collections.sort(arrayList);
        return new LyricsTimeline(arrayList, true);
    }

    public static LyricsTimeline parsePlainLyrics(String string) {
        if (string == null || string.isBlank()) {
            return LyricsTimeline.empty();
        }
        ArrayList<LyricLine> arrayList = new ArrayList<LyricLine>();
        for (String string2 : string.split("\n")) {
            String string3 = string2.trim();
            if (string3.isEmpty()) continue;
            arrayList.add(new LyricLine(-1L, string3, List.of()));
        }
        return new LyricsTimeline(arrayList, false);
    }

    public static LyricsTimeline empty() {
        return new LyricsTimeline(List.of(), false);
    }

    public boolean isEmpty() {
        return this.lines.isEmpty();
    }

    public int findLineAtOrBefore(long l) {
        if (!this.hasTiming || this.lines.isEmpty()) {
            return -1;
        }
        int n = 0;
        int n2 = this.lines.size() - 1;
        int n3 = -1;
        while (n <= n2) {
            int n4 = n + n2 >>> 1;
            if (this.lines.get(n4).getTimestampMillis() <= l) {
                n3 = n4;
                n = n4 + 1;
                continue;
            }
            n2 = n4 - 1;
        }
        return n3;
    }

    public int findLineForPlayback(double d, long l, long l2) {
        int n = this.findLineAtOrBefore((long)d);
        if (n < 0 || LyricsTimeline.isPunctuationOnly(this.lines.get(n).getText())) {
            return -1;
        }
        long l3 = this.getLineEndTime(n, l);
        if (d <= (double)l3) {
            return n;
        }
        long l4 = this.findNextLineTimestamp(n);
        if (l4 != Long.MAX_VALUE && l4 - l3 <= l2 * 2L) {
            return n;
        }
        return d - (double)l3 <= (double)l2 ? n : -1;
    }

    public float getLineRevealProgress(int n, double d, long l) {
        if (!this.hasTiming || n < 0 || n >= this.lines.size()) {
            return 0.0f;
        }
        return this.interpolateLineProgress(n, d, this.getLineEndTimestamp(n, l));
    }

    public float getCharacterRevealProgress(int n, double d, long l) {
        if (!this.hasTiming || n < 0 || n >= this.lines.size()) {
            return 0.0f;
        }
        return this.interpolateLineProgress(n, d, this.getLineEndTime(n, l));
    }

    private float interpolateLineProgress(int n, double d, long l) {
        LyricLine lyricLine = this.lines.get(n);
        long l2 = lyricLine.getTimestampMillis();
        if (d <= (double)l2) {
            return 0.0f;
        }
        if (d >= (double)l) {
            return 1.0f;
        }
        if (lyricLine.getCues().isEmpty()) {
            return (float)((d - (double)l2) / ((double)l - (double)l2));
        }
        long l3 = l2;
        int n2 = 0;
        int n3 = lyricLine.getText().codePointCount(0, lyricLine.getText().length());
        for (LyricCue lyricCue : lyricLine.getCues()) {
            if ((double)lyricCue.getTimestampMillis() > d) {
                return LyricsTimeline.interpolateCueProgress(n3, d, l3, lyricCue.getTimestampMillis(), n2, lyricCue.getCharacterIndex());
            }
            l3 = Math.max(l2, lyricCue.getTimestampMillis());
            n2 = Math.clamp((long)lyricCue.getCharacterIndex(), 0, n3);
        }
        return LyricsTimeline.interpolateCueProgress(n3, d, l3, l, n2, n3);
    }

    private long getLineEndTimestamp(int n, long l) {
        long l2 = this.lines.get(n).getTimestampMillis();
        long l3 = this.findNextLineTimestamp(n);
        if (l3 != Long.MAX_VALUE) {
            return l3;
        }
        long l4 = l2 + this.getLineDurationMillis(n);
        return l > l2 ? Math.min(l, l4) : l4;
    }

    private long getLineEndTime(int n, long l) {
        long l2 = this.lines.get(n).getTimestampMillis();
        long l3 = Math.min(this.findNextLineTimestamp(n), l2 + this.getLineDurationMillis(n));
        return l > l2 ? Math.min(l, l3) : l3;
    }

    private long findNextLineTimestamp(int n) {
        long l = this.lines.get(n).getTimestampMillis();
        for (int i = n + 1; i < this.lines.size(); ++i) {
            if (this.lines.get(i).getTimestampMillis() <= l) continue;
            return this.lines.get(i).getTimestampMillis();
        }
        return Long.MAX_VALUE;
    }

    private long getLineDurationMillis(int n) {
        String string = this.lines.get(n).getText();
        return Math.clamp((long)string.codePointCount(0, string.length()) * 150L, 1500L, 8000L);
    }

    private static boolean isPunctuationOnly(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (!Character.isLetterOrDigit(string.charAt(i))) continue;
            return false;
        }
        return true;
    }

    private static float interpolateCueProgress(int n, double d, long l, long l2, int n2, int n3) {
        if (l2 <= l || n == 0) {
            return (float)n3 / (float)Math.max(1, n);
        }
        double d2 = Math.clamp((d - (double)l) / ((double)l2 - (double)l), 0.0, 1.0);
        return (float)(((double)n2 + (double)(n3 - n2) * d2) / (double)n);
    }

    private static long parseTimestamp(Matcher matcher) {
        int n = Integer.parseInt(matcher.group(3));
        if (matcher.group(3).length() == 2) {
            n *= 10;
        }
        return (long)Integer.parseInt(matcher.group(1)) * 60000L + (long)Integer.parseInt(matcher.group(2)) * 1000L + (long)n;
    }

    @Generated
    public List<LyricLine> getLines() {
        return this.lines;
    }

    @Generated
    public boolean hasTiming() {
        return this.hasTiming;
    }

    public static final class LyricCue {
        private final long timestampMillis;
        private final int characterIndex;

        public LyricCue(long l, int n) {
            this.timestampMillis = l;
            this.characterIndex = n;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "timestampMillis", "characterIndex");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "timestampMillis", "characterIndex");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "timestampMillis", "characterIndex");
        }

        public long getTimestampMillis() {
            return this.timestampMillis;
        }

        public int getCharacterIndex() {
            return this.characterIndex;
        }
    }

    public static final class LyricLine
    implements Comparable<LyricLine> {
        private final long timestampMillis;
        private final String text;
        private final List<LyricCue> cues;

        public LyricLine(long l, String string, List<LyricCue> list) {
            list = List.copyOf(list);
            this.timestampMillis = l;
            this.text = string;
            this.cues = list;
        }

        public int compareByTimestamp(LyricLine lyricLine) {
            return Long.compare(this.timestampMillis, lyricLine.timestampMillis);
        }

        @Override
        public int compareTo(LyricLine other) {
            return this.compareByTimestamp(other);
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "timestampMillis", "text", "cues");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "timestampMillis", "text", "cues");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "timestampMillis", "text", "cues");
        }

        public long getTimestampMillis() {
            return this.timestampMillis;
        }

        public String getText() {
            return this.text;
        }

        public List<LyricCue> getCues() {
            return this.cues;
        }

    }
}

