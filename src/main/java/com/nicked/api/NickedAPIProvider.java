package com.nicked.api;

/**
 * Static accessor for the {@link NickedAPI} singleton.
 *
 * <p>The instance is set by the Nicked plugin during {@code onEnable()}
 * and cleared during {@code onDisable()}. External plugins should obtain
 * the instance once and store it, or check for null on each access if
 * they support hot-reload scenarios.</p>
 */
public final class NickedAPIProvider {

    private static NickedAPI instance;

    private NickedAPIProvider() {
        throw new UnsupportedOperationException("NickedAPIProvider is a static utility class");
    }

    /**
     * Returns the active {@link NickedAPI} instance.
     *
     * @return the API instance
     * @throws IllegalStateException if called before the Nicked plugin has been enabled
     */
    public static NickedAPI getAPI() {
        if (instance == null) {
            throw new IllegalStateException("NickedAPI is not available. Is the Nicked plugin enabled?");
        }
        return instance;
    }

    /**
     * Returns the API instance, or {@code null} if the plugin is not enabled.
     *
     * @return the API instance or null
     */
    public static NickedAPI getAPIOrNull() {
        return instance;
    }

    /**
     * Sets the singleton instance. Called by the Nicked plugin internals only.
     *
     * @param api the implementation to register, or {@code null} to clear
     */
    public static void setInstance(NickedAPI api) {
        instance = api;
    }
}
