package dev.rono.permissions.core.storage.backend;

import dev.rono.permissions.core.storage.LocalSqlRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LocalSqlGroupData extends LocalSqlEntityData implements ru.tehkode.permissions.PermissionsGroupData {

    private Integer groupId;

    public LocalSqlGroupData(LocalSqlBackend backend, String groupName) {
        super(backend, groupName);
    }

    @Override
    public void load() {
        try {
            groupId = backend.resolveGroupId(identifier);
            loaded = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<String> permissionsForContext(String contextKey) throws Exception {
        return repository.getGroupPermissions(requireGroupId(), contextKey);
    }

    @Override
    protected void replacePermissions(String contextKey, List<String> permissions) throws Exception {
        ensurePersisted();
        repository.replaceGroupPermissions(requireGroupId(), contextKey, permissions);
    }

    @Override
    protected Set<String> worldsForEntity() throws Exception {
        Set<String> worlds = new HashSet<>(repository.listGroupPermissionWorlds(requireGroupId()));
        worlds.addAll(repository.listGroupOptionWorlds(requireGroupId()));
        return worlds;
    }

    @Override
    protected String optionForContext(String option, String contextKey) throws Exception {
        int id = requireGroupId();
        return switch (option) {
            case "prefix" -> contextKey == null ? repository.loadGroup(id).getOptions().getPrefix() : null;
            case "suffix" -> contextKey == null ? repository.loadGroup(id).getOptions().getSuffix() : null;
            case "default" -> contextKey == null && repository.loadGroup(id).isDefaultGroup() ? "true" : null;
            case "weight" -> contextKey == null ? Integer.toString(repository.loadGroup(id).getWeight()) : null;
            case "rank" -> contextKey == null
                    ? repository.findLadderRank(id).map(r -> Integer.toString(r.position())).orElse(null)
                    : null;
            case "rank-ladder" -> contextKey == null
                    ? repository.findLadderRank(id).map(LocalSqlRepository.LadderRank::ladderName).orElse(null)
                    : null;
            default -> repository.getGroupEntityOptions(id, contextKey).get(option);
        };
    }

    @Override
    protected Map<String, String> optionsForContext(String contextKey) throws Exception {
        Map<String, String> out = new java.util.LinkedHashMap<>(repository.getGroupEntityOptions(requireGroupId(), contextKey));
        if (contextKey == null) {
            for (String key : List.of("prefix", "suffix", "default", "weight", "rank", "rank-ladder")) {
                String value = optionForContext(key, null);
                if (value != null) {
                    out.putIfAbsent(key, value);
                }
            }
        }
        return out;
    }

    @Override
    protected void setEntityOption(String option, String value, String contextKey) throws Exception {
        ensurePersisted();
        int id = requireGroupId();
        if ("default".equals(option) && contextKey == null) {
            repository.upsertGroup(identifier, repository.loadGroup(id).getWeight(), Boolean.parseBoolean(value));
            return;
        }
        if ("weight".equals(option) && contextKey == null && value != null) {
            repository.upsertGroup(identifier, Integer.parseInt(value), repository.loadGroup(id).isDefaultGroup());
            return;
        }
        if ("rank".equals(option) && contextKey == null && value != null) {
            String ladder = optionForContext("rank-ladder", null);
            if (ladder == null) {
                ladder = "default";
            }
            repository.setGroupLadderRank(id, ladder, Integer.parseInt(value));
            return;
        }
        if ("rank-ladder".equals(option) && contextKey == null) {
            if (value == null || value.isEmpty() || "default".equals(value)) {
                repository.clearGroupLadder(id);
            } else {
                String rank = optionForContext("rank", null);
                int position = rank != null ? Integer.parseInt(rank) : 1;
                repository.setGroupLadderRank(id, value, position);
            }
            return;
        }
        repository.setGroupOption(id, option, value, contextKey);
    }

    @Override
    protected List<String> parentsForContext(String contextKey) throws Exception {
        if (contextKey != null) {
            return List.of();
        }
        return repository.getGroupParents(requireGroupId());
    }

    @Override
    protected void replaceParents(String contextKey, List<String> parents) throws Exception {
        if (contextKey != null) {
            return;
        }
        ensurePersisted();
        int id = requireGroupId();
        repository.clearGroupParents(id);
        for (String parent : parents) {
            repository.findGroupId(parent).ifPresent(parentId -> {
                try {
                    repository.setGroupParent(id, parentId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    protected boolean entityExists() throws Exception {
        return repository.findGroupId(identifier).isPresent();
    }

    @Override
    protected void persist() throws Exception {
        if (!entityExists()) {
            groupId = repository.upsertGroup(identifier, 0, false);
        }
    }

    @Override
    protected void deleteEntity() throws Exception {
        repository.deleteGroup(requireGroupId());
    }

    private void ensurePersisted() throws Exception {
        if (!entityExists()) {
            groupId = repository.upsertGroup(identifier, 0, false);
        }
    }

    private int requireGroupId() throws Exception {
        if (groupId == null) {
            groupId = backend.resolveGroupId(identifier);
        }
        return groupId;
    }
}
