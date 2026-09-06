package moscow.rockstar.modules.visuals.hud;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.media.MediaTracker;
import moscow.rockstar.media.lyrics.LyricsTimeline;
import moscow.rockstar.modules.config.ModuleConfigurationStore;
import moscow.rockstar.modules.visuals.esp.entities.NametagTextUtils;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.state.UiScissorStack;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.hud.DynamicIslandExpandableEntry;
import moscow.rockstar.ui.hud.DynamicIslandManager;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextComponent;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.Identifier;
import pyrock.events.window.MouseScrollEvent;
import pyrock.utility.render.ColorRGBA;

/**
 * The music / lyrics island status. 1:1 with {@code rockstar/ilIlil/IiiIIIIIi}
 * (extends {@code IiIiiIIii} = {@link DynamicIslandExpandableEntry}, implements
 * {@code iIIiIIiIi} = {@link ClientAccess}), entry [7] of
 * {@code rockstar/ilIlil/IiIiiiIIi#I(Lrockstar/ilIlil/IIiiiiiii;)V}.
 *
 * <p>Every member below was pinned by disassembly, not by declaration order - CFR collapses this
 * class's duplicate member names ({@code I}, {@code i}, {@code II}, ...) into nonsense. The obf
 * name of each ported member is recorded in its javadoc so the port can be re-verified.</p>
 *
 * <h2>Deliberate substitutions</h2>
 * <ul>
 *   <li>The original's {@code canShow()} hook is expressed as {@code isVisible()}, exactly as
 *       {@link IslandModulesStatus} does - see {@code DynamicIslandEntry}'s class javadoc.</li>
 *   <li>{@code Ii.I().I().I()} (the HUD manager's island field) is
 *       {@link DynamicIslandManager#island()}, and {@code Ii.I().I().i("client")} is
 *       {@link ModuleConfigurationStore#saveConfiguration()} - the same two mappings
 *       {@link DynamicIslandHud} already uses.</li>
 *   <li>{@code iIIIIiIiI.i(String)} (the shared small-caps normaliser) is
 *       {@link NametagTextUtils#smallCaps(String)}, the only remapped copy of that helper.</li>
 *   <li>{@code iIiiiIIiI} (the matrix push/rotate/scale helper) is {@link ItemRenderUtils} and
 *       {@code iIiiiIiII} (the scissor helper) is {@link UiScissorStack}.</li>
 * </ul>
 */
public class IslandMusicStatus extends DynamicIslandExpandableEntry implements ClientAccess {
    /** ORIGINAL: the static {@code I:F} .. {@code iiI:F} block, in declaration order. */
    private static final float BAR_RADIUS = 0.5f;
    private static final float EXPANDED_WIDTH = 164.0f;
    private static final float COLLAPSED_HEIGHT = 80.0f;
    private static final float LYRICS_HEIGHT = 125.0f;
    private static final float COMPACT_HEIGHT = 15.0f;
    private static final float LYRICS_WIDTH = 144.0f;
    private static final float TEXT_INSET = 32.0f;
    private static final float TRANSPORT_THRESHOLD = 0.7f;
    private static final float BUTTON_SIZE = 16.0f;
    private static final float SMALL_BUTTON_SIZE = 8.0f;
    private static final float BAR_IDLE = 3.0f;
    /** ORIGINAL: the static {@code I:Ljava/lang/String;}. */
    private static final String LYRICS_SIGNAL = "lyrics-expansion";
    private static final float TITLE_LIMIT = 95.0f;
    private static final float BAR_BOX = 7.0f;
    /** ORIGINAL: the static {@code I:J} - the hold window handed to findLineForPlayback. */
    private static final long LINE_WINDOW = 3000L;
    /** ORIGINAL: the static {@code I:[F}, {@code i:[F} and {@code II:[F}. */
    private static final float[] BAR_FREQ_A = new float[]{1.0f, 2.0f, 0.5f, 1.5f};
    private static final float[] BAR_FREQ_B = new float[]{3.0f, 1.5f, 2.5f, 4.0f};
    private static final float[] BAR_PHASE = new float[]{0.0f, 1.9f, 4.2f, 2.7f};

    /** ORIGINAL: {@code Ii:[F} - the four BPM bar levels. */
    final float[] bars;
    /** ORIGINAL: {@code i:J} - the last bar-integration timestamp. */
    private long lastBarTime;
    /** ORIGINAL: {@code I:IiiiIiIii} - the play/pause cross-fade (300ms, easeOutOvershootSoft). */
    final Animation playPauseAnim;
    /** ORIGINAL: {@code i:IiiiIiIii} - the BPM readout fade (600ms, easeInOutCubicBezier). */
    final Animation bpmAnim;
    /** ORIGINAL: {@code iii:F} - the last non-zero BPM. */
    float bpm;
    /** ORIGINAL: {@code I:I} - the manual lyrics scroll index. */
    int scrollIndex;
    /** ORIGINAL: {@code IIII:F} - the optimistic play state, -1 when not pending. */
    float pendingPlayState;
    /** ORIGINAL: {@code II:J} - when the optimistic play state expires. */
    long pendingUntil;
    /** ORIGINAL: {@code IIIi:F} - the lyric-scroller alpha. */
    float lyricsAlpha;
    /** ORIGINAL: {@code I:Lrockstar/ilIlil/iii;} - the lazily built content root. */
    private Component content;
    /** ORIGINAL: {@code II:IiiiIiIii} - the karaoke-overlay fade (260ms, easeInOutCubicBezier). */
    private final Animation karaokeAnim;
    /** ORIGINAL: {@code i:I} - the karaoke line index, -1 when none. */
    int karaokeLine;
    /** ORIGINAL: {@code i:Ljava/lang/String;} - the karaoke line text. */
    String karaokeText;
    /** ORIGINAL: {@code IIiI:F} - the karaoke character-reveal progress. */
    float karaokeProgress;
    /** ORIGINAL: {@code I:Lrockstar/ilIlil/IiIIIiII;} - the MouseScrollEvent listener. */
    private final EventListener<MouseScrollEvent> scrollListener;

    /** ORIGINAL: {@code IiiIIIIIi(IIiiiiiii)}; the field stores are in this exact order. */
    public IslandMusicStatus(MultiBooleanSetting statuses) {
        super(statuses, "music");
        this.bars = new float[4];
        this.lastBarTime = System.currentTimeMillis();
        this.playPauseAnim = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
        this.bpmAnim = new Animation(600L, 0.0f, Easing.easeInOutCubicBezier);
        this.scrollIndex = 0;
        this.pendingPlayState = -1.0f;
        this.karaokeAnim = new Animation(260L, 0.0f, Easing.easeInOutCubicBezier);
        this.karaokeLine = -1;
        this.karaokeText = "";
        this.scrollListener = this::onMouseScroll;
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    /** ORIGINAL: {@code prepare(IiIiiIIII)V}. */
    @Override
    public void prepare(DynamicIslandHud island) {
        this.updateKaraokeLine();
    }

    /** ORIGINAL: {@code I(IiIiiIIII)Lrockstar/ilIlil/iii;} (bridged by {@code content}). */
    @Override
    public UiNode content(DynamicIslandHud island) {
        if (this.content == null) {
            this.content = new MusicContent(island);
        }
        return this.content;
    }

    /** ORIGINAL: {@code I()Lnet/minecraft/class_2960;}. */
    public Identifier artworkTexture() {
        Identifier texture = this.tracker().getArtworkTexture();
        return texture != null ? texture : RockstarClient.resourceId("icons/music/no_image.png");
    }

    /** ORIGINAL: {@code I()Ljava/lang/String;} - the raw BPM number, or "--". */
    public String bpmValueText() {
        float value = this.tracker().getBpm();
        return value > 0.0f ? String.valueOf(Math.round(value)) : "--";
    }

    /** ORIGINAL: {@code I()Lpyrock/utility/render/ColorRGBA;} - the album-art accent. */
    public ColorRGBA artworkColor() {
        return this.tracker().getArtworkColor();
    }

    /** ORIGINAL: {@code I()[F} - integrates the bars, then hands out the live array. */
    public float[] barLevels() {
        this.updateBars();
        return this.bars;
    }

    /** ORIGINAL: {@code I(BooleanSupplier,Runnable,IiiIIIIIi$II)LIiiIIIIIi$IIi;}. */
    TransportButton button(BooleanSupplier gate, Runnable action, ButtonPainter painter) {
        return this.button(16.0f, gate, action, painter);
    }

    /** ORIGINAL: {@code I(F,BooleanSupplier,Runnable,IiiIIIIIi$II)LIiiIIIIIi$IIi;}. */
    TransportButton button(float size, BooleanSupplier gate, Runnable action, ButtonPainter painter) {
        TransportButton node = new TransportButton(gate);
        node.size(size, size);
        node.onClick(action);
        node.paint((drawContext, self) -> painter.paint(drawContext, node));
        return node;
    }

    /** ORIGINAL: {@code canShow()Z}; the remap expresses that hook as {@code isVisible()}. */
    @Override
    public boolean isVisible() {
        IMediaSession session = this.session();
        if (session == null) {
            return false;
        }
        String owner = session.getOwner();
        return owner == null || !owner.toLowerCase(Locale.ROOT).contains("gram");
    }

    /** ORIGINAL: {@code getColor()}; {@code IiIIIiiIi.Iii()F} serialises as "albomColor". */
    @Override
    public ColorRGBA getColor() {
        return ColorPalette.getThemeColorSettings().getGlassSaturation() == 1.0f
                ? super.getColor().mix(this.tracker().getArtworkColor(), 0.2f)
                : super.getColor();
    }

    /** ORIGINAL: {@code I()Lrockstar/ilIlil/iiIiIIIiI;}. */
    MediaTracker tracker() {
        return RockstarClient.create().getMediaTracker();
    }

    /** ORIGINAL: {@code I()Ldev/redstones/mediaplayerinfo/IMediaSession;}. */
    IMediaSession session() {
        MediaTracker tracker = this.tracker();
        if (!tracker.hasActiveMediaSession()) {
            return null;
        }
        return tracker.getMediaSession();
    }

    /** ORIGINAL: {@code I()Ldev/redstones/mediaplayerinfo/MediaInfo;}. */
    MediaInfo media() {
        IMediaSession session = this.session();
        return session == null ? null : session.getMedia();
    }

    /** ORIGINAL: {@code I(Ljava/util/function/Function;)Ljava/lang/String;}. */
    String text(Function<MediaInfo, String> accessor) {
        MediaInfo media = this.media();
        if (media == null) {
            return "";
        }
        String value = accessor.apply(media);
        return value == null ? "" : value;
    }

    /** ORIGINAL: {@code I(Ldev/redstones/mediaplayerinfo/MediaInfo;)F}. */
    float contentWidth(MediaInfo media) {
        float limit = this.karaokeLine >= 0 ? 127.0f : 144.0f;
        return Math.max(48.0f, Math.min(this.measuredWidth(media), limit));
    }

    /** ORIGINAL: the private {@code i(Ldev/redstones/mediaplayerinfo/MediaInfo;)F}. */
    private float measuredWidth(MediaInfo media) {
        if (this.karaokeLine >= 0) {
            return 32.0f + Font.MEDIUM.metrics(7.0f).measureText(this.karaokeText) + 6.0f;
        }
        String title = media.getTitle();
        return 32.0f + Font.MEDIUM.metrics(7.0f).measureText(title == null ? "" : title);
    }

    /** ORIGINAL: the private {@code I()V} - resolves the karaoke line for the current position. */
    private void updateKaraokeLine() {
        MediaInfo media = this.media();
        LyricsTimeline lyrics = this.tracker().getLyrics();
        int line = -1;
        if (media != null && media.isPlaying() && lyrics.hasTiming() && this.lyricsEnabled()) {
            double position = this.tracker().getPlaybackPositionSeconds() * 1000.0 + 150.0;
            long duration = media.getDuration() * 1000L;
            line = lyrics.findLineForPlayback(position, duration, LINE_WINDOW);
            if (line >= 0) {
                this.karaokeText = lyrics.getLines().get(line).getText();
                this.karaokeProgress = lyrics.getCharacterRevealProgress(line, position, duration);
            }
        }
        this.karaokeLine = line;
        this.karaokeAnim.setReverse(line >= 0);
    }

    /** ORIGINAL: {@code I()F} - the karaoke overlay alpha; it fades out as the island expands. */
    float karaokeAlpha() {
        return this.karaokeAnim.getValue() * (1.0f - this.islandExpansion());
    }

    /** ORIGINAL: {@code i()F}. */
    float expandedHeight() {
        return this.lyricsVisible() ? 125.0f : 80.0f;
    }

    /** ORIGINAL: {@code I()Z} - the track has lyrics at all. */
    boolean hasLyrics() {
        return !this.tracker().getLyrics().isEmpty();
    }

    /** ORIGINAL: {@code i()Z} - the island's "song_lyrics" setting (true when there is no island). */
    boolean lyricsEnabled() {
        DynamicIslandHud island = DynamicIslandManager.island();
        return island == null || island.getSongLyrics().isEnabled();
    }

    /** ORIGINAL: {@code I(Z)V}. */
    void setLyricsEnabled(boolean enabled) {
        DynamicIslandHud island = DynamicIslandManager.island();
        if (island == null) {
            return;
        }
        island.getSongLyrics().setValueInternal(enabled);
        if (enabled) {
            this.scrollIndex = 0;
        }
        ModuleConfigurationStore.saveConfiguration();
    }

    /** ORIGINAL: {@code II()Z}. */
    boolean lyricsVisible() {
        return this.lyricsEnabled() && this.hasLyrics();
    }

    /** ORIGINAL: {@code II()F} - the island's expansion animation. */
    float islandExpansion() {
        DynamicIslandHud island = DynamicIslandManager.island();
        return island == null ? 0.0f : island.getExpandAnim().getValue();
    }

    /** ORIGINAL: {@code Ii()F}. */
    float lyricsAlpha() {
        return this.lyricsAlpha;
    }

    /** ORIGINAL: {@code i()Ljava/lang/String;} - the media-source badge key, or null. */
    String sourceKey() {
        IMediaSession session = this.session();
        if (session == null || session.getOwner() == null) {
            return null;
        }
        String owner = session.getOwner().toLowerCase(Locale.ROOT);
        if (owner.contains("yandex") || owner.contains("\u044f\u043d\u0434\u0435\u043a\u0441")) {
            return "yandex_music";
        }
        if (owner.contains("edge")) {
            return "edge";
        }
        if (owner.contains("spotify")) {
            return "spotify";
        }
        return null;
    }

    /** ORIGINAL: {@code II()Ljava/lang/String;}. */
    String bpmText() {
        return Math.round(this.bpm * this.bpmAnim.getValue()) + " BPM";
    }

    /** ORIGINAL: {@code iI()F} - the on-beat pulse, (1 - beatFraction)^4. */
    float beatPulse() {
        MediaInfo media = this.media();
        if (media == null || !media.isPlaying()) {
            return 0.0f;
        }
        float measured = this.tracker().getBpm();
        float tempo = measured > 0.0f ? measured : 120.0f;
        double beats = this.tracker().getPlaybackPositionSeconds() * (double)tempo / 60.0;
        return (float)Math.pow(1.0 - (beats - Math.floor(beats)), 4.0);
    }

    /** ORIGINAL: {@code i()V} - integrates the four BPM bars. */
    void updateBars() {
        MediaInfo media = this.media();
        if (media == null) {
            return;
        }
        float measured = this.tracker().getBpm();
        float tempo = measured > 0.0f ? measured : 120.0f;
        double beats = this.tracker().getPlaybackPositionSeconds() * (double)tempo / 60.0;
        long now = System.currentTimeMillis();
        float delta = Math.min((float)(now - this.lastBarTime) / 1000.0f, 0.05f);
        this.lastBarTime = now;
        for (int i = 0; i < this.bars.length; ++i) {
            float target;
            if (media.isPlaying()) {
                double phase = beats - (double)i * 0.05;
                float fraction = (float)(phase - Math.floor(phase));
                float attack = (float)Math.pow(1.0f - fraction, 6.0) * 4.0f;
                float halfBeat = fraction >= 0.5f
                        ? (float)Math.pow(1.0f - (fraction - 0.5f) * 2.0f, 6.0) * 1.6f
                        : 0.0f;
                float wave = (float)(Math.abs(MathUtils.lookupSine(phase * Math.PI * (double)BAR_FREQ_A[i]
                                + (double)BAR_PHASE[i])) * 0.65
                        + Math.abs(MathUtils.lookupSine(phase * Math.PI * (double)BAR_FREQ_B[i]
                                + (double)BAR_PHASE[i] * 1.7)) * 0.35);
                float swell = 0.5f + 0.5f
                        * (float)MathUtils.lookupSine(phase * Math.PI * 0.25 + (double)i * 2.1);
                target = Math.min(1.2f + wave * (2.2f + 2.6f * swell)
                        + (attack + halfBeat) * (0.7f + 0.3f * swell), 10.0f);
            } else {
                target = 3.0f;
            }
            float rate = target > this.bars[i] ? 45.0f : 9.0f;
            this.bars[i] = this.bars[i] + (target - this.bars[i]) * Math.min(1.0f, delta * rate);
        }
    }

    /** ORIGINAL: the private synthetic {@code I(Lpyrock/events/window/MouseScrollEvent;)V}. */
    private void onMouseScroll(MouseScrollEvent event) {
        if (!this.lyricsEnabled()) {
            return;
        }
        DynamicIslandHud island = DynamicIslandManager.island();
        if (island == null || island.active() != this || !island.isExpanded()) {
            return;
        }
        Vector2f mouse = UiUtils.mousePosition();
        if (!UiUtils.contains(island.getX(), island.getY(), island.getWidth(), island.getHeight(),
                mouse.getX(), mouse.getY())) {
            return;
        }
        LyricsTimeline lyrics = this.tracker().getLyrics();
        if (lyrics.hasTiming()) {
            return;
        }
        if (event.getVerticalAmount() < 0.0) {
            ++this.scrollIndex;
        } else if (event.getVerticalAmount() > 0.0) {
            --this.scrollIndex;
        } else {
            return;
        }
        int max = Math.max(0, lyrics.getLines().size() - 6);
        this.scrollIndex = Math.clamp((long)this.scrollIndex, 0, max);
    }

    /** ORIGINAL: the static {@code I(FFF)F} - a clamped easeOutCubic ramp between two edges. */
    static float smoothstep(float value, float low, float high) {
        if (high <= low) {
            return value >= high ? 1.0f : 0.0f;
        }
        float t = IslandMusicStatus.clamp01((value - low) / (high - low));
        return Easing.easeOutCubic.ease(t, 0.0f, 1.0f, 1.0f);
    }

    /** ORIGINAL: the static {@code I(F)F}. */
    static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    /** ORIGINAL: the static {@code i(FFF)F}. */
    static float lerp(float from, float to, float t) {
        return from + (to - from) * IslandMusicStatus.clamp01(t);
    }

    /** ORIGINAL: the public static {@code I(J)Ljava/lang/String;}. */
    public static String formatTime(long seconds) {
        long minutes = seconds / 60L;
        long remainder = seconds % 60L;
        return String.format("%d:%02d", minutes, remainder);
    }

    public Animation getPlayPauseAnim() {
        return this.playPauseAnim;
    }

    public Animation getBpmAnim() {
        return this.bpmAnim;
    }

    public EventListener<MouseScrollEvent> getScrollListener() {
        return this.scrollListener;
    }

    /** ORIGINAL: the static interface {@code IiiIIIIIi$II}. */
    interface ButtonPainter {
        void paint(RockstarDrawContext drawContext, TransportButton node);
    }

    /**
     * ORIGINAL: {@code IiiIIIIIi$IiI} (final, extends {@code iii}) - the layout root. It slots all
     * fifteen children by hand every tick and every frame; there is no flex layout here.
     */
    final class MusicContent extends Component {
        private final AlbumArtNode albumArt;
        private final MarqueeTextNode title;
        private final KaraokeLineNode karaoke;
        private final MarqueeTextNode artist;
        private final SmallTextNode position;
        private final SmallTextNode duration;
        private final ProgressBarNode progress;
        private final TransportButton previous;
        private final TransportButton play;
        private final TransportButton next;
        private final SourceBadgeNode source;
        private final BpmTextNode bpmLabel;
        private final TransportButton lyricsToggle;
        private final LyricsScrollerNode lyrics;
        private final BpmBarsNode bpmBars;
        private final DynamicIslandHud island;

        MusicContent(DynamicIslandHud island) {
            this.albumArt = new AlbumArtNode();
            this.title = new MarqueeTextNode(Font.MEDIUM.metrics(7.0f), this::titleText, this::titleColor,
                    0.3f, 0.7f);
            this.karaoke = new KaraokeLineNode();
            this.artist = new MarqueeTextNode(Font.REGULAR.metrics(7.0f), this::artistText, this::artistColor,
                    0.3f, 0.7f);
            this.position = new SmallTextNode(Font.REGULAR.metrics(5.0f), this::positionText,
                    () -> ColorPalette.getPrimaryTextColor().withAlpha(255.0f));
            this.duration = new SmallTextNode(Font.REGULAR.metrics(5.0f), this::durationText,
                    () -> ColorPalette.getPrimaryTextColor().withAlpha(255.0f));
            this.progress = new ProgressBarNode();
            this.previous = IslandMusicStatus.this.button(this::transportEnabled, this::previousTrack,
                    (drawContext, node) -> drawContext.drawIcon("music/previous", node.x(), node.y(), node.w(),
                            ColorPalette.getPrimaryTextColor().withAlpha(255.0f - 100.0f * node.hover())));
            this.play = IslandMusicStatus.this.button(this::transportEnabled, this::togglePlay,
                    this::paintPlayPause);
            this.next = IslandMusicStatus.this.button(this::transportEnabled, this::nextTrack,
                    (drawContext, node) -> drawContext.drawIcon("music/next", node.x(), node.y(), node.w(),
                            ColorPalette.getPrimaryTextColor().withAlpha(255.0f - 100.0f * node.hover())));
            this.source = new SourceBadgeNode();
            this.bpmLabel = new BpmTextNode();
            this.lyricsToggle = IslandMusicStatus.this.button(8.0f, this::lyricsToggleEnabled,
                    this::toggleLyrics,
                    (drawContext, node) -> drawContext.drawIcon("music/text", node.x(), node.y(), node.w(),
                            ColorPalette.getPrimaryTextColor().withAlpha(255.0f - 100.0f * node.hover())));
            this.lyrics = new LyricsScrollerNode();
            this.bpmBars = new BpmBarsNode();
            this.island = island;
            this.size(48.0f, 15.0f);
            this.interactive(false);
            this.bind(LYRICS_SIGNAL, IslandMusicStatus.this::lyricsVisible,
                    Motion.resolveMotionMotionFromFloatAndFloat(300.0f, 30.0f));
            this.snapPosition();
            this.snapSize();
            this.mount(this.albumArt);
            this.mount(this.title);
            this.mount(this.karaoke);
            this.mount(this.artist);
            this.mount(this.position);
            this.mount(this.duration);
            this.mount(this.progress);
            this.mount(this.previous);
            this.mount(this.play);
            this.mount(this.next);
            this.mount(this.source);
            this.mount(this.bpmLabel);
            this.mount(this.lyricsToggle);
            this.mount(this.lyrics);
            this.mount(this.bpmBars);
        }

        @Override
        protected void measure() {
            MediaInfo media = IslandMusicStatus.this.media();
            this.prefW = this.contentWidth(media);
            this.prefH = this.contentHeight();
        }

        @Override
        protected void onTick(float delta, float mouseX, float mouseY) {
            this.children().removeIf(node -> node.phase() == UiNode.LifecyclePhase.DISCARDED);
            this.updateLyricsAlpha();
            this.layoutChildren();
            for (UiNode node : this.children()) {
                node.tick(delta, mouseX, mouseY);
            }
        }

        @Override
        protected void drawChildren(RockstarDrawContext drawContext, float alpha) {
            this.updatePlayback();
            IslandMusicStatus.this.updateBars();
            this.layoutChildren();
            for (UiNode node : this.children()) {
                if (!node.inFlow()) {
                    continue;
                }
                node.draw(drawContext, alpha);
            }
        }

        /** ORIGINAL: the private {@code II()V} - the whole hand-rolled layout pass. */
        private void layoutChildren() {
            MediaInfo media = IslandMusicStatus.this.media();
            float expansion = this.expansion();
            boolean expandedLike = this.expandedLike();
            float width = media == null ? 48.0f : IslandMusicStatus.this.contentWidth(media);
            if (!expandedLike && this.w() > 1.0f) {
                width = this.w();
            }
            float left = expandedLike
                    ? WindowMetricsProvider.INSTANCE.width() / 2.0f - 82.0f
                    : this.x();
            float centerX = expandedLike
                    ? WindowMetricsProvider.INSTANCE.width() / 2.0f
                    : left + width / 2.0f;
            float right = expandedLike ? left + 164.0f : left + width;
            float height = IslandMusicStatus.this.expandedHeight();
            float pad = 4.0f + 6.0f * expansion;
            float art = 7.0f + 19.0f * expansion;
            float artY = this.y() + (this.island.isExpanded()
                    ? pad
                    : (this.island.getSize().height - art) / 2.0f);
            this.albumArt.setSlot(left + pad - 10.0f + 10.0f * IslandMusicStatus.this.animation.getValue(),
                    artY, art, art);
            float rightEdge = expandedLike ? right - 22.0f : left + width - 12.0f;
            float textX = left - 5.0f + 20.0f * IslandMusicStatus.this.animation.getValue()
                    + 29.0f * expansion;
            float textWidth = Math.max(0.0f, rightEdge - 3.0f - textX);
            this.title.setSlot(textX, this.y() + 5.0f + 11.0f * expansion, textWidth,
                    this.title.lineHeight());
            this.karaoke.setSlot(textX, this.y() + 5.0f, textWidth, this.karaoke.lineHeight());
            this.artist.setSlot(left + 20.0f + 24.0f * expansion, this.y() + 5.0f + 19.0f * expansion,
                    textWidth, this.artist.lineHeight());
            String durationText = media == null ? "0:00" : IslandMusicStatus.formatTime(media.getDuration());
            float durationWidth = Font.REGULAR.metrics(5.0f).measureText(durationText);
            this.position.setSlot(centerX - 82.0f + 11.0f * expansion, this.y() + 43.0f * expansion,
                    Font.REGULAR.metrics(5.0f).measureText(this.position.value()), this.position.lineHeight());
            this.duration.setSlot(centerX + 82.0f - (9.5f + durationWidth * expansion),
                    this.y() + 43.0f * expansion, durationWidth, this.duration.lineHeight());
            float barWidth = 115.0f + Font.REGULAR.measure("0:00", 5.0f)
                    - Font.REGULAR.measure(durationText, 5.0f);
            this.progress.setSlot(centerX - barWidth / 2.0f,
                    this.y() + height - (IslandMusicStatus.this.lyricsVisible() ? 45.0f : 0.0f)
                            - 36.5f * expansion,
                    barWidth, 3.0f);
            float buttonY = this.y() + height - 25.0f * expansion;
            this.previous.setSlot(centerX - 40.0f, buttonY, 16.0f, 16.0f);
            this.play.setSlot(centerX - 8.0f, buttonY, 16.0f, 16.0f);
            this.next.setSlot(centerX + 24.0f, buttonY, 16.0f, 16.0f);
            this.source.setSlot(right - 22.0f, this.y() + height - 21.0f, 8.0f, 8.0f);
            String bpm = IslandMusicStatus.this.bpmText();
            FontMetrics bpmFont = Font.MEDIUM.metrics(4.5f);
            float bpmWidth = bpmFont.measureText(bpm);
            this.bpmLabel.setSlot(right - 18.0f - bpmWidth, this.y() + 25.0f, bpmWidth,
                    bpmFont.getFontTopOffset());
            this.lyricsToggle.setSlot(left + 14.0f, this.y() + height - 21.0f, 8.0f, 8.0f);
            this.lyrics.setSlot(left + 10.0f, this.y() + 55.0f, 144.0f, 45.0f);
            this.bpmBars.setSlot(rightEdge,
                    this.y() + MathUtils.interpolateDouble(4.25, 14.0, (double)expansion), 14.0f, 10.0f);
        }

        /** ORIGINAL: the private {@code i()Z}. */
        private boolean expandedLike() {
            return this.island.isExpanded() || this.expansion() > 0.001f;
        }

        /** ORIGINAL: the private {@code Ii()V}. */
        private void updateLyricsAlpha() {
            float fromHeight = IslandMusicStatus.clamp01((this.h() - 80.0f) / 45.0f);
            IslandMusicStatus.this.lyricsAlpha = IslandMusicStatus.smoothstep(
                    Math.min(IslandMusicStatus.clamp01(this.sig(LYRICS_SIGNAL)), fromHeight), 0.1f, 0.7f);
        }

        /** ORIGINAL: the private {@code II()Z}. */
        private boolean expanding() {
            return this.expansion() != 0.0f;
        }

        /** ORIGINAL: the private {@code Ii()Z} - the transport buttons' hit/draw gate. */
        private boolean transportEnabled() {
            return this.expansion() > 0.7f && IslandMusicStatus.this.session() != null;
        }

        /** ORIGINAL: the private {@code iI()Z} - the lyrics toggle's gate. */
        private boolean lyricsToggleEnabled() {
            return this.expanding() && IslandMusicStatus.this.hasLyrics();
        }

        /** ORIGINAL: the private {@code I(Lrockstar/ilIlil/iiI;)V}. */
        private void mount(UiNode node) {
            node.lifeMotion(Motion.withLinearEasing(1L));
            this.add(node);
        }

        /** ORIGINAL: the private {@code iII()F}. */
        private float expansion() {
            return this.island.getExpandAnim().getValue();
        }

        /** ORIGINAL: the private {@code I(Ldev/redstones/mediaplayerinfo/MediaInfo;)F}. */
        private float contentWidth(MediaInfo media) {
            if (media == null) {
                return 48.0f;
            }
            return this.island.isExpanded() ? 164.0f : IslandMusicStatus.this.contentWidth(media);
        }

        /** ORIGINAL: the private {@code iIi()F}. */
        private float contentHeight() {
            return this.island.isExpanded() ? IslandMusicStatus.this.expandedHeight() : 15.0f;
        }

        /** ORIGINAL: the private {@code iI()V} - optimistic play state plus the BPM latch. */
        private void updatePlayback() {
            MediaInfo media = IslandMusicStatus.this.media();
            if (media != null && this.expanding()) {
                float target = media.isPlaying() ? 1.0f : 0.0f;
                long now = System.currentTimeMillis();
                if (IslandMusicStatus.this.pendingPlayState >= 0.0f) {
                    if (target == IslandMusicStatus.this.pendingPlayState
                            || now >= IslandMusicStatus.this.pendingUntil) {
                        IslandMusicStatus.this.pendingPlayState = -1.0f;
                    } else {
                        target = IslandMusicStatus.this.pendingPlayState;
                    }
                }
                IslandMusicStatus.this.playPauseAnim.setDuration(600L);
                IslandMusicStatus.this.playPauseAnim.update(target);
            }
            MediaTracker tracker = IslandMusicStatus.this.tracker();
            float measured = tracker.getBpm();
            boolean hasBpm = measured > 0.0f;
            if (hasBpm) {
                IslandMusicStatus.this.bpm = measured;
            }
            IslandMusicStatus.this.bpmAnim.update(hasBpm ? 1.0f : 0.0f);
        }

        /** ORIGINAL: the private {@code I(Lrockstar/ilIlil/III;LIiiIIIIIi$IIi;)V}. */
        private void paintPlayPause(RockstarDrawContext drawContext, TransportButton node) {
            float progress = IslandMusicStatus.this.playPauseAnim.getValue();
            float centerX = node.x() + node.w() / 2.0f;
            float centerY = node.y() + node.h() / 2.0f;
            float playAlpha = 255.0f * (1.0f - progress) - 100.0f * node.hover();
            float pauseAlpha = 255.0f * progress - 100.0f * node.hover();
            if (playAlpha > 0.5f) {
                ItemRenderUtils.translateAndRotate(drawContext.getMatrices(), centerX, centerY,
                        90.0f * progress);
                ItemRenderUtils.translateAndScale(drawContext.getMatrices(), centerX, centerY,
                        1.0f - progress);
                drawContext.drawIcon("music/play", node.x(), node.y(), node.w(),
                        ColorPalette.getPrimaryTextColor().withAlpha(playAlpha));
                ItemRenderUtils.popMatrix(drawContext.getMatrices());
                ItemRenderUtils.popMatrix(drawContext.getMatrices());
            }
            if (pauseAlpha > 0.5f) {
                ItemRenderUtils.translateAndRotate(drawContext.getMatrices(), centerX, centerY,
                        -90.0f + 90.0f * progress);
                ItemRenderUtils.translateAndScale(drawContext.getMatrices(), centerX, centerY, progress);
                drawContext.drawIcon("music/pause", node.x(), node.y(), node.w(),
                        ColorPalette.getPrimaryTextColor().withAlpha(pauseAlpha));
                ItemRenderUtils.popMatrix(drawContext.getMatrices());
                ItemRenderUtils.popMatrix(drawContext.getMatrices());
            }
        }

        /** ORIGINAL: the private synthetic {@code IiI()V}. */
        private void previousTrack() {
            IMediaSession session = IslandMusicStatus.this.session();
            if (session != null) {
                session.previous();
            }
        }

        /** ORIGINAL: the private synthetic {@code IIi()V}. */
        private void togglePlay() {
            IMediaSession session = IslandMusicStatus.this.session();
            if (session == null) {
                return;
            }
            MediaInfo media = session.getMedia();
            float target = media != null && media.isPlaying() ? 0.0f : 1.0f;
            session.playPause();
            IslandMusicStatus.this.pendingPlayState = target;
            IslandMusicStatus.this.pendingUntil = System.currentTimeMillis() + 1000L;
            IslandMusicStatus.this.playPauseAnim.setDuration(600L);
            IslandMusicStatus.this.playPauseAnim.update(target);
        }

        /** ORIGINAL: the private synthetic {@code III()V}. */
        private void nextTrack() {
            IMediaSession session = IslandMusicStatus.this.session();
            if (session != null) {
                session.next();
            }
        }

        /** ORIGINAL: the private synthetic {@code ii()V}. */
        private void toggleLyrics() {
            if (!IslandMusicStatus.this.hasLyrics()) {
                return;
            }
            IslandMusicStatus.this.setLyricsEnabled(!IslandMusicStatus.this.lyricsEnabled());
        }

        /** ORIGINAL: the private synthetic {@code Ii()Ljava/lang/String;}. */
        private String titleText() {
            return IslandMusicStatus.this.text(MediaInfo::getTitle);
        }

        /** ORIGINAL: the private synthetic {@code Ii()Lpyrock/utility/render/ColorRGBA;}. */
        private ColorRGBA titleColor() {
            return ColorPalette.getPrimaryTextColor()
                    .withAlpha(255.0f * (1.0f - IslandMusicStatus.this.karaokeAlpha()));
        }

        /** ORIGINAL: the private synthetic {@code II()Ljava/lang/String;}. */
        private String artistText() {
            return IslandMusicStatus.this.text(MediaInfo::getArtist);
        }

        /** ORIGINAL: the private synthetic {@code II()Lpyrock/utility/render/ColorRGBA;}. */
        private ColorRGBA artistColor() {
            return ColorPalette.getPrimaryTextColor().withAlpha(178.5f * this.expansion());
        }

        /** ORIGINAL: the private synthetic {@code i()Ljava/lang/String;}. */
        private String positionText() {
            MediaInfo media = IslandMusicStatus.this.media();
            return IslandMusicStatus.formatTime(media == null ? 0L : media.getPosition());
        }

        /** ORIGINAL: the private synthetic {@code I()Ljava/lang/String;}. */
        private String durationText() {
            MediaInfo media = IslandMusicStatus.this.media();
            return IslandMusicStatus.formatTime(media == null ? 0L : media.getDuration());
        }
    }

    /**
     * ORIGINAL: {@code IiiIIIIIi$IIi} (final, extends {@code Iii} = {@link TextComponent}) - a
     * media-transport button that is both untouchable and invisible while its gate is false.
     */
    final class TransportButton extends TextComponent {
        private final BooleanSupplier gate;

        TransportButton(BooleanSupplier gate) {
            this.gate = gate;
            this.snapPosition();
            this.snapSize();
            this.cursor(Cursor.HAND);
        }

        @Override
        public boolean contains(float mouseX, float mouseY) {
            return this.gate.getAsBoolean() && super.contains(mouseX, mouseY);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            if (this.gate.getAsBoolean()) {
                super.drawSelf(drawContext, alpha);
            }
        }
    }

    /** ORIGINAL: {@code IiiIIIIIi$I} - the album-art tile. */
    final class AlbumArtNode extends UiNode {
        AlbumArtNode() {
            this.snapPosition();
            this.snapSize();
            this.interactive(false);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            Identifier texture = IslandMusicStatus.this.artworkTexture();
            float expansion = IslandMusicStatus.this.islandExpansion();
            drawContext.drawRoundedTexture(texture, this.x(), this.y(), this.w(), this.h(),
                    WidgetState.uniform(1.0f + 5.0f * expansion));
        }
    }

    /**
     * ORIGINAL: {@code IiiIIIIIi$iI} - the title / artist line: plain text while it fits, a
     * scissored fade-out otherwise.
     */
    final class MarqueeTextNode extends UiNode {
        private final FontMetrics font;
        private final Supplier<String> text;
        private final Supplier<ColorRGBA> color;
        private final float fadeStart;
        private final float fadeEnd;

        MarqueeTextNode(FontMetrics font, Supplier<String> text, Supplier<ColorRGBA> color, float fadeStart,
                        float fadeEnd) {
            this.font = font;
            this.text = text;
            this.color = color;
            this.fadeStart = fadeStart;
            this.fadeEnd = fadeEnd;
            this.snapPosition();
            this.snapSize();
            this.interactive(false);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            String value = this.value();
            if (value.isEmpty()) {
                return;
            }
            ColorRGBA colorRGBA = this.color.get();
            if (colorRGBA == null) {
                return;
            }
            float width = Math.max(0.0f, this.w());
            if (width <= 0.0f) {
                return;
            }
            if (this.font.measureText(value) <= width) {
                drawContext.drawText(this.font, value, this.x(), this.y(), colorRGBA);
            } else {
                UiScissorStack.push(drawContext.getMatrices(), this.x() - 1.0f, this.y() - 1.0f, width + 1.0f,
                        this.font.getFontTopOffset() + 2.0f);
                try {
                    drawContext.drawFadeoutText(this.font, value, this.x(), this.y(), colorRGBA,
                            Math.max(this.fadeStart, 0.84f), Math.max(this.fadeEnd, 0.96f), width);
                } finally {
                    UiScissorStack.pop();
                }
            }
        }

        private String value() {
            String value = this.text.get();
            return value == null ? "" : value;
        }

        /** ORIGINAL: the package-private {@code I()F}. */
        float lineHeight() {
            return this.font.getFontTopOffset();
        }
    }

    /** ORIGINAL: {@code IiiIIIIIi$iII} - a plain one-line label that hides while collapsed. */
    final class SmallTextNode extends UiNode {
        private final FontMetrics font;
        private final Supplier<String> text;
        private final Supplier<ColorRGBA> color;

        SmallTextNode(FontMetrics font, Supplier<String> text, Supplier<ColorRGBA> color) {
            this.font = font;
            this.text = text;
            this.color = color;
            this.snapPosition();
            this.snapSize();
            this.interactive(false);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            if (IslandMusicStatus.this.islandExpansion() == 0.0f) {
                return;
            }
            String value = this.value();
            if (!value.isEmpty()) {
                drawContext.drawText(this.font, value, this.x(), this.y(), this.color.get());
            }
        }

        /** ORIGINAL: the package-private {@code I()Ljava/lang/String;}. */
        String value() {
            String value = this.text.get();
            return value == null ? "" : value;
        }

        /** ORIGINAL: the package-private {@code I()F}. */
        float lineHeight() {
            return this.font.getFontTopOffset();
        }
    }

    /** ORIGINAL: {@code IiiIIIIIi$iIi} - the playback progress bar. */
    final class ProgressBarNode extends UiNode {
        ProgressBarNode() {
            this.snapPosition();
            this.snapSize();
            this.interactive(false);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            MediaInfo media = IslandMusicStatus.this.media();
            if (media == null || IslandMusicStatus.this.islandExpansion() == 0.0f) {
                return;
            }
            ColorRGBA color = ColorPalette.getPrimaryTextColor();
            drawContext.drawRoundedRect(this.x(), this.y(), this.w(), this.h(), WidgetState.uniform(0.5f),
                    color.withAlpha(63.75f));
            float duration = media.getDuration();
            float filled = duration <= 0.0f
                    ? 0.0f
                    : this.w() * Math.min(1.0f, (float)media.getPosition() / duration);
            drawContext.drawRoundedRect(this.x(), this.y(), filled, this.h(), WidgetState.uniform(0.5f),
                    color.withAlpha(150.0f));
        }
    }

    /** ORIGINAL: {@code IiiIIIIIi$Iii} - the media-source badge. */
    final class SourceBadgeNode extends UiNode {
        SourceBadgeNode() {
            this.snapPosition();
            this.snapSize();
            this.interactive(false);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            if (IslandMusicStatus.this.islandExpansion() == 0.0f) {
                return;
            }
            String source = IslandMusicStatus.this.sourceKey();
            if (source != null) {
                drawContext.drawTexture(RockstarClient.resourceId("icons/media/" + source + ".png"), this.x(),
                        this.y(), this.w(), this.h(), ColorRGBA.WHITE);
            }
        }
    }

    /** ORIGINAL: {@code IiiIIIIIi$i} - the "NNN BPM" readout, pulsed on the beat. */
    final class BpmTextNode extends UiNode {
        BpmTextNode() {
            this.snapPosition();
            this.snapSize();
            this.interactive(false);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            if (IslandMusicStatus.this.islandExpansion() == 0.0f
                    || IslandMusicStatus.this.bpmAnim.getValue() <= 0.02f
                    || IslandMusicStatus.this.bpm <= 0.0f) {
                return;
            }
            String text = IslandMusicStatus.this.bpmText();
            float pulse = IslandMusicStatus.this.beatPulse();
            float textAlpha = (140.0f + 90.0f * pulse) * IslandMusicStatus.this.islandExpansion()
                    * IslandMusicStatus.this.bpmAnim.getValue();
            FontMetrics font = Font.MEDIUM.metrics(4.5f);
            ItemRenderUtils.translateAndScale(drawContext.getMatrices(), this.x() + this.w() / 2.0f,
                    this.y() + 2.5f, 1.0f + 0.1f * pulse);
            drawContext.drawText(font, text, this.x(), this.y(),
                    IslandMusicStatus.this.tracker().getArtworkColor().withAlpha(textAlpha));
            ItemRenderUtils.popMatrix(drawContext.getMatrices());
        }
    }

    /** ORIGINAL: {@code IiiIIIIIi$iiI} - the four beat bars. */
    final class BpmBarsNode extends UiNode {
        BpmBarsNode() {
            this.snapPosition();
            this.snapSize();
            this.interactive(false);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            MediaTracker tracker = IslandMusicStatus.this.tracker();
            float expansion = IslandMusicStatus.this.islandExpansion();
            for (int i = 0; i < IslandMusicStatus.this.bars.length; ++i) {
                drawContext.drawRoundedRect(this.x() + (float)i * (2.0f + expansion),
                        this.y() + (7.0f - IslandMusicStatus.this.bars[i]) / 2.0f, 1.0f + expansion,
                        IslandMusicStatus.this.bars[i], WidgetState.uniform(0.5f), tracker.getArtworkColor());
            }
        }
    }

    /**
     * ORIGINAL: {@code IiiIIIIIi$ii} (static final) - the per-character / per-word measurement model
     * the karaoke line animates over.
     */
    static final class WordTiming {
        final String[] chars;
        final float[] charX;
        final String[] words;
        final int[] wordStart;
        final int[] wordEnd;
        final float[] wordStartX;
        final float[] wordEndX;
        final float[] wordScale;
        final float[] wordShift;
        final float totalWidth;
        final long startTime;
        float extraWidth;
        float scrollOffset;
        float maxScroll;
        float revealX;
        long exitTime;

        WordTiming(FontMetrics font, String text, long startTime) {
            this.startTime = startTime;
            int length = text.length();
            this.chars = new String[length];
            this.charX = new float[length];
            int wordCount = 0;
            boolean inWord = false;
            for (int i = 0; i < length; ++i) {
                boolean whitespace = Character.isWhitespace(text.charAt(i));
                if (!whitespace && !inWord) {
                    ++wordCount;
                }
                inWord = !whitespace;
            }
            this.words = new String[wordCount];
            this.wordStart = new int[wordCount];
            this.wordEnd = new int[wordCount];
            this.wordStartX = new float[wordCount];
            this.wordEndX = new float[wordCount];
            this.wordScale = new float[wordCount];
            this.wordShift = new float[wordCount];
            Arrays.fill(this.wordScale, 1.0f);
            int word = -1;
            inWord = false;
            for (int i = 0; i < length; ++i) {
                char character = text.charAt(i);
                this.chars[i] = String.valueOf(character);
                this.charX[i] = font.measureText(text.substring(0, i));
                boolean whitespace = Character.isWhitespace(character);
                if (!whitespace) {
                    if (!inWord) {
                        this.wordStart[++word] = i;
                        this.wordStartX[word] = this.charX[i];
                    }
                    this.wordEnd[word] = i;
                    this.wordEndX[word] = this.charX[i] + font.measureCharacter(character);
                }
                inWord = !whitespace;
            }
            for (int i = 0; i < wordCount; ++i) {
                this.words[i] = text.substring(this.wordStart[i], this.wordEnd[i] + 1);
            }
            this.totalWidth = font.measureText(text);
        }

        /** ORIGINAL: the package-private {@code I()I}. */
        int wordCount() {
            return this.words.length;
        }

        /** ORIGINAL: the package-private {@code I(I)F}. */
        float charAdvance(int index) {
            return (index + 1 < this.chars.length ? this.charX[index + 1] : this.totalWidth)
                    - this.charX[index];
        }
    }

    /**
     * ORIGINAL: {@code IiiIIIIIi$Ii} - the karaoke line that sits over the title: per-character
     * reveal, per-word scale pop, and an auto-scroll that follows the sung word.
     */
    final class KaraokeLineNode extends UiNode {
        private static final float REVEAL_SPAN = 240.0f;
        private static final float STEP_LIMIT = 16.0f;
        private static final float ENTER_TOTAL = 320.0f;
        private static final float ACTIVE_POP = 0.13f;
        private static final float LIFT = 3.0f;
        private static final float DIM_ALPHA = 0.42f;
        private static final float EDGE_FADE = 9.0f;
        private static final float WORD_GAP = 5.0f;
        private static final float EXIT_SPAN = 180.0f;
        private static final float EXIT_TOTAL = 220.0f;
        private static final float RISE = 6.0f;
        private static final float HANDOVER = 90.0f;

        private final FontMetrics font = Font.MEDIUM.metrics(7.0f);
        private WordTiming current;
        private WordTiming outgoing;
        private int lastLine = -1;
        private String lastText = "";
        private long lastTime = System.currentTimeMillis();

        KaraokeLineNode() {
            this.snapPosition();
            this.snapSize();
            this.interactive(false);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            long now = System.currentTimeMillis();
            float delta = Math.min(0.05f, Math.max(0.0f, (float)(now - this.lastTime) / 1000.0f));
            this.lastTime = now;
            this.syncLine(now);
            float fade = IslandMusicStatus.this.karaokeAlpha();
            if (fade <= 0.003f) {
                return;
            }
            float width = Math.max(1.0f, this.w());
            if (this.outgoing != null) {
                if ((float)(now - this.outgoing.exitTime) >= 400.0f) {
                    this.outgoing = null;
                } else {
                    this.drawTiming(drawContext, this.outgoing, -1, fade, width, now, true);
                }
            }
            if (this.current == null) {
                return;
            }
            int active = this.activeWord(this.current);
            this.updateScales(this.current, active, delta);
            this.updateShifts(this.current);
            this.updateScroll(this.current, width, active, delta);
            this.current.revealX = this.revealX(this.current);
            this.drawTiming(drawContext, this.current, active, fade, width, now, false);
        }

        /** ORIGINAL: the private {@code I(III,ii,IFFJZ)V}. */
        private void drawTiming(RockstarDrawContext drawContext, WordTiming timing, int active, float fade,
                                float width, long now, boolean exiting) {
            if (timing.wordCount() == 0) {
                return;
            }
            float step = Math.min(16.0f, (exiting ? 220.0f : 320.0f) / (float)timing.chars.length);
            float elapsed = now - (exiting ? timing.exitTime : timing.startTime);
            if (elapsed <= 0.0f) {
                return;
            }
            UiScissorStack.push(drawContext.getMatrices(), this.x() - 1.0f, this.y() - 1.5f, width + 2.0f,
                    this.font.getFontTopOffset() + this.font.getFontBottomOffset() + 3.0f);
            for (int i = 0; i < timing.wordCount(); ++i) {
                this.drawWord(drawContext, timing, i, active, fade, elapsed, step, width, exiting);
            }
            UiScissorStack.pop();
        }

        /** ORIGINAL: the private {@code I(III,ii,IIFFFFZ)V}; the {@code active} argument is unused. */
        private void drawWord(RockstarDrawContext drawContext, WordTiming timing, int index, int active,
                              float fade, float elapsed, float step, float width, boolean exiting) {
            float scale = timing.wordScale[index];
            float shift = timing.wordShift[index];
            float lift = (scale - 1.0f) * 3.0f;
            boolean scaled = Math.abs(scale - 1.0f) > 0.002f;
            if (scaled) {
                ItemRenderUtils.translateAndScale(drawContext.getMatrices(),
                        this.x() - timing.scrollOffset + shift
                                + (timing.wordStartX[index] + timing.wordEndX[index]) / 2.0f,
                        this.y() + this.font.getFontTopOffset() / 2.0f, scale);
            }
            ColorRGBA color = ColorPalette.getPrimaryTextColor();
            boolean outsideReveal = timing.revealX <= timing.wordStartX[index]
                    || timing.revealX >= timing.wordEndX[index];
            boolean settled = !exiting && elapsed - (float)timing.wordEnd[index] * step >= 240.0f;
            if (outsideReveal && settled && timing.totalWidth <= width) {
                float wordAlpha = timing.revealX >= timing.wordEndX[index] ? 1.0f : 0.42f;
                drawContext.drawText(this.font, timing.words[index],
                        this.x() - timing.scrollOffset + shift + timing.wordStartX[index], this.y() - lift,
                        color.withAlpha(255.0f * fade * wordAlpha));
                if (scaled) {
                    ItemRenderUtils.popMatrix(drawContext.getMatrices());
                }
                return;
            }
            for (int i = timing.wordStart[index]; i <= timing.wordEnd[index]; ++i) {
                float progress = exiting
                        ? 1.0f - IslandMusicStatus.clamp01((elapsed - (float)i * step) / 180.0f)
                        : IslandMusicStatus.clamp01((elapsed - (float)i * step) / 240.0f);
                if (progress <= 0.001f) {
                    continue;
                }
                float eased = Easing.easeOutCubic.ease(progress, 0.0f, 1.0f, 1.0f);
                float charX = this.x() - timing.scrollOffset + shift + timing.charX[i];
                float edge = this.edgeFade(timing, charX, width);
                if (edge <= 0.001f) {
                    continue;
                }
                float reveal = IslandMusicStatus.clamp01((timing.revealX - timing.charX[i])
                        / Math.max(0.5f, timing.charAdvance(i) * 0.55f));
                float charAlpha = 0.42f + (1.0f - 0.42f) * reveal;
                float rise = (1.0f - eased) * (exiting ? -6.0f : 6.0f);
                drawContext.drawText(this.font, timing.chars[i], charX, this.y() - lift + rise,
                        color.withAlpha(255.0f * fade * charAlpha * eased * edge));
            }
            if (scaled) {
                ItemRenderUtils.popMatrix(drawContext.getMatrices());
            }
        }

        /** ORIGINAL: the private {@code I(ii)F}. */
        private float revealX(WordTiming timing) {
            float exact = IslandMusicStatus.clamp01(IslandMusicStatus.this.karaokeProgress)
                    * (float)timing.chars.length;
            if (exact >= (float)timing.chars.length) {
                return timing.totalWidth;
            }
            int index = (int)exact;
            return timing.charX[index] + timing.charAdvance(index) * (exact - (float)index);
        }

        /** ORIGINAL: the private {@code I(J)V}. */
        private void syncLine(long now) {
            if (IslandMusicStatus.this.karaokeLine < 0
                    || (IslandMusicStatus.this.karaokeLine == this.lastLine
                        && IslandMusicStatus.this.karaokeText.equals(this.lastText))) {
                return;
            }
            this.lastLine = IslandMusicStatus.this.karaokeLine;
            this.lastText = IslandMusicStatus.this.karaokeText;
            long start = now;
            if (this.current != null) {
                this.current.exitTime = now;
                this.outgoing = this.current;
                start += 90L;
            }
            this.current = new WordTiming(this.font,
                    NametagTextUtils.smallCaps(IslandMusicStatus.this.karaokeText), start);
        }

        /** ORIGINAL: the private {@code I(ii)I} - note it uses the RAW karaoke progress. */
        private int activeWord(WordTiming timing) {
            for (int i = timing.wordCount() - 1; i > 0; --i) {
                if (!(IslandMusicStatus.this.karaokeProgress * (float)timing.chars.length
                        >= (float)timing.wordStart[i])) {
                    continue;
                }
                return i;
            }
            return 0;
        }

        /** ORIGINAL: the private {@code I(ii,IF)V}. */
        private void updateScales(WordTiming timing, int active, float delta) {
            float rate = Math.min(1.0f, delta * 11.0f);
            for (int i = 0; i < timing.wordCount(); ++i) {
                float target = i == active ? 1.13f : 1.0f;
                timing.wordScale[i] = timing.wordScale[i] + (target - timing.wordScale[i]) * rate;
            }
        }

        /** ORIGINAL: the private {@code I(ii)V}. */
        private void updateShifts(WordTiming timing) {
            float total = 0.0f;
            for (int i = 0; i < timing.wordCount(); ++i) {
                float extra = (timing.wordScale[i] - 1.0f)
                        * (timing.wordEndX[i] - timing.wordStartX[i] + 5.0f);
                timing.wordShift[i] = total + extra / 2.0f;
                total += extra;
            }
            timing.extraWidth = total;
        }

        /** ORIGINAL: the private {@code I(ii,FIF)V}. */
        private void updateScroll(WordTiming timing, float width, int active, float delta) {
            float overflow = Math.max(0.0f, timing.totalWidth - width);
            timing.maxScroll = overflow;
            if (overflow <= 0.5f) {
                timing.scrollOffset -= timing.scrollOffset * Math.min(1.0f, delta * 9.0f);
                return;
            }
            float limit = Math.max(overflow, timing.totalWidth + timing.extraWidth - width);
            float target = Math.clamp(timing.wordEndX[active] + timing.wordShift[active] - width * 0.6f,
                    0.0f, limit);
            if (target <= timing.scrollOffset) {
                return;
            }
            timing.scrollOffset += (target - timing.scrollOffset)
                    * (1.0f - (float)Math.exp(-9.0f * delta));
        }

        /** ORIGINAL: the private {@code I(ii,FF)F}. */
        private float edgeFade(WordTiming timing, float charX, float width) {
            if (timing.totalWidth <= width) {
                return 1.0f;
            }
            float local = charX - this.x();
            float leftFade = 1.0f - (1.0f - IslandMusicStatus.clamp01(local / 9.0f))
                    * IslandMusicStatus.clamp01(timing.scrollOffset / 9.0f);
            float rightFade = 1.0f - (1.0f - IslandMusicStatus.clamp01((width - local) / 9.0f))
                    * IslandMusicStatus.clamp01((timing.maxScroll - timing.scrollOffset) / 9.0f);
            return Math.min(leftFade, rightFade);
        }

        /** ORIGINAL: the package-private {@code I()F}. */
        float lineHeight() {
            return this.font.getFontTopOffset();
        }
    }

    /**
     * ORIGINAL: {@code IiiIIIIIi$III} - the six-line lyric scroller with the karaoke highlight
     * sweep and the per-line marquee.
     */
    final class LyricsScrollerNode extends UiNode {
        private static final int VISIBLE_LINES = 6;
        private static final float LINE_HEIGHT = 8.0f;

        private final Animation scrollAnim = new Animation(360L, 0.0f, Easing.easeInOutCubicBezier);
        private final Animation activeAnim = new Animation(280L, -1.0f, Easing.easeInOutCubicBezier);
        private LyricsTimeline timeline;
        private int marqueeLine = -1;
        private String marqueeText = "";
        private float marqueeOffset;
        private long lastTime = System.currentTimeMillis();

        LyricsScrollerNode() {
            this.snapPosition();
            this.snapSize();
            this.interactive(false);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            LyricsTimeline lyrics = IslandMusicStatus.this.tracker().getLyrics();
            MediaInfo media = IslandMusicStatus.this.media();
            float fade = IslandMusicStatus.this.lyricsAlpha();
            if (!IslandMusicStatus.this.lyricsEnabled() || lyrics.isEmpty() || media == null
                    || fade <= 0.001f) {
                return;
            }
            List<LyricsTimeline.LyricLine> lines = lyrics.getLines();
            int count = lines.size();
            double position = IslandMusicStatus.this.tracker().getPlaybackPositionSeconds() * 1000.0 + 150.0;
            int currentLine = lyrics.hasTiming() ? lyrics.findLineAtOrBefore((long)position) : -1;
            int maxScroll = Math.max(0, count - 6);
            int top = lyrics.hasTiming()
                    ? Math.clamp((long)(currentLine - 2), 0, maxScroll)
                    : Math.clamp((long)IslandMusicStatus.this.scrollIndex, 0, maxScroll);
            IslandMusicStatus.this.scrollIndex = top;
            this.syncTimeline(lyrics, top, currentLine);
            float scroll = this.scrollAnim.getValue();
            float activeValue = this.activeAnim.getValue();
            int first = Math.max(0, (int)Math.floor(scroll) - 1);
            int last = Math.min(count - 1, (int)Math.ceil(scroll) + 6 + 1);
            int marqueeIndex = lyrics.hasTiming()
                    ? currentLine
                    : Math.clamp((long)Math.round(scroll + 2.0f), 0, count - 1);
            for (int i = first; i <= last; ++i) {
                float offset = (float)i - scroll;
                float visibility = Math.min(IslandMusicStatus.clamp01(offset + 1.0f),
                        IslandMusicStatus.clamp01(6.0f - offset));
                if (visibility <= 0.001f) {
                    continue;
                }
                float focus = lyrics.hasTiming()
                        ? IslandMusicStatus.clamp01(1.0f - Math.abs((float)i - activeValue))
                        : 0.0f;
                float halo = lyrics.hasTiming()
                        ? Math.max(0.0f, IslandMusicStatus.clamp01(1.5f - Math.abs((float)i - activeValue))
                                - focus)
                        : 0.0f;
                float lineAlpha = (lyrics.hasTiming() ? 50.0f + 205.0f * focus + 100.0f * halo : 200.0f)
                        * fade * visibility;
                FontMetrics font = focus > 0.1f
                        ? Font.MEDIUM.metrics(IslandMusicStatus.lerp(5.0f, 6.0f, focus))
                        : Font.REGULAR.metrics(5.0f);
                float lineY = this.y() + offset * 8.0f;
                float reveal = i == currentLine
                        ? lyrics.getLineRevealProgress(i, position,
                                (long)((double)media.getDuration() * 1000.0))
                        : -1.0f;
                ColorRGBA color = ColorPalette.getPrimaryTextColor()
                        .withAlpha(reveal >= 0.0f ? lineAlpha * 0.38f : lineAlpha);
                this.drawLine(drawContext, font, lines.get(i).getText(), i, i == marqueeIndex, this.x(),
                        lineY, Math.max(1.0f, this.w()), color, ColorRGBA.WHITE.withAlpha(lineAlpha), reveal);
            }
        }

        /** ORIGINAL: the private {@code I(iiIiIIIii,II)V}. */
        private void syncTimeline(LyricsTimeline lyrics, int top, int currentLine) {
            if (this.timeline != lyrics) {
                this.timeline = lyrics;
                this.scrollAnim.setValue((float)top);
                this.activeAnim.setValue((float)currentLine);
                this.resetMarquee();
                return;
            }
            this.scrollAnim.update((float)top);
            this.activeAnim.update((float)currentLine);
        }

        /** ORIGINAL: the private {@code I(III,IIiIIi,String,IZFFFColorRGBA,ColorRGBA,F)V}. */
        private void drawLine(RockstarDrawContext drawContext, FontMetrics font, String text, int index,
                              boolean marquee, float x, float y, float width, ColorRGBA base,
                              ColorRGBA highlight, float reveal) {
            float textWidth = font.measureText(text);
            float revealWidth = this.revealWidth(font, text, reveal);
            if (!marquee || textWidth <= width) {
                drawContext.drawFadeoutText(font, text, x, y, base, 0.88f, 1.0f, width);
                if (reveal >= 0.0f) {
                    this.drawHighlight(drawContext, font, text, x, y, revealWidth, width, highlight,
                            color -> drawContext.drawFadeoutText(font, text, x, y, color, 0.88f, 1.0f, width));
                }
                return;
            }
            this.advanceMarquee(index, text, textWidth, width, revealWidth, reveal,
                    System.currentTimeMillis());
            float overflow = Math.max(0.0f, textWidth - width);
            this.marqueeOffset = Math.min(this.marqueeOffset, overflow);
            UiScissorStack.push(drawContext.getMatrices(), x - 2.5f, y - 3.0f, width + 5.0f,
                    font.getFontMetricsFloat() + 6.0f);
            drawContext.pushMatrix();
            drawContext.getMatrices().translate(-this.marqueeOffset, 0.0f, 0.0f);
            drawContext.drawText(font, text, x, y, base);
            if (reveal >= 0.0f) {
                this.drawHighlight(drawContext, font, text, x, y, revealWidth, textWidth, highlight,
                        color -> drawContext.drawText(font, text, x, y, color));
            }
            drawContext.popMatrix();
            UiScissorStack.pop();
        }

        /** ORIGINAL: the private {@code I(III,IIiIIi,String,FFFFColorRGBA,Consumer)V}. */
        private void drawHighlight(RockstarDrawContext drawContext, FontMetrics font, String text, float x,
                                   float y, float revealWidth, float maxWidth, ColorRGBA color,
                                   Consumer<ColorRGBA> draw) {
            if (revealWidth <= 0.0f || text.isEmpty()) {
                return;
            }
            this.clipAndDraw(drawContext, x, y, Math.min(maxWidth, revealWidth + 1.2f),
                    font.getFontMetricsFloat(), color.mulAlpha(0.18f), draw);
            this.clipAndDraw(drawContext, x, y, Math.min(maxWidth, revealWidth + 0.55f),
                    font.getFontMetricsFloat(), color.mulAlpha(0.38f), draw);
            this.clipAndDraw(drawContext, x, y, Math.min(maxWidth, revealWidth),
                    font.getFontMetricsFloat(), color, draw);
        }

        /** ORIGINAL: the private {@code I(III,FFFFColorRGBA,Consumer)V}. */
        private void clipAndDraw(RockstarDrawContext drawContext, float x, float y, float width, float height,
                                 ColorRGBA color, Consumer<ColorRGBA> draw) {
            if (width <= 0.0f || color.getAlpha() <= 0.0f) {
                return;
            }
            UiScissorStack.push(drawContext.getMatrices(), x - 2.0f, y - 3.0f, width + 2.0f, height + 6.0f);
            draw.accept(color);
            UiScissorStack.pop();
        }

        /** ORIGINAL: the private {@code I(IIiIIi,String,F)F} - code-point aware reveal width. */
        private float revealWidth(FontMetrics font, String text, float progress) {
            int count = text.codePointCount(0, text.length());
            if (count == 0) {
                return 0.0f;
            }
            float exact = IslandMusicStatus.clamp01(progress) * (float)count;
            int whole = Math.min(count, (int)exact);
            int end = text.offsetByCodePoints(0, whole);
            float width = font.measureText(text.substring(0, end));
            if (whole == count) {
                return width;
            }
            int next = text.offsetByCodePoints(end, 1);
            float nextWidth = font.measureText(text.substring(0, next));
            return width + (nextWidth - width) * (exact - (float)whole);
        }

        /** ORIGINAL: the private {@code I(ILjava/lang/String;FFFFJ)V}. */
        private void advanceMarquee(int index, String text, float textWidth, float width, float revealWidth,
                                    float reveal, long now) {
            if (this.marqueeLine != index || !this.marqueeText.equals(text)) {
                this.marqueeLine = index;
                this.marqueeText = text;
                this.marqueeOffset = 0.0f;
                this.lastTime = now;
                return;
            }
            float overflow = Math.max(0.0f, textWidth - width);
            float delta = Math.min(0.05f, Math.max(0.0f, (float)(now - this.lastTime) / 1000.0f));
            this.lastTime = now;
            this.marqueeOffset = Math.min(this.marqueeOffset, overflow);
            if (overflow <= 0.0f) {
                return;
            }
            if (reveal < 0.0f) {
                this.marqueeOffset = Math.min(overflow, this.marqueeOffset + delta * 24.0f);
                return;
            }
            float target = Math.clamp(revealWidth - width * 0.62f, 0.0f, overflow);
            if (target <= this.marqueeOffset) {
                return;
            }
            float rate = 1.0f - (float)Math.exp(-14.0f * delta);
            this.marqueeOffset = Math.min(target, this.marqueeOffset + (target - this.marqueeOffset) * rate);
        }

        /** ORIGINAL: the private {@code I()V}. */
        private void resetMarquee() {
            this.marqueeLine = -1;
            this.marqueeText = "";
            this.marqueeOffset = 0.0f;
            this.lastTime = System.currentTimeMillis();
        }
    }
}
