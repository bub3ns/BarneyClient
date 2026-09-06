/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.combat.rotation;

import java.io.BufferedWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import lombok.Generated;
import moscow.rockstar.combat.RotationController;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.NeuralAimModel;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import pyrock.events.game.AttackEvent;
import pyrock.events.player.ClientPlayerTickEvent;

public final class RotationLogWriter {
    private static final int MAX_TARGET_AGE_TICKS = 60;
    private static final int NO_TARGET_GRACE_TICKS = 40;
    private static final double MAX_TRACKING_DISTANCE = 5.0;
    private static final int FLUSH_INTERVAL_ROWS = 256;
    private static final String CSV_HEADER = "t,gcd,clean,yaw,pitch,dyaw,dpitch,has,tid,rx,ry,rz,bw,bh,dist,vis,on,atk,hp,ground,sprint\n";
    private static final int CSV_COLUMN_COUNT = "t,gcd,clean,yaw,pitch,dyaw,dpitch,has,tid,rx,ry,rz,bw,bh,dist,vis,on,atk,hp,ground,sprint\n".split(",").length;
    private static final double[] TARGET_SAMPLE_X_OFFSETS = new double[]{0.2, 0.5, 0.8};
    private static final double[] TARGET_SAMPLE_Y_OFFSETS = new double[]{0.15, 0.4, 0.65, 0.9};
    private static final double[] TARGET_SAMPLE_Z_OFFSETS = new double[]{0.2, 0.5, 0.8};
    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private BufferedWriter csvWriter;
    private Path sessionFile;
    private String sessionName = "session";
    private boolean recording;
    private int recordedRowCount;
    private int tickCount;
    private int attackSampleCount;
    private int targetGraceTicksRemaining;
    private int rowsSinceFlush;
    private int lastAttackedEntityId = -1;
    private int attackTick = -1;
    private boolean attackSamplePending;
    private boolean previousRotationAvailable;
    private float previousYaw;
    private float previousPitch;
    private final EventListener<AttackEvent> attackEventListener = attackEvent -> {
        LivingEntity class_13092;
        if (!this.recording) {
            return;
        }
        Entity class_12972 = attackEvent.getEntity();
        if (class_12972 instanceof LivingEntity && (class_13092 = (LivingEntity)class_12972) != this.minecraftClient.player) {
            this.lastAttackedEntityId = class_13092.getId();
            this.attackTick = this.tickCount;
            this.attackSamplePending = true;
        }
    };
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (this.minecraftClient.player == null || this.minecraftClient.world == null || this.csvWriter == null) {
            return;
        }
        ++this.tickCount;
        boolean bl = RockstarClient.create().getRotationManager().isIdle();
        Rotation rotation = RockstarClient.create().getRotationManager().getEffectiveRotation();
        float f = rotation.getYaw();
        float f2 = rotation.getPitch();
        float f3 = this.previousRotationAvailable ? MathHelper.wrapDegrees((float)(f - this.previousYaw)) : 0.0f;
        float f4 = this.previousRotationAvailable ? f2 - this.previousPitch : 0.0f;
        this.previousYaw = f;
        this.previousPitch = f2;
        this.previousRotationAvailable = true;
        LivingEntity class_13092 = this.findTrainingTarget();
        if (class_13092 == null) {
            if (this.targetGraceTicksRemaining-- > 0) {
                this.writeTrainingRow(bl, f, f2, f3, f4, null);
            }
            this.attackSamplePending = false;
        } else {
            this.targetGraceTicksRemaining = 40;
            this.writeTrainingRow(bl, f, f2, f3, f4, class_13092);
        }
    };
    public static final int TRAINING_ROW_TARGET = 12000;
    private static final double TICKS_PER_MINUTE = 1200.0;

    public String startRecording(String string) {
        if (this.recording) {
            return "\u0417\u0430\u043f\u0438\u0441\u044c \u0443\u0436\u0435 \u0438\u0434\u0451\u0442: " + this.sessionName;
        }
        try {
            boolean bl;
            Path path = NeuralAimModel.getModelPath();
            Files.createDirectories(path, new FileAttribute[0]);
            Path path2 = path.resolve(string + ".csv");
            boolean bl2 = bl = !Files.isRegularFile(path2, new LinkOption[0]) || Files.size(path2) == 0L;
            if (!bl) {
                RotationLogWriter.truncateIncompleteRow(path2);
            }
            this.csvWriter = Files.newBufferedWriter(path2, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (bl) {
                this.csvWriter.write(CSV_HEADER);
            }
            this.sessionFile = path2;
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("[Neuro] \u043d\u0435 \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0434\u0430\u0442\u0430\u0441\u0435\u0442", (Throwable)exception);
            return "\u041d\u0435 \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0444\u0430\u0439\u043b " + string + ".csv";
        }
        this.sessionName = string;
        this.rowsSinceFlush = 0;
        this.targetGraceTicksRemaining = 0;
        this.attackSampleCount = 0;
        this.recordedRowCount = 0;
        this.tickCount = 0;
        this.attackTick = -1;
        this.lastAttackedEntityId = -1;
        this.previousRotationAvailable = false;
        this.attackSamplePending = false;
        this.recording = true;
        RockstarClient.create().getEventBus().registerListeners(this);
        return null;
    }

    private static void truncateIncompleteRow(Path path) {
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);){
            long l = fileChannel.size();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int)Math.min(l, 8192L));
            fileChannel.read(byteBuffer, l - (long)byteBuffer.capacity());
            byte[] byArray = byteBuffer.array();
            for (int i = byArray.length - 1; i >= 0; --i) {
                if (byArray[i] != 10) continue;
                long l2 = l - (long)byArray.length + (long)i + 1L;
                if (l2 < l) {
                    fileChannel.truncate(l2);
                }
                return;
            }
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.warn("[Neuro] \u043d\u0435 \u043f\u043e\u0434\u0440\u0435\u0437\u0430\u0442\u044c \u0445\u0432\u043e\u0441\u0442 \u0434\u0430\u0442\u0430\u0441\u0435\u0442\u0430: {}", (Object)exception.getMessage());
        }
    }

    public String stopRecording() {
        if (!this.recording) {
            return "\u0417\u0430\u043f\u0438\u0441\u044c \u043d\u0435 \u0438\u0434\u0451\u0442.";
        }
        RockstarClient.create().getEventBus().unregisterListeners(this);
        this.recording = false;
        this.closeWriter();
        int n = this.sessionFile == null ? this.recordedRowCount : RotationLogWriter.countRowsInFile(this.sessionFile);
        int n2 = RotationLogWriter.countAllTrainingRows();
        String string = RotationLogWriter.formatRemainingTrainingTime(n2);
        return this.sessionName + ".csv: +" + RotationLogWriter.formatMinutes(this.recordedRowCount) + " \u0437\u0430 \u0441\u0435\u0441\u0441\u0438\u044e, \u0432\u0441\u0435\u0433\u043e " + RotationLogWriter.formatMinutes(n) + " (" + RotationLogWriter.formatTrainingQuality(n) + "), \u0443\u0434\u0430\u0440\u043e\u0432 " + this.attackSampleCount + (String)(string == null ? "" : ". \u0414\u043e \u043e\u0431\u0443\u0447\u0435\u043d\u0438\u044f \u043d\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u0435\u0449\u0451 " + string);
    }

    private LivingEntity findTrainingTarget() {
        LivingEntity class_13092;
        Entity class_12972;
        Vec3d VanillaChestLootTableGenerator = this.minecraftClient.player.getEyePos();
        if (this.lastAttackedEntityId >= 0 && this.tickCount - this.attackTick <= 60 && (class_12972 = this.minecraftClient.world.getEntityById(this.lastAttackedEntityId)) instanceof LivingEntity && (class_13092 = (LivingEntity)class_12972).isAlive() && RotationController.distanceToBox(VanillaChestLootTableGenerator, class_13092.getBoundingBox()) <= 5.0) {
            return class_13092;
        }
        class_13092 = null;
        double d = 5.0;
        for (Entity class_12973 : this.minecraftClient.world.getEntities()) {
            double d2;
            LivingEntity class_13093;
            if (!(class_12973 instanceof LivingEntity) || (class_13093 = (LivingEntity)class_12973) == this.minecraftClient.player || !class_13093.isAlive() || !((d2 = RotationController.distanceToBox(VanillaChestLootTableGenerator, class_13093.getBoundingBox())) <= d) || !this.isTargetVisible(class_13093)) continue;
            d = d2;
            class_13092 = class_13093;
        }
        return class_13092;
    }

    private void writeTrainingRow(boolean bl, float f, float f2, float f3, float f4, LivingEntity class_13092) {
        boolean bl2 = this.attackSamplePending;
        this.attackSamplePending = false;
        if (bl2) {
            ++this.attackSampleCount;
        }
        Vec3d VanillaChestLootTableGenerator = this.minecraftClient.player.getEyePos();
        Box Vec3i = class_13092 == null ? null : class_13092.getBoundingBox();
        Vec3d WallPlayerSkullBlock = Vec3i == null ? Vec3d.ZERO : Vec3i.getCenter().subtract(VanillaChestLootTableGenerator);
        try {
            this.csvWriter.write(String.format(Locale.ROOT, "%d,%.6f,%d,%.4f,%.4f,%.4f,%.4f,%d,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%d,%d,%d,%.1f,%d,%d\n", this.tickCount, Float.valueOf(AimRotationMath.getMouseRotationStep()), bl ? 1 : 0, Float.valueOf(f), Float.valueOf(f2), Float.valueOf(f3), Float.valueOf(f4), class_13092 == null ? 0 : 1, class_13092 == null ? -1 : class_13092.getId(), WallPlayerSkullBlock.x, WallPlayerSkullBlock.y, WallPlayerSkullBlock.z, Vec3i == null ? 0.0 : Vec3i.getLengthX(), Vec3i == null ? 0.0 : Vec3i.getLengthY(), Vec3i == null ? -1.0 : RotationController.distanceToBox(VanillaChestLootTableGenerator, Vec3i), class_13092 != null && this.isTargetVisible(class_13092) ? 1 : 0, Vec3i != null && this.isCrosshairOnTarget(f, f2, Vec3i) ? 1 : 0, bl2 ? 1 : 0, Float.valueOf(class_13092 == null ? -1.0f : class_13092.getHealth()), this.minecraftClient.player.isOnGround() ? 1 : 0, this.minecraftClient.player.isSprinting() ? 1 : 0));
            ++this.recordedRowCount;
            if (++this.rowsSinceFlush >= 256) {
                this.rowsSinceFlush = 0;
                this.csvWriter.flush();
            }
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("[Neuro] \u043d\u0435 \u0437\u0430\u043f\u0438\u0441\u0430\u0442\u044c \u0442\u0438\u043a \u0431\u043e\u044f", (Throwable)exception);
            this.closeWriter();
        }
    }

    private boolean isCrosshairOnTarget(float f, float f2, Box Vec3i) {
        Vec3d VanillaChestLootTableGenerator = this.minecraftClient.player.getEyePos();
        if (Vec3i.contains(VanillaChestLootTableGenerator)) {
            return true;
        }
        double d = Math.toRadians(f);
        double d2 = Math.toRadians(f2);
        double d3 = Math.cos(d2);
        Vec3d WallPlayerSkullBlock = new Vec3d(-Math.sin(d) * d3, -Math.sin(d2), Math.cos(d) * d3);
        return Vec3i.raycast(VanillaChestLootTableGenerator, VanillaChestLootTableGenerator.add(WallPlayerSkullBlock.multiply(5.0))).isPresent();
    }

    private boolean isTargetVisible(LivingEntity class_13092) {
        Box Vec3i = class_13092.getBoundingBox();
        for (double d : TARGET_SAMPLE_X_OFFSETS) {
            for (double d2 : TARGET_SAMPLE_Y_OFFSETS) {
                for (double d3 : TARGET_SAMPLE_Z_OFFSETS) {
                    if (!MathUtils.hasClearLineOfSight(new Vec3d(MathHelper.lerp((double)d, (double)Vec3i.minX, (double)Vec3i.maxX), MathHelper.lerp((double)d2, (double)Vec3i.minY, (double)Vec3i.maxY), MathHelper.lerp((double)d3, (double)Vec3i.minZ, (double)Vec3i.maxZ)))) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private void closeWriter() {
        try {
            if (this.csvWriter != null) {
                this.csvWriter.flush();
                this.csvWriter.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.csvWriter = null;
    }

    public static String formatMinutes(int n) {
        return String.format(Locale.ROOT, "%.1f \u043c\u0438\u043d", (double)n / 1200.0);
    }

    public static String formatRemainingTrainingTime(int n) {
        return n >= 12000 ? null : RotationLogWriter.formatMinutes(12000 - n);
    }

    public static String formatTrainingQuality(int n) {
        if (n < 6000) {
            return "\u043c\u0430\u043b\u043e";
        }
        if (n < 18000) {
            return "\u043c\u0430\u043b\u043e\u0432\u0430\u0442\u043e";
        }
        if (n < 36000) {
            return "\u043d\u043e\u0440\u043c\u0430\u043b\u044c\u043d\u043e";
        }
        return "\u0445\u043e\u0440\u043e\u0448\u043e";
    }

    public static List<String> listTrainingSessions() {
        ArrayList<String> arrayList = new ArrayList<String>();
        try (Stream<Path> stream = Files.list(NeuralAimModel.getModelPath());){
            stream.filter(path -> path.getFileName().toString().endsWith(".csv")).sorted().forEach(path -> {
                int[] nArray = RotationLogWriter.analyzeTrainingFile(path);
                arrayList.add(path.getFileName().toString().replaceFirst("\\.csv$", "") + " \u00b7 " + RotationLogWriter.formatMinutes(nArray[0]) + " \u00b7 " + RotationLogWriter.formatTrainingQuality(nArray[0]) + (String)(nArray[1] > 0 ? " \u00b7 \u00a7c\u0431\u0438\u0442\u044b\u0445 \u0441\u0442\u0440\u043e\u043a " + nArray[1] + "\u00a7r" : ""));
            });
        }
        catch (Exception exception) {
            // empty catch block
        }
        return arrayList;
    }

    public static int countInvalidTrainingRows() {
        int n = 0;
        try (Stream<Path> stream = Files.list(NeuralAimModel.getModelPath());){
            for (Path path2 : stream.filter(path -> path.getFileName().toString().endsWith(".csv")).toList()) {
                n += RotationLogWriter.analyzeTrainingFile(path2)[1];
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return n;
    }

    public static int countSessionRows(String string) {
        Path path = NeuralAimModel.getModelPath().resolve(string + ".csv");
        return Files.isRegularFile(path, new LinkOption[0]) ? RotationLogWriter.countRowsInFile(path) : 0;
    }

    public static int countAllTrainingRows() {
        int n = 0;
        try (Stream<Path> stream = Files.list(NeuralAimModel.getModelPath());){
            for (Path path2 : stream.filter(path -> path.getFileName().toString().endsWith(".csv")).toList()) {
                n += RotationLogWriter.countRowsInFile(path2);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return n;
    }

    private static int countRowsInFile(Path path) {
        return RotationLogWriter.analyzeTrainingFile(path)[0];
    }

    private static int[] analyzeTrainingFile(Path path) {
        int n = 0;
        int n2 = 0;
        try (Stream<String> stream = Files.lines(path);){
            Iterator iterator = stream.iterator();
            while (iterator.hasNext()) {
                String string = (String)iterator.next();
                if (string.isBlank() || string.startsWith("t,")) continue;
                ++n;
                int n3 = 1;
                for (int i = 0; i < string.length(); ++i) {
                    if (string.charAt(i) != ',') continue;
                    ++n3;
                }
                if (n3 == CSV_COLUMN_COUNT) continue;
                ++n2;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new int[]{n, n2};
    }

    @Generated
    public String getSessionName() {
        return this.sessionName;
    }

    @Generated
    public boolean isRecording() {
        return this.recording;
    }

    @Generated
    public int getRecordedRowCount() {
        return this.recordedRowCount;
    }
}

