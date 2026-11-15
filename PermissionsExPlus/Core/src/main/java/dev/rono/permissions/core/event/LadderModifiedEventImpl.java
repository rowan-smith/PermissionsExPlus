package dev.rono.permissions.core.event;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.event.ladder.LadderModifiedEvent;
import dev.rono.permissions.api.ladder.Ladder;

@Value
@Accessors(fluent = true)
public class LadderModifiedEventImpl implements LadderModifiedEvent {
    Ladder previous;
    Ladder current;

    public LadderModifiedEventImpl(Ladder previous, Ladder current) {
        this.previous = previous;
        this.current = current;
    }
}
