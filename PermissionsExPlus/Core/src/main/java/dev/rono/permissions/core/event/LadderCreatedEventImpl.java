package dev.rono.permissions.core.event;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.event.ladder.LadderCreatedEvent;
import dev.rono.permissions.api.ladder.Ladder;

@Value
@Accessors(fluent = true)
public class LadderCreatedEventImpl implements LadderCreatedEvent {
    Ladder ladder;

    public LadderCreatedEventImpl(Ladder ladder) {
        this.ladder = ladder;
    }
}
