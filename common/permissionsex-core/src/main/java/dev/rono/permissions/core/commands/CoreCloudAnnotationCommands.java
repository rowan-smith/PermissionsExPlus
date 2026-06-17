package dev.rono.permissions.core.commands;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.meta.SimpleCommandMeta;
import dev.rono.permissions.core.commands.cloud.PexCloudSuggestions;
import dev.rono.permissions.core.commands.cloud.classic.*;
import dev.rono.permissions.core.commands.cloud.modern.*;
import dev.rono.permissions.core.config.CommandFramework;

final class CoreCloudAnnotationCommands {
    private CoreCloudAnnotationCommands() {}

    static <C> void register(CoreCloudCommandContext<C> context) {
        AnnotationParser<C> parser = new AnnotationParser<>(
                context.manager(),
                context.senderType(),
                parameters -> SimpleCommandMeta.empty());
        parser.parse(new PexCloudSuggestions<>(context));
        if (context.commandFramework() == CommandFramework.MODERN) {
            registerModern(parser, context);
        } else {
            registerClassic(parser, context);
        }
    }

    private static <C> void registerClassic(AnnotationParser<C> parser, CoreCloudCommandContext<C> context) {
        parser.parse(new ClassicRootCommand<>(context));
        parser.parse(new ClassicSystemCommand<>(context));
        parser.parse(new ClassicWorldCommand<>(context));
        switch (context.cloudPlatform()) {
            case GAME_SERVER -> parser.parse(new ClassicRealmSubtreeCommand.Game<>(context));
            case PROXY -> parser.parse(new ClassicRealmSubtreeCommand.Proxy<>(context));
        }
        parser.parse(new ClassicPromotionCommand<>(context));
        parser.parse(new ClassicUserScopedCommand<>(context));
        parser.parse(new ClassicUserCommand<>(context));
        parser.parse(new ClassicGroupScopedCommand<>(context));
        parser.parse(new ClassicGroupCommand<>(context));
    }

    private static <C> void registerModern(AnnotationParser<C> parser, CoreCloudCommandContext<C> context) {
        parser.parse(new PexCloudContextParser<>());
        parser.parse(new ModernUserCommand<>(context));
        parser.parse(new ModernGroupCommand<>(context));
        parser.parse(new ModernUsersCommand<>(context));
        parser.parse(new ModernGroupsCommand<>(context));
        parser.parse(new ModernLadderCommand<>(context));
        parser.parse(new ModernRootCommand<>(context));
        parser.parse(new ModernSystemCommand<>(context));
        parser.parse(new ModernBackendCommand<>(context));
        parser.parse(new ModernContextCommand<>(context));
    }
}
