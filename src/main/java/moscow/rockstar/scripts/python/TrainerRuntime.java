/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.scripts.python;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import moscow.rockstar.combat.rotation.NeuralAimModel;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;

public final class TrainerRuntime {
    private static final String TRAINER_RESOURCE_PATH = "assets/rockstar/neuro/trainer/";
    private static final String COMMON_SCRIPT_NAME = "common.py";
    private static final String AURA_TRAINING_SCRIPT_NAME = "train_aura.py";
    private static final String PYTORCH_INDEX_URL = "https://download.pytorch.org/whl/cpu";
    private static final String PYPI_INDEX_URL = "https://pypi.org/simple";
    private static volatile boolean setupStarted;
    private static volatile boolean setupInProgress;
    private static volatile String installationError;

    private TrainerRuntime() {
    }

    public static void initializeTrainerEnvironment() {
        if (setupStarted) {
            return;
        }
        setupStarted = true;
        Thread thread = new Thread(TrainerRuntime::prepareTrainerEnvironment, "Neuro-Trainer-Setup");
        thread.setDaemon(true);
        thread.start();
    }

    private static void prepareTrainerEnvironment() {
        File file = TrainerRuntime.findPythonExecutable();
        if (file == null) {
            return;
        }
        if (TrainerRuntime.isEnvironmentReady()) {
            RockstarClient.LOGGER.info("[Neuro] \u0442\u0440\u0435\u043d\u0435\u0440 \u0433\u043e\u0442\u043e\u0432: numpy \u0438 torch \u043d\u0430 \u043c\u0435\u0441\u0442\u0435");
            return;
        }
        TrainerRuntime.installTrainerDependencies(file);
    }

    private static void installTrainerDependencies(File file) {
        setupInProgress = true;
        try {
            if (!TrainerRuntime.isNumpyAvailable() && !TrainerRuntime.installPythonPackages(file, "numpy")) {
                return;
            }
            if (!TrainerRuntime.isTorchAvailable()) {
                RockstarClient.LOGGER.info("[Neuro] \u043a\u0430\u0447\u0430\u044e torch \u0434\u043b\u044f \u0442\u0440\u0435\u043d\u0435\u0440\u0430 (~200 \u041c\u0411, \u0440\u0430\u0437\u043e\u0432\u043e)");
                if (!TrainerRuntime.installPythonPackages(file, "torch", "--index-url", PYTORCH_INDEX_URL, "--extra-index-url", PYPI_INDEX_URL)) {
                    return;
                }
            }
            if (TrainerRuntime.isEnvironmentReady()) {
                RockstarClient.LOGGER.info("[Neuro] \u0437\u0430\u0432\u0438\u0441\u0438\u043c\u043e\u0441\u0442\u0438 \u0442\u0440\u0435\u043d\u0435\u0440\u0430 \u043d\u0430 \u043c\u0435\u0441\u0442\u0435, \u043e\u0431\u0443\u0447\u0435\u043d\u0438\u0435 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e");
                installationError = null;
            }
        }
        finally {
            setupInProgress = false;
        }
    }

    private static boolean installPythonPackages(File file, String ... stringArray) {
        ArrayList<String> arrayList = new ArrayList<String>(List.of(file.getAbsolutePath(), "-m", "pip", "install"));
        arrayList.addAll(List.of(stringArray));
        arrayList.add("--no-warn-script-location");
        arrayList.add("--disable-pip-version-check");
        try {
            Process process = new ProcessBuilder(arrayList).redirectErrorStream(true).start();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                String string;
                while ((string = bufferedReader.readLine()) != null) {
                    if (string.isBlank()) continue;
                    RockstarClient.LOGGER.info("[Neuro][pip] {}", (Object)string.strip());
                }
            }
            int n = process.waitFor();
            if (n == 0) {
                return true;
            }
            installationError = "pip " + stringArray[0] + " \u0432\u0435\u0440\u043d\u0443\u043b " + n;
            RockstarClient.LOGGER.warn("[Neuro] {} \u043d\u0435 \u043f\u043e\u0441\u0442\u0430\u0432\u0438\u043b\u0441\u044f (\u043a\u043e\u0434 {})", (Object)stringArray[0], (Object)n);
        }
        catch (Exception exception) {
            installationError = exception.getMessage();
            RockstarClient.LOGGER.error("[Neuro] \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0430 {} \u0441\u043e\u0440\u0432\u0430\u043b\u0430\u0441\u044c", (Object)stringArray[0], (Object)exception);
        }
        return false;
    }

    public static Process launchTrainingProcess(File file, Path path, Path path2, String string) throws Exception {
        String string2 = TrainerRuntime.buildTrainingScript();
        ArrayList<String> arrayList = new ArrayList<String>(List.of(file.getAbsolutePath(), "-u", "-", "--data", path.toString(), "--out", path2.toString()));
        if (string != null) {
            arrayList.add("--epochs");
            arrayList.add(string);
        }
        ProcessBuilder processBuilder = new ProcessBuilder(arrayList).redirectErrorStream(true);
        processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
        processBuilder.environment().put("PYTHONDONTWRITEBYTECODE", "1");
        Process process = processBuilder.start();
        try (OutputStream outputStream = process.getOutputStream();){
            outputStream.write(string2.getBytes(StandardCharsets.UTF_8));
        }
        return process;
    }

    private static String buildTrainingScript() {
        String string = TrainerRuntime.encodeBase64(TrainerRuntime.loadTrainingSource(COMMON_SCRIPT_NAME));
        String string2 = TrainerRuntime.encodeBase64(TrainerRuntime.loadTrainingSource(AURA_TRAINING_SCRIPT_NAME));
        String string3 = TrainerRuntime.getTrainerDirectory().toString().replace("\\", "\\\\");
        return "import base64, sys, types\n__rs_home = \"%s\"\n__rs_common = types.ModuleType(\"common\")\n__rs_common.__file__ = __rs_home + \"/common.py\"\nexec(compile(base64.b64decode(\"%s\").decode(\"utf-8\"), \"common.py\", \"exec\"),\n     __rs_common.__dict__)\nsys.modules[\"common\"] = __rs_common\n__rs_globals = {\"__name__\": \"__main__\", \"__file__\": __rs_home + \"/train_aura.py\"}\nexec(compile(base64.b64decode(\"%s\").decode(\"utf-8\"), \"train_aura.py\", \"exec\"),\n     __rs_globals)\n".formatted(string3, string, string2);
    }

    private static String loadTrainingSource(String string) {
        Path repositorySource = MinecraftClient.getInstance().runDirectory.toPath().resolve("../tools/rotation-model/" + string).normalize();
        try {
            if (Files.isRegularFile(repositorySource, new LinkOption[0])) {
                return Files.readString(repositorySource, StandardCharsets.UTF_8);
            }
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.warn("[Neuro] \u043d\u0435 \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u0442\u0440\u0435\u043d\u0435\u0440 \u0438\u0437 \u0440\u0435\u043f\u043e\u0437\u0438\u0442\u043e\u0440\u0438\u044f: {}", (Object)exception.getMessage());
        }
        try (InputStream inputStream = TrainerRuntime.class.getClassLoader().getResourceAsStream(TRAINER_RESOURCE_PATH + string)) {
            if (inputStream == null) {
                throw new IllegalStateException("\u0432 jar \u043d\u0435\u0442 " + string);
            }
            // The bundled trainer sources are plain UTF-8 files. Keep the
            // source-loading path intact, but do not decrypt executable text.
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (Exception exception) {
            throw new IllegalStateException("\u0442\u0440\u0435\u043d\u0435\u0440 \u043d\u0435 \u0447\u0438\u0442\u0430\u0435\u0442\u0441\u044f: " + exception.getMessage(), exception);
        }
    }

    private static String encodeBase64(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isInstallingDependencies() {
        return setupInProgress;
    }

    public static String getInstallationError() {
        return installationError;
    }

    public static boolean isEnvironmentReady() {
        return TrainerRuntime.isNumpyAvailable() && TrainerRuntime.isTorchAvailable();
    }

    public static boolean isNumpyAvailable() {
        return TrainerRuntime.isPythonPackageAvailable("numpy");
    }

    public static boolean isTorchAvailable() {
        return TrainerRuntime.isPythonPackageAvailable("torch");
    }

    private static boolean isPythonPackageAvailable(String string) {
        File file = PythonRuntime.getEnvironmentDirectory();
        return new File(file, "Lib/site-packages/" + string + "/__init__.py").isFile() || new File(file, "lib/python3.11/site-packages/" + string + "/__init__.py").isFile();
    }

    public static Path getTrainerDirectory() {
        return NeuralAimModel.getModelFilePath().resolve("trainer");
    }

    public static File findPythonExecutable() {
        File file = PythonRuntime.getEnvironmentDirectory();
        File file2 = new File(file, "python.exe");
        if (file2.isFile()) {
            return file2;
        }
        File file3 = new File(file, "bin/python3");
        return file3.isFile() ? file3 : null;
    }
}
