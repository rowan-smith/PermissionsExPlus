package dev.rono.permissions.api.options;

import lombok.var;

import dev.rono.permissions.api.util.Node;

import java.util.Objects;

public interface OptionNode extends Node {

    String key();

    String value();

    static OptionNodeBuilder builder() {
        return Options.builder();
    }

    static OptionNodeBuilder builder(OptionNode option) {
        Objects.requireNonNull(option, "option");

        var builder = builder()
                .key(option.key())
                .value(option.value())
                .contexts(option.contexts());

        option.expiry().ifPresent(builder::expiry);

        return builder;
    }
}
