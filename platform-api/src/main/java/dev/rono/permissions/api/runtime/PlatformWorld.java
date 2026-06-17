package dev.rono.permissions.api.runtime;

/**
 * Permission realm / world namespace known to the host.
 *
 * <p>On game servers this is typically a loaded dimension name; on proxies a backend server id.</p>
 */
public interface PlatformWorld {

    String name();
}
