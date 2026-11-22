package dev.rono.permissions.sponge7.service;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;

/**
 * Sponge subject data backed by a PermissionsExPlus user or group. Reads come
 * from the current snapshot; writes go through Core's modifiers, so they are
 * persisted, published as events, and invalidate the permission cache.
 */
final class PersistentSubjectData implements SubjectData {
    /** The writes Sponge can request, independent of user or group modifiers. */
    interface Edits {
        void permission(String permission, PermissionValue value, ContextSet contexts);

        void removePermission(String permission, ContextSet contexts);

        void clearPermissions();

        void clearPermissions(ContextSet contexts);

        void addParent(String group, ContextSet contexts);

        void removeParent(String group, ContextSet contexts);

        void clearParents();

        void option(String key, String value, ContextSet contexts);

        void removeOption(String key, ContextSet contexts);

        void clearOptions();

        void clearOptions(ContextSet contexts);
    }

    private final PexPermissionService service;
    private final Supplier<PermissionHolder> holder;
    private final Function<PermissionHolder, Set<ParentNode>> parents;
    private final Function<Consumer<Edits>, CompletableFuture<?>> apply;

    PersistentSubjectData(PexPermissionService service, Supplier<PermissionHolder> holder, Function<PermissionHolder, Set<ParentNode>> parents, Function<Consumer<Edits>, CompletableFuture<?>> apply) {
        this.service = service;
        this.holder = holder;
        this.parents = parents;
        this.apply = apply;
    }

    private CompletableFuture<Boolean> edit(Consumer<Edits> action) {
        return apply.apply(action).thenApply(ignored -> true);
    }

    @Override
    public Map<Set<Context>, Map<String, Boolean>> getAllPermissions() {
        PermissionHolder current = holder.get();
        Map<Set<Context>, Map<String, Boolean>> result = new LinkedHashMap<>();

        if (current != null) {
            for (PermissionNode node : current.explicitPermissions()) {
                if (!node.expired()) {
                    result.computeIfAbsent(Contexts.toSponge(node.contexts()), ignored -> new LinkedHashMap<>()).put(node.permission(), node.value() == PermissionValue.ALLOW);
                }
            }
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<String, Boolean> getPermissions(Set<Context> contexts) {
        Map<String, Boolean> values = getAllPermissions().get(contexts);
        return values == null ? Collections.<String, Boolean>emptyMap() : Collections.unmodifiableMap(values);
    }

    @Override
    public CompletableFuture<Boolean> setPermission(Set<Context> contexts, String permission, Tristate value) {
        ContextSet pex = Contexts.toPex(contexts);

        return edit(edits -> {
            if (value == Tristate.UNDEFINED) {
                edits.removePermission(permission, pex);
            } else {
                edits.permission(permission, value.asBoolean() ? PermissionValue.ALLOW : PermissionValue.DENY, pex);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> clearPermissions() {
        return edit(Edits::clearPermissions);
    }

    @Override
    public CompletableFuture<Boolean> clearPermissions(Set<Context> contexts) {
        ContextSet pex = Contexts.toPex(contexts);
        return edit(edits -> edits.clearPermissions(pex));
    }

    @Override
    public Map<Set<Context>, List<SubjectReference>> getAllParents() {
        PermissionHolder current = holder.get();
        Map<Set<Context>, List<SubjectReference>> result = new LinkedHashMap<>();

        if (current != null) {
            for (ParentNode node : parents.apply(current)) {
                if (!node.expired()) {
                    result.computeIfAbsent(Contexts.toSponge(node.contexts()), ignored -> new ArrayList<>())
                            .add(service.newSubjectReference(PermissionService.SUBJECTS_GROUP, node.group()));
                }
            }
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public List<SubjectReference> getParents(Set<Context> contexts) {
        List<SubjectReference> values = getAllParents().get(contexts);
        return values == null ? Collections.<SubjectReference>emptyList() : Collections.unmodifiableList(values);
    }

    @Override
    public CompletableFuture<Boolean> addParent(Set<Context> contexts, SubjectReference parent) {
        if (!PermissionService.SUBJECTS_GROUP.equalsIgnoreCase(parent.getCollectionIdentifier())) {
            return CompletableFuture.completedFuture(false); // PermissionsExPlus inherits from groups only.
        }

        ContextSet pex = Contexts.toPex(contexts);

        // The parent group must exist before it can be referenced.
        return service.groups().loadSubject(parent.getSubjectIdentifier())
                .thenCompose(ignored -> edit(edits -> edits.addParent(parent.getSubjectIdentifier(), pex)));
    }

    @Override
    public CompletableFuture<Boolean> removeParent(Set<Context> contexts, SubjectReference parent) {
        if (!PermissionService.SUBJECTS_GROUP.equalsIgnoreCase(parent.getCollectionIdentifier())) {
            return CompletableFuture.completedFuture(false);
        }

        ContextSet pex = Contexts.toPex(contexts);
        return edit(edits -> edits.removeParent(parent.getSubjectIdentifier(), pex));
    }

    @Override
    public CompletableFuture<Boolean> clearParents() {
        return edit(Edits::clearParents);
    }

    @Override
    public CompletableFuture<Boolean> clearParents(Set<Context> contexts) {
        PermissionHolder current = holder.get();
        ContextSet pex = Contexts.toPex(contexts);

        if (current == null) {
            return CompletableFuture.completedFuture(false);
        }

        List<String> remove = new ArrayList<>();

        for (ParentNode node : parents.apply(current)) {
            if (node.contexts().equals(pex)) {
                remove.add(node.group());
            }
        }

        return edit(edits -> remove.forEach(group -> edits.removeParent(group, pex)));
    }

    @Override
    public Map<Set<Context>, Map<String, String>> getAllOptions() {
        PermissionHolder current = holder.get();
        Map<Set<Context>, Map<String, String>> result = new LinkedHashMap<>();

        if (current != null) {
            for (OptionNode node : current.explicitOptions()) {
                if (!node.expired()) {
                    result.computeIfAbsent(Contexts.toSponge(node.contexts()), ignored -> new LinkedHashMap<>()).put(node.key(), node.value());
                }
            }
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<String, String> getOptions(Set<Context> contexts) {
        Map<String, String> values = getAllOptions().get(contexts);
        return values == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(values);
    }

    @Override
    public CompletableFuture<Boolean> setOption(Set<Context> contexts, String key, String value) {
        ContextSet pex = Contexts.toPex(contexts);

        return edit(edits -> {
            if (value == null) {
                edits.removeOption(key, pex);
            } else {
                edits.option(key, value, pex);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> clearOptions() {
        return edit(Edits::clearOptions);
    }

    @Override
    public CompletableFuture<Boolean> clearOptions(Set<Context> contexts) {
        ContextSet pex = Contexts.toPex(contexts);
        return edit(edits -> edits.clearOptions(pex));
    }
}
