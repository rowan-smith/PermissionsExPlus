package dev.rono.permissions.core.event;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.event.group.GroupModifiedEvent;
import dev.rono.permissions.api.group.Group;

@Value
@Accessors(fluent = true)
public class GroupModifiedEventImpl implements GroupModifiedEvent {
    Group previous;
    Group current;

    public GroupModifiedEventImpl(Group previous, Group current) {
        this.previous = previous;
        this.current = current;
    }
}
