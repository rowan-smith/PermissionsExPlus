package dev.rono.permissions.core.commands.cloud;

import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;
import dev.rono.permissions.core.commands.CoreCloudSuggestionHelper;
import ru.tehkode.permissions.backends.PermissionBackend;

import java.util.List;

/**
 * Registers named suggestion providers for annotation arguments ({@link cloud.commandframework.annotations.Argument#suggestions}).
 *
 * @param <C> command sender type
 */
public final class PexCloudSuggestions<C> {

    private final CoreCloudCommandContext<C> ctx;

    public PexCloudSuggestions(CoreCloudCommandContext<C> ctx) {
        this.ctx = ctx;
    }

    @Suggestions("pex-user")
    public List<String> suggestUsers(CommandContext<C> commandContext, String input) {
        return CoreCloudSuggestionHelper.matchSuggestions(ctx.commandService().knownUsers(500), input);
    }

    @Suggestions("pex-group")
    public List<String> suggestGroups(CommandContext<C> commandContext, String input) {
        return CoreCloudSuggestionHelper.matchSuggestions(ctx.commandService().knownGroups(), input);
    }

    @Suggestions("pex-ladder")
    public List<String> suggestLadders(CommandContext<C> commandContext, String input) {
        return CoreCloudSuggestionHelper.matchSuggestions(ctx.commandService().knownLadders(), input);
    }

    @Suggestions("pex-world")
    public List<String> suggestWorlds(CommandContext<C> commandContext, String input) {
        return CoreCloudSuggestionHelper.matchSuggestions(ctx.commandService().worldNames(), input);
    }

    @Suggestions("pex-server")
    public List<String> suggestProxyServers(CommandContext<C> commandContext, String input) {
        return suggestWorlds(commandContext, input);
    }

    @Suggestions("pex-permission")
    public List<String> suggestPermissions(CommandContext<C> commandContext, String input) {
        return CoreCloudSuggestionHelper.matchPermissionSuggestions(ctx.commandService().knownPermissions(), input);
    }

    @Suggestions("pex-groups-csv")
    public List<String> suggestCsvGroups(CommandContext<C> commandContext, String input) {
        return CoreCloudSuggestionHelper.matchCsvSuggestions(ctx.commandService().knownGroups(), input);
    }

    /** Aliases registered on this platform ({@link PermissionBackend#registerBackendAlias}). */
    @Suggestions("pex-backend")
    public List<String> suggestBackends(CommandContext<C> commandContext, String input) {
        return CoreCloudSuggestionHelper.matchSuggestions(PermissionBackend.getRegisteredBackendAliases(), input);
    }
}
