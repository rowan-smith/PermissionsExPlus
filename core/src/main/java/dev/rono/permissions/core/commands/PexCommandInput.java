package dev.rono.permissions.core.commands;

/**
 * Normalizes raw command lines before Cloud parses them. Trailing spaces would otherwise
 * produce an empty final token (see {@code CommandInputTokenizer}).
 * Values that must retain leading or trailing spaces should be quoted in the command.
 */
public final class PexCommandInput {
    private PexCommandInput() {}

    public static String strip(String commandInput) {
        return commandInput == null ? null : commandInput.strip();
    }
}
