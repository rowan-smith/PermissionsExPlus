package dev.rono.permissions.core.model;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.ladder.Ladder;
import dev.rono.permissions.api.ladder.PromotionResult;
import dev.rono.permissions.api.ladder.PromotionStatus;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.user.User;

import java.util.Optional;

@Value
@Accessors(fluent = true)
public class PromotionResultImpl implements PromotionResult {
    User user;
    Ladder ladder;
    Optional<ParentNode> previousMembership;
    Optional<ParentNode> currentMembership;
    PromotionStatus status;

    public PromotionResultImpl(User user, Ladder ladder, Optional<ParentNode> previousMembership, Optional<ParentNode> currentMembership, PromotionStatus status) {
        this.user = user;
        this.ladder = ladder;
        this.previousMembership = previousMembership;
        this.currentMembership = currentMembership;
        this.status = status;
    }
}
