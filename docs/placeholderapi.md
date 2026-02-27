# PlaceholderAPI

Nicked includes a built-in [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) expansion that exposes three placeholders under the `nicked` identifier.

---

## Setup

No manual registration is required. When the server starts, Nicked checks whether PlaceholderAPI is installed and, if so, registers the expansion automatically. You will see the following in the console:

```
[Nicked] Hooked into PlaceholderAPI.
```

If PlaceholderAPI is not installed, Nicked starts normally and the placeholders are simply unavailable. No errors are thrown.

**Requirements:**

- PlaceholderAPI 2.11.6 or newer installed as a separate plugin
- Nicked 1.0.0 or newer

The expansion persists across PlaceholderAPI reloads (`/papi reload`) because `persist()` returns `true`.

---

## Placeholders

### `%nicked_displayname%`

Returns the player's **display name** as resolved by the Nicked API.

- If the player is currently nicked, returns their **nick name**.
- If the player is not nicked, returns their **real username**.

This is equivalent to calling [`NickedAPI.getDisplayName(uuid)`](api/methods.md#getdisplayname) and fires [`NickResolveEvent`](api/events.md#nickresolveevent), meaning other plugins can override the value.

| Player state | Example output |
|---|---|
| Not nicked (real name: `Steve`) | `Steve` |
| Nicked as `Dream` | `Dream` |

---

### `%nicked_is_nicked%`

Returns a plain-string boolean indicating whether the player is currently nicked.

| Player state | Output |
|---|---|
| Not nicked | `false` |
| Nicked | `true` |

Useful in conditional logic supported by plugins like CMI, DeluxeMenus, or TAB.

---

### `%nicked_real_name%`

Returns the player's **real username**, regardless of nick state.

| Player state | Example output |
|---|---|
| Not nicked (real name: `Steve`) | `Steve` |
| Nicked as `Dream` (real name: `Steve`) | `Steve` |

This placeholder never returns the nick name — use `%nicked_displayname%` for that.

---

## Usage Examples

### Chat format (EssentialsX)

Show the nick name in chat while keeping the real name in logs:

```yaml
# EssentialsX config.yml
chat:
  format: '<{DISPLAYNAME}> {MESSAGE}'
```

Since Nicked sets the Bukkit display name directly, EssentialsX picks it up natively. For plugins that need the PAPI placeholder explicitly:

```
<%nicked_displayname%> {message}
```

### TAB plugin (tab list)

Display the nick name in the tab list header:

```yaml
# TAB config.yml
tablist-name: "%nicked_displayname%"
```

### Scoreboard (DeluxeMenus / FeatherBoard)

Show whether a player is disguised on their personal scoreboard:

```
&7Disguised: &f%nicked_is_nicked%
```

### Conditional menus (DeluxeMenus)

Show a "Remove Disguise" button only when the player is nicked:

```yaml
view_requirement:
  requirements:
    is_nicked:
      type: string equals
      input: '%nicked_is_nicked%'
      output: 'true'
```

### Staff tools

Display a player's real name next to their nick in a staff GUI:

```
&7Real name: &f%nicked_real_name%
&7Nick: &f%nicked_displayname%
```

### CMI holograms / signs

```
[Nicked as]
%nicked_displayname%
```

---

## Expansion Metadata

The registered expansion reports the following metadata to PlaceholderAPI:

| Property | Value |
|---|---|
| Identifier | `nicked` |
| Author | `Nicker` |
| Version | `1.0.0` |
| Persist across reloads | Yes |

You can verify registration with:

```
/papi info nicked
```

---

## Troubleshooting

### Placeholders show as `%nicked_displayname%` (not parsed)

- Confirm PlaceholderAPI is installed and enabled (`/papi list`).
- Confirm the Nicked expansion is registered (`/papi info nicked`).
- Confirm the plugin consuming the placeholder supports PAPI (most do, but some require an explicit `parse-placeholders: true` config option).

### `%nicked_is_nicked%` returns `false` even when a player is nicked

- Placeholders are evaluated per-player. Make sure the placeholder is being evaluated against the **correct** player's context, not a static string.
- If the nick was applied less than one server tick ago (e.g. in the same event handler), the state may not have propagated yet. This is extremely rare in normal usage.

### Placeholder returns the real name even when nicked

- Check that `%nicked_displayname%` is used (not `%nicked_real_name%`).
- If a `NickResolveEvent` listener in another plugin is overriding the resolved name, it may be replacing it with the real name.
