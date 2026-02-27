# Nicked

**Packet-level nickname plugin with skin changing, a full developer API, and PlaceholderAPI support.**

[![Build](https://github.com/PharoGames/Nicked/actions/workflows/build.yml/badge.svg)](https://github.com/PharoGames/Nicked/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Spigot 1.20+](https://img.shields.io/badge/Spigot-1.20%2B-orange.svg)](https://www.spigotmc.org/)

Nicked lets operators and players adopt a completely different identity on the server — a different name *and* a different skin — without the real player ever being exposed to others. Everything is handled at the packet level, so the change is seamless and requires no client mods.

---

## Features

- **Packet-level nicking** — Names are rewritten inside `PLAYER_INFO_UPDATE` packets before they reach clients. No client mods needed. Works with 1.20+ Paper and Spigot servers.
- **Skin changing** — When a nick is applied, Nicked fetches the corresponding Mojang skin asynchronously and rewrites it in the same packet. The nicked player even sees their own new skin (optional, enabled by default).
- **Developer API** — A clean, well-documented Java API lets other plugins check nick state, apply or remove nicks programmatically, and listen to five purpose-built events.
- **PlaceholderAPI support** — Three placeholders (`%nicked_displayname%`, `%nicked_is_nicked%`, `%nicked_real_name%`) integrate with any chat, scoreboard, or tab-list plugin that supports PlaceholderAPI.
- **Persistence** — Nicks can survive player logouts and full server restarts. Persistent data is stored in `plugins/Nicked/nicks.yml` and restored automatically on join.
- **Customisable messages** — Every message is defined in `messages.yml` using MiniMessage format. Gradients, hex colours, hover events — anything MiniMessage supports is fair game.

---

## Requirements

| Requirement | Version |
|---|---|
| Java | 21 or newer |
| Spigot / Paper | 1.20 or newer |
| PacketEvents | Bundled (no separate installation needed) |

**Optional dependencies**

| Plugin | Purpose |
|---|---|
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | Enables `%nicked_*%` placeholders |

---

## Quick Start

1. Download the latest `Nicked.jar` from the [Releases](https://github.com/PharoGames/Nicked/releases) page.
2. Drop it into your server's `plugins/` folder.
3. Restart the server.
4. Grant `nicked.admin` to yourself (OPs have it by default).
5. Run `/nick Dream` — your name and skin change instantly.
6. Run `/nick` again (no arguments) or `/unnick` to revert.

---

## Commands

| Command | Description |
|---|---|
| `/nick [name]` | Nick yourself (or pick a random nick if no name given); toggle off if already nicked |
| `/unnick` | Remove your own nick |
| `/nickother <player> <name>` | Nick another player |
| `/nickall <name>` | Nick all online players |
| `/realname <name>` | Look up the real username behind a nick |

---

## Developer API

Add Nicked as a dependency via JitPack:

**Maven**
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>net.pharogames</groupId>
    <artifactId>Nicked</artifactId>
    <version>VERSION</version>
    <classifier>api</classifier>
    <scope>provided</scope>
</dependency>
```

**Gradle (Groovy)**
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'net.pharogames:Nicked:VERSION:api'
}
```

Replace `VERSION` with the latest release tag (e.g. `v1.0.0`).

See the [Developer API docs](https://pharogames.github.io/Nicked/api/) for the full reference.

---

## Documentation

Full documentation is available at: **[pharogames.github.io/Nicked](https://pharogames.github.io/Nicked)**

| Section | Contents |
|---|---|
| [Getting Started](https://pharogames.github.io/Nicked/getting-started/) | Installation walkthrough, first-run tips |
| [Commands](https://pharogames.github.io/Nicked/commands/) | Every command with arguments, examples, and edge cases |
| [Configuration](https://pharogames.github.io/Nicked/configuration/) | Full `config.yml` reference |
| [Messages](https://pharogames.github.io/Nicked/messages/) | Customising every plugin message |
| [Permissions](https://pharogames.github.io/Nicked/permissions/) | All permission nodes |
| [Developer API](https://pharogames.github.io/Nicked/api/) | Accessing the API, listening to events, code examples |
| [PlaceholderAPI](https://pharogames.github.io/Nicked/placeholderapi/) | All three placeholders and usage examples |

---

## Contributing

Found a bug or have a feature idea? [Open an issue](https://github.com/PharoGames/Nicked/issues/new/choose).

Want to contribute code? Read [CONTRIBUTING.md](.github/CONTRIBUTING.md) first.

---

## License

Nicked is licensed under the [MIT License](LICENSE).
