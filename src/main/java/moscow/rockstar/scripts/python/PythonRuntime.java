package moscow.rockstar.scripts.python;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import jep.SharedInterpreter;
import moscow.rockstar.core.ClientPaths;

/** Owns the single JEP interpreter used by user-authored Rockstar scripts. */
public final class PythonRuntime {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Barney-Python");
        thread.setDaemon(true);
        return thread;
    });
    private static volatile SharedInterpreter interpreter;
    private static volatile Throwable initializationFailure;
    private static volatile boolean initialized;

    private PythonRuntime() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            Future<?> initialization = EXECUTOR.submit(() -> interpreter = new SharedInterpreter());
            initialization.get();
        } catch (Throwable throwable) {
            initializationFailure = throwable.getCause() != null ? throwable.getCause() : throwable;
        }
    }

    public static boolean isAvailable() {
        initialize();
        return interpreter != null;
    }

    public static SharedInterpreter getInterpreter() {
        initialize();
        return interpreter;
    }

    public static Throwable getInitializationFailure() {
        initialize();
        return initializationFailure;
    }

    public static void execute(Runnable task) {
        if (!isAvailable()) {
            return;
        }
        EXECUTOR.execute(task);
    }

    public static File getEnvironmentDirectory() {
        return ClientPaths.resolve("Barney", "runtime", "python");
    }
}
