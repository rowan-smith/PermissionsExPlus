---
sidebar_position: 4
---

# Promote / demote

Move users along a [ladder](ladders) with `promote` / `demote`.

## Promote and demote

```java
PermissionUser user = manager.getUser(player);

try {
    // null promoter = console / plugin (no rank check on promoter)
    PermissionGroup next = user.promote(null, "default");
    getLogger().info(user.getName() + " promoted to " + next.getIdentifier());
} catch (RankingException e) {
    getLogger().warning("Cannot promote: " + e.getMessage());
}

try {
    PermissionGroup prev = user.demote(null, "default");
    getLogger().info(user.getName() + " demoted to " + prev.getIdentifier());
} catch (RankingException e) {
    getLogger().warning("Cannot demote: " + e.getMessage());
}
```

With a player promoter (enforces that the promoter outranks the target on that ladder):

```java
PermissionUser staff = manager.getUser(moderatorPlayer);
PermissionUser target = manager.getUser(targetPlayer);

try {
    target.promote(staff, "default");
} catch (RankingException e) {
    moderatorPlayer.sendMessage("You cannot promote that player: " + e.getMessage());
}
```

Promote/demote swaps the user's current ladder group for the next/previous ranked group. Other groups are left alone.

## Bulk promote online players

```java
for (Player player : Bukkit.getOnlinePlayers()) {
    PermissionUser member = manager.getUser(player);
    if (!member.inGroup("newcomer", false)) {
        continue;
    }
    try {
        member.promote(null, "default");
        player.sendMessage("You have been promoted!");
    } catch (RankingException ignored) {
    }
}
```

## Inspect ladder position

```java
boolean onDefault = user.isRanked("default");
int rank = user.getRank("default");
PermissionGroup current = user.getRankLadderGroup("default");

Map<String, PermissionGroup> ladders = user.getRankLadders();
// ladder name → group the user is in on that ladder
```

## Complete setup example

```java
PermissionManager manager = PermissionsEx.getPermissionManager();

String[] names = { "elite", "trusted", "member", "newcomer" };
int[] ranks = { 100, 200, 300, 400 };
int[] weights = { 30, 20, 10, 0 };

for (int i = 0; i < names.length; i++) {
    PermissionGroup g = manager.getGroup(names[i]);
    g.setRank(ranks[i]);
    g.setRankLadder("default");
    g.setWeight(weights[i]);
    if (g.isVirtual()) {
        g.save();
    }
}

manager.getGroup("member").setParents(List.of(manager.getGroup("newcomer")));
manager.getGroup("trusted").setParents(List.of(manager.getGroup("member")));
manager.getGroup("elite").setParents(List.of(manager.getGroup("trusted")));

PermissionUser user = manager.getUser(player);
user.setGroups(new String[] { "newcomer" });
user.promote(null, "default"); // → member
```
