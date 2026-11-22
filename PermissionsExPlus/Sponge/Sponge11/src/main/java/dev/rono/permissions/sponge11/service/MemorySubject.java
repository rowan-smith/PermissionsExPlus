package dev.rono.permissions.sponge11.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;

/**
 * Subject in a collection PermissionsExPlus does not persist, such as the
 * defaults, command blocks, or plugin-defined collections. Data lasts until the
 * server stops; persistent user and group data belongs in PermissionsExPlus.
 */
final class MemorySubject extends PexSubject {
    private final MemorySubjectData data = new MemorySubjectData(() -> this, false);

    MemorySubject(PexPermissionService service, PexSubjectCollection collection, String identifier) {
        super(service, collection, identifier);
    }

    @Override
    public boolean isSubjectDataPersisted() {
        return false;
    }

    @Override
    public MemorySubjectData subjectData() {
        return data;
    }

    @Override
    protected Tristate persistentValue(String permission, Set<Context> contexts) {
        return data.permissionValue(contexts, permission);
    }

    @Override
    protected List<SubjectReference> persistentParents(Set<Context> contexts) {
        return data.activeParents(contexts);
    }

    @Override
    protected Optional<String> persistentOption(String key, Set<Context> contexts) {
        return Optional.ofNullable(data.option(contexts, key));
    }
}
