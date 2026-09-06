/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 */
package moscow.rockstar.api.commands;
import moscow.rockstar.ui.localization.Localization;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.combat.RotationController;
import moscow.rockstar.combat.rotation.NeuralAimModel;
import moscow.rockstar.combat.rotation.RotationLogWriter;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.scripts.python.TrainerRuntime;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;

public class NeuroCommandService
implements ClientAccess {
    private static final int MIN_TRAINING_EPOCHS = 2;
    private static volatile boolean trainingInProgress;

    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("neuro")
            .aliases("nr", "neurorotation")
            .description("commands.neuro.description")
            .argument("action", argument -> argument
                .optional()
                .validator(PluginResolver::resolveValue)
                .choices("record", "stop", "data", "train", "load", "list", "dir", "why"))
            .argument("name", argument -> argument.optional().validator(PluginResolver::resolveValue))
            .argument("epochs", argument -> argument
                .optional()
                .validator(PluginResolver::resolveValue)
                .choices("auto", "400", "800"))
            .handler(this::executeNeuroCommand)
            .build();
    }

    private void executeNeuroCommand(DispatchContext dispatchContext) {
        String string = (String)dispatchContext.getArguments().get(0);
        String string2 = (String)dispatchContext.getArguments().get(1);
        String string3 = (String)dispatchContext.getArguments().get(2);
        switch (string == null ? "status" : string.toLowerCase(Locale.ROOT)) {
            case "status": {
                this.showModelStatus();
                break;
            }
            case "list": {
                this.listModels();
                break;
            }
            case "load": {
                this.loadModel(string2);
                break;
            }
            case "dir": {
                this.openModelDirectory();
                break;
            }
            case "train": {
                this.startTraining(string2, string3);
                break;
            }
            case "record": 
            case "rec": {
                this.startRecording(string2);
                break;
            }
            case "stop": {
                this.stopTraining();
                break;
            }
            case "data": {
                this.listTrainingSessions();
                break;
            }
            case "why": {
                this.showMissReasons();
                break;
            }
            default: {
                NeuroCommandService.sendErrorMessage(Localization.translate("commands.neuro.usage"));
            }
        }
    }

    private void showModelStatus() {
        NeuralAimModel neuralAimModel = NeuralAimModel.getActiveModel();
        NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.model_status", NeuralAimModel.getActiveModelName(), Localization.translate(neuralAimModel == null ? "commands.neuro.model_failed" : "commands.neuro.model_ready"), NeuralAimModel.getFeatureNames().size()));
        int n = RotationLogWriter.countAllTrainingRows();
        RotationLogWriter rotationLogWriter = this.getRotationLogger();
        String string = RotationLogWriter.formatRemainingTrainingTime(n);
        NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.recorded", RotationLogWriter.formatMinutes(n), RotationLogWriter.formatTrainingQuality(n)) + (String)(rotationLogWriter != null && rotationLogWriter.isRecording() ? " " + Localization.translateFormatted("commands.neuro.recording_now", rotationLogWriter.getSessionName()) : ""));
        if (string != null) {
            NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.need_more", string));
        }
        NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.trainer", this.getTrainerStatus()));
    }

    private String getTrainerStatus() {
        String string;
        if (TrainerRuntime.findPythonExecutable() == null) {
            return Localization.translate("commands.neuro.trainer.no_python");
        }
        if (TrainerRuntime.isEnvironmentReady()) {
            return Localization.translate("commands.neuro.trainer.ready");
        }
        String string2 = string = TrainerRuntime.isNumpyAvailable() ? "torch" : Localization.translate("commands.neuro.deps_both");
        if (TrainerRuntime.isInstallingDependencies()) {
            return Localization.translateFormatted("commands.neuro.trainer.downloading", string);
        }
        String string3 = TrainerRuntime.getInstallationError();
        return Localization.translateFormatted("commands.neuro.trainer.missing", string, string3 == null ? "" : ": " + string3);
    }

    private void listModels() {
        List<String> list = NeuralAimModel.getFeatureNames();
        NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.models_count", list.size()));
        for (String string : list) {
            boolean bl = string.equals(NeuralAimModel.getActiveModelName());
            boolean bl2 = !Files.isRegularFile(NeuralAimModel.resolveModelPath(string), new LinkOption[0]);
            NeuroCommandService.sendInfoMessage((bl ? "\u00a7b > " : "\u00a77 \u00b7 ") + string + (bl2 ? " " + Localization.translate("commands.neuro.model_builtin") : "") + (bl ? " " + Localization.translate("commands.neuro.model_active") : "") + "\u00a7r");
        }
    }

    private void loadModel(String string) {
        if (NeuroCommandService.isBlank(string)) {
            NeuroCommandService.sendErrorMessage(Localization.translate("commands.neuro.usage_load"));
            return;
        }
        if (!NeuralAimModel.isModelNameValid(string)) {
            NeuroCommandService.sendErrorMessage(Localization.translateFormatted("commands.neuro.model_missing", string));
            return;
        }
        NeuralAimModel.acceptsModelName(string);
        NeuroCommandService.sendInfoMessage(NeuralAimModel.getActiveModel() == null ? Localization.translateFormatted("commands.neuro.model_unreadable", string) : Localization.translateFormatted("commands.neuro.model_loaded", string));
    }

    private void openModelDirectory() {
        Path path = NeuralAimModel.getModelFilePath();
        try {
            Files.createDirectories(path, new FileAttribute[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (NeuroCommandService.minecraftClient.keyboard != null) {
            NeuroCommandService.minecraftClient.keyboard.setClipboard(path.toString());
        }
        if (this.openDirectory(path)) {
            NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.dir_opened", String.valueOf(path)));
        } else {
            NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.dir_path", String.valueOf(path)));
        }
    }

    private boolean openDirectory(Path path) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(path.toFile());
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            new ProcessBuilder("explorer.exe", path.toString()).start();
            return true;
        }
        catch (Exception exception) {
            return false;
        }
    }

    private void startRecording(String string) {
        RotationLogWriter rotationLogWriter = this.getRotationLogger();
        if (rotationLogWriter == null) {
            NeuroCommandService.sendErrorMessage(Localization.translate("commands.neuro.no_aura"));
            return;
        }
        if (rotationLogWriter.isRecording()) {
            NeuroCommandService.sendErrorMessage(Localization.translateFormatted("commands.neuro.already_recording", rotationLogWriter.getSessionName()));
            return;
        }
        if (NeuroCommandService.isBlank(string)) {
            NeuroCommandService.sendErrorMessage(Localization.translate("commands.neuro.usage_record"));
            return;
        }
        String string2 = string.replaceAll("[^\\p{L}\\p{N}_.-]", "");
        if (string2.isEmpty()) {
            NeuroCommandService.sendErrorMessage(Localization.translate("commands.neuro.bad_dataset_name"));
            return;
        }
        int n = RotationLogWriter.countSessionRows(string2);
        String string3 = rotationLogWriter.startRecording(string2);
        if (string3 != null) {
            NeuroCommandService.sendErrorMessage(string3);
            return;
        }
        NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.recording_started", string2, n > 0 ? " " + Localization.translateFormatted("commands.neuro.recording_append", RotationLogWriter.formatMinutes(n)) : ""));
        String string4 = RotationLogWriter.formatRemainingTrainingTime(RotationLogWriter.countAllTrainingRows());
        if (string4 != null) {
            NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.need_more_combat", string4));
        }
    }

    private void showMissReasons() {
        Aura aura = RockstarClient.create().getModuleRegistry().getModule(Aura.class);
        if (aura == null) {
            NeuroCommandService.sendErrorMessage(Localization.translate("commands.neuro.no_aura"));
            return;
        }
        Map<String, Integer> map = aura.getAttackStatistics();
        if (map.isEmpty()) {
            NeuroCommandService.sendInfoMessage(Localization.translate("commands.neuro.why_empty"));
            return;
        }
        NeuroCommandService.sendInfoMessage(Localization.translate("commands.neuro.why_header"));
        map.entrySet().stream().sorted((entry, entry2) -> (Integer)entry2.getValue() - (Integer)entry.getValue()).limit(8L).forEach(entry -> NeuroCommandService.sendInfoMessage("  " + (String)entry.getKey() + ": " + String.valueOf(entry.getValue())));
        map.clear();
        NeuroCommandService.sendInfoMessage(Localization.translate("commands.neuro.why_reset"));
    }

    private void stopTraining() {
        RotationLogWriter rotationLogWriter = this.getRotationLogger();
        NeuroCommandService.sendInfoMessage(rotationLogWriter == null ? Localization.translate("commands.neuro.no_aura") : rotationLogWriter.stopRecording());
    }

    private void listTrainingSessions() {
        List<String> list = RotationLogWriter.listTrainingSessions();
        if (list.isEmpty()) {
            NeuroCommandService.sendInfoMessage(Localization.translate("commands.neuro.no_datasets"));
            return;
        }
        int n = RotationLogWriter.countAllTrainingRows();
        NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.datasets", list.size(), RotationLogWriter.formatMinutes(n), RotationLogWriter.formatTrainingQuality(n)));
        for (String string2 : list) {
            NeuroCommandService.sendInfoMessage("\u00a78 \u00b7 \u00a77" + string2 + "\u00a7r");
        }
        int n2 = RotationLogWriter.countInvalidTrainingRows();
        if (n2 > 0) {
            NeuroCommandService.sendInfoMessage(Localization.translate("commands.neuro.broken_rows"));
        }
        String remaining = RotationLogWriter.formatRemainingTrainingTime(n);
        if (remaining != null) {
            NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.need_more", remaining));
        }
    }

    private void startTraining(String string, String string2) {
        String string3 = null;
        String string4;
        if (trainingInProgress) {
            NeuroCommandService.sendErrorMessage(Localization.translate("commands.neuro.already_training"));
            return;
        }
        String string5 = string4 = NeuroCommandService.isBlank(string) ? NeuralAimModel.getActiveModelName() : string;
        String string6 = string2 == null ? null : (string2.equalsIgnoreCase("auto") ? "0" : (string3 = string2.matches("\\d+") ? string2 : null));
        if (string2 != null && string3 == null) {
            NeuroCommandService.sendErrorMessage(Localization.translate("commands.neuro.epochs_invalid"));
            return;
        }
        File file = TrainerRuntime.findPythonExecutable();
        if (file == null) {
            NeuroCommandService.sendErrorMessage(Localization.translate("commands.neuro.no_python"));
            NeuroCommandService.sendInfoMessage(Localization.translate("commands.neuro.no_python_hint"));
            return;
        }
        if (!TrainerRuntime.isEnvironmentReady()) {
            if (TrainerRuntime.isInstallingDependencies()) {
                NeuroCommandService.sendErrorMessage(Localization.translate("commands.neuro.deps_downloading"));
            } else {
                NeuroCommandService.sendErrorMessage(Localization.translateFormatted("commands.neuro.deps_failed", TrainerRuntime.getInstallationError() == null ? "" : ": " + TrainerRuntime.getInstallationError()));
                NeuroCommandService.sendInfoMessage(Localization.translate("commands.neuro.deps_failed_hint"));
            }
            return;
        }
        int n = RotationLogWriter.countAllTrainingRows();
        String string7 = RotationLogWriter.formatRemainingTrainingTime(n);
        if (string7 != null) {
            NeuroCommandService.sendErrorMessage(Localization.translateFormatted("commands.neuro.not_enough_data", RotationLogWriter.formatMinutes(n), RotationLogWriter.formatMinutes(12000)));
            NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.record_more", string7));
            return;
        }
        trainingInProgress = true;
        NeuroCommandService.sendInfoMessage(Localization.translateFormatted("commands.neuro.training_started", string4, "0".equals(string3) ? Localization.translate("commands.neuro.epochs_auto") : Localization.translateFormatted("commands.neuro.epochs_count", string3 == null ? "400" : string3)));
        final String modelName = string4;
        final String epochCount = string3;
        Thread thread = new Thread(() -> this.runTrainingProcess(file, NeuralAimModel.getModelPath(), NeuralAimModel.resolveModelPath(modelName), modelName, epochCount), "neuro-train");
        thread.setDaemon(true);
        thread.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void runTrainingProcess(File file, Path path, Path path2, String string, String string2) {
        try {
            Files.createDirectories(path2.getParent(), new FileAttribute[0]);
            Process process = TrainerRuntime.launchTrainingProcess(file, path, path2, string2);
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                String string3;
                while ((string3 = bufferedReader.readLine()) != null) {
                    String string4 = string3.strip();
                    if (string4.isEmpty()) continue;
                    if (string4.startsWith("!! ")) {
                        this.scheduleInfoMessage("\u00a7c" + string4.substring(3) + "\u00a7r");
                        continue;
                    }
                    if (string4.startsWith("! ")) {
                        this.scheduleInfoMessage("\u00a7e" + string4.substring(2) + "\u00a7r");
                        continue;
                    }
                    this.scheduleInfoMessage("\u00a77" + string4 + "\u00a7r");
                }
            }
            int n = process.waitFor();
            if (n == 2) {
                return;
            }
            if (n != 0) {
                this.scheduleInfoMessage(Localization.translateFormatted("commands.neuro.trainer_crashed", n));
                return;
            }
            this.scheduleInfoMessage(Localization.translateFormatted("commands.neuro.training_done", string));
            minecraftClient.execute(() -> NeuralAimModel.acceptsModelName(string));
        }
        catch (Exception exception) {
            this.scheduleInfoMessage(Localization.translateFormatted("commands.neuro.training_failed", exception.getMessage()));
        }
        finally {
            trainingInProgress = false;
        }
    }

    private void scheduleInfoMessage(String string) {
        minecraftClient.execute(() -> NeuroCommandService.sendInfoMessage(string));
    }

    private RotationLogWriter getRotationLogger() {
        RotationController rotationController = this.getAuraRotationController();
        return rotationController == null ? null : rotationController.getRotationLogger();
    }

    private RotationController getAuraRotationController() {
        Aura aura = RockstarClient.create().getModuleRegistry().getModule(Aura.class);
        return aura == null ? null : aura.getRotationController();
    }

    private static boolean isBlank(String string) {
        return string == null || string.isBlank();
    }

    private static void sendInfoMessage(String string) {
        Notification.info(Text.of((String)string));
    }

    private static void sendErrorMessage(String string) {
        Notification.error(Text.of((String)string));
    }
}
