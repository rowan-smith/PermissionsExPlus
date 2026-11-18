package dev.rono.permissions.api;

import dev.rono.permissions.api.backend.BackendManager;
import dev.rono.permissions.api.config.ConfigurationManager;
import dev.rono.permissions.api.context.WorldInheritance;
import dev.rono.permissions.api.event.EventBus;
import dev.rono.permissions.api.group.GroupManager;
import dev.rono.permissions.api.ladder.LadderManager;
import dev.rono.permissions.api.platform.context.ContextManager;
import dev.rono.permissions.api.resolver.Resolvers;
import dev.rono.permissions.api.user.UserManager;

import java.util.UUID;

public interface PexApi {

    /**
     * Returns the manager responsible for user lookup, caching, persistence, and
     * mutation.
     *
     * @return the user manager
     */
    UserManager users();

    /**
     * Returns the manager responsible for group lookup, caching, persistence, and
     * mutation.
     *
     * @return the group manager
     */
    GroupManager groups();

    /**
     * Returns the manager responsible for ladder lookup, caching, persistence, and
     * mutation.
     *
     * @return the ladder manager
     */
    LadderManager ladders();

    /** Returns the grouped effective-state resolvers. */
    Resolvers resolvers();

    EventBus events();

    /** Returns read-only information about the configured storage backend. */
    BackendManager backend();

    /** Returns the runtime context registry used to build user query options. */
    ContextManager<UUID> contexts();

    /** Returns this server's world inheritance, applied to every query. */
    WorldInheritance worlds();

    ConfigurationManager config();
}
