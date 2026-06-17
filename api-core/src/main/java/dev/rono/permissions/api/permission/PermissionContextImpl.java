package dev.rono.permissions.api.permission;

import java.util.Collections;
import java.util.Map;

final class PermissionContextImpl implements PermissionContext {

    static final PermissionContext EMPTY = new PermissionContextImpl(Map.of());

    private final Map<String, String> attributes;

    private PermissionContextImpl(Map<String, String> attributes) {
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    static PermissionContext of(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return EMPTY;
        }
        return new PermissionContextImpl(Map.copyOf(attributes));
    }

    @Override
    public Map<String, String> attributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return attributes.toString();
    }
}
