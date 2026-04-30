package dev.rono.permissions.core.commands;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.meta.SimpleCommandMeta;
import dev.rono.permissions.core.commands.cloud.GroupCommand;
import dev.rono.permissions.core.commands.cloud.GroupScopedCommand;
import dev.rono.permissions.core.commands.cloud.PexCloudSuggestions;
import dev.rono.permissions.core.commands.cloud.PromotionCommand;
import dev.rono.permissions.core.commands.cloud.ProxyServerSubtreeCommand;
import dev.rono.permissions.core.commands.cloud.RootCommand;
import dev.rono.permissions.core.commands.cloud.SystemCommand;
import dev.rono.permissions.core.commands.cloud.UserCommand;
import dev.rono.permissions.core.commands.cloud.UserScopedCommand;
import dev.rono.permissions.core.commands.cloud.WorldCommand;
import dev.rono.permissions.core.commands.cloud.WorldGameSubtreeCommand;

final class CoreCloudAnnotationCommands {
    private CoreCloudAnnotationCommands() {}

    static <C> void register(CoreCloudCommandContext<C> context) {
        AnnotationParser<C> parser = new AnnotationParser<>(
                context.manager(),
                context.senderType(),
                parameters -> SimpleCommandMeta.empty());
        parser.parse(new PexCloudSuggestions<>(context));
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
}
