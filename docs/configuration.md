# Configuration

Nicked's behaviour is controlled by `plugins/Nicked/config.yml`, which is generated with defaults on first start. You can edit it while the server is running, but changes require a server restart (or plugin reload if your server supports it) to take effect.

---

## Full Default Config

```yaml
# ============================================================
# Nicked - Configuration
# ============================================================

# ============================================================
# DANGER ZONE - READ CAREFULLY BEFORE ENABLING
# ============================================================
# When enabled, Nicked will modify the server-side GameProfile
# for each nicked player via reflection. This means:
#   - ALL other plugins will see the fake name as the real name
#   - /tp, /msg, /give, etc. will resolve to the fake name
#   - Plugins that store data by player name WILL be confused
#   - Data corruption is possible if other plugins save by name
# This is disabled by default. USE AT YOUR OWN RISK.
# ============================================================
internal_name_change:
  enabled: false

# Keep nicks active after the server restarts.
# Nicks are written to plugins/Nicked/nicks.yml on change.
persist_across_restarts: false

# Keep nicks active when a player logs out and back in.
# If persist_across_restarts is also true, this survives restarts too.
persist_across_logout: false

# Also change the skin for the nicked player themselves
# (requires a brief respawn packet sent to the player).
skin_change_for_self: true

# Enforce that nick names are valid Minecraft usernames:
# 3-16 characters, alphanumeric and underscores only.
strict_name_validation: true

# How long to cache fetched skins from the Mojang API (in minutes).
# Mojang rate limits at 600 requests per 10 minutes.
# Must be greater than 0.
skin_cache_ttl_minutes: 30

# Pool of names to pick from for /nick (no args) and /nickall.
# Leave empty to fall back to currently online player names.
random_nick_pool:
  - Notch
  - jeb_
  - Dinnerbone
  - Herobrine
  - Dream
  - Technoblade
```

---

## Option Reference

### `internal_name_change.enabled`

| Key | Type | Default |
|---|---|---|
| `internal_name_change.enabled` | Boolean | `false` |

!!! danger "Danger zone"
    Enabling this option modifies the server-side `GameProfile` for each nicked player using Java reflection. The consequences are significant:

    - Every other plugin on the server will see the fake name as if it were the player's real name.
    - Built-in commands such as `/tp`, `/msg`, `/give`, and `/kick` will resolve to the fake name.
    - Plugins that store player data keyed by username (not UUID) **will be confused** and may corrupt their data.
    - When Nicked is disabled or the player is unnicked, the original `GameProfile` is restored. However, any external data that was written during the nick session using the fake name will not be corrected automatically.

This option exists for rare use cases where full server-side name hiding is required and the implications are understood and accepted. Leave it `false` unless you are certain you need it.

---

### `persist_across_restarts`

| Key | Type | Default |
|---|---|---|
| `persist_across_restarts` | Boolean | `false` |

When `true`, the plugin writes each nick change to `plugins/Nicked/nicks.yml` and reads it back when the server starts. Players who were nicked when the server shut down will have their nick reapplied 5 ticks after they log in.

The stored data includes the nicked name, the original skin, the nicked skin, the cause, and the timestamp.

---

### `persist_across_logout`

| Key | Type | Default |
|---|---|---|
| `persist_across_logout` | Boolean | `false` |

When `true`, a player's nick is kept in memory even after they disconnect. When they rejoin, the nick is reapplied automatically (5-tick delay to allow the join process to complete).

Combine with `persist_across_restarts: true` to survive both logouts **and** server restarts.

| `persist_across_logout` | `persist_across_restarts` | Nick survives… |
|---|---|---|
| `false` | `false` | Nothing — nick is always lost on logout |
| `true` | `false` | Player disconnecting and reconnecting in the same server session |
| `true` | `true` | Player disconnecting and reconnecting, plus full server restarts |

---

### `skin_change_for_self`

| Key | Type | Default |
|---|---|---|
| `skin_change_for_self` | Boolean | `true` |

When `true`, Nicked sends the nicked player a sequence of packets (a `PLAYER_INFO_REMOVE` followed by `PLAYER_INFO_UPDATE`, and then a same-dimension `RESPAWN`) so they see their own new skin in first-person and on the pause screen. Without this, the player would see their old skin in their own client even though everyone else sees the new one.

The respawn packet approach causes a brief screen flash but does not teleport the player or change their inventory/state.

Set to `false` if you prefer to avoid the respawn packet, accepting that the nicked player will not see their own new skin locally.

---

### `strict_name_validation`

| Key | Type | Default |
|---|---|---|
| `strict_name_validation` | Boolean | `true` |

When `true`, nick names must match the regular expression `^[a-zA-Z0-9_]{3,16}$` — the same rules Mojang enforces for real Minecraft usernames:

- Minimum 3 characters
- Maximum 16 characters
- Only letters (A–Z, a–z), digits (0–9), and underscores (`_`)

Set to `false` to allow any string as a nick name. Note that non-standard names (e.g. names with spaces or special characters) may cause visual glitches in the tab list or chat on some client versions.

---

### `skin_cache_ttl_minutes`

| Key | Type | Default | Minimum |
|---|---|---|---|
| `skin_cache_ttl_minutes` | Integer | `30` | `1` |

Skins fetched from the Mojang API are cached in memory for this many minutes. When a cached entry expires and the same nick is applied again, a fresh request is made to the API.

Mojang's rate limit is **600 requests per 10 minutes** per IP address. With the default TTL of 30 minutes, a frequently used nick name will only incur one API call every 30 minutes regardless of how many players use it.

The plugin validates this value on startup and disables itself if it is set to `0` or a negative number.

---

### `random_nick_pool`

| Key | Type | Default |
|---|---|---|
| `random_nick_pool` | List of Strings | `[Notch, jeb_, Dinnerbone, Herobrine, Dream, Technoblade]` |

The list of names that `/nick` (no arguments) and `/nickall` draw from when picking a random nick.

At runtime, Nicked merges this list with the names of currently online players (deduplicated) to build the full candidate pool. The player's own real name is excluded from the pool to prevent them from being "nicked" as themselves.

If this list is empty **and** no other players are online (other than the player requesting the random nick), the command fails with the `no_random_pool` message.

```yaml
# Example: a large pool of well-known Minecraft personalities
random_nick_pool:
  - Notch
  - jeb_
  - Dinnerbone
  - Herobrine
  - Dream
  - Technoblade
  - xNestorio
  - Grian
  - Mumbo
  - iskall85
```

!!! tip
    You can listen to [`NickRandomSelectEvent`](api/events.md#nickrandomselectevent) in your own plugin to replace or override the selected name at runtime without modifying this file.
