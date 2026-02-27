# Getting Started

This page walks you through installing Nicked, verifying it works, and taking it for a first spin.

---

## Requirements

Before installing, make sure you have the following:

- **Java 21** or newer on the server JVM
- **Spigot or Paper 1.20+** (Paper is recommended)
- No separate PacketEvents download needed — it is bundled and relocated inside the Nicked jar

---

## Installation

### 1. Download or build the jar

=== "Pre-built release"

    Download the latest `Nicked.jar` from the releases page and skip to step 2.

=== "Build from source"

    Clone the repository and run:

    ```bash
    ./gradlew build
    ```

    The output jar is placed at `build/libs/Nicked.jar` (the classifier-less jar produced by the Shadow task).

### 2. Drop it in the plugins folder

Copy `Nicked.jar` into your server's `plugins/` directory.

### 3. (Optional) Install PlaceholderAPI

If you want the `%nicked_*%` placeholders to work in chat plugins, scoreboards, or tab lists, install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) as well. Nicked detects it automatically — no extra configuration needed.

### 4. Start or restart the server

Nicked validates its configuration during `onEnable()`. If anything is wrong, it logs a descriptive error and disables itself rather than starting in a broken state. Check the console output for any warnings.

On a clean first run you will see something like:

```
[Nicked] Enabling Nicked v1.0.0
[Nicked] Hooked into PlaceholderAPI.   ← only if PAPI is installed
```

---

## Default Configuration

Two files are generated under `plugins/Nicked/` on first start:

| File | Purpose |
|---|---|
| `config.yml` | Feature toggles and behaviour settings |
| `messages.yml` | Every player-facing message in MiniMessage format |

A third file, `nicks.yml`, is created automatically when persistence is enabled and a nick is first saved.

See [Configuration](configuration.md) and [Messages](messages.md) for the full reference.

---

## First Use

By default all commands require the `nicked.admin` permission (or individual `nicked.command.*` nodes). OPs have this by default.

### Nick yourself

```
/nick Dream
```

Your name changes to **Dream** and the Mojang skin for "Dream" is fetched and applied to your profile. Everyone on the server — including yourself — sees the change immediately.

### Random nick

```
/nick
```

Running `/nick` with no arguments when you are not nicked picks a random name from the configured pool (see `random_nick_pool` in `config.yml`) and nicks you with it.

### Remove your nick

```
/nick
```

Running `/nick` with no arguments when you *are* already nicked removes your nick (toggle behaviour). You can also use the dedicated command:

```
/unnick
```

### Nick another player

```
/nickother Steve Notch
```

This nicks the online player **Steve** as **Notch**.

### Find someone's real name

```
/realname Notch
```

If a player called Notch (real name or nick name) is online, this reveals their real username.

---

## How Nicking Works Internally

Understanding the flow helps when integrating with other plugins or debugging unexpected behaviour.

```
/nick <name>
    │
    ├─ Validate name (if strict_name_validation = true)
    │
    ├─ Fire NickChangeEvent (cancellable)
    │    └─ if cancelled → stop, send nick_cancelled message
    │
    ├─ Store NickData immediately (isNicked() returns true right away)
    ├─ Set Bukkit display name + tab-list name
    │
    ├─ [async] Fetch skin from Mojang API
    │    └─ Update NickData with skin
    │    └─ Save to nicks.yml (if persistence enabled)
    │    └─ Apply GameProfile modification (if internal_name_change enabled)
    │    └─ Send PLAYER_INFO_UPDATE packets to all viewers
    │    └─ Send RESPAWN packet to self (if skin_change_for_self = true)
    │
    └─ Fire NickApplyEvent (on main thread, after skin step completes)
```

If the Mojang API cannot be reached or rate-limits the request, the nick is still applied but without a skin change, and the player receives the `skin_fetch_failed` message.

---

## Troubleshooting

### Nick is applied but skin does not change

- The Mojang API may be rate-limited. The skin cache TTL is 30 minutes by default — once the entry expires it will be re-fetched.
- Check that `skin_change_for_self` is `true` in `config.yml` if you want the nicked player to also see their own skin update.

### Other plugins still see the player's real name

This is expected behaviour by default. Nicked operates at the packet level and does not touch server-side data. To make the fake name visible server-side, enable `internal_name_change.enabled` in `config.yml`. Read the warning in that section carefully before doing so.

### Nicks are lost on restart

Enable `persist_across_restarts: true` in `config.yml`.

### Nicks are lost when a player disconnects

Enable `persist_across_logout: true` in `config.yml`. Combine with `persist_across_restarts: true` if you also want them to survive restarts.
