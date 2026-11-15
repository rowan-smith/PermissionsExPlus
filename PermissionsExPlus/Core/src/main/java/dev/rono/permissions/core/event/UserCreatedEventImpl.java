package dev.rono.permissions.core.event;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.event.user.UserCreatedEvent;
import dev.rono.permissions.api.user.User;

@Value
@Accessors(fluent = true)
public class UserCreatedEventImpl implements UserCreatedEvent {
    User user;

    public UserCreatedEventImpl(User user) {
        this.user = user;
    }
}
