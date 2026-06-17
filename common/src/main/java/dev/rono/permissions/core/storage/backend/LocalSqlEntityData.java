package dev.rono.permissions.core.storage.backend;

import dev.rono.permissions.core.storage.ContextKeyCodec;
import dev.rono.permissions.core.storage.LocalSqlRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class LocalSqlEntityData implements ru.tehkode.permissions.PermissionsData {

    protected final LocalSqlBackend backend;
    protected final LocalSqlRepository repository;
    protected final String identifier;
    protected boolean loaded;
    protected boolean dirty;

    protected LocalSqlEntityData(LocalSqlBackend backend, String identifier) {
        this.backend = backend;
        this.repository = backend.repository();
        this.identifier = identifier;
    }

    @Override
    public void load() {
        loaded = true;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public List<String> getPermissions(String worldName) {
        ensureLoaded();
        try {
            return permissionsForContext(ContextKeyCodec.encodeLegacyWorld(worldName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPermissions(List<String> permissions, String worldName) {
        ensureLoaded();
        try {
            replacePermissions(ContextKeyCodec.encodeLegacyWorld(worldName), permissions);
            dirty = true;
            backend.markDirty(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<String>> getPermissionsMap() {
        ensureLoaded();
        Map<String, List<String>> out = new HashMap<>();
        try {
            for (String world : getWorlds()) {
                out.put(world, getPermissions(world));
            }
            out.put(null, getPermissions(null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    @Override
    public Set<String> getWorlds() {
        ensureLoaded();
        try {
            return worldsForEntity();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getOption(String option, String worldName) {
        ensureLoaded();
        try {
            return optionForContext(option, ContextKeyCodec.encodeLegacyWorld(worldName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setOption(String option, String value, String world) {
        ensureLoaded();
        try {
            setEntityOption(option, value, ContextKeyCodec.encodeLegacyWorld(world));
            dirty = true;
            backend.markDirty(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> getOptions(String worldName) {
        ensureLoaded();
        try {
            return optionsForContext(ContextKeyCodec.encodeLegacyWorld(worldName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Map<String, String>> getOptionsMap() {
        Map<String, Map<String, String>> out = new HashMap<>();
        out.put(null, getOptions(null));
        for (String world : getWorlds()) {
            out.put(world, getOptions(world));
        }
        return out;
    }

    @Override
    public List<String> getParents(String worldName) {
        ensureLoaded();
        try {
            return parentsForContext(ContextKeyCodec.encodeLegacyWorld(worldName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setParents(List<String> parents, String worldName) {
        ensureLoaded();
        try {
            replaceParents(ContextKeyCodec.encodeLegacyWorld(worldName), parents);
            dirty = true;
            backend.markDirty(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove() {
        ensureLoaded();
        try {
            deleteEntity();
            backend.invalidateAfterSave(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<String>> getParentsMap() {
        Map<String, List<String>> out = new HashMap<>();
        out.put(null, getParents(null));
        for (String world : getWorlds()) {
            out.put(world, getParents(world));
        }
        return out;
    }

    @Override
    public void save() {
        if (!dirty) {
            return;
        }
        try {
            persist();
            dirty = false;
            backend.invalidateAfterSave(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isVirtual() {
        ensureLoaded();
        try {
            return !entityExists();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void ensureLoaded() {
        if (!loaded) {
            load();
        }
    }

    protected abstract List<String> permissionsForContext(String contextKey) throws Exception;
    protected abstract void replacePermissions(String contextKey, List<String> permissions) throws Exception;
    protected abstract Set<String> worldsForEntity() throws Exception;
    protected abstract String optionForContext(String option, String contextKey) throws Exception;
    protected abstract Map<String, String> optionsForContext(String contextKey) throws Exception;
    protected abstract void setEntityOption(String option, String value, String contextKey) throws Exception;
    protected abstract List<String> parentsForContext(String contextKey) throws Exception;
    protected abstract void replaceParents(String contextKey, List<String> parents) throws Exception;
    protected abstract boolean entityExists() throws Exception;
    protected abstract void deleteEntity() throws Exception;
    protected abstract void persist() throws Exception;
}
