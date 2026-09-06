package moscow.rockstar.modules;

public final class ModuleNotFoundException extends RuntimeException {
    public ModuleNotFoundException(String moduleName) {
        super("Unknown module: " + moduleName);
    }
}
