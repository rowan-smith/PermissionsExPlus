package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionService;

/** Package-internal fluent scaffolding; use {@link PermissionService#query()}. */
final class SubjectRefs {

    private SubjectRefs() {}

    static UserRef user(PermissionService service, java.util.UUID uuid, String identifier) {
        return new UserRef(service, uuid, identifier);
    }

    static GroupRef group(PermissionService service, String name) {
        return new GroupRef(service, name);
    }
}
