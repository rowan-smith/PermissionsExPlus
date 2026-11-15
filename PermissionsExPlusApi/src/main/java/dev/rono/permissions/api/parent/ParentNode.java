package dev.rono.permissions.api.parent;

import lombok.var;

import dev.rono.permissions.api.util.Node;

import java.util.Objects;

public interface ParentNode extends Node {

    String group();

    static ParentNodeBuilder builder() {
        return ParentNodes.builder();
    }

    static ParentNodeBuilder builder(ParentNode node) {
        Objects.requireNonNull(node, "node");

        var builder = builder()
                .group(node.group())
                .contexts(node.contexts());

        node.expiry().ifPresent(builder::expiry);

        return builder;
    }
}
