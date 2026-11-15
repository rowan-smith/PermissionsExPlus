package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.suggestions.Suggestions;
import dev.rono.permissions.api.context.ContextSet;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import dev.rono.permissions.core.util.Immutables;
import dev.rono.permissions.core.util.Strings;

class CommandCompletionContractTest {
    private static final Set<Class<?>> COMMAND_TYPES = Immutables.setOf(CommandSuggestions.class, RootCommands.class,
            BackendCommands.class, GroupsCommands.class, GroupCommands.class, UsersCommands.class, UserCommands.class,
            LaddersCommands.class, LadderCommands.class);

    @Test
    void everyReferenceArgumentHasARegisteredSuggestionProvider() {
        var providerNames = Arrays.stream(CommandSuggestions.class.getDeclaredMethods())
                .map(method -> method.getAnnotation(Suggestions.class))
                .filter(java.util.Objects::nonNull)
                .map(Suggestions::value)
                .collect(Collectors.toSet());

        assertEquals(Immutables.setOf("users", "groups", "ladders", "backends", "permissions", "options", "user-permissions-add", "user-permissions-remove", "user-groups-add", "user-groups-remove"), providerNames);

        var arguments = COMMAND_TYPES.stream().flatMap(type -> Arrays.stream(type.getDeclaredMethods()))
                .flatMap(method -> Arrays.stream(method.getParameterAnnotations())).flatMap(Arrays::stream)
                .filter(Argument.class::isInstance)
                .map(Argument.class::cast).collect(Immutables.toList());

        var referenceNames = Immutables.setOf("user", "parent", "backend", "permission", "key");

        arguments.stream().filter(argument -> referenceNames.contains(argument.value())).forEach(argument -> {
            assertFalse(Strings.isBlank(argument.suggestions()), () -> "Missing suggestions for " + argument.value());
            assertTrue(providerNames.contains(argument.suggestions()), () -> "Unknown suggestions provider " + argument.suggestions());
        });
    }

    @Test
    void contextualCommandsUseTheUnifiedContextSet() {
        var contextParameters = COMMAND_TYPES.stream().flatMap(type -> Arrays.stream(type.getDeclaredMethods()))
                .flatMap(method -> Arrays.stream(method.getParameterTypes())).filter(ContextSet.class::equals).collect(Immutables.toList());

        assertFalse(contextParameters.isEmpty());
    }
}
