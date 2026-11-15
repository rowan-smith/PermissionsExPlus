package dev.rono.permissions.core.event;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.event.ladder.LadderDeletedEvent;
import dev.rono.permissions.api.ladder.Ladder;

@Value
@Accessors(fluent = true)
public class LadderDeletedEventImpl implements LadderDeletedEvent {
    Ladder ladder;

    public LadderDeletedEventImpl(Ladder ladder) {
        this.ladder = ladder;
    }
}
