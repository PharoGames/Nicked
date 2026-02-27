![Nicked Banner](https://raw.githubusercontent.com/PharoGames/Nicked/main/images/banner.png)

# Nicked

**Packet-level nickname plugin with skin changing, a full developer API, and PlaceholderAPI support.**

[![Build](https://img.shields.io/github/actions/workflow/status/PharoGames/Nicked/build.yml?branch=main&label=Build&logo=github)](https://github.com/PharoGames/Nicked/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/PharoGames/Nicked/blob/main/LICENSE)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Servers: 1.20+](https://img.shields.io/badge/Minecraft-1.20%2B-brightgreen.svg)](https://papermc.io/)

Nicked lets operators and players adopt a completely different identity — a different **name** *and* a different **skin** — without the real player ever being exposed to others. Everything is handled at the packet level, so the illusion is seamless and requires no client-side mods.

---

## Features

- **Packet-level nicking** — Names are rewritten inside `PLAYER_INFO_UPDATE` packets before they reach any client. No client mods needed. Compatible with Paper, Spigot, Purpur, Folia, and Bukkit 1.20+.
- **Skin changing** — When a nick is applied, Nicked fetches the corresponding Mojang skin asynchronously and injects it into the same packet. The nicked player even sees their own new skin in first-person (configurable).
- **Random nicking** — Run `/nick` with no arguments to be assigned a name at random from your configured name pool.
- **Persistence** — Nicks survive player disconnects and full server restarts. Persistent data is stored in `plugins/Nicked/nicks.yml` and restored automatically on join.
- **Developer API** — A clean, well-documented Java API lets other plugins check nick state, apply or remove nicks programmatically, and listen to five purpose-built events (`NickApplyEvent`, `NickRemoveEvent`, `NickChangeEvent`, `NickRandomSelectEvent`, `NickResolveEvent`).
- **PlaceholderAPI support** — Three placeholders integrate with any chat, scoreboard, or tab-list plugin:
  - `%nicked_displayname%` — The player's current display name (nick or real name)
  - `%nicked_is_nicked%` — `true` / `false`
  - `%nicked_real_name%` — The player's actual username
- **Fully customisable messages** — Every message is defined in `messages.yml` using [MiniMessage](https://docs.advntr.dev/minimessage/format.html). Gradients, hex colours, hover events — anything MiniMessage supports is fair game.

---

## Compatibility

| Platform | Supported |
|---|---|
| Paper 1.20+ | ✅ |
| Spigot 1.20+ | ✅ |
| Purpur 1.20+ | ✅ |
| Folia 1.20+ | ✅ |
| Bukkit 1.20+ | ✅ |
| Java | 21+ required |

**Optional soft dependency:** [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi) — enables `%nicked_*%` placeholders.

---

## Commands

| Command | Description | Permission |
|---|---|---|
| `/nick [name]` | Nick yourself (random if no name given); toggle off if already nicked | `nicked.command.nick` |
| `/unnick` | Remove your own nick | `nicked.command.unnick` |
| `/nickother <player> <name>` | Nick another player to a specific name | `nicked.command.nickother` |
| `/nickall` | Randomly nick all online players | `nicked.command.nickall` |
| `/realname <name>` | Reveal the real username behind a nick | `nicked.command.realname` |

The `nicked.admin` permission grants access to all commands and is assigned to OPs by default.

---

## Quick Start

1. Drop `Nicked.jar` into your server's `plugins/` folder.
2. Restart the server — `plugins/Nicked/config.yml` and `messages.yml` will be generated.
3. Grant `nicked.admin` to trusted staff (OPs already have it).
4. Run `/nick Dream` — your name and skin change instantly.
5. Run `/nick` again with no arguments, or `/unnick`, to revert.

---

## Developer API

Add Nicked as a compile-only dependency via JitPack:

**Gradle (Groovy)**
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.PharoGames:Nicked:VERSION:api'
}
```

**Maven**
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
<dependency>
    <groupId>com.github.PharoGames</groupId>
    <artifactId>Nicked</artifactId>
    <version>VERSION</version>
    <classifier>api</classifier>
    <scope>provided</scope>
</dependency>
```

Replace `VERSION` with the latest release tag (e.g. `v1.0.0`). See the [API documentation](https://pharogames.github.io/Nicked/api/) for the full reference.

---

## Links

| Resource | URL |
|---|---|
| 📖 Documentation | [pharogames.github.io/Nicked](https://pharogames.github.io/Nicked) |
| 💬 Discord | [discord.gg/7eQt8sQ8at](https://discord.gg/7eQt8sQ8at) |
| 🐛 Issues | [GitHub Issues](https://github.com/PharoGames/Nicked/issues) |
| 💡 Feature Requests | [GitHub Issues](https://github.com/PharoGames/Nicked/issues/new/choose) |
| 🔧 Source Code | [github.com/PharoGames/Nicked](https://github.com/PharoGames/Nicked) |
| 📦 Developer API | [API Docs](https://pharogames.github.io/Nicked/api/) |

---

## License

Nicked is licensed under the [MIT License](https://github.com/PharoGames/Nicked/blob/main/LICENSE).
