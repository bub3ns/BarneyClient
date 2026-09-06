package moscow.rockstar.api.scripts;

/** Raised when a script asks for an invalid client configuration value. */
public final class ScriptConfigurationException extends IllegalArgumentException {
    public ScriptConfigurationException(String message) {
        super(message);
    }

    public ScriptConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
