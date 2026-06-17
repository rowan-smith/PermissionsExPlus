package dev.rono.permissions.core.storage.backend;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class LocalSqlUserData extends LocalSqlEntityData implements ru.tehkode.permissions.PermissionsUserData {

    private UUID userId;

    public LocalSqlUserData(LocalSqlBackend backend, String userName) {
        super(backend, userName);
    }

    @Override
    public boolean setIdentifier(String identifier) {
        return false;
    }

    @Override
    public void load() {
        try {
            userId = backend.resolveUserId(identifier);
            loaded = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<String> permissionsForContext(String contextKey) throws Exception {
        return repository.getUserPermissions(requireUserId(), contextKey);
    }

    @Override
    protected void replacePermissions(String contextKey, List<String> permissions) throws Exception {
        ensurePersisted();
        repository.replaceUserPermissions(requireUserId(), contextKey, permissions);
    }

    @Override
    protected Set<String> worldsForEntity() throws Exception {
        Set<String> worlds = new HashSet<>(repository.listPermissionWorlds(requireUserId()));
        worlds.addAll(repository.listUserOptionWorlds(requireUserId()));
        worlds.addAll(repository.listUserGroupWorlds(requireUserId()));
        return worlds;
    }

    @Override
    protected String optionForContext(String option, String contextKey) throws Exception {
        if ("prefix".equals(option) && contextKey == null) {
            return repository.loadUser(requireUserId()).getOptions().getPrefix();
        }
        if ("suffix".equals(option) && contextKey == null) {
            return repository.loadUser(requireUserId()).getOptions().getSuffix();
        }
        return repository.getUserEntityOptions(requireUserId(), contextKey).get(option);
    }

    @Override
    protected Map<String, String> optionsForContext(String contextKey) throws Exception {
        Map<String, String> out = new java.util.LinkedHashMap<>(repository.getUserEntityOptions(requireUserId(), contextKey));
        if (contextKey == null) {
            String prefix = repository.loadUser(requireUserId()).getOptions().getPrefix();
            if (prefix != null) {
                out.putIfAbsent("prefix", prefix);
            }
            String suffix = repository.loadUser(requireUserId()).getOptions().getSuffix();
            if (suffix != null) {
                out.putIfAbsent("suffix", suffix);
            }
        }
        return out;
    }

    @Override
    protected void setEntityOption(String option, String value, String contextKey) throws Exception {
        ensurePersisted();
        repository.setUserOption(requireUserId(), option, value, contextKey);
    }

    @Override
    protected List<String> parentsForContext(String contextKey) throws Exception {
        return repository.getUserParents(requireUserId(), contextKey);
    }

    @Override
    protected void replaceParents(String contextKey, List<String> parents) throws Exception {
        ensurePersisted();
        UUID id = requireUserId();
        repository.clearUserGroups(id, contextKey);
        for (String parent : parents) {
            repository.findGroupId(parent).ifPresent(groupId -> {
                try {
                    repository.setUserGroup(id, groupId, contextKey, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    protected boolean entityExists() throws Exception {
        try {
            UUID id = UUID.fromString(identifier);
            if (repository.userExists(id)) {
                return true;
            }
        } catch (IllegalArgumentException ignored) {
        }
        return repository.userExistsByName(identifier);
    }

    @Override
    protected void persist() throws Exception {
        UUID id = requireUserId();
        if (!repository.userExists(id)) {
            repository.upsertUser(id, identifier, null, Instant.now());
        }
    }

    @Override
    protected void deleteEntity() throws Exception {
        repository.deleteUser(requireUserId());
    }

    private void ensurePersisted() throws Exception {
        UUID id = requireUserId();
        if (!repository.userExists(id)) {
            repository.upsertUser(id, identifier, null, Instant.now());
        }
    }

    private UUID requireUserId() throws Exception {
        if (userId == null) {
            userId = backend.resolveUserId(identifier);
        }
        return userId;
    }
}
