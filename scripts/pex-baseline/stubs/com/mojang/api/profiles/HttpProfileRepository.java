package com.mojang.api.profiles;

/** Compile-only stand-in for Mojang's AccountsClient, which is no longer published. */
public class HttpProfileRepository implements ProfileRepository {
    public HttpProfileRepository(String agent) {
    }

    @Override
    public Profile[] findProfilesByNames(String... names) {
        throw new UnsupportedOperationException();
    }
}
