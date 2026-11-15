package dev.rono.permissions.api.permission;

import lombok.var;

import dev.rono.permissions.api.util.Node;

import java.util.Objects;

public interface PermissionNode extends Node {

    String permission();

    PermissionValue value();

    default boolean allowed() {
        return value() == PermissionValue.ALLOW;
    }

    default boolean denied() {
        return value() == PermissionValue.DENY;
    }

    static PermissionNodeBuilder builder() {
        return PermissionNodes.builder();
    }

    static PermissionNodeBuilder builder(PermissionNode node) {
        Objects.requireNonNull(node, "node");

        var builder = builder()
                .permission(node.permission())
                .value(node.value())
                .contexts(node.contexts());

        node.expiry().ifPresent(builder::expiry);

        return builder;
    }
}
