package dev.rono.permissions.core.event;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.event.group.GroupCreatedEvent;
import dev.rono.permissions.api.group.Group;

@Value
@Accessors(fluent = true)
public class GroupCreatedEventImpl implements GroupCreatedEvent {
    Group group;

    public GroupCreatedEventImpl(Group group) {
        this.group = group;
    }
}
