package dev.rono.permissions.sponge11.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;

/**
 * In-memory subject data: transient data for every subject, and all data for
 * collections PermissionsExPlus does not persist (command blocks, system, ...).
 */
final class MemorySubjectData extends AbstractSubjectData {
    private final Map<Set<Context>, Map<String, Boolean>> permissions = new ConcurrentHashMap<>();
    private final Map<Set<Context>, List<SubjectReference>> parents = new ConcurrentHashMap<>();
    private final Map<Set<Context>, Map<String, String>> options = new ConcurrentHashMap<>();

    MemorySubjectData(Supplier<Subject> subject, boolean transientData) {
        super(subject, transientData);
    }

    /** Most specific defined value among entries whose contexts are all active. */
    Tristate permissionValue(Set<Context> active, String permission) {
        Tristate result = Tristate.UNDEFINED;
        int best = -1;

        for (Map.Entry<Set<Context>, Map<String, Boolean>> entry : permissions.entrySet()) {
            Boolean value = entry.getValue().get(permission.toLowerCase(Locale.ROOT));

            if (value != null && active.containsAll(entry.getKey()) && entry.getKey().size() > best) {
                best = entry.getKey().size();
                result = Tristate.fromBoolean(value);
            }
        }

        return result;
    }

    List<SubjectReference> activeParents(Set<Context> active) {
        List<SubjectReference> result = new ArrayList<>();

        parents.forEach((contexts, values) -> {
            if (active.containsAll(contexts)) {
                result.addAll(values);
            }
        });

        return result;
    }

    String option(Set<Context> active, String key) {
        String result = null;
        int best = -1;

        for (Map.Entry<Set<Context>, Map<String, String>> entry : options.entrySet()) {
            String value = entry.getValue().get(key);

            if (value != null && active.containsAll(entry.getKey()) && entry.getKey().size() > best) {
                best = entry.getKey().size();
                result = value;
            }
        }

        return result;
    }

    @Override
    public Map<Set<Context>, Map<String, Boolean>> allPermissions() {
        Map<Set<Context>, Map<String, Boolean>> copy = new LinkedHashMap<>();
        permissions.forEach((contexts, values) -> copy.put(contexts, Collections.unmodifiableMap(new LinkedHashMap<>(values))));
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public Map<String, Boolean> permissions(Set<Context> contexts) {
        Map<String, Boolean> values = permissions.get(key(contexts));
        return values == null ? Collections.<String, Boolean>emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    @Override
    public CompletableFuture<Boolean> setPermission(Set<Context> contexts, String permission, Tristate value) {
        String node = permission.toLowerCase(Locale.ROOT);
        Map<String, Boolean> values = permissions.computeIfAbsent(key(contexts), ignored -> new ConcurrentHashMap<>());
        boolean changed = value == Tristate.UNDEFINED ? values.remove(node) != null : !Boolean.valueOf(value.asBoolean()).equals(values.put(node, value.asBoolean()));
        return CompletableFuture.completedFuture(changed);
    }

    @Override
    public CompletableFuture<Boolean> clearPermissions() {
        boolean changed = !permissions.isEmpty();
        permissions.clear();
        return CompletableFuture.completedFuture(changed);
    }

    @Override
    public CompletableFuture<Boolean> clearPermissions(Set<Context> contexts) {
        return CompletableFuture.completedFuture(permissions.remove(key(contexts)) != null);
    }

    @Override
    public Map<Set<Context>, List<SubjectReference>> allParents() {
        Map<Set<Context>, List<SubjectReference>> copy = new LinkedHashMap<>();
        parents.forEach((contexts, values) -> copy.put(contexts, Collections.unmodifiableList(new ArrayList<>(values))));
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public List<SubjectReference> parents(Set<Context> contexts) {
        List<SubjectReference> values = parents.get(key(contexts));
        return values == null ? Collections.<SubjectReference>emptyList() : Collections.unmodifiableList(new ArrayList<>(values));
    }

    @Override
    public CompletableFuture<Boolean> addParent(Set<Context> contexts, SubjectReference parent) {
        List<SubjectReference> values = parents.computeIfAbsent(key(contexts), ignored -> new CopyOnWriteArrayList<>());
        return CompletableFuture.completedFuture(!values.contains(parent) && values.add(parent));
    }

    @Override
    public CompletableFuture<Boolean> removeParent(Set<Context> contexts, SubjectReference parent) {
        List<SubjectReference> values = parents.get(key(contexts));
        return CompletableFuture.completedFuture(values != null && values.remove(parent));
    }

    @Override
    public CompletableFuture<Boolean> clearParents() {
        boolean changed = !parents.isEmpty();
        parents.clear();
        return CompletableFuture.completedFuture(changed);
    }

    @Override
    public CompletableFuture<Boolean> clearParents(Set<Context> contexts) {
        return CompletableFuture.completedFuture(parents.remove(key(contexts)) != null);
    }

    @Override
    public Map<Set<Context>, Map<String, String>> allOptions() {
        Map<Set<Context>, Map<String, String>> copy = new LinkedHashMap<>();
        options.forEach((contexts, values) -> copy.put(contexts, Collections.unmodifiableMap(new LinkedHashMap<>(values))));
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public Map<String, String> options(Set<Context> contexts) {
        Map<String, String> values = options.get(key(contexts));
        return values == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    @Override
    public CompletableFuture<Boolean> setOption(Set<Context> contexts, String key, String value) {
        Map<String, String> values = options.computeIfAbsent(key(contexts), ignored -> new ConcurrentHashMap<>());
        boolean changed = value == null ? values.remove(key) != null : !value.equals(values.put(key, value));
        return CompletableFuture.completedFuture(changed);
    }

    @Override
    public CompletableFuture<Boolean> clearOptions() {
        boolean changed = !options.isEmpty();
        options.clear();
        return CompletableFuture.completedFuture(changed);
    }

    @Override
    public CompletableFuture<Boolean> clearOptions(Set<Context> contexts) {
        return CompletableFuture.completedFuture(options.remove(key(contexts)) != null);
    }
}
