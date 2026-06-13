package dev.rono.permissions.core.commands;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.meta.SimpleCommandMeta;
import dev.rono.permissions.core.commands.cloud.*;
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
        parser.parse(new RootCommand<>(context));
        parser.parse(new SystemCommand<>(context));
        parser.parse(new WorldCommand<>(context));
        switch (context.cloudPlatform()) {
            case GAME_SERVER -> parser.parse(new WorldGameSubtreeCommand<>(context));
            case PROXY -> parser.parse(new ProxyServerSubtreeCommand<>(context));
        }
        parser.parse(new PromotionCommand<>(context));
        parser.parse(new UserScopedCommand<>(context));
        parser.parse(new UserCommand<>(context));
        parser.parse(new GroupScopedCommand<>(context));
        parser.parse(new GroupCommand<>(context));
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
        parser.parse(new ModernPromotionCommand<>(context));
    }
}
