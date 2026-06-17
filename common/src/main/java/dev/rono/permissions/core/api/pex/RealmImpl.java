package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.realm.Realm;
import dev.rono.permissions.core.DefaultPermissionManager;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class RealmImpl implements Realm {

    private final String name;
    private final DefaultPermissionManager manager;
    private final PermissionHolder holder;

    RealmImpl(String name, DefaultPermissionManager manager) {
        this.name = name;
        this.manager = manager;
        this.holder = new WorldPermissionHolder(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PermissionHolder asHolder() {
        return holder;
    }

    @Override
    public List<String> parents() {
        return List.copyOf(manager.getWorldInheritance(name));
    }

    @Override
    public void setParents(List<String> parentNames) {
        manager.setWorldInheritance(name, parentNames == null ? List.of() : List.copyOf(parentNames));
    }

    @Override
    public void addParent(String parentName) {
        if (parentName == null || parentName.isBlank()) {
            return;
        }
        var parents = new ArrayList<>(parents());
        if (!parents.contains(parentName)) {
            parents.add(parentName);
            setParents(parents);
        }
    }

    @Override
    public void removeParent(String parentName) {
        if (parentName == null || parentName.isBlank()) {
            return;
        }
        var parents = new ArrayList<>(parents());
        if (parents.remove(parentName)) {
            setParents(parents);
        }
    }

    @Override
    public List<String> parentTree() {
        Set<String> visited = new LinkedHashSet<>();
        List<String> queue = new ArrayList<>(parents());
        int index = 0;
        while (index < queue.size()) {
            String current = queue.get(index++);
            if (!visited.add(current)) {
                continue;
            }
            for (String parent : manager.getWorldInheritance(current)) {
                if (!visited.contains(parent) && !queue.contains(parent)) {
                    queue.add(parent);
                }
            }
        }
        visited.retainAll(queue);
        return List.copyOf(visited);
    }
}
