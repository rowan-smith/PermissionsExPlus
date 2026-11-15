package dev.rono.permissions.api.ladder;

import lombok.var;

import dev.rono.permissions.api.util.Identifiers;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/** A ladder whose name and contained group names are lowercase-normalized. */
public interface Ladder {

    String name();

    /**
     * Returns group names from lowest to highest position.
     */
    List<String> groups();

    default int size() {
        return groups().size();
    }

    default boolean contains(String groupName) {
        return groups().contains(Identifiers.group(groupName));
    }

    default OptionalInt positionOf(String groupName) {
        int index = groups().indexOf(Identifiers.group(groupName));

        return index < 0 ? OptionalInt.empty() : OptionalInt.of(index);
    }

    default Optional<String> next(String group) {
        var position = positionOf(group);

        if (!position.isPresent()) {
            return Optional.empty();
        }

        int next = position.getAsInt() + 1;

        if (next >= groups().size()) {
            return Optional.empty();
        }

        return Optional.of(groups().get(next));
    }

    default Optional<String> previous(String group) {
        var position = positionOf(group);

        if (!position.isPresent()) {
            return Optional.empty();
        }

        int previous = position.getAsInt() - 1;

        if (previous < 0) {
            return Optional.empty();
        }

        return Optional.of(groups().get(previous));
    }
}
