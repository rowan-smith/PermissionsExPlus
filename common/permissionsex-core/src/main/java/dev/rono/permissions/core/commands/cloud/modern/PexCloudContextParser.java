package dev.rono.permissions.core.commands.cloud.modern;

import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.annotations.parsers.Parser;
import cloud.commandframework.context.CommandContext;

import java.util.List;
import java.util.Queue;

/**
 * Parses optional trailing {@link PexCommandFlags} for modern commands.
 *
 * @param <C> command sender type
 */
public final class PexCloudContextParser<C> {

    @Parser(name = "pex-flags", suggestions = "pex-flags")
    public PexCommandFlags parseFlags(CommandContext<C> commandContext, Queue<String> input) {
        return PexCommandFlags.parseOptional(input);
    }

    @Suggestions("pex-flags")
    public List<String> suggestFlags(CommandContext<C> commandContext, String input) {
        if (input == null || input.isBlank() || input.startsWith("-")) {
            return List.of("--world", "--server", "--region", "--gamemode");
        }
        return List.of();
    }
}
