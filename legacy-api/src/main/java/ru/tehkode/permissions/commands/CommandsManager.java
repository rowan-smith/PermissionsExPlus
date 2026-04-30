package ru.tehkode.permissions.commands;

/**
 * Legacy command façade type retained for classpath compatibility with classic PermissionsEx integrations.
 *
 * <p>PEXPlus routes commands through Cloud; this exists so {@code ru.tehkode.permissions.commands.CommandsManager}
 * continues to resolve against {@code permissionsex-legacy-api}.</p>
 */
public interface CommandsManager {

    enum Noop implements CommandsManager {
        INSTANCE
    }

    /** @deprecated Compatibility no-op. */
    @Deprecated
    static CommandsManager getInstance() {
        return Noop.INSTANCE;
    }
}
