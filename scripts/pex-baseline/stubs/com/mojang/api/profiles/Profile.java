package com.mojang.api.profiles;

/** Compile-only stand-in for Mojang's AccountsClient, which is no longer published. */
public class Profile {
    public String getId() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        throw new UnsupportedOperationException();
    }
}
