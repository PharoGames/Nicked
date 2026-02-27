# Commands

All Nicked commands are operator-gated by default. See [Permissions](permissions.md) for the full node list.

---

## `/nick`

Nick yourself, pick a random nick, or toggle your nick off.

```
/nick [name]
```

| Argument | Required | Description |
|---|---|---|
| `name` | No | The nick name to apply. Must be 3–16 characters, alphanumeric and underscores only (when `strict_name_validation` is enabled). |

**Permission:** `nicked.command.nick`  
**Sender:** Player only

### Behaviour

| Situation | What happens |
|---|---|
| No arguments, not currently nicked | A random name is picked from `random_nick_pool` and applied as your nick. |
| No arguments, already nicked | Your nick is removed (toggle). |
| One argument provided | You are nicked with that exact name. |

### Examples

```
# Nick yourself as "Notch"
/nick Notch

# Pick a random nick from the pool
/nick

# Remove your nick (when already nicked)
/nick
```

### Notes

- If the random pool is empty and no other players are online, the command fails with the `no_random_pool` message.
- The nick name is validated against `^[a-zA-Z0-9_]{3,16}$` when `strict_name_validation` is `true` in `config.yml`.
- The skin matching the nick name is fetched asynchronously from the Mojang API. If the fetch fails, the nick is still applied but without a skin change.
- Fires [`NickRandomSelectEvent`](api/events.md#nickrandomselectevent) when a random nick is picked (other plugins can override the selected name or cancel the operation).
- Fires [`NickChangeEvent`](api/events.md#nickchangeevent) before any change is made (cancellable).

---

## `/unnick`

Remove your active nick and restore your real name and skin.

```
/unnick
```

**Permission:** `nicked.command.unnick`  
**Sender:** Player only  
**Arguments:** None

### Behaviour

- If you are currently nicked, your nick is removed. Your real name reappears in chat, the tab list, and above your head. Your original skin is restored.
- If you are not currently nicked, you receive the `unnick_not_nicked` error message and nothing changes.

### Examples

```
/unnick
```

### Notes

- Fires [`NickRemoveEvent`](api/events.md#nickremoveevent) before removing the nick (cancellable). If another plugin cancels the event, your nick stays in place.

---

## `/nickall`

Randomly nick every online player at once.

```
/nickall
```

**Permission:** `nicked.command.nickall`  
**Sender:** Player or console  
**Arguments:** None

### Behaviour

- Iterates over all online players and applies a random nick to each one, exactly as if `/nick` (no arguments) had been run for each player individually.
- Each player's nick is drawn from the same `random_nick_pool` pool. Names are not deduplicated across players — two players may receive the same nick.
- Players who are already nicked receive a new random nick, replacing their previous one.
- The `nick_all_applied` message is sent to the command sender on completion.

### Examples

```
/nickall
```

### Notes

- Each player's random nick selection fires [`NickRandomSelectEvent`](api/events.md#nickrandomselectevent) individually, so event listeners can customise or cancel each player's random nick.
- Each nick change fires [`NickChangeEvent`](api/events.md#nickchangeevent). Cancelling it for a specific player skips that player while the rest continue.

---

## `/nickother`

Nick a specific online player with a specific name.

```
/nickother <player> <nick>
```

| Argument | Required | Description |
|---|---|---|
| `player` | Yes | The name of an online player. Tab-completes with online player names. |
| `nick` | Yes | The nick name to apply. Same validation rules as `/nick`. |

**Permission:** `nicked.command.nickother`  
**Sender:** Player or console

### Behaviour

- Looks up the target player by name.
- If the target is already nicked, their previous nick is replaced with the new one.
- On success, both the sender and the target player receive confirmation messages (`nick_other_applied`).
- Fails with `player_not_found` if no online player matches `<player>`.
- Fails with `invalid_name` if the nick name fails strict validation.

### Examples

```
# Nick the player "Steve" as "Notch"
/nickother Steve Notch

# Nick "Alex" as "jeb_"
/nickother Alex jeb_
```

### Notes

- Tab completion for the `<player>` argument filters online player names by the typed prefix.
- Fires [`NickChangeEvent`](api/events.md#nickchangeevent) before applying the nick; cancelling it aborts the operation and sends `nick_cancelled` to the sender.

---

## `/realname`

Reveal the real username of a nicked player.

```
/realname <player>
```

| Argument | Required | Description |
|---|---|---|
| `player` | Yes | A display name (nick or real name) of an online player. Tab-completes with current display names. |

**Permission:** `nicked.command.realname`  
**Sender:** Player or console

### Behaviour

- Searches online players by matching against both their nick name **and** their real name.
- If a match is found and the player is nicked, the real name is displayed using the `realname_result` message.
- If the matched player is not nicked, the `realname_not_nicked` message is sent instead.
- Fails with `player_not_found` if no online player matches the provided name.

### Examples

```
# Find out who "Dream" really is
/realname Dream

# Also works with the real name (shows "not nicked" if not nicked)
/realname Steve
```

### Notes

- Tab completion lists current display names (the nicked names are shown, not real names), so you can look up someone whose real name you do not know.
- The search is case-insensitive within the matching logic.
