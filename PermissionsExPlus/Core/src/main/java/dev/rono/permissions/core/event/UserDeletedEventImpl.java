package dev.rono.permissions.core.event;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.event.user.UserDeletedEvent;
import dev.rono.permissions.api.user.User;

@Value
@Accessors(fluent = true)
public class UserDeletedEventImpl implements UserDeletedEvent {
    User user;

    public UserDeletedEventImpl(User user) {
        this.user = user;
    }
}
