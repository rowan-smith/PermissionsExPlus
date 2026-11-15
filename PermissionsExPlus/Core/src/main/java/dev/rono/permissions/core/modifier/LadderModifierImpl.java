package dev.rono.permissions.core.modifier;

import lombok.var;

import dev.rono.permissions.api.ladder.Ladder;
import dev.rono.permissions.api.ladder.LadderModifier;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.core.model.LadderSnapshot;

import java.util.ArrayList;
import java.util.List;

public final class LadderModifierImpl implements LadderModifier {
    private final List<String> groups;

    public LadderModifierImpl(Ladder ladder) {
        groups = new ArrayList<>(ladder.groups());
    }

    @Override
    public LadderModifier add(String group) {
        var value = Identifiers.group(group);

        if (!groups.contains(value)) {
            groups.add(value);
        }

        return this;
    }

    @Override
    public LadderModifier insert(int index, String group) {
        var value = Identifiers.group(group);

        groups.remove(value);
        groups.add(index, value);

        return this;
    }

    @Override
    public LadderModifier remove(String group) {
        groups.remove(Identifiers.group(group));
        return this;
    }

    @Override
    public LadderModifier move(String group, int index) {
        var value = Identifiers.group(group);

        if (!groups.remove(value)) {
            throw new IllegalArgumentException("Group is not on ladder: " + value);
        }

        groups.add(index, value);

        return this;
    }

    @Override
    public LadderModifier setGroups(List<String> values) {
        groups.clear();

        values.forEach(this::add);

        return this;
    }

    @Override
    public LadderModifier clear() {
        groups.clear();
        return this;
    }

    public LadderSnapshot build(Ladder previous) {
        return new LadderSnapshot(previous.name(), groups);
    }
}
