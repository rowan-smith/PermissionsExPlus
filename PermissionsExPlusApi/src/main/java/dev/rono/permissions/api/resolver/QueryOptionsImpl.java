package dev.rono.permissions.api.resolver;

import dev.rono.permissions.api.context.ContextSet;

import java.util.Objects;

final class QueryOptionsImpl implements QueryOptions {
    private final ContextSet contexts;
    private final boolean includeInheritance;
    private final boolean includeDefaults;

    QueryOptionsImpl(ContextSet contexts, boolean includeInheritance, boolean includeDefaults) {
        Objects.requireNonNull(contexts, "contexts");

        this.contexts = contexts;
        this.includeInheritance = includeInheritance;
        this.includeDefaults = includeDefaults;
    }

    @Override
    public ContextSet contexts() {
        return contexts;
    }

    @Override
    public boolean includeInheritance() {
        return includeInheritance;
    }

    @Override
    public boolean includeDefaults() {
        return includeDefaults;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof QueryOptionsImpl)) {
            return false;
        }

        QueryOptionsImpl that = (QueryOptionsImpl) other;

        return contexts.equals(that.contexts)
                && includeInheritance == that.includeInheritance
                && includeDefaults == that.includeDefaults;
    }

    @Override
    public int hashCode() {
        int result = contexts.hashCode();
        result = 31 * result + Boolean.hashCode(includeInheritance);
        result = 31 * result + Boolean.hashCode(includeDefaults);

        return result;
    }

    @Override
    public String toString() {
        return "QueryOptionsImpl[contexts=" + contexts + ", includeInheritance=" + includeInheritance + ", includeDefaults=" + includeDefaults + "]";
    }
}
