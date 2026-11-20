# Weight

Weights determine the priority of groups during inheritance and resolution.

## How Weight Works

- Groups with **higher** weight values take precedence over groups with lower values.
- If a user is in multiple groups, the options (like prefix) from the group with the highest weight will be chosen.
- Weight is stored as an option node.

## Setting Weight

```bash
/pex group admin option set weight 100
```
