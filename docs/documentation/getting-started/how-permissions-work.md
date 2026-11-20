---
sidebar_position: 3
---

# How Permissions Work

PermissionsExPlus uses a modern resolution engine to determine if a user has a specific permission.

## Resolution Logic

1. **User Direct Permissions**: Checks if the user has the permission directly assigned.
2. **Inheritance**: Checks groups assigned to the user, following the inheritance tree.
3. **Contextual Nodes**: Permissions can be specific to a world, server, or other custom contexts.
4. **Wildcards**: Supports standard `*` wildcards and negation (`-permission`).
5. **Precedence**: 
   - Exact matches beat wildcard matches.
   - Explicit deny tie-breaking.
   - Group weights determine inheritance order.
