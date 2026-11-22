package dev.rono.permissions.sponge7.service;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionValue;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.PexApiImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;

/** A subject backed by a PermissionsExPlus user or group. */
abstract class HolderSubject extends PexSubject {
    protected final PexApiImpl<?> core;
    private final PersistentSubjectData data;

    HolderSubject(PexPermissionService service, PexSubjectCollection collection, String identifier) {
        super(service, collection, identifier);
        this.core = service.core();
        this.data = new PersistentSubjectData(service, this::holder, this::parentNodes, this::apply);
    }

    /** Current snapshot, or {@code null} when it is not loaded. */
    protected abstract PermissionHolder holder();

    protected abstract Set<ParentNode> parentNodes(PermissionHolder holder);

    protected abstract CompletableFuture<?> apply(Consumer<PersistentSubjectData.Edits> action);

    @Override
    public final boolean isSubjectDataPersisted() {
        return true;
    }

    @Override
    public final SubjectData getSubjectData() {
        return data;
    }

    @Override
    protected Tristate persistentValue(Set<Context> contexts, String permission) {
        PermissionHolder holder = holder();

        if (holder == null) {
            return Tristate.UNDEFINED;
        }

        return Contexts.tristate(core.resolvers().permissions().check(holder, permission, Contexts.query(contexts)));
    }

    @Override
    protected List<SubjectReference> persistentParents(Set<Context> contexts) {
        PermissionHolder holder = holder();

        if (holder == null) {
            return Collections.emptyList();
        }

        ContextSet active = Contexts.toPex(contexts);
        List<SubjectReference> result = new ArrayList<>();

        for (ParentNode node : parentNodes(holder)) {
            if (!node.expired() && Contexts.applies(node.contexts(), active)) {
                result.add(service.newSubjectReference(PermissionService.SUBJECTS_GROUP, node.group()));
            }
        }

        return result;
    }

    @Override
    protected Optional<String> persistentOption(Set<Context> contexts, String key) {
        PermissionHolder holder = holder();

        return holder == null ? Optional.<String>empty() : core.resolvers().options().resolve(holder, key, Contexts.query(contexts));
    }

    static final class UserSubject extends HolderSubject {
        private final UUID uniqueId;

        UserSubject(PexPermissionService service, PexSubjectCollection collection, UUID uniqueId) {
            super(service, collection, uniqueId.toString());
            this.uniqueId = uniqueId;
        }

        @Override
        protected PermissionHolder holder() {
            return core.users().cache().get(uniqueId).orElse(null);
        }

        @Override
        protected Tristate persistentValue(Set<Context> contexts, String permission) {
            if (holder() == null) {
                // Not loaded yet: answer like the Bukkit platforms (default-group fallback).
                return Contexts.tristate(core.cacheFailureFallback(uniqueId, permission));
            }

            return super.persistentValue(contexts, permission);
        }

        @Override
        protected Set<ParentNode> parentNodes(PermissionHolder holder) {
            return ((User) holder).groups();
        }

        @Override
        protected CompletableFuture<?> apply(Consumer<PersistentSubjectData.Edits> action) {
            User user = (User) holder();

            if (user == null) {
                return service.users().loadSubject(getIdentifier()).thenCompose(ignored -> apply(action));
            }

            return core.users().modify(user, modifier -> action.accept(new PersistentSubjectData.Edits() {
                @Override
                public void permission(String permission, PermissionValue value, ContextSet contexts) {
                    if (value == PermissionValue.ALLOW) {
                        modifier.allowPermission(permission, contexts);
                    } else {
                        modifier.denyPermission(permission, contexts);
                    }
                }

                @Override
                public void removePermission(String permission, ContextSet contexts) {
                    modifier.removePermission(permission, contexts);
                }

                @Override
                public void clearPermissions() {
                    modifier.clearPermissions();
                }

                @Override
                public void clearPermissions(ContextSet contexts) {
                    modifier.clearPermissions(contexts);
                }

                @Override
                public void addParent(String group, ContextSet contexts) {
                    modifier.addGroup(group, contexts);
                }

                @Override
                public void removeParent(String group, ContextSet contexts) {
                    modifier.removeGroup(group, contexts);
                }

                @Override
                public void clearParents() {
                    modifier.clearGroups();
                }

                @Override
                public void option(String key, String value, ContextSet contexts) {
                    modifier.setOption(key, value, contexts);
                }

                @Override
                public void removeOption(String key, ContextSet contexts) {
                    modifier.removeOption(key, contexts);
                }

                @Override
                public void clearOptions() {
                    modifier.clearOptions();
                }

                @Override
                public void clearOptions(ContextSet contexts) {
                    modifier.clearOptions(contexts);
                }
            })).toCompletableFuture();
        }

        @Override
        public Optional<CommandSource> getCommandSource() {
            if (!Sponge.isServerAvailable()) {
                return Optional.empty();
            }

            Optional<Player> player = Sponge.getServer().getPlayer(uniqueId);

            return player.isPresent() ? Optional.<CommandSource>of(player.get()) : Optional.<CommandSource>empty();
        }
    }

    static final class GroupSubject extends HolderSubject {
        GroupSubject(PexPermissionService service, PexSubjectCollection collection, String name) {
            super(service, collection, name);
        }

        @Override
        protected PermissionHolder holder() {
            return core.groups().cache().get(getIdentifier()).orElse(null);
        }

        @Override
        protected Set<ParentNode> parentNodes(PermissionHolder holder) {
            return ((Group) holder).parents();
        }

        @Override
        protected CompletableFuture<?> apply(Consumer<PersistentSubjectData.Edits> action) {
            Group group = (Group) holder();

            if (group == null) {
                return service.groups().loadSubject(getIdentifier()).thenCompose(ignored -> apply(action));
            }

            return core.groups().modify(group, modifier -> action.accept(new PersistentSubjectData.Edits() {
                @Override
                public void permission(String permission, PermissionValue value, ContextSet contexts) {
                    if (value == PermissionValue.ALLOW) {
                        modifier.allowPermission(permission, contexts);
                    } else {
                        modifier.denyPermission(permission, contexts);
                    }
                }

                @Override
                public void removePermission(String permission, ContextSet contexts) {
                    modifier.removePermission(permission, contexts);
                }

                @Override
                public void clearPermissions() {
                    modifier.clearPermissions();
                }

                @Override
                public void clearPermissions(ContextSet contexts) {
                    modifier.clearPermissions(contexts);
                }

                @Override
                public void addParent(String parent, ContextSet contexts) {
                    modifier.addParent(parent, contexts);
                }

                @Override
                public void removeParent(String parent, ContextSet contexts) {
                    modifier.removeParent(parent, contexts);
                }

                @Override
                public void clearParents() {
                    modifier.clearParents();
                }

                @Override
                public void option(String key, String value, ContextSet contexts) {
                    modifier.setOption(key, value, contexts);
                }

                @Override
                public void removeOption(String key, ContextSet contexts) {
                    modifier.removeOption(key, contexts);
                }

                @Override
                public void clearOptions() {
                    modifier.clearOptions();
                }

                @Override
                public void clearOptions(ContextSet contexts) {
                    modifier.clearOptions(contexts);
                }
            })).toCompletableFuture();
        }
    }
}
