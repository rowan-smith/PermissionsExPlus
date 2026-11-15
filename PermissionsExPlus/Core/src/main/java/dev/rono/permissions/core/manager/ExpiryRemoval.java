package dev.rono.permissions.core.manager;

import lombok.Value;
import lombok.experimental.Accessors;
import java.util.Objects;

/** One expired node removed from a cached permission holder. */
@Value
@Accessors(fluent = true)
public class ExpiryRemoval {
    String subjectType;
    String subject;
    String nodeType;
    String node;

    public ExpiryRemoval(String subjectType, String subject, String nodeType, String node) {
        Objects.requireNonNull(subjectType, "subjectType");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(nodeType, "nodeType");
        Objects.requireNonNull(node, "node");

        this.subjectType = subjectType;
        this.subject = subject;
        this.nodeType = nodeType;
        this.node = node;
    }
}
