package dev.rono.permissions.core.model;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.ladder.Ladder;
import dev.rono.permissions.api.util.Identifiers;

import java.util.List;
import dev.rono.permissions.core.util.Immutables;

@Value
@Accessors(fluent = true)
public class LadderSnapshot implements Ladder {
    String name;
    List<String> groups;

    public LadderSnapshot(String name, List<String> groups) {
        name = Identifiers.ladder(name);
        groups = groups.stream().map(Identifiers::group).collect(Immutables.toList());

        this.name = name;
        this.groups = groups;
    }
}
