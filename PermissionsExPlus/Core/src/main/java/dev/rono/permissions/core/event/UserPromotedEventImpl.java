package dev.rono.permissions.core.event;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.event.user.UserPromotedEvent;
import dev.rono.permissions.api.ladder.PromotionResult;

@Value
@Accessors(fluent = true)
public class UserPromotedEventImpl implements UserPromotedEvent {
    PromotionResult result;

    public UserPromotedEventImpl(PromotionResult result) {
        this.result = result;
    }
}
