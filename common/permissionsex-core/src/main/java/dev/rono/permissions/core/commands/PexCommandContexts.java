package dev.rono.permissions.core.commands;

final class PexCommandContexts {
    private PexCommandContexts() {}

    static String displayRealm(String realm) {
        return realm == null ? "global" : realm;
    }
}
