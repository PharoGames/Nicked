# Messages

Every player-facing message in Nicked is defined in `plugins/Nicked/messages.yml`. All messages use **MiniMessage** format, and dynamic values are injected via `{placeholder}` tokens.

---

## MiniMessage Format

MiniMessage is a string-based rich-text format for Adventure. Tags are written with angle brackets.

| Tag | Effect | Example |
|---|---|---|
| `<red>` | Red text | `<red>Error!` |
| `<green>` | Green text | `<green>Success!` |
| `<white>` | White text | `<white>{nick}` |
| `<gray>` | Gray text | `<gray>Info` |
| `<aqua>` | Aqua/cyan text | `<aqua>{real_name}` |
| `<yellow>` | Yellow text | `<yellow>Warning` |
| `<bold>` | Bold | `<bold>Important` |
| `<italic>` | Italic | `<italic>note` |
| `<underlined>` | Underlined | `<underlined>link` |
| `<strikethrough>` | Strikethrough | `<strikethrough>old` |
| `<gradient:#hex1:#hex2>` | Gradient between two hex colours | `<gradient:#ff6b6b:#ffa502>Nicked` |
| `<color:#rrggbb>` | Arbitrary hex colour | `<color:#ff00ff>custom` |
| `<reset>` | Reset all formatting | `<reset>` |

For the full specification, see the [MiniMessage documentation](https://docs.advntr.dev/minimessage/format.html).

---

## Full Default Messages

```yaml
# ============================================================
# Nicked - Messages
# All messages use MiniMessage format.
# Placeholders are wrapped in {curly_braces}.
# ============================================================

prefix: "<gray>[<gradient:#ff6b6b:#ffa502>Nicked</gradient>]</gray> "

nick_applied: "<green>You are now nicked as <white>{nick}</white>."
nick_removed: "<green>Your nick has been removed. You are now <white>{name}</white> again."

nick_other_applied: "<green>Nicked <white>{target}</white> as <white>{nick}</white>."

nick_all_applied: "<green>All online players have been randomly nicked."

unnick_not_nicked: "<red>You are not currently nicked."
unnick_other_not_nicked: "<red><white>{target}</white> is not currently nicked."

realname_result: "<gray><white>{nick}</white> is really <aqua>{real_name}</aqua>."
realname_not_nicked: "<gray><white>{player}</white> is not nicked."

no_permission: "<red>You do not have permission to do that."
player_only: "<red>This command can only be used by players."
invalid_name: "<red>'<white>{name}</white>' is not a valid Minecraft username (3-16 chars, alphanumeric + underscores)."
player_not_found: "<red>Player '<white>{name}</white>' not found."
skin_fetch_failed: "<yellow>Could not fetch skin for '<white>{nick}</white>'. Nick applied without skin change."

usage_nick: "<red>Usage: /nick <name> or /nick (to random nick / unnick)"
usage_nickother: "<red>Usage: /nickother <player> <nick>"
usage_realname: "<red>Usage: /realname <player>"

nick_cancelled: "<red>Nick change was cancelled by another plugin."
no_random_pool: "<red>No names available in the random nick pool and no players are online."
```

---

## Message Reference

### `prefix`

The prefix prepended to most plugin messages.

**Default:** `<gray>[<gradient:#ff6b6b:#ffa502>Nicked</gradient>]</gray> `  
**Placeholders:** None

---

### `nick_applied`

Sent to a player when they are successfully nicked.

**Default:** `<green>You are now nicked as <white>{nick}</white>.`

| Placeholder | Value |
|---|---|
| `{nick}` | The nick name that was applied |

---

### `nick_removed`

Sent to a player when their nick is removed.

**Default:** `<green>Your nick has been removed. You are now <white>{name}</white> again.`

| Placeholder | Value |
|---|---|
| `{name}` | The player's real username |

---

### `nick_other_applied`

Sent to the command sender after `/nickother` succeeds.

**Default:** `<green>Nicked <white>{target}</white> as <white>{nick}</white>.`

| Placeholder | Value |
|---|---|
| `{target}` | The real name of the player who was nicked |
| `{nick}` | The nick name that was applied |

---

### `nick_all_applied`

Sent to the command sender after `/nickall` completes.

**Default:** `<green>All online players have been randomly nicked.`  
**Placeholders:** None

---

### `unnick_not_nicked`

Sent when `/unnick` is run but the player is not nicked.

**Default:** `<red>You are not currently nicked.`  
**Placeholders:** None

---

### `unnick_other_not_nicked`

Sent when a target player is not nicked (used in contexts where unnicking another player is attempted).

**Default:** `<red><white>{target}</white> is not currently nicked.`

| Placeholder | Value |
|---|---|
| `{target}` | The real name of the target player |

---

### `realname_result`

Sent to the command sender when `/realname` finds a nicked player.

**Default:** `<gray><white>{nick}</white> is really <aqua>{real_name}</aqua>.`

| Placeholder | Value |
|---|---|
| `{nick}` | The nick name the player is currently using |
| `{real_name}` | The player's real username |

---

### `realname_not_nicked`

Sent when `/realname` is used on a player who is not nicked.

**Default:** `<gray><white>{player}</white> is not nicked.`

| Placeholder | Value |
|---|---|
| `{player}` | The name that was searched |

---

### `no_permission`

Sent when the command sender lacks the required permission.

**Default:** `<red>You do not have permission to do that.`  
**Placeholders:** None

---

### `player_only`

Sent when a player-only command is run from the console.

**Default:** `<red>This command can only be used by players.`  
**Placeholders:** None

---

### `invalid_name`

Sent when a provided nick name fails strict name validation.

**Default:** `<red>'<white>{name}</white>' is not a valid Minecraft username (3-16 chars, alphanumeric + underscores).`

| Placeholder | Value |
|---|---|
| `{name}` | The invalid name that was submitted |

---

### `player_not_found`

Sent when a referenced player cannot be found online.

**Default:** `<red>Player '<white>{name}</white>' not found.`

| Placeholder | Value |
|---|---|
| `{name}` | The name that was searched |

---

### `skin_fetch_failed`

Sent when the Mojang API cannot be reached or returns no skin for the nick name. The nick is still applied — only the skin is missing.

**Default:** `<yellow>Could not fetch skin for '<white>{nick}</white>'. Nick applied without skin change.`

| Placeholder | Value |
|---|---|
| `{nick}` | The nick name whose skin could not be fetched |

---

### `usage_nick`

Shown when `/nick` is called with the wrong number of arguments.

**Default:** `<red>Usage: /nick <name> or /nick (to random nick / unnick)`  
**Placeholders:** None

---

### `usage_nickother`

Shown when `/nickother` is called with the wrong number of arguments.

**Default:** `<red>Usage: /nickother <player> <nick>`  
**Placeholders:** None

---

### `usage_realname`

Shown when `/realname` is called with the wrong number of arguments.

**Default:** `<red>Usage: /realname <player>`  
**Placeholders:** None

---

### `nick_cancelled`

Sent to the player or command sender when a [`NickChangeEvent`](api/events.md#nickchangeevent) fired by another plugin was cancelled.

**Default:** `<red>Nick change was cancelled by another plugin.`  
**Placeholders:** None

---

### `no_random_pool`

Sent when a random nick is requested but the combined pool (configured names + online players) is empty.

**Default:** `<red>No names available in the random nick pool and no players are online.`  
**Placeholders:** None

---

## Customisation Tips

### Removing the prefix

Set the `prefix` key to an empty string to remove it from all messages:

```yaml
prefix: ""
```

### Custom colour scheme

Replace the gradient with a flat colour, or pick your own hex values:

```yaml
prefix: "<dark_aqua>[<bold>Nicked</bold>]</dark_aqua> "
```

### Adding hover or click events

MiniMessage supports hover and click events:

```yaml
nick_applied: "<green>You are nicked as <white><hover:show_text:'<gray>Use /unnick to remove'>{nick}</hover></white>."
```

### Translating messages

All messages are plain strings — you can translate them into any language by replacing the values in `messages.yml`. Placeholders like `{nick}` must remain exactly as-is (curly braces, no spaces).
