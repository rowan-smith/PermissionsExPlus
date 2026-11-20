package com.mojang.api.profiles;

/** Compile-only stand-in for Mojang's AccountsClient, which is no longer published. */
public interface ProfileRepository {
    Profile[] findProfilesByNames(String... names);
}
