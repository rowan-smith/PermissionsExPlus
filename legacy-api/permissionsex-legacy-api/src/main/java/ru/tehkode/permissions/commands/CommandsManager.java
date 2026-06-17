package ru.tehkode.permissions.commands;

/**
 * Legacy command façade type retained for classpath compatibility with classic PermissionsEx integrations.
 *
 * <p>PEXPlus routes commands through Cloud; this exists so {@code ru.tehkode.permissions.commands.CommandsManager}
 * continues to resolve against {@code permissionsex-legacy-api}.</p>
 */
public interface CommandsManager {

    /**
     * No-op implementation used when the legacy command manager is not active.
     */
    enum Noop implements CommandsManager {
        /** Singleton no-op instance. */
        INSTANCE
    }

    /**
     * Returns the legacy command manager singleton.
     *
     * @return the no-op {@link Noop#INSTANCE}; never {@code null}
     * @deprecated Compatibility no-op; PEXPlus does not expose a live {@code CommandsManager}.
     */
    @Deprecated(since = "1.24.x")    static CommandsManager getInstance() {
        return Noop.INSTANCE;
    }
}
