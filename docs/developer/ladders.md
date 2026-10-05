---
sidebar_position: 3
---

# Ladders

Ladders are named promotion tracks. Each group stores a numeric **rank** and a **rank-ladder** name.

:::important Rank ordering
In PermissionsExPlus, **lower rank numbers are higher on the ladder**. Promoting moves a player toward a *smaller* rank number. Rank `0` (or unset) means the group is not on the ladder.
:::

Example progression (`default` ladder):

| Group | Rank | Meaning |
|-------|------|---------|
| `elite` | 100 | Top of ladder |
| `trusted` | 200 | |
| `member` | 300 | |
| `newcomer` | 400 | Bottom of ladder |

## Assign a group to a ladder

```java
PermissionGroup newcomer = manager.getGroup("newcomer");
newcomer.setRank(400);
newcomer.setRankLadder("default");

PermissionGroup member = manager.getGroup("member");
member.setRank(300);
member.setRankLadder("default");

PermissionGroup trusted = manager.getGroup("trusted");
trusted.setRank(200);
trusted.setRankLadder("default");

PermissionGroup elite = manager.getGroup("elite");
elite.setRank(100);
elite.setRankLadder("default");
```

Helpers:

```java
boolean onLadder = group.isRanked(); // rank > 0
int rank = group.getRank();
String ladder = group.getRankLadder(); // defaults to "default"
```

### Remove from a ladder

```java
group.setRank(0); // clears the rank option
group.setRankLadder("default");
```

### Multiple ladders

```java
manager.getGroup("bronze").setRank(300);
manager.getGroup("bronze").setRankLadder("donor");

manager.getGroup("silver").setRank(200);
manager.getGroup("silver").setRankLadder("donor");

manager.getGroup("gold").setRank(100);
manager.getGroup("gold").setRankLadder("donor");
```

A group participates in **one** ladder name at a time (`rank-ladder` option). Use separate groups for separate tracks.

## Read a ladder

```java
Map<Integer, PermissionGroup> ladder = manager.getRankLadder("default");
for (Map.Entry<Integer, PermissionGroup> entry : ladder.entrySet()) {
    getLogger().info(entry.getKey() + " → " + entry.getValue().getIdentifier());
}
```

Empty map means no ranked groups use that ladder name.

Next: [Promote / demote](promote-demote). For the command-side walkthrough, see [Ranks & Ladders](/docs/next/concepts-guides/ranks-ladders).
