package dev.rono.permissions.core.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
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

    /**
     * The stored revision this snapshot was read at, or 0 when it was not read
     * from revision-checked storage. Not part of the snapshot's value.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    long revision;

    public LadderSnapshot(String name, List<String> groups) {
        this(name, groups, 0);
    }

    public LadderSnapshot(String name, List<String> groups, long revision) {
        name = Identifiers.ladder(name);
        groups = groups.stream().map(Identifiers::group).collect(Immutables.toList());

        this.name = name;
        this.groups = groups;
        this.revision = revision;
    }
}
