---
layout: default
title: Getting Started
permalink: /
description: Install PermissionsExPlus and set up permissions on your Minecraft server.
---

PermissionsExPlus (PEX) is a permissions plugin for Minecraft servers. It controls **who can do what** by assigning **permissions** to **users** and **groups**.

<div class="version-cards">
  <a class="version-card" href="https://github.com/{{ site.repo }}/releases">
    <span class="version-card-label">Download v{{ site.version }}</span>
    <span class="version-card-desc">Get the latest release jar</span>
  </a>
  <a class="version-card" href="{{ site.baseurl }}/guides/recipes/">
    <span class="version-card-label">Common Setups</span>
    <span class="version-card-desc">Staff ranks, VIP, survival — copy-paste recipes</span>
  </a>
  <a class="version-card" href="{{ site.baseurl }}/commands/general/">
    <span class="version-card-label">Command Reference</span>
    <span class="version-card-desc">Full /pex command documentation</span>
  </a>
</div>

## Install in 4 steps

1. Download **`PermissionsExPlus-{{ site.version }}.jar`** from [GitHub Releases](https://github.com/{{ site.repo }}/releases).
2. Remove any old PermissionsEx jars from `plugins/`.
3. Copy the jar into `plugins/` and restart the server.
4. Run `/pex` in-game — you should see the help menu.

See [Requirements]({{ site.baseurl }}/requirements/) for Java and platform details.

## Your first permissions

**Create a group, give it permissions, assign a player:**

```text
/pex group admin create
/pex group admin add permissions.*
/pex group admin add '*'
/pex group admin prefix &c[Admin]
/pex user Steve group set admin
```

**Give one player a single permission:**

```text
/pex user Alex add essentials.home
```

**Grant a temporary permission (7 days):**

```text
/pex user Alex timed add essentials.fly 7d
```

## Core ideas

| Concept | Summary | Learn more |
|---------|---------|------------|
| **Groups** | Bundles of permissions (admin, vip, default) | [Group commands]({{ site.baseurl }}/commands/groups/) |
| **Inheritance** | Groups inherit permissions from parent groups | [Inheritance]({{ site.baseurl }}/concepts/inheritance/) |
| **Context** | Different permissions per world | [Context]({{ site.baseurl }}/concepts/context/) |
| **Weight** | Which group's prefix wins in chat | [Weight]({{ site.baseurl }}/concepts/weight/) |

## Documentation map

| I want to… | Go to |
|------------|-------|
| Set up a staff hierarchy | [Common Setups]({{ site.baseurl }}/guides/recipes/) |
| Learn every command | [Commands]({{ site.baseurl }}/commands/general/) |
| Edit config files | [Configuration]({{ site.baseurl }}/configuration/) |
| Fix something broken | [Troubleshooting]({{ site.baseurl }}/guides/troubleshooting/) |
| Integrate my plugin | [API Cookbook]({{ site.baseurl }}/developers/cookbook/) |
| Browse API classes | [Javadoc]({{ site.baseurl }}/developers/reference/) |

## Get help

- [GitHub Issues](https://github.com/{{ site.repo }}/issues) — bug reports and questions
- `/pex report` — generate a diagnostic report in-game
- [Migration guide]({{ site.baseurl }}/faq/migration/) — switching from another permissions plugin
