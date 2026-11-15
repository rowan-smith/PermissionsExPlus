package dev.rono.permissions.core.event;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.event.user.UserDemotedEvent;
import dev.rono.permissions.api.ladder.PromotionResult;

@Value
@Accessors(fluent = true)
public class UserDemotedEventImpl implements UserDemotedEvent {
    PromotionResult result;

    public UserDemotedEventImpl(PromotionResult result) {
        this.result = result;
    }
}
