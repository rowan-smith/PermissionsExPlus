package dev.rono.permissions.core.event;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.event.user.UserModifiedEvent;
import dev.rono.permissions.api.user.User;

@Value
@Accessors(fluent = true)
public class UserModifiedEventImpl implements UserModifiedEvent {
    User previous;
    User current;

    public UserModifiedEventImpl(User previous, User current) {
        this.previous = previous;
        this.current = current;
    }
}
