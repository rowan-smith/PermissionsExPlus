package dev.rono.permissions.core.command;

import lombok.var;

import lombok.Value;
import lombok.experimental.Accessors;
import cloud.commandframework.CommandManager;
import dev.rono.permissions.api.context.ContextRegistry;
import dev.rono.permissions.api.context.ContextSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import dev.rono.permissions.core.util.Immutables;
import dev.rono.permissions.core.util.Strings;

final class PosixContextFlagExtractor<C> {
    static final String CONTEXTS_KEY = "pex:command-contexts";
    private static final String PENDING_KEY = "pex:pending-context-key";
    private static final String PENDING_VALUE = "pex:pending-context-value";

    private final ContextRegistry registry;

    PosixContextFlagExtractor(ContextRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    void register(CommandManager<C> manager) {
        manager.registerCommandPreProcessor(context -> extract(context.getInputQueue(), context.getCommandContext().isSuggestions(), new Storage() {
            @Override
            public void contexts(ContextSet contexts) {
                context.getCommandContext().store(CONTEXTS_KEY, contexts);
            }

            @Override
            public void pendingKey(String keyPrefix) {
                context.getCommandContext().store(PENDING_KEY, keyPrefix);
            }

            @Override
            public void pendingValue(String key, String valuePrefix) {
                context.getCommandContext().store(PENDING_KEY, key);
                context.getCommandContext().store(PENDING_VALUE, valuePrefix);
            }
        }));

        var previous = manager.commandSuggestionProcessor();

        manager.commandSuggestionProcessor((context, suggestions) -> context.getCommandContext().<String>getOptional(PENDING_KEY).map(key -> context.getCommandContext().<String>getOptional(PENDING_VALUE).map(value -> valueSuggestions(key, value)).orElseGet(() -> keySuggestions(key))).orElseGet(() -> previous.apply(context, suggestions)));
        manager.parameterInjectorRegistry().registerInjector(ContextSet.class, (context, annotations) -> context.getOrDefault(CONTEXTS_KEY, ContextSet.empty()));
    }

    Extraction extract(LinkedList<String> input, boolean suggestions) {
        var storage = new ResultStorage();

        extract(input, suggestions, storage);

        return new Extraction(storage.contexts, storage.pendingKey, storage.pendingValue);
    }

    private void extract(LinkedList<String> input, boolean suggestions, Storage storage) {
        var trailingWhitespace = suggestions && !input.isEmpty() && Strings.isBlank(input.getLast());
        if (trailingWhitespace && input.stream().anyMatch(token -> token.startsWith("--"))) {
            input.removeLast();
        }

        if (suggestions && !input.isEmpty()) {
            var value = input.getLast();
            if (value.equals("-")) {
                input.removeLast();

                storage.pendingKey("");
            } else if (trailingWhitespace && value.startsWith("--")) {
                if (value.length() == 2) {
                    input.removeLast();

                    storage.contexts(ContextSet.empty());

                    return;
                }

                var key = flagKey(value);

                ensureRegistered(key);

                input.removeLast();

                storage.pendingValue(key, "");
            } else if (!trailingWhitespace && input.size() >= 2 && input.get(input.size() - 2).startsWith("--")) {
                var key = flagKey(input.get(input.size() - 2));
                ensureRegistered(key);

                input.removeLast();
                input.removeLast();

                storage.pendingValue(key, value);
            } else if (value.startsWith("--")) {
                input.removeLast();

                storage.pendingKey(value.substring(2).toLowerCase(Locale.ROOT));
            }
        }

        var builder = ContextSet.builder();

        while (input.size() >= 2 && input.get(input.size() - 2).startsWith("--")) {
            var value = input.removeLast();
            var key = flagKey(input.removeLast());

            ensureRegistered(key);

            if (Strings.isBlank(value) || value.startsWith("--")) {
                throw new IllegalArgumentException("Missing value for --" + key);
            }

            if (!registry.validValues(key).contains(value)) {
                throw new IllegalArgumentException("Invalid value '" + value + "' for --" + key);
            }

            builder.add(key, value);
        }

        input.stream().filter(token -> token.startsWith("--")).findFirst().ifPresent(flag -> {
            throw new IllegalArgumentException("Context flags must be trailing key/value pairs: " + flag);
        });

        storage.contexts(builder.build());
    }

    List<String> keySuggestions(String prefix) {
        return registry.registeredKeys().stream().filter(key -> key.startsWith(prefix)).map(key -> "--" + key).collect(Immutables.toList());
    }

    List<String> valueSuggestions(String key, String prefix) {
        var normalizedPrefix = prefix.toLowerCase(Locale.ROOT);
        return registry.validValues(key).stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix)).collect(Immutables.toList());
    }

    private void ensureRegistered(String key) {
        if (!registry.registeredKeys().contains(key)) {
            throw new IllegalArgumentException("Unknown context flag: --" + key);
        }
    }

    private String flagKey(String flag) {
        if (!flag.startsWith("--") || flag.length() == 2) {
            throw new IllegalArgumentException("Invalid context flag: " + flag);
        }

        return flag.substring(2).toLowerCase(Locale.ROOT);
    }

    @Value
    @Accessors(fluent = true)
    static class Extraction {
        ContextSet contexts;
        String pendingKey;
        String pendingValue;

        Extraction(ContextSet contexts, String pendingKey, String pendingValue) {
            this.contexts = contexts;
            this.pendingKey = pendingKey;
            this.pendingValue = pendingValue;
        }
    }

    private interface Storage {
        void contexts(ContextSet contexts);

        void pendingKey(String keyPrefix);

        void pendingValue(String key, String valuePrefix);
    }

    private static final class ResultStorage implements Storage {
        private ContextSet contexts = ContextSet.empty();
        private String pendingKey;
        private String pendingValue;

        @Override
        public void contexts(ContextSet contexts) {
            this.contexts = contexts;
        }

        @Override
        public void pendingKey(String keyPrefix) {
            pendingKey = keyPrefix;
        }

        @Override
        public void pendingValue(String key, String valuePrefix) {
            pendingKey = key;
            pendingValue = valuePrefix;
        }
    }
}
