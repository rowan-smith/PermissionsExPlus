---
sidebar_position: 5
---

# Inheritance

Inheritance allows groups and users to inherit permissions from other groups.

## Group Inheritance

Groups can have "parent" groups. When a group inherits from a parent, it gains all permissions and options of that parent, unless overridden.

## User Inheritance

Users inherit permissions from the groups they are members of.

## Circular Dependency Check

The engine automatically detects and prevents circular inheritance loops.

## Inheritance Depth

The maximum depth of inheritance can be configured in `advanced.yml`.
