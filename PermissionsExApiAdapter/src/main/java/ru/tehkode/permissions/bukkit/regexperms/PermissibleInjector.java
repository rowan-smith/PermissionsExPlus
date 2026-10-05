package ru.tehkode.permissions.bukkit.regexperms;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

/**
 * ABI shell. Injection helpers are unused; Plus owns Bukkit attachment wiring.
 */
public abstract class PermissibleInjector {
    protected final String clazzName;
    protected final String fieldName;
    protected final boolean copyValues;

    public PermissibleInjector(String clazzName, String fieldName, boolean copyValues) {
        this.clazzName = clazzName;
        this.fieldName = fieldName;
        this.copyValues = copyValues;
    }

    public Permissible inject(Player player, Permissible permissible) throws NoSuchFieldException, IllegalAccessException {
        return null;
    }

    public Permissible getPermissible(Player player) throws NoSuchFieldException, IllegalAccessException {
        return null;
    }

    public abstract boolean isApplicable(Player player);

    public static class ServerNamePermissibleInjector extends PermissibleInjector {
        private final String serverName;

        public ServerNamePermissibleInjector(String clazz, String field, boolean copyValues, String serverName) {
            super(clazz, field, copyValues);
            this.serverName = serverName;
        }

        @Override
        public boolean isApplicable(Player player) {
            return false;
        }
    }

    public static class ClassPresencePermissibleInjector extends PermissibleInjector {
        public ClassPresencePermissibleInjector(String clazzName, String fieldName, boolean copyValues) {
            super(clazzName, fieldName, copyValues);
        }

        @Override
        public boolean isApplicable(Player player) {
            return false;
        }
    }

    public static class ClassNameRegexPermissibleInjector extends PermissibleInjector {
        private final String regex;

        public ClassNameRegexPermissibleInjector(String clazz, String field, boolean copyValues, String regex) {
            super(clazz, field, copyValues);
            this.regex = regex;
        }

        @Override
        public boolean isApplicable(Player player) {
            return false;
        }
    }
}
