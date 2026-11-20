---
sidebar_position: 5
---

# Adventure Support

PermissionsExPlus uses Kyori Adventure for component-based messages and modern text formatting.

When Adventure audience support is available, PermissionsExPlus messages can contain both traditional `&` color codes and MiniMessage tags. The two formats can be used together in the same message.

## Supported Formats

PermissionsExPlus supports:

- Legacy `&` color and formatting codes
- MiniMessage colors and decorations
- Hex colors
- Gradients and rainbow effects
- Hover and click events
- Mixed legacy and MiniMessage formatting

For example:

```text
&7Welcome, <gradient:#55ffff:#5555ff><bold>Player</bold></gradient>&7!
```

In this message:

- `&7` applies legacy gray formatting
- `<gradient:...>` applies a MiniMessage gradient
- `<bold>` applies a MiniMessage decoration

## Legacy Color Codes

Legacy formatting uses an ampersand followed by a color or decoration code.

```text
&c[Admin]&r
```

### Colors

| Code | Color       | Code | Color        |
|------|-------------|------|--------------|
| `&0` | Black       | `&8` | Dark Gray    |
| `&1` | Dark Blue   | `&9` | Blue         |
| `&2` | Dark Green  | `&a` | Green        |
| `&3` | Dark Aqua   | `&b` | Aqua         |
| `&4` | Dark Red    | `&c` | Red          |
| `&5` | Dark Purple | `&d` | Light Purple |
| `&6` | Gold        | `&e` | Yellow       |
| `&7` | Gray        | `&f` | White        |

### Decorations

| Code | Decoration    |
|------|---------------|
| `&k` | Obfuscated    |
| `&l` | Bold          |
| `&m` | Strikethrough |
| `&n` | Underlined    |
| `&o` | Italic        |
| `&r` | Reset         |

## MiniMessage

MiniMessage is a tag-based formatting language provided by Kyori Adventure.

```text
<red>[Admin]</red>
```

### Common Tags

| Tag            | Purpose          | Example                               |
|----------------|------------------|---------------------------------------|
| `<red>`        | Named color      | `<red>Warning</red>`                  |
| `<#55ffff>`    | Hex color        | `<#55ffff>Builder</#55ffff>`          |
| `<bold>`       | Bold text        | `<bold>Important</bold>`              |
| `<italic>`     | Italic text      | `<italic>Notice</italic>`             |
| `<underlined>` | Underlined text  | `<underlined>Rules</underlined>`      |
| `<gradient>`   | Color gradient   | `<gradient:red:gold>Admin</gradient>` |
| `<rainbow>`    | Rainbow coloring | `<rainbow>Owner</rainbow>`            |
| `<reset>`      | Reset formatting | `<reset>`                             |

MiniMessage tag names are case-insensitive, although lowercase tags are recommended.

## Prefix and Suffix Examples

Set a group prefix using legacy formatting:

```bash
/pex group admin prefix '&c[Admin]&r '
```

Set a group prefix using MiniMessage:

```bash
/pex group admin prefix '<red><bold>[Admin]</bold></red> '
```

Use both formats together:

```bash
/pex group admin prefix '&8[<gradient:red:gold><bold>Admin</bold></gradient>&8]&r '
```

:::note
PermissionsExPlus stores and exposes prefixes and suffixes. The plugin displaying them must support the selected formatting format.

A prefix retrieved as a plain string through Vault or PlaceholderAPI may not retain interactive component features.
:::

## Interactive Components

MiniMessage supports hover and click events:

```text
<hover:show_text:'<gray>Click to view the rules'>
  <click:run_command:'/rules'>
    <green>[Rules]</green>
  </click>
</hover>
```

Interactive components work in messages sent directly as Adventure components.

Whether they work in prefixes, suffixes, tab lists or scoreboards depends on the plugin displaying the value.

:::caution
Only allow trusted administrators to configure unrestricted MiniMessage.

MiniMessage can include interactive actions such as clickable commands and URLs.
:::

## How Messages Are Parsed

PermissionsExPlus processes formatted messages in the following order:

1. Legacy `&` color codes are parsed into an Adventure component.
2. The component is converted into MiniMessage formatting.
3. Native MiniMessage tags in the original message are restored.
4. The final MiniMessage string is parsed and sent as an Adventure component.

This allows legacy formatting and MiniMessage to coexist in one message.

## Fallback Behaviour

If Adventure audience support is unavailable, PermissionsExPlus falls back to the platform's standard string-based message API.

In fallback mode:

- Legacy colors may be converted for the platform
- MiniMessage tags are not interpreted
- Gradients, hover events and click events are unavailable

Check the server console if modern formatting is unexpectedly displayed as plain text.

## Limitations

- Interactive formatting is only retained when the receiving plugin handles Adventure components.
- Vault and PlaceholderAPI commonly expose metadata as strings.
- Older clients cannot display RGB colors exactly and may receive converted legacy colors.
- Text intended to display MiniMessage tags literally may require special handling.
- Invalid MiniMessage tags may be displayed as text or ignored.

## Kyori Adventure

[Kyori Adventure](https://github.com/KyoriPowered/adventure) provides the component system used for modern Minecraft messages.

Adventure supports:

- Structured text components
- Colors and decorations
- Hover and click events
- Legacy serialization
- MiniMessage parsing
- Platform-independent audience handling

## Related Pages

- [Prefix & Suffix](../concepts-guides/prefix-suffix)
- [Vault](vault)
- [PlaceholderAPI](placeholder-api/overview)
