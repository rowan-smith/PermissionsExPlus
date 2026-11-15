package dev.rono.permissions.api.context;

import java.util.Collections;
import java.util.Set;

final class ContextSets {

    private static final ContextSet EMPTY = new ImmutableContextSet(Collections.<String, Set<String>>emptyMap());

    private ContextSets() {
        throw new AssertionError();
    }

    static ContextSet empty() {
        return EMPTY;
    }

    static ContextBuilder builder() {
        return new ContextBuilderImpl();
    }
}
