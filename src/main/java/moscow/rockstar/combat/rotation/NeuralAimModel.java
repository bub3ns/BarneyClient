/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package moscow.rockstar.combat.rotation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;

public final class NeuralAimModel {
    public static final int MODEL_FEATURE_COUNT = 17;
    public static final String DEFAULT_MODEL_NAME = "default";
    private static final float DEFAULT_CONFIDENCE_THRESHOLD = 0.95f;
    private static NeuralAimModel activeModel;
    private static boolean modelLoadInProgress;
    private static String activeModelName;
    private final float[][] GRU_INPUT_WEIGHTS;
    private final float[][] GRU_HIDDEN_WEIGHTS;
    private final float[][] OUTPUT_WEIGHTS;
    private final float[] GRU_INPUT_BIASES;
    private final float[] GRU_HIDDEN_BIASES;
    private final float[] OUTPUT_BIASES;
    private final float[] ERROR_BIAS;
    private final int HIDDEN_SIZE;
    private final int MIX_COMPONENT_COUNT;
    private final int FREEZE_CUT_INDEX;
    private final float LOWER_LIMIT;
    private final float UPPER_LIMIT;

    private NeuralAimModel(JsonObject jsonObject) {
        this.HIDDEN_SIZE = jsonObject.get("hidden").getAsInt();
        this.MIX_COMPONENT_COUNT = jsonObject.get("mix").getAsInt();
        this.FREEZE_CUT_INDEX = jsonObject.has("freezeCut") ? jsonObject.get("freezeCut").getAsInt() : 12;
        JsonArray jsonArray = jsonObject.getAsJsonArray("limits");
        this.LOWER_LIMIT = jsonArray.get(0).getAsFloat();
        this.UPPER_LIMIT = jsonArray.get(1).getAsFloat();
        this.ERROR_BIAS = jsonObject.has("error") ? NeuralAimModel.parseBiasVector(jsonObject.getAsJsonArray("error")) : new float[]{};
        JsonObject jsonObject2 = jsonObject.getAsJsonObject("gru");
        this.GRU_INPUT_WEIGHTS = NeuralAimModel.parseWeightMatrix(jsonObject2.getAsJsonArray("wi"));
        this.GRU_HIDDEN_WEIGHTS = NeuralAimModel.parseWeightMatrix(jsonObject2.getAsJsonArray("wh"));
        this.GRU_INPUT_BIASES = NeuralAimModel.parseBiasVector(jsonObject2.getAsJsonArray("bi"));
        this.GRU_HIDDEN_BIASES = NeuralAimModel.parseBiasVector(jsonObject2.getAsJsonArray("bh"));
        JsonObject jsonObject3 = jsonObject.getAsJsonObject("out");
        this.OUTPUT_WEIGHTS = NeuralAimModel.parseWeightMatrix(jsonObject3.getAsJsonArray("w"));
        this.OUTPUT_BIASES = NeuralAimModel.parseBiasVector(jsonObject3.getAsJsonArray("b"));
    }

    private static float[][] parseWeightMatrix(JsonArray jsonArray) {
        float[][] fArrayArray = new float[jsonArray.size()][];
        for (int i = 0; i < jsonArray.size(); ++i) {
            fArrayArray[i] = NeuralAimModel.parseBiasVector(jsonArray.get(i).getAsJsonArray());
        }
        return fArrayArray;
    }

    private static float[] parseBiasVector(JsonArray jsonArray) {
        float[] fArray = new float[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); ++i) {
            fArray[i] = jsonArray.get(i).getAsFloat();
        }
        return fArray;
    }

    public static Path getModelFilePath() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("Barney").resolve("neuro");
    }

    public static Path getModelPath() {
        return NeuralAimModel.getModelFilePath().resolve("data");
    }

    public static Path resolveModelPath(String string) {
        return NeuralAimModel.getModelFilePath().resolve(string + ".json");
    }

    public static String getActiveModelName() {
        if (activeModelName == null) {
            try {
                Path path = NeuralAimModel.getModelFilePath().resolve("active.txt");
                activeModelName = Files.isRegularFile(path, new LinkOption[0]) ? Files.readString(path).trim() : DEFAULT_MODEL_NAME;
            }
            catch (Exception exception) {
                activeModelName = DEFAULT_MODEL_NAME;
            }
            if (activeModelName.isEmpty()) {
                activeModelName = DEFAULT_MODEL_NAME;
            }
        }
        return activeModelName;
    }

    public static List<String> getFeatureNames() {
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add(DEFAULT_MODEL_NAME);
        try (Stream<Path> stream = Files.list(NeuralAimModel.getModelFilePath());){
            stream.filter(path -> path.getFileName().toString().endsWith(".json")).map(path -> path.getFileName().toString().replaceFirst("\\.json$", "")).filter(string -> !arrayList.contains(string)).sorted().forEach(arrayList::add);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return arrayList;
    }

    public static boolean isModelNameValid(String string) {
        return DEFAULT_MODEL_NAME.equals(string) || Files.isRegularFile(NeuralAimModel.resolveModelPath(string), new LinkOption[0]);
    }

    public static boolean acceptsModelName(String string) {
        if (!NeuralAimModel.isModelNameValid(string)) {
            return false;
        }
        activeModelName = string;
        try {
            Files.createDirectories(NeuralAimModel.getModelFilePath(), new FileAttribute[0]);
            Files.writeString(NeuralAimModel.getModelFilePath().resolve("active.txt"), (CharSequence)string, new OpenOption[0]);
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("[Neuro] \u043d\u0435 \u0437\u0430\u043f\u0438\u0441\u0430\u0442\u044c \u0430\u043a\u0442\u0438\u0432\u043d\u0443\u044e \u043c\u043e\u0434\u0435\u043b\u044c", (Throwable)exception);
        }
        NeuralAimModel.reloadModel();
        return true;
    }

    public static NeuralAimModel getActiveModel() {
        if (!modelLoadInProgress) {
            modelLoadInProgress = true;
            activeModel = NeuralAimModel.loadModel(NeuralAimModel.getActiveModelName());
        }
        return activeModel;
    }

    public static void reloadModel() {
        modelLoadInProgress = false;
        activeModel = null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static NeuralAimModel loadModel(String string) {
        try {
            JsonObject jsonObject;
            Path path = NeuralAimModel.resolveModelPath(string);
            if (Files.isRegularFile(path, new LinkOption[0])) {
                jsonObject = JsonParser.parseString((String)Files.readString(path)).getAsJsonObject();
            } else {
                if (!DEFAULT_MODEL_NAME.equals(string)) return null;
                try (InputStream inputStream = NeuralAimModel.class.getClassLoader().getResourceAsStream("assets/rockstar/neuro/default.json");){
                    if (inputStream == null) {
                        NeuralAimModel neuralAimModel = null;
                        return neuralAimModel;
                    }
                    jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                }
            }
            if (jsonObject.get("features").getAsInt() != 17) return null;
            if (!jsonObject.has("mix")) {
                return null;
            }
            if (!jsonObject.has("units")) return null;
            if ("deg".equals(jsonObject.get("units").getAsString())) return new NeuralAimModel(jsonObject);
            return null;
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("[Neuro] \u043c\u043e\u0434\u0435\u043b\u044c " + string + " \u043d\u0435 \u0447\u0438\u0442\u0430\u0435\u0442\u0441\u044f", (Throwable)exception);
            return null;
        }
    }

    public int getHiddenSize() {
        return this.FREEZE_CUT_INDEX;
    }

    public float[] getOutputBiases() {
        return new float[this.HIDDEN_SIZE];
    }

    public float[] addVectors(float[] fArray, float[] fArray2) {
        float f;
        int n;
        float[] fArray3 = new float[3 * this.HIDDEN_SIZE];
        float[] fArray4 = new float[3 * this.HIDDEN_SIZE];
        for (n = 0; n < 3 * this.HIDDEN_SIZE; ++n) {
            int n2;
            f = this.GRU_INPUT_BIASES[n];
            float[] fArray5 = this.GRU_INPUT_WEIGHTS[n];
            for (n2 = 0; n2 < fArray5.length; ++n2) {
                f += fArray5[n2] * fArray[n2];
            }
            fArray3[n] = f;
            f = this.GRU_HIDDEN_BIASES[n];
            fArray5 = this.GRU_HIDDEN_WEIGHTS[n];
            for (n2 = 0; n2 < fArray5.length; ++n2) {
                f += fArray5[n2] * fArray2[n2];
            }
            fArray4[n] = f;
        }
        for (n = 0; n < this.HIDDEN_SIZE; ++n) {
            f = NeuralAimModel.clampOutput(fArray3[n] + fArray4[n]);
            float f2 = NeuralAimModel.clampOutput(fArray3[this.HIDDEN_SIZE + n] + fArray4[this.HIDDEN_SIZE + n]);
            float f3 = (float)Math.tanh(fArray3[2 * this.HIDDEN_SIZE + n] + f * fArray4[2 * this.HIDDEN_SIZE + n]);
            fArray2[n] = (1.0f - f2) * f3 + f2 * fArray2[n];
        }
        float[] fArray6 = new float[this.OUTPUT_WEIGHTS.length];
        for (int i = 0; i < this.OUTPUT_WEIGHTS.length; ++i) {
            float f4 = this.OUTPUT_BIASES[i];
            float[] fArray7 = this.OUTPUT_WEIGHTS[i];
            for (int j = 0; j < fArray7.length; ++j) {
                f4 += fArray7[j] * fArray2[j];
            }
            fArray6[i] = f4;
        }
        return fArray6;
    }

    public int argmax(float[] fArray, float f) {
        float f2 = fArray[1];
        for (int i = 1; i < this.MIX_COMPONENT_COUNT; ++i) {
            f2 = Math.max(f2, fArray[1 + 6 * i]);
        }
        float f3 = 0.0f;
        for (int i = 0; i < this.MIX_COMPONENT_COUNT; ++i) {
            f3 += (float)Math.exp(fArray[1 + 6 * i] - f2);
        }
        float f4 = f * f3;
        for (int i = 0; i < this.MIX_COMPONENT_COUNT; ++i) {
            if (!((f4 -= (float)Math.exp(fArray[1 + 6 * i] - f2)) <= 0.0f)) continue;
            return i;
        }
        return this.MIX_COMPONENT_COUNT - 1;
    }

    public float activate(float f) {
        if (this.ERROR_BIAS.length == 0) {
            return 0.0f;
        }
        float f2 = Math.max(0.0f, Math.min(1.0f, f)) * (float)(this.ERROR_BIAS.length - 1);
        int n = (int)f2;
        return n >= this.ERROR_BIAS.length - 1 ? this.ERROR_BIAS[this.ERROR_BIAS.length - 1] : this.ERROR_BIAS[n] + (this.ERROR_BIAS[n + 1] - this.ERROR_BIAS[n]) * (f2 - (float)n);
    }

    public float blendPrediction(float f, float f2, boolean bl, float f3, float f4) {
        float f5 = (float)Math.exp(Math.max(-4.0f, Math.min(1.5f, f2)));
        float f6 = Math.max(-8.0f, Math.min(8.0f, f + f4 * f5 * f3));
        float f7 = bl ? this.LOWER_LIMIT : this.UPPER_LIMIT;
        return Math.max(-f7, Math.min(f7, (float)Math.sinh(f6)));
    }

    public static float normalizeInput(float f) {
        return NeuralAimModel.clampOutput(f);
    }

    public static float denormalizeOutput(float f) {
        return (float)Math.tanh(f) * 0.95f;
    }

    private static float clampOutput(float f) {
        return 1.0f / (1.0f + (float)Math.exp(-f));
    }
}

