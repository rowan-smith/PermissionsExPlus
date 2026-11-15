package dev.rono.permissions.core.event;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.event.group.GroupDeletedEvent;
import dev.rono.permissions.api.group.Group;

@Value
@Accessors(fluent = true)
public class GroupDeletedEventImpl implements GroupDeletedEvent {
    Group group;

    public GroupDeletedEventImpl(Group group) {
        this.group = group;
    }
}
